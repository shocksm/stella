package org.stellasql.stella.connection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.DriverVO;
import org.stellasql.stella.classloader.CustomClassLoader;
import org.stellasql.stella.classloader.CustomDriverManager;
import org.stellasql.stella.session.SessionData;

public class ConnectionManager
{
  private final static Logger logger = LogManager.getLogger(ConnectionManager.class);

  private AliasVO aliasVO = null;
  private Connection con = null;
  private CustomClassLoader classLoader = null;
  private boolean inUse = false;
  private boolean shutdown = false;
  private String currentCatalog = null;
  private List catalogChangeLitenerList = new LinkedList();
  private Connection secondaryCon = null;
  private boolean secondaryInUse = false;
  private List catalogNames = new ArrayList();
  private LinkedList connectionWaitList = new LinkedList();
  private String username = "";
  private String password = "";

  private boolean useCatalogs = false;
  private boolean useSchemas = false;
  private String separator = ".";
  private String identifierQuote = "\"";

  private DriverVO driver = null;
  private SessionData sessionData = null;

  private boolean autocommit = true;
  private boolean storesLowerCaseIdentifiers;
  private boolean storesLowerCaseQuotedIdentifiers;
  private boolean storesUpperCaseIdentifiers;
  private boolean storesUpperCaseQuotedIdentifiers;

  public boolean getUseCatalogs()
  {
    return useCatalogs;
  }

  public boolean getUseSchemas()
  {
    return useSchemas;
  }

  public String getSeparator()
  {
    return separator;
  }

  public String getIdentifierQuote()
  {
    return identifierQuote;
  }

