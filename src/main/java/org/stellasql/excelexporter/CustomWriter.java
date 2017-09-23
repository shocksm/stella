package org.stellasql.excelexporter;

import java.io.BufferedWriter;
import java.io.IOException;

public class CustomWriter
{
  private int indent = 0;
  BufferedWriter writer = null;

  public CustomWriter(BufferedWriter bw)
  {
    writer = bw;
  }

  public CustomWriter iwrite(String value) throws IOException
  {
    for (int count = 0; count < indent; count++)
      write(" ");
    write(value);
    return this;
  }

  public CustomWriter iwriteln(String value) throws IOException
  {
    iwrite(value);
    write("\n");
    return this;
  }

  public CustomWriter write(String value) throws IOException
  {
    writer.write(value);
    return this;
  }

  public CustomWriter writeln(String value) throws IOException
  {
    write(value);
    write("\n");
    return this;
  }

  public CustomWriter writeString(String value) throws IOException
  {
    write("\"");
    write(escapeXML(value));
    write("\"");
    return this;
  }

  public CustomWriter writeText(String value) throws IOException
  {
    write(escapeXML(value));
    return this;
  }

  public void indent()
  {
    indent++;
  }

  public void unindent()
  {
    indent--;
    if (indent < 0)
      indent = 0;
  }

  public void close() throws IOException
  {
    writer.close();
  }

  public static String escapeXML(String value)
  {
    StringBuffer sbuf = new StringBuffer();

    if (value != null && value.length() > 0)
    {
      for(int index = 0; index < value.length(); index++)
      {
        char c = value.charAt(index);
        if (c == '"')
          sbuf.append("&quot;");
        else if (c == '\'')
          sbuf.append("&apos;");
        else if (c == '&')
          sbuf.append("&amp;");
        else if (c == '<')
          sbuf.append("&lt;");
        else if (c == '>')
          sbuf.append("&gt;");
        else
          sbuf.append(c);
      }
    }

    return sbuf.toString();
  }

}
