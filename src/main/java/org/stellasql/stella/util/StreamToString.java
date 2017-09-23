package org.stellasql.stella.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamToString
{
  private final static Logger logger = LogManager.getLogger(StreamToString.class);
  
  public String read(InputStream is)
  {
    StringBuilder textBuilder = new StringBuilder();
    int c = 0;
    try 
    {
      while ((c = is.read()) != -1)
      {
         textBuilder.append((char) c);
      }
    }
    catch (IOException e)
    {
      logger.error(e.getMessage(),  e);
    }
    
    return textBuilder.toString();
  }

}
