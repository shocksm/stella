package org.stellasql.stella.connection;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stellasql.stella.export.ExportOptions;
import org.stellasql.stella.gui.ResultData;
import org.stellasql.stella.query.QueryParser;
import org.stellasql.stella.query.Table;
import org.stellasql.stella.session.SessionData;

public class QuerySequencer
{
  private final static Logger logger = LogManager.getLogger(QuerySequencer.class);

  private ConnectionManager connectionManager = null;
  private LinkedList workQueue = new LinkedList();
  private LinkedList queryTransactionListener = new LinkedList();
  private SessionData sessionData = null;
  private Thread thread = null;
  private boolean shutdown = false;

  public QuerySequencer(ConnectionManager connectionManager, SessionData sessionData)
  {
    this.connectionManager = connectionManager;
    this.sessionData = sessionData;
  }

  public void setCatalog(String catalog)
  {
    synchronized (workQueue)
    {
      workQueue.add(new CatalogChange(catalog));
      getToWork();
    }
  }

  public void addQueryTransactionListener(QueryTransactionListener listener)
  {
    queryTransactionListener.add(listener);
  }

  private void queryRollback()
  {
    for (Iterator it = queryTransactionListener.iterator(); it.hasNext();)
    {
      QueryTransactionListener listener = (QueryTransactionListener)it.next();
      listener.queryRollback();
    }
  }

  private void queryCommit()
  {
    for (Iterator it = queryTransactionListener.iterator(); it.hasNext();)
    {
      QueryTransactionListener listener = (QueryTransactionListener)it.next();
      listener.queryCommit();
    }
  }

  private void querySuccess()
  {
    for (Iterator it = queryTransactionListener.iterator(); it.hasNext();)
    {
      QueryTransactionListener listener = (QueryTransactionListener)it.next();
      listener.querySuccess();
    }
  }

  public Object runQuery(QueryListener listener, String queryText, Object data, int maxResults, boolean transaction, ExportOptions eo)
  {
    Query query = null;
    synchronized (workQueue)
    {
      query = new Query(listener, queryText, data, maxResults, transaction, eo);
      workQueue.add(query);
      getToWork();
    }

    return query;
  }

  public void cancelQueries(List keyList)
  {
    synchronized (workQueue)
    {
      for (Iterator it = keyList.iterator(); it.hasNext();)
      {
        Object key = it.next();
        Query query = (Query)key;
        try
        {
          query.cancel();
        }
        catch (SQLException e)
        {
          logger.info("Exception caused from canceling query: " + e.getMessage(), e);
        }

        if (query.isCanceled())
          query.getListener().queryCanceled(query.getQuery(), query.getData(), query);
      }
    }
  }
/*
  public void cancelQuery(Object runKey)
  {
    Query query = (Query)runKey;
    synchronized (workQueue)
    {
      try
      {
        query.cancel();
      }
      catch (SQLException e)
      {
        logger.info("Exception caused from canceling query: " + e.getMessage(), e);
      }

      if (query.isCanceled())
        query.getListener().queryCanceled(query.getQuery(), query.getData(), query);
    }
  }
*/
  public void shutdown()
  {
    synchronized (workQueue)
    {
      shutdown = true;
      workQueue.notify();
    }
  }

  private void getToWork()
  {
    synchronized (workQueue)
    {
      if (thread == null)
      {
        thread = new Thread(new WorkerRunnable());
        thread.setDaemon(true);
        thread.start();
      }
      else
        workQueue.notify();
    }
  }

  private void process()
  {
    Object item = null;
    synchronized (workQueue)
    {
      if (workQueue.size() > 0)
        item = workQueue.removeFirst();
    }

    if (item == null)
      return;

    if (item instanceof CatalogChange)
    {
      changeCatalog((CatalogChange)item);
    }
    else if (item instanceof Query)
    {
      runQuery((Query)item);
    }
  }

  private void changeCatalog(CatalogChange change)
  {
    Connection con = connectionManager.getConnection();

    try
    {
      con.setCatalog(change.getCatalog());
    }
    catch (SQLException e)
    {
      logger.info(e.getMessage(), e);
      sessionData.addErrorMessage(e.getMessage(), false);
    }
    catch (Error e)
    {
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
    }
    finally
    {
      connectionManager.releaseConnection(true);
    }
  }

