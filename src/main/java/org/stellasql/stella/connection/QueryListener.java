package org.stellasql.stella.connection;

import java.util.List;

public interface QueryListener
{
  public void queryRan(String query, boolean valid, Object data, Object key, List resultsList, int limitedRows, long queryMillis, long formatMillis);
  public void queryBeingExecuted(String query, Object data, Object key);
  public void queryCanceled(String query, Object data, Object key);
}
