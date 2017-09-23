package org.stellasql.stella;

public class ProcedureColumnInfo
{
  private String columnName;
  private int dataType;
  private int length;
  private boolean nullable;
  private String typeName;
  private String defaultVal;
  private Integer orderIndex;
  private String columnType;
  private String remarks;

  public String getColumnType()
  {
    return columnType;
  }

  public void setColumnType(String columnType)
  {
    this.columnType = columnType;
  }

  public int getLength()
  {
    return length;
  }

  public void setLength(int columnSize)
  {
    length = columnSize;
  }

  public int getDataType()
  {
    return dataType;
  }

  public void setDataType(int dataType)
  {
    this.dataType = dataType;
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

  public String getRemarks()
  {
    return remarks;
  }

  public void setRemarks(String remarks)
  {
    this.remarks = remarks;
  }

}