  private void runQuery(Query query)
  {
    Connection con = connectionManager.getConnection();
    List resultsList = new LinkedList();
    boolean valid = true;
    long queryTime = 0;
    long formatTime = 0;
    int exported = 0;

    boolean commit = false;
    boolean rollback = false;
    boolean success = false;

    query.getListener().queryBeingExecuted(query.getQuery(), query.getData(), query);

    try
    {
      String queryText = query.getQuery();
      Statement stmt = con.createStatement(); // don't use prepared statments - can cause problems with stored procs

      try
      {
        ResultSet rs = null;
        int count = -1;
        query.setStatment(stmt);

        if (!query.isCanceled())
        {
          QueryParser queryParser = new QueryParser(connectionManager.getSeparator(), connectionManager.getIdentifierQuote(), connectionManager.getUseSchemas());
          queryParser.parseQuery(queryText);
          if (sessionData.getStripComments())
            queryText = queryParser.removeComments();
          logger.debug("Query: " + queryText);
          logger.debug("Query Type: " + queryParser.getQueryType());

          // only set this for selects, otherwise it will also effect updates and deletes
          if (queryParser.getQueryType() == QueryParser.QUERY_TYPE_SELECT)
            stmt.setMaxRows(query.getMaxResults());

          if (query.getTransaction())
          {
            if (queryParser.getQueryType() == QueryParser.QUERY_TYPE_COMMIT)
              commit = true;
            else if (queryParser.getQueryType() == QueryParser.QUERY_TYPE_ROLLBACK)
              rollback = true;
          }

          long startMillis = System.currentTimeMillis();
          boolean rsReturned = stmt.execute(queryText);

          String warning = ConnectionManager.getWarningString(stmt.getWarnings());
          if (warning != null)
          {
            String msg = "Query executed with warnings: " + warning;
            sessionData.addMessage(msg, false);
            logger.info(msg);
          }

          count = stmt.getUpdateCount();
          if (rsReturned)
            rs = stmt.getResultSet();
          if (query.getTransaction())
            success = true;
          long stopMillis = System.currentTimeMillis();
          queryTime = stopMillis - startMillis;

          startMillis = System.currentTimeMillis();

          while (rs != null || count != -1)
          {
            if (count != -1)
            {
              /* MS SQL Server will return a 0 count on select statements
               * so this is needed to filter those out
               */
              if (count != 0 || queryParser.getQueryType() != QueryParser.QUERY_TYPE_SELECT)
              {
                resultsList.add(new Integer(count));
              }
            }

            if (rs != null)
            {
              if (query.getExportOptions() == null)
                resultsList.add(processResultSet(rs, queryText, queryParser));
              else
              {
                resultsList.add(query.getExportOptions().exportResultSet(rs, queryText, query.getMaxResults(), exported++));
              }
            }

            rs = null;
            count = -1;

            rsReturned = stmt.getMoreResults();
            count = stmt.getUpdateCount();
            if (rsReturned || count != -1) // this call will close the last ResultSet
            {
              rs = stmt.getResultSet();
            }
          }

          stopMillis = System.currentTimeMillis();
          formatTime = stopMillis - startMillis;
        }

        query.setStatment(null);
      }
      catch (Exception e)
      {
        valid = false;
        if (!query.isCanceled())
        {
          logger.info(e.getMessage(), e);
          String msg = e.getMessage();

          if (e.getClass().getName().startsWith("com.ibm.db2")) {
            // poor sod is using ibm db2, lets have pity on them

            // ibm code is disappointing :( so we have to bend over backwards to get a useful error message
            try {
              Method method = e.getClass().getMethod("getSqlca", null);
              if (method != null) {
                Object obj = method.invoke(e, null);
                method = obj.getClass().getMethod("getMessage", null);
                Object ibmissubpar = method.invoke(obj, null);
                if (ibmissubpar instanceof String) {
                  msg = (String) ibmissubpar;
                }
              }
            }
            catch (Exception ex) {
              logger.warn(ex.getMessage(), ex);
            }
          }

          if (msg == null)
            msg = e.getClass().getName();
          resultsList.add(new QueryError(msg));
        }
        else
        {
          logger.info("Exception caused from canceled query: " + e.getMessage(), e);
        }
      }
      catch (Error e)
      {
        valid = false;
        if (!query.isCanceled())
        {
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        else
        {
          logger.info("Error caused from canceled query: " + e.getMessage(), e);
        }
      }

      stmt.close();
    }
    catch (Exception e)
    {
      if (!query.isCanceled())
      {
        logger.error(e.getMessage(), e);
        sessionData.addErrorMessage(e.getMessage(), false);
      }
      else
      {
        logger.info("Exception caused from canceled query: " + e.getMessage(), e);
      }
    }
    finally
    {
      connectionManager.releaseConnection(false);
    }

    query.setComplete(true);
    if (query.isCanceled())
      query.getListener().queryCanceled(query.getQuery(), query.getData(), query);
    else
      query.getListener().queryRan(query.getQuery(), valid, query.getData(), query, resultsList, query.getMaxResults(), queryTime, formatTime);

    if (commit)
      queryCommit();
    else if (rollback)
      queryRollback();
    else if (success)
      querySuccess();
  }

  public static Object getColumnValue(int sqlType, ResultSet rs, int columnIndex) throws SQLException
  {

    Object obj = null;
    if (sqlType == Types.DATE) /* oracle uses a Timstamp here, yep not a Date or
                                  oracle Timestamp. getObject will return Date for other DBs or
                                  Timestamp for Oracle
                                  note: that above is prior to oracle 9 jdbc drivers
                                      the oracle 10 drivers do return a date now
                                      which has messed everything all up - have to set a
                                      system property -Doracle.jdbc.V8Compatible=true to get
                                      the driver to work the old way (oracle docs are wrong
                                      on the property name.  decompile oracle.jdbc.driver.OracleDriver
                                      and search for V8Compat to find where it is read in)
                                      note: with that set the sqlType will == TIMESTAMP and
                                      not date like it use to with the older driver */
    {
      obj = rs.getObject(columnIndex);
    }
    else if (sqlType == Types.TIMESTAMP)
      obj = rs.getTimestamp(columnIndex); // oracle returns as an oracle class (oracle.sql.TIMESTAMP), so getting object won't work
    else if (sqlType == Types.TIME)
      obj = rs.getTime(columnIndex);
    else
    {
      obj = rs.getObject(columnIndex);

      // kludge to work around oracle odd drivers
      if (obj != null)
      {
        String classname = obj.getClass().getName().toLowerCase();
        if (classname.indexOf("oracle") >= 0
            && classname.indexOf("timestamp") >= 0)
        {
          obj = rs.getTimestamp(columnIndex); /* oracle jdbc drivers :(
                                                 doing a 'select systimestamp from dual' returns
                                                 a sqlType of -101 which is not defined */
        }
        else if (classname.indexOf("oracle") >= 0
            && classname.indexOf("rowid") >= 0)
        {
          obj = rs.getString(columnIndex); /* oracle returns ROWID as a special class that
                                              like the other oracle classes returns the classname
                                              instead of a meaningful value when the toString()
                                              method is called. calling rs.getString on a ROWID
                                              will return a real value */
        }
      }
    }

    return obj;
  }

  private ResultData processResultSet(ResultSet rs, String queryText, QueryParser queryParser) throws SQLException
  {
    ResultSetMetaData rsmd = rs.getMetaData();
    ResultData rd = new ResultData(rsmd, queryText);

    int count = 0;
    while (rs.next())
    {
      Object[] objArray = new Object[rsmd.getColumnCount() + 1];
      for (int index = 1; index <= rsmd.getColumnCount(); index++)
      {
        int type = rd.getSqlType(index-1);
        try
        {
          objArray[index-1] = getColumnValue(type, rs, index);
        }
        catch (SQLException e)
        {
          logger.error("Error getting column value", e);
        }
      }
      objArray[rsmd.getColumnCount()] = new Integer(count++);
      rd.addRow(objArray);
    }

    if (queryParser.getQueryType() == QueryParser.QUERY_TYPE_SELECT)
    {
      List tableList = queryParser.getTables();
      // only editable if one table was selected from
      // otherwise it gets way too messy trying to find out what
      // table a column came from
      if (tableList != null && tableList.size() == 1)
      {
        Table table = (Table)tableList.get(0);
        if (table.catalog == null && sessionData.getConnectionManager().getUseCatalogs())
        {
          table.catalog = sessionData.getConnectionManager().getCurrentCatalog();
        }

        rd.setTable(table.table, table.catalog, table.schema, connectionManager);
      }
    }

    return rd;
  }

  private class Query
  {
    private QueryListener listener = null;
    private String query = null;
    private Object data = null;
    private Statement stmt = null;
    private boolean canceled = false;
    private int maxResults = -1;
    private boolean complete = false;
    private boolean transaction = false;
    private ExportOptions exportOptions = null;

    public Query(QueryListener listener, String query, Object data, int maxResults, boolean transaction, ExportOptions eo)
    {
      this.listener = listener;
      this.query = query;
      this.data = data;
      this.maxResults  = maxResults;
      this.transaction = transaction;
      exportOptions = eo;
    }

    public ExportOptions getExportOptions()
    {
      return exportOptions;
    }

    public boolean getTransaction()
    {
      return transaction;
    }

    public synchronized void setComplete(boolean complete)
    {
      this.complete = complete;
    }

    public synchronized boolean isCanceled()
    {
      return canceled;
    }

    public synchronized void setStatment(Statement stmt)
    {
      this.stmt = stmt;
    }

    public synchronized void cancel() throws SQLException
    {
      if (!complete)
      {
        canceled = true;

        if (workQueue.contains(this))
        {
          workQueue.remove(this);
        }
        else if (stmt != null)
        {
          //stmt.cancel(); oracle jdbc drivers don't work with cancel.  After cancel they continue to cancel later statements ocasionaly
          stmt.close();
        }
      }
    }

    public Object getData()
    {
      return data;
    }

    public QueryListener getListener()
    {
      return listener;
    }

    public int getMaxResults()
    {
      return maxResults;
    }

    public String getQuery()
    {
      return query;
    }
  }

  private class CatalogChange
  {
    private String catalog = "";
    public CatalogChange(String catalog)
    {
      this.catalog = catalog;
    }

    public String getCatalog()
    {
      return catalog;
    }
  }

  private class WorkerRunnable implements Runnable
  {
    @Override
    public void run()
    {
      logger.debug("worker starting");
      while (!shutdown)
      {
        try
        {
          process();
          //Thread.yield();
        }
        catch (Exception e)
        {
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        catch (Error e)
        {
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }

        synchronized (workQueue)
        {
          while (workQueue.size() == 0 && !shutdown)
          {
            try
            {
              workQueue.wait();
            }
            catch (InterruptedException e)
            {
            }
          }
        }
      }
      logger.debug("worker exiting");
    }
  }


}
