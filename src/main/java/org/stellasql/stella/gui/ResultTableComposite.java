package org.stellasql.stella.gui;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.IndexInfo;
import org.stellasql.stella.PrimaryKeyInfo;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.connection.ConnectionManager;
import org.stellasql.stella.export.ExportOptions;
import org.stellasql.stella.gui.custom.CustomTable;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.statement.DataTypeUtil;
import org.stellasql.stella.gui.statement.DeleteDialog;
import org.stellasql.stella.gui.statement.InsertBuilder;
import org.stellasql.stella.gui.statement.InsertDialog;
import org.stellasql.stella.gui.statement.UpdateDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.StyledTextContextMenu;
import org.stellasql.stella.gui.util.WorkerRunnable;
import org.stellasql.stella.session.SessionData;
import org.stellasql.stella.util.TimeFormatter;

public class ResultTableComposite extends Composite implements MouseListener, DisposeListener, FontChangeListener, MenuListener, SelectionListener
{
  private final static Logger logger = LogManager.getLogger(ResultTableComposite.class);

  private final static int UPDATEROW = 1;
  private final static int INSERTROW = 2;
  private final static int DELETEROW = 3;
  private final static int UPDATE = 4;
  private final static int INSERT = 5;
  private final static int DELETE = 6;

  private String sessionName = null;
  private CustomTable customTable = null;
  private MenuItem viewDataMI = null;
  private MenuItem updateMI  = null;
  private MenuItem updateRowMI  = null;
  private MenuItem insertMI  = null;
  private MenuItem insertRowMI  = null;
  private MenuItem deleteMI  = null;
  private MenuItem deleteRowMI  = null;
  private StyledText status = null;
  private ToolBar runToolBar = null;
  private ToolItem runButton = null;
  private StyledText query = null;
  private boolean editable = false;

  private QueryComposite queryComposite;
  private String queryString;
  private ResultData resultData;

  private MenuItem exportMI;

  public ResultTableComposite(Composite parent, String sessionName, QueryComposite qc)
  {
    super(parent, SWT.NONE);
    this.setBackgroundMode(SWT.INHERIT_DEFAULT);
    //this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

    addDisposeListener(this);

    this.sessionName = sessionName;
    queryComposite = qc;

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 2;
    gridLayout.marginWidth = 2;
    gridLayout.verticalSpacing = 2;
    this.setLayout(gridLayout);

    status = new StyledText(this, SWT.READ_ONLY | SWT.SINGLE);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalIndent = 8;
    gridData.verticalIndent = 2;
    gridData.horizontalSpan = 2;
    status.setLayoutData(gridData);
    status.setBackground(this.getBackground());
    new StyledTextContextMenu(status);

    runToolBar = new ToolBar(this, SWT.FLAT);
    runButton = new ToolItem(runToolBar, SWT.PUSH);
    runButton.setImage(StellaImages.getInstance().getRunImage());
    runButton.addSelectionListener(this);
    runButton.setToolTipText("Re-Execute query");
    runButton.setEnabled(false);
    runToolBar.setVisible(false);

    query = new StyledText(this, SWT.READ_ONLY | SWT.MULTI);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalIndent = 8;
    query.setLayoutData(gridData);
    query.setBackground(this.getBackground());
    new StyledTextContextMenu(query);

    customTable = new CustomTable(this, true, true, true);
    customTable.setVisible(false);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    gridData.horizontalSpan = 2;
    customTable.setLayoutData(gridData);
    customTable.addMouseListener(this);

    customTable.getMenu().addMenuListener(this);

    new MenuItem(customTable.getMenu(), SWT.SEPARATOR);

    viewDataMI = new MenuItem(customTable.getMenu(), SWT.PUSH);
    viewDataMI.addSelectionListener(this);
    viewDataMI.setText("&View cell data");

    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);

