package org.stellasql.stella.gui.statement;

import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataTypeUtil
{
  private static SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
  private static SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss");

  public static boolean isCharacterType(int type)
  {
    if (type == Types.CHAR)
      return true;
    else if (type == Types.CLOB)
      return true;
    else if (type == Types.LONGVARCHAR)
      return true;
    else if (type == Types.LONGVARCHAR)
      return true;
    else if (type == Types.VARCHAR)
      return true;


    return false;
  }

  public static boolean isTimestampType(int type)
  {
    if (type == Types.TIMESTAMP)
      return true;

    return false;
  }

  public static boolean isDateType(int type)
  {
    if (type == Types.DATE)
      return true;

    return false;
  }

  public static boolean isTimeType(int type)
  {
    if (type == Types.TIME)
      return true;

    return false;
  }

  public static String formatAsTimestamp(Calendar cal)
  {
    return formatAsTimestamp(cal.getTime());
  }

  public static String formatAsTimestamp(Date dt)
  {
    StringBuffer sbuf = new StringBuffer();

    sbuf.append("{ts '");
    sbuf.append(dateSdf.format(dt));
    sbuf.append(" ");
    sbuf.append(timeSdf.format(dt));
    sbuf.append("'}");

    return sbuf.toString();
  }

  public static String formatAsTimestampWithNanos(Timestamp ts)
  {
    StringBuffer sbuf = new StringBuffer();

    double nanos = ts.getNanos() / 1000000000d;
    DecimalFormat df = new DecimalFormat("#.0#############");

    sbuf.append("{ts '");
    sbuf.append(dateSdf.format(ts));
    sbuf.append(" ");
    sbuf.append(timeSdf.format(ts));
    sbuf.append(df.format(nanos));
    sbuf.append("'}");

    return sbuf.toString();
  }

  public static String formatAsDate(Calendar cal)
  {
    return formatAsDate(cal.getTime());
  }

  public static String formatAsDate(Date dt)
  {
    StringBuffer sbuf = new StringBuffer();

    sbuf.append("{d '");
    sbuf.append(dateSdf.format(dt));
    sbuf.append("'}");

    return sbuf.toString();
  }

  public static String formatAsTime(Calendar cal)
  {
    return formatAsTime(cal.getTime());
  }

  public static String formatAsTime(Date dt)
  {
    StringBuffer sbuf = new StringBuffer();

    sbuf.append("{t '");
    sbuf.append(timeSdf.format(dt));
    sbuf.append("'}");

    return sbuf.toString();
  }

  public static String formatAsTimeWithNanos(Timestamp ts)
  {
    StringBuffer sbuf = new StringBuffer();

    sbuf.append("{t '");
    sbuf.append(timeSdf.format(ts));
    sbuf.append(".").append(ts.getNanos());
    sbuf.append("'}");

    return sbuf.toString();
  }

  public static String getNanoSecond(Timestamp ts)
  {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(ts.getNanos());

    return sbuf.toString();
  }

  public static String getTypename(int type)
  {
    if (type == Types.ARRAY)
      return "ARRAY";
    if (type == Types.BIGINT)
      return "BIGINT";
    if (type == Types.BINARY)
      return "BINARY";
    if (type == Types.BIT)
      return "BIT";
    if (type == Types.BLOB)
      return "BLOB";
    if (type == Types.BOOLEAN)
      return "BOOLEAN";
    if (type == Types.CHAR)
      return "CHAR";
    if (type == Types.CLOB)
      return "CLOB";
    if (type == Types.DATALINK)
      return "DATALINK";
    if (type == Types.DATE)
      return "DATE";
    if (type == Types.DECIMAL)
      return "DECIMAL";
    if (type == Types.DISTINCT)
      return "DISTINCT";
    if (type == Types.DOUBLE)
      return "DOUBLE";
    if (type == Types.FLOAT)
      return "FLOAT";
    if (type == Types.INTEGER)
      return "INTEGER";
    if (type == Types.JAVA_OBJECT)
      return "JAVA_OBJECT";
    if (type == Types.LONGVARBINARY)
      return "LONGVARBINARY";
    if (type == Types.LONGVARCHAR)
    return "LONGVARCHAR";
    if (type == Types.NULL)
      return "NULL";
    if (type == Types.NUMERIC)
      return "NUMERIC";
    if (type == Types.OTHER)
      return "OTHER";
    if (type == Types.REAL)
      return "REAL";
    if (type == Types.REF)
      return "REF";
    if (type == Types.SMALLINT)
      return "SMALLINT";
    if (type == Types.STRUCT)
      return "STRUCT";
    if (type == Types.TIME)
      return "TIME";
    if (type == Types.TIMESTAMP)
      return "TIMESTAMP";
    if (type == Types.TINYINT)
      return "TINYINT";
    if (type == Types.VARBINARY)
      return "VARBINARY";
    if (type == Types.VARCHAR)
      return "VARCHAR";

    return "unknown";
  }

}
