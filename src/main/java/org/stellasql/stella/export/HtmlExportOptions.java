package org.stellasql.stella.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class HtmlExportOptions extends ExportOptions
{
  private String lineBreak = "\n";
  private boolean tableStarted = false;
  private Writer writer = null;

  @Override
  public File open(int resultSetCount) throws IOException
  {
    File file = getFile(resultSetCount);
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    return file;
  }

  @Override
  public void close() throws IOException
  {
    if (writer != null)
      writer.close();
    writer = null;
  }

  @Override
  public void begin() throws IOException
  {
    tableStarted = false;

    writer.write("<HTML>");
    writer.write(lineBreak);
    writer.write("  <BODY>");
    writer.write(lineBreak);
  }

  @Override
  public void end() throws IOException
  {
    if (tableStarted)
    {
      writer.write("    </TABLE>");
      writer.write(lineBreak);
    }
    writer.write("  </BODY>");
    writer.write(lineBreak);
    writer.write("</HTML>");
    writer.write(lineBreak);
  }

  private void startTable() throws IOException
  {
    if (tableStarted)
      return;

    writer.write("    <TABLE BORDER=\"1\">");
    writer.write(lineBreak);

    tableStarted = true;
  }

  @Override
  public void writeSql(String sql) throws IOException
  {

    writer.write("    <TABLE>");
    writer.write(lineBreak);
    writer.write("      <TR>");
    writer.write(lineBreak);
    writer.write("        <TD>");
    writer.write(escapeValue(sql));
    writer.write("</TD>");
    writer.write(lineBreak);
    writer.write("      </TR>");
    writer.write(lineBreak);

    writer.write("    </TABLE>");
    writer.write(lineBreak);
  }

  private String escapeValue(String value)
  {
    if (value == null)
      return "";
    if (value.length() == 0)
      return value;

    value = value.replaceAll("&", "&amp;");
    value = value.replaceAll("<", "&lt;");
    value = value.replaceAll(">", "&gt;");
    value = value.replaceAll("\n", "</BR>");

    return value;
  }


  @Override
  public void writeColumnNames(String[] columnNames) throws IOException
  {
    startTable();
    writer.write("      <TR BGCOLOR=\"#CCCCCC\">");
    writer.write(lineBreak);
    for (int index = 0; index < columnNames.length; index++)
    {
      writer.write("        <TD>");
      writer.write(escapeValue(columnNames[index]));
      writer.write("</TD>");
      writer.write(lineBreak);
    }
    writer.write("      </TR>");
    writer.write(lineBreak);
  }

  @Override
  public void writeRow(Object[] row) throws IOException
  {
    startTable();
    writer.write("      <TR>");
    writer.write(lineBreak);

    for (int index = 0; index < row.length; index++)
    {
      writer.write("        <TD>");
      writer.write(escapeValue(getStringValue(row[index])));
      writer.write("</TD>");
      writer.write(lineBreak);
    }
    writer.write("      </TR>");
    writer.write(lineBreak);
  }

}
