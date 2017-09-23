package org.stellasql.excelexporter;

public class StringCell extends Cell
{
  public StringCell(Style style, String value)
  {
    super(style);
    dataType = "String";
    if (value == null)
      value = "";
    this.value = value;
  }
}