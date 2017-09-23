package org.stellasql.stella;

public interface DriverChangeListener
{
  public void driverChanged(String oldName, String newName);
  public void driverRemoved(String newName);
  public void driverAdded(String newName);
}
