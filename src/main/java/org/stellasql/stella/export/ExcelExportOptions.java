package org.stellasql.stella.export;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import org.stellasql.excelexporter.ExcelExport;

public class ExcelExportOptions extends ExportOptions
{
  private ExcelExport excelExport = null;

  public ExcelExportOptions()
  {
    super();
  }

  public ExcelExportOptions(int rowsplit)
  {
    super();
  }

  @Override
  public File open(int resultSetCount) throws IOException, SQLException
  {
    File file = getFile(resultSetCount);

    excelExport = new ExcelExport(file);


    int columns = getColumnCount();
    for (int count = 0; count < columns; count++)
    {
      int type = getColumnType(count);
      
      if (type == Types.CHAR
          || type == Types.CLOB
          || type == Types.LONGVARCHAR
          || type == Types.VARCHAR)
      {
        excelExport.setColumnType(count, ExcelExport.STRING_TYPE);
      }
      else if (type == Types.DATE
              ||type == Types.TIME
              || type == Types.TIMESTAMP)
      {
        String format = getDateFormat();
        if (format.length() > 0 && getTimeFormat().length() > 0)
          format += " ";
        format += getTimeFormat().replaceAll("a", "AM/PM");

        excelExport.setColumnType(count, ExcelExport.DATETIME_TYPE, format);
      }
      else if (type == Types.BIGINT
              || type == Types.BINARY
              || type == Types.BIT
              || type == Types.DECIMAL
              || type == Types.DOUBLE
              || type == Types.FLOAT
              || type == Types.INTEGER
              || type == Types.NUMERIC
              || type == Types.REAL
              || type == Types.SMALLINT
              || type == Types.TINYINT)
      {
        excelExport.setColumnType(count, ExcelExport.NUMBER_TYPE);
      }
      else
      {
        excelExport.setColumnType(count, ExcelExport.STRING_TYPE);
      }
    }

    return file;
  }

  @Override
  public void writeRow(Object[] rowValues) throws IOException
  {
    excelExport.writeRow(rowValues);
  }

  @Override
  public void close() throws IOException
  {
    excelExport.close();
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
    excelExport.writeHeaderRow(columnNames);
  }

  @Override
  public void writeSql(String sql) throws IOException
  {
    excelExport.writeHeaderRow(sql, 10);
  }

}
