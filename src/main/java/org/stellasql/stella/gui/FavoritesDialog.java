package org.stellasql.stella.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.QueryFavoriteItem;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.StyledTextContextMenu;
import org.stellasql.stella.gui.util.SyntaxHighlighter;

public class FavoritesDialog extends StellaDialog implements SelectionListener, ControlListener, MenuListener, KeyListener
{
  private Composite composite = null;
  private StyledText statementText = null;
  private Button allButton = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private SyntaxHighlighter syntaxHighlighter;
  private Table table = null;
  private Combo aliasCombo = null;
  private ToolItem favoritesAddButton = null;
  private ToolItem editButton = null;
  private ToolItem copyButton = null;
  private ToolItem deleteButton = null;
  private Menu menu = null;
  private MenuItem addMI;
  private MenuItem editMI;
  private MenuItem copyMI;
  private MenuItem deleteMI;
  private MenuItem selectAllMI;

  public FavoritesDialog(Shell parent)
  {
    super(parent, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
    getShell().setImage(StellaImages.getInstance().getFavoritesImage());

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

    // alias combo
    Label label = new Label(inner, SWT.RIGHT);
    label.setText("C&onnection:");
    aliasCombo = new Combo(inner, SWT.SINGLE | SWT.READ_ONLY);
    aliasCombo.setVisibleItemCount(25);
    aliasCombo.addSelectionListener(this);

    allButton = new Button(inner, SWT.CHECK);
    allButton.setText("&Show all Favorites");
    allButton.addSelectionListener(this);

    ToolBar toolBar = new ToolBar(inner, SWT.FLAT);
    GridData gd = new GridData(SWT.RIGHT, SWT.FILL, true, true);
    toolBar.setLayoutData(gd);

    favoritesAddButton = new ToolItem(toolBar, SWT.PUSH);
    favoritesAddButton.setToolTipText("New favorite");
    favoritesAddButton.setImage(StellaImages.getInstance().getFavoritesNewImage());
    favoritesAddButton.addSelectionListener(this);
    new ToolItem(toolBar, SWT.SEPARATOR);
    editButton = new ToolItem(toolBar, SWT.PUSH);
    editButton.setToolTipText("Edit the selected favorite");
    editButton.setImage(StellaImages.getInstance().getEditImage());
    editButton.setDisabledImage(StellaImages.getInstance().getEditDisImage());
    editButton.addSelectionListener(this);
    editButton.setEnabled(false);
    copyButton = new ToolItem(toolBar, SWT.PUSH);
    copyButton.setToolTipText("Copy the selected favorite");
    copyButton.setImage(StellaImages.getInstance().getCopyImage());
    copyButton.setDisabledImage(StellaImages.getInstance().getCopyDisImage());
    copyButton.addSelectionListener(this);
    copyButton.setEnabled(false);
    deleteButton = new ToolItem(toolBar, SWT.PUSH);
    deleteButton.setToolTipText("Delete the selected favorite");
    deleteButton.setImage(StellaImages.getInstance().getDeleteImage());
    deleteButton.setDisabledImage(StellaImages.getInstance().getDeleteDisImage());
    deleteButton.addSelectionListener(this);
    deleteButton.setEnabled(false);

    // history table
    table = new Table(this.composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    table.setLinesVisible(true);
    table.addKeyListener(this);
    table.addSelectionListener(this);
    table.setToolTipText(""); // disable tooltip (null doesn't work here)
    new TableColumn(table, SWT.NONE);
    new TableColumn(table, SWT.NONE);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.heightHint = 300;
    gd.widthHint = 500;
    table.setLayoutData(gd);
    table.addControlListener(this);
    menu = new Menu(table);
    menu.addMenuListener(this);
    table.setMenu(menu);

    addMI = new MenuItem(menu, SWT.PUSH);
    addMI.addSelectionListener(this);
    addMI.setText("&New");

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
    syntaxHighlighter = new SyntaxHighlighter(statementText, "");
    new StyledTextContextMenu(statementText);
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


    String selectedName = "";
    if (Stella.getInstance().getSelectedSessionData() != null)
      selectedName = Stella.getInstance().getSelectedSessionData().getAlias().getName();
    int count = ApplicationData.getInstance().getAliasCount();
    for (int index = 0; index < count; index++)
    {
      AliasVO aliasVO = ApplicationData.getInstance().getAlias(index);
      aliasCombo.add(aliasVO.getName());
      if (aliasVO.getName().equals(selectedName))
      {
        aliasCombo.select(index);
      }
    }
    if (aliasCombo.getSelectionIndex() < 0
        && aliasCombo.getItemCount() > 0)
    {
      aliasCombo.select(0);
    }

    populateTableValues();
    selectFirstIfNone();
    table.setFocus();
  }

  public void open()
  {
    openInternal(-1, -1);
  }

  private void populateTableValues()
  {
    table.setRedraw(false); // prevent flicker
    table.removeAll();

    if (aliasCombo.getSelectionIndex() >= 0
        || allButton.getSelection())
    {
      List both = new ArrayList();

      if (!allButton.getSelection())
      {
        String alias = aliasCombo.getText();
        List aliasFavoriteList = ApplicationData.getInstance().getQueryFavorites(alias);
        List allFavoriteList = ApplicationData.getInstance().getQueryFavorites("");

        both.addAll(aliasFavoriteList);
        both.addAll(allFavoriteList);
      }
      else
      {
        int count = ApplicationData.getInstance().getAliasCount();
        for (int index = 0; index < count; index++)
        {
          AliasVO aliasVO = ApplicationData.getInstance().getAlias(index);
          List aliasFavoriteList = ApplicationData.getInstance().getQueryFavorites(aliasVO.getName());
          both.addAll(aliasFavoriteList);
        }

        List allFavoriteList = ApplicationData.getInstance().getQueryFavorites("");
        both.addAll(allFavoriteList);
      }

      Collections.sort(both, new FavoriteComparator());
      for (int index = 0; index < both.size(); index++)
      {
        QueryFavoriteItem qfi = (QueryFavoriteItem)both.get(index);
        TableItem item = new TableItem(table, SWT.NONE);
        item.setData(qfi);
        String aliasName = qfi.getAliasName();
        if (aliasName.length() == 0)
          aliasName = "All";
        item.setText(0, aliasName);
        item.setText(1, qfi.getDescription());
      }

      for (int i = 0; i < table.getColumnCount(); i++)
      {
        table.getColumn(i).pack();
      }
    }

    sizeTableColumns();
    table.setRedraw(true); // prevent flicker
    setQueryTextAndButtons();
  }

  private void selectFirstIfNone()
  {
    if (table.getItemCount() > 0 && table.getSelectionCount() == 0)
    {
      table.select(0);
      setQueryTextAndButtons();
    }
  }

  private void sizeTableColumns()
  {
    int width = table.getClientArea().width;

    int colWidth = 0;
    int otherColWidth = 0;
    for (int i = 0; i < table.getColumnCount(); i++)
    {
      colWidth += table.getColumn(i).getWidth();
      if (i < table.getColumnCount() - 1)
        otherColWidth  += table.getColumn(i).getWidth();
    }

    if (colWidth < width)
    {
      TableColumn column = table.getColumn(table.getColumnCount() - 1);
      column.setWidth(column.getWidth() + (width - colWidth));
    }
    else if (colWidth > width)
    {
      TableColumn column = table.getColumn(table.getColumnCount() - 1);
      column.pack();
      if (otherColWidth + column.getWidth() < width)
        column.setWidth(column.getWidth() + (width - otherColWidth));
    }
  }

  private void setQueryTextAndButtons()
  {
    if (table.getSelectionCount() == 1)
    {
      QueryFavoriteItem qfi = (QueryFavoriteItem)table.getItem(table.getSelectionIndex()).getData();
      statementText.setText(qfi.getQuery());
      editButton.setEnabled(true);
      copyButton.setEnabled(true);
      deleteButton.setEnabled(true);

      String querySeperator = "";
      if (ApplicationData.getInstance().getAlias(qfi.getAliasName()) != null)
        querySeperator = ApplicationData.getInstance().getAlias(qfi.getAliasName()).getQuerySeperator();
      syntaxHighlighter.querySeparatorChanged(querySeperator);

      okBtn.setEnabled(true);
    }
    else
    {
      statementText.setText("");
      editButton.setEnabled(false);
      copyButton.setEnabled(false);
      deleteButton.setEnabled(table.getSelectionCount() >= 1);
      okBtn.setEnabled(false);
    }
  }

  private void okPressed()
  {
    if (Stella.getInstance().getSelectedSessionData() != null)
    {
      if (table.getSelectionCount() == 1)
      {
        QueryFavoriteItem qfi = (QueryFavoriteItem)table.getItem(table.getSelectionIndex()).getData();
        Stella.getInstance().getSelectedSessionData().addQueryText(qfi.getQuery());
      }
    }
    getShell().dispose();
  }

  private void edit()
  {
    QueryFavoriteItem qfi = (QueryFavoriteItem)table.getItem(table.getSelectionIndex()).getData();

    FavoriteAddDialog fd = new FavoriteAddDialog(Stella.getInstance().getShell(), true);
    fd.setAliasName(qfi.getAliasName());
    fd.setDescription(qfi.getDescription());
    fd.setQueryText(qfi.getQuery());
    QueryFavoriteItem qfiNew = fd.openAndWait();
    if (qfiNew != null)
    {
      if (!qfi.getAliasName().equalsIgnoreCase(qfiNew.getAliasName()))
      {
        ApplicationData.getInstance().removeQueryFavorite(qfi);
        qfi.setAliasName(qfiNew.getAliasName());
        ApplicationData.getInstance().addQueryFavorite(qfi);
      }
      qfi.setDescription(qfiNew.getDescription());
      qfi.setQuery(qfiNew.getQuery());
      Stella.getInstance().saveFavorites();

      populateTableValues();
      for (int index = 0; index < table.getItemCount(); index++)
      {
        if (table.getItem(index).getData() == qfi)
        {
          table.select(index);
          setQueryTextAndButtons();
          break;
        }
      }
      selectFirstIfNone();
    }
  }

  private void copy()
  {
    QueryFavoriteItem qfi = (QueryFavoriteItem)table.getItem(table.getSelectionIndex()).getData();

    FavoriteAddDialog fd = new FavoriteAddDialog(Stella.getInstance().getShell(), false);
    fd.setAliasName(qfi.getAliasName());
    fd.setDescription("Copy of " + qfi.getDescription());
    fd.setQueryText(qfi.getQuery());
    QueryFavoriteItem qfiNew = fd.openAndWait();
    if (qfiNew != null)
    {
      populateTableValues();
      for (int index = 0; index < table.getItemCount(); index++)
      {
        if (table.getItem(index).getData() == qfiNew)
        {
          table.select(index);
          setQueryTextAndButtons();
          break;
        }
      }
      selectFirstIfNone();
    }
  }

  private void delete()
  {
    MessageDialog msgDialog = new MessageDialog(getShell(), SWT.OK | SWT.CANCEL);
    msgDialog.setText("Delete Favorite");
    if (table.getSelectionCount() == 1)
    {
      QueryFavoriteItem qfi = (QueryFavoriteItem)table.getItem(table.getSelectionIndex()).getData();
      msgDialog.setMessage("Delete the '" + qfi.getDescription() + "' favorite?");
    }
    else
      msgDialog.setMessage("Delete all of the selected favorites?");
    if (msgDialog.open() == SWT.OK)
    {
      TableItem[] items = table.getSelection();
      for (int index = 0; index < items.length; index++)
      {
        QueryFavoriteItem qfi = (QueryFavoriteItem)items[index].getData();
        ApplicationData.getInstance().removeQueryFavorite(qfi);
      }
      Stella.getInstance().saveFavorites();
      populateTableValues();
      selectFirstIfNone();
    }
  }

  private void selectAll()
  {
    table.selectAll();
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
    else if (e.widget == table)
    {
      setQueryTextAndButtons();
    }
    else if (e.widget == aliasCombo)
    {
      populateTableValues();
      selectFirstIfNone();
    }
    else if (e.widget == allButton)
    {
      aliasCombo.setEnabled(!allButton.getSelection());
      populateTableValues();
      selectFirstIfNone();
    }
    else if (e.widget == favoritesAddButton
           || e.widget == addMI)
    {
      FavoriteAddDialog fd = new FavoriteAddDialog(Stella.getInstance().getShell(), false);
      fd.setAliasName(aliasCombo.getText());
      QueryFavoriteItem qfiNew = fd.openAndWait();
      if (qfiNew != null)
      {
        populateTableValues();
        for (int index = 0; index < table.getItemCount(); index++)
        {
          if (table.getItem(index).getData() == qfiNew)
          {
            table.select(index);
            setQueryTextAndButtons();
            break;
          }
        }
        selectFirstIfNone();
      }
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
    if (e.widget == table)
    {
      if (table.getSelectionCount() == 1
          && Stella.getInstance().getSelectedSessionData() != null)
        okPressed();
    }
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
      FavoritesDialog hd = new FavoritesDialog(Stella.getInstance().getShell());
      hd.open();
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

  @Override
  public void controlMoved(ControlEvent e)
  {
  }
  @Override
  public void controlResized(ControlEvent e)
  {
    sizeTableColumns();
  }

  private class FavoriteComparator implements Comparator
  {
    @Override
    public int compare(Object arg1, Object arg2)
    {
      QueryFavoriteItem qfi1 = (QueryFavoriteItem)arg1;
      QueryFavoriteItem qfi2 = (QueryFavoriteItem)arg2;
      return qfi1.getDescription().compareToIgnoreCase(qfi2.getDescription());
    }
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }

  @Override
  public void menuShown(MenuEvent e)
  {
    editMI.setEnabled(table.getSelectionCount() == 1);
    copyMI.setEnabled(table.getSelectionCount() == 1);
    deleteMI.setEnabled(table.getSelectionCount() > 0);
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


}
