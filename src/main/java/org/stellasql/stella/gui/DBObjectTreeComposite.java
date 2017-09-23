package org.stellasql.stella.gui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.ProcedureColumnInfo;
import org.stellasql.stella.ProcedureInfo;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.gui.statement.DataTypeUtil;
import org.stellasql.stella.gui.statement.DeleteDialog;
import org.stellasql.stella.gui.statement.InsertDialog;
import org.stellasql.stella.gui.statement.UpdateDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.session.DBObjectListener;
import org.stellasql.stella.session.SessionData;

public class DBObjectTreeComposite extends Composite implements TreeListener, MouseTrackListener, MenuListener, DBObjectListener, SelectionListener, KeyListener, DisposeListener, FontChangeListener, MouseListener
{
  private Composite compositeInner = null;
  private Tree tree = null;
  private String sessionName = "";
  private Menu menu = null;
  private MenuItem copyMI;
  private List tableTypes = null;
  private ToolItem refreshButton = null;
  private ToolItem insertButton = null;
  private ToolItem updateButton = null;
  private ToolItem deleteButton = null;
  private ToolItem infoButton = null;
  private ToolItem contentsButton = null;
  private ToolItem maximizeButton = null;
  private String action = null;
  private boolean maximized = false;
  private SessionComposite sessionComposite;

  private static final String CATALOG_ITEM = "CATALOG_ITEM";
  private static final String TABLETYPE_ITEM = "TABLETYPE_ITEM";
  private static final String PROCEDUREFOLDER_ITEM = "PROCEDUREFOLDER_ITEM";
  private static final String TALBEPLACEHOLDER_ITEM = "TABLEPLACEHOLDER_ITEM";
  private static final String PROCEDUREPLACEHOLDER_ITEM = "PROCEDUREPLACEHOLDER_ITEM";
  private static final String COLUMNPLACEHOLDER_ITEM = "COLUMNPLACEHOLDER_ITEM";
  private static final String PROCEDURECOLUMNPLACEHOLDER_ITEM = "PROCEDURECOLUMNPLACEHOLDER_ITEM";
  private static final String TABLE_ITEM = "TABLE_ITEM";
  private static final String PROCEDURE_ITEM = "PROCEDURE_ITEM";
  private static final String COLUMN_ITEM = "COLUMN_ITEM";
  private static final String PROCEDURE_COLUMN_ITEM = "PROCEDURE_COLUMN_ITEM";
  private static final String CATALOG_NAME_KEY = "CATALOG_NAME";
  private static final String COLUMN_DATA_KEY = "COLUMN_DATA";
  private static final String PROCEDURE_COLUMN_DATA_KEY = "PROCEDURE_COLUMN_DATA_KEY";
  private static final String TABLETYPE_KEY = "TABLETYPE";
  private static final String TABLE_DATA_KEY = "TABLE_DATA";
  private static final String PROCEDURE_DATA_KEY = "PROCEDURE_DATA_KEY";
  private static final String TABLE_INFO = "TABLE_INFO";
  private static final String ROW_COUNT = "ROW_COUNT";
  private static final String TABLE_CONTENT = "TABLE_CONTENT";
  private static final String REFRESH_ITEM = "REFRESH_ITEM";
  private static final String REFRESH_TREE = "REFRESH_TREE";
  private static final String TABLE_INSERT = "TABLE_INSERT";
  private static final String TABLE_UPDATE = "TABLE_UPDATE";
  private static final String TABLE_DELETE = "TABLE_DELETE";

