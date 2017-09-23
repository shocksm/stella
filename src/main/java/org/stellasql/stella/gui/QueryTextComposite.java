package org.stellasql.stella.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.connection.CatalogChangeListener;
import org.stellasql.stella.connection.QueryTransactionListener;
import org.stellasql.stella.export.ExportOptions;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.SqlTextAdditions;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.WorkerRunnable;
import org.stellasql.stella.query.TokenScanner;
import org.stellasql.stella.session.QueryTextListener;
import org.stellasql.stella.session.SQLActionHandler;
import org.stellasql.stella.session.SQLTextHandler;
import org.stellasql.stella.session.SessionData;
import org.stellasql.stella.session.SessionPreparingToEndListener;
import org.stellasql.stella.session.SessionReadyListener;

public class QueryTextComposite extends Composite implements /*KeyListener, VerifyKeyListener, */SelectionListener, MouseListener, MenuListener, SessionReadyListener, CatalogChangeListener, QueryTextListener, DisposeListener, FontChangeListener, VerifyListener, SessionPreparingToEndListener, QueryTransactionListener, SQLActionHandler, SQLTextHandler, ModifyListener
{
  private final static Logger logger = LogManager.getLogger(QueryTextComposite.class);

  private Composite innerComposite = null;
  private Composite toolsComposite1 = null;
  private Button autoCommitButton = null;
  private Button commitButton = null;
  private Button rollbackButton = null;
  private Menu runMenu = null;
  private MenuItem runTableMenuItem = null;
  private MenuItem runFileMenuItem = null;
  private ToolBar runToolBar = null;
  private ToolItem runButton = null;
  private ToolItem historyButton = null;
  private ToolItem favoritesButton = null;
  private ToolItem favoritesAddButton = null;
  private ToolItem maximizeButton = null;
  private StyledText sqlText = null;
  //private boolean keyReleased = true;
  private String sessionName = "";
  private QueryComposite queryComposite = null;
  private boolean maximized = false;
  private Label catalogLabel = null;
  private Combo catalogCombo = null;
  private Composite catalogComposite = null;
  private Button limitCheck = null;
  private Text limitText = null;
  private Button stripCommentsCheck = null;
  private SqlTextAdditions sqlTextAdditions = null;

  private ToolItem disconnectButton = null;

  private int lastCatalogSelected = -1;
  private Color enabledColor = null;

