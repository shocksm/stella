package org.stellasql.stella.gui.custom;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.stellasql.stella.gui.ResultData;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.gui.util.StellaColors;
import org.stellasql.stella.gui.util.WorkerRunnable;

public class CustomTable extends Composite implements KeyListener, MouseListener, MouseMoveListener, SelectionListener, MenuListener, PaintListener
{
  private Composite innerComposite = null;
  private Table table = null;
  private int selectionStart = -1;
  private int selectionStop = -1;
  private boolean mouseSelect = false;
  private Menu menu = null;
  private MenuItem copyCellMI = null;
  private MenuItem copyRowMI = null;
  private MenuItem copyHtmlMI = null;
  private MenuItem copyHeaderMI = null;
  private MenuItem copyHtmlHeaderMI = null;
  private MenuItem selectAllMI = null;
  private int selectedRow = -1;
  private int selectedColumn = 0;


  private TableData tableData = null;

  private int scrollUnit = -1;
  private ScrollTask scrollTask = null;

  private static Timer timer = new Timer(true);

  public CustomTable(Composite parent)
  {
    this(parent, true, true, true);
  }

  public CustomTable(Composite parent, boolean border, boolean virtual, boolean headerVisible)
  {
    super(parent, SWT.NONE);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    this.setLayout(gridLayout);

    // used to prevent input (setEnable(false)) while the table sorting is being done
    innerComposite = new Composite(this, SWT.NONE);
    gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    innerComposite.setLayout(gridLayout);
    innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    int style = SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL;

    if (border)
      style = style | SWT.BORDER;
    if (virtual)
      style = style | SWT.VIRTUAL;

    table = new Table(innerComposite, style);
    table.setLinesVisible(true);
    table.setHeaderVisible(headerVisible);
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    table.addKeyListener(this);
    table.addMouseListener(this);
    table.addMouseMoveListener(this);
    table.addListener(SWT.SetData, new DataListener());
    table.addPaintListener(this);
    table.addSelectionListener(this);
    //table.setVisible(false);

    menu = new Menu(table);
    table.setMenu(menu);

    copyCellMI = new MenuItem(menu, SWT.PUSH);
    copyCellMI.addSelectionListener(this);
    copyCellMI.setText("&Copy cell\tCtrl+C");

    new MenuItem(menu, SWT.SEPARATOR);

    copyRowMI = new MenuItem(menu, SWT.PUSH);
    copyRowMI.addSelectionListener(this);
    copyRowMI.setText("Copy &row(s)");

    if (headerVisible)
    {
      copyHeaderMI = new MenuItem(menu, SWT.PUSH);
      copyHeaderMI.addSelectionListener(this);
      copyHeaderMI.setText("C&opy row(s) with Header");
    }

    copyHtmlMI = new MenuItem(menu, SWT.PUSH);
    copyHtmlMI.addSelectionListener(this);
    copyHtmlMI.setText("Copy row(s) &HTML");

    if (headerVisible)
    {
      copyHtmlHeaderMI = new MenuItem(menu, SWT.PUSH);
      copyHtmlHeaderMI.addSelectionListener(this);
      copyHtmlHeaderMI.setText("Copy row(s) H&TML with Header");
    }

    new MenuItem(menu, SWT.SEPARATOR);

    selectAllMI = new MenuItem(menu, SWT.PUSH);
    selectAllMI.addSelectionListener(this);
    selectAllMI.setText ("Select &All\tCtrl+A");

    menu.addMenuListener(this);
  }

  @Override
  public Menu getMenu()
  {
    return menu;
  }


  @Override
  public void setFont(Font font)
  {
    super.setFont(font);
    table.setFont(font);
  }

  @Override
  public void setBackground(Color color)
  {
    super.setBackground(color);
    table.setBackground(color);
  }

  @Override
  public void setForeground(Color color)
  {
    super.setForeground(color);
    table.setForeground(color);
  }

