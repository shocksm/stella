package org.stellasql.excelexporter;

public class NumberCell extends Cell
{
  public NumberCell(Style style, Number number)
  {
    super(style);
    dataType = "Number";
    value = "";
    if (number != null)
    {
      value = number.toString();
    }
  }
}
