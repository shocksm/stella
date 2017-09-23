package org.stellasql.stella;

import java.sql.DatabaseMetaData;

public class IndexInfo
{
  private boolean nonUnique;
  private String qualifier;
  private String name;
  private String type;
  private int ordinalPosition;
  private String columnName;
  private String ascOrDesc;
  private int cardinality;
  private int pages;
  private String filterCondition;
  private Integer orderIndex;

  public String getAscOrDesc()
  {
    return ascOrDesc;
  }
  public void setAscOrDesc(String ascOrDesc)
  {
    this.ascOrDesc = ascOrDesc;
  }
  public int getCardinality()
  {
    return cardinality;
  }
  public void setCardinality(int cardinality)
  {
    this.cardinality = cardinality;
  }
  public String getColumnName()
  {
    return columnName;
  }
  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }
  public String getFilterCondition()
  {
    return filterCondition;
  }
  public void setFilterCondition(String filterCondition)
  {
    this.filterCondition = filterCondition;
  }
  public String getName()
  {
    return name;
  }
  public void setName(String name)
  {
    this.name = name;
  }
  public boolean getNonUnique()
  {
    return nonUnique;
  }
  public void setNonUnique(boolean nonUnique)
  {
    this.nonUnique = nonUnique;
  }
  public int getOrdinalPosition()
  {
    return ordinalPosition;
  }
  public void setOrdinalPosition(int ordinalPosition)
  {
    this.ordinalPosition = ordinalPosition;
  }
  public int getPages()
  {
    return pages;
  }
  public void setPages(int pages)
  {
    this.pages = pages;
  }
  public String getQualifier()
  {
    return qualifier;
  }
  public void setQualifier(String qualifier)
  {
    this.qualifier = qualifier;
  }
  public void setType(String type)
  {
    this.type = type;
  }
  public String getType()
  {
    return type;
  }
  public void setType(int type)
  {
    if (type == DatabaseMetaData.tableIndexStatistic)
      this.type = "Stastic";
    else if (type == DatabaseMetaData.tableIndexClustered)
      this.type = "Clustered";
    else if (type == DatabaseMetaData.tableIndexHashed)
      this.type = "Hashed";
    else
      this.type = "Other";
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
