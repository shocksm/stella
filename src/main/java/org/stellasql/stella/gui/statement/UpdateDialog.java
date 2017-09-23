package org.stellasql.stella.gui.statement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.SqlTextAdditions;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.WorkerRunnable;
import org.stellasql.stella.session.SessionData;

public class UpdateDialog extends BaseInsertUpdateDialog implements ModifyListener
{
  private final static Logger logger = LogManager.getLogger(UpdateDialog.class);

  private Composite whereComposite = null;
  private Label whereLabel;
  private StyledText whereText = null;
  private Sash sash2 = null;
  private int sash2Limit = 100;
  private int lastCompositeHeight = 0;
  private int lastSash2Height = 0;
  private int lastSashHeight = 0;
  private ToolItem refreshButton = null;
  private Label countLabel = null;

  public UpdateDialog(Shell parent, TableInfo tableInfo, SessionData sessionData)
  {
    super(parent, tableInfo, sessionData);

    super.init(true, false, true);

    setText("Create update statment for " + this.tableInfo.getName());

    getShell().addControlListener(this);
  }

  @Override
  protected void createControls(boolean editable, boolean forceChecks, boolean hasWhere)
  {
    super.createControls(editable, forceChecks, hasWhere);

    whereComposite = new Composite(composite, SWT.NONE);
    whereComposite.addControlListener(this);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    whereComposite.setLayout(gridLayout);
    whereComposite.setBackground(whereComposite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

    Composite composite = new Composite(whereComposite, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 4;
    gridLayout.marginHeight = 0;
    gridLayout.verticalSpacing = 1;
    gridLayout.marginWidth = 0;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    whereLabel = new Label(composite, SWT.NONE);
    whereLabel.setText("WHERE");
    whereLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    GridData gd = new GridData();
    gd.horizontalIndent = 4;
    whereLabel.setLayoutData(gd);

    ToolBar toolBar = new ToolBar(composite, SWT.FLAT);
    refreshButton = new ToolItem(toolBar, SWT.PUSH);
    refreshButton.setToolTipText("Refresh record count");
    refreshButton.setImage(StellaImages.getInstance().getRefreshImage());
    refreshButton.addSelectionListener(this);
    gd = new GridData();
    gd.horizontalIndent = 40;
    toolBar.setLayoutData(gd);

    countLabel = new Label(composite, SWT.NONE);
    countLabel.setText("?");
    countLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    Label label = new Label(composite, SWT.NONE);
    label.setText("rows will be updated");
    label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

    whereText = new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    whereText.addModifyListener(this);
    whereText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
    whereText.setIndent(2);
    new SqlTextAdditions(whereText, sessionData.getQuerySeparator());

    sash2 = new Sash(this.composite, SWT.HORIZONTAL | SWT.SMOOTH);
    sash2.addSelectionListener(this);
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
    fd.top = new FormAttachment(0, 250);
    fd.height = 4;
    sash.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(sash, 0);
    fd.bottom = new FormAttachment(sash2, 0);
    fd.height = 100;
    whereComposite.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 400);
    fd.height = 4;
    sash2.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(sash2, 0);
    fd.bottom = new FormAttachment(100, 0);
    statementText.setLayoutData(fd);
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
      moveSash2(sashLoc + 150);
    }

    lastSash2Height = lastCompositeHeight - sash2.getLocation().y;
    lastSashHeight = lastCompositeHeight - sash.getLocation().y;
  }


  @Override
  protected void buildStatement()
  {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("UPDATE ");
    sbuf.append(tableInfo.getProperName());
    sbuf.append("\nSET ");

    StringBuffer updates = new StringBuffer();

    int max = 80;
    StringBuffer line = new StringBuffer();

    for (Iterator it = cwList.iterator(); it.hasNext();)
    {
      ColumnWidget cw = (ColumnWidget)it.next();
      if (cw.getChecked())
      {
        StringBuffer temp = new StringBuffer();
        if (updates.length() > 0 || line.length() > 0)
          line.append(", ");
        temp.append(cw.getColumnInfo().getColumnName());
        temp.append(" = ");
        if (cw.getValue().length() > 0)
          temp.append(cw.getValue());
        else
          temp.append("?");


        if (line.length() + temp.length() > max)
        {
          updates.append(line).append("\n");
          line.delete(0, line.length());
        }

        line.append(temp);
      }
    }
    updates.append(line);

    sbuf.append(updates);

    String where = whereText.getText().trim();

    if (where.length() > 0)
      sbuf.append("\n").append("WHERE ").append(where);

    statementText.setText(sbuf.toString());
  }