  public DBObjectTreeComposite(Composite parent, SessionComposite sessionComposite, String sessionName)
  {
    super(parent, SWT.NONE);
    this.sessionComposite = sessionComposite;

    addDisposeListener(this);

    this.sessionName = sessionName;

    SessionData.getSessionData(this.sessionName).getDBObjectRetriever().addListener(this);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    this.setLayout(gridLayout);
    this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

    compositeInner = new Composite(this, SWT.NONE);
    compositeInner.addMouseListener(this);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.verticalSpacing = 0;
    compositeInner.setLayout(gridLayout);
    compositeInner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Composite compositeToolbar = new Composite(compositeInner, SWT.NONE);
    compositeToolbar.addMouseListener(this);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 2;
    gridLayout.marginWidth = 2;
    compositeToolbar.setLayout(gridLayout);
    compositeToolbar.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    ToolBar toolBar = new ToolBar(compositeToolbar, SWT.FLAT);
    refreshButton = new ToolItem(toolBar, SWT.PUSH);
    refreshButton.setToolTipText("Refresh the DB Object tree");
    refreshButton.setImage(StellaImages.getInstance().getRefreshImage());
    refreshButton.addSelectionListener(this);
    refreshButton.setEnabled(false);

    infoButton = new ToolItem(toolBar, SWT.PUSH);
    infoButton.setToolTipText("Display information for selected table");
    infoButton.setImage(StellaImages.getInstance().getTableInfoImage());
    infoButton.addSelectionListener(this);
    infoButton.setEnabled(false);

    contentsButton = new ToolItem(toolBar, SWT.PUSH);
    contentsButton.setToolTipText("Display contents of selected table");
    contentsButton.setImage(StellaImages.getInstance().getTableViewImage());
    contentsButton.addSelectionListener(this);
    contentsButton.setEnabled(false);

    insertButton = new ToolItem(toolBar, SWT.PUSH);
    insertButton.setToolTipText("Create insert statement for the selected table");
    insertButton.setImage(StellaImages.getInstance().getInsertImage());
    insertButton.addSelectionListener(this);
    insertButton.setEnabled(false);

    updateButton = new ToolItem(toolBar, SWT.PUSH);
    updateButton.setToolTipText("Create update statement for the selected table");
    updateButton.setImage(StellaImages.getInstance().getUpdateImage());
    updateButton.addSelectionListener(this);
    updateButton.setEnabled(false);

    deleteButton = new ToolItem(toolBar, SWT.PUSH);
    deleteButton.setToolTipText("Create delete statement for the selected table");
    deleteButton.setImage(StellaImages.getInstance().getTableDeleteImage());
    deleteButton.addSelectionListener(this);
    deleteButton.setEnabled(false);


    ToolBar toolBar2 = new ToolBar(compositeToolbar, SWT.FLAT);
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.END;
    gridData.verticalAlignment = SWT.BEGINNING;
    toolBar2.setLayoutData(gridData);
    maximizeButton = new ToolItem(toolBar2, SWT.PUSH);
    maximizeButton.setToolTipText("Maximize");
    maximizeButton.setImage(StellaImages.getInstance().getMaximizeImage());
    maximizeButton.addSelectionListener(this);
    maximizeButton.setEnabled(false);


    Composite compositeTree = new Composite(compositeInner, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.marginTop = 1;
    compositeTree.setLayout(gridLayout);
    compositeTree.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
    compositeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    tree = new Tree(compositeTree, SWT.NONE);
    tree.addTreeListener(this);
    tree.addKeyListener(this);
    tree.addSelectionListener(this);
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    tree.setEnabled(false);

    menu = new Menu(tree);
    menu.addMenuListener(this);
    tree.setMenu(menu);

    copyMI = new MenuItem(menu, SWT.PUSH);
    copyMI.addSelectionListener(this);
    copyMI.setText("&Copy\tCtrl+C");

    tree.addMouseTrackListener(this);
    tree.setToolTipText("");

    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);

    compositeInner.pack();
  }

  private void setFonts()
  {
    FontSetter.setFont(tree, ApplicationData.getInstance().getTreeFont());
  }

  public void init()
  {
    refreshButton.setEnabled(true);
    maximizeButton.setEnabled(true);
    tree.setEnabled(true);

    TreeItem item = new TreeItem(tree, SWT.NULL);
    item.setText("Loading...");
    compositeInner.setEnabled(false); // prevent input to the tree
    BusyManager.setBusy(this);
  }

  public void toggleMaximize()
  {
    setMaximized(!maximized);
  }

  public void setMaximized(boolean maximized)
  {
    if (maximized)
    {
      this.maximized = true;
      maximizeButton.setToolTipText("Restore");
      maximizeButton.setImage(StellaImages.getInstance().getRestoreImage());
      sessionComposite.setMaximized(this);
    }
    else
    {
      this.maximized = false;
      maximizeButton.setToolTipText("Maximize");
      maximizeButton.setImage(StellaImages.getInstance().getMaximizeImage());
      sessionComposite.setMaximized(null);
    }
  }


  private void getTables(final TreeItem parentItem)
  {
    String tableType = (String)parentItem.getData(TABLETYPE_KEY);
    String catalogName = (String)parentItem.getData(CATALOG_NAME_KEY);
    compositeInner.setEnabled(false); // prevent input to the tree
    BusyManager.setBusy(this);
    SessionData.getSessionData(sessionName).getDBObjectRetriever().getTables(catalogName, tableType);
  }

  private void getProcedures(final TreeItem parentItem)
  {
    String catalogName = (String)parentItem.getData(CATALOG_NAME_KEY);
    compositeInner.setEnabled(false); // prevent input to the tree
    BusyManager.setBusy(this);
    SessionData.getSessionData(sessionName).getDBObjectRetriever().getProcedures(catalogName);
  }

  private void getTableInfo(final TreeItem parentItem)
  {
    TableInfo tableInfo = (TableInfo)parentItem.getData(TABLE_DATA_KEY);
    compositeInner.setEnabled(false); // prevent input to the tree
    BusyManager.setBusy(this);
    SessionData.getSessionData(sessionName).getDBObjectRetriever().getColumns(tableInfo);
  }

