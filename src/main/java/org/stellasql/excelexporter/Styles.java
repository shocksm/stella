package org.stellasql.excelexporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Styles
{
  private Style defaultStyle = null;
  private List styles = new LinkedList();
  private Map columnStyles = new HashMap();
  private Map formatStyles = new HashMap();
  private int styleCount = 1;

  public Styles()
  {
    defaultStyle = new Style("Default");
    defaultStyle.setName("Normal");
    addStyle(defaultStyle);
  }

  public Style getDefaultStyle()
  {
    return defaultStyle;
  }

  public void addColumnStyle(int column, String format)
  {
    Style style = (Style)formatStyles.get(format);
    if (style == null)
    {
      style = new Style("s" + styleCount++);
      style.setNumberFormat(format);
      styles.add(style);
      formatStyles.put(format, style);
    }

    columnStyles.put(new Integer(column), style);
  }

  public void addStyle(Style style)
  {
    styles.add(style);
  }

  public Style getStyle(int column)
  {
    Integer key = new Integer(column);
    Style style = (Style)columnStyles.get(key);
    if (style == null)
      style = defaultStyle;

    return style;
  }

  public void write(CustomWriter writer) throws IOException
  {
    writer.iwriteln("<Styles>");
    writer.indent();
    for (Iterator it = styles.iterator(); it.hasNext();)
    {
      Style style = (Style)it.next();
      style.write(writer);
    }

    writer.unindent();
    writer.iwriteln("</Styles>");
  }

}
