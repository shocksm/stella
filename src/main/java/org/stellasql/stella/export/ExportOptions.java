package org.stellasql.stella.export;

import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stellasql.stella.connection.QuerySequencer;
import org.stellasql.stella.gui.ResultData;

public abstract class ExportOptions
{
  private final static Logger logger = LogManager.getLogger(ExportOptions.class);

  protected File file = null;
  private boolean includeColumnNames = false;
  private boolean includeSql = false;
  private String dateFormat = null;
  private String timeFormat = null;
  private SimpleDateFormat dateSdf = null;
  private SimpleDateFormat timeSdf = null;
  private int[] sqlTypeArray = null;

  protected File getFile(int resultSetCount) throws IOException
  {
    File file = this.file;

    if (resultSetCount > 1)
    {
      if (this.file.getName().lastIndexOf(".") < 0)
        file = new File(this.file.getParent(), this.file.getName() + "_rs" + resultSetCount);
      else
      {
        String extension = this.file.getName().substring(this.file.getName().lastIndexOf("."));
        String name = this.file.getName().substring(0, this.file.getName().lastIndexOf("."));
        file = new File(this.file.getParent(), name + "_rs" + resultSetCount + extension);
      }
    }

    return file;
  }


  public abstract File open(int resultSetCount) throws Exception;

  public abstract void close() throws IOException;

  public abstract void begin() throws IOException;

  public abstract void end() throws IOException;

  public abstract void writeSql(String sql) throws IOException;

  public abstract void writeColumnNames(String[] columnNames) throws IOException;

  public abstract void writeRow(Object[] row) throws IOException;


  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
    if (this.dateFormat != null)
      dateSdf = new SimpleDateFormat(this.dateFormat);
  }

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }

  public boolean getIncludeColumnNames()
  {
    return includeColumnNames;
  }

  public void setIncludeColumnNames(boolean includeColumnNames)
  {
    this.includeColumnNames = includeColumnNames;
  }

  public boolean getIncludeSql()
  {
    return includeSql;
  }

  public void setIncludeSql(boolean includeSql)
  {
    this.includeSql = includeSql;
  }

  public String getTimeFormat()
  {
    return timeFormat;
  }

  public void setTimeFormat(String timeFormat)
  {
    this.timeFormat = timeFormat;
    if (this.timeFormat != null)
      timeSdf = new SimpleDateFormat(this.timeFormat);
  }

  protected String getStringValue(Object obj)
  {
    String value = null;
    if (obj == null)
      return null;
    else if (obj instanceof Clob)
      value = "CLOB";
    else if (obj instanceof Blob)
      value = "BLOB";
    else if (obj instanceof Timestamp)
    {
      value = "";
      if (dateSdf != null)
        value += dateSdf.format(obj);
      if (timeSdf != null)
      {
        if (value.length() > 0)
          value += " ";
        value +=  timeSdf.format(obj);
      }
    }
    else if (obj instanceof Date)
    {
      if (dateSdf != null)
        value = dateSdf.format(obj);
    }
    else if (obj instanceof Time)
    {
      if (timeSdf != null)
        value = timeSdf.format(obj);
    }
    else
      value = obj.toString();

    return value;
  }

  public int getColumnCount()
  {
    return sqlTypeArray.length;
  }

  public int getColumnType(int index)
  {
    return sqlTypeArray[index];
  }

  public Object exportResultSet(ResultSet rs, String queryText, int maxResults, int resultSetCount) throws Exception
  {
    File file = null;
    int rowCount = 0;
    try
    {
      ResultSetMetaData rsmd = rs.getMetaData();
      int columnCount = rsmd.getColumnCount();
      String[] columnNameArray = new String[columnCount];
      sqlTypeArray = new int[columnCount];
      for (int index = 0; index < columnCount; index++)
      {
        columnNameArray[index] = rsmd.getColumnName(index + 1);
        sqlTypeArray[index] = rsmd.getColumnType(index + 1);
      }

      file = open(resultSetCount);

      begin();

      if (getIncludeSql())
        writeSql(queryText);

      if (getIncludeColumnNames())
        writeColumnNames(columnNameArray);

      while (rs.next())
      {
        rowCount++;
        Object[] objArray = new Object[columnCount];
        for (int index = 1; index <= columnCount; index++)
        {
          int type = sqlTypeArray[index-1];
          objArray[index-1] = QuerySequencer.getColumnValue(type, rs, index);
        }

        writeRow(objArray);
      }

      end();
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }
    finally
    {
      try
      {
        close();
      }
      catch (IOException e)
      {
      }
    }

    String msg = rowCount + " rows";
    if (maxResults == rowCount)
      msg = "Limited to " + rowCount + " rows";
    msg += " written to " + file.toString();

    return msg;
  }

  public String exportResultData(ResultData rd) throws Exception
  {
    File file = null;
    try
    {
      int columnCount = rd.getColumnCount();
      sqlTypeArray = new int[columnCount];
      for (int index = 0; index < columnCount; index++)
      {
        sqlTypeArray[index] = rd.getSqlType(index);
      }

      file = open(1);

      begin();

      if (getIncludeSql())
        writeSql(rd.getQuery());

      String[] columnNameArray = rd.getColumnNames();
      if (getIncludeColumnNames())
        writeColumnNames(columnNameArray);

      for (int row = 0; row < rd.getRowCount(); row++)
      {
        Object[] objArray = new Object[columnCount];
        for (int column = 0; column < columnCount; column++)
        {
          objArray[column] = rd.getCell(row, column);
        }

        writeRow(objArray);
      }

      end();
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }
    finally
    {
      try
      {
        close();
      }
      catch (IOException e)
      {
        logger.error(e.getMessage(), e);
      }
    }

    String msg = rd.getRowCount() + " rows written to " + file.toString();

    return msg;
  }

}
