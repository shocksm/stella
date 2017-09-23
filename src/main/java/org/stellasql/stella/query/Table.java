package org.stellasql.stella.query;

public class Table
{
  public String catalog;
  public String schema;
  public String table;
  public String alias;

  @Override
  public String toString()
  {
    return catalog + "." + schema + "." + table + " " + alias;
  }

}