  public QueryTextComposite(Composite parent, QueryComposite queryComposite, String sessionName)
  {
    super(parent, SWT.NONE);

    addDisposeListener(this);

    this.sessionName = sessionName;
    SessionData.getSessionData(this.sessionName).addSessionPreparingToEndListener(this);
    SessionData.getSessionData(this.sessionName).addSessionReadyListener(this);
    SessionData.getSessionData(this.sessionName).getConnectionManager().addCatalogChangeListener(this);
    SessionData.getSessionData(this.sessionName).addQueryTextListener(this);
    SessionData.getSessionData(this.sessionName).setSQLActionHandler(this);
    SessionData.getSessionData(this.sessionName).setSQLTextHandler(this);
    SessionData.getSessionData(this.sessionName).getQuerySequencer().addQueryTransactionListener(this);

    this.queryComposite = queryComposite;

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    this.setLayout(gridLayout);
    this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

    // used to prevent input (setEnable(false)) while the busy cursor is displayed
    innerComposite = new Composite(this, SWT.NONE);
    gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.verticalSpacing = 0;
    innerComposite.setLayout(gridLayout);
    innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


    toolsComposite1 = new Composite(innerComposite, SWT.NONE);
    toolsComposite1.addMouseListener(this);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 11;
    gridLayout.marginHeight = 2;
    gridLayout.marginWidth = 2;
    gridLayout.horizontalSpacing = 2;
    toolsComposite1.setLayout(gridLayout);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    toolsComposite1.setLayoutData(gridData);

    //------------------------------------------------------------------

    runToolBar = new ToolBar(toolsComposite1, SWT.FLAT);
    runButton = new ToolItem(runToolBar, SWT.DROP_DOWN);
    runButton.setImage(StellaImages.getInstance().getRunImage());
    runButton.addSelectionListener(this);
    //runButton.setToolTipText("Execute query Ctrl+Enter");
    runButton.setImage(StellaImages.getInstance().getRunImage());
    runButton.setEnabled(false);
    runToolBar.setToolTipText("Execute query Ctrl+Enter");

    gridData = new GridData();
    gridData.horizontalIndent = 5;
    gridData.horizontalAlignment = SWT.BEGINNING;
    //gridData.grabExcessHorizontalSpace = true;
    runToolBar.setLayoutData(gridData);

    runMenu = new Menu (runToolBar);
    runTableMenuItem = new MenuItem(runMenu, SWT.PUSH);
    runTableMenuItem.setText("Execute and &display results\tCtrl+Enter");
    runTableMenuItem.addSelectionListener(this);

    runFileMenuItem = new MenuItem(runMenu, SWT.PUSH);
    runFileMenuItem.setText("Execute and &export results\tCtrl+Shift+Enter");
    runFileMenuItem.addSelectionListener(this);

    //------------------------------------------------------------------

    ToolBar utilToolBar = new ToolBar(toolsComposite1, SWT.FLAT);
    historyButton = new ToolItem(utilToolBar, SWT.PUSH);
    historyButton.setImage(StellaImages.getInstance().getHistoryImage());
    historyButton.addSelectionListener(this);
    historyButton.setEnabled(false);
    historyButton.setToolTipText("Display query history Ctrl+H");

    favoritesButton = new ToolItem(utilToolBar, SWT.PUSH);
    favoritesButton.setImage(StellaImages.getInstance().getFavoritesImage());
    favoritesButton.addSelectionListener(this);
    favoritesButton.setEnabled(false);
    favoritesButton.setToolTipText("Display favorite queries Ctrl+F");

    favoritesAddButton = new ToolItem(utilToolBar, SWT.PUSH);
    favoritesAddButton.setImage(StellaImages.getInstance().getFavoritesNewImage());
    favoritesAddButton.addSelectionListener(this);
    favoritesAddButton.setEnabled(false);
    favoritesAddButton.setToolTipText("Add query to favorites Ctrl+D");

    //------------------------------------------------------------------



    autoCommitButton = new Button(toolsComposite1, SWT.CHECK);
    autoCommitButton.setText("&Auto Commit");
    gridData = new GridData();
    gridData.horizontalIndent = 10;
    autoCommitButton.setLayoutData(gridData);
    autoCommitButton.setSelection(SessionData.getSessionData(this.sessionName).getAutoCommit());
    autoCommitButton.setEnabled(false);
    autoCommitButton.addSelectionListener(this);

    commitButton = new Button(toolsComposite1, SWT.PUSH);
    commitButton.setText("Commit");
    commitButton.setEnabled(false);
    commitButton.addSelectionListener(this);

    rollbackButton = new Button(toolsComposite1, SWT.PUSH);
    rollbackButton.setText("Rollback");
    rollbackButton.setEnabled(false);
    rollbackButton.addSelectionListener(this);

    //------------------------------------------------------------------

    limitCheck = new Button(toolsComposite1, SWT.CHECK);
    limitCheck.setText("&Limit results:");
    limitCheck.setSelection(SessionData.getSessionData(this.sessionName).getLimitResults());
    limitCheck.addSelectionListener(this);
    gridData = new GridData();
    //gridData.horizontalAlignment = SWT.END;
    //gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalIndent = 10;
    limitCheck.setLayoutData(gridData);
    limitCheck.setEnabled(false);

    limitText = new Text(toolsComposite1, SWT.BORDER);
    limitText.setText("" + SessionData.getSessionData(this.sessionName).getMaxRows());
    limitText.addVerifyListener(this);
    limitText.addModifyListener(this);
    gridData = new GridData();
    gridData.widthHint = 35;
    limitText.setLayoutData(gridData);
    limitText.setEnabled(false);

    stripCommentsCheck = new Button(toolsComposite1, SWT.CHECK);
    stripCommentsCheck.setText("&Strip Comments");
    stripCommentsCheck.setToolTipText("Strip comments from queries before submitting them to the database");
    stripCommentsCheck.setSelection(SessionData.getSessionData(this.sessionName).getStripComments());
    stripCommentsCheck.addSelectionListener(this);
    gridData = new GridData();
    gridData.horizontalIndent = 10;
    stripCommentsCheck.setLayoutData(gridData);
    stripCommentsCheck.setEnabled(false);

    //------------------------------------------------------------------
    // this composite is a spacer - the component with grabExcessHorizontalSpace
    // will 'pop out' when the container is too small. it looks odd so it is best
    // to have a non visible one do that
    Composite rightsideComposite = new Composite(toolsComposite1, SWT.NONE);
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.heightHint = 1;
    rightsideComposite.setLayoutData(gridData);

    //------------------------------------------------------------------

    ToolBar toolBar = new ToolBar(toolsComposite1, SWT.FLAT);
    gridData = new GridData();
    gridData.horizontalIndent = 10;
    //gridData.horizontalAlignment = SWT.END;
    //gridData.grabExcessHorizontalSpace = true;
    toolBar.setLayoutData(gridData);

    disconnectButton = new ToolItem(toolBar, SWT.PUSH);
    //disconnectButton.setText("D");
    disconnectButton.setImage(StellaImages.getInstance().getDisconnectImage());
    disconnectButton.setDisabledImage(StellaImages.getInstance().getDisconnectDisImage());
    disconnectButton.setToolTipText("Disconnect from the database");
    disconnectButton.setEnabled(false);
    disconnectButton.addSelectionListener(this);


    //------------------------------------------------------------------

    toolBar = new ToolBar(toolsComposite1, SWT.FLAT);
    gridData = new GridData();
    gridData.horizontalIndent = 10;
    gridData.horizontalAlignment = SWT.END;
    //gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = SWT.BEGINNING;
    toolBar.setLayoutData(gridData);

    maximizeButton = new ToolItem(toolBar, SWT.PUSH);
    maximizeButton.setToolTipText("Maximize");
    maximizeButton.setImage(StellaImages.getInstance().getMaximizeImage());
    maximizeButton.addSelectionListener(this);
    maximizeButton.setEnabled(false);


    //------------------------------------------------------------------
    catalogComposite = new Composite(innerComposite, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 2;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 2;
    catalogComposite.setLayout(gridLayout);
    gridData = new GridData();
    gridData.exclude = true;
    gridData.horizontalIndent = 5;
    catalogComposite.setLayoutData(gridData);
    catalogComposite.setVisible(false);

    catalogLabel = new Label(catalogComposite, SWT.NONE);
    catalogLabel.setText("C&atalog:");
    catalogLabel.setLayoutData(new GridData());

    catalogCombo = new Combo(catalogComposite, SWT.SINGLE | SWT.READ_ONLY);
    catalogCombo.addSelectionListener(this);
    catalogCombo.setLayoutData(new GridData());
    catalogCombo.setEnabled(false);
    catalogCombo.setVisibleItemCount(20);

    //------------------------------------------------------------------


    Composite compositeText = new Composite(innerComposite, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.marginTop = 1;
    compositeText.setLayout(gridLayout);
    compositeText.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
    compositeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    sqlText = new StyledText(compositeText, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    sqlText.setIndent(2);
    sqlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    enabledColor = sqlText.getBackground();
    sqlText.setEnabled(false);
    sqlText.setBackground(sqlText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    //sqlText.addVerifyKeyListener(this);
    //sqlText.addKeyListener(this);

    sqlTextAdditions = new SqlTextAdditions(sqlText, SessionData.getSessionData(this.sessionName).getQuerySeparator());

    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);

    SessionData.getSessionData(this.sessionName).setSQLControl(sqlText);
  }

  protected void setFonts()
  {
    FontSetter.setFont(sqlText, ApplicationData.getInstance().getQueryTextFont());
    FontSetter.setFont(catalogLabel, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(catalogCombo, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(limitCheck, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(limitText, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(stripCommentsCheck, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(autoCommitButton, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(commitButton, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(rollbackButton, ApplicationData.getInstance().getGeneralFont());
  }

  protected void limitToggled()
  {
    limitText.setEnabled(limitCheck.getSelection());

    SessionData.getSessionData(sessionName).setLimitResults(limitCheck.getSelection());
  }

  protected void stripCommentsToggled()
  {
    SessionData.getSessionData(sessionName).setStripComments(stripCommentsCheck.getSelection());
  }

  protected void autoCommitToggled()
  {
    final boolean selected = autoCommitButton.getSelection();

    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;
      @Override
      public void doTask()
      {
        try
        {
          SessionData.getSessionData(sessionName).getConnectionManager().setAutocommit(selected);
        }
        catch (SQLException e)
        {
          ex = e;
        }
      }

      @Override
      public void doUITask()
      {
        if (!QueryTextComposite.this.isDisposed())
        {
          if (ex != null)
          {
            SessionData.getSessionData(sessionName).addErrorMessage("Could not change auto commit: " + ex.getMessage(), true);
            autoCommitButton.setSelection(!selected);
          }
          else
          {
            SessionData.getSessionData(sessionName).addMessage("Auto commit set to " + selected, true);

            if (autoCommitButton.getSelection())
            {
              commitButton.setEnabled(false);
              rollbackButton.setEnabled(false);
            }
          }

          innerComposite.setEnabled(true);
          BusyManager.setNotBusy(QueryTextComposite.this);
        }
      }
    };

    BusyManager.setBusy(this);
    innerComposite.setEnabled(false);
    worker.startTask();

  }

  protected void commitPressed()
  {
    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;
      @Override
      public void doTask()
      {
        Connection con = SessionData.getSessionData(sessionName).getConnectionManager().getConnection();
        try
        {
          con.commit();
        }
        catch (SQLException e)
        {
          ex = e;
        }
        finally
        {
          SessionData.getSessionData(sessionName).getConnectionManager().releaseConnection();
        }
      }

      @Override
      public void doUITask()
      {
        if (!QueryTextComposite.this.isDisposed())
        {
          if (ex != null)
          {
            SessionData.getSessionData(sessionName).addErrorMessage("Could not commit: " + ex.getMessage(), true);
          }
          else
          {
            SessionData.getSessionData(sessionName).addMessage("Transaction has been committed", true);
            commitButton.setEnabled(false);
            rollbackButton.setEnabled(false);
          }

          innerComposite.setEnabled(true);
          BusyManager.setNotBusy(QueryTextComposite.this);
        }
      }
    };

    BusyManager.setBusy(this);
    innerComposite.setEnabled(false);
    worker.startTask();
  }

  protected void rollbackPressed()
  {
    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;
      @Override
      public void doTask()
      {
        Connection con = SessionData.getSessionData(sessionName).getConnectionManager().getConnection();
        try
        {
          con.rollback();
        }
        catch (SQLException e)
        {
          ex = e;
        }
        finally
        {
          SessionData.getSessionData(sessionName).getConnectionManager().releaseConnection();
        }
      }

      @Override
      public void doUITask()
      {
        if (!QueryTextComposite.this.isDisposed())
        {
          if (ex != null)
          {
            SessionData.getSessionData(sessionName).addErrorMessage("Could not rollback: " + ex.getMessage(), true);
          }
          else
          {
            SessionData.getSessionData(sessionName).addMessage("Transaction has been rolled back", true);
            commitButton.setEnabled(false);
            rollbackButton.setEnabled(false);
          }

          innerComposite.setEnabled(true);
          BusyManager.setNotBusy(QueryTextComposite.this);
        }
      }
    };

    BusyManager.setBusy(this);
    innerComposite.setEnabled(false);
    worker.startTask();
  }




  private List getLines(String text, String delim)
  {
    List list = new ArrayList();
    StringBuffer sbuf = new StringBuffer(text);

    int index = 0;
    while ((index = sbuf.indexOf(delim)) >= 0)
    {
      if (index == 0)
        list.add("");
      else
        list.add(sbuf.substring(0, index));

      sbuf.replace(0, index+delim.length(), "");
      if (sbuf.length() == 0)
        list.add("");
    }
    if (sbuf.length() > 0)
      list.add(sbuf.toString());

    return list;
  }

  private int getOffsetLine(List lines, int offset, String delim)
  {
    int line = -1;
    int count = 0;
    for (int index = 0; index < lines.size(); index++)
    {
      String text = (String)lines.get(index);
      count += text.length();
      if (count >= offset)
      {
        line = index;
        break;
      }
      count += delim.length();
    }

    return line;
  }

  private StringBuffer getQueryText()
  {
    StringBuffer sbuf = new StringBuffer();
    if (sqlText.getSelectionText().length() > 0)
    {
      sbuf.append(sqlText.getSelectionText());
    }
    else
    {
      // get text

  /* this is really slow
      Point range = getCurrentLineRange();
      for (int index = range.x; index <= range.y; index++)
      {
        text += getTextOnLine(index);
        if (index < range.y)
          text += "\n";
      }
      */


      String delim = sqlText.getLineDelimiter();
      int offset = sqlText.getCaretOffset();
      List lines = getLines(sqlText.getText(), delim);
      int line = getOffsetLine(lines, offset, delim);

      if (line >= 0 && ((String)lines.get(line)).trim().length() == 0)
      {
        // blank line
        line = -1;
      }

      if (line >= 0)
      {
        int start = line;
        int end = line;

        // find start
        for (int index = line-1; index >= 0; index--)
        {
          String lineText = (String)lines.get(index);
          if (lineText.trim().length() == 0)
            break;
          else
            start = index;
        }

        // find end
        for (int index = line+1; index < lines.size(); index++)
        {
          String lineText = (String)lines.get(index);
          if (lineText.trim().length() == 0)
            break;
          else
            end = index;
        }

        for (int index = start; index <= end; index++)
        {
          sbuf.append((String)lines.get(index));
          if (index < end)
            sbuf.append("\n");
        }
      }
    }

    return sbuf;
  }

  private void processQueryText(boolean outputToFile)
  {

    List queryList = new ArrayList();
    StringBuffer sbuf = getQueryText();

    if (sbuf.length() > 0)
    {
      TokenScanner tokenScanner = new TokenScanner(SessionData.getSessionData(sessionName).getQuerySeparator());

      tokenScanner.setText(sbuf.toString());
      Object token = null;
      boolean onlyComments = true;
      StringBuffer queryBuf = new StringBuffer();
      while ((token = tokenScanner.nextToken()) != null)
      {
        if (token != TokenScanner.BLOCKCOMMENT
            && token != TokenScanner.LINECOMMENT
            && token != TokenScanner.SEPARATOR)
        {
          onlyComments = false;
        }

        if (token == TokenScanner.SEPARATOR)
        {
          if (!onlyComments)
            queryList.add(queryBuf.toString().trim());
          queryBuf.setLength(0);

          onlyComments = true;
        }
        else
        {
          queryBuf.append(sbuf.substring(tokenScanner.getStartPosition(), tokenScanner.getEndPosition() + 1));
        }
      }

      if (queryBuf.toString().trim().length() > 0 && !onlyComments)
      {
        queryList.add(queryBuf.toString().trim());
      }
    }

    if (queryList.size() > 0)
    {
      if (outputToFile)
      {
        ExportDialog ed = new ExportDialog(this.getShell(), SessionData.getSessionData(sessionName));
        ExportOptions eo = ed.open();
        if (eo != null)
        {
          runQueries(queryList, eo);
        }
      }
      else
      {
        runQueries(queryList, null);
      }
    }
    else
      noQueryMessage();



  }

  private void runQueries(List queryList, ExportOptions eo)
  {
    if (queryList.size() > 0)
    {
      int maxRows = 0;
      if (limitCheck.getSelection())
      {
        try
        {
          maxRows = Integer.parseInt(limitText.getText());
        }
        catch (NumberFormatException e)
        {
        }
      }
      queryComposite.runQueries(queryList, maxRows, eo);
    }
  }

  private void noQueryMessage()
  {
    SessionData.getSessionData(sessionName).addErrorMessage("No query selected. Position the caret on or highlight the query to be executed.", true);
  }

/*

  public void keyPressed(KeyEvent e)
  {
  }
  public void keyReleased(KeyEvent e)
  {
    // prevent query from being run multiple times if the keys are held down
    keyReleased = true;
  }

  public void verifyKey(VerifyEvent e)
  {
    if ((e.keyCode == 'e' || e.keyCode == SWT.CR)
        && e.stateMask == SWT.CONTROL)
    {
      // prevent query from being run multiple times if the keys are held down
      if (keyReleased)
      {
        keyReleased = false;
        processQueryText(false);
      }
      e.doit = false;
    }
    else if (e.keyCode == SWT.CR
        && e.stateMask == (SWT.CONTROL | SWT.SHIFT))
    {
      // prevent query from being run multiple times if the keys are held down
      if (keyReleased)
      {
        keyReleased = false;
        processQueryText(true);
      }
      e.doit = false;
    }
  }
*/

  public void connectionOpened()
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        disconnectButton.setEnabled(true);
      }});
  }

  private void addFavoritePressed()
  {
    StringBuffer sbuf = getQueryText();
    FavoriteAddDialog2 fd = new FavoriteAddDialog2(Stella.getInstance().getShell(), false, true);
    fd.setQuery(sbuf.toString());
    fd.open();
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == runButton)
    {
      if (e.detail == SWT.ARROW)
      {
        Rectangle bounds = runButton.getBounds();
        Point pt = runToolBar.toDisplay(bounds.x, bounds.y + bounds.height);
        runMenu.setLocation(pt.x, pt.y);
        runMenu.setVisible(true);
      }
      else
      {
        processQueryText(false);
      }
    }
    else if (e.widget == runTableMenuItem)
    {
      processQueryText(false);
    }
    else if (e.widget == runFileMenuItem)
    {
      processQueryText(true);
    }
    else if (e.widget == maximizeButton)
    {
      toggleMaximize();
    }
    else if (e.widget == catalogCombo)
    {
      SessionData.getSessionData(sessionName).getQuerySequencer().setCatalog(catalogCombo.getText());
      sqlText.setFocus();
    }
    else if (e.widget == historyButton)
    {
      Stella.getInstance().displayHistory();
    }
    else if (e.widget == favoritesButton)
    {
      Stella.getInstance().displayFavorites();
    }
    else if (e.widget == favoritesAddButton)
    {
      addFavoritePressed();
    }
    else if (e.widget == limitCheck)
    {
      limitToggled();
    }
    else if (e.widget == stripCommentsCheck)
    {
      stripCommentsToggled();
    }
    else if (e.widget == autoCommitButton)
    {
      autoCommitToggled();
    }
    else if (e.widget == commitButton)
    {
      commitPressed();
    }
    else if (e.widget == rollbackButton)
    {
      rollbackPressed();
    }
    else if (e.widget == disconnectButton)
    {
      try
      {
        if (sessionPreparingToEnd())
        {
          SessionData.getSessionData(sessionName).getConnectionManager().close();
          commitButton.setEnabled(false);
          rollbackButton.setEnabled(false);
          disconnectButton.setEnabled(false);
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  @Override
  public void addQueryText(String text)
  {
    if (sqlText.getCharCount() > 0)
    {
      sqlText.append(sqlText.getLineDelimiter());
      sqlText.append(sqlText.getLineDelimiter());
    }
    int start = sqlText.getCharCount();
    sqlText.append(text);
    sqlText.setFocus();
    sqlText.setCaretOffset(sqlText.getCharCount());
    sqlText.setSelection(start, sqlText.getCharCount());
  }

  private void toggleMaximize()
  {
    setMaximized(!maximized);
  }

  public boolean isMaximized()
  {
    return maximized;
  }

  public void setMaximized(boolean maximized)
  {
    if (maximized)
    {
      this.maximized = true;
      maximizeButton.setToolTipText("Restore");
      maximizeButton.setImage(StellaImages.getInstance().getRestoreImage());
      queryComposite.setMaximized(this);
    }
    else
    {
      this.maximized = false;
      maximizeButton.setToolTipText("Maximize");
      maximizeButton.setImage(StellaImages.getInstance().getMaximizeImage());
      queryComposite.setMaximized(null);
    }
  }



  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
    if (e.widget == toolsComposite1 || e.widget == innerComposite)
      toggleMaximize();
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
  }

  @Override
  public void sessionReady()
  {
    runButton.setEnabled(true);
    autoCommitButton.setEnabled(true);
    catalogCombo.setEnabled(true);
    historyButton.setEnabled(true);
    favoritesButton.setEnabled(true);
    favoritesAddButton.setEnabled(true);
    limitCheck.setEnabled(true);
    limitText.setEnabled(true);
    stripCommentsCheck.setEnabled(true);
    disconnectButton.setEnabled(true);
    maximizeButton.setEnabled(true);
    sqlText.setEnabled(true);
    sqlText.setBackground(enabledColor);
    sqlText.setFocus();

    if (SessionData.getSessionData(sessionName).getConnectionManager().getCatalogNames().size() > 0)
    {
      Iterator it = SessionData.getSessionData(sessionName).getConnectionManager().getCatalogNames().iterator();
      while (it.hasNext())
      {
        catalogCombo.add((String)it.next());
      }

      GridData gridData = (GridData)catalogComposite.getLayoutData();
      gridData.exclude = false;

      String catalog = SessionData.getSessionData(sessionName).getConnectionManager().getCurrentCatalog();
      if (catalog != null)
      {
        int index = SessionData.getSessionData(sessionName).getConnectionManager().getCatalogNames().indexOf(catalog);
        catalogCombo.select(index);
        lastCatalogSelected = catalogCombo.getSelectionIndex();
      }

      catalogComposite.setVisible(true);
      //toolsComposite2.layout(true);
      innerComposite.layout(true);
    }

    if (!autoCommitButton.getSelection())
      autoCommitToggled();
  }



  @Override
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {


  }

  @Override
  public void catalogChanged(final String catalog)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        int index = SessionData.getSessionData(sessionName).getConnectionManager().getCatalogNames().indexOf(catalog);
        catalogCombo.select(index);
        if (lastCatalogSelected != catalogCombo.getSelectionIndex())
        {
          lastCatalogSelected = catalogCombo.getSelectionIndex();
          SessionData.getSessionData(sessionName).addMessage("Catalog changed to " + catalogCombo.getText(), true);
        }
      }
    });
  }

  @Override
  public void insertQueryText(String text)
  {
    if (sqlText.getSelectionCount() == 0)
    {
      sqlText.insert(text);
      sqlText.setCaretOffset(sqlText.getCaretOffset() + text.length());
    }
    else
    {
      Point pt = sqlText.getSelection();
      sqlText.insert(text);
      sqlText.setCaretOffset(pt.x + text.length());
    }

    sqlText.setFocus();
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    ApplicationData.getInstance().removeFontChangeListener(this);
  }

  @Override
  public void fontChanged()
  {
    setFonts();
    layout(true, true);
  }

  @Override
  public void verifyText(VerifyEvent e)
  {
    if (e.widget == limitText)
    {
      if (e.text != null)
      {
        for (int index = 0; index < e.text.length(); index++)
        {
          if (!Character.isDigit(e.text.charAt(index)))
          {
            e.doit = false;
            break;
          }
        }

      }
    }
  }

  @Override
  public boolean sessionPreparingToEnd()
  {
    if (!autoCommitButton.getSelection() && commitButton.getEnabled())
    {
      MessageDialog md = new MessageDialog(this.getShell(), SWT.OK | SWT.CANCEL);
      md.setText("Transaction not committed");
      md.setMessage("The current transaction has not been committed or rolled back\n"
           + "for the " + sessionName + " session"
           + "\n\nContinue to close the connection?");
      if (md.open() == SWT.CANCEL)
        return false;
    }

    return true;
  }

  @Override
  public void querySuccess()
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (!autoCommitButton.getSelection())
        {
          commitButton.setEnabled(true);
          rollbackButton.setEnabled(true);
        }
      }
    });
  }

  @Override
  public void queryCommit()
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (!autoCommitButton.getSelection())
        {
          commitButton.setEnabled(false);
          rollbackButton.setEnabled(false);
        }
      }
    });
  }

  @Override
  public void queryRollback()
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (!autoCommitButton.getSelection())
        {
          commitButton.setEnabled(false);
          rollbackButton.setEnabled(false);
        }
      }
    });
  }

  @Override
  public void addFavorite()
  {
    addFavoritePressed();
  }

  @Override
  public void commit()
  {
    commitPressed();
  }
  @Override
  public void execute()
  {
    processQueryText(false);
  }
  @Override
  public void executeExport()
  {
    processQueryText(true);
  }
  @Override
  public void rollback()
  {
    rollbackPressed();
  }
  @Override
  public boolean isAutoCommit()
  {
    return autoCommitButton.getSelection();
  }


  @Override
  public void clear()
  {
    sqlText.setText("");
  }
  @Override
  public void cut()
  {
    sqlText.cut();
  }
  @Override
  public void copy()
  {
    sqlText.copy();
  }
  @Override
  public void paste()
  {
    sqlText.paste();
  }
  @Override
  public void addQuotes()
  {
    sqlTextAdditions.quoteText();
  }
  @Override
  public void removeQuotes()
  {
    sqlTextAdditions.dequoteText();
  }
  @Override
  public void selectAll()
  {
    sqlText.selectAll();
  }
  @Override
  public boolean textSelected()
  {
    return sqlText.getSelectionCount() > 0;
  }
  @Override
  public boolean undoAvailable()
  {
    return sqlTextAdditions.undoAvailable();
  }
  @Override
  public boolean redoAvailable()
  {
    return sqlTextAdditions.redoAvailable();
  }
  @Override
  public void insertDateLiteral()
  {
    sqlTextAdditions.insertDateLiteral();
  }

  @Override
  public void redo()
  {
    sqlTextAdditions.redo();
  }

  @Override
  public void undo()
  {
    sqlTextAdditions.undo();
  }

  @Override
  public void appendText(String text)
  {
    sqlText.append(sqlText.getLineDelimiter());
    sqlText.append(text);
  }

  @Override
  public String getText()
  {
    return sqlText.getText().replaceAll(sqlText.getLineDelimiter(), "\n");
  }

  @Override
  public void setText(String text)
  {
    sqlText.setText(text);
  }

  @Override
  public boolean hasText()
  {
    return sqlText.getCharCount() > 0;
  }

  @Override
  public void modifyText(ModifyEvent e)
  {
    if (e.getSource() == limitText)
    {
      try
      {
        int maxRows = Integer.parseInt(limitText.getText());
        SessionData.getSessionData(sessionName).setMaxRows(maxRows);
      }
      catch (NumberFormatException ex)
      {
      }
    }

  }

}