  public void setLinesVisible(boolean visible)
  {
    table.setLinesVisible(visible);
  }

  public void clear(int index)
  {
    table.clear(index);
  }

  public void clear(int start, int end)
  {
    table.clear(start, end);
  }

  public void clear(int[] indices)
  {
    table.clear(indices);
  }

  public void clearAll()
  {
    table.clearAll();
  }

  @Override
  public void addListener(int type, Listener listener)
  {
    table.addListener(type, listener);
  }

  public void setSelection(TableItem[] items)
  {
    table.setSelection(items);
  }

  @Override
  public void notifyListeners(int type, Event e)
  {
    table.notifyListeners(type, e);
  }

  @Override
  public void setToolTipText(String text)
  {
    table.setToolTipText("");
  }

  public TableItem createItem()
  {
    TableItem item = new TableItem(table, SWT.NONE);
    return item;
  }

  public TableColumn createTableColumn()
  {
    TableColumn column = new TableColumn(table, SWT.NONE);
    return column;
  }

  public void setItemCount(int count)
  {
    table.setItemCount(count);
  }

  public TableColumn getColumn(int index)
  {
    return table.getColumn(index);
  }

  public TableItem getItem(int index)
  {
    return table.getItem(index);
  }

  public TableItem getItem(Point pt)
  {
    return table.getItem(pt);
  }

  @Override
  public void addMouseListener(MouseListener listener)
  {
    table.addMouseListener(listener);
  }

  public Object getSelectedCellObject()
  {
    Object obj = null;
    if (selectedRow >=0 && selectedColumn >= 0)
    {
      obj = tableData.getCell(selectedRow, selectedColumn);
    }

    return obj;
  }

  public int getSelectionCount()
  {
    return table.getSelectionCount();
  }

  public int getSelectionIndex()
  {
    return table.getSelectionIndex();
  }

  public int[] getSelectionIndices() {
  	return table.getSelectionIndices();
  }

  private void copyCell()
  {
    TableItem item = table.getItem(selectedRow);
    String value = item.getText(selectedColumn);
    TextTransfer textTransfer = TextTransfer.getInstance();
    StellaClipBoard.getClipBoard().setContents(new Object[]{value.trim()}, new Transfer[]{textTransfer});
  }

  private void copy(boolean includeHeader)
  {
    int[] selIndexArray = table.getSelectionIndices();
    StringBuffer sbuf = new StringBuffer();

    int[] colIndexArray = table.getColumnOrder();

    if (table.getHeaderVisible() && includeHeader)
    {
      for (int col = 0; col < colIndexArray.length; col++)
      {
        sbuf.append(table.getColumn(colIndexArray[col]).getText());
        if (col < colIndexArray.length - 1)
          sbuf.append("\t");
      }
      sbuf.append("\n");
    }

    for (int i = 0; i < selIndexArray.length; i++)
    {
      TableItem item = table.getItem(selIndexArray[i]);
      for (int col = 0; col < colIndexArray.length; col++)
      {
        sbuf.append(item.getText(colIndexArray[col]));
        if (col < colIndexArray.length - 1)
          sbuf.append("\t");
      }
      sbuf.append("\n");
    }

    TextTransfer textTransfer = TextTransfer.getInstance();
    StellaClipBoard.getClipBoard().setContents(new Object[]{sbuf.toString()}, new Transfer[]{textTransfer});
  }

