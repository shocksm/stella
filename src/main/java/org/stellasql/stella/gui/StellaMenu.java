package org.stellasql.stella.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.WorkerRunnable;
import org.stellasql.stella.session.SessionData;

public class StellaMenu implements SelectionListener, MenuListener
{
  private final static Logger logger = LogManager.getLogger(StellaMenu.class);

  private Shell mainShell = null;
  private MenuItem exitItem = null;
  private MenuItem connectionItem = null;
  private MenuItem connectAliasItem = null;
  private MenuItem closeAliasItem = null;
  private MenuItem newAliasItem = null;
  private MenuItem editAliasItem = null;
  private MenuItem copyAliasItem = null;
  private MenuItem deleteAliasItem = null;
  private MenuItem driverManagerItem = null;
  private MenuItem helpContentsItem = null;
  private MenuItem aboutItem = null;
  private MenuItem preferencesItem = null;
  private Menu fileSubmenu = null;
  private Menu editSubmenu = null;
  private Menu connectionSubmenu = null;
  private Menu sqlSubmenu = null;
  private Menu helpSubmenu = null;

  private MenuItem copyItem;
  private MenuItem sqlExecuteItem;
  private MenuItem sqlExecuteExportItem;
  private MenuItem sqlCommitItem;
  private MenuItem sqlHistoryItem;
  private MenuItem sqlFavoritesItem;
  private MenuItem sqlFavoriteAddItem;
  private MenuItem sqlRollbackItem;
  private MenuItem undoItem;
  private MenuItem redoItem;
  private MenuItem cutItem;
  private MenuItem pasteItem;
  private MenuItem clearItem;
  private MenuItem selectAllItem;
  private MenuItem removeQuotesItem;
  private MenuItem quoteTextItem;
  private MenuItem insertDateItem;
  private MenuItem fileOpenItem;
  private MenuItem fileSaveAsItem;
  private MenuItem fileExportItem;

  private String lastPath = null;

