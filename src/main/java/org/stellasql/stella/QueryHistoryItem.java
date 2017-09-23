package org.stellasql.stella;

public class QueryHistoryItem
{
  private String aliasName;
  private long time = 0;
  private String query;

  public QueryHistoryItem(String aliasName, long time, String query)
  {
    this.aliasName = aliasName;
    this.time = time;
    this.query = query;
  }

  public void setAliasName(String aliasName)
  {
    this.aliasName = aliasName;
  }

  public String getAliasName()
  {
    return aliasName;
  }

  public String getQuery()
  {
    return query;
  }

  public long getTime()
  {
    return time;
  }

  @Override
  public boolean equals(Object obj)
  {
    QueryHistoryItem other = (QueryHistoryItem)obj;
    if (this.getQuery().equalsIgnoreCase(other.getQuery()))
      return true;
    else
      return false;
  }

  @Override
  public int hashCode()
  {
    return this.getQuery().toLowerCase().hashCode();
  }

}

