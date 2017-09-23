package org.stellasql.excelexporter;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Row
{
  private List cells = new LinkedList();

  public void addCell(Cell cell)
  {
    cells.add(cell);
  }

  public void write(CustomWriter writer) throws IOException
  {
    writer.indent();
    writer.iwriteln("<Row>");
    writer.indent();
    for (Iterator it = cells.iterator(); it.hasNext();)
    {
      Cell cell = (Cell)it.next();
      cell.write(writer);
    }

    writer.unindent();
    writer.iwriteln("</Row>");
    writer.unindent();
  }

}