package org.stellasql.stella.query;

public class Column
{
  public Table table;
  public String tablename;
  public String column;
  public String alias;

  @Override
  public String toString()
  {
    return tablename + "." + column + " " + alias + " table: " + table;
  }

}
