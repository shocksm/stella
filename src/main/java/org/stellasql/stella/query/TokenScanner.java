package org.stellasql.stella.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stellasql.stella.gui.util.SyntaxHighlighter;

public class TokenScanner
{
  private final static Logger logger = LogManager.getLogger(SyntaxHighlighter.class);

  private static final int NEWLINE = 10;

  public static final String UNKNOWN = "UNKNOWN";
  public static final String LINECOMMENT = "LINECOMMENT";
  public static final String BLOCKCOMMENT = "BLOCKCOMMENT";
  public static final String STRING = "STRING";
  public static final String NUMBER = "NUMBER";
  public static final String WHITESPACE = "WHITESPACE";
  public static final String NON_KEYWORD = "NON_KEYWORD";
  public static final String KEYWORD = "KEYWORD";
  public static final String OPERATOR = "OPERATOR";
  public static final String SEPARATOR = "SEPARATOR";

  private int position;
  private int endPosition;
  private int tokenStartPosition;
  private String text = null;;
  private StringBuffer sbuf = new StringBuffer();
  private String querySeparator = "";

  //private int mark;

  private static HashMap keywordTypeMap = null;

  public TokenScanner()
  {
    initializeKeywords();
  }

  public TokenScanner(String querySeparator)
  {
    initializeKeywords();
    this.querySeparator = querySeparator.toLowerCase();
  }

  public void setQuerySeparator(String querySeparator)
  {
    this.querySeparator = querySeparator;
  }

  public void clear()
  {
    text = null;
  }

  public void setText(String text)
  {
    this.text = text;
    position = 0;
    endPosition = this.text.length() - 1;
  }

  public int getEndPosition()
  {
    return position - 1;
  }

  public int getStartPosition()
  {
    return tokenStartPosition;
  }

  public int getLength()
  {
    return position - tokenStartPosition;
  }

  public Object nextToken()
  {
    int c;
    tokenStartPosition = position;
    while (true)
    {
      c = read();

      if (c < 0)
        return null;
      else if (c == '-') // line comment
      {
        c = read();
        if (c == '-')
        {
          while (true)
          {
            c = read();
            if (c < 0 || c == NEWLINE)
            {
              if (c < 0)
                putback(c);
              return LINECOMMENT;
            }
          }
        }

        putback(c);
        return OPERATOR;
      }
      else if (c == '/') // block comment
      {
        c = read();
        if (c == '*')
        {
          while (true)
          {
            c = read();
            if (c < 0)
            {
              return BLOCKCOMMENT;
            }
            else if (c == '*')
            {
              c = read();
              if (c == '/')
                return BLOCKCOMMENT;

              putback(c);
            }
          }
        }

        putback(c);
        return OPERATOR;
      }
      else if (c == '\\') // string escape start
      {
        c = read();
        if (c == '\'')
          return UNKNOWN;

        putback(c);
        return UNKNOWN;
      }
      else if (c == '\'') // string
      {
        while (true)
        {
          c = read();
          if (c == '\\')
          {
            c = read(); // escape char so skip the next character
          }
          else if (c == '\'')
          {
            return STRING;
          }
          else if (c < 0)
          {
            return STRING;
          }
        }
      }
      else if (Character.isDigit((char)c))
      {
        while (Character.isDigit((char)c))
        {
          // read until we find a non digit
          c = read();
        }

        putback(c);
        return NUMBER;
      }
      else if (Character.isWhitespace((char)c))
      {
        while (Character.isWhitespace((char)c))
        {
          // read until we find a non whitespace
          c = read();
        }
        putback(c);
        return WHITESPACE;
      }
      else if (isKeywordStart((char)c) && keywordTypeMap != null)
      {
        do
        {
          sbuf.append((char)c);
          c = read();
        }
        while (isKeywordPart((char)c));
        putback(c);

        String text = sbuf.toString().toLowerCase();
        Object key = keywordTypeMap.get(text);

        sbuf.setLength(0);

        if (text.equalsIgnoreCase(querySeparator))
          return SEPARATOR;
        else if (key != null)
          return key;
        else
          return NON_KEYWORD;
      }
      else if (isSeparatorStart((char)c))
      {
        boolean separator = true;
        for (int index = 1; index < querySeparator.length(); index++)
        {
          int temp = read();
          sbuf.append((char)temp);

          if (Character.toLowerCase((char)temp) != querySeparator.charAt(index))
          {
            separator = false;
            break;
          }
        }

        if (!separator)
        {
          for (int index = sbuf.length() - 1; index >= 0; index--)
            putback(sbuf.charAt(index));
        }

        sbuf.setLength(0);

        if (separator)
          return SEPARATOR;
        else if (isOperator((char)c))
          return OPERATOR;
        else
          return UNKNOWN;
      }
      else if (isOperator((char)c))
      {
        return OPERATOR;
      }
      else
      {
        return UNKNOWN;
      }
    }
  }

  private int read()
  {
    if (position <= endPosition)
      return text.charAt(position++);
    else
      return -1;
  }

  private void putback(int c)
  {
    if (c != -1)
      position--;
  }

  /*
  private void mark()
  {
    mark = position + 1;
  }

  private void rewindToMark()
  {
    position = mark;
  }
  */

  protected boolean isSeparatorStart(char c)
  {
    if (querySeparator == null || querySeparator.length() == 0)
      return false;

    return Character.toLowerCase(c) == querySeparator.charAt(0);
  }

  protected boolean isKeywordStart(char c)
  {
    return Character.isLetter(c);
  }

  protected boolean isKeywordPart(char c)
  {
    if (Character.isLetterOrDigit(c) || c == '_')
      return true;
    else
      return false;
  }

  protected boolean isOperator(char c)
  {
    if ("!%^&*(){}[]-=+|;:<>/".indexOf(c) >= 0)
      return true;
    else
      return false;
  }

  private synchronized void initializeKeywords()
  {
    if (keywordTypeMap == null)
    {
      keywordTypeMap = new HashMap();
      try
      {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("keywords.txt");
        if (is == null)
        {
          TokenScanner.logger.error("keywords.txt file not found");
        }
        else
        {
          BufferedReader br = new BufferedReader(new InputStreamReader(is));
          String line;
          while ((line = br.readLine()) != null)
          {
            keywordTypeMap.put(line.toLowerCase(), TokenScanner.KEYWORD);
          }
        }
      }
      catch (IOException e)
      {
        logger.error(e.getMessage(), e);
      }
    }
  }

}
