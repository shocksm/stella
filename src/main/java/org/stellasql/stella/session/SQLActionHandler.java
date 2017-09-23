package org.stellasql.stella.session;

public interface SQLActionHandler
{
  public void addFavorite();
  public void execute();
  public void executeExport();
  public void commit();
  public void rollback();
  public boolean isAutoCommit();
}
