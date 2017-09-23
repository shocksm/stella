package org.stellasql.stella;

public class ColumnInfo
{
  private String columnName;
  private int dataType;
  private int columnSize;
  private int decimalDigits;
  private boolean nullable;
  private String typeName;
  private String defaultVal;
  private Integer orderIndex;

  public int getColumnSize()
  {
    return columnSize;
  }

  public void setColumnSize(int columnSize)
  {
    this.columnSize = columnSize;
  }

  public int getDataType()
  {
    return dataType;
  }

  public void setDataType(int dataType)
  {
    this.dataType = dataType;
  }

  public int getDecimalDigits()
  {
    return decimalDigits;
  }

  public void setDecimalDigits(int decimalDigits)
  {
    this.decimalDigits = decimalDigits;
  }

  public boolean getNullable()
  {
    return nullable;
  }

  public void setNullable(boolean nullable)
  {
    this.nullable = nullable;
  }

  public String getColumnName()
  {
    return columnName;
  }

  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }

  public void setTypeName(String type)
  {
    typeName = type;
  }
  public String getTypeName()
  {
    return typeName;
  }

  public void setDefault(String value)
  {
    defaultVal = value;
  }

  public String getDefault()
  {
    return defaultVal;
  }

  public void setOrderIndex(int index)
  {
    orderIndex = new Integer(index);
  }

  public Integer getOrderIndex()
  {
    return orderIndex;
  }

}