  public StellaMenu(Shell shell)
  {
    mainShell = shell;

    Menu bar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(bar);

    //----------------------------------------------------------

    MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
    fileItem.setText("&File");

    fileSubmenu = new Menu(shell, SWT.DROP_DOWN);
    fileSubmenu.addMenuListener(this);
    fileItem.setMenu(fileSubmenu);

    fileOpenItem = new MenuItem(fileSubmenu, SWT.PUSH);
    fileOpenItem.setText("&Open File...");
    fileOpenItem.addSelectionListener(this);

    fileSaveAsItem = new MenuItem(fileSubmenu, SWT.PUSH);
    fileSaveAsItem.setText("Save &As...");
    fileSaveAsItem.addSelectionListener(this);

    fileExportItem = new MenuItem(fileSubmenu, SWT.PUSH);
    fileExportItem.setText("&Export Results...");
    fileExportItem.addSelectionListener(this);

    exitItem = new MenuItem(fileSubmenu, SWT.PUSH);
    exitItem.setText("E&xit");
    exitItem.addSelectionListener(this);

    //----------------------------------------------------------
    MenuItem editItem = new MenuItem(bar, SWT.CASCADE);
    editItem.setText("&Edit");

    editSubmenu = new Menu(shell, SWT.DROP_DOWN);
    editItem.setMenu(editSubmenu);
    editSubmenu.addMenuListener(this);

    undoItem = new MenuItem(editSubmenu, SWT.PUSH);
    undoItem.setText("&Undo \tCtrl+Z");
    undoItem.addSelectionListener(this);

    redoItem = new MenuItem(editSubmenu, SWT.PUSH);
    redoItem.setText("&Redo \tCtrl+Y");
    redoItem.addSelectionListener(this);

    new MenuItem(editSubmenu, SWT.SEPARATOR);

    cutItem = new MenuItem(editSubmenu, SWT.PUSH);
    cutItem.setText("Cu&t \tCtrl+X");
    cutItem.addSelectionListener(this);

    copyItem = new MenuItem(editSubmenu, SWT.PUSH);
    copyItem.setText("&Copy \tCtrl+C");
    copyItem.addSelectionListener(this);

    pasteItem = new MenuItem(editSubmenu, SWT.PUSH);
    pasteItem.setText("&Paste \tCtrl+V");
    pasteItem.addSelectionListener(this);

    new MenuItem(editSubmenu, SWT.SEPARATOR);

    clearItem = new MenuItem(editSubmenu, SWT.PUSH);
    clearItem.setText("C&lear");
    clearItem.addSelectionListener(this);

    selectAllItem = new MenuItem(editSubmenu, SWT.PUSH);
    selectAllItem.setText("Select &all \tCtrl+A");
    selectAllItem.addSelectionListener(this);

    new MenuItem(editSubmenu, SWT.SEPARATOR);

    removeQuotesItem = new MenuItem(editSubmenu, SWT.PUSH);
    removeQuotesItem.setText("&Remove Quotes (Java syntax)");
    removeQuotesItem.addSelectionListener(this);

    quoteTextItem = new MenuItem(editSubmenu, SWT.PUSH);
    quoteTextItem.setText("Add &Quotes (Java syntax)");
    quoteTextItem.addSelectionListener(this);

    insertDateItem = new MenuItem(editSubmenu, SWT.PUSH);
    insertDateItem.setText("Insert &Date Literal");
    insertDateItem.addSelectionListener(this);


    //----------------------------------------------------------

    connectionItem = new MenuItem(bar, SWT.CASCADE);
    connectionItem.setText("&Connection");

    connectionSubmenu = new Menu(shell, SWT.DROP_DOWN);
    connectionItem.setMenu(connectionSubmenu);
    connectionSubmenu.addMenuListener(this);

    connectAliasItem = new MenuItem(connectionSubmenu, SWT.PUSH);
    connectAliasItem.setText("&Open...");
    connectAliasItem.addSelectionListener(this);

    closeAliasItem = new MenuItem(connectionSubmenu, SWT.PUSH);
    closeAliasItem.setText("C&lose");
    closeAliasItem.addSelectionListener(this);

    editAliasItem = new MenuItem(connectionSubmenu, SWT.PUSH);
    editAliasItem.setText("&Edit...");
    editAliasItem.addSelectionListener(this);

    copyAliasItem = new MenuItem(connectionSubmenu, SWT.PUSH);
    copyAliasItem.setText("&Copy...");
    copyAliasItem.addSelectionListener(this);

    newAliasItem = new MenuItem(connectionSubmenu, SWT.PUSH);
    newAliasItem.setText("&New...");
    newAliasItem.addSelectionListener(this);

    deleteAliasItem = new MenuItem(connectionSubmenu, SWT.PUSH);
    deleteAliasItem.setText("&Delete...");
    deleteAliasItem.addSelectionListener(this);

    //----------------------------------------------------------

    MenuItem sqlItem = new MenuItem(bar, SWT.CASCADE);
    sqlItem.setText("&SQL");

    sqlSubmenu = new Menu(shell, SWT.DROP_DOWN);
    sqlSubmenu.addMenuListener(this);
    sqlItem.setMenu(sqlSubmenu);

    sqlHistoryItem = new MenuItem(sqlSubmenu, SWT.PUSH);
    sqlHistoryItem.setText("&History\tCtrl+H");
    sqlHistoryItem.addSelectionListener(this);

    sqlFavoritesItem = new MenuItem(sqlSubmenu, SWT.PUSH);
    sqlFavoritesItem.setText("&Favorites\tCtrl+F");
    sqlFavoritesItem.addSelectionListener(this);

    sqlFavoriteAddItem = new MenuItem(sqlSubmenu, SWT.PUSH);
    sqlFavoriteAddItem.setText("&Add Favorite\tCtrl+D");
    sqlFavoriteAddItem.addSelectionListener(this);

    new MenuItem(sqlSubmenu, SWT.SEPARATOR);

    sqlExecuteItem = new MenuItem(sqlSubmenu, SWT.PUSH);
    sqlExecuteItem.setText("&Execute\tCtrl+Enter");
    sqlExecuteItem.addSelectionListener(this);

    sqlExecuteExportItem = new MenuItem(sqlSubmenu, SWT.PUSH);
    sqlExecuteExportItem.setText("Execute and E&xport...\tCtrl+Shift+Enter");
    sqlExecuteExportItem.addSelectionListener(this);

    sqlCommitItem = new MenuItem(sqlSubmenu, SWT.PUSH);
    sqlCommitItem.setText("&Commit");
    sqlCommitItem.addSelectionListener(this);

    sqlRollbackItem = new MenuItem(sqlSubmenu, SWT.PUSH);
    sqlRollbackItem.setText("&Rollback");
    sqlRollbackItem.addSelectionListener(this);


    //----------------------------------------------------------


    MenuItem toolsItem = new MenuItem(bar, SWT.CASCADE);
    toolsItem.setText("&Tools");

    Menu toolsSubmenu = new Menu(shell, SWT.DROP_DOWN);
    toolsItem.setMenu(toolsSubmenu);

    driverManagerItem = new MenuItem(toolsSubmenu, SWT.PUSH);
    driverManagerItem.setText("&Driver Manager...");
    driverManagerItem.addSelectionListener(this);

    preferencesItem = new MenuItem(toolsSubmenu, SWT.PUSH);
    preferencesItem.setText("&Preferences...");
    preferencesItem.addSelectionListener(this);

    //----------------------------------------------------------

    MenuItem helpItem = new MenuItem(bar, SWT.CASCADE);
    helpItem.setText("&Help");

    helpSubmenu = new Menu(shell, SWT.DROP_DOWN);
    helpSubmenu.addMenuListener(this);
    helpItem.setMenu(helpSubmenu);

    helpContentsItem = new MenuItem(helpSubmenu, SWT.PUSH);
    helpContentsItem.setText("&Help Contents");
    helpContentsItem.addSelectionListener(this);

    aboutItem = new MenuItem(helpSubmenu, SWT.PUSH);
    aboutItem.setText("&About Stella SQL");
    aboutItem.addSelectionListener(this);

    /*
    if (!ApplicationData.getInstance().getRegistered())
    {
      registerItem = new MenuItem(helpSubmenu, SWT.PUSH);
      registerItem.setText("&Register Stella SQL");
      registerItem.addSelectionListener(this);
    }
    */
  }

