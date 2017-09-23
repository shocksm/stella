package org.stellasql.stella.session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.stellasql.stella.AliasChangeListener;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.connection.ConnectionManager;
import org.stellasql.stella.connection.QuerySequencer;
import org.stellasql.stella.gui.QueryComposite;

public class SessionData implements AliasChangeListener
{
  private static HashMap map = new HashMap();

  private AliasVO aliasVO = null;
  private List sessionPreparingToEndListenerList = new LinkedList();
  private List sessionEndListenerList = new LinkedList();
  private List sessionReadyListenerList = new LinkedList();
  private List messageHandlerList = new LinkedList();
  private String sessionName = "";
  private ConnectionManager connectionManager = null;
  private QuerySequencer querySequencer = null;
  private DBObjectRetriever dbObjectRetriever = null;
  private LinkedList queryTextListenerList = new LinkedList();
  private QueryComposite queryComposite = null;
  private Control sqlControl = null;
  private CTabFolder resultsTab = null;
  private boolean autoCommit = true;
  private boolean limitResults = true;
  private int maxRows = 100;
  private boolean ended = false;
  private String querySeparator = "go";
  private LinkedList querySeparatorChangeListenerList = new LinkedList();
  private SQLActionHandler sqlActionHandler = null;
  private SQLTextHandler sqlTextHandler = null;
  private SQLResultHandler sqlResultHandler = null;
  private boolean stripComments = false;
  private String exportFormat = null;
  private String exportFile = null;
  private boolean exportInlcudeColumnName = true;
  private boolean exportInlcudeSql = false;
  private String exportDateFormat = null;
  private String exportTimeFormat = null;
  private String exportTextDelimiter = null;
  private boolean exportExcelSplitWorksheet = true;
  private int exportExcelSplitRowCount = 65535;

  public String getExportFormat()
  {
    return exportFormat;
  }

  public void setExportFormat(String exportFormat)
  {
    this.exportFormat = exportFormat;
  }

  private SessionData(AliasVO aliasVO, String sessionName, String username, String password, boolean autoCommit, boolean limitResults, int maxRows, String querySeparator, boolean stripComments)
  {
    this.sessionName = sessionName;
    this.aliasVO = aliasVO;
    ApplicationData.getInstance().addAliasChangeListener(this);
    connectionManager = new ConnectionManager(this, this.aliasVO, username, password);
    querySequencer = new QuerySequencer(connectionManager, this);
    dbObjectRetriever = new DBObjectRetriever(this);

    this.autoCommit = autoCommit;
    this.limitResults = limitResults;
    this.maxRows = maxRows;
    this.querySeparator = querySeparator;
    this.stripComments = stripComments;
  }

  public static synchronized SessionData createSessionData(AliasVO aliasVO, String sessionName, String username, String password, boolean autoCommit, boolean limitResults, int maxRows, String querySeparator, boolean stripComments)
  {
    SessionData sd = new SessionData(aliasVO, sessionName, username, password, autoCommit, limitResults, maxRows, querySeparator, stripComments);
    map.put(sessionName, sd);

    return sd;
  }

  public void addQuerySeparatorChangeListener(QuerySeparatorChangeListener listener)
  {
    querySeparatorChangeListenerList.add(listener);
  }

  public void removeQuerySeparatorChangeListener(QuerySeparatorChangeListener listener)
  {
    querySeparatorChangeListenerList.remove(listener);
  }

  public void setQuerySeparator(String querySeparator)
  {
    this.querySeparator = querySeparator;
    for (Iterator it = querySeparatorChangeListenerList.iterator(); it.hasNext();)
    {
      QuerySeparatorChangeListener listener = (QuerySeparatorChangeListener)it.next();
      listener.querySeparatorChanged(this.querySeparator);
    }
  }

  public String getQuerySeparator()
  {
    return querySeparator;
  }

  public boolean getAutoCommit()
  {
    return autoCommit;
  }

  public boolean getStripComments()
  {
    return stripComments;
  }

  public void setStripComments(boolean stripComments)
  {
    this.stripComments = stripComments;
  }

  public boolean getLimitResults()
  {
    return limitResults;
  }