  private void copyHtml(boolean includeHeader)
  {
    int[] selIndexArray = table.getSelectionIndices();
    StringBuffer sbuf = new StringBuffer();

    sbuf.append("<HTML><BODY><TABLE BORDER=\"1\">");
    int[] colIndexArray = table.getColumnOrder();
    if (table.getHeaderVisible() && includeHeader)
    {
      sbuf.append("<TR BGCOLOR=\"#CCCCCC\">\n");
      for (int col = 0; col < colIndexArray.length; col++)
      {
        sbuf.append("  <TD>");
        sbuf.append(table.getColumn(colIndexArray[col]).getText());
        sbuf.append("</TD>\n");
      }
      sbuf.append("</TR>\n\n");
    }

    for (int i = 0; i < selIndexArray.length; i++)
    {
      TableItem item = table.getItem(selIndexArray[i]);
      sbuf.append("<TR>\n");
      for (int col = 0; col < colIndexArray.length; col++)
      {
        sbuf.append("  <TD>");
        sbuf.append(item.getText(colIndexArray[col]));
        sbuf.append("</TD>\n");
      }
      sbuf.append("</TR>\n\n");
    }

    sbuf.append("</TABLE></BODY></HTML>\n");

    HTMLTransfer textTransfer = HTMLTransfer.getInstance();
    StellaClipBoard.getClipBoard().setContents(new Object[]{sbuf.toString()}, new Transfer[]{textTransfer});
  }


