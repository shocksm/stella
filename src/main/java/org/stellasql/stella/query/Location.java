package org.stellasql.stella.query;

public class Location implements Comparable
{
  public int start = -1;
  public int end = -1;
  public String type = null;

  public Location(int start)
  {
    this.start = start;
  }

  public Location(int start, int end)
  {
    this.start = start;
    this.end = end;
  }

  @Override
  public int compareTo(Object obj)
  {
    Location other = (Location)obj;

    if (other.start < start)
    {
      return 1;
    }
    else if (other.start > start)
    {
      return -1;
    }
    else
    {
      return 0;
    }
  }

  @Override
  public String toString()
  {
    return "(" + start + ", " + end + ")";
  }
}
