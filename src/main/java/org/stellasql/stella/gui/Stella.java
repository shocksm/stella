package org.stellasql.stella.gui;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.TextKeyListener;
import org.stellasql.stella.gui.util.WebSiteOpener;
import org.stellasql.stella.session.SessionData;
import org.stellasql.stella.util.AliasUtil;

public class Stella implements ControlListener, ShellListener
{
  private final static Logger logger = LogManager.getLogger(Stella.class);

  // TODO
  /*
   * 
   * splash screen on multiple monitor is spanning across monitors
   * 
   * Scale images and text based on screen size / DPI for now, preferable create larger images for each step up
   * org.stellasql.stella.gui.util.ImageLoader
   *   new Image(Display.getCurrent(), imageData.scaledTo(imageData.width*4, imageData.height*4));
   * Google SWT Snippet367.java
   *
   * set appropiate dilaog size based on screen size / DPI
   *    org.stellasql.stella.gui.DriverDialog
   *
   * encrypt passwords - need a key/password - random or user defined?
   *
   * finish openHelpContents() and package docs
   *
   * Change default JDBC Urls to be basic friendly
   *
   * disabled images for toolbar, run button, and tree icons
   *
   * create quoted table and columns and test query parser and select * update from grid
   * test table name lower case, upper case, mixed case with above also
   * also test with keywords as column / table names -> go, select, set
   *
   * cancel seems to hang the whole app for Oracle -> worker thread it and hide gui?
   * add key menu items for ctrl-tab ctrl-shift-tab and ctrl-pageup ctrl-pagedown
   *    Connection->Next Session Ctrl+Tab, Previous Session Ctrl+Shift+Tab
   *    SQL->Next Result Ctrl+PageUp, Previous Result Ctrl+PageDown
   * add filter for what tables / dbs show up in the db tree
   * custom key mapping
   * look into using JavaCC for the SQL parser https://javacc.dev.java.net/   *
   * test data creation tool
   * create table wizards -> this would be db specific?
   * filter option for db tree
   * add 'database filters' to do db specific tweaks i.e. oracle column type of Date should be interepted at Timestamp
   *   all calls for metadata or datatypes should filter through this with a default implementation returning direct values
   * support BLOBs
   * i18n
   * import data
   * common way to log exceptions (and look for nested exceptions) to the status text
   * import/export config
   * track memory usage
   * option to allow using single or 2 connections?
   * autocomplete
   * multiple queries ran -> stop at first error option
   * thread sytanx highlighter on modifyText and fullscan -> use queing and one thread
   * handle multiple instances? currently last to exit overwrites all config changes (same as squirrel)
   * I like the query history impl in SQLCreator
   * Option to show schemas as a folder or all together (current)
   * option to limit max number of result tabs open -> oldest would drop off
   * remove newlines from history combo and everywhere else then map back to newline when needed
   *
   * Notes:
   *  JTDS Driver sucks with Sybase.  It fails on several metadata calls
   *  and also on stored proc calls. It seems to try to store data in temp tables
   *  or other tables and does not have permissions
   *
   *  Oracle Drivers pretty much suck. If you are expecting to see time values for
   *  date columns you will need to add this to the Stella.ini file.
   *  -Doracle.jdbc.V8Compatible=true
   *
   *
   *
   */

  private static Stella stella = null;
  private Shell mainShell = null;
  private Shell displayShell = null;
  private Text displayText = null;
  private SessionTabComposite stc = null;
  private int dialogY = 0;

  private Stella(Display display)
  {
    mainShell = new Shell(display);
    mainShell.setText("Stella SQL");
    mainShell.addShellListener(this);

    // Create the layout
    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 1;
    gridLayout.marginBottom = 1;
    mainShell.setLayout(gridLayout);
    Image[] images = {StellaImages.getInstance().getAppSmallImage(),
        StellaImages.getInstance().getAppMediumImage(),
        StellaImages.getInstance().getAppBigImage()};
    mainShell.setImages(images);

    new StellaMenu(mainShell);

    StellaToolBar toolBar = new StellaToolBar(mainShell);
    toolBar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    stc = new SessionTabComposite(mainShell);
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    stc.setLayoutData(gridData);
  }

