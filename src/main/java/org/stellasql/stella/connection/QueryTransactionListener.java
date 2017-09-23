package org.stellasql.stella.connection;


public interface QueryTransactionListener
{
  public void querySuccess();
  public void queryCommit();
  public void queryRollback();
}
