package org.stellasql.stella.classloader;

import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class URLEnumeration implements Enumeration
{
  private List urlList;
  private int loc = 0;

  public URLEnumeration(List urlList)
  {
    this.urlList = urlList;
  }

  @Override
  public boolean hasMoreElements()
  {
    if (loc < urlList.size())
      return true;
    else
      return false;
  }

  @Override
  public Object nextElement()
  {
    if (!hasMoreElements())
    {
      throw new NoSuchElementException();
    }

    return urlList.get(loc++);
  }

}