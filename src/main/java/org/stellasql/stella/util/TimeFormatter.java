package org.stellasql.stella.util;

import java.text.NumberFormat;

public class TimeFormatter
{
  public static String format(long millis)
  {
    StringBuffer sbuf = new StringBuffer();

    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(2);
    nf.setMinimumFractionDigits(2);

    float seconds = millis / 1000f;
    if (sbuf.length() > 0)
      sbuf.append(" ");
    sbuf.append(nf.format(seconds)).append(" secs");

    return sbuf.toString();
  }
}
