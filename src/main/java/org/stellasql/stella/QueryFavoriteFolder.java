package org.stellasql.stella;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class QueryFavoriteFolder extends QueryFavoriteObject implements Comparable
{
  private List children = new LinkedList();
  private static int NEXTID = 1;

  public QueryFavoriteFolder(int id, String name)
  {
    this.id = id;
    if (this.id >= NEXTID)
      NEXTID = this.id + 1;
    this.name = name;
  }

  public QueryFavoriteFolder(QueryFavoriteFolder qffOrig)
  {
    id = getNextId();
    name = qffOrig.getName();
  }

  public static int getNextId()
  {
    return NEXTID++;
  }

  public static void setNextId(int id)
  {
    NEXTID = id;
  }

  public List getChildren()
  {
    return children;
  }

  public void addChild(QueryFavoriteObject child, int index)
  {
    if (child.getParent() != null)
      child.getParent().removeChild(child);
    if (index < 0)
      children.add(child);
    else
      children.add(index, child);
    child.setParent(this);
  }

  public void addChild(QueryFavoriteObject child)
  {
    addChild(child, -1);
  }

  public void removeChild(QueryFavoriteObject child)
  {
    children.remove(child);
    child.setParent(null);
  }

  public void copyChildren(QueryFavoriteFolder qffOrig)
  {
    for (Iterator it = qffOrig.getChildren().iterator(); it.hasNext();)
    {
      QueryFavoriteObject qfo = (QueryFavoriteObject)it.next();
      QueryFavoriteObject qfoNew = QueryFavoriteObject.copy(qfo);
      this.addChild(qfoNew);

      if (qfo instanceof QueryFavoriteFolder)
        ((QueryFavoriteFolder)qfoNew).copyChildren((QueryFavoriteFolder)qfo);
    }
  }

}