  public SessionTabComposite getSessionTabComposite()
  {
    return stc;
  }

  public void exit()
  {
    if (stc.closeAll())
      mainShell.dispose();
  }
  public int getSessionCount()
  {
    return stc.getSessionCount();
  }

  public void closeSelected()
  {
    stc.closeSelected();
  }

  public void closeSessionTab(String sessionName)
  {
    stc.closeTab(sessionName);
  }

  public SessionData getSelectedSessionData()
  {
    return stc.getSelectedSessionData();
  }

  public Shell getShell()
  {
    return mainShell;
  }

  public void displayHistory()
  {
    HistoryDialog hd = new HistoryDialog(Stella.getInstance().getShell());
    hd.open();
  }

  public void displayFavorites()
  {
    FavoritesDialog2 fd = new FavoritesDialog2(Stella.getInstance().getShell());
    fd.open();
  }

  public void addFavorite()
  {
    if (Stella.getInstance().getSelectedSessionData() != null)
      Stella.getInstance().getSelectedSessionData().getSQLActionHandler().addFavorite();
    else
    {
      FavoriteAddDialog2 fd = new FavoriteAddDialog2(Stella.getInstance().getShell(), false, true);
      fd.open();
    }
  }

  public void open()
  {
    mainShell.pack();

    int x = ApplicationData.getInstance().getPositionX();
    int y = ApplicationData.getInstance().getPositionY();
    int width = ApplicationData.getInstance().getWidth();
    int height = ApplicationData.getInstance().getHeight();

    mainShell.setLocation(x, y);
    mainShell.setSize(width, height);

    if (ApplicationData.getInstance().isMaximized())
      mainShell.setMaximized(true);

    mainShell.open();

    mainShell.addControlListener(this);

    dialogY = stc.getLocation().y;
  }


  @Override
  public void controlMoved(ControlEvent e)
  {
    if (!mainShell.getMaximized())
    {
      ApplicationData.getInstance().setPositionX(mainShell.getLocation().x);
      ApplicationData.getInstance().setPositionY(mainShell.getLocation().y);
    }
  }

  @Override
  public void controlResized(ControlEvent e)
  {
    if (!mainShell.getMaximized())
    {
      ApplicationData.getInstance().setWidth(mainShell.getSize().x);
      ApplicationData.getInstance().setHeight(mainShell.getSize().y);
    }
    ApplicationData.getInstance().setMaximized(mainShell.getMaximized());
  }