  private void getProcedureInfo(final TreeItem parentItem)
  {
    ProcedureInfo procInfo = (ProcedureInfo)parentItem.getData(PROCEDURE_DATA_KEY);
    compositeInner.setEnabled(false); // prevent input to the tree
    BusyManager.setBusy(this);
    SessionData.getSessionData(sessionName).getDBObjectRetriever().getProcedureColumns(procInfo);
  }


  @Override
  public void catalogDataAvailable(final List tableTypes, final List catalogs)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        processCatalogs(tableTypes, catalogs);
      }
    });
  }

  @Override
  public void tableDataAvailable(final String catalog, final String tableType, final List tableInfoList)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        processTables(catalog, tableType, tableInfoList);
      }
    });
  }


  @Override
  public void procedureDataAvailable(final String catalog, final List procedureInfoList)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        processProcedures(catalog, procedureInfoList);
      }
    });
  }

  @Override
  public void columnDataAvailable(final TableInfo tableInfo)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        processColumns(tableInfo);
      }
    });
  }

  private void processColumns(TableInfo tableInfo)
  {
    if (!tree.isDisposed())
    {
      TreeItem parentItem = findItem(tableInfo);
      parentItem.removeAll();

      if (tableInfo.getColumns() != null)
      {
        Iterator it = tableInfo.getColumns().iterator();
        while (it.hasNext())
        {
          ColumnInfo columnInfo = (ColumnInfo)it.next();
          TreeItem item = new TreeItem(parentItem, SWT.NULL);
          item.setData(COLUMN_ITEM);
          item.setData(TABLE_DATA_KEY, tableInfo);
          item.setData(COLUMN_DATA_KEY, columnInfo);

          item.setText(columnInfo.getColumnName());
          item.setImage(StellaImages.getInstance().getColumnImage());

          tree.update(); // force redraw of the tree to avoid it blanking out
                           // while new items are being added
        }
        parentItem.setExpanded(true);
      }

      compositeInner.setEnabled(true);
      BusyManager.setNotBusy(DBObjectTreeComposite.this);
    }
  }

  @Override
  public void procedureColumnDataAvailable(final ProcedureInfo procInfo)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        processProcedureColumns(procInfo);
      }
    });
  }

  private void processProcedureColumns(ProcedureInfo procInfo)
  {
    if (!tree.isDisposed())
    {
      TreeItem parentItem = findItem(procInfo);
      parentItem.removeAll();

      if (procInfo.getColumns() != null)
      {
        Iterator it = procInfo.getColumns().iterator();
        while (it.hasNext())
        {
          ProcedureColumnInfo columnInfo = (ProcedureColumnInfo)it.next();
          TreeItem item = new TreeItem(parentItem, SWT.NULL);
          item.setData(PROCEDURE_COLUMN_ITEM);
          item.setData(PROCEDURE_DATA_KEY, procInfo);
          item.setData(PROCEDURE_COLUMN_DATA_KEY, columnInfo);

          item.setText(columnInfo.getColumnName() + " (" + columnInfo.getTypeName() + ")" + " " + DataTypeUtil.getTypename(columnInfo.getDataType()));
          item.setImage(StellaImages.getInstance().getColumnImage());

          tree.update(); // force redraw of the tree to avoid it blanking out
                           // while new items are being added
        }
        parentItem.setExpanded(true);
      }

      compositeInner.setEnabled(true);
      BusyManager.setNotBusy(DBObjectTreeComposite.this);
    }
  }

  private TreeItem findItem(ProcedureInfo procInfo)
  {
    TreeItem found = null;

    TreeItem parent = findProcedureFolderItem(procInfo.getCatalog());
    TreeItem items[] = parent.getItems();
    for (int index = 0; index < items.length; index++)
    {
      ProcedureInfo itemProcedureInfo = (ProcedureInfo)items[index].getData(PROCEDURE_DATA_KEY);
      if (procInfo == itemProcedureInfo)
      {
        found = items[index];
        break;
      }
    }

    return found;
  }

  private TreeItem findItem(TableInfo tableInfo)
  {
    TreeItem found = null;

    TreeItem parent = findItem(tableInfo.getCatalog(), tableInfo.getType());
    TreeItem items[] = parent.getItems();
    for (int index = 0; index < items.length; index++)
    {
      TableInfo itemTableInfo = (TableInfo)items[index].getData(TABLE_DATA_KEY);
      if (tableInfo == itemTableInfo)
      {
        found = items[index];
        break;
      }
    }

    return found;
  }

  private TreeItem findItem(String catalog, String tableType)
  {
    TreeItem found = null;

    TreeItem items[] = tree.getItems();
    if (catalog != null)
    {
      for (int index = 0; index < items.length; index++)
      {
        if (((String)items[index].getData(CATALOG_NAME_KEY)).equals(catalog))
        {
          items = items[index].getItems();
          break;
        }
      }
    }

    for (int index = 0; index < items.length; index++)
    {
      if (items[index].getData(TABLETYPE_KEY) != null &&
          ((String)items[index].getData(TABLETYPE_KEY)).equals(tableType))
      {
        found = items[index];
        break;
      }
    }

    return found;
  }

  private TreeItem findProcedureFolderItem(String catalog)
  {
    TreeItem found = null;

    TreeItem items[] = tree.getItems();
    if (catalog != null)
    {
      for (int index = 0; index < items.length; index++)
      {
        if (((String)items[index].getData(CATALOG_NAME_KEY)).equals(catalog))
        {
          items = items[index].getItems();
          break;
        }
      }
    }

    for (int index = 0; index < items.length; index++)
    {
      if (items[index].getData() == PROCEDUREFOLDER_ITEM)
      {
        found = items[index];
        break;
      }
    }

    return found;
  }

  private void processProcedures(String catalog, List procedureInfoList)
  {
    if (!tree.isDisposed())
    {
      TreeItem parentItem = findProcedureFolderItem(catalog);
      parentItem.removeAll();
      if (procedureInfoList == null || procedureInfoList.size() == 0)
      {
        TreeItem item = new TreeItem(parentItem, SWT.NULL);
        item.setText("This item is empty or the user account does not have access");
        item.setImage(StellaImages.getInstance().getInactiveImage());
        parentItem.setExpanded(true);
      }
      else
      {
        for(Iterator it = procedureInfoList.iterator(); it.hasNext();)
        {
          ProcedureInfo procedureInfo = (ProcedureInfo)it.next();
          TreeItem item = new TreeItem(parentItem, SWT.NULL);
          item.setData(PROCEDURE_ITEM);
          item.setData(PROCEDURE_DATA_KEY, procedureInfo);
          item.setImage(StellaImages.getInstance().getTableImage());

          if (SessionData.getSessionData(sessionName).getConnectionManager().getUseSchemas())
          {
            item.setText(procedureInfo.getSchema()
                + SessionData.getSessionData(sessionName).getConnectionManager().getSeparator()
                + procedureInfo.getName());
          }
          else
          {
            item.setText(procedureInfo.getName());
          }

          TreeItem placeholderItem = new TreeItem(item, SWT.NULL);
          placeholderItem.setData(PROCEDURECOLUMNPLACEHOLDER_ITEM);
          placeholderItem.setText("Loading...");

          tree.update(); // force redraw of the tree to avoid it blanking out
                           // while new items are being added
        }

        parentItem.setExpanded(true);
      }

      compositeInner.setEnabled(true);
      BusyManager.setNotBusy(DBObjectTreeComposite.this);
    }
  }

  private void processTables(String catalog, String tableType, List tableInfoList)
  {
    if (!tree.isDisposed())
    {
      TreeItem parentItem = findItem(catalog, tableType);
      parentItem.removeAll();
      if (tableInfoList == null || tableInfoList.size() == 0)
      {
        TreeItem item = new TreeItem(parentItem, SWT.NULL);
        item.setText("This item is empty or the user account does not have access");
        item.setImage(StellaImages.getInstance().getInactiveImage());
        parentItem.setExpanded(true);
      }
      else
      {
        for(Iterator it = tableInfoList.iterator(); it.hasNext();)
        {
          TableInfo tableInfo = (TableInfo)it.next();
          TreeItem item = new TreeItem(parentItem, SWT.NULL);
          item.setData(TABLE_ITEM);
          item.setData(TABLE_DATA_KEY, tableInfo);
          item.setImage(StellaImages.getInstance().getTableImage());

          if (SessionData.getSessionData(sessionName).getConnectionManager().getUseSchemas())
          {
            item.setText(tableInfo.getSchema()
                + SessionData.getSessionData(sessionName).getConnectionManager().getSeparator()
                + tableInfo.getName());
          }
          else
          {
            item.setText(tableInfo.getName());
          }

          TreeItem placeholderItem = new TreeItem(item, SWT.NULL);
          placeholderItem.setData(COLUMNPLACEHOLDER_ITEM);
          placeholderItem.setText("Loading...");

          tree.update(); // force redraw of the tree to avoid it blanking out
                           // while new items are being added
        }

        parentItem.setExpanded(true);
      }

      compositeInner.setEnabled(true);
      BusyManager.setNotBusy(DBObjectTreeComposite.this);
    }
  }

  private void processCatalogs(List tableTypes, List catalogs)
  {
    if (!tree.isDisposed())
    {
      tree.removeAll();

      this.tableTypes = tableTypes;

      if (catalogs != null && catalogs.size() > 0)
      {
        Iterator it = catalogs.iterator();
        while (it.hasNext())
        {
          String catalogName = (String)it.next();
          TreeItem catalogItem = new TreeItem(tree, SWT.NULL);
          catalogItem.setData(CATALOG_ITEM);
          catalogItem.setData(CATALOG_NAME_KEY, catalogName);
          catalogItem.setText(catalogName);
          catalogItem.setImage(StellaImages.getInstance().getDatabaseImage());

          Iterator itTypes = tableTypes.iterator();
          while (itTypes.hasNext())
          {
            String typeName = (String)itTypes.next();

            TreeItem typeItem = new TreeItem(catalogItem, SWT.NULL);
            typeItem.setData(TABLETYPE_ITEM);
            typeItem.setData(TABLETYPE_KEY, typeName);
            typeItem.setData(CATALOG_NAME_KEY, catalogName);
            typeItem.setText(typeName);
            typeItem.setImage(StellaImages.getInstance().getFolderImage());

            TreeItem placeholderItem = new TreeItem(typeItem, SWT.NULL);
            placeholderItem.setData(TALBEPLACEHOLDER_ITEM);
            placeholderItem.setText("Loading...");
          }

          TreeItem typeItem = new TreeItem(catalogItem, SWT.NULL);
          typeItem.setData(PROCEDUREFOLDER_ITEM);
          typeItem.setData(CATALOG_NAME_KEY, catalogName);
          typeItem.setText("PROCEDURE");
          typeItem.setImage(StellaImages.getInstance().getFolderImage());

          TreeItem placeholderItem = new TreeItem(typeItem, SWT.NULL);
          placeholderItem.setData(PROCEDUREPLACEHOLDER_ITEM);
          placeholderItem.setText("Loading...");

        }
      }
      else if (tableTypes != null)
      {
        Iterator it = tableTypes.iterator();
        while (it.hasNext())
        {
          String typeName = (String)it.next();

          TreeItem typeItem = new TreeItem(tree, SWT.NULL);
          typeItem.setData(TABLETYPE_ITEM);
          typeItem.setData(TABLETYPE_KEY, typeName);
          typeItem.setText(typeName);
          typeItem.setImage(StellaImages.getInstance().getFolderImage());

          TreeItem placeholderItem = new TreeItem(typeItem, SWT.NULL);
          placeholderItem.setData(TALBEPLACEHOLDER_ITEM);
          placeholderItem.setText("Loading...");
        }

        TreeItem typeItem = new TreeItem(tree, SWT.NULL);
        typeItem.setData(PROCEDUREFOLDER_ITEM);
        typeItem.setText("PROCEDURE");
        typeItem.setImage(StellaImages.getInstance().getFolderImage());

        TreeItem placeholderItem = new TreeItem(typeItem, SWT.NULL);
        placeholderItem.setData(PROCEDUREPLACEHOLDER_ITEM);
        placeholderItem.setText("Loading...");
      }

      compositeInner.setEnabled(true);
      BusyManager.setNotBusy(DBObjectTreeComposite.this);
    }

  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {
    if (tree.getSelectionCount() > 0)
    {
      MenuItem[] menuItems = menu.getItems();
      for (int i = 1; i < menuItems.length; i++)
      {
        menuItems[i].dispose();
      }

      TreeItem item = tree.getSelection()[0];
      if (item != null && (item.getData() == COLUMN_ITEM || item.getData() == TABLE_ITEM))
      {
        TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);

        new MenuItem(menu, SWT.SEPARATOR);

        MenuItem menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("&Table Info");
        menuItem.setData(TABLE_INFO);
        menuItem.setData(TABLE_DATA_KEY, tableInfo);
        menuItem.addSelectionListener(this);

        menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("&Row Count");
        menuItem.setData(ROW_COUNT);
        menuItem.setData(TABLE_DATA_KEY, tableInfo);
        menuItem.addSelectionListener(this);

        menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("&View Content");
        menuItem.setData(TABLE_CONTENT);
        menuItem.setData(TABLE_DATA_KEY, tableInfo);
        menuItem.addSelectionListener(this);

        menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("Create &Insert Statement");
        menuItem.setData(TABLE_INSERT);
        menuItem.setData(TABLE_DATA_KEY, tableInfo);
        menuItem.addSelectionListener(this);

        menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("Create &Update Statement");
        menuItem.setData(TABLE_UPDATE);
        menuItem.setData(TABLE_DATA_KEY, tableInfo);
        menuItem.addSelectionListener(this);

        menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("Create &Delete Statement");
        menuItem.setData(TABLE_DELETE);
        menuItem.setData(TABLE_DATA_KEY, tableInfo);
        menuItem.addSelectionListener(this);
      }

      if (item != null)
      {
        new MenuItem(menu, SWT.SEPARATOR);
        MenuItem menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("&Refresh Item");
        menuItem.setData(REFRESH_ITEM);
        if (item.getData() == COLUMN_ITEM)
          menuItem.setData(REFRESH_ITEM, item.getParentItem());
        else
          menuItem.setData(REFRESH_ITEM, item);
        menuItem.addSelectionListener(this);

        menuItem = new MenuItem (menu, SWT.NONE);
        menuItem.setText("&Refresh Tree");
        menuItem.setData(REFRESH_TREE);
        menuItem.addSelectionListener(this);
      }
    }
  }

  @Override
  public void treeCollapsed(TreeEvent e)
  {
    TreeItem item = (TreeItem)e.item;
    if (item.getData() == TABLETYPE_ITEM)
      item.setImage(StellaImages.getInstance().getFolderImage());
  }
  @Override
  public void treeExpanded(TreeEvent e)
  {
    TreeItem item = (TreeItem)e.item;

    if (item.getData() == TABLETYPE_ITEM)
      item.setImage(StellaImages.getInstance().getFolderOpenImage());

    if (item.getItemCount() == 1
        && item.getItem(0).getData() == TALBEPLACEHOLDER_ITEM)
    {
      getTables(item);
    }
    else if (item.getItemCount() == 1
        && item.getItem(0).getData() == PROCEDUREPLACEHOLDER_ITEM)
    {
      getProcedures(item);
    }
    else if (item.getItemCount() == 1
        && item.getItem(0).getData() == COLUMNPLACEHOLDER_ITEM)
    {
      getTableInfo(item);
    }
    else if (item.getItemCount() == 1
        && item.getItem(0).getData() == PROCEDURECOLUMNPLACEHOLDER_ITEM)
    {
      getProcedureInfo(item);
    }
  }

  @Override
  public void mouseEnter(MouseEvent e)
  {
  }

  @Override
  public void mouseExit(MouseEvent e)
  {
  }

  @Override
  public void mouseHover(MouseEvent e)
  {
    TreeItem item = tree.getItem(new Point(e.x, e.y));
    if (item != null && item.getData() == COLUMN_ITEM)
    {
      StringBuffer sbuf = new StringBuffer();
      ColumnInfo columnInfo = (ColumnInfo)item.getData(COLUMN_DATA_KEY);
      sbuf.append("Column: " + columnInfo.getColumnName() + "\n");
      sbuf.append("Type: " + columnInfo.getTypeName() + "\n");
      sbuf.append("Size: " + columnInfo.getColumnSize() + "\n");
      sbuf.append("Decimal Digits: " + columnInfo.getDecimalDigits() + "\n");
      sbuf.append("Nullable: " + columnInfo.getNullable());
      if (columnInfo.getDefault() != null)
        sbuf.append("\nDefault: " + columnInfo.getDefault());

      tree.setToolTipText(sbuf.toString());
    }
    else if (item != null)
    {
      int itemWidth = item.getBounds().x + item.getBounds().width;
      int treeWidth = tree.getSize().x - 2 * tree.getBorderWidth();
      treeWidth -= tree.getVerticalBar().getSize().x;

      if (itemWidth > treeWidth)
        tree.setToolTipText(item.getText());
      else
        tree.setToolTipText("");
    }
    else
      tree.setToolTipText("");
  }

  public void copy()
  {
    TreeItem item = tree.getSelection()[0];

    String text = item.getText();
    if (item.getData() == TABLE_ITEM)
    {
      TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
      text = tableInfo.getProperName();
    }
    else if (item.getData() == PROCEDURE_COLUMN_ITEM)
    {
      ProcedureColumnInfo procColumnInfo = (ProcedureColumnInfo)item.getData(PROCEDURE_COLUMN_DATA_KEY);
      text = procColumnInfo.getColumnName();
    }

    if (text == null || text.length() == 0)
      text = " ";

    TextTransfer textTransfer = TextTransfer.getInstance();
    StellaClipBoard.getClipBoard().setContents(new Object[]{text}, new Transfer[]{textTransfer});
  }

  private void refreshTree()
  {
    tree.removeAll();
    TreeItem treeItem = new TreeItem(tree, SWT.NULL);
    treeItem.setText("Loading...");
    compositeInner.setEnabled(false); // prevent input to the tree
    BusyManager.setBusy(this);

    SessionData.getSessionData(sessionName).getDBObjectRetriever().getCatalogs();
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == copyMI)
    {
      copy();
    }
    else if (e.widget == tree)
    {
      if (tree.getSelectionCount() == 1
          && (tree.getSelection()[0].getData() == COLUMN_ITEM || tree.getSelection()[0].getData() == TABLE_ITEM))
      {
        infoButton.setEnabled(true);
        contentsButton.setEnabled(true);
        insertButton.setEnabled(true);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
      }
      else
      {
        infoButton.setEnabled(false);
        contentsButton.setEnabled(false);
        insertButton.setEnabled(false);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
      }
    }
    else if (e.widget == refreshButton)
    {
      refreshTree();
    }
    else if (e.widget == infoButton)
    {
      TableInfo tableInfo = (TableInfo)tree.getSelection()[0].getData(TABLE_DATA_KEY);
      displayTableInfo(tableInfo);
    }
    else if (e.widget == contentsButton)
    {
      TableInfo tableInfo = (TableInfo)tree.getSelection()[0].getData(TABLE_DATA_KEY);
      displayTableContents(tableInfo);
    }
    else if (e.widget == insertButton)
    {
      TableInfo tableInfo = (TableInfo)tree.getSelection()[0].getData(TABLE_DATA_KEY);
      displayTableInsert(tableInfo);
    }
    else if (e.widget == updateButton)
    {
      TableInfo tableInfo = (TableInfo)tree.getSelection()[0].getData(TABLE_DATA_KEY);
      displayTableUpdate(tableInfo);
    }
    else if (e.widget == deleteButton)
    {
      TableInfo tableInfo = (TableInfo)tree.getSelection()[0].getData(TABLE_DATA_KEY);
      displayTableDelete(tableInfo);
    }
    else if (e.widget == maximizeButton)
    {
      toggleMaximize();
    }
    else if (e.widget instanceof MenuItem)
    {
      MenuItem item = (MenuItem)e.widget;
      if (item.getData() == TABLE_INFO)
      {
        TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
        displayTableInfo(tableInfo);
      }
      else if (item.getData() == TABLE_INSERT)
      {
        TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
        displayTableInsert(tableInfo);
      }
      else if (item.getData() == TABLE_UPDATE)
      {
        TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
        displayTableUpdate(tableInfo);
      }
      else if (item.getData() == TABLE_DELETE)
      {
        TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
        displayTableDelete(tableInfo);
      }
      else if (item.getData() == TABLE_CONTENT)
      {
        TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
        displayTableContents(tableInfo);
      }
      else if (item.getData() == ROW_COUNT)
      {
        TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
        displayRowCount(tableInfo);
      }
      else if (item.getData() == REFRESH_TREE)
      {
        refreshTree();
      }
      else if (item.getData() == REFRESH_ITEM)
      {
        TreeItem treeItem = (TreeItem)item.getData(REFRESH_ITEM);
        if (treeItem.getData() == CATALOG_ITEM)
        {
          String catalogName = (String)treeItem.getData(CATALOG_NAME_KEY);
          treeItem.removeAll();

          Iterator itTypes = tableTypes.iterator();
          while (itTypes.hasNext())
          {
            String typeName = (String)itTypes.next();

            TreeItem typeItem = new TreeItem(treeItem, SWT.NULL);
            typeItem.setData(TABLETYPE_ITEM);
            typeItem.setData(TABLETYPE_KEY, typeName);
            typeItem.setData(CATALOG_NAME_KEY, catalogName);
            typeItem.setText(typeName);

            TreeItem placeholderItem = new TreeItem(typeItem, SWT.NULL);
            placeholderItem.setData(TALBEPLACEHOLDER_ITEM);
            placeholderItem.setText("Loading...");
          }
        }
        else if (treeItem.getData() == TABLETYPE_ITEM)
        {
          String tableType = (String)treeItem.getData(TABLETYPE_KEY);
          String catalogName = (String)treeItem.getData(CATALOG_NAME_KEY);
          treeItem.removeAll();
          TreeItem placeholderItem = new TreeItem(treeItem, SWT.NULL);
          placeholderItem.setData(TALBEPLACEHOLDER_ITEM);
          placeholderItem.setText("Loading...");

          compositeInner.setEnabled(false); // prevent input to the tree
          BusyManager.setBusy(this);
          SessionData.getSessionData(sessionName).getDBObjectRetriever().getTables(catalogName, tableType);
        }
        else if (treeItem.getData() == TABLE_ITEM)
        {
          TableInfo tableInfo = (TableInfo)treeItem.getData(TABLE_DATA_KEY);
          treeItem.removeAll();
          TreeItem placeholderItem = new TreeItem(treeItem, SWT.NULL);
          placeholderItem.setData(COLUMNPLACEHOLDER_ITEM);
          placeholderItem.setText("Loading...");

          tableInfo.clearColumns();
          compositeInner.setEnabled(false); // prevent input to the tree
          BusyManager.setBusy(this);
          SessionData.getSessionData(sessionName).getDBObjectRetriever().getColumns(tableInfo);
        }

      }

    }
  }

  private void displayTableContents(TableInfo tableInfo)
  {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("select * from ");
    sbuf.append(tableInfo.getProperName());
    int rows = 0;
    if (SessionData.getSessionData(sessionName).getLimitResults())
      rows = SessionData.getSessionData(sessionName).getMaxRows();
    SessionData.getSessionData(sessionName).runQuery(sbuf.toString(), rows, false);
  }

  private void displayRowCount(TableInfo tableInfo)
  {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("select count(*) from ");
    sbuf.append(tableInfo.getProperName());
    int rows = 0;
    if (SessionData.getSessionData(sessionName).getLimitResults())
      rows = SessionData.getSessionData(sessionName).getMaxRows();
    SessionData.getSessionData(sessionName).runQuery(sbuf.toString(), rows, false);
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    if (tree.getSelectionCount() > 0)
    {
      TreeItem item = tree.getSelection()[0];
      if (item != null
          && (item.getData() == COLUMN_ITEM || item.getData() == TABLE_ITEM || item.getData() == CATALOG_ITEM
              || item.getData() == PROCEDURE_ITEM || item.getData() == PROCEDURE_COLUMN_ITEM))
      {
        String text = item.getText();

        if (item.getData() == TABLE_ITEM)
        {
          TableInfo tableInfo = (TableInfo)item.getData(TABLE_DATA_KEY);
          text = tableInfo.getProperName();
        }
        else if (item.getData() == PROCEDURE_COLUMN_ITEM)
        {
          ProcedureColumnInfo procColumnInfo = (ProcedureColumnInfo)item.getData(PROCEDURE_COLUMN_DATA_KEY);
          text = procColumnInfo.getColumnName();
        }

        if (text == null || text.length() == 0)
          text = " ";

        SessionData.getSessionData(sessionName).insertQueryText(text);
      }
    }
  }

  @Override
  public void tableDetailDataAvailable(final TableInfo tableInfo)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (action == TABLE_INFO)
          displayTableInfo(tableInfo);
        else if (action == TABLE_INSERT)
          displayTableInsert(tableInfo);
        else if (action == TABLE_UPDATE)
          displayTableUpdate(tableInfo);
        else if (action == TABLE_DELETE)
          displayTableDelete(tableInfo);

        compositeInner.setEnabled(true);
        BusyManager.setNotBusy(DBObjectTreeComposite.this);
      }
    });
  }

  private void displayTableInfo(TableInfo tableInfo)
  {
    if (tableInfo != null && tableInfo.getColumns() != null)
    {
      if (tableInfo.getFullyLoaded())
      {
        TableInfoDialog tableInfoDialog = new TableInfoDialog(this.getShell(), tableInfo);
        tableInfoDialog.open();
      }
      else
      {
        action = TABLE_INFO;
        compositeInner.setEnabled(false); // prevent input to the tree
        BusyManager.setBusy(this);
        SessionData.getSessionData(sessionName).getDBObjectRetriever().getTableInfo(tableInfo);
      }
    }
  }

  private void displayTableInsert(TableInfo tableInfo)
  {
    if (tableInfo != null && tableInfo.getColumns() != null)
    {
      if (tableInfo.getFullyLoaded())
      {
        InsertDialog insertDialog = new InsertDialog(this.getShell(), tableInfo, SessionData.getSessionData(sessionName));
        insertDialog.open();
      }
      else
      {
        action = TABLE_INSERT;
        compositeInner.setEnabled(false); // prevent input to the tree
        BusyManager.setBusy(this);
        SessionData.getSessionData(sessionName).getDBObjectRetriever().getTableInfo(tableInfo);
      }
    }
  }

  private void displayTableUpdate(TableInfo tableInfo)
  {
    if (tableInfo != null && tableInfo.getColumns() != null)
    {
      if (tableInfo.getFullyLoaded())
      {
        UpdateDialog updateDialog = new UpdateDialog(this.getShell(), tableInfo, SessionData.getSessionData(sessionName));
        updateDialog.open();
      }
      else
      {
        action = TABLE_UPDATE;
        compositeInner.setEnabled(false); // prevent input to the tree
        BusyManager.setBusy(this);
        SessionData.getSessionData(sessionName).getDBObjectRetriever().getTableInfo(tableInfo);
      }
    }
  }

  private void displayTableDelete(TableInfo tableInfo)
  {
    if (tableInfo != null && tableInfo.getColumns() != null)
    {
      if (tableInfo.getFullyLoaded())
      {
        DeleteDialog deleteDialog = new DeleteDialog(this.getShell(), tableInfo, SessionData.getSessionData(sessionName));
        deleteDialog.open();
      }
      else
      {
        action = TABLE_DELETE;
        compositeInner.setEnabled(false); // prevent input to the tree
        BusyManager.setBusy(this);
        SessionData.getSessionData(sessionName).getDBObjectRetriever().getTableInfo(tableInfo);
      }
    }
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.keyCode == 'c' && e.stateMask == SWT.CONTROL)
    {
      copy();
    }
  }
  @Override
  public void keyReleased(KeyEvent e)
  {
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
  public void mouseDoubleClick(MouseEvent e)
  {
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


}

