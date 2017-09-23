package org.stellasql.excelexporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExcelXMLExport
{
  private CustomWriter writer = null;
  private Styles styles = null;
  private File file = null;
  private Map columnType = new HashMap();
  private int sheet = 1;
  private int rowCount = 0;
  private int rowSplit = 0;
  private boolean splitWorksheet = false;

  public final static String STRING_TYPE = "STRING";
  public final static String NUMBER_TYPE = "NUMBER";
  public final static String DATETIME_TYPE = "DATETIME";

  public ExcelXMLExport(File file)
  {
    styles = new Styles();
    this.file = file;
  }

  public ExcelXMLExport(File file, int rowsplit)
  {
    this(file);
    splitWorksheet = true;
    rowSplit = rowsplit;
  }

  public void setColumnType(int column, String type)
  {
    columnType.put(new Integer(column), type);
  }

  public void setColumnType(int column, String type, String format)
  {
    columnType.put(new Integer(column), type);
    styles.addColumnStyle(column, format);
  }

  public void writeHeader() throws IOException
  {
    writer = new CustomWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
    writer.iwriteln("<?xml version=\"1.0\"?>");
    writer.iwriteln("<?mso-application progid=\"Excel.Sheet\"?>");
    writer.iwriteln("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
    writer.indent();
    writer.iwriteln("xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
    writer.iwriteln("xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
    writer.iwriteln("xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"");
    writer.iwriteln("xmlns:html=\"http://www.w3.org/TR/REC-html40\">");

    styles.write(writer);

    writer.iwriteln("<Worksheet ss:Name=\"Sheet1\">");
    writer.indent();

    writer.iwriteln("<Table>");
  }

  public void close() throws IOException
  {
    writer.iwriteln("</Table>");

    writer.unindent();
    writer.iwriteln("</Worksheet>");

    writer.unindent();
    writer.iwriteln("</Workbook>");
    writer.close();
  }

  public void writeRow(Object[] rowValues) throws IOException
  {
    rowCount++;
    if (splitWorksheet && rowCount > rowSplit)
    {
      rowCount = 1;
      sheet++;
      writer.iwriteln("</Table>");
      writer.unindent();
      writer.iwriteln("</Worksheet>");
      writer.iwriteln("<Worksheet ss:Name=\"Sheet" + sheet + "\">");
      writer.indent();
      writer.iwriteln("<Table>");
    }

    Row row = new Row();
    for (int count = 0; count < rowValues.length; count++)
    {
      if ((count + 1) % 65536 == 0)
      {


      }
      Cell cell = null;
      String type = (String)columnType.get(new Integer(count));
      if (type == STRING_TYPE)
      {
        String value = "";
        if (rowValues[count] != null)
          value = rowValues[count].toString();
        cell = new StringCell(styles.getStyle(count), value);
      }
      else if (type == DATETIME_TYPE)
      {
        Object obj = rowValues[count];
        if (obj == null || obj instanceof Date)
        {
          cell = new DateTimeCell(styles.getStyle(count), (Date)obj);
        }
        else
        {
          String value = null;
          if (obj != null)
            value = obj.toString();
          cell = new StringCell(styles.getStyle(count), value);
        }
      }
      else if (type == NUMBER_TYPE)
      {
        Object obj = rowValues[count];
        if (obj == null || obj instanceof Number)
        {
          cell = new NumberCell(styles.getStyle(count), (Number)obj);
        }
        else
        {
          String value = null;
          if (obj != null)
            value = obj.toString();
          cell = new StringCell(styles.getStyle(count), value);
        }
      }
      row.addCell(cell);
    }
    row.write(writer);
  }

  public void writeHeaderRow(String[] rowValues) throws IOException
  {
    rowCount++;
    Row row = new Row();
    for (int count = 0; count < rowValues.length; count++)
    {
      Cell cell = new StringCell(styles.getDefaultStyle(), rowValues[count]);
      row.addCell(cell);
    }
    row.write(writer);
  }

  public void writeHeaderRow(String value, int mergeCellCount) throws IOException
  {
    rowCount++;
    Row row = new Row();
    Cell cell = new StringCell(styles.getDefaultStyle(), value);
    cell.setMergeAcross(mergeCellCount);
    row.addCell(cell);
    row.write(writer);
  }

/*
  public static void main(String[] args)
  {
    try
    {
      ExcelXMLExport export = new ExcelXMLExport(new File("C:/Documents and Settings/shocksm/Desktop/xmltest/test.xml"));
      //export.writeRow();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.out.println(e.getMessage());
    }
    System.out.println("Finished");
  }
  */


}