  public void setLimitResults(boolean limit)
  {
    limitResults = limit;
  }

  public int getMaxRows()
  {
    return maxRows;
  }

  public void setMaxRows(int max)
  {
    maxRows = max;
  }

  public void runQuery(String query, int maxResults, boolean transaction)
  {
    queryComposite.runQuery(query, maxResults, transaction);
  }

  public void setQueryHandler(QueryComposite queryComposite)
  {
    this.queryComposite = queryComposite;
  }

  public void connectionOpened(String warningMessage)
  {
    queryComposite.connectionOpened();
    if (warningMessage != null)
      addSuccessMessage("Connection opened with warning: " + warningMessage, false);
    else
      addSuccessMessage("Connection opened", false);
  }

  public void connectionClosed()
  {
    addMessage("Connection closed", false);
  }

  public void setSQLActionHandler(SQLActionHandler sqlActionHandler)
  {
    this.sqlActionHandler = sqlActionHandler;
  }

  public SQLActionHandler getSQLActionHandler()
  {
    return sqlActionHandler;
  }

  public void setSQLTextHandler(SQLTextHandler sqlLTextHandler)
  {
    sqlTextHandler = sqlLTextHandler;
  }

  public SQLTextHandler getSQLTextHandler()
  {
    return sqlTextHandler;
  }

  public void setSQLResultHandler(SQLResultHandler sqlResultHandler)
  {
    this.sqlResultHandler = sqlResultHandler;
  }

  public SQLResultHandler getSQLResultHandler()
  {
    return sqlResultHandler;
  }

  public void setSQLControl(Control control)
  {
    sqlControl = control;
  }

  public Control getSQLControl()
  {
    return sqlControl;
  }

  public void setResultsTab(CTabFolder tabFolder)
  {
    resultsTab = tabFolder;
  }

  public CTabFolder getResultsTab()
  {
    return resultsTab;
  }

  public QuerySequencer getQuerySequencer()
  {
    return querySequencer;
  }

  public void sessionEnded()
  {
    ended = true;
    querySequencer.shutdown();

    ApplicationData.getInstance().removeAliasChangeListener(this);
    for (Iterator it = sessionEndListenerList.iterator(); it.hasNext();)
    {
      SessionEndListener listener = (SessionEndListener)it.next();
      listener.sessionEnded();
    }
    removeSessionData(sessionName);
  }

  public void addSessionEndListener(SessionEndListener listener)
  {
    sessionEndListenerList.add(listener);
  }

  public boolean sessionPreparingToEnd()
  {
    for (Iterator it = sessionPreparingToEndListenerList.iterator(); it.hasNext();)
    {
      SessionPreparingToEndListener listener = (SessionPreparingToEndListener)it.next();
      if (!listener.sessionPreparingToEnd())
        return false;
    }

    return true;
  }

  public void addSessionPreparingToEndListener(SessionPreparingToEndListener listener)
  {
    sessionPreparingToEndListenerList.add(listener);
  }

  public void sessionReady()
  {
    ApplicationData.getInstance().removeAliasChangeListener(this);
    for (Iterator it = sessionReadyListenerList.iterator(); it.hasNext();)
    {
      SessionReadyListener listener = (SessionReadyListener)it.next();
      listener.sessionReady();
    }

    dbObjectRetriever.start();
  }

  public void addSessionReadyListener(SessionReadyListener listener)
  {
    sessionReadyListenerList.add(listener);
  }

  private synchronized void removeSessionData(String sessionName)
  {
    map.remove(sessionName);
  }

  public static synchronized SessionData getSessionData(String sessionName)
  {
    SessionData sd = (SessionData)map.get(sessionName);
    return sd;
  }

  public AliasVO getAlias()
  {
    return aliasVO;
  }

  public ConnectionManager getConnectionManager()
  {
    return connectionManager;
  }

  public DBObjectRetriever getDBObjectRetriever()
  {
    return dbObjectRetriever;
  }

  public void addMessageHandler(MessageHandler messageHandler)
  {
    messageHandlerList.add(messageHandler);
  }

