package org.stellasql.stella.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.stellasql.stella.QueryHistoryComparator;
import org.stellasql.stella.QueryHistoryItem;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.StyledTextContextMenu;
import org.stellasql.stella.gui.util.SyntaxHighlighter;

public class HistoryDialog extends StellaDialog implements SelectionListener, ControlListener, MenuListener, KeyListener
{
  private Composite composite = null;
  private Button allButton = null;
  private StyledText statementText = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private SyntaxHighlighter syntaxHighlighter;
  private Table table = null;
  private Combo aliasCombo = null;
  private ToolItem favoritesAddButton = null;
  private ToolItem deleteButton = null;
  private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm a");
  private Menu menu = null;
  private MenuItem addMI;
  private MenuItem deleteMI;
  private MenuItem selectAllMI;

  public HistoryDialog(Shell parent)
  {
    super(parent, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
    getShell().setImage(StellaImages.getInstance().getHistoryImage());

    setText("Query History");
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
    gridLayout.horizontalSpacing = 2;
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
    allButton.setText("&Show all History");
    allButton.addSelectionListener(this);

    ToolBar toolBar = new ToolBar(inner, SWT.FLAT);
    GridData gd = new GridData(SWT.RIGHT, SWT.FILL, true, true);
    toolBar.setLayoutData(gd);
    favoritesAddButton = new ToolItem(toolBar, SWT.PUSH);
    favoritesAddButton.setImage(StellaImages.getInstance().getFavoritesNewImage());
    favoritesAddButton.addSelectionListener(this);
    favoritesAddButton.setToolTipText("Add query to favorites");
    favoritesAddButton.setEnabled(false);
    deleteButton = new ToolItem(toolBar, SWT.PUSH);
    deleteButton.setToolTipText("Delete the selected history");
    deleteButton.setImage(StellaImages.getInstance().getDeleteImage());
    deleteButton.setDisabledImage(StellaImages.getInstance().getDeleteDisImage());
    deleteButton.addSelectionListener(this);
    deleteButton.setEnabled(false);

    // history table
    table = new Table(this.composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    table.setLinesVisible(true);
    table.addSelectionListener(this);
    table.addKeyListener(this);
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
    addMI.setText("&Add favorite");

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

    //add dispose if using this statementBg = new Color(getShell().getDisplay(), 220, 220, 220);
    //statementText.setBackground(statementBg);

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

    if (aliasCombo.getSelectionIndex() >=0
        || allButton.getSelection())
    {
      List historyList = new ArrayList();

      if (!allButton.getSelection())
      {
        String alias = aliasCombo.getText();
        List aliasHistoryList = ApplicationData.getInstance().getQueryHistory(alias);
        historyList.addAll(aliasHistoryList);
      }
      else
      {
        int count = ApplicationData.getInstance().getAliasCount();
        for (int index = 0; index < count; index++)
        {
          AliasVO aliasVO = ApplicationData.getInstance().getAlias(index);
          List aliasHistoryList = ApplicationData.getInstance().getQueryHistory(aliasVO.getName());
          historyList.addAll(aliasHistoryList);
        }
      }

      int columns = allButton.getSelection() ? 3 : 2;
      while (table.getColumnCount() < columns)
        new TableColumn(table, SWT.NONE);
      while (table.getColumnCount() > columns)
        table.getColumn(table.getColumnCount()-1).dispose();

      Collections.sort(historyList, new QueryHistoryComparator(QueryHistoryComparator.ASCENDING));
      for (int index = historyList.size() - 1; index >= 0; index--)
      {
        QueryHistoryItem qhi = (QueryHistoryItem)historyList.get(index);
        TableItem item = new TableItem(table, SWT.NONE);
        item.setData(qhi);
        int column = 0;
        if (qhi.getTime() > 0)
          item.setText(column++, sdf.format(new Date(qhi.getTime())));
        else
          item.setText(column++, "");

        if (allButton.getSelection())
          item.setText(column++, qhi.getAliasName());

        String text = qhi.getQuery().replaceAll("\n", " ").replaceAll("\t", " ");
        if (text.length() > 160)
          text = text.substring(0, 160) + "...";
        item.setText(column++, text);
      }

      if (table.getItemCount() > 0)
        table.select(0);

      for (int i = 0; i < table.getColumnCount(); i++)
      {
        table.getColumn(i).pack();
      }
    }

    sizeTableColumns();
    table.setRedraw(true); // prevent flicker
    setQueryText();
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

  private void setQueryText()
  {
    if (table.getSelectionCount() == 1)
    {
      QueryHistoryItem qhi = (QueryHistoryItem)table.getItem(table.getSelectionIndex()).getData();
      statementText.setText(qhi.getQuery());
      favoritesAddButton.setEnabled(true);
      deleteButton.setEnabled(true);

      String querySeperator = "";
      if (ApplicationData.getInstance().getAlias(qhi.getAliasName()) != null)
        querySeperator = ApplicationData.getInstance().getAlias(qhi.getAliasName()).getQuerySeperator();
      syntaxHighlighter.querySeparatorChanged(querySeperator);
      okBtn.setEnabled(true);
    }
    else
    {
      statementText.setText("");
      favoritesAddButton.setEnabled(false);
      deleteButton.setEnabled(table.getSelectionCount() > 0);
      okBtn.setEnabled(false);
    }
  }

  private void okPressed()
  {
    if (Stella.getInstance().getSelectedSessionData() != null)
    {
      if (table.getSelectionCount() == 1)
      {
        QueryHistoryItem qhi = (QueryHistoryItem)table.getItem(table.getSelectionIndex()).getData();
        Stella.getInstance().getSelectedSessionData().addQueryText(qhi.getQuery());
      }
    }
    getShell().dispose();
  }

  private void delete()
  {
    MessageDialog msgDialog = new MessageDialog(getShell(), SWT.OK | SWT.CANCEL);
    msgDialog.setText("Delete History");
    msgDialog.setMessage("Delete the selected history?");
    if (msgDialog.open() == SWT.OK)
    {
      TableItem[] items = table.getSelection();
      for (int index = 0; index < items.length; index++)
      {
        QueryHistoryItem qhi = (QueryHistoryItem)items[index].getData();
        ApplicationData.getInstance().removeQueryHistory(qhi);
      }
      Stella.getInstance().saveHistory();
      populateTableValues();
    }
  }

  private void selectAll()
  {
    table.selectAll();
    setQueryText();
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
      setQueryText();
    }
    else if (e.widget == aliasCombo)
    {
      populateTableValues();
    }
    else if (e.widget == allButton)
    {
      aliasCombo.setEnabled(!allButton.getSelection());
      populateTableValues();
    }
    else if (e.widget == favoritesAddButton
            || e.widget == addMI)
    {
      QueryHistoryItem qhi = (QueryHistoryItem)table.getItem(table.getSelectionIndex()).getData();
      FavoriteAddDialog2 fd = new FavoriteAddDialog2(Stella.getInstance().getShell(), false, true);
      fd.setQuery(qhi.getQuery());
      fd.open();
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

  @Override
  public void controlMoved(ControlEvent e)
  {
  }
  @Override
  public void controlResized(ControlEvent e)
  {
    sizeTableColumns();
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {
    addMI.setEnabled(table.getSelectionCount() == 1);
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


  public static void main(String[] args)
  {
    try
    {
      Display display = new Display();
      ApplicationData.getInstance().load();
      StellaClipBoard.init(display);
      Stella.init(display);
      Stella.getInstance().open();
      HistoryDialog hd = new HistoryDialog(Stella.getInstance().getShell());
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

}
