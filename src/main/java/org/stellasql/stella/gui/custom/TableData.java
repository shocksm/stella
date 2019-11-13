package org.stellasql.stella.gui.custom;



public interface TableData<T>
{
  public Object getCell(int row, int column);

  public String getCellAsString(int row, int column);

  public String getNullString();

  public String[] getColumnNames();

  public int getColumnCount();

  public int getRowCount();

  public void sort(int columnIndex, int dir);

  public String getColumnToolTip(int index);

  public T getRowObject(int row);

  public int getIndexOfRowObject(T obj);
}
