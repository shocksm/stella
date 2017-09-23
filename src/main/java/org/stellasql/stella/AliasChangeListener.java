package org.stellasql.stella;

public interface AliasChangeListener
{
  public void connectionChanged(String oldName, String newName);
  public void connectionAdded(String name);
  public void connectionRemoved(String name);

}
