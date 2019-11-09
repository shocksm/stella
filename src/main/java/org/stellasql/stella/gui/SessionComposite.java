package org.stellasql.stella.gui;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.connection.ConnectionManager;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StyledTextContextMenu;
import org.stellasql.stella.gui.util.WorkerRunnable;
import org.stellasql.stella.session.MessageHandler;
import org.stellasql.stella.session.SessionData;
import org.stellasql.stella.session.SessionEndListener;

public class SessionComposite extends Composite implements SessionEndListener, MessageHandler, DisposeListener,
        FontChangeListener, MouseListener, ShellListener, FocusListener, SelectionListener, ControlListener
{
  private final static Logger logger = LogManager.getLogger(SessionComposite.class);
  private final static int SUCCESS = 1;
  private final static int ERROR = 2;
  private final static int NORMAL = 3;

  private static Timer timer = new Timer(true);

  private DBObjectTreeComposite dbotc = null;
  private StyledText messageText = null;
  private String sessionName = "";
  private Sash sash = null;
  private Composite sashComposite = null;
  private Composite statusComposite = null;
  private Label statusLabel = null;
  private Button statusButton = null;
  private Color defaultColor = null;
  private Shell textShell = null;
  private boolean flashing = false;
  private int lastStatusType = 0;
  private int lastSashPosition = 0;

  public SessionComposite(Composite parent, String sessionName)
  {
    super(parent, SWT.NONE);

    addDisposeListener(this);

    this.sessionName = sessionName;
    SessionData.getSessionData(this.sessionName).addSessionEndListener(this);
    SessionData.getSessionData(this.sessionName).addMessageHandler(this);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginTop = 2;
    gridLayout.marginBottom = 2;
    gridLayout.marginWidth = 2;
    gridLayout.verticalSpacing = 2;
    this.setLayout(gridLayout);

    sashComposite = new Composite(this, SWT.NONE);
    FormLayout fl = new FormLayout();
    sashComposite.setLayout(fl);
    sashComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    dbotc = new DBObjectTreeComposite(sashComposite, this, this.sessionName);

    sash = new Sash(sashComposite, SWT.VERTICAL | SWT.SMOOTH);
    sash.addSelectionListener(this);


    QueryComposite qc = new QueryComposite(sashComposite, this, this.sessionName);

    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(sash, 0);
    fd.top = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(100, 0);
    dbotc.setLayoutData(fd);

    fd = new FormData();
    int width = SessionData.getSessionData(this.sessionName).getAlias().getDBObjectTreeWidth();
    fd.left = new FormAttachment(0, width);
    fd.top = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(100, 0);
    fd.width = 4;
    sash.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(sash, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(100, 0);
    qc.setLayoutData(fd);

    statusComposite = new Composite(this, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    statusComposite.setLayout(gridLayout);
    statusComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    statusButton = new Button(statusComposite, SWT.ARROW | SWT.UP | SWT.FLAT);
    statusButton.addMouseListener(this);

    statusLabel = new Label(statusComposite, SWT.NONE);
    statusLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

    statusComposite.addMouseListener(this);
    statusLabel.addMouseListener(this);

    textShell = new Shell(this.getShell(), SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    textShell.setLayout(gridLayout);
    textShell.addShellListener(this);

    messageText = new StyledText(textShell, SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
    messageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    messageText.setIndent(5);
    messageText.setWordWrap(true);
    messageText.setLineSpacing(1);
    messageText.addFocusListener(this);
    new StyledTextContextMenu(messageText);


    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);
  }

  private void setFonts()
  {
    FontSetter.setFont(messageText, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(statusLabel, ApplicationData.getInstance().getGeneralFont());
    if (ApplicationData.getInstance().getGeneralFont() == null)
      defaultColor = null;
    else
      defaultColor = ApplicationData.getInstance().getGeneralFont().getColor();
  }

  public void setMaximized(Control control)
  {
    if (control != null)
    {
      FormData fd = (FormData)sash.getLayoutData();
      lastSashPosition = sash.getLocation().x;

      if (control == dbotc)
        fd.left = new FormAttachment(100, 0);
      else
        fd.left = new FormAttachment(0, -sash.getSize().x);

      sash.setVisible(false);
      sash.getParent().layout();
    }
    else
    {
      FormData fd = (FormData)sash.getLayoutData();
      fd.left = new FormAttachment(0, lastSashPosition);
      sash.setVisible(true);
      sash.getParent().layout();
    }
  }

  private void truncMessageText()
  {
    int maxLines = 100;
    if (messageText.getLineCount() > maxLines)
    {
      int len = messageText.getOffsetAtLine(messageText.getLineCount() - maxLines);
      messageText.getContent().replaceTextRange(0, len, "");
    }
  }

  private void scrollMessageToBottom()
  {
    int line = messageText.getLineCount() - 1;
    messageText.setTopIndex(line);
  }


  @Override
  public void addMessage(final String text, boolean onGuiThread)
  {
    if (onGuiThread)
      processMessage(text, NORMAL);
    else
    {
      this.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          processMessage(text, NORMAL);
        }
      });
    }
  }

  @Override
  public void addSuccessMessage(final String text, boolean onGuiThread)
  {
    if (onGuiThread)
      processMessage(text, SUCCESS);
    else
    {
      this.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          processMessage(text, SUCCESS);
        }
      });
    }
  }

  @Override
  public void addErrorMessage(final String text, boolean onGuiThread)
  {
    if (onGuiThread)
      processMessage(text, ERROR);
    else
    {
      this.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          processMessage(text, ERROR);
        }
      });
    }
  }

  private void flash()
  {
    if (!flashing)
    {
      flashing = true;
      statusComposite.setBackground(statusComposite.getDisplay().getSystemColor(SWT.COLOR_RED));
      statusLabel.setBackground(statusLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
      statusLabel.setForeground(statusLabel.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      statusComposite.update();
      timer.schedule(new FlashTask(), 1000);
    }
  }

  private void addStatusText(String text, int type)
  {
    lastStatusType = type;
    statusLabel.setToolTipText(text);
    statusLabel.setText(text.replaceAll("\r", " ").replaceAll("\n", " "));
    if (!flashing)
      setStatusTextColor();
    statusLabel.pack();
  }

  private void setStatusTextColor()
  {
    if (lastStatusType == SUCCESS)
    {
      statusLabel.setForeground(defaultColor);
      //statusLabel.setBackground(statusLabel.getDisplay().getSystemColor(SWT.COLOR_GREEN));
      statusLabel.setBackground(statusComposite.getBackground());
    }
    else if (lastStatusType == ERROR)
    {
      //statusLabel.setForeground(statusLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
      statusLabel.setForeground(defaultColor);
      statusLabel.setBackground(statusComposite.getBackground());
      flash();
    }
    else
    {
      statusLabel.setForeground(defaultColor);
      statusLabel.setBackground(statusComposite.getBackground());
    }
  }

  private void processMessage(String text, int type)
  {
    if (text != null && !messageText.isDisposed())
    {
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
      String pre = df.format(new Date()) + ": ";

      text = text.trim();
      if (type == ERROR)
        text = "Error - " + text;
      String msg = pre + text;

      if (messageText.getText().length() > 0)
        messageText.append(messageText.getLineDelimiter());
      int start = messageText.getText().length();
      messageText.append(msg.trim());

      if (type == SUCCESS)
      {
        StyleRange style1 = new StyleRange();
        style1.start = start;
        style1.length = msg.length();
        style1.background = messageText.getDisplay().getSystemColor(SWT.COLOR_GREEN);
        messageText.setStyleRange(style1);
      }
      else if (type == ERROR)
      {
        StyleRange style1 = new StyleRange();
        style1.start = start;
        style1.length = msg.length();
        style1.foreground = messageText.getDisplay().getSystemColor(SWT.COLOR_RED);
        messageText.setStyleRange(style1);
      }

      truncMessageText();
      scrollMessageToBottom();

      addStatusText(msg, type);
    }
  }


  public void init()
  {
    addMessage("Opening connection...", true);
    final ConnectionManager conManager = SessionData.getSessionData(sessionName).getConnectionManager();

    WorkerRunnable worker = new WorkerRunnable()
    {
      private Throwable ex = null;

      @Override
      public void doTask()
      {
        try
        {
          if (!conManager.isShutdown())
          {
            conManager.open();
          }
        }
        catch (Exception e)
        {
          ex = e;
          logger.error(e.getMessage(), e);
        }
      }

      @Override
      public void doUITask()
      {
        if (!SessionComposite.this.isDisposed())
        {
          boolean opened = true;
          if (ex != null)
          {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append(ex.getClass().getName() + ": " + ex.getMessage());

            addErrorMessage(ex.getClass().getName() + ": " + ex.getMessage(), true);

            while (ex.getCause() != null)
            {
              ex = ex.getCause();
              sbuf.append("\n").append(ex.getClass().getName() + ": " + ex.getMessage());
              addErrorMessage(ex.getClass().getName() + ": " + ex.getMessage(), true);
            }

            MessageDialog md = new MessageDialog(getShell(), SWT.OK);
            md.setText("Login Failed - " + sessionName);
            md.setMessage(sbuf.toString());
            md.open();

            opened = false;
          }

          BusyManager.setNotBusy(SessionComposite.this);
          if (opened && !conManager.isShutdown())
          {
            sashComposite.setEnabled(true);
            dbotc.init();

            dbotc.addControlListener(SessionComposite.this);

            SessionData.getSessionData(sessionName).sessionReady();
          }
          else
          {
            Stella.getInstance().closeSessionTab(sessionName);
          }
        }
      }
    };

    BusyManager.setBusy(this);
    sashComposite.setEnabled(false);
    worker.startTask();
  }

  @Override
  public void sessionEnded()
  {
    final ConnectionManager conManager = SessionData.getSessionData(sessionName).getConnectionManager();
    Runnable runnable = new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          conManager.closeAndShutdown();
        }
        catch (Exception e)
        {
          logger.error(e.getMessage(), e);
        }
      }
    };
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    thread.start();
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

  public boolean isDropped()
  {
    return textShell.getVisible();
  }

  public void dropDown(boolean drop)
  {
    if (drop == isDropped())
      return;
    if (!drop)
    {
      textShell.setVisible(false);
      return;
    }

    Point pt = this.toDisplay(statusComposite.getLocation());

    textShell.setSize(statusComposite.getSize().x, 200);

    textShell.setLocation(pt.x, pt.y - textShell.getSize().y);
    textShell.open();
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
    if (e.widget == statusComposite || e.widget == statusLabel || e.widget == statusButton)
    {
      dropDown(!isDropped());
    }
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
  }

  @Override
  public void shellActivated(ShellEvent e)
  {
  }
  @Override
  public void shellClosed(ShellEvent e)
  {
    dropDown(false);
    e.doit = false;
  }
  @Override
  public void shellDeactivated(ShellEvent e)
  {
    dropDown(false);
    e.doit = false;
  }
  @Override
  public void shellDeiconified(ShellEvent e)
  {
  }
  @Override
  public void shellIconified(ShellEvent e)
  {
  }

  @Override
  public void focusGained(FocusEvent e)
  {
  }

  @Override
  public void focusLost(FocusEvent e)
  {
    dropDown(false);
  }

  @Override
  public void controlMoved(ControlEvent controlEvent) {

  }

  @Override
  public void controlResized(ControlEvent controlEvent) {
    int width = dbotc.getSize().x;
    ApplicationData.getInstance().setAliasDBObjectTreeWidth(SessionData.getSessionData(sessionName).getAlias().getName(), width);
  }

  private class FlashTask extends TimerTask
  {
    @Override
    public void run()
    {
      if (!statusComposite.isDisposed())
      {
        statusComposite.getDisplay().asyncExec(new Runnable(){
          @Override
          public void run() {
            if (!statusComposite.isDisposed())
            {
              statusComposite.setBackground(statusComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
              setStatusTextColor();
              statusComposite.update();

              flashing = false;
            }
          }
        });
      }
    }
  }


  public static void main(String[] args)
  {
    Display display = new Display();
    final Shell shell = new Shell(display);


    Button button1 = new Button(shell, SWT.PUSH);
    button1.setText("Button 1");
    final Sash sash = new Sash(shell, SWT.VERTICAL);
    Button button2 = new Button(shell, SWT.PUSH);
    button2.setText("Button 2");

    Button button3 = new Button(shell, SWT.PUSH);
    button3.setText("Button 3");

    final FormLayout form = new FormLayout();
    shell.setLayout(form);

    FormData button1Data = new FormData();
    button1Data.left = new FormAttachment(0, 0);
    button1Data.right = new FormAttachment(sash, 0);
    //button1Data.top = new FormAttachment(0, 0);
    //button1Data.bottom = new FormAttachment(button3, 0);
    button1.setLayoutData(button1Data);

    final int limit = 20, percent = 50;
    final FormData sashData = new FormData();
    sashData.left = new FormAttachment(percent, 0);
    //sashData.top = new FormAttachment(0, 0);
    sashData.bottom = new FormAttachment(button3, 0);
    sash.setLayoutData(sashData);
    sash.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event e)
      {
        Rectangle sashRect = sash.getBounds();
        Rectangle shellRect = shell.getClientArea();
        int right = shellRect.width - sashRect.width - limit;
        e.x = Math.max(Math.min(e.x, right), limit);
        if (e.x != sashRect.x)
        {
          sashData.left = new FormAttachment(0, e.x);
          shell.layout();
        }
      }
    });

    FormData button2Data = new FormData();
    button2Data.left = new FormAttachment(sash, 0);
    button2Data.right = new FormAttachment(100, 0);
    //button2Data.top = new FormAttachment(0, 0);
    //button2Data.bottom = new FormAttachment(button3, 0);
    button2.setLayoutData(button2Data);

    FormData button3Data = new FormData();
    button3Data.height = 20;
    button3Data.left = new FormAttachment(0, 0);
    button3Data.right = new FormAttachment(100, 0);
    button3Data.top = new FormAttachment(button1, 0);
    button3Data.bottom = new FormAttachment(100, 0);
    button3.setLayoutData(button3Data);

    shell.pack();
    shell.open();

    SelectionListener listener = new SelectionAdapter(){
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        if (((Button)e.widget).getText().equals("Button 3"))
        {
          sash.setVisible(true);
          sashData.left = new FormAttachment(50, 0);
          shell.layout();
        }
        else if (((Button)e.widget).getText().equals("Button 1"))
        {
          sash.setVisible(false);
          sashData.left = new FormAttachment(0, -sash.getSize().x);
          shell.layout();
        }
        else if (((Button)e.widget).getText().equals("Button 2"))
        {
          sash.setVisible(false);
          sashData.left = new FormAttachment(100, 0);
          shell.layout();
        }
      }
    };

    button1.addSelectionListener(listener);
    button2.addSelectionListener(listener);
    button3.addSelectionListener(listener);


    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == sash)
    {
      int limit = 150;
      Rectangle sashRect = sash.getBounds();
      Rectangle shellRect = sash.getParent().getClientArea();
      int right = shellRect.width - sashRect.width - limit;
      e.x = Math.max(Math.min(e.x, right), limit);
      if (e.x != sashRect.x)
      {
        FormData fd = (FormData)sash.getLayoutData();
        fd.left = new FormAttachment(0, e.x);
        lastSashPosition = e.x;
        sash.getParent().layout();
      }
    }
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

}

