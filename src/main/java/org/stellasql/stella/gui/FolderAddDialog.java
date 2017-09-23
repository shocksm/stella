package org.stellasql.stella.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.QueryFavoriteFolder;
import org.stellasql.stella.QueryFavoriteObject;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.StellaImages;

public class FolderAddDialog extends StellaDialog implements SelectionListener, TreeListener
{
  protected Composite composite = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private Text nameText = null;
  private Text locationText = null;
  private Button showTreeBtn = null;
  private Button hideTreeBtn = null;
  private Tree tree = null;
  private Map folderMap = new HashMap();
  private QueryFavoriteFolder qffNew = null;
  private QueryFavoriteFolder qffHold = null;
  private boolean edit = false;

  public FolderAddDialog(Shell parent, boolean edit, QueryFavoriteFolder doNotShow)
  {
    super(parent, SWT.TITLE | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    getShell().setImage(StellaImages.getInstance().getFavoritesNewImage());

    this.edit = edit;
    if (this.edit)
      setText("Edit Favorites Folder");
    else
      setText("Add Favorites Folder");
    Composite composite = createComposite(1);

    this.composite = new Composite(composite, SWT.NONE);
    this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 4;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    gridLayout.horizontalSpacing = 0;
    this.composite.setLayout(gridLayout);

    Label label = new Label(this.composite, SWT.RIGHT);
    label.setText("&Name:");
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    gd.horizontalSpan = 1;
    label.setLayoutData(gd);

    nameText = new Text(this.composite, SWT.BORDER);
    gd = new GridData();
    gd.horizontalIndent = 2;
    gd.horizontalSpan = 3;
    gd.horizontalAlignment = SWT.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.widthHint = 300;
    nameText.setLayoutData(gd);


    label = new Label(this.composite, SWT.RIGHT);
    label.setText("&Location:");
    gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
    label.setLayoutData(gd);

    locationText = new Text(this.composite, SWT.BORDER);
    locationText.setEditable(false);
    gd = new GridData();
    gd.horizontalIndent = 2;
    gd.horizontalAlignment = SWT.FILL;
    gd.grabExcessHorizontalSpace = true;
    locationText.setLayoutData(gd);
    locationText.setBackground(this.composite.getBackground());

    showTreeBtn = new Button(this.composite, SWT.ARROW | SWT.DOWN);
    showTreeBtn.addSelectionListener(this);
    gd = new GridData();
    gd.horizontalIndent = 2;
    showTreeBtn.setLayoutData(gd);

    hideTreeBtn = new Button(this.composite, SWT.ARROW | SWT.UP);
    hideTreeBtn.addSelectionListener(this);
    hideTreeBtn.setVisible(false);
    gd = new GridData();
    gd.exclude = true;
    gd.horizontalIndent = 2;
    hideTreeBtn.setLayoutData(gd);

    tree = new Tree(this.composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    tree.addSelectionListener(this);
    tree.addTreeListener(this);
    tree.setLinesVisible(true);
    tree.setVisible(false);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.exclude = true;
    gd.heightHint = 100;
    gd.horizontalSpan = 4;
    tree.setLayoutData(gd);

    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());

    populateTree(doNotShow);
  }

  public void setFolder(QueryFavoriteFolder qff)
  {
    qffHold = qff;
    nameText.setText(qffHold.getName());
  }

  public void setName(String name)
  {
    nameText.setText(name);
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

  private void addTreeFolder(TreeItem parent, QueryFavoriteFolder folder, QueryFavoriteFolder doNotShow)
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

    List list = new ArrayList(folder.getChildren());
    for (Iterator it = list.iterator(); it.hasNext();)
    {
      QueryFavoriteObject qfo = (QueryFavoriteObject)it.next();
      if (qfo instanceof QueryFavoriteFolder)
      {
        if (qfo != doNotShow)
          addTreeFolder(item, (QueryFavoriteFolder)qfo, doNotShow);
      }
    }
  }

  private void populateTree(QueryFavoriteFolder doNotShow)
  {
    QueryFavoriteFolder root = ApplicationData.getInstance().getFavoritesRootFolder();
    addTreeFolder(null, root, doNotShow);

    if (tree.getItemCount() > 0 && tree.getItem(0).getItemCount() > 0)
    {
      setExpanded(tree.getItem(0), true);
      tree.setSelection(tree.getItem(0));
      locationText.setText(tree.getItem(0).getText());
    }
  }

  public QueryFavoriteFolder openAndWait(QueryFavoriteFolder folder)
  {
    openInternal(-1, -1);
    if (folder == null)
      folder = ApplicationData.getInstance().getFavoritesRootFolder();
    selectFolder(folder);
    Display display = getShell().getDisplay();
    while (!getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return qffNew;
  }

  private void setExpanded(TreeItem item, boolean expanded)
  {
    item.setExpanded(expanded);
    if (item.getExpanded())
      item.setImage(StellaImages.getInstance().getFolderOpenImage());
    else
      item.setImage(StellaImages.getInstance().getFolderImage());
  }

  private void okPressed()
  {
    boolean valid = true;

    if (nameText.getText().trim().length() == 0)
    {
      valid = false;
      MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
      messageDlg.setText("Name required");
      messageDlg.setMessage("Name can not be blank");
      messageDlg.open();
      nameText.setFocus();
    }

    if (valid)
    {
      QueryFavoriteFolder qff = (QueryFavoriteFolder)tree.getSelection()[0].getData();

      if (!edit)
      {
        qffNew = new QueryFavoriteFolder(QueryFavoriteFolder.getNextId(), nameText.getText());
        qff.addChild(qffNew);
      }
      else
      {
        qffNew = qffHold;
        qffNew.setName(nameText.getText());
        if (qffNew.getParent() != qff)
          qff.addChild(qffNew);
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

      gd = (GridData)tree.getLayoutData();
      gd.exclude = false;
      tree.setVisible(true);
    }
    else
    {
      GridData gd = (GridData)showTreeBtn.getLayoutData();
      gd.exclude = false;
      showTreeBtn.setVisible(true);

      gd = (GridData)hideTreeBtn.getLayoutData();
      gd.exclude = true;
      hideTreeBtn.setVisible(false);

      gd = (GridData)tree.getLayoutData();
      gd.exclude = true;
      tree.setVisible(false);
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

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
      getShell().dispose();
    else if (e.widget == okBtn)
    {
      okPressed();
    }
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

}

