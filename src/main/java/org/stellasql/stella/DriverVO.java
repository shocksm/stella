package org.stellasql.stella;


import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.stellasql.stella.util.XMLUtils;


public class DriverVO implements Comparable
{
  private String name = "";
  private List driverPathList = new LinkedList();
  private String driverClass = "";
  private String exampleJdbcUrl = "";
  private String websiteUrl = "";
  private boolean basicParameters = true;

  public DriverVO()
  {
  }

  public DriverVO(DriverVO driverVO)
  {
    copy(driverVO);
  }

  public void copy(DriverVO driverVO)
  {
    setName(driverVO.getName());
    setDriverClass(driverVO.getDriverClass());
    setDriverPathList(new LinkedList(driverVO.getDriverPathList()));
    setExampleJdbcUrl(driverVO.getExampleJdbcUrl());
    setWebsiteUrl(driverVO.getWebsiteUrl());
    setUseBasicParameters(driverVO.getUseBasicParameters());
  }

  public boolean isActive()
  {

    if (driverPathList.size() > 0 && driverClass.length() > 0)
    {
      for (Iterator it = driverPathList.iterator(); it.hasNext();)
      {
        String path = (String)it.next();
        File file = new File(path);
        if (!file.exists())
          return false;
      }
    }
    else
      return false;

    return true;
  }

  public String getDriverClass()
  {
    return driverClass;
  }

