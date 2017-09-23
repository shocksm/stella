package org.stellasql.stella;

import java.util.Comparator;

public class QueryFavoriteObject implements Comparable
{
  private static OrderComparator orderComparator = new OrderComparator();
  protected QueryFavoriteFolder parent;
  protected String name;
  protected int id;
  protected int order;

  public QueryFavoriteFolder getParent()
  {
    return parent;
  }

  protected void setParent(QueryFavoriteFolder parent)
  {
    this.parent = parent;
  }

  public int getOrder()
  {
    return order;
  }

  public void setOrder(int order)
  {
    this.order = order;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  @Override
  public int compareTo(Object arg)
  {
    QueryFavoriteObject other = (QueryFavoriteObject)arg;

    if (this instanceof QueryFavoriteFolder
        && other instanceof QueryFavoriteItem2)
    {
      return -1;
    }
    else if (this instanceof QueryFavoriteItem2
        && other instanceof QueryFavoriteFolder)
    {
      return 1;
    }
    else if (this instanceof QueryFavoriteFolder
        && other instanceof QueryFavoriteFolder)
    {
      int value = getName().compareToIgnoreCase(other.getName());
      if (value == 0 && getId() < other.getId())
        value = -1;
      else if (value == 0 && getId() > other.getId())
        value = 1;
      return value;
    }
    else if (this instanceof QueryFavoriteItem2
        && other instanceof QueryFavoriteItem2)
    {
      int value = getName().compareToIgnoreCase(other.getName());
      if (value == 0)
        value = ((QueryFavoriteItem2)this).getQuery().compareToIgnoreCase(((QueryFavoriteItem2)other).getQuery());
      if (value == 0 && getId() < other.getId())
        value = -1;
      else if (value == 0 && getId() > other.getId())
        value = 1;
      return value;
    }

    return 0;
  }

  public static QueryFavoriteObject copy(QueryFavoriteObject qfo)
  {
    return copy(qfo, false);
  }

  public static QueryFavoriteObject copy(QueryFavoriteObject qfo, boolean deepCopy)
  {
    QueryFavoriteObject qfoNew = null;

    if (qfo instanceof QueryFavoriteItem2)
      qfoNew = new QueryFavoriteItem2((QueryFavoriteItem2)qfo);
    else if (qfo instanceof QueryFavoriteFolder)
    {
      qfoNew = new QueryFavoriteFolder((QueryFavoriteFolder)qfo);
      if (deepCopy)
        ((QueryFavoriteFolder)qfoNew).copyChildren((QueryFavoriteFolder)qfo);
    }

    return qfoNew;
  }

  public static OrderComparator getOrderComparator()
  {
    return orderComparator;
  }

  public static class OrderComparator implements Comparator
  {
    @Override
    public int compare(Object arg1, Object arg2)
    {
      QueryFavoriteObject qfo1 = (QueryFavoriteObject)arg1;
      QueryFavoriteObject qfo2 = (QueryFavoriteObject)arg2;

      return qfo1.getOrder() - qfo2.getOrder();
    }
  }

}
