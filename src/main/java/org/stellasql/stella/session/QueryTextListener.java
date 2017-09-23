package org.stellasql.stella.session;


public interface QueryTextListener
{
  public void insertQueryText(String text);
  public void addQueryText(String text);

}
