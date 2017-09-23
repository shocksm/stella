package org.stellasql.excelexporter;

import java.io.IOException;

public abstract class Cell
{
  private Style style = null;
  protected String value = null;
  protected String dataType = null;
  protected int mergeAcross = 1;

  public Cell(Style style)
  {
    this.style = style;
  }

  public void setMergeAcross(int value)
  {
    mergeAcross = value;
  }

  public void write(CustomWriter writer) throws IOException
  {
    writer.iwrite("<Cell ");
    if (mergeAcross > 1)
      writer.write("ss:MergeAcross=").writeString("" + mergeAcross).write(" ");
    writer.write("ss:StyleID=").writeString(style.getId()).write(">");
    if (value != null && value.length() > 0)
    {
      writer.write("<Data ss:Type=").writeString(dataType).write(">");
      writer.writeText(value);
      writer.write("</Data>");
    }
    writer.writeln("</Cell>");
  }
}
