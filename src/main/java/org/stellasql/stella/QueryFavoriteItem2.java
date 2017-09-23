package org.stellasql.stella;

public class QueryFavoriteItem2 extends QueryFavoriteObject  implements Comparable
{
  private String query;

  public QueryFavoriteItem2(String description, String query)
  {
    name = description;
    this.query = query;
  }

  public QueryFavoriteItem2(QueryFavoriteItem2 qfiOrig)
  {
    name = qfiOrig.getName();
    query = qfiOrig.getQuery();
  }

  public String getQuery()
  {
    return query;
  }

  public void setQuery(String query)
  {
    this.query = query;
  }

}

