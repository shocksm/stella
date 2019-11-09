package org.stellasql.stella;


import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.stellasql.stella.util.XMLUtils;


public class AliasVO implements Comparable<AliasVO>
{
  private String name = "";
  private String driverName = "";
  private String url = "";
  private String username = "";
  private String password = "";
  private boolean prompt = false;
  private int dbOTWidth = 300;

  public AliasVO()
  {
  }

  public AliasVO(AliasVO aliasVO)
  {
    copy(aliasVO);
  }

  public void copy(AliasVO aliasVO)
  {
    setName(aliasVO.getName());
    setURL(aliasVO.getURL());
    setUsername(aliasVO.getUsername());
    setPassword(aliasVO.getPassword());
    setDriverName(aliasVO.getDriverName());
    setPrompt(aliasVO.getPrompt());
    setDBObjectTreeWidth(aliasVO.getDBObjectTreeWidth());
  }


  public String getQuerySeperator()
  {
    // TODO add setting per alias. for now this just gets the default value
    return ApplicationData.getInstance().getQuerySeparator();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getURL()
  {
    return url;
  }

  public void setURL(String url)
  {
    this.url = url;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public int getDBObjectTreeWidth() { return dbOTWidth; }

  public void setDBObjectTreeWidth(int width) {
    dbOTWidth = width;
  }

  public void setXML(Node node)
  {
    setName(XMLUtils.getNodeText(node, node.getUniquePath() + "/name"));
    setDriverName(XMLUtils.getNodeText(node, node.getUniquePath() + "/drivername"));
    setURL(XMLUtils.getNodeText(node, node.getUniquePath() + "/url"));
    setUsername(XMLUtils.getNodeText(node, node.getUniquePath() + "/username"));
    setPassword(XMLUtils.getNodeText(node, node.getUniquePath() + "/password"));
    setPrompt(XMLUtils.getNodeAsBoolean(node, node.getUniquePath() + "/prompt"));
    setDBObjectTreeWidth(XMLUtils.getNodeAsInt(node, node.getUniquePath() + "/dbobjecttreewidth", 300));
  }


  public Element getXML()
  {
    Element elem = DocumentHelper.createElement("connection");
    elem.addElement("name").addText(getName());
    elem.addElement("drivername").addText(getDriverName());
    elem.addElement("url").addText(getURL());
    elem.addElement("username").addText(getUsername());
    elem.addElement("password").addText(getPassword());
    elem.addElement("prompt").addText("" + getPrompt());
    elem.addElement("dbobjecttreewidth").addText("" + getDBObjectTreeWidth());

    return elem;
  }

  @Override
  public String toString()
  {
    return name;
  }


  @Override
  public int compareTo(AliasVO arg)
  {
    return getName().compareToIgnoreCase(arg.getName());
  }

  public String getDriverName()
  {
    return driverName;
  }

  public void setDriverName(String name)
  {
    driverName = name;
  }

  public void setPrompt(boolean prompt)
  {
    this.prompt = prompt;
  }

  public boolean getPrompt()
  {
    return prompt;
  }

}