  private void selectAll()
  {
    table.selectAll();
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == copyCellMI)
    {
      copyCell();
    }
    else if (e.widget == copyRowMI)
    {
      copy(false);
    }
    else if (e.widget == copyHtmlMI)
    {
      copyHtml(false);
    }
    else if (e.widget == copyHeaderMI)
    {
      copy(true);
    }
    else if (e.widget == copyHtmlHeaderMI)
    {
      copyHtml(true);
    }
    else if (e.widget == selectAllMI)
    {
      selectAll();
    }
    else if (e.widget instanceof TableColumn)
    {
      TableColumn sortColumn = table.getSortColumn();
      TableColumn currentColumn = (TableColumn) e.widget;
      int dir = table.getSortDirection();
      if (sortColumn == currentColumn)
      {
        if (dir == SWT.DOWN)
        {
          table.setSortColumn(null);
          table.setSortDirection(SWT.NONE);
        }
        else
        {
          dir = SWT.DOWN;
          table.setSortDirection(dir);
        }
      }
      else
      {
        table.setSortColumn(currentColumn);
        dir = SWT.UP;
        table.setSortDirection(dir);
      }

      final int columnIndex = table.getSortColumn() == null ? tableData.getColumnCount() : table.indexOf(table.getSortColumn());
      final int sortDir =  table.getSortColumn() == null ? SWT.UP : dir;

      Object rowObject = null;
      if (selectedRow >= 0)
        rowObject = tableData.getRowObject(selectedRow);
      final Object selectedRowObject = rowObject;

      WorkerRunnable worker = new WorkerRunnable()
      {
        @Override
        public void doTask()
        {
          tableData.sort(columnIndex, sortDir);
        }

        @Override
        public void doUITask()
        {
          if (!table.isDisposed())
          {
            table.deselectAll();
            table.clearAll();

            if (selectedRowObject != null)
            {
              int row = tableData.getIndexOfRowObject(selectedRowObject);
              selectCell(row, selectedColumn);
              table.select(row);
              table.showSelection();
            }

            innerComposite.setEnabled(true);
            BusyManager.setNotBusy(CustomTable.this);
          }
        }
      };

      BusyManager.setBusy(this);
      innerComposite.setEnabled(false);
      worker.startTask();
    }
    else if (e.widget == table)
    {
      TableItem item = (TableItem)e.item;
      selectCell(table.indexOf(item), selectedColumn);
    }

  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    widgetSelected(e);
  }

  public void setTableData(TableData tableData)
  {
    this.tableData = tableData;

    String[] columnNames = tableData.getColumnNames();
    for (int index = 0; index < tableData.getColumnCount(); index++)
    {
      TableColumn column = new TableColumn(table, SWT.NONE);
      if (columnNames != null)
      {
        column.setText(columnNames[index]);
        column.addSelectionListener(this);
        column.setMoveable(true);

        column.setToolTipText(tableData.getColumnToolTip(index));
      }
    }

    table.setItemCount(this.tableData.getRowCount());

    if (columnNames != null)
    {
      for (int index = 0; index < columnNames.length; index++)
      {
        TableColumn column = table.getColumn(index);
        column.pack();
        column.setWidth(column.getWidth() + 30); // for the sort icon
      }
    }

    table.setVisible(true);
  }

  private void deselect()
  {
    int[] selections = table.getSelectionIndices();
    int min = Math.min(selectionStart, selectionStop);
    int max = Math.max(selectionStart, selectionStop);
    for (int i = 0; i < selections.length; i++)
    {
      int index = selections[i];
      if (index < min || index > max)
        table.deselect(index);
    }
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL)
    {
      selectAll();
    }
    else if (e.keyCode == 'c' && e.stateMask == SWT.CONTROL)
    {
      copyCell();
    }
    else if (e.keyCode == SWT.ARROW_RIGHT)
    {
      e.doit = false;

      int currentIndex = getCurrentColumnIndex(selectedColumn);
      if (selectedRow >= 0 && currentIndex + 1 < table.getColumnCount())
      {
        table.showColumn(getColumnForIndex(currentIndex + 1));
        selectCell(selectedRow, getColumnNumberForIndex(currentIndex + 1));
      }
    }
    else if (e.keyCode == SWT.ARROW_LEFT)
    {
      e.doit = false;

      int currentIndex = getCurrentColumnIndex(selectedColumn);
      if (selectedRow >= 0 && currentIndex - 1 >= 0)
      {
        table.showColumn(getColumnForIndex(currentIndex - 1));
        selectCell(selectedRow, getColumnNumberForIndex(currentIndex - 1));
      }
    }
  }
  @Override
  public void keyReleased(KeyEvent e)
  {
  }

  private TableColumn getColumnForIndex(int index)
  {
    int[] order = table.getColumnOrder();
    int column = 0;
    while (column < order.length)
    {
      if (order[column] == index)
        break;
      column++;
    }
    return table.getColumn(column);
  }

  private int getColumnNumberForIndex(int index)
  {
    int[] order = table.getColumnOrder();
    int column = 0;
    while (column < order.length)
    {
      if (order[column] == index)
        break;
      column++;
    }
    return column;
  }

  private int getCurrentColumnIndex(int column)
  {
    int[] order = table.getColumnOrder();
    int index = 0;
    while (index < order.length)
    {
      if (order[index] == column)
        break;
      index++;
    }

    return index;
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
    if (e.button == 1)
    {
      Point pt = new Point(1, e.y);
      TableItem item = table.getItem(pt);
      if (item != null)
      {
        selectCell(new Point(e.x, e.y));

        selectionStart = table.indexOf(item);
        if (table.getSelectionCount() > 1)
        {
          int min = 999999999;
          int max = -1;
          int[] selections = table.getSelectionIndices();
          for (int i = 0; i < selections.length; i++)
          {
            if (selections[i] < min)
              min = selections[i];
            else if (selections[i] > max)
              max = selections[i];
          }

          if (selectionStart == max)
          {
            selectionStart = min;
            selectionStop = max;
          }
          else
          {
            selectionStart = max;
            selectionStop = min;
          }

        }
        mouseSelect = true;
        table.setCapture(true);
      }
    }
    else if (e.button == 3)
    {
      selectCell(new Point(e.x, e.y));
      /*
      TableItem item = table.getItem(pt);
      if (item != null)
      {
        int columnCount = table.getColumnCount();
        for (int i = 0; i < columnCount; i++)
        {
          Rectangle rect = item.getBounds(i);
          if (rect.contains(pt))
          {
            cellText = item.getText(i).trim();
            break;
          }
        }
      }
      else
        cellText = "";
        */
    }
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
    if (e.button == 1)
    {
      selectionStart = -1;
      selectionStop = -1;
      mouseSelect = false;
      table.setCapture(false);
      scrollUnit = -1;
      if (scrollTask != null)
      {
        scrollTask.cancel();
        scrollTask = null;
      }
    }
  }
  @Override
  public void mouseMove(MouseEvent e)
  {
    if (mouseSelect)
    {
      int scrollBarHeight = 0;
      if (table.getHorizontalBar() != null)
        scrollBarHeight = table.getHorizontalBar().getSize().y;
      int height = table.getBounds().height - table.getBorderWidth() - scrollBarHeight;

      if (e.y < table.getHeaderHeight() || e.y > height)
      {
        if (e.y < table.getHeaderHeight())
        {
          int pixAbove = Math.abs(e.y - table.getHeaderHeight());
          int unit = (pixAbove / 5 + 1);
          if (unit > 5)
            unit = 5;

          if (unit != scrollUnit)
          {
            scrollUnit = pixAbove / 5 + 1;
            if (scrollTask != null)
            {
              scrollTask.cancel();
              scrollTask = null;
            }

            scrollTask = new ScrollTask(ScrollTask.UP);
            timer.schedule(scrollTask, 0, 100 - (unit - 1) * 20);
          }
        }
        else if (e.y > height)
        {
          int pixBelow = e.y - height;
          int unit = (pixBelow / 5 + 1);
          if (unit > 5)
            unit = 5;

          if (unit != scrollUnit)
          {
            scrollUnit = pixBelow / 5 + 1;
            if (scrollTask != null)
            {
              scrollTask.cancel();
              scrollTask = null;
            }

            scrollTask = new ScrollTask(ScrollTask.DOWN);
            timer.schedule(scrollTask, 0, 100 - (unit - 1) * 20);
          }
        }
      }
      else
      {
        Point pt = new Point(1, e.y);
        TableItem item = table.getItem(pt);
        if (item != null)
        {
          scrollUnit = -1;
          if (scrollTask != null)
          {
            scrollTask.cancel();
            scrollTask = null;
          }

          int index = table.indexOf(item);
          if (index != selectionStop)
          {
            selectionStop = index;
            deselect();

            if (selectionStart < selectionStop)
              table.select(selectionStart, selectionStop);
            else
              table.select(selectionStop, selectionStart);
          }

          selectCell(new Point(e.x, e.y));
        }
      }
    }
  }




  private class ScrollTask extends TimerTask
  {
    public final static int UP = 1;
    public final static int DOWN = 2;

    private int direction;
    public ScrollTask(int direction)
    {
      this.direction = direction;
    }

    @Override
    public void run()
    {
      table.getDisplay().syncExec(new Runnable(){
        @Override
        public void run() {

          boolean changed = false;
          int top = 0;
          if (direction == DOWN)
          {
            table.setTopIndex(table.getTopIndex() + 1);

            TableItem[] items = table.getItems();
            for (int i = selectionStop + 1; i < items.length; i++)
            {
              Rectangle rect = items[i].getBounds(0);
              if (rect.y < (table.getBounds().height - table.getBorderWidth()))
              {
                if (selectionStop != i)
                {
                  changed = true;
                  selectionStop = i;
                }
              }
              else
                break;
            }
          }
          else
          {
            top = table.getTopIndex() - 1;
            if (top > selectionStop)
              top = selectionStop;
            if (top < 0)
              top = 0;

            table.setTopIndex(top);

            TableItem[] items = table.getItems();
            for (int i = selectionStop - 1; i >= 0; i--)
            {
              Rectangle rect = items[i].getBounds(0);
              if (rect.y + rect.height >= 0)
              {
                if (selectionStop != i)
                {
                  changed = true;
                  selectionStop = i;
                }
              }
              else
                break;
            }

          }

          if (changed)
          {
            deselect();
            if (selectionStart < selectionStop)
            {
              table.select(selectionStart, selectionStop);
              int line = table.getItemHeight() + table.getGridLineWidth();
              int linesVis = table.getClientArea().height / line;
              if (table.getTopIndex() < (selectionStop - linesVis + 1))
              {
                table.setTopIndex(selectionStop - linesVis + 1);
              }
            }
            else
            {
              table.select(selectionStop, selectionStart);
              if (table.getTopIndex() > selectionStop)
                table.setTopIndex(selectionStop);
            }

            selectCell(selectionStop, selectedColumn);
          }
        }
      });
    }
  }

  private class DataListener implements Listener
  {
    @Override
    public void handleEvent(Event e)
    {
      TableItem item = (TableItem) e.item;
      int rowIndex = table.indexOf(item);

      for (int colIndex = 0; colIndex < tableData.getColumnCount(); colIndex++)
      {
        String value = tableData.getCellAsString(rowIndex, colIndex);
        if (value == null)
        {
          item.setBackground(colIndex, StellaColors.getInstance().getLightGray());
          value = tableData.getNullString();
        }

        item.setText(colIndex, value);
      }
    }
  }

  public Object getObjectAt(Point pt)
  {
    Object obj = null;
    TableItem item = table.getItem(pt);
    if (item != null)
    {
      int rowIndex = table.indexOf(item);
      int column = -1;
      for (int i = 0; i < table.getColumnCount(); i++)
      {
        Rectangle rect = item.getBounds(i);
        if (rect.contains(pt))
        {
          column = i;
          break;
        }
      }

      if (rowIndex >= 0 && column >= 0)
      {
        obj = tableData.getCell(rowIndex, column);
      }
    }

    return obj;
  }

  public static void main(String[] args)
  {

    Display display = new Display();
    StellaClipBoard.init(display);

    Shell shell = new Shell(display);
    shell.setText("Test");
    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 5;
    gridLayout.marginWidth = 5;
    gridLayout.marginBottom = 5;
    gridLayout.horizontalSpacing = 0;
    shell.setLayout(gridLayout);

    CustomTable test = new CustomTable(shell);
    test.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    test.setTableData(new ResultData());



    shell.setSize(500, 400);

    shell.open();

    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {
    copyRowMI.setEnabled(table.getSelectionCount() > 0);
    copyHtmlMI.setEnabled(table.getSelectionCount() > 0);

    if (selectedRow >= 0)
    {
      TableItem item = table.getItem(selectedRow);
      String value = item.getText(selectedColumn);
      copyCellMI.setEnabled(value != null && value.trim().length() > 0);
    }
    else
      copyCellMI.setEnabled(false);
  }

  private void updateCellSelectRedraw()
  {
    if (selectedRow >= 0)
    {
      TableItem item = table.getItem(selectedRow);
      Rectangle rect = item.getBounds(selectedColumn);
      table.redraw(rect.x - 5, rect.y - 5, rect.width + 10, rect.height + 10, true);
    }
  }

  private void selectCell(Point pt)
  {
    TableItem item = table.getItem(pt);
    if (item != null)
    {
      int row = table.indexOf(item);
      int column = selectedColumn;

      int columnCount = table.getColumnCount();
      for (int i = 0; i < columnCount; i++)
      {
        Rectangle rect = item.getBounds(i);
        if (rect.contains(pt))
        {
          column = i;
          break;
        }
      }

      selectCell(row, column);
    }
  }

  private void selectCell(int row, int column)
  {
    if (row != selectedRow || column != selectedColumn)
    {
      updateCellSelectRedraw();
      selectedRow = row;
      selectedColumn = column;
      updateCellSelectRedraw();
    }
  }

  @Override
  public void paintControl(PaintEvent e)
  {
    if (selectedRow >= 0)
    {
      TableItem item = table.getItem(selectedRow);
      Rectangle rect = item.getBounds(selectedColumn);
      e.gc.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      e.gc.setLineWidth(2);
      e.gc.drawRectangle(rect);
    }
  }

}
