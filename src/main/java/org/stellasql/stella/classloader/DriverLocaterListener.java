package org.stellasql.stella.classloader;

public interface DriverLocaterListener
{
  public void filesToProcess(int count);
  public void filesProcessed(int count);
}