  @Override
  public void modifyText(ModifyEvent e)
  {
    buildStatement();
    if (!countLabel.getText().equals("?"))
    {
      countLabel.setText("?");
      countLabel.getParent().layout();
    }
  }

  private int moveSash(int y)
  {
    int limit = 100;
    Rectangle sashRect = sash.getBounds();
    int top = limit;
    int bottom = sash2.getLocation().y - limit;

    if (y > bottom)
      bottom = moveSash2(sash2.getLocation().y + (y - bottom)) - limit;

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

  private int moveSash2(int y)
  {
    int top = sash.getLocation().y + sash2Limit;
    int bottom = sash2.getParent().getSize().y - sash2.getSize().y - sash2Limit;

    if (y < top)
      top = moveSash(sash.getLocation().y - (top - y)) + sash2Limit;

    y = Math.max(Math.min(y, bottom), top);

    if (y != sash2.getLocation().y)
    {
      FormData fd = (FormData)sash2.getLayoutData();
      fd.top = new FormAttachment(0, y);
      sash2.getParent().layout();
    }

    lastSash2Height = sash.getParent().getSize().y - sash2.getLocation().y;

    return y;
  }

  public void setWhereClause(String where)
  {
    whereText.setText(where);
    updateCount();
  }

  @Override
  public void addToWhere(String text)
  {
    if (whereText.getSelectionCount() == 0)
    {
      whereText.insert(text);
      whereText.setCaretOffset(whereText.getCaretOffset() + text.length());
    }
    else
    {
      Point pt = whereText.getSelection();
      whereText.insert(text);
      whereText.setCaretOffset(pt.x + text.length());
    }
    whereText.setFocus();
  }

  private void updateCount()
  {
    String temp = "select count(*) from " + tableInfo.getProperName();
    if (whereText.getText().trim().length() > 0)
      temp += " where " + whereText.getText();

    final String sql = temp;

    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;
      int count = 0;

      @Override
      public void doTask()
      {
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
          Connection con = UpdateDialog.this.sessionData.getConnectionManager().getConnection();
          stmt = con.createStatement();
          rs = stmt.executeQuery(sql);
          if (rs.next())
          {
            count = rs.getInt(1);
          }
        }
        catch (SQLException e)
        {
          logger.info(e.getMessage(), e);
          ex = e;
        }
        finally
        {
          if (rs != null)
          {
            try
            {
              rs.close();
            }
            catch (SQLException e)
            {
              logger.error(e.getMessage(), e);
            }
          }

          if (stmt != null)
          {
            try
            {
              stmt.close();
            }
            catch (SQLException e)
            {
              logger.error(e.getMessage(), e);
            }
          }

          UpdateDialog.this.sessionData.getConnectionManager().releaseConnection();
        }
      }

      @Override
      public void doUITask()
      {
        if (!getShell().isDisposed())
        {
          if (ex != null)
          {
            countLabel.setText("?");
            countLabel.getParent().layout();

            MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
            messageDlg.setText("Select count failed");
            messageDlg.setMessage(ex.getClass().getName() + "\n\n" + ex.getMessage());
            messageDlg.open();
          }
          else
          {
            countLabel.setText(NumberFormat.getInstance().format(count));
            countLabel.getParent().layout();
          }

          setEnabled(true);
          BusyManager.setNotBusy(getShell());
        }
      }
    };

    setEnabled(false);
    BusyManager.setBusy(getShell());
    worker.startTask();

  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == sash)
    {
      e.y = moveSash(e.y);
    }
    else if (e.widget == sash2)
    {
      e.y = moveSash2(e.y);
    }
    else if (e.widget == refreshButton)
    {
      updateCount();
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
        moveSash2(sash.getParent().getSize().y - lastSash2Height);
      }
      else if (sash.getParent().getSize().y > lastCompositeHeight)
      {
        moveSash2(sash.getParent().getSize().y - lastSash2Height);
        moveSash(sash.getParent().getSize().y - lastSashHeight);
      }

      lastCompositeHeight = sash.getParent().getSize().y;

    }

    super.controlResized(e);
  }


  public static void main(String[] args)
  {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.pack();
    shell.open();


    TableInfo tableInfo = new TableInfo("SomeTable", "SomeCatalog", "SomeScheme", "TABLE", true, true, ".");
    ColumnInfo columnInfo = new ColumnInfo();
    columnInfo.setColumnName("Column1");
    columnInfo.setTypeName("varchar2");
    columnInfo.setNullable(false);
    columnInfo.setDefault("asdf");
    columnInfo.setDataType(Types.CHAR);
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


    UpdateDialog id = new UpdateDialog(shell, tableInfo, null);
    id.open();

    while (!id.getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }


}
