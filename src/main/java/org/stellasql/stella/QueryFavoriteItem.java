package org.stellasql.stella;

public class QueryFavoriteItem
{
  private String aliasName;
  private String description;
  private String query;

  public QueryFavoriteItem(String aliasName, String description, String query)
  {
    this.aliasName = aliasName;
    if (this.aliasName == null)
      this.aliasName = "";
    this.description = description;
    this.query = query;
  }

  public String getAliasName()
  {
    return aliasName;
  }

  public void setAliasName(String alisName)
  {
    aliasName = alisName;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getQuery()
  {
    return query;
  }

  public void setQuery(String query)
  {
    this.query = query;
  }

  @Override
  public boolean equals(Object obj)
  {
    QueryFavoriteItem other = (QueryFavoriteItem)obj;
    if (this.getAliasName().equalsIgnoreCase(other.getAliasName())
        && this.getDescription().equalsIgnoreCase(other.getDescription()))
      return true;
    else
      return false;
  }

  @Override
  public int hashCode()
  {
    return this.getAliasName().toLowerCase().hashCode() + this.getDescription().toLowerCase().hashCode();
  }

}