  public void addErrorMessage(String text, boolean guiThread)
  {
    if (!ended)
      fireMessage(new SessionMessage(SessionMessage.ERROR, text, guiThread));
  }

  public void addSuccessMessage(String text, boolean guiThread)
  {
    if (!ended)
      fireMessage(new SessionMessage(SessionMessage.SUCCESS, text, guiThread));
  }

  public void addMessage(String text, boolean guiThread)
  {
    if (!ended)
      fireMessage(new SessionMessage(SessionMessage.NORMAL, text, guiThread));
  }

  public void addMessage(SessionMessage sessionMessage)
  {
    if (!ended)
      fireMessage(sessionMessage);
  }

  private void fireMessage(SessionMessage sessionMessage)
  {
    if (!ended)
    {
      for (Iterator it = messageHandlerList.iterator(); it.hasNext();)
      {
        MessageHandler handler = (MessageHandler)it.next();
        if (sessionMessage.getType() == SessionMessage.ERROR)
          handler.addErrorMessage(sessionMessage.getText(), sessionMessage.onGuiThread());
        else if (sessionMessage.getType() == SessionMessage.SUCCESS)
          handler.addSuccessMessage(sessionMessage.getText(), sessionMessage.onGuiThread());
        else
          handler.addMessage(sessionMessage.getText(), sessionMessage.onGuiThread());
      }
    }
  }

  @Override
  public void connectionChanged(String oldName, String newName)
  {
    if (getAlias().getName().equals(oldName))
    {
      getAlias().setName(newName);
    }
  }
  @Override
  public void connectionAdded(String name)
  {
  }
  @Override
  public void connectionRemoved(String name)
  {
  }

  public void addQueryTextListener(QueryTextListener listener)
  {
    queryTextListenerList.add(listener);
  }

  public void insertQueryText(String text)
  {
    if (!ended)
    {
      for (Iterator it = queryTextListenerList.iterator(); it.hasNext();)
      {
        QueryTextListener listener = (QueryTextListener)it.next();
        listener.insertQueryText(text);
      }
    }
  }

  public void addQueryText(String text)
  {
    if (!ended)
    {
      for (Iterator it = queryTextListenerList.iterator(); it.hasNext();)
      {
        QueryTextListener listener = (QueryTextListener)it.next();
        listener.addQueryText(text);
      }
    }
  }

  public String getExportFile()
  {
    return exportFile;
  }

  public void setExportFile(String exportFile)
  {
    this.exportFile = exportFile;
  }

  public String getExportDateFormat()
  {
    return exportDateFormat;
  }

  public void setExportDateFormat(String exportDateFormat)
  {
    this.exportDateFormat = exportDateFormat;
  }

  public boolean getExportInlcudeColumnName()
  {
    return exportInlcudeColumnName;
  }

  public void setExportInlcudeColumnName(boolean exportInlcudeColumnName)
  {
    this.exportInlcudeColumnName = exportInlcudeColumnName;
  }

  public boolean getExportInlcudeSql()
  {
    return exportInlcudeSql;
  }

  public void setExportInlcudeSql(boolean exportInlcudeSql)
  {
    this.exportInlcudeSql = exportInlcudeSql;
  }

  public String getExportTimeFormat()
  {
    return exportTimeFormat;
  }

  public void setExportTimeFormat(String exportTimeFormat)
  {
    this.exportTimeFormat = exportTimeFormat;
  }

  public String getExportTextDelimiter()
  {
    return exportTextDelimiter;
  }

  public void setExportTextDelimiter(String exportTextDelimiter)
  {
    this.exportTextDelimiter = exportTextDelimiter;
  }

  public boolean getExportExcelSplitWorksheet()
  {
    return exportExcelSplitWorksheet;
  }

  public void setExportExcelSplitWorksheet(boolean exportExcelSplitWorksheet)
  {
    this.exportExcelSplitWorksheet = exportExcelSplitWorksheet;
  }

  public int getExportExcelSplitRowCount()
  {
    return exportExcelSplitRowCount;
  }

  public void setExportExcelSplitRowCount(int exportExcelSplitRowCount)
  {
    this.exportExcelSplitRowCount = exportExcelSplitRowCount;
  }

}