    layout();
    gridData = (GridData)query.getLayoutData();
    gridData.heightHint = query.getBounds().height;
  }

  public String getQuery()
  {
    return queryString;
  }

  protected void setFonts()
  {
    FontSetter.setAllControlFonts(this, ApplicationData.getInstance().getResultsFont());
  }

  public ResultData getResultData()
  {
    return resultData;
  }

  public void setResultData(ResultData rd, int maxRows, long queryMillis, long formatMillis)
  {
    resultData = rd;
    if (rd.getTable() != null)
    {
      editable = true;

      new MenuItem(customTable.getMenu(), SWT.SEPARATOR);

      insertRowMI  = new MenuItem(customTable.getMenu(), SWT.PUSH);
      insertRowMI.addSelectionListener(this);
      insertRowMI.setText("Create I&nsert for selected row(s)");

      updateRowMI  = new MenuItem(customTable.getMenu(), SWT.PUSH);
      updateRowMI.addSelectionListener(this);
      updateRowMI.setText("Create U&pdate for selected row");

      deleteRowMI  = new MenuItem(customTable.getMenu(), SWT.PUSH);
      deleteRowMI.addSelectionListener(this);
      deleteRowMI.setText("Create De&lete for selected row");

      new MenuItem(customTable.getMenu(), SWT.SEPARATOR);

      insertMI  = new MenuItem(customTable.getMenu(), SWT.PUSH);
      insertMI.addSelectionListener(this);
      insertMI.setText("Create &Insert Statement");

      updateMI  = new MenuItem(customTable.getMenu(), SWT.PUSH);
      updateMI.addSelectionListener(this);
      updateMI.setText("Create &Update Statement");

      deleteMI  = new MenuItem(customTable.getMenu(), SWT.PUSH);
      deleteMI.addSelectionListener(this);
      deleteMI.setText("Create &Delete Statement");
    }

    new MenuItem(customTable.getMenu(), SWT.SEPARATOR);

    exportMI = new MenuItem(customTable.getMenu(), SWT.PUSH);
    exportMI.addSelectionListener(this);
    exportMI.setText("&Export Results");

    StringBuffer sbuf = new StringBuffer();
    int maxLength = 0;
    if (maxRows > 0 && rd.getRowCount() == maxRows)
      sbuf.append("Results limited to ").append(maxRows);
    else
      sbuf.append(NumberFormat.getInstance().format(rd.getRowCount()));

    sbuf.append(" rows");
    if (maxRows > 0 && rd.getRowCount() == maxRows)
      maxLength = sbuf.length();

    sbuf.append(" - time: " + TimeFormatter.format(queryMillis + formatMillis));
    sbuf.append(" (query: " + TimeFormatter.format(queryMillis));
    sbuf.append(", processing results: " + TimeFormatter.format(formatMillis));
    sbuf.append(")");

    status.setText(sbuf.toString());
    if (maxLength > 0)
    {
      StyleRange style = new StyleRange(0, maxLength, this.getDisplay().getSystemColor(SWT.COLOR_RED), null);
      status.setStyleRange(style);
    }

    runButton.setEnabled(true);

    queryString = rd.getQuery();
    query.setText(rd.getQuery());
    query.setToolTipText(rd.getQuery());

    customTable.setTableData(rd);
    customTable.setVisible(true);

    runToolBar.setVisible(true);
  }

  private void displayCellData()
  {
    Object obj = customTable.getSelectedCellObject();

    if (obj != null)
    {
      if (obj instanceof Clob)
      {
        Clob clob = (Clob)obj;
        try
        {
          Reader reader = clob.getCharacterStream();
          char[] charArray = new char[1024];
          int len = 0;
          StringBuffer sbuf = new StringBuffer();
          while ((len = reader.read(charArray)) > 0)
          {
            sbuf.append(charArray, 0, len);
          }
          reader.close();
          String value = sbuf.toString();
          Stella.getInstance().displayValue(value);
        }
        catch (Exception e1)
        {
          logger.error(e1.getMessage(), e1);
          SessionData.getSessionData(sessionName).addErrorMessage(e1.getMessage(), true);
        }
      }
      else
      {
        Stella.getInstance().displayValue(obj.toString());
      }
    }
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
    displayCellData();
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
  }
  @Override
  public void mouseUp(MouseEvent e)
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
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {
    if (customTable.getSelectedCellObject() != null)
      viewDataMI.setEnabled(true);
    else
      viewDataMI.setEnabled(false);

    if (editable)
    {
      insertRowMI.setEnabled(customTable.getSelectionCount() >= 1);
      updateRowMI.setEnabled(customTable.getSelectionCount() == 1);
      deleteRowMI.setEnabled(customTable.getSelectionCount() == 1);
    }

    exportMI.setEnabled(resultData.getRowCount() > 0);
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == viewDataMI)
      displayCellData();
    else if (e.widget == updateRowMI)
      showUpdateRow();
    else if (e.widget == deleteRowMI)
      showDeleteRow();
    else if (e.widget == insertRowMI)
      showInsertRow();
    else if (e.widget == updateMI)
      showUpdate();
    else if (e.widget == deleteMI)
      showDelete();
    else if (e.widget == insertMI)
      showInsert();
    else if (e.widget == exportMI)
      exportResults();
    else if (e.widget == runButton)
      queryComposite.rerun(this);
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  public void exportResults()
  {
    ExportDialog ed = new ExportDialog(this.getShell(), SessionData.getSessionData(sessionName));
    final ExportOptions eo = ed.open();
    if (eo != null)
    {

      WorkerRunnable worker = new WorkerRunnable()
      {
        String msg = null;
        Exception ex = null;
        @Override
        public void doTask()
        {
          try
          {
            msg = eo.exportResultData(resultData);
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
          if (!ResultTableComposite.this.isDisposed())
          {
            if (ex != null)
            {
              MessageDialog messageDlg = new MessageDialog(ResultTableComposite.this.getShell(), SWT.OK);
              messageDlg.setText("Error");
              messageDlg.setMessage(ex.getClass().getName() + "\n\n" + ex.getMessage());
              messageDlg.open();
            }
            else
            {
              SessionData sd = Stella.getInstance().getSelectedSessionData();
              sd.addMessage(msg, true);
            }

            BusyManager.setNotBusy(Stella.getInstance().getShell());
          }
        }
      };

      BusyManager.setBusy(Stella.getInstance().getShell());
      worker.startTask();
    }
  }

  private void findPrimaryKeys(TableInfo tableInfo, List resultColumnIndex)
  {
    if (tableInfo.getPrimaryKeys().size() > 0)
    {
      for (Iterator it = tableInfo.getPrimaryKeys().iterator(); it.hasNext();)
      {
        PrimaryKeyInfo pkInfo = (PrimaryKeyInfo)it.next();
        boolean found = false;
        for (int index = 0; index < resultData.getColumnNames().length; index++)
        {
          if (pkInfo.getColumnName().equals(resultData.getColumnNames()[index]))
          {
            if (isValidForWhere(resultData.getSqlType(index)))
            {
              resultColumnIndex.add(new Integer(index));
              found = true;
              break;
            }
          }
        }
        if (!found)
        {
          resultColumnIndex.clear();
          break;
        }
      }
    }
  }

  private void findUniqueIndex(TableInfo tableInfo, List resultColumnIndex)
  {
    if (tableInfo.getIndexes().size() > 0)
    {
      HashSet indexNameSet = new HashSet();
      for (Iterator it = tableInfo.getIndexes().iterator(); it.hasNext();)
      {
        IndexInfo iInfo = (IndexInfo)it.next();
        if (!iInfo.getNonUnique() && iInfo.getName() != null)
          indexNameSet.add(iInfo.getName());
      }

      if (indexNameSet.size() > 0)
      {
        for (Iterator itSet = indexNameSet.iterator(); itSet.hasNext();)
        {
          String indexName = (String)itSet.next();

          boolean uniqueIndexfound = true;
          for (Iterator it = tableInfo.getIndexes().iterator(); it.hasNext();)
          {
            IndexInfo iInfo = (IndexInfo)it.next();
            if (iInfo.getName() != null && iInfo.getName().equals(indexName))
            {
              boolean found = false;
              for (int index = 0; index < resultData.getColumnNames().length; index++)
              {
                if (iInfo.getColumnName().equals(resultData.getColumnNames()[index]))
                {
                  if (isValidForWhere(resultData.getSqlType(index)))
                  {
                    resultColumnIndex.add(new Integer(index));
                    found = true;
                    break;
                  }
                }
              }
              if (!found)
              {
                resultColumnIndex.clear();
                uniqueIndexfound = false;
                break;
              }
            }
          }

          if (uniqueIndexfound)
          {
            break;
          }
        }
      }
    }
  }

  private void findAllColumns(TableInfo tableInfo, List resultColumnIndex, boolean includeAll)
  {
    for (Iterator it = tableInfo.getColumns().iterator(); it.hasNext();)
    {
      ColumnInfo colInfo = (ColumnInfo)it.next();
      for (int index = 0; index < resultData.getColumnNames().length; index++)
      {
        if (colInfo.getColumnName().equals(resultData.getColumnNames()[index]))
        {
          if (includeAll || isValidForWhere(resultData.getSqlType(index)))
          {
            resultColumnIndex.add(new Integer(index));
          }
        }
      }
    }
  }

  private void showNoColumnsMessage()
  {
    MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
    messageDlg.setText("No columns found");
    messageDlg.setMessage("No columns could be matched between the result set and the table");
    messageDlg.open();
  }

  private void showInsertRowInternal()
  {
    TableInfo tableInfo = resultData.getTable();
    List<Integer> resultColumnIndex = new LinkedList<Integer>();
    findAllColumns(tableInfo, resultColumnIndex, true);

    if (resultColumnIndex.size() == 0)
    {
      showNoColumnsMessage();
    }
    else
    {
      for (int row : customTable.getSelectionIndices())
      {
        InsertBuilder ib = new InsertBuilder(tableInfo);
        for (Iterator<Integer> it = resultColumnIndex.iterator(); it.hasNext(); )
        {
          int colIndex = it.next().intValue();
          String colName = resultData.getColumnNames()[colIndex];
          Object value = resultData.getCell(row, colIndex);

          ColumnInfo ci = tableInfo.getColumn(colName);
          if (ci != null)
          {
            ib.addColumnValue(ci, value);
          }
        }
        SessionData.getSessionData(sessionName).addQueryText(ib.buildStatement());
      }
    }
  }

  private void showDeleteRowInternal()
  {
    TableInfo tableInfo = resultData.getTable();

    List resultColumnIndex = new LinkedList();
    // find primary keys
    findPrimaryKeys(tableInfo, resultColumnIndex);

    // find unique index
    if (resultColumnIndex.size() == 0)
    {
      findUniqueIndex(tableInfo, resultColumnIndex);
    }

    // use all columns
    if (resultColumnIndex.size() == 0)
    {
      findAllColumns(tableInfo, resultColumnIndex, false);
    }


    if (resultColumnIndex.size() == 0)
    {
      showNoColumnsMessage();
    }
    else
    {
      String where = generateWhereClause(resultColumnIndex);

      DeleteDialog ud = new DeleteDialog(getShell(), tableInfo, SessionData.getSessionData(sessionName));
      ud.open();
      ud.setWhereClause(where);
    }
  }

  private String generateWhereClause(List resultColumnIndex)
  {
    //  get column values where names match from columnNames list
    // create where clause with column names and appropriate ='s with value
    // i.e. if object value is Timestamp -> columnName = {ts 'MM/DD/YYYY HH:MM:SS.fff'}
    StringBuffer sbuf = new StringBuffer();
    int row = customTable.getSelectionIndex();
    for (Iterator it = resultColumnIndex.iterator(); it.hasNext();)
    {
      int colIndex = ((Integer)it.next()).intValue();
      String colName = resultData.getColumnNames()[colIndex];
      Object value = resultData.getCell(row, colIndex);

      sbuf.append(colName);
      if (value == null)
        sbuf.append(" IS NULL ");
      else
      {
        sbuf.append(" = ");
        if (value instanceof Number)
          sbuf.append(value);
        else if (value instanceof Time)
          sbuf.append(DataTypeUtil.formatAsTime((java.util.Date)value));
        else if (value instanceof java.sql.Date)
          sbuf.append(DataTypeUtil.formatAsDate((java.util.Date)value));
        else if (value instanceof java.sql.Timestamp)
          sbuf.append(DataTypeUtil.formatAsTimestampWithNanos((java.sql.Timestamp)value));
        else if (DataTypeUtil.isCharacterType(resultData.getSqlType(colIndex)))
          sbuf.append("'").append(value).append("'");
        else
          sbuf.append(value);
      }

      if (it.hasNext())
        sbuf.append("\nAND ");
    }

    return sbuf.toString();
  }

  private void showUpdateRowInternal()
  {
    TableInfo tableInfo = resultData.getTable();

    List resultColumnIndex = new LinkedList();
    // find primary keys
    findPrimaryKeys(tableInfo, resultColumnIndex);

    // find unique index
    if (resultColumnIndex.size() == 0)
    {
      findUniqueIndex(tableInfo, resultColumnIndex);
    }

    // use all columns
    if (resultColumnIndex.size() == 0)
    {
      findAllColumns(tableInfo, resultColumnIndex, false);
    }


    if (resultColumnIndex.size() == 0)
    {
      showNoColumnsMessage();
    }
    else
    {
      String where = generateWhereClause(resultColumnIndex);

      UpdateDialog ud = new UpdateDialog(getShell(), tableInfo, SessionData.getSessionData(sessionName));
      ud.open();
      ud.setWhereClause(where);
    }
  }

  private void showUpdate()
  {
    if (resultData.getTable().getFullyLoaded())
    {
      UpdateDialog ud = new UpdateDialog(getShell(), resultData.getTable(), SessionData.getSessionData(sessionName));
      ud.open();
    }
    else
      getTableData(resultData.getTable(), UPDATE);
  }

  private void showInsert()
  {
    if (resultData.getTable().getFullyLoaded())
    {
      InsertDialog id = new InsertDialog(getShell(), resultData.getTable(), SessionData.getSessionData(sessionName));
      id.open();
    }
    else
      getTableData(resultData.getTable(), INSERT);
  }

  private void showDelete()
  {
    if (resultData.getTable().getFullyLoaded())
    {
      DeleteDialog dd = new DeleteDialog(getShell(), resultData.getTable(), SessionData.getSessionData(sessionName));
      dd.open();
    }
    else
      getTableData(resultData.getTable(), DELETE);
  }

  private void showUpdateRow()
  {
    if (resultData.getTable().getFullyLoaded())
      showUpdateRowInternal();
    else
      getTableData(resultData.getTable(), UPDATEROW);
  }

  private void showDeleteRow()
  {
    if (resultData.getTable().getFullyLoaded())
      showDeleteRowInternal();
    else
      getTableData(resultData.getTable(), DELETEROW);
  }

  private void showInsertRow()
  {
    if (resultData.getTable().getFullyLoaded())
      showInsertRowInternal();
    else
      getTableData(resultData.getTable(), INSERTROW);
  }

  private void getTableData(final TableInfo tableInfo, final int action)
  {
    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;

      @Override
      public void doTask()
      {
        try
        {
          getSchema(tableInfo);
          SessionData.getSessionData(sessionName).getDBObjectRetriever().getTableInfoSync(tableInfo);
        }
        catch (SQLException e)
        {
          logger.error(e.getMessage(), e);
          ex = e;
        }
      }

      @Override
      public void doUITask()
      {
        if (ex != null)
        {
          MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
          messageDlg.setText("Failed");
          messageDlg.setMessage(ex.getClass().getName() + "\n\n" + ex.getMessage());
          messageDlg.open();
        }
        else
        {
          if (action == UPDATEROW)
            showUpdateRowInternal();
          else if (action == DELETEROW)
            showDeleteRowInternal();
          else if (action == INSERTROW)
            showInsertRowInternal();
          else if (action == UPDATE)
            showUpdate();
          else if (action == DELETE)
            showDelete();
          else if (action == INSERT)
            showInsert();
        }

        ResultTableComposite.this.setEnabled(true);
        BusyManager.setNotBusy(ResultTableComposite.this.getParent());
      }
    };

    ResultTableComposite.this.setEnabled(false);
    BusyManager.setBusy(ResultTableComposite.this.getParent());
    worker.startTask();
  }

  private boolean isValidForWhere(int sqlType)
  {
    if (sqlType == Types.CLOB)
      return false;
    else if (sqlType == Types.BLOB)
      return false;
    else if (sqlType == Types.LONGVARBINARY)
      return false;
    else if (sqlType == Types.LONGVARCHAR)
      return false;

    return true;
  }

  private void getSchema(TableInfo tableInfo) throws SQLException
  {
    ConnectionManager conManager = SessionData.getSessionData(sessionName).getConnectionManager();
    if (tableInfo.getSchema() == null && conManager.getUseSchemas())
    {
      ResultSet tableRs = null;
      try
      {
        Connection con = conManager.getConnection();

        boolean quoted = tableInfo.getName().startsWith(conManager.getIdentifierQuote());
        if (quoted && conManager.getStoresLowerCaseQuotedIdentifiers())
          tableInfo.setName(tableInfo.getName().toLowerCase());
        else if (quoted && conManager.getStoresUpperCaseQuotedIdentifiers())
          tableInfo.setName(tableInfo.getName().toUpperCase());
        else if (!quoted && conManager.getStoresLowerCaseIdentifiers())
          tableInfo.setName(tableInfo.getName().toLowerCase());
        else if (!quoted && conManager.getStoresUpperCaseIdentifiers())
          tableInfo.setName(tableInfo.getName().toUpperCase());



        tableRs = con.getMetaData().getTables(tableInfo.getCatalog(), null, tableInfo.getName(), null);
        String schema = null;
        String type = null;
        while (tableRs.next())
        {
          if (schema == null || type.equalsIgnoreCase("synonym"))
          { // if last type was synonym ignore it and use this one instead
            schema = tableRs.getString("TABLE_SCHEM");
            type = tableRs.getString("TABLE_TYPE");
          }
          else
          {
            break;
          }
        }
        tableInfo.setSchema(schema);
      }
      catch (Exception e)
      {
        logger.error(e.getMessage(), e);
      }
      finally
      {
        if (tableRs != null)
          tableRs.close();

        conManager.releaseConnection();
      }
    }

    logger.debug("After getting Schema TableInfo: " + tableInfo);
  }


}
