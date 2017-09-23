package org.stellasql.stella.util;

import org.dom4j.Node;

public class XMLUtils
{
  public static String getNodeText(Node node, String xpath)
  {
    String ret = "";
    Node selectedNode = node.selectSingleNode(xpath);
    if (selectedNode != null)
      ret = selectedNode.getText();
    return ret;
  }

  public static boolean getNodeAsBoolean(Node node, String xpath)
  {
    boolean ret = false;

    Node selectedNode = node.selectSingleNode(xpath);
    if (selectedNode != null)
    {
      String text = selectedNode.getText();
      if (text != null)
      {
        ret = (new Boolean(text)).booleanValue();
      }
    }

    return ret;
  }

  public static int getNodeAsInt(Node node, String xpath)
  {
    return getNodeAsInt(node, xpath, 0);
  }

  public static int getNodeAsInt(Node node, String xpath, int defValue)
  {
    int ret = defValue;

    Node selectedNode = node.selectSingleNode(xpath);
    if (selectedNode != null)
    {
      String text = selectedNode.getText();
      if (text != null)
      {
        try
        {
          ret = Integer.parseInt(text);
        }
        catch (NumberFormatException e)
        {
        }
      }
    }

    return ret;
  }

}
