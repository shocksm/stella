package org.stellasql.excelexporter;

import java.io.IOException;

public class Style
{
  private String id = null;
  private String name = null;
  private String numberFormat = null;

  public Style(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getNumberFormat()
  {
    return numberFormat;
  }

  public void setNumberFormat(String numberFormat)
  {
    this.numberFormat = numberFormat;
  }

  public void write(CustomWriter writer) throws IOException
  {
    writer.iwrite("<Style ss:ID=").writeString(id);
    if (name != null)
      writer.write(" ss:Name=").writeString(name);
    writer.writeln(">");
    if (numberFormat != null)
    {
      writer.indent();
      writer.iwrite("<NumberFormat ss:Format=").writeString(numberFormat).writeln("/>");
      writer.unindent();
    }
    writer.iwriteln("</Style>");
  }

}
