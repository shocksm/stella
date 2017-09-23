package org.stellasql.stella.gui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.QueryFavoriteFolder;
import org.stellasql.stella.QueryFavoriteItem2;
import org.stellasql.stella.QueryFavoriteObject;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.StyledTextContextMenu;
import org.stellasql.stella.gui.util.SyntaxHighlighter;

public class FavoritesDialog2 extends StellaDialog implements SelectionListener, MenuListener, KeyListener, TreeListener, DragSourceListener, DropTargetListener, DisposeListener
{
  private Composite composite = null;
  private StyledText statementText = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private Tree tree = null;
  private ToolItem favoritesAddButton = null;
  private ToolItem folderAddButton = null;
  private ToolItem editButton = null;
  private ToolItem copyButton = null;
  private ToolItem deleteButton = null;
  private Menu menu = null;
  private MenuItem addFavoriteMI;
  private MenuItem addFolderMI;
  private MenuItem editMI;
  private MenuItem copyMI;
  private MenuItem deleteMI;
  private MenuItem selectAllMI;
  private DragSource dragSource;
  private TreeItem dragSourceItem = null;
  private int dragDetail = 0;
  private boolean needToSave = false;

  public FavoritesDialog2(Shell parent)
  {
    super(parent, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
    getShell().setImage(StellaImages.getInstance().getFavoritesImage());
    getShell().addDisposeListener(this);

    setText("Query Favorites");
    Composite composite = createComposite(1);

    this.composite = new Composite(composite, SWT.NONE);
    this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    this.composite.setLayout(gridLayout);

    Composite inner = new Composite(this.composite, SWT.NONE);
    inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    gridLayout = new GridLayout();
    gridLayout.numColumns = 4;
    gridLayout.horizontalSpacing = 3;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    inner.setLayout(gridLayout);


    ToolBar toolBar = new ToolBar(inner, SWT.FLAT);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    toolBar.setLayoutData(gd);

    favoritesAddButton = new ToolItem(toolBar, SWT.PUSH);
    favoritesAddButton.setToolTipText("New favorite");
    favoritesAddButton.setImage(StellaImages.getInstance().getFavoritesNewImage());
    favoritesAddButton.addSelectionListener(this);

    folderAddButton = new ToolItem(toolBar, SWT.PUSH);
    folderAddButton.setToolTipText("New folder");
    folderAddButton.setImage(StellaImages.getInstance().getFolderNewImage());
    folderAddButton.addSelectionListener(this);

    new ToolItem(toolBar, SWT.SEPARATOR);
    editButton = new ToolItem(toolBar, SWT.PUSH);
    editButton.setToolTipText("Edit the selected item");
    editButton.setImage(StellaImages.getInstance().getEditImage());
    editButton.setDisabledImage(StellaImages.getInstance().getEditDisImage());
    editButton.addSelectionListener(this);
    editButton.setEnabled(false);
    copyButton = new ToolItem(toolBar, SWT.PUSH);
    copyButton.setToolTipText("Copy the selected item");
    copyButton.setImage(StellaImages.getInstance().getCopyImage());
    copyButton.setDisabledImage(StellaImages.getInstance().getCopyDisImage());
    copyButton.addSelectionListener(this);
    copyButton.setEnabled(false);
    deleteButton = new ToolItem(toolBar, SWT.PUSH);
    deleteButton.setToolTipText("Delete the selected item");
    deleteButton.setImage(StellaImages.getInstance().getDeleteImage());
    deleteButton.setDisabledImage(StellaImages.getInstance().getDeleteDisImage());
    deleteButton.addSelectionListener(this);
    deleteButton.setEnabled(false);

    // favorites tree
    tree = new Tree(this.composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    tree.addKeyListener(this);
    tree.addSelectionListener(this);
    tree.addTreeListener(this);
    tree.setLinesVisible(true);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.heightHint = 300;
    gd.widthHint = 500;
    tree.setLayoutData(gd);

    Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
    int operations = DND.DROP_MOVE | DND.DROP_COPY;

    dragSource = new DragSource(tree, operations);
    dragSource.setTransfer(types);
    dragSource.addDragListener(this);

    DropTarget target = new DropTarget(tree, operations);
    target.setTransfer(types);
    target.addDropListener(this);

    menu = new Menu(tree);
    menu.addMenuListener(this);
    tree.setMenu(menu);

    addFavoriteMI = new MenuItem(menu, SWT.PUSH);
    addFavoriteMI.addSelectionListener(this);
    addFavoriteMI.setText("&New Favorite");

    addFolderMI = new MenuItem(menu, SWT.PUSH);
    addFolderMI.addSelectionListener(this);
    addFolderMI.setText("New &Folder");

    editMI = new MenuItem(menu, SWT.PUSH);
    editMI.addSelectionListener(this);
    editMI.setText("&Edit");

    copyMI = new MenuItem(menu, SWT.PUSH);
    copyMI.addSelectionListener(this);
    copyMI.setText("&Copy");

    deleteMI = new MenuItem(menu, SWT.PUSH);
    deleteMI.addSelectionListener(this);
    deleteMI.setText("&Delete");

    new MenuItem(menu, SWT.SEPARATOR);

    selectAllMI = new MenuItem(menu, SWT.PUSH);
    selectAllMI.addSelectionListener(this);
    selectAllMI.setText("Select &All");

    // sql text
    statementText = new StyledText(this.composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    statementText.setEditable(false);
    statementText.setIndent(2);
    new StyledTextContextMenu(statementText);
    new SyntaxHighlighter(statementText, "");
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.heightHint = 100;
    gd.minimumHeight = 100;
    statementText.setLayoutData(gd);

    okBtn = createButton(0);
    okBtn.setText("&Paste into SQL Editor");
    okBtn.addSelectionListener(this);
    if (Stella.getInstance().getSelectedSessionData() == null)
      okBtn.setVisible(false);

    cancelBtn = createButton(0);
    if (Stella.getInstance().getSelectedSessionData() == null)
      cancelBtn.setText("&OK");
    else
      cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);


    setFonts(ApplicationData.getInstance().getGeneralFont());


    populateTree();
    selectFirstIfNone();
    tree.setFocus();
  }

  public void open()
  {
    openInternal(-1, -1);
  }

  private TreeItem locateTreeItem(TreeItem[] items, QueryFavoriteFolder qff)
  {
    for (int index = 0; index < items.length; index++)
    {
      if (items[index].getData() == qff)
        return items[index];

      TreeItem item = locateTreeItem(items[index].getItems(), qff);
      if (item != null)
        return item;
    }

    return null;
  }

  private TreeItem locateTreeItem(QueryFavoriteFolder qff)
  {
    return locateTreeItem(tree.getItems(), qff);
  }

  private void getExpandedChildren(TreeItem item, Set expandedSet)
  {
    if (item.getExpanded())
      expandedSet.add(item.getData());

    for (int index = 0; index < item.getItems().length; index++)
      getExpandedChildren(item.getItems()[index], expandedSet);
  }

  private void setExpandedChildren(TreeItem item, Set expandedSet)
  {
    if (expandedSet.contains(item.getData()))
      setExpanded(item, true);

    for (int index = 0; index < item.getItems().length; index++)
      setExpandedChildren(item.getItems()[index], expandedSet);
  }

  private void setExpanded(TreeItem item, boolean expanded)
  {
    item.setExpanded(expanded);
    if (item.getExpanded())
      item.setImage(StellaImages.getInstance().getFolderOpenImage());
    else
      item.setImage(StellaImages.getInstance().getFolderImage());
  }

  private TreeItem createTreeItem(TreeItem parent, int index, QueryFavoriteObject qfo, boolean expanded)
  {
    TreeItem item = null;
    if (parent == null)
      item = new TreeItem(tree, SWT.NONE, index);
    else
      item = new TreeItem(parent, SWT.NONE, index);

    item.setData(qfo);
    item.setText(qfo.getName());
    if (qfo instanceof QueryFavoriteItem2)
      item.setImage(StellaImages.getInstance().getFavoritesImage());
    else
    {
      setExpanded(item, expanded);
      QueryFavoriteFolder qff = (QueryFavoriteFolder)qfo;
      int childIndex = 0;
      for (Iterator it = qff.getChildren().iterator(); it.hasNext();)
      {
        QueryFavoriteObject child = (QueryFavoriteObject)it.next();
        createTreeItem(item, childIndex++, child, false);
      }
    }

    return item;
  }

  private void populateTree()
  {
    QueryFavoriteFolder root = ApplicationData.getInstance().getFavoritesRootFolder();
    int childIndex = 0;
    for (Iterator it = root.getChildren().iterator(); it.hasNext();)
    {
      QueryFavoriteObject child = (QueryFavoriteObject)it.next();
      createTreeItem(null, childIndex++, child, false);
    }
  }

  private void selectFirstIfNone()
  {
    if (tree.getItemCount() > 0 && tree.getSelectionCount() == 0)
    {
      tree.setSelection(tree.getItem(0));
    }
    setQueryTextAndButtons();
  }

  private void setQueryTextAndButtons()
  {
    if (tree.getSelectionCount() == 1)
    {
      TreeItem item = tree.getSelection()[0];
      if (item.getData() instanceof QueryFavoriteItem2)
      {
        QueryFavoriteItem2 qfi = (QueryFavoriteItem2)item.getData();
        statementText.setText(qfi.getQuery());
        okBtn.setEnabled(true);
      }
      else
      {
        statementText.setText("");
        okBtn.setEnabled(false);
      }

      editButton.setEnabled(true);
      copyButton.setEnabled(true);
      deleteButton.setEnabled(true);
    }
    else
    {
      statementText.setText("");
      editButton.setEnabled(false);
      copyButton.setEnabled(false);
      deleteButton.setEnabled(tree.getSelectionCount() >= 1);
      okBtn.setEnabled(false);
    }
  }

  private void okPressed()
  {
    if (Stella.getInstance().getSelectedSessionData() != null)
    {
      if (tree.getSelectionCount() == 1)
      {
        TreeItem item = tree.getSelection()[0];
        if (item.getData() instanceof QueryFavoriteItem2)
        {
          QueryFavoriteItem2 qfi = (QueryFavoriteItem2)item.getData();
          Stella.getInstance().getSelectedSessionData().addQueryText(qfi.getQuery());
        }
      }
    }
    getShell().dispose();
  }

  private TreeItem addToTree(QueryFavoriteObject qfo)
  {
    QueryFavoriteFolder folder = qfo.getParent();
    TreeItem parentItem = locateTreeItem(folder);

    List list = folder.getChildren();
    int index = list.indexOf(qfo);

    TreeItem itemNew = createTreeItem(parentItem, index, qfo, false);

    if (parentItem != null && !parentItem.getExpanded())
      setExpanded(parentItem, true);

    tree.setSelection(itemNew);
    setQueryTextAndButtons();

    return itemNew;
  }

  private void addFavorite()
  {
    FavoriteAddDialog2 fd = new FavoriteAddDialog2(Stella.getInstance().getShell(), false, false);

    QueryFavoriteFolder folder = null;
    if (tree.getSelectionCount() == 1)
    {
      TreeItem item = tree.getSelection()[0];
      TreeItem parent = item.getParentItem();
      while (!(item.getData() instanceof QueryFavoriteFolder)
             && parent != null)
      {
        item = parent;
        parent = item.getParentItem();
      }

      if (item.getData() instanceof QueryFavoriteFolder)
      {
        folder = (QueryFavoriteFolder)item.getData();
      }
    }

    QueryFavoriteItem2 qfiNew = fd.openAndWait(folder);
    if (qfiNew != null)
    {
      needToSave = true;
      addToTree(qfiNew);
    }
  }

  private void addFolder()
  {
    FolderAddDialog fd = new FolderAddDialog(Stella.getInstance().getShell(), false, null);
    QueryFavoriteFolder folder = null;
    if (tree.getSelectionCount() == 1)
    {
      TreeItem item = tree.getSelection()[0];
      if (item.getData() instanceof QueryFavoriteFolder)
        folder = (QueryFavoriteFolder)item.getData();
      else if (item.getParentItem() != null)
        folder = (QueryFavoriteFolder)item.getParentItem().getData();
    }
    QueryFavoriteFolder qffNew = fd.openAndWait(folder);
    if (qffNew != null)
    {
      needToSave = true;
      addToTree(qffNew);
    }
  }

  private void edit()
  {
    if (tree.getSelectionCount() != 1)
      return;

    TreeItem item = tree.getSelection()[0];
    if (item.getData() instanceof QueryFavoriteItem2)
    {
      QueryFavoriteItem2 qfi = (QueryFavoriteItem2)item.getData();
      QueryFavoriteFolder qffParent = qfi.getParent();

      FavoriteAddDialog2 fd = new FavoriteAddDialog2(Stella.getInstance().getShell(), true, false);
      fd.setFavorite(qfi);

      qfi = fd.openAndWait(qffParent);
      if (qfi != null)
      {
        needToSave = true;
        item.dispose();
        addToTree(qfi);
      }
    }
    else if (item.getData() instanceof QueryFavoriteFolder)
    {
      QueryFavoriteFolder qff = (QueryFavoriteFolder)item.getData();
      QueryFavoriteFolder qffParent = qff.getParent();

      FolderAddDialog fd = new FolderAddDialog(Stella.getInstance().getShell(), true, qff);
      fd.setFolder(qff);

      qff = fd.openAndWait(qffParent);
      if (qff != null)
      {
        needToSave = true;
        Set expandedSet = new HashSet();
        getExpandedChildren(item, expandedSet);
        item.dispose();
        item = addToTree(qff);
        setExpandedChildren(item, expandedSet);
      }
    }
  }

  private void copy()
  {
    if (tree.getSelectionCount() != 1)
      return;

    TreeItem item = tree.getSelection()[0];
    if (item.getData() instanceof QueryFavoriteItem2)
    {
      QueryFavoriteItem2 qfi = (QueryFavoriteItem2)item.getData();
      QueryFavoriteFolder qffParent = qfi.getParent();

      FavoriteAddDialog2 fd = new FavoriteAddDialog2(Stella.getInstance().getShell(), false, false);
      fd.setDescription(qfi.getName());
      fd.setQuery(qfi.getQuery());

      qfi = fd.openAndWait(qffParent);
      if (qfi != null)
      {
        needToSave = true;
        addToTree(qfi);
      }
    }
    else if (item.getData() instanceof QueryFavoriteFolder)
    {
      QueryFavoriteFolder qffOrig = (QueryFavoriteFolder)item.getData();
      QueryFavoriteFolder qffParent = qffOrig.getParent();

      FolderAddDialog fd = new FolderAddDialog(Stella.getInstance().getShell(), false, null);
      fd.setName(qffOrig.getName());

      QueryFavoriteFolder qffNew = fd.openAndWait(qffParent);
      if (qffNew != null)
      {
        QueryFavoriteFolder parent = qffNew.getParent();

        // prevent infinite loop if adding a copy back to the original
        parent.removeChild(qffNew);

        //copy children
        qffNew.copyChildren(qffOrig);
        parent.addChild(qffNew);

        needToSave = true;
        item = addToTree(qffNew);
      }
    }
  }

  private void delete()
  {
    if (tree.getSelectionCount() < 1)
      return;

    MessageDialog msgDialog = new MessageDialog(getShell(), SWT.OK | SWT.CANCEL);
    msgDialog.setText("Delete Favorite");
    if (tree.getSelectionCount() == 1)
    {
      TreeItem item = tree.getSelection()[0];
      msgDialog.setMessage("Delete '" + ((QueryFavoriteObject)item.getData()).getName() + "'?");
    }
    else
      msgDialog.setMessage("Delete all of the selected items?");

    if (msgDialog.open() == SWT.OK)
    {
      TreeItem[] items = tree.getSelection();
      for (int index = 0; index < items.length; index++)
      {
        if (!items[index].isDisposed())
        {
          if (items[index].getData() instanceof QueryFavoriteItem2)
          {
            QueryFavoriteItem2 qfi = (QueryFavoriteItem2)items[index].getData();
            if (qfi.getParent() != null)
              qfi.getParent().removeChild(qfi);
          }
          else if (items[index].getData() instanceof QueryFavoriteFolder)
          {
            QueryFavoriteFolder qff = (QueryFavoriteFolder)items[index].getData();
            if (qff.getParent() != null)
              qff.getParent().removeChild(qff);
          }

          items[index].dispose();
        }
      }

      needToSave = true;
      selectFirstIfNone();
    }
  }

  private void selectAll()
  {
    tree.selectAll();
    setQueryTextAndButtons();
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
      getShell().dispose();
    else if (e.widget == okBtn)
    {
      okPressed();
    }
    else if (e.widget == tree)
    {
      setQueryTextAndButtons();
    }
    else if (e.widget == favoritesAddButton
           || e.widget == addFavoriteMI)
    {
      addFavorite();
    }
    else if (e.widget == folderAddButton
        || e.widget == addFolderMI)
    {
      addFolder();
    }
    else if (e.widget == editButton
            || e.widget == editMI)
    {
      edit();
    }
    else if (e.widget == copyButton
        || e.widget == copyMI)
    {
      copy();
    }
    else if (e.widget == deleteButton
        || e.widget == deleteMI)
    {
      delete();
    }
    else if (e.widget == selectAllMI)
    {
      selectAll();
    }

  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    if (e.widget == tree)
    {
      if (tree.getSelectionCount() == 1)
      {
        TreeItem item = tree.getSelection()[0];
        if (item.getData() instanceof QueryFavoriteItem2)
        {
          if (Stella.getInstance().getSelectedSessionData() != null)
            okPressed();
        }
        else if (item.getItemCount() > 0)
        {
          setExpanded(item, !item.getExpanded());
        }
      }
    }
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }

  @Override
  public void menuShown(MenuEvent e)
  {
    editMI.setEnabled(tree.getSelectionCount() == 1);
    copyMI.setEnabled(tree.getSelectionCount() == 1);
    deleteMI.setEnabled(tree.getSelectionCount() > 0);
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL)
    {
      selectAll();
    }
  }
  @Override
  public void keyReleased(KeyEvent e)
  {

  }

  @Override
  public void treeCollapsed(TreeEvent e)
  {
    TreeItem item = (TreeItem)e.item;
    item.setImage(StellaImages.getInstance().getFolderImage());
  }

  @Override
  public void treeExpanded(TreeEvent e)
  {
    TreeItem item = (TreeItem)e.item;
    item.setImage(StellaImages.getInstance().getFolderOpenImage());
  }

  @Override
  public void dragStart(DragSourceEvent event)
  {
    if (tree.getSelectionCount() == 1)
    {
      event.doit = true;
      dragSourceItem = tree.getSelection()[0];
    }
    else
    {
      event.doit = false;
    }
  }

  @Override
  public void dragFinished(DragSourceEvent event)
  {
    dragSourceItem = null;
  }

  @Override
  public void dragSetData(DragSourceEvent event)
  {
    event.data = "asdf";
  }

  @Override
  public void dragEnter(DropTargetEvent event)
  {
  }
  @Override
  public void dragLeave(DropTargetEvent event)
  {
  }
  @Override
  public void dragOperationChanged(DropTargetEvent event)
  {
    dragDetail = event.detail;
  }
  @Override
  public void dragOver(DropTargetEvent event)
  {
    if (event.detail != DND.DROP_NONE)
      dragDetail = event.detail;

    event.detail = dragDetail;

    event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
    if (event.item != null)
    {
      TreeItem item = (TreeItem)event.item;

      boolean valid = true;
      if (event.detail == DND.DROP_MOVE
          && (dragSourceItem== item
            || dragSourceItem.getParentItem() == item
            || childOf(item, dragSourceItem)))
      {
        valid = false;
      }

      if (!valid)
      {
        if (event.detail != DND.DROP_NONE)
          dragDetail = event.detail;
        event.detail = DND.DROP_NONE;
        //event.feedback;
      }
      else
      {
        event.detail = dragDetail;
        Point pt = getShell().getDisplay().map(null, tree, event.x, event.y);
        Rectangle bounds = item.getBounds();
        if (pt.y < bounds.y + bounds.height / 3)
        {
          event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
        }
        else if (pt.y > bounds.y + 2 * bounds.height / 3)
        {
          event.feedback |= DND.FEEDBACK_INSERT_AFTER;
        }
        else
        {
          event.feedback |= DND.FEEDBACK_SELECT;
        }
      }
    }
  }
  @Override
  public void drop(DropTargetEvent event)
  {
    QueryFavoriteObject qfo = (QueryFavoriteObject)dragSourceItem.getData();
    QueryFavoriteObject placeHolder = null;

    if (event.detail == DND.DROP_MOVE)
    {
      int index = qfo.getParent().getChildren().indexOf(qfo);
      // needed to keep the index numbers in line with the tree items
      placeHolder = QueryFavoriteObject.copy(qfo, false);
      qfo.getParent().addChild(placeHolder, index);
      qfo.getParent().removeChild(qfo);
    }
    else
      qfo = QueryFavoriteObject.copy(qfo, true);

    if (event.item == null)
    {
      ApplicationData.getInstance().getFavoritesRootFolder().addChild(qfo);
      TreeItem item = createTreeItem(null, tree.getItemCount(), qfo, dragSourceItem.getExpanded());
      Set expandedSet = new HashSet();
      getExpandedChildren(dragSourceItem, expandedSet);
      setExpandedChildren(item, expandedSet);
      tree.setSelection(item);
      setQueryTextAndButtons();
    }
    else
    {
      TreeItem item = (TreeItem)event.item;

      int index = 0;
      if (item.getParentItem() != null)
        index = item.getParentItem().indexOf(item);
      else
        index = tree.indexOf(item);

      TreeItem newItem = null;
      Point pt = getShell().getDisplay().map(null, tree, event.x, event.y);
      Rectangle bounds = item.getBounds();
      if (pt.y < bounds.y + bounds.height / 3)
      {
        ((QueryFavoriteObject)item.getData()).getParent().addChild(qfo, index);
        newItem = createTreeItem(item.getParentItem(), index, qfo, dragSourceItem.getExpanded());
      }
      else if (pt.y > bounds.y + 2 * bounds.height / 3)
      {
        ((QueryFavoriteObject)item.getData()).getParent().addChild(qfo, index+1);
        newItem = createTreeItem(item.getParentItem(), index+1, qfo, dragSourceItem.getExpanded());
      }
      else
      {
        if (item.getData() instanceof QueryFavoriteFolder)
        {
          ((QueryFavoriteFolder)item.getData()).addChild(qfo);
          newItem = createTreeItem(item, item.getItemCount(), qfo, dragSourceItem.getExpanded());
        }
        else
        {
          ((QueryFavoriteObject)item.getData()).getParent().addChild(qfo, index+1);
          newItem = createTreeItem(item.getParentItem(), index+1, qfo, dragSourceItem.getExpanded());
        }
      }

      Set expandedSet = new HashSet();
      getExpandedChildren(dragSourceItem, expandedSet);
      setExpandedChildren(newItem, expandedSet);

      tree.setSelection(newItem);
      setQueryTextAndButtons();
    }

    if (event.detail == DND.DROP_MOVE)
    {
      placeHolder.getParent().removeChild(placeHolder);
      dragSourceItem.dispose();
    }
    needToSave = true;
  }
  @Override
  public void dropAccept(DropTargetEvent event)
  {
  }

  private boolean childOf(TreeItem checkChild, TreeItem source)
  {
    for (int index = 0; index < source.getItems().length; index++)
    {
      if (source.getItems()[index] == checkChild)
        return true;

      if (childOf(checkChild, source.getItems()[index]))
        return true;
    }

    return false;
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    if (needToSave)
      Stella.getInstance().saveFavorites();
  }


  public static void main(String[] args)
  {
    try
    {
      Display display = new Display();
      ApplicationData.getInstance().load();
      StellaClipBoard.init(display);
      Stella.init(display);
      Stella.getInstance().open();
      FavoritesDialog2 fd = new FavoritesDialog2(Stella.getInstance().getShell());
      fd.open();
      Shell shell = Stella.getInstance().getShell();
      while (!shell.isDisposed())
      {
        if (!display.readAndDispatch())
          display.sleep();
      }
      display.dispose();
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

}
