package org.stellasql.stella.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
import org.stellasql.stella.gui.util.SqlTextAdditions;
import org.stellasql.stella.gui.util.StellaImages;

public class FavoriteAddDialog2 extends StellaDialog implements SelectionListener, TreeListener, MenuListener
{
  protected Composite composite = null;
  protected Composite treeComposite = null;
  protected StyledText statementText = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private Text locationText = null;
  private Button showTreeBtn = null;
  private Button hideTreeBtn = null;
  private Tree tree = null;
  private Text descriptionText = null;
  private QueryFavoriteItem2 qfiNew = null;
  private QueryFavoriteItem2 qfiHold = null;
  private boolean edit = false;
  private boolean allowNewFolders = false;
  private Map folderMap = new HashMap();
  private ToolItem folderAddButton = null;
  private Menu menu = null;
  private MenuItem addFolderMI;

  public FavoriteAddDialog2(Shell parent, boolean edit, boolean allowNewFolders)
  {
    super(parent, SWT.TITLE | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    getShell().setImage(StellaImages.getInstance().getFavoritesNewImage());

    this.edit = edit;
    this.allowNewFolders = allowNewFolders;
    if (this.edit)
      setText("Edit Favorite Query");
    else
      setText("Add Favorite Query");
    Composite composite = createComposite(1);

    this.composite = new Composite(composite, SWT.NONE);
    this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    gridLayout.horizontalSpacing = 0;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    this.composite.setLayout(gridLayout);

    Label label = new Label(this.composite, SWT.RIGHT);
    label.setText("&Description:");
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(gd);

    descriptionText = new Text(this.composite, SWT.BORDER);
    gd = new GridData();
    gd.horizontalIndent = 2;
    gd.horizontalSpan = 2;
    gd.horizontalAlignment = SWT.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.widthHint = 100;
    descriptionText.setLayoutData(gd);

    label = new Label(this.composite, SWT.RIGHT);
    label.setText("&Location:");
    gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(gd);

    locationText = new Text(this.composite, SWT.BORDER);
    locationText.setEditable(false);
    gd = new GridData();
    gd.horizontalIndent = 2;
    gd.horizontalSpan = 1;
    gd.horizontalAlignment = SWT.FILL;
    gd.grabExcessHorizontalSpace = true;
    locationText.setLayoutData(gd);
    locationText.setBackground(this.composite.getBackground());

    showTreeBtn = new Button(this.composite, SWT.ARROW | SWT.DOWN);
    showTreeBtn.addSelectionListener(this);
    gd = new GridData();
    gd.verticalAlignment = SWT.CENTER;
    gd.horizontalIndent = 2;
    showTreeBtn.setLayoutData(gd);

    hideTreeBtn = new Button(this.composite, SWT.ARROW | SWT.UP);
    hideTreeBtn.addSelectionListener(this);
    hideTreeBtn.setVisible(false);
    gd = new GridData();
    gd.verticalAlignment = SWT.CENTER;
    gd.exclude = true;
    gd.horizontalIndent = 2;
    hideTreeBtn.setLayoutData(gd);


    treeComposite = new Composite(this.composite, SWT.NONE);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.horizontalSpan = 3;
    gd.exclude = true;
    treeComposite.setLayoutData(gd);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.horizontalSpacing = 2;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    treeComposite.setLayout(gridLayout);

    tree = new Tree(treeComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    tree.addSelectionListener(this);
    tree.addTreeListener(this);
    tree.setLinesVisible(true);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.heightHint = 100;
    if (this.allowNewFolders)
      gd.horizontalSpan = 1;
    else
      gd.horizontalSpan = 2;
    tree.setLayoutData(gd);

    if (this.allowNewFolders)
    {
      menu = new Menu(tree);
      menu.addMenuListener(this);
      tree.setMenu(menu);

      addFolderMI = new MenuItem(menu, SWT.PUSH);
      addFolderMI.addSelectionListener(this);
      addFolderMI.setText("New &Folder");
    }

    ToolBar toolBar = new ToolBar(treeComposite, SWT.FLAT);
    toolBar.setVisible(this.allowNewFolders);
    gd = new GridData(SWT.FILL, SWT.FILL, false, true);
    gd.exclude = !this.allowNewFolders;
    gd.horizontalSpan = 1;
    toolBar.setLayoutData(gd);

    folderAddButton = new ToolItem(toolBar, SWT.PUSH);
    folderAddButton.setToolTipText("New folder");
    folderAddButton.setImage(StellaImages.getInstance().getFolderNewImage());
    folderAddButton.addSelectionListener(this);


    label = new Label(this.composite, SWT.NONE);
    label.setText("&Query:");
    gd = new GridData(SWT.FILL, SWT.TOP, false, false);
    gd.horizontalSpan = 3;
    gd.verticalIndent = 10;
    label.setLayoutData(gd);

    // sql text
    statementText = new StyledText(this.composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    statementText.setIndent(2);
    new SqlTextAdditions(statementText, "");

    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.heightHint = 100;
    gd.minimumHeight = 100;
    gd.widthHint = 500;
    gd.horizontalSpan = 3;
    statementText.setLayoutData(gd);

    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());

    populateTree();
  }

  public void setFavorite(QueryFavoriteItem2 qfi)
  {
    qfiHold = qfi;
    descriptionText.setText(qfiHold.getName());
    statementText.setText(qfiHold.getQuery());
  }

  public void setDescription(String description)
  {
    descriptionText.setText(description);
  }

  public void setQuery(String query)
  {
    statementText.setText(query);
  }

  private void selectFolder(QueryFavoriteFolder folder)
  {
    TreeItem item = (TreeItem)folderMap.get(folder);
    if (item != null)
    {
      TreeItem parent = item.getParentItem();
      while (parent != null)
      {
        setExpanded(parent, true);
        parent = parent.getParentItem();
      }
      tree.setSelection(item);
      locationText.setText(item.getText());
    }
  }

  private void setExpanded(TreeItem item, boolean expanded)
  {
    item.setExpanded(expanded);
    if (item.getExpanded())
      item.setImage(StellaImages.getInstance().getFolderOpenImage());
    else
      item.setImage(StellaImages.getInstance().getFolderImage());
  }

  private void addTreeFolder(TreeItem parent, QueryFavoriteFolder folder)
  {
    TreeItem item = null;
    if (parent == null)
      item = new TreeItem(tree, SWT.NONE);
    else
      item = new TreeItem(parent, SWT.NONE);
    item.setData(folder);
    item.setText(folder.getName());
    item.setImage(StellaImages.getInstance().getFolderImage());

    folderMap.put(folder, item);

    List list = folder.getChildren();
    for (Iterator it = list.iterator(); it.hasNext();)
    {
      QueryFavoriteObject child = (QueryFavoriteObject)it.next();
      if (child instanceof QueryFavoriteFolder)
        addTreeFolder(item, (QueryFavoriteFolder)child);
    }
  }

  private void populateTree()
  {
    QueryFavoriteFolder root = ApplicationData.getInstance().getFavoritesRootFolder();
    addTreeFolder(null, root);

    if (tree.getItemCount() > 0 && tree.getItem(0).getItemCount() > 0)
    {
      setExpanded(tree.getItem(0), true);
      tree.setSelection(tree.getItem(0));
      locationText.setText(tree.getItem(0).getText());
    }
    else if (tree.getItemCount() > 0)
    {
      tree.setSelection(tree.getItem(0));
      locationText.setText(tree.getItem(0).getText());
    }
  }

  public void open()
  {
    openInternal(-1, -1);
  }

  public QueryFavoriteItem2 openAndWait(QueryFavoriteFolder folder)
  {
    open();
    if (folder == null)
      folder = ApplicationData.getInstance().getFavoritesRootFolder();
    selectFolder(folder);
    Display display = getShell().getDisplay();
    while (!getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return qfiNew;
  }

  private void okPressed()
  {
    boolean valid = true;

    if (descriptionText.getText().trim().length() == 0)
    {
      valid = false;
      MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
      messageDlg.setText("Description required");
      messageDlg.setMessage("Description can not be blank");
      messageDlg.open();
      descriptionText.setFocus();
    }

    if (valid)
    {
      QueryFavoriteFolder qff = null;
      if (tree.getSelection().length > 0)
        qff = (QueryFavoriteFolder)tree.getSelection()[0].getData();
      else
        qff = (QueryFavoriteFolder)tree.getItem(0).getData();
      if (!edit)
      {
        QueryFavoriteItem2 qfi = new QueryFavoriteItem2(descriptionText.getText(), statementText.getText());
        qfiNew = qfi;
        qff.addChild(qfiNew);
      }
      else
      {
        qfiNew = qfiHold;
        qfiNew.setName(descriptionText.getText());
        qfiNew.setQuery(statementText.getText());
        if (qfiNew.getParent() != qff)
          qff.addChild(qfiNew);
      }

      getShell().dispose();
    }
  }

  private void toggleTree()
  {
    if (showTreeBtn.isVisible())
    {
      GridData gd = (GridData)showTreeBtn.getLayoutData();
      gd.exclude = true;
      showTreeBtn.setVisible(false);

      gd = (GridData)hideTreeBtn.getLayoutData();
      gd.exclude = false;
      hideTreeBtn.setVisible(true);

      gd = (GridData)treeComposite.getLayoutData();
      gd.exclude = false;
      treeComposite.setVisible(true);
    }
    else
    {
      GridData gd = (GridData)showTreeBtn.getLayoutData();
      gd.exclude = false;
      showTreeBtn.setVisible(true);

      gd = (GridData)hideTreeBtn.getLayoutData();
      gd.exclude = true;
      hideTreeBtn.setVisible(false);

      gd = (GridData)treeComposite.getLayoutData();
      gd.exclude = true;
      treeComposite.setVisible(false);
    }

    Point pt = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);;
    getShell().setSize(getShell().getSize().x, pt.y);

    if (tree.isVisible())
    {
      tree.setTopItem(tree.getItem(0));
      if (tree.getSelectionCount() == 1)
        tree.setSelection(tree.getSelection()[0]);
    }
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

  private TreeItem addToTree(QueryFavoriteObject qfo)
  {
    QueryFavoriteFolder folder = qfo.getParent();
    TreeItem parentItem = locateTreeItem(folder);

    TreeItem itemNew = null;
    if (parentItem == null)
      itemNew = new TreeItem(tree, SWT.NONE);
    else
      itemNew = new TreeItem(parentItem, SWT.NONE);
    itemNew.setData(qfo);
    itemNew.setText(qfo.getName());
    setExpanded(itemNew, false);

    if (parentItem != null && !parentItem.getExpanded())
      setExpanded(parentItem, true);

    tree.setSelection(itemNew);

    return itemNew;
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
      getShell().dispose();
    else if (e.widget == okBtn)
      okPressed();
    else if (e.widget == showTreeBtn
            || e.widget == hideTreeBtn)
    {
      toggleTree();
    }
    else if (e.widget == tree)
    {
      if (tree.getSelectionCount() == 1)
      {
        locationText.setText(tree.getSelection()[0].getText());
      }
    }
    else if (e.widget == folderAddButton
          || e.widget == addFolderMI)
    {
      FolderAddDialog fd = new FolderAddDialog(getShell(), false, null);
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
        Stella.getInstance().saveFavorites();
        addToTree(qffNew);
      }
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
        if (item.getData() instanceof QueryFavoriteFolder)
        {
          setExpanded(item, !item.getExpanded());
        }
      }
    }
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
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {
  }

}