  public ConnectionManager(SessionData sessionData, AliasVO aliasVO, String username, String password)
  {
    this.sessionData = sessionData;
    this.aliasVO = aliasVO;
    driver  = ApplicationData.getInstance().getDriver(this.aliasVO.getDriverName());
    this.username = username;
    this.password = password;
    classLoader = new CustomClassLoader(this.getClass().getClassLoader(), driver.getDriverPathFileList());
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  protected static String getWarningString(SQLWarning sqlLWarning)
  {
    String value = null;

    if (sqlLWarning != null)
    {
      StringBuffer sbuf = new StringBuffer();
      while (sqlLWarning != null)
      {
        if (sbuf.length() > 0)
          sbuf.append("\n\n");
        sbuf.append("SQLState: ").append(sqlLWarning.getSQLState());
        sbuf.append("\nMessage:  ").append(sqlLWarning.getMessage());
        sbuf.append("\nError code:   ").append(sqlLWarning.getErrorCode());
        sqlLWarning = sqlLWarning.getNextWarning();
      }
      value = sbuf.toString();
    }
    return value;
  }

  public synchronized String open() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
  {
    return open(true);
  }

  private synchronized String open(boolean initialize) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
  {
    logger.debug("opening connection");
    CustomDriverManager driverManager = new CustomDriverManager();

    con = driverManager.getConnection(classLoader, driver.getDriverClass(), aliasVO.getURL(), username, password);
    if (con == null)
      throw new SQLException("Connection returned was null");

    if (con.getAutoCommit() != autocommit)
      con.setAutoCommit(autocommit);

    logger.debug("Connection opened");

    if (initialize)
    {
      if (con.getMetaData().supportsCatalogsInDataManipulation())
        useCatalogs = true;

      if (con.getMetaData().supportsSchemasInDataManipulation())
        useSchemas = true;

      storesLowerCaseIdentifiers = con.getMetaData().storesLowerCaseIdentifiers();
      storesLowerCaseQuotedIdentifiers = con.getMetaData().storesLowerCaseQuotedIdentifiers();
      storesUpperCaseIdentifiers = con.getMetaData().storesUpperCaseIdentifiers();
      storesUpperCaseQuotedIdentifiers = con.getMetaData().storesUpperCaseQuotedIdentifiers();

      if (con.getMetaData().getCatalogSeparator() != null
          && con.getMetaData().getCatalogSeparator().length() > 0)
        separator = con.getMetaData().getCatalogSeparator();

      if (con.getMetaData().getIdentifierQuoteString() != null
          && con.getMetaData().getIdentifierQuoteString().length() > 0)
      {
        identifierQuote = con.getMetaData().getIdentifierQuoteString();
      }

      if (useCatalogs)
      {
        logger.debug("Getting catalogs");
        currentCatalog = con.getCatalog();
        ResultSet rs = con.getMetaData().getCatalogs();
        while (rs.next())
        {
          catalogNames.add(rs.getString("TABLE_CAT"));
        }
        rs.close();

        logger.debug("Catalogs retrieved");
      }
    }
    else if (currentCatalog != null)
      con.setCatalog(currentCatalog);

    String warning = getWarningString(con.getWarnings());

    if (sessionData != null)
      sessionData.connectionOpened(warning);

    return warning;
  }

  public synchronized String getCurrentCatalog()
  {
    return currentCatalog;
  }


  public List getCatalogNames()
  {
    return catalogNames;
  }

  public void addCatalogChangeListener(CatalogChangeListener listener)
  {
    catalogChangeLitenerList.add(listener);
  }

  private void fireCatalogChange()
  {
    String catalog = currentCatalog;
    for (Iterator it = catalogChangeLitenerList.iterator(); it.hasNext();)
    {
      CatalogChangeListener listener = (CatalogChangeListener)it.next();
      listener.catalogChanged(catalog);
    }
  }

  public synchronized void closeAndShutdown() throws SQLException
  {
    close(true);
  }

  public synchronized void close() throws SQLException
  {
    close(false);
  }

  private synchronized void close(boolean shutdown) throws SQLException
  {
    if (shutdown)
      this.shutdown = true;
    if (con != null)
    {
      con.close();
      logger.debug("Primary connection closed");
    }

    if (sessionData != null)
      sessionData.connectionClosed();

    if (secondaryCon != null)
    {
      secondaryCon.close();
      logger.debug("Secondary connection closed");
    }

    con = null;
    secondaryCon = null;
  }

  public boolean isShutdown()
  {
    return shutdown;
  }

  public synchronized Connection getConnection()
  {
    connectionWaitList.addLast(Thread.currentThread());

    while ((inUse || connectionWaitList.getFirst() != Thread.currentThread()) && !shutdown)
    {
      logger.debug("Waiting on connection");
      try
      {
        this.wait();
      }
      catch (InterruptedException e)
      {}
    }

    if (shutdown)
    {
      logger.debug("Connection is shutdown.");
      return null;
    }

    //logger.debug("Got connection");
    connectionWaitList.removeFirst();
    inUse = true;

    try
    {
      if (con == null || con.isClosed() || !con.isValid(2))
      {
        logger.info("Connection was closed. Opening a new connection.");
        open(false);
      }
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }

    return con;
  }

  public void setAutocommit(boolean enabled) throws SQLException
  {
    Connection con = getConnection();
    try
    {
      if (!con.getAutoCommit() && enabled)
        con.commit();
      con.setAutoCommit(enabled);

      autocommit = enabled;
    }
    finally
    {
      releaseConnection();
    }
  }

  public synchronized void releaseConnection()
  {
    releaseConnection(false);
  }

  protected synchronized void releaseConnection(boolean forceCatalogEvent)
  {
    if (currentCatalog != null)
    {
      try
      {
        if (con != null && !con.isClosed())
        {
          String catalog = con.getCatalog();
          if (forceCatalogEvent || !currentCatalog.equals(catalog))
          {
            currentCatalog = catalog;
            fireCatalogChange();
          }
        }
      }
      catch (Exception e)
      {
        logger.error(e.getMessage(), e);
      }
    }

    //logger.debug("Released connection");

    inUse = false;
    this.notifyAll();
  }

  public synchronized Connection getSecondaryConnection() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
  {
    while (secondaryInUse)
    {
      try
      {
        logger.debug("waiting on secondary connection");
        this.wait();
      }
      catch (InterruptedException e)
      {}
    }

    if (secondaryCon == null || secondaryCon.isClosed() || !secondaryCon.isValid(2))
    {
      logger.debug("secondary connection being created");
      CustomDriverManager driverManager = new CustomDriverManager();
      secondaryCon = driverManager.getConnection(classLoader, driver.getDriverClass(), aliasVO.getURL(), aliasVO.getUsername(), aliasVO.getPassword());
      if (secondaryCon == null)
        throw new SQLException("Connection returned was null");
    }

    logger.debug("secondary connection taken");
    secondaryInUse = true;
    return secondaryCon;
  }

  public synchronized void releaseSecondaryConnection()
  {
    logger.debug("Secondary connection released");
    secondaryInUse = false;
    this.notify();
  }

  public boolean getStoresLowerCaseIdentifiers()
  {
    return storesLowerCaseIdentifiers;
  }

  public boolean getStoresLowerCaseQuotedIdentifiers()
  {
    return storesLowerCaseQuotedIdentifiers;
  }

  public boolean getStoresUpperCaseIdentifiers()
  {
    return storesUpperCaseIdentifiers;
  }

  public boolean getStoresUpperCaseQuotedIdentifiers()
  {
    return storesUpperCaseQuotedIdentifiers;
  }

}
