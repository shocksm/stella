package org.stellasql.excelexporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelExport
{
  private Map columnType = new HashMap();
  private Map formatMask = new HashMap();
  private File file = null;
  private SXSSFWorkbook wb;
  private Sheet sh;
  private int rowCount = 0;
  
  public final static String STRING_TYPE = "STRING";
  public final static String NUMBER_TYPE = "NUMBER";
  public final static String DATETIME_TYPE = "DATETIME";

  public ExcelExport(File file)
  {
    this.file = file;
    wb = new SXSSFWorkbook(100);
    sh = wb.createSheet();
  }
  
  public void close() throws IOException
  {
    FileOutputStream out = new FileOutputStream(file);
    wb.write(out);
    out.close();
    
    // dispose of temporary files backing this workbook on disk
    wb.dispose();
  }

  public void setColumnType(int column, String type)
  {
    columnType.put(new Integer(column), type);
  }

  public void setColumnType(int column, String type, String format)
  {
    columnType.put(new Integer(column), type);
    formatMask.put(new Integer(column), format);
  }
  
  public void writeRow(Object[] rowValues) throws IOException
  {
    Row row = sh.createRow(rowCount++);
    
    for (int count = 0; count < rowValues.length; count++)
    {
      Cell cell = row.createCell(count);
      
      String type = (String)columnType.get(new Integer(count));
      String format = (String)formatMask.get(new Integer(count));
      if (type == STRING_TYPE)
      {
        String value = rowValues[count] == null ? "" : rowValues[count].toString();
        cell.setCellValue(value);
      }
      else if (type == DATETIME_TYPE)
      {
        Object obj = rowValues[count];
        if (obj == null || obj instanceof Date)
        {
          cell.setCellValue((Date)obj);
          
          DataFormat df = wb.createDataFormat();
          
          CellStyle style = wb.createCellStyle();
          style.setDataFormat(df.getFormat(DateFormatConverter.convert(Locale.getDefault(), format)));
          cell.setCellStyle(style);
        }
        else
        {
          cell.setCellValue((String)obj);
        }
      }
      else if (type == NUMBER_TYPE)
      {
        Object obj = rowValues[count];
        if (obj instanceof Number)
        {
          cell.setCellValue(((Number)obj).doubleValue());
        }
        else if (obj instanceof Boolean)
        {
          cell.setCellValue(((Boolean)obj).booleanValue());
        }
        else if (obj != null)
        {
          cell.setCellValue(obj.toString());
        }
      }
    }
  }
  
  public void writeHeaderRow(String[] rowValues) throws IOException
  {
    Row row = sh.createRow(rowCount++);
    for (int count = 0; count < rowValues.length; count++)
    {
      Cell cell = row.createCell(count);
      cell.setCellValue(rowValues[count]);
    }
  }
  
  public void writeHeaderRow(String value, int mergeCellCount) throws IOException
  {
    Row row = sh.createRow(rowCount++);
    
    Cell cell = row.createCell(0);
    cell.setCellValue(value);
    sh.addMergedRegion(new CellRangeAddress(rowCount-1, rowCount-1, 0, mergeCellCount-1));
    
  }
  
}