  private AliasVO selectAlias(int type)
  {

    AliasSelectDialog asd = new AliasSelectDialog(mainShell, type);

    Point pt = mainShell.toDisplay(75, 0);
    AliasVO aliasVO = asd.open(pt.x, pt.y);

    return aliasVO;
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == exitItem)
      Stella.getInstance().exit();
    else if (e.widget == fileSaveAsItem)
    {
      FileDialog dialog = new FileDialog(mainShell, SWT.SAVE);

      dialog.setFilterExtensions(new String[]{"*.sql", "*.ddl", "*"});
      dialog.setFilterNames(new String[]{"SQL files (*.sql)", "DDL files (*.ddl)", "all files (*)"});
      if (lastPath != null)
        dialog.setFilterPath(lastPath);

      String path = dialog.open();
      if (path != null)
      {
        saveFile(path);
      }
    }
    else if (e.widget == fileExportItem)
      Stella.getInstance().getSelectedSessionData().getSQLResultHandler().getSelectedResults().exportResults();
    else if (e.widget == fileOpenItem)
    {
      FileDialog dialog = new FileDialog(mainShell, SWT.OPEN);

      dialog.setFilterExtensions(new String[]{"*.sql", "*.ddl", "*"});
      dialog.setFilterNames(new String[]{"SQL files (*.sql)", "DDL files (*.ddl)", "all files (*)"});
      if (lastPath != null)
        dialog.setFilterPath(lastPath);

      String path = dialog.open();
      if (path != null)
      {
        openFile(path);
      }
    }
    else if (e.widget == undoItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().undo();
    }
    else if (e.widget == redoItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().redo();
    }
    else if (e.widget == copyItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().copy();
    }
    else if (e.widget == cutItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().cut();
    }
    else if (e.widget == pasteItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().paste();
    }
    else if (e.widget == clearItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().clear();
    }
    else if (e.widget == selectAllItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().selectAll();
    }
    else if (e.widget == removeQuotesItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().removeQuotes();
    }
    else if (e.widget == quoteTextItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().addQuotes();
    }
    else if (e.widget == insertDateItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLTextHandler().insertDateLiteral();
    }
    else if (e.widget == connectAliasItem)
    {
      AliasVO aliasVO = selectAlias(AliasSelectDialog.OPEN);
      if (aliasVO != null)
        Stella.getInstance().connect(aliasVO);
    }
    else if (e.widget == closeAliasItem)
      Stella.getInstance().closeSelected();
    else if (e.widget == editAliasItem)
    {
      AliasVO aliasVO = selectAlias(AliasSelectDialog.EDIT);
      if (aliasVO != null)
        Stella.getInstance().editAlias(aliasVO);
    }
    else if (e.widget == copyAliasItem)
    {
      AliasVO aliasVO = selectAlias(AliasSelectDialog.COPY);
      if (aliasVO != null)
        Stella.getInstance().copyAlias(aliasVO);
    }
    else if (e.widget == newAliasItem)
    {
      Stella.getInstance().newAlias();
    }
    else if (e.widget == deleteAliasItem)
    {
      AliasVO aliasVO = selectAlias(AliasSelectDialog.DELETE);
      if (aliasVO != null)
        Stella.getInstance().deleteAlias(aliasVO);
    }
    else if (e.widget == sqlHistoryItem)
    {
      Stella.getInstance().displayHistory();
    }
    else if (e.widget == sqlFavoritesItem)
    {
      Stella.getInstance().displayFavorites();
    }
    else if (e.widget == sqlFavoriteAddItem)
    {
      Stella.getInstance().addFavorite();
    }
    else if (e.widget == sqlExecuteItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLActionHandler().execute();
    }
    else if (e.widget == sqlExecuteExportItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLActionHandler().executeExport();
    }
    else if (e.widget == sqlCommitItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLActionHandler().commit();
    }
    else if (e.widget == sqlRollbackItem)
    {
      Stella.getInstance().getSelectedSessionData().getSQLActionHandler().rollback();
    }
    else if (e.widget == driverManagerItem)
    {
      Stella.getInstance().openDriverManager();
    }
    else if (e.widget == preferencesItem)
    {
      Stella.getInstance().openPreferencesDialog();
    }
    else if (e.widget == helpContentsItem)
    {
      Stella.getInstance().openHelpContents();
    }
    else if (e.widget == aboutItem)
    {
      Stella.getInstance().openAboutDialog();
    }
  }

  private void saveFile(String path)
  {
    final File file = new File(path);
    String text = Stella.getInstance().getSelectedSessionData().getSQLTextHandler().getText();
    String lineSep = System.getProperty("line.separator");
    text = text.replaceAll("\n", lineSep);

    if (!file.isDirectory() && file.getParentFile() != null && file.getParentFile().isDirectory())
      lastPath = file.getParentFile().getPath();

    final String textOut = text;
    text = null;

    boolean write = true;
    if (file.exists())
    {
      MessageDialog messageDlg = new MessageDialog(mainShell, SWT.OK | SWT.CANCEL);
      messageDlg.setText("File exists");
      messageDlg.setMessage("The file '" + file.toString() + "' already exists.\nOverwrite it?");
      if (messageDlg.open() == SWT.CANCEL)
        write = false;
    }

    if (!write)
      return;

    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;
      @Override
      public void doTask()
      {
        try
        {
          FileOutputStream fos = new FileOutputStream(file);
          fos.write(textOut.getBytes());
          fos.close();
        }
        catch (Exception e)
        {
          logger.error(e.getMessage(), e);
          ex = e;
        }
      }

      @Override
      public void doUITask()
      {
        if (!mainShell.isDisposed())
        {
          if (ex != null)
          {
            MessageDialog messageDlg = new MessageDialog(mainShell, SWT.OK);
            messageDlg.setText("Error");
            messageDlg.setMessage(ex.getClass().getName() + "\n\n" + ex.getMessage());
            messageDlg.open();
          }
          else
          {
            SessionData sd = Stella.getInstance().getSelectedSessionData();
            if (sd != null)
              sd.addMessage("File saved", true);
          }

          BusyManager.setNotBusy(mainShell);
        }
      }
    };

    BusyManager.setBusy(mainShell);
    worker.startTask();

  }

  private void openFile(String path)
  {
    final File file = new File(path);

    if (!file.isDirectory() && file.getParentFile() != null && file.getParentFile().isDirectory())
      lastPath = file.getParentFile().getPath();

    boolean append = false;
    if (Stella.getInstance().getSelectedSessionData().getSQLTextHandler().hasText())
    {
      MessageDialog messageDlg = new MessageDialog(mainShell, SWT.OK | SWT.CANCEL);
      messageDlg.setOkText("&Append");
      messageDlg.setCancelText("&Replace");
      messageDlg.setText("Text exists");
      messageDlg.setMessage("Append to or replace the existing text?");
      if (messageDlg.open() == SWT.OK)
        append = true;
    }

    final boolean appendText = append;

    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;
      StringBuffer sbuf = new StringBuffer();
      @Override
      public void doTask()
      {
        try
        {
          BufferedReader br = new BufferedReader(new FileReader(file));
          String line = null;
          while ((line = br.readLine()) != null)
          {
            sbuf.append(line + "\n");
          }
          br.close();
        }
        catch (Exception e)
        {
          logger.error(e.getMessage(), e);
          ex = e;
        }
      }

      @Override
      public void doUITask()
      {
        if (!mainShell.isDisposed())
        {
          if (ex != null)
          {
            MessageDialog messageDlg = new MessageDialog(mainShell, SWT.OK);
            messageDlg.setText("Error");
            messageDlg.setMessage(ex.getClass().getName() + "\n\n" + ex.getMessage());
            messageDlg.open();
          }
          else
          {
            SessionData sd = Stella.getInstance().getSelectedSessionData();
            if (sd != null)
            {
              if (appendText)
                sd.getSQLTextHandler().appendText(sbuf.toString());
              else
                sd.getSQLTextHandler().setText(sbuf.toString());
            }
          }

          BusyManager.setNotBusy(mainShell);
        }
      }
    };

    BusyManager.setBusy(mainShell);
    worker.startTask();

  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }

  @Override
  public void menuShown(MenuEvent e)
  {
    if (e.widget == fileSubmenu)
    {
      boolean enabled = Stella.getInstance().getSelectedSessionData() != null;
      boolean export = false;
      if (enabled)
        export = Stella.getInstance().getSelectedSessionData().getSQLResultHandler().getSelectedResults() != null;
      fileOpenItem.setEnabled(enabled);
      fileSaveAsItem.setEnabled(enabled);
      fileExportItem.setEnabled(enabled && export);
    }
    else if (e.widget == connectionSubmenu)
    {
      int count = ApplicationData.getInstance().getAliasCount();

      connectAliasItem.setEnabled(count > 0);
      editAliasItem.setEnabled(count > 0);
      copyAliasItem.setEnabled(count > 0);
      deleteAliasItem.setEnabled(count > 0);

      closeAliasItem.setEnabled(Stella.getInstance().getSessionCount() > 0);
    }
    else if (e.widget == sqlSubmenu)
    {
      boolean enabled = Stella.getInstance().getSelectedSessionData() != null;
      boolean autoCommit = false;
      if (enabled)
        autoCommit = Stella.getInstance().getSelectedSessionData().getSQLActionHandler().isAutoCommit();

      sqlExecuteItem.setEnabled(enabled);
      sqlExecuteExportItem.setEnabled(enabled);
      sqlCommitItem.setEnabled(enabled && !autoCommit);
      sqlRollbackItem.setEnabled(enabled && !autoCommit);
    }
    else if (e.widget == editSubmenu)
    {
      boolean enabled = Stella.getInstance().getSelectedSessionData() != null;
      boolean selected = false;
      boolean undo = false;
      boolean redo = false;
      if (enabled)
      {
        selected = Stella.getInstance().getSelectedSessionData().getSQLTextHandler().textSelected();
        undo = Stella.getInstance().getSelectedSessionData().getSQLTextHandler().undoAvailable();
        redo = Stella.getInstance().getSelectedSessionData().getSQLTextHandler().redoAvailable();
      }
      undoItem.setEnabled(enabled && undo);
      redoItem.setEnabled(enabled && redo);
      cutItem.setEnabled(enabled && selected);
      copyItem.setEnabled(enabled && selected);
      pasteItem.setEnabled(enabled);
      clearItem.setEnabled(enabled);
      selectAllItem.setEnabled(enabled);
      removeQuotesItem.setEnabled(enabled);
      quoteTextItem.setEnabled(enabled);
      insertDateItem.setEnabled(enabled);
    }
    else if (e.widget == helpSubmenu)
    {

    }
  }

}
