package org.stellasql.stella.util;

import java.io.FileInputStream;

/**
 * Reads a file and spits out a byte array that can be pasted into code.
 *
 * @author Lord Commander Shockey
 *
 */
public class ByteToCode
{

  public static void main(String[] args)
  {
    try {
      String file = "c:/error.png";
      FileInputStream fis = new FileInputStream(file);
      System.out.println("{");
      int count = 0;
      int i = fis.read();
      while (i >= 0)
      {
        count++;
        String b = Integer.toHexString(i);
        if (b.length() == 1)
        {
          b = "0" + b;
        }

        System.out.print("(byte)0x" + b);

        i = fis.read();

        if (i >= 0)
        {
          System.out.print(", ");
        }

        if (count == 10)
        {
          System.out.println();
          count = 0;
        }
      }

      System.out.println("};");

      fis.close();
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }

  }
}
