package org.stellasql.excelexporter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeCell extends Cell
{
  public DateTimeCell(Style style, Date dt)
  {
    super(style);
    dataType = "DateTime";
    value = "";
    if (dt != null)
    {
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      DateFormat tf = new SimpleDateFormat("HH:mm:ss.SSS");
      value = df.format(dt) + "T" + tf.format(dt);
    }
  }
}