  public void displayValue(String value)
  {
    if (displayShell == null || displayShell.isDisposed())
    {
      displayShell = new Shell(mainShell, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE);
      displayShell.setImage(StellaImages.getInstance().getAppSmallImage());
      displayShell.setText("Data Viewer");
      GridLayout gridLayout  = new GridLayout();
      gridLayout.numColumns = 1;
      //gridLayout.marginHeight = 0;
      //gridLayout.marginWidth = 1;
      displayShell.setLayout(gridLayout);
      displayShell.setSize(500, 300);

      displayText = new Text(displayShell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
      FontSetter.setFont(displayText, ApplicationData.getInstance().getGeneralFont());
      new TextKeyListener(displayText);
      GridData gridData = new GridData();
      gridData.grabExcessHorizontalSpace = true;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessVerticalSpace = true;
      gridData.verticalAlignment = SWT.FILL;
      displayText.setLayoutData(gridData);

      Color color = displayText.getBackground();
      displayText.setEditable(false);
      displayText.setBackground(color);
    }

    displayText.setText(value);


    int x = mainShell.getSize().x / 2 - displayShell.getSize().x / 2;
    int y = mainShell.getSize().y / 2 - displayShell.getSize().y / 2;

    x += mainShell.getLocation().x;
    y += mainShell.getLocation().y;
    if (y < 0)
      y = 0;
    if (x < 0)
      x = 0;

    displayShell.setLocation(x, y);
    displayShell.open();
  }

  public void openDriverManager()
  {
    DriverManagementDialog dd = DriverManagementDialog.getInstance(mainShell);
    dd.open(-1, mainShell.toDisplay(0, dialogY).y);
  }

  public void openPreferencesDialog()
  {
    PreferencesDialog pd = new PreferencesDialog(mainShell);
    pd.open(-1, mainShell.toDisplay(0, dialogY).y);
  }

  private File findDocs(File file)
  {
    while (file != null)
    {
      if (!file.isDirectory())
        file = file.getParentFile();
      if (file != null)
      {
        File docDir = new File(file, "docs");
        if (docDir.exists() && docDir.isDirectory())
        {
          File htmlFile = new File(docDir, "admain.html");
          if (htmlFile.exists() && !htmlFile.isDirectory())
          {
            return htmlFile;
          }
        }

        file = file.getParentFile();
      }
    }

    return file;
  }

  public void openHelpContents()
  {
    File file = null;
    Class cls = Logger.class; // LogForJ is in the lib directory
    ProtectionDomain pDomain = cls.getProtectionDomain();
    CodeSource cSource = pDomain.getCodeSource();
    if (cSource != null)
    {
      URL loc = cSource.getLocation();
      file = new File(loc.getFile().replaceAll("%20", " "));
      file = findDocs(file);
    }

    if (file == null)
    {
      file = new File("");
      file = new File(file.getAbsolutePath());
      file = findDocs(file);
    }

    if (file == null)
    {
      cls = this.getClass();
      pDomain = cls.getProtectionDomain();
      cSource = pDomain.getCodeSource();
      if (cSource != null)
      {
        URL loc = cSource.getLocation();
        file = new File(loc.getFile().replaceAll("%20", " "));
        file = findDocs(file);
      }
    }

    String urlString = null;
    if (file != null)
    {
      urlString = "file:///" + file.getAbsolutePath();
    }
    else
    {
      urlString = "http://www.stellasql.com/support.php";
    }

    WebSiteOpener.openURL(urlString, mainShell);
  }

  public void openAboutDialog()
  {
    AboutDialog ad = new AboutDialog(mainShell);
    ad.open(-1, mainShell.toDisplay(0, dialogY).y);
  }

  public void connect(AliasVO aliasVO)
  {
    String username = aliasVO.getUsername();
    String password = aliasVO.getPassword();

    if (AliasUtil.validateAlias(aliasVO, getShell()))
    {
      if (aliasVO.getPrompt())
      {
        UserInfoDialog uid = new UserInfoDialog(mainShell, aliasVO.getName());
        uid.setUsername(username);
        uid.setPassword(password);

        if (uid.open(-1, mainShell.toDisplay(0, 100).y) == SWT.OK)
        {
          username = uid.getUsernameText();
          password = uid.getPasswordText();
          stc.addSession(aliasVO, username, password);
        }
      }
      else
      {
        stc.addSession(aliasVO, username, password);
      }
    }
  }

  public void editAlias(AliasVO aliasVO)
  {
    AliasDialog dsd = new AliasDialog(mainShell, aliasVO, true);
    dsd.setText("Connection Configuration");
    AliasVO aliasVONew = dsd.open(-1, mainShell.toDisplay(0, dialogY).y);

    if (aliasVONew != null)
    {
      ApplicationData.getInstance().updateAlias(aliasVO, aliasVONew);
      saveGeneral();
    }
  }

  public AliasVO copyAlias(AliasVO aliasVO)
  {
    AliasVO copy = new AliasVO(aliasVO);
    copy.setName("Copy of " + aliasVO.getName());

    AliasDialog dsd = new AliasDialog(mainShell, copy, false);
    dsd.setText("Connection Configuration");
    copy = dsd.open(-1, mainShell.toDisplay(0, dialogY).y);

    if (copy != null)
    {
      ApplicationData.getInstance().addAlias(copy);
      saveGeneral();
    }

    return copy;
  }

  public void deleteAlias(AliasVO aliasVO)
  {
    MessageDialog msgDialog = new MessageDialog(mainShell, SWT.OK | SWT.CANCEL);
    msgDialog.setText("Delete Connection");
    msgDialog.setMessage("Delete the '" + aliasVO.getName() + "' connection?");
    if (msgDialog.open() == SWT.OK)
    {
      ApplicationData.getInstance().removeAlias(aliasVO);
      saveGeneral();
    }
  }

  public void newAlias()
  {
    AliasDialog dsd = new AliasDialog(mainShell, null, false);
    dsd.setText("Connection Configuration");
    AliasVO aliasVO = dsd.open(-1, mainShell.toDisplay(0, dialogY).y);
    if (aliasVO != null)
    {
      ApplicationData.getInstance().addAlias(aliasVO);
      saveGeneral();
    }
  }

  public void saveGeneral()
  {
    try
    {
      ApplicationData.getInstance().saveFavorites();
    }
    catch(Exception e)
    {
      logger.error(e.getMessage(), e);

      MessageDialog messageDlg = new MessageDialog(mainShell, SWT.OK);
      messageDlg.setText("Error");
      messageDlg.setMessage(e.getMessage());
      messageDlg.open();
    }
  }

  public void saveHistory()
  {
    try
    {
      ApplicationData.getInstance().saveHistory();
    }
    catch(Exception e)
    {
      logger.error(e.getMessage(), e);

      MessageDialog messageDlg = new MessageDialog(mainShell, SWT.OK);
      messageDlg.setText("Error");
      messageDlg.setMessage(e.getMessage());
      messageDlg.open();
    }
  }

  public void saveFavorites()
  {
    try
    {
      ApplicationData.getInstance().saveFavorites();
    }
    catch(Exception e)
    {
      logger.error(e.getMessage(), e);

      MessageDialog messageDlg = new MessageDialog(mainShell, SWT.OK);
      messageDlg.setText("Error");
      messageDlg.setMessage(e.getMessage());
      messageDlg.open();
    }
  }

  public static void init(Display display)
  {
    stella = new Stella(display);
  }

  public static Stella getInstance()
  {
    return stella;
  }

  @Override
  public void shellActivated(ShellEvent e)
  {
  }
  @Override
  public void shellClosed(ShellEvent e)
  {
    e.doit = false;
    exit();
  }
  @Override
  public void shellDeactivated(ShellEvent e)
  {
  }
  @Override
  public void shellDeiconified(ShellEvent e)
  {
  }
  @Override
  public void shellIconified(ShellEvent e)
  {
  }

  public static void main(String[] args)
  {
    Display display = null;
    try
    {
      System.out.println("starting");
      logger.info("Java Version: " + System.getProperty("java.version"));

      display = new Display();

      Color color = null;
      Shell splashShell = null;

      if (args.length == 0)
      {
        color = new Color(display, 69, 153, 218);
        splashShell = new Shell(display, SWT.NONE);
        splashShell.setText("Stella SQL");
        splashShell.setBackground(color);
        // Create the layout
        GridLayout gridLayout  = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 10;
        gridLayout.marginWidth = 10;
        gridLayout.horizontalSpacing = 10;
        splashShell.setLayout(gridLayout);
        splashShell.setImage(StellaImages.getInstance().getAppSmallImage());

        Label labelImage = new Label(splashShell, SWT.NONE);
        labelImage.setImage(StellaImages.getInstance().getSplashImage());
        labelImage.setBackground(color);
        GridData gridData = new GridData();
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = SWT.CENTER;
        labelImage.setLayoutData(gridData);

        splashShell.pack();

        int x = display.getClientArea().width / 2 - splashShell.getSize().x / 2;
        int y = display.getClientArea().height / 2 - splashShell.getSize().y / 2;

        splashShell.setLocation(x, y);
        splashShell.open();
      }

      try
      {
        ApplicationData.getInstance().load();
      }
      catch (Exception e)
      {
        logger.error(e.getMessage(), e);
      }

      StellaClipBoard.init(display);
      init(display);
      getInstance().open();
      KeyListener.getInstance().init(display);

      if (splashShell != null)
      {
        splashShell.close();
        splashShell.dispose();
        color.dispose();
      }

      Shell shell = getInstance().getShell();
      while (!shell.isDisposed())
      {
        if (!display.readAndDispatch())
          display.sleep();
      }
    }
    catch (Exception e)
    {
      logger.error("Uncaught Exception: " + e.getMessage(), e);
    }
    catch (Error e)
    {
      logger.error("Uncaught Error: " + e.getMessage(), e);
    }
    finally
    {
      try
      {
        ApplicationData.getInstance().save();
      }
      catch (IOException e)
      {
        logger.error(e.getMessage(), e);
      }

      if (display != null)
        display.dispose();
    }
  }

}
