package org.stellasql.stella.gui.custom;



public interface TableData
{
  public Object getCell(int row, int column);

  public String getCellAsString(int row, int column);

  public String getNullString();

  public String[] getColumnNames();

  public int getColumnCount();

  public int getRowCount();

  public void sort(int columnIndex, int dir);

  public String getColumnToolTip(int index);

  public Object getRowObject(int row);

  public int getIndexOfRowObject(Object obj);
}
