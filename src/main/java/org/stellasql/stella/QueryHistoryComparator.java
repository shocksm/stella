package org.stellasql.stella;

import java.util.Comparator;

public class QueryHistoryComparator implements Comparator
{
  public final static int ASCENDING = 1;
  public final static int DESCENDING = 2;
  private int sortDirection = 0;
  public QueryHistoryComparator(int sortDirection)
  {
    this.sortDirection = sortDirection;
  }

  @Override
  public int compare(Object arg1, Object arg2)
  {
    QueryHistoryItem qhi1 = (QueryHistoryItem)arg1;
    QueryHistoryItem qhi2 = (QueryHistoryItem)arg2;

    if (sortDirection == ASCENDING)
    {
      if (qhi1.getTime() < qhi2.getTime())
        return -1;
      else if (qhi1.getTime() == qhi2.getTime())
        return 0;
      else
        return 1;
    }
    else
    {
      if (qhi1.getTime() > qhi2.getTime())
        return -1;
      else if (qhi1.getTime() == qhi2.getTime())
        return 0;
      else
        return 1;
    }
  }
}