package org.stellasql.stella;

public class PrimaryKeyInfo
{
  private String columnName = "";
  private int keySeq;
  private String pkName = "";
  private Integer orderIndex;

  public String getColumnName()
  {
    return columnName;
  }
  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }
  public int getKeySeq()
  {
    return keySeq;
  }
  public void setKeySeq(int keySeq)
  {
    this.keySeq = keySeq;
  }
  public String getPkName()
  {
    return pkName;
  }
  public void setPkName(String pkName)
  {
    this.pkName = pkName;
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
