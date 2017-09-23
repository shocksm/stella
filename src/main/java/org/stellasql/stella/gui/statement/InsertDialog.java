package org.stellasql.stella.gui.statement;

import java.sql.Types;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.gui.util.SyntaxHighlighter;
import org.stellasql.stella.session.SessionData;

public class InsertDialog extends BaseInsertUpdateDialog
{
  private int lastCompositeHeight = 0;
  private int lastSashHeight = 0;

  public InsertDialog(Shell parent, TableInfo tableInfo, SessionData sessionData)
  {
    super(parent, tableInfo, sessionData);

    super.init(true, true, false);
    setText("Create insert statment for " + this.tableInfo.getName());

    getShell().addControlListener(this);
  }

  public void addColumnValue(String columnName, Object value)
  {
    for (Iterator it = cwList.iterator(); it.hasNext();)
    {
      ColumnWidget cw = (ColumnWidget)it.next();
      if (cw.getColumnInfo().getColumnName().equals(columnName))
        cw.setValue(value);
    }
  }

  @Override
  public void open()
  {
    super.open();

    lastCompositeHeight = sash.getParent().getSize().y;

    Point pt = columnsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    if (pt.y < columnsComposite.getSize().y)
    {
      int sashLoc = pt.y + inner.getLocation().y + 2; // 2 = top and bottom line from inner
      if (sc.getClientArea().width < pt.x) // add the height of the scroll bar if it will be shown
        sashLoc += sc.getHorizontalBar().getSize().y;
      moveSash(sashLoc);
    }

    lastSashHeight = lastCompositeHeight - sash.getLocation().y;
  }

  private int moveSash(int y)
  {
    int limit = 100;
    Rectangle sashRect = sash.getBounds();
    int top = limit;
    int bottom = sash.getParent().getSize().y - limit;

    y = Math.max(Math.min(y, bottom), top);
    if (y != sashRect.y)
    {
      FormData fd = (FormData)sash.getLayoutData();
      fd.top = new FormAttachment(0, y);
      sash.getParent().layout();
    }

    lastSashHeight = sash.getParent().getSize().y - sash.getLocation().y;
    return y;
  }

  @Override
  protected void layoutControls()
  {
    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(sash, 0);
    inner.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 350);
    fd.height = 4;
    sash.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(sash, 0);
    fd.bottom = new FormAttachment(100, 0);
    statementText.setLayoutData(fd);
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == sash)
    {
      e.y = moveSash(e.y);
    }
    else
      super.widgetSelected(e);
  }

  @Override
  public void controlResized(ControlEvent e)
  {
    if (getShell().isVisible())
    {
      if (sash.getParent().getSize().y < lastCompositeHeight)
      {
        moveSash(sash.getParent().getSize().y - lastSashHeight);
      }
      else if (sash.getParent().getSize().y > lastCompositeHeight)
      {
        moveSash(sash.getParent().getSize().y - lastSashHeight);
      }

      lastCompositeHeight = sash.getParent().getSize().y;

    }

    super.controlResized(e);
  }

  @Override
  protected void buildStatement()
  {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("INSERT INTO ");
    sbuf.append(tableInfo.getProperName());

    StringBuffer columns = new StringBuffer();
    StringBuffer values = new StringBuffer();

    int max = 80;
    StringBuffer columsLine = new StringBuffer();
    StringBuffer valuesLine = new StringBuffer();

    for (Iterator it = cwList.iterator(); it.hasNext();)
    {
      ColumnWidget cw = (ColumnWidget)it.next();
      if (cw.getChecked())
      {
        StringBuffer temp = new StringBuffer();
        if (columns.length() > 0 || columsLine.length() > 0)
          columsLine.append(", ");

        temp.append(cw.getColumnInfo().getColumnName());

        if (columsLine.length() + temp.length() > max)
        {
          columns.append(columsLine).append("\n");
          columsLine.delete(0, columsLine.length());
        }
        columsLine.append(temp);



        temp = new StringBuffer();
        if (values.length() > 0 || valuesLine.length() > 0)
          valuesLine.append(", ");

        if (cw.getValue().length() > 0)
          temp.append(cw.getValue());
        else
          temp.append("?");

        if (valuesLine.length() + temp.length() > max)
        {
          values.append(valuesLine).append("\n");
          valuesLine.delete(0, valuesLine.length());
        }
        valuesLine.append(temp);
      }
    }
    columns.append(columsLine);
    values.append(valuesLine);

    sbuf.append("\n(").append(columns).append(")\n");
    sbuf.append("VALUES");
    sbuf.append("\n(").append(values).append(")\n");

    statementText.setText(sbuf.toString());
  }


  public static void main(String[] args)
  {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.pack();
    shell.open();

    display.addFilter(SWT.KeyDown, new Listener(){@Override
    public void handleEvent(Event event)
    {
      System.out.println("HERE: " + event.keyCode + " " + event.stateMask);

      event.doit = false;
    }});

    display.addFilter(SWT.Traverse, new Listener(){@Override
    public void handleEvent(Event event)
    {
      System.out.println("HERE2: " + event.keyCode + " " + event.stateMask);

      event.doit = false;
    }});

    try
    {
      ApplicationData.getInstance().load();
    }
    catch (DocumentException e)
    {
      e.printStackTrace();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    SyntaxHighlighter.initializeColors();

    TableInfo tableInfo = new TableInfo("SomeTable", "SomeCatalog", "SomeScheme", "TABLE", true, true, ".");
    ColumnInfo columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column1");
    columnInfo.setTypeName("varchar2");
    columnInfo.setDataType(Types.CHAR);
    columnInfo.setNullable(false);
    columnInfo.setDefault("asdf");
    tableInfo.addColumn(columnInfo);

    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column2");
    columnInfo.setTypeName("date");
    columnInfo.setNullable(true);
    columnInfo.setDataType(Types.DATE);
    tableInfo.addColumn(columnInfo);

    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column3");
    columnInfo.setTypeName("timestamp");
    columnInfo.setNullable(true);
    columnInfo.setDataType(Types.TIMESTAMP);
    tableInfo.addColumn(columnInfo);

    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("time");
    columnInfo.setNullable(false);
    columnInfo.setDataType(Types.TIME);
    tableInfo.addColumn(columnInfo);

    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);

    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);

    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);
    columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column4");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    tableInfo.addColumn(columnInfo);

    InsertDialog id = new InsertDialog(shell, tableInfo, null);
    id.open();

    while (!id.getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }






}
