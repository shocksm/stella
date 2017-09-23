package org.stellasql.stella.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TextExportOptions extends ExportOptions
{
  private String delimiter = null;
  private String lineBreak = "\n";
  private Writer writer = null;

  public TextExportOptions(String delimiter)
  {
    this.delimiter = delimiter;
  }

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

  private String escapeValue(String value)
  {
    if (value == null)
      return "";
    if (value.length() == 0)
      return value;

    boolean quote = false;
    if (value.indexOf("\"") >= 0)
    {
      value = value.replaceAll("\"", "\"\"");
      quote = true;
    }
    else if (value.indexOf(delimiter) >= 0)
      quote = true;
    else if (value.indexOf("\n") >= 0)
      quote = true;
    else if (Character.isWhitespace(value.charAt(0)))
      quote = true;
    else if (Character.isWhitespace(value.charAt(value.length() - 1)))
      quote = true;

    if (quote)
      value = "\"" + value + "\"";

    return value;
  }

  @Override
  public void begin() throws IOException
  {
  }

  @Override
  public void end() throws IOException
  {
  }

  @Override
  public void writeColumnNames(String[] columnNames) throws IOException
  {
    for (int index = 0; index < columnNames.length; index++)
    {
      writer.write(escapeValue(columnNames[index]));
      if (index < columnNames.length - 1)
        writer.write(delimiter);
    }
    writer.write(lineBreak);
  }

  @Override
  public void writeRow(Object[] row) throws IOException
  {
    for (int index = 0; index < row.length; index++)
    {
      writer.write(escapeValue(getStringValue(row[index])));
      if (index < row.length - 1)
        writer.write(delimiter);
    }
    writer.write(lineBreak);
  }

  @Override
  public void writeSql(String sql) throws IOException
  {
    writer.write(sql);
    writer.write(lineBreak);
  }

}