  public void setDriverClass(String driverClass)
  {
    this.driverClass = driverClass;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getExampleJdbcUrl()
  {
    return exampleJdbcUrl;
  }

  public void setExampleJdbcUrl(String url)
  {
    exampleJdbcUrl = url;
  }

  public void setXML(Node node)
  {
    setName(XMLUtils.getNodeText(node, node.getUniquePath() + "/name"));
    setDriverClass(XMLUtils.getNodeText(node, node.getUniquePath() + "/driverclass"));
    List nodeList = node.selectNodes("driverpath");
    List pathList = new LinkedList();
    for (Iterator it = nodeList.iterator(); it.hasNext();)
    {
      Node pathNode = (Node)it.next();
      pathList.add(pathNode.getText());
    }
    setDriverPathList(pathList);
    setExampleJdbcUrl(XMLUtils.getNodeText(node, node.getUniquePath() + "/examplejdbcurl"));
    setUseBasicParameters(XMLUtils.getNodeAsBoolean(node, node.getUniquePath() + "/usebasic"));
    setWebsiteUrl(XMLUtils.getNodeText(node, node.getUniquePath() + "/websiteurl"));
  }

  public Element getXML()
  {
    Element elem = DocumentHelper.createElement("driver");
    elem.addElement("name").addText(getName());
    elem.addElement("driverclass").addText(getDriverClass());

    for (Iterator it = driverPathList.iterator(); it.hasNext();)
    {
      String path = (String)it.next();
      elem.addElement("driverpath").addText(path);
    }

    elem.addElement("examplejdbcurl").addText(getExampleJdbcUrl());
    elem.addElement("usebasic").addText("" + getUseBasicParameters());
    elem.addElement("websiteurl").addText(getWebsiteUrl());

    return elem;
  }

  @Override
  public boolean equals(Object arg)
  {
    DriverVO driverVO2 = (DriverVO)arg;
    if (driverVO2 == null)
      return false;
    else
      return getName().equalsIgnoreCase(driverVO2.getName());
  }

  @Override
  public int compareTo(Object arg)
  {
    DriverVO driverVO2 = (DriverVO)arg;
    return getName().compareToIgnoreCase(driverVO2.getName());
  }

  public List getDriverPathList()
  {
    return driverPathList;
  }

  public List getDriverPathFileList()
  {
    List list = new LinkedList();
    for (Iterator it = driverPathList.iterator(); it.hasNext();)
    {
      String path = (String)it.next();
      list.add(new File(path));
    }
    return list;
  }

  public void setDriverPathList(List driverPathList)
  {
    this.driverPathList = new LinkedList();
    Iterator it = driverPathList.iterator();
    while (it.hasNext())
    {
      String path = (String)it.next();
      if (path.trim().length() > 0)
      {
        this.driverPathList.add(path.trim());
      }
    }

  }

  public String getWebsiteUrl()
  {
    return websiteUrl;
  }

  public void setWebsiteUrl(String websiteUrl)
  {
    this.websiteUrl = websiteUrl;
  }

  public static String validateBasicParameters(String jdbcURL)
  {
    String msg = null;

    if (jdbcURL == null || jdbcURL.trim().length() == 0)
    {
      msg = "The JDBC URL is empty";
    }
    else
    {
      boolean valid = true;
      boolean oneFound = false;
      boolean inBrace = false;
      boolean inReq = false;
      boolean inBraceFoundOne = false;
      for (int index = 0; index < jdbcURL.length(); index++)
      {
        char c = jdbcURL.charAt(index);
        if (c == '<')
        {
          if (inReq)
          {
            valid = false;
            msg = "The '<' character can not be inside of a <...> segment";
            break;
          }
          else
            inReq = true;
        }
        else if (c == '>')
        {
          if (inReq && jdbcURL.charAt(index-1) != '<')
          {
            inReq = false;
            oneFound = true;

            if (inBraceFoundOne)
            {
              msg = "Only one <...> segment can appear withing a [...] segment";
              valid = false;
              break;
            }

            if (inBrace)
              inBraceFoundOne = true;
          }
          else if (jdbcURL.charAt(index-1) == '<')
          {
            msg = "A segment name must be between the '<' and '>' characters";
            valid = false;
            break;
          }
          else
          {
            msg = "The '>' character must be after a matching '<' character";
            valid = false;
            break;
          }
        }
        else if (c == '[')
        {
          if (inBrace)
          {
            valid = false;
            msg = "The '[' character can not be inside of a [...] segment";
            break;
          }
          else
            inBrace = true;
        }
        else if (c == ']')
        {
          if (inBrace)
          {
            inBrace = false;
            if (!inBraceFoundOne)
            {
              msg = "One <...> segment must appear within a [...] segment";
              valid = false;
              break;
            }
            inBraceFoundOne = false;
          }
          else
          {
            msg = "The ']' character must be after a matching '[' character";
            valid = false;
            break;
          }
        }
      }
      if (valid && !oneFound)
      {
        msg = "At least one <...> segment must be present";
        valid = false;
      }
      else if (valid && inReq)
      {
        msg = "The '<' character must be have a matching '>' character to close the segment";
        valid = false;
      }
      else if (valid && inBrace)
      {
        msg = "The ']' character must be have a matching ']' character to close the segment";
        valid = false;
      }
    }

    return msg;
  }

  public boolean getUseBasicParameters()
  {
    boolean value = basicParameters;

    if (value)
    {
      if (exampleJdbcUrl.trim().length() == 0
          || validateBasicParameters(exampleJdbcUrl) != null)
      {
        value = false;
      }
    }

    return value;
  }

  public void setUseBasicParameters(boolean basicParameters)
  {
    this.basicParameters = basicParameters;
  }

  public List getUrlParameters()
  {
    List parameters = new LinkedList();

    if (exampleJdbcUrl == null || exampleJdbcUrl.length() == 0)
      return parameters;

    if (exampleJdbcUrl.indexOf('<') < 0 || exampleJdbcUrl.indexOf('>') < 0)
      return parameters;

    List tokens = tokenize();
    for (Iterator it = tokens.iterator(); it.hasNext();)
    {
      Object obj = it.next();
      if (obj instanceof ParameterToken)
      {
        ParameterToken pt = (ParameterToken)obj;
        DriverParameter dp = new DriverParameter(pt.name, pt.value, true);
        parameters.add(dp);
      }
      else if (obj instanceof OptionalToken)
      {
        OptionalToken ot = (OptionalToken)obj;
        ParameterToken pt = ot.paramToken;
        if (pt != null)
        {
          DriverParameter dp = new DriverParameter(pt.name, pt.value, false);
          parameters.add(dp);
        }
      }
    }

    return parameters;
  }

  private String getLiteral(Object obj)
  {
    if (obj instanceof OptionalToken)
    {
      OptionalToken ot = (OptionalToken)obj;
      return ot.pre;
    }
    else if (obj instanceof LiteralToken)
    {
      LiteralToken lt = (LiteralToken)obj;
      return lt.value;
    }

    // invalid
    return null;
  }

  private String parseValue(List tokens, String url, int start)
  {
    if (start >= tokens.size())
      return url;

    String value = null;
    int nextIndex = start;
    while (nextIndex < tokens.size())
    {
      String next = getLiteral(tokens.get(nextIndex));
      if (next == null)
        break;

      if (url.indexOf(next) > 0)
      {
        value = url.substring(0, url.indexOf(next));
        break;
      }
      nextIndex++;
    }

    return value;
  }

  public Map parseUrlParameters(String url)
  {
    Map map = new HashMap();

    List tokens = tokenize();
    for (int index = 0; index < tokens.size() && url.length() > 0; index++)
    {
      Object obj = tokens.get(index);
      if (obj instanceof ParameterToken)
      {
        ParameterToken pt = (ParameterToken)obj;
        String value = parseValue(tokens, url, index + 1);
        if (value == null)
        {
          value = url;
          url = "";
        }
        else
          url = url.substring(value.length());

        if (value.length() > 0)
          map.put(pt.name, value);
      }
      else if (obj instanceof OptionalToken)
      {
        OptionalToken ot = (OptionalToken)obj;
        ParameterToken pt = ot.paramToken;
        if (url.startsWith(ot.pre))
        {
          url = url.substring(ot.pre.length());
          if (ot.post.length() > 0 && url.indexOf(ot.post) > 0)
          {
            String value = url.substring(0, url.indexOf(ot.post));
            url = url.substring(value.length());
            if (value.length() > 0)
              map.put(pt.name, value);
          }
          else
          {
            String value = parseValue(tokens, url, index + 1);
            if (value == null)
            {
              value = url;
              url = "";
            }
            else
              url = url.substring(value.length());

            if (value.length() > 0)
              map.put(pt.name, value);
          }
        }
      }
      else
      {
        LiteralToken lt = (LiteralToken)obj;
        if (url.length() < lt.value.length())
          return new HashMap();

        if (!url.toLowerCase().startsWith(lt.value.toLowerCase()))
          return new HashMap();

        url = url.substring(lt.value.length());
      }
    }

    return map;
  }

  public String buildUrl(Map paramMap)
  {
    StringBuffer sbuf = new StringBuffer();

    List tokens = tokenize();
    for (int index = 0; index < tokens.size(); index++)
    {
      Object obj = tokens.get(index);
      if (obj instanceof ParameterToken)
      {
        ParameterToken pt = (ParameterToken)obj;
        if (paramMap.containsKey(pt.name))
          sbuf.append(paramMap.get(pt.name));
        else
          sbuf.append("?");
      }
      else if (obj instanceof OptionalToken)
      {
        OptionalToken ot = (OptionalToken)obj;
        ParameterToken pt = ot.paramToken;
        if (paramMap.containsKey(pt.name))
        {
          sbuf.append(ot.pre);
          if (paramMap.containsKey(pt.name))
            sbuf.append(paramMap.get(pt.name));
          else
            sbuf.append("?");
          sbuf.append(ot.post);
        }
      }
      else
      {
        LiteralToken lt = (LiteralToken)obj;
        sbuf.append(lt.value);
      }
    }

    return sbuf.toString();
  }

  private List tokenize()
  {
    UrlScanner scanner = new UrlScanner();
    scanner.setText(exampleJdbcUrl);
    List tokenList = new LinkedList();
    Object token = null;
    while ((token = scanner.nextToken()) != null)
    {
      int start = scanner.getStartPosition();
      int end = scanner.getEndPosition();
      if (token == UrlScanner.PARAMETER)
        tokenList.add(new ParameterToken(exampleJdbcUrl.substring(start, end+1)));
      else if (token == UrlScanner.OPTIONAL)
        tokenList.add(new OptionalToken(exampleJdbcUrl.substring(start, end+1)));
      else
        tokenList.add(new LiteralToken(exampleJdbcUrl.substring(start, end+1)));
    }

    return tokenList;
  }

  private class LiteralToken
  {
    public String value = "";
    public LiteralToken(String text)
    {
      if (text.startsWith("<"))
        text = text.substring(1);
      if (text.endsWith(">"))
        text = text.substring(0, text.length() - 1);

      value = text;
    }

    @Override
    public String toString()
    {
      return "LiteralToken '" + value + "'";
    }
  }

  private class ParameterToken
  {
    public String name = "";
    public String value = "";
    public ParameterToken(String text)
    {
      if (text.startsWith("<"))
        text = text.substring(1);
      if (text.endsWith(">"))
        text = text.substring(0, text.length() - 1);

      name = text;
      if (name.indexOf('-') >= 0)
      {
        value = name.substring(name.indexOf('-') + 1);
        name = name.substring(0, name.indexOf('-'));
      }

      name = name.toLowerCase();
    }

    @Override
    public String toString()
    {
      return "ParamaterToken name = '" + name + "' default = '" + value + "'";
    }
  }

  private class OptionalToken
  {
    public String pre = "";
    public String post = "";
    public ParameterToken paramToken;
    public OptionalToken(String text)
    {
      if (text.startsWith("["))
        text = text.substring(1);
      if (text.endsWith("]"))
        text = text.substring(0, text.length() - 1);

      UrlScanner scanner = new UrlScanner();
      scanner.setText(text);
      Object token = null;
      while ((token = scanner.nextToken()) != null)
      {
        int start = scanner.getStartPosition();
        int end = scanner.getEndPosition();
        if (token == UrlScanner.PARAMETER)
          paramToken = new ParameterToken(text.substring(start, end+1));
        else if (paramToken == null)
          pre = text.substring(start, end+1);
        else
          post = text.substring(start, end+1);
      }
    }

    @Override
    public String toString()
    {
      return "OptionalToken pre = '" + pre + "' post = '" + post + "' " + paramToken;
    }
  }



  private class UrlScanner
  {
    public static final String LITERAL = "LITERAL";
    public static final String PARAMETER = "PARAMETER";
    public static final String OPTIONAL = "OPTIONAL";

    private int position;
    private int endPosition;
    private int tokenStartPosition;
    private String text = null;;

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
        else if (c == '<') // param start
        {
          while (true)
          {
            c = read();
            if (c < 0 || c == '>')
            {
              return PARAMETER;
            }
          }
        }
        else if (c == '[') // optional blockcomment
        {
          while (true)
          {
            c = read();
            if (c < 0 || c == ']')
            {
              return OPTIONAL;
            }
          }
        }
        else
        {
          while (true)
          {
            c = read();
            if (c < 0 || c == '<' || c == '>' || c == '[' || c == ']')
            {
              putback(c);
              return LITERAL;
            }
          }
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

  }

  public static void main(String[] args)
  {
    DriverVO driverVo = new DriverVO();
    driverVo.test();
  }

  public void test()
  {
    String urlExample = "jdbc:oracle:thin:@<server>:<port>:<database SID>";
    String url = "jdbc:oracle:thin:@ecommhost.corp.anthem.com:123:ecomm";

    exampleJdbcUrl = urlExample;

    Map map = parseUrlParameters(url);

    System.out.println(buildUrl(map));
  }


}
