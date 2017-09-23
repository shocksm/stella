package org.stellasql.stella.session;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.IndexInfo;
import org.stellasql.stella.PrimaryKeyInfo;
import org.stellasql.stella.ProcedureColumnInfo;
import org.stellasql.stella.ProcedureInfo;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.connection.ConnectionManager;

public class DBObjectRetriever
{
  private final static Logger logger = LogManager.getLogger(DBObjectRetriever.class);
  private SessionData sessionData = null;
  private DBObjectListener listener = null;

  public DBObjectRetriever(SessionData sessionData)
  {
    this.sessionData = sessionData;
  }

  private ConnectionManager getConnectionManager()
  {
    return sessionData.getConnectionManager();
  }

  public void getTables(final String catalog, final String tableType)
  {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run()
      {
        ConnectionManager conMan = getConnectionManager();
        try
        {
          Connection con = conMan.getConnection();
          List list = getTablesForType(con, tableType, catalog);
          listener.tableDataAvailable(catalog, tableType, list);
        }
        catch (Exception e)
        {
          listener.tableDataAvailable(catalog, tableType, null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        catch (Error e)
        {
          listener.tableDataAvailable(catalog, tableType, null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        finally
        {
          conMan.releaseConnection();
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  public void getProcedures(final String catalog)
  {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run()
      {
        ConnectionManager conMan = getConnectionManager();
        try
        {
          Connection con = conMan.getConnection();
          List list = getProcedures(con, catalog);
          listener.procedureDataAvailable(catalog, list);
        }
        catch (Exception e)
        {
          listener.procedureDataAvailable(catalog, null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        catch (Error e)
        {
          listener.procedureDataAvailable(catalog, null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        finally
        {
          conMan.releaseConnection();
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  private List getProcedures(Connection con, String catalog) throws SQLException
  {
    List list = new LinkedList();

    ResultSet rs = null;
    try
    {
      boolean useCatalogs = sessionData.getConnectionManager().getUseCatalogs();
      boolean useSchemas = sessionData.getConnectionManager().getUseSchemas();
      String seperator = sessionData.getConnectionManager().getSeparator();

      rs = con.getMetaData().getProcedures(catalog, null, null);
      while (rs.next())
      {
        ProcedureInfo proc = new ProcedureInfo(rs.getString("PROCEDURE_NAME"), catalog, rs.getString("PROCEDURE_SCHEM"), rs.getString("REMARKS"), useCatalogs, useSchemas, seperator);

        list.add(proc);
      }
    }
    catch (SQLException e)
    {
      sessionData.addErrorMessage(e.getMessage(), false);
      logger.error(e.getMessage(), e);
      list = null;
    }
    finally
    {
      if (rs != null)
        rs.close();
    }

    return list;
  }

  public void getColumns(final TableInfo tableInfo)
  {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run()
      {
        ConnectionManager conMan = getConnectionManager();
        try
        {
          Connection con = conMan.getConnection();
          getColumns(con, tableInfo);
          listener.columnDataAvailable(tableInfo);
        }
        catch (Exception e)
        {
          listener.columnDataAvailable(null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        catch (Error e)
        {
          listener.columnDataAvailable(null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        finally
        {
          conMan.releaseConnection();
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  public void getProcedureColumns(final ProcedureInfo procInfo)
  {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run()
      {
        ConnectionManager conMan = getConnectionManager();
        try
        {
          Connection con = conMan.getConnection();
          getProcedureColumns(con, procInfo);
          listener.procedureColumnDataAvailable(procInfo);
        }
        catch (Exception e)
        {
          listener.procedureColumnDataAvailable(null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        catch (Error e)
        {
          listener.procedureColumnDataAvailable(null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        finally
        {
          conMan.releaseConnection();
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  public void getTableInfo(final TableInfo tableInfo)
  {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run()
      {
        ConnectionManager conMan = getConnectionManager();
        try
        {
          Connection con = conMan.getConnection();
          getFullTableInfo(con, tableInfo, true);
          listener.tableDetailDataAvailable(tableInfo);
        }
        catch (Exception e)
        {
          listener.tableDetailDataAvailable(null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        catch (Error e)
        {
          listener.tableDetailDataAvailable(null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        finally
        {
          conMan.releaseConnection();
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  public void getTableInfoSync(TableInfo tableInfo)
  {
    ConnectionManager conMan = getConnectionManager();
    try
    {
      Connection con = conMan.getConnection();
      getFullTableInfo(con, tableInfo, false);
    }
    catch (Exception e)
    {
      listener.tableDetailDataAvailable(null);
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
    }
    catch (Error e)
    {
      listener.tableDetailDataAvailable(null);
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
    }
    finally
    {
      conMan.releaseConnection();
    }
  }

  public void getCatalogs()
  {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run()
      {
        ConnectionManager conMan = getConnectionManager();
        try
        {
          Connection con = conMan.getConnection();
          List catList = null;
          List typesList = getTableTypes(con);
          if (conMan.getUseCatalogs())
            catList = getCatalogs(con);
          else
            catList = new LinkedList();
          listener.catalogDataAvailable(typesList, catList);
        }
        catch (Exception e)
        {
          listener.catalogDataAvailable(null, null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        catch (Error e)
        {
          listener.catalogDataAvailable(null, null);
          logger.error(e.getMessage(), e);
          sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
        }
        finally
        {
          conMan.releaseConnection();
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  public void start()
  {
    getCatalogs();

    /*

    Thread thread = new Thread(new Runnable() {
      public void run()
      {
        ConnectionManager conMan = getConnectionManager();
        try
        {
          Connection con = conMan.getConnection();

          List catList = null;
          List typesList = getTableTypes(con);
          if (conMan.getUseCatalogs())
            catList = getCatalogs(con);
          else
            catList = new LinkedList();
          listener.catalogDataAvailable(typesList, catList);

          /*
          if (catalogList.size() == 0)
          {
            getTables(con, null);
          }
          else
          {
            for (Iterator it = catalogList.iterator(); it.hasNext();)
            {
              String catalog = (String)it.next();
              try
              {
                getTables(con, catalog);
              }
              catch (SQLException e)
              {
                sessionData.addErrorMessage(e.getMessage());
              }
            }
          }

          if (catalogList.size() == 0)
          {
            Map typeMap = (Map)catalogTypeMap.get(null);
            for (Iterator it = tableTypesList.iterator(); it.hasNext();)
            {
              String tableType = (String)it.next();
              List tables = (List)typeMap.get(tableType);
              if (tables != null && tables.size() > 0)
              {
                for (Iterator tableIt = tables.iterator(); tableIt.hasNext();)
                {
                  TableInfo tableInfo = (TableInfo)tableIt.next();
                  getColumns(con, tableInfo);
                  listener.columnDataAvailable(tableInfo);
                }
              }
            }
          }
          */

    /*

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
        finally
        {
          conMan.releaseConnection();
        }
      }});

    thread.setDaemon(true);
    thread.start();
    */
  }

  private void getFullTableInfo(Connection con, TableInfo tableInfo, boolean notifyListener) throws SQLException
  {
    ResultSet rs = null;

    tableInfo.setFullyLoaded(true);

    try
    {
      if (!tableInfo.getColumnsLoaded())
      {
        getColumns(con, tableInfo);
        if (notifyListener)
          listener.columnDataAvailable(tableInfo);
      }
    }
    catch (SQLException e)
    {
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage("Can't get column info: " + e.getMessage(), false);
    }
    catch (Error e)
    {
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
    }
    finally
    {
      if (rs != null)
        rs.close();
      rs = null;
    }

    try
    {
      int count = 0;
      rs = con.getMetaData().getIndexInfo(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName(), false, false);
      while (rs.next())
      {
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setNonUnique(rs.getBoolean("NON_UNIQUE"));
        indexInfo.setQualifier(rs.getString("INDEX_QUALIFIER"));
        indexInfo.setName(rs.getString("INDEX_NAME"));
        indexInfo.setType(rs.getInt("TYPE"));
        indexInfo.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
        indexInfo.setColumnName(rs.getString("COLUMN_NAME"));
        indexInfo.setAscOrDesc(rs.getString("ASC_OR_DESC"));
        indexInfo.setCardinality(rs.getInt("CARDINALITY"));
        indexInfo.setPages(rs.getInt("PAGES"));
        indexInfo.setFilterCondition(rs.getString("FILTER_CONDITION"));
        indexInfo.setOrderIndex(count++);
        tableInfo.addIndex(indexInfo);
      }
    }
    catch (SQLException e)
    {
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage("Can't get index info: " + e.getMessage(), false);
    }
    catch (Error e)
    {
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
    }
    finally
    {
      if (rs != null)
        rs.close();
      rs = null;
    }

    try
    {
      int count = 0;
      rs = con.getMetaData().getPrimaryKeys(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName());
      while (rs.next())
      {
        PrimaryKeyInfo pkInfo = new PrimaryKeyInfo();
        pkInfo.setColumnName(rs.getString("COLUMN_NAME"));
        pkInfo.setKeySeq(rs.getInt("KEY_SEQ"));
        pkInfo.setPkName(rs.getString("PK_NAME"));
        pkInfo.setOrderIndex(count++);
        tableInfo.addPrimaryKey(pkInfo);
      }
    }
    catch (SQLException e)
    {
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage("Can't get primary key info: " + e.getMessage(), false);
    }
    catch (Error e)
    {
      logger.error(e.getMessage(), e);
      sessionData.addErrorMessage(e.getClass().getName() + ": " + e.getMessage(), false);
    }
    finally
    {
      if (rs != null)
        rs.close();
      rs = null;
    }
  }

  /*
  private void getTables(Connection con, String catalog) throws SQLException
  {
    for (Iterator it = tableTypesList.iterator(); it.hasNext();)
    {
      String tableType = (String)it.next();
      List list = getTablesForType(con, tableType, catalog);
      listener.tableDataAvailable(catalog, tableType, list);
    }
  }
  */

  private void getColumns(Connection con, TableInfo tableInfo) throws SQLException
  {
    ResultSet rs = null;
    try
    {
      int count = 0;
      rs = con.getMetaData().getColumns(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName(), "%");
      while (rs.next())
      {
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setColumnName(rs.getString("COLUMN_NAME"));
        columnInfo.setDataType(rs.getInt("DATA_TYPE"));
        columnInfo.setTypeName(rs.getString("TYPE_NAME"));
        columnInfo.setDefault(rs.getString("COLUMN_DEF"));
        columnInfo.setColumnSize(rs.getInt("COLUMN_SIZE"));
        columnInfo.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
        columnInfo.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
        columnInfo.setOrderIndex(count++);

        tableInfo.addColumn(columnInfo);
      }
      tableInfo.setColumnsLoaded(true);
    }
    finally
    {
      if (rs != null)
        rs.close();
    }
  }

  private void getProcedureColumns(Connection con, ProcedureInfo procInfo) throws SQLException
  {
    ResultSet rs = null;
    try
    {
      int count = 0;
      rs = con.getMetaData().getProcedureColumns(procInfo.getCatalog(), procInfo.getSchema(), procInfo.getName(), "%");
      while (rs.next())
      {
        ProcedureColumnInfo columnInfo = new ProcedureColumnInfo();
        columnInfo.setColumnName(rs.getString("COLUMN_NAME"));
        if (columnInfo.getColumnName() == null)
          columnInfo.setColumnName("");
        int type = rs.getInt("COLUMN_TYPE");
        columnInfo.setDataType(rs.getInt("DATA_TYPE"));
        columnInfo.setTypeName(rs.getString("TYPE_NAME"));
        columnInfo.setLength(rs.getInt("LENGTH"));
        columnInfo.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.procedureNullable );
        columnInfo.setTypeName(rs.getString("REMARKS"));
        columnInfo.setOrderIndex(count++);

        // TODO
        //COLUMN_TYPE
        if (type == DatabaseMetaData.procedureColumnUnknown)
          columnInfo.setTypeName("Unknown");
        else if (type == DatabaseMetaData.procedureColumnIn)
          columnInfo.setTypeName("IN parameter");
        else if (type == DatabaseMetaData.procedureColumnInOut)
          columnInfo.setTypeName("INOUT parameter");
        else if (type == DatabaseMetaData.procedureColumnOut)
          columnInfo.setTypeName("OUT parameter");
        else if (type == DatabaseMetaData.procedureColumnReturn)
          columnInfo.setTypeName("return value");
        else if (type == DatabaseMetaData.procedureColumnResult)
          columnInfo.setTypeName("result");

        procInfo.addColumn(columnInfo);
      }
      procInfo.setColumnsLoaded(true);
    }
    finally
    {
      if (rs != null)
        rs.close();
    }
  }

  private List getTablesForType(Connection con, String tableType, String catalog) throws SQLException
  {
    List list = new LinkedList();

    ResultSet rs = null;
    try
    {
      boolean useCatalogs = sessionData.getConnectionManager().getUseCatalogs();
      boolean useSchemas = sessionData.getConnectionManager().getUseSchemas();
      String seperator = sessionData.getConnectionManager().getSeparator();

      String types[] = {tableType};
      rs = con.getMetaData().getTables(catalog, null, null, types);
      while (rs.next())
      {
        TableInfo table = new TableInfo(rs.getString("TABLE_NAME"), catalog, rs.getString("TABLE_SCHEM"), rs.getString("TABLE_TYPE"), useCatalogs, useSchemas, seperator);

        list.add(table);
      }
    }
    catch (SQLException e)
    {
      sessionData.addErrorMessage(e.getMessage(), false);
      logger.error(e.getMessage(), e);
      list = null;
    }
    finally
    {
      if (rs != null)
        rs.close();
    }

    return list;
  }

  private List getTableTypes(Connection con) throws SQLException
  {
    List list = new LinkedList();
    ResultSet rs = null;
    try
    {
      rs = con.getMetaData().getTableTypes();
      while (rs.next())
      {
        String typeName = rs.getString("TABLE_TYPE").trim();
        // exclude synonym - from oracle and long list of non-tables
        if (!typeName.equalsIgnoreCase("synonym"))
          list.add(typeName);
      }
    }
    finally
    {
      if (rs != null)
        rs.close();
    }

    return list;
  }

  private List getCatalogs(Connection con) throws SQLException
  {
    List list = new LinkedList();
    ResultSet rs = null;

    try
    {
      rs = con.getMetaData().getCatalogs();
      while (rs.next())
      {
        String cat = rs.getString("TABLE_CAT");
        list.add(cat);
      }
    }
    finally
    {
      if (rs != null)
        rs.close();
    }

    return list;
  }

  public void addListener(DBObjectListener listener)
  {
    this.listener = listener;
  }

}
