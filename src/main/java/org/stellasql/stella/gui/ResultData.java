package org.stellasql.stella.gui;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.connection.ConnectionManager;
import org.stellasql.stella.gui.custom.TableData;

public class ResultData implements TableData
{
  private List objArrayList = new ArrayList();
  private String[] nameArray = null;
  private String query = "";
  private ResultComparator resultComparator = null;
  private TableInfo tableInfo = null;
  private DateFormat dfDateTime = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS a");
  private DateFormat dfTime = new SimpleDateFormat("hh:mm:ss.SSS a");
  private int[] sqlTypeArray = null;

  public ResultData()
  {
    String[] array = {"Column 1", "Column 2", "Column 3", "Column 4", "Column 5", "Column 6", "Column 7"};
    nameArray = array;
    query = "this is a test";
    for (int count = 0; count < 1000; count++)
    {
      array = new String[nameArray.length];
      for (int index = 0; index < nameArray.length; index++)
        array[index] = "really really really really reallylong string in a cell " + count + ", " + index;

      objArrayList.add(array);
    }
  }

  public ResultData(ResultSetMetaData rsmd, String query) throws SQLException
  {
    this.query = query;
    int count = rsmd.getColumnCount();
    nameArray = new String[count];
    sqlTypeArray  = new int[count];
    for (int index = 0; index < count; index++)
    {
      nameArray[index] = rsmd.getColumnName(index + 1);
      sqlTypeArray[index] = rsmd.getColumnType(index + 1);
    }
  }

  public int getSqlType(int column)
  {
    return sqlTypeArray[column];
  }

  public TableInfo getTable()
  {
    return tableInfo;
  }

  public void setTable(String table, String catalog, String schema, ConnectionManager conManager)
  {
    tableInfo = new TableInfo(table, catalog, schema, null, conManager);
  }

  public String getQuery()
  {
    return query;
  }

  public void addRow(Object[] objArray)
  {
    objArrayList.add(objArray);
  }

  @Override
  public String[] getColumnNames()
  {
    return nameArray;
  }

  @Override
  public String getColumnToolTip(int columnIndex)
  {
    return null;
  }

  @Override
  public Object getCell(int row, int column)
  {
    if (row >= objArrayList.size()) {
      return null;
    }

    Object[] objArray = (Object[])objArrayList.get(row);
    return objArray[column];
  }

  private String getStringValue(Object obj)
  {
    String value = null;
    if (obj == null)
      return null;
    else if (obj instanceof Clob)
      value = "CLOB";
    else if (obj instanceof Blob)
      value = "BLOB";
    else if (obj instanceof Timestamp)
      value = dfDateTime.format(obj);
    else if (obj instanceof Date)
      value = dfDateTime.format(obj);
    else if (obj instanceof Time)
      value = dfTime.format(obj);
    else
    {
      value = obj.toString();
    }

    return value;
  }

  @Override
  public String getNullString()
  {
    return "(null)";
  }

  @Override
  public String getCellAsString(int row, int column)
  {
    Object obj = getCell(row, column);
    return getStringValue(obj);
  }

  @Override
  public int getColumnCount()
  {
    return nameArray.length;
  }

  @Override
  public int getRowCount()
  {
    return objArrayList.size();
  }

  @Override
  public Object getRowObject(int row)
  {
    return objArrayList.get(row);
  }

  @Override
  public int getIndexOfRowObject(Object obj)
  {
    return objArrayList.indexOf(obj);
  }

  @Override
  public void sort(int columnIndex, int dir)
  {
    if (resultComparator == null)
      resultComparator = new ResultComparator();

    resultComparator.setSortColumn(columnIndex);
    resultComparator.setSortDirection(dir);

    Collections.sort(objArrayList, resultComparator);
  }

  private class ResultComparator implements Comparator
  {
    private int columnIndex = -1;
    private int direction = -1;

    public void setSortDirection(int dir)
    {
      direction = dir;
    }

    public void setSortColumn(int columnIndex)
    {
      this.columnIndex = columnIndex;
    }

    @Override
    public int compare(Object arg1, Object arg2)
    {
      Object obj1 = ((Object[])arg1)[columnIndex];
      Object obj2 = ((Object[])arg2)[columnIndex];

      int val = 0;

      if (obj1 == null && obj2 != null)
        val = 1;
      else if (obj1 != null && obj2 == null)
        val = -1;
      else if (obj1 == null && obj2 == null)
        val = 0;
      else if (obj1 instanceof Number)
        val = Double.compare(((Number)obj1).doubleValue(), ((Number)obj2).doubleValue());
      else if (obj1 instanceof java.util.Date)
        val = ((java.util.Date)obj1).compareTo((java.util.Date)obj2);
      else
        val = getStringValue(obj1).compareToIgnoreCase(getStringValue(obj2));

      if (direction == SWT.DOWN)
        val = -val;

      return val;
    }
  }

}
