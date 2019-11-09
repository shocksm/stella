package org.stellasql.stella;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.stellasql.stella.gui.custom.FontContainer;
import org.stellasql.stella.gui.util.SyntaxContainer;
import org.stellasql.stella.gui.util.SyntaxHighlighter;
import org.stellasql.stella.query.TokenScanner;
import org.stellasql.stella.util.XMLUtils;

public class ApplicationData
{
  private final static Logger logger = LogManager.getLogger(ApplicationData.class);

  private final static String VERSION = "1.0";


  private static ApplicationData applicationData = null;

  private File appDir = null;
  private File dataFile = null;
  private File historyFile = null;
  private File favoriteFile = null;
  private List aliasList = new LinkedList();
  private Map queryHistoryMap = new HashMap();
  private Map queryFavoriteMap = new HashMap();
  //private Map queryHistoryListenerMap = new HashMap();
  private boolean maximized = false;
  private int width = 800;
  private int height = 600;
  private int positionX = 0;
  private int positionY = 0;
  private String selectedAlias = "";
  private List aliasListenerList = new LinkedList();
  private List fontChangeListenerList = new LinkedList();
  private List disposeFonts = new LinkedList();
  private FontContainer generalFont = null;
  private FontContainer queryTextFont = null;
  private FontContainer resultsFont = null;
  private FontContainer treeFont;
  private boolean queryTextFontSet = false;
  private boolean resultsFontSet = false;
  private boolean treeFontSet = false;
  private List driverList = new LinkedList();
  private List driverListenerList = new LinkedList();
  private boolean autoCommit = true;
  private boolean limitResults = true;
  private boolean stripComments = false;
  private int maxRows = 100;
  private int maxQueryHistory = 100;
  private String querySeparator = ";";
  private Map syntaxMap = new HashMap();
  private List syntaxListenerList = new LinkedList();
  private QueryFavoriteFolder favoritesRootFolder = null;


  private ApplicationData()
  {
    String home = System.getProperties().getProperty("user.home");
    if (home == null)
      home = "";
    File homeDir = new File(home);
    appDir = new File(homeDir, ".stella");
    dataFile = new File(appDir, "data.xml");
    historyFile = new File(appDir, "history.xml");
    favoriteFile = new File(appDir, "favorites.xml");

    favoritesRootFolder = new QueryFavoriteFolder(0, "Favorites");
  }

  public static synchronized ApplicationData getInstance()
  {
    if (applicationData == null)
      applicationData = new ApplicationData();

    return applicationData;
  }

  public int getDriverCount()
  {
    return driverList.size();
  }

  public boolean getDriverExists(DriverVO driverVO)
  {
    boolean exists = false;
    for (int count = 0; count < driverList.size(); count++)
    {
      DriverVO driverVOCheck = (DriverVO)driverList.get(count);
      if (driverVOCheck.getName().equalsIgnoreCase(driverVO.getName()))
      {
        exists = true;
        break;
      }
    }
    return exists;
  }

  public DriverVO getDriver(int index)
  {
    return new DriverVO((DriverVO)driverList.get(index));
  }

  public DriverVO getDriver(String name)
  {
    for (int count = 0; count < driverList.size(); count++)
    {
      DriverVO driverVOCheck = (DriverVO)driverList.get(count);
      if (driverVOCheck.getName().equalsIgnoreCase(name))
      {
        return driverVOCheck;
      }
    }
    return null;
  }

  public void addDriver(DriverVO driverVO)
  {
    driverList.add(driverVO);
    Collections.sort(driverList);

    fireDriverAdded(driverVO.getName());
  }

  public void updateDriver(DriverVO driverVOOld, DriverVO driverVONew)
  {
    for (Iterator it = driverList.iterator(); it.hasNext();)
    {
      DriverVO driverVOCheck = (DriverVO)it.next();
      if (driverVOCheck.getName().equalsIgnoreCase(driverVOOld.getName()))
      {
        it.remove();
        break;
      }
    }

    driverList.add(driverVONew);
    Collections.sort(driverList);

    if (!driverVOOld.getName().equalsIgnoreCase(driverVONew.getName()))
    {
      for (Iterator it = aliasList.iterator(); it.hasNext();)
      {
        AliasVO aliasVO = (AliasVO)it.next();
        if (driverVOOld.getName().equalsIgnoreCase(aliasVO.getDriverName()))
        {
          aliasVO.setDriverName(driverVONew.getName());
        }
      }
    }

    fireDriverChanged(driverVOOld.getName(), driverVONew.getName());
  }

  public void removeDriver(DriverVO driverVO)
  {
    for (Iterator it = driverList.iterator(); it.hasNext();)
    {
      DriverVO driverVOCheck = (DriverVO)it.next();
      if (driverVOCheck.getName().equalsIgnoreCase(driverVO.getName()))
      {
        it.remove();
        break;
      }
    }

    for (Iterator it = aliasList.iterator(); it.hasNext();)
    {
      AliasVO aliasVO = (AliasVO)it.next();
      if (driverVO.getName().equalsIgnoreCase(aliasVO.getDriverName()))
      {
        aliasVO.setDriverName("");
      }
    }

    fireDriverRemoved(driverVO.getName());
  }

  public boolean getAliasExists(AliasVO aliasVO)
  {
    boolean exists = false;
    for (int count = 0; count < aliasList.size(); count++)
    {
      AliasVO aliasVOCheck = (AliasVO)aliasList.get(count);
      if (aliasVOCheck.getName().equalsIgnoreCase(aliasVO.getName()))
      {
        exists = true;
        break;
      }
    }
    return exists;
  }

  public int getAliasIndex(AliasVO aliasVO)
  {
    int index = -1;
    for (int count = 0; count < aliasList.size(); count++)
    {
      AliasVO aliasVOCheck = (AliasVO)aliasList.get(count);
      if (aliasVOCheck.getName().equalsIgnoreCase(aliasVO.getName()))
      {
        index = count;
        break;
      }
    }
    return index;
  }

  public void setAliasDBObjectTreeWidth(String name, int width)
  {
    AliasVO value = null;
    for (Iterator it = aliasList.iterator(); it.hasNext();)
    {
      AliasVO aliasVO = (AliasVO)it.next();
      if (aliasVO.getName().equals(name))
      {
        aliasVO.setDBObjectTreeWidth(width);
        break;
      }
    }
  }

  public AliasVO getAlias(String name)
  {
    AliasVO value = null;
    for (Iterator it = aliasList.iterator(); it.hasNext();)
    {
      AliasVO aliasVO = (AliasVO)it.next();
      if (aliasVO.getName().equals(name))
      {
        value = new AliasVO(aliasVO);
        break;
      }
    }
    return value;
  }

  public AliasVO getAlias(int index)
  {
    return new AliasVO((AliasVO)aliasList.get(index));
  }

  public int getAliasCount()
  {
    return aliasList.size();
  }

  public void addAlias(AliasVO aliasVO)
  {
    aliasList.add(aliasVO);
    Collections.sort(aliasList);

    fireAliasAdd(aliasVO.getName());
  }

  public void updateAlias(AliasVO aliasVOOld, AliasVO aliasVONew)
  {
    for (Iterator it = aliasList.iterator(); it.hasNext();)
    {
      AliasVO aliasVOCheck = (AliasVO)it.next();
      if (aliasVOCheck.getName().equalsIgnoreCase(aliasVOOld.getName()))
      {
        it.remove();
        break;
      }
    }

    aliasList.add(aliasVONew);
    Collections.sort(aliasList);

    fireAliasChange(aliasVOOld.getName(), aliasVONew.getName());
    updateQueryHistoryAndFavoritesAliasName(aliasVOOld.getName(), aliasVONew.getName());
  }

  public void removeAlias(AliasVO aliasVO)
  {
    for (Iterator it = aliasList.iterator(); it.hasNext();)
    {
      AliasVO aliasVOCheck = (AliasVO)it.next();
      if (aliasVOCheck.getName().equalsIgnoreCase(aliasVO.getName()))
      {
        it.remove();
        break;
      }
    }

    fireAliasRemoved(aliasVO.getName());
    removeQueryHistoryAndFavoritesAlias(aliasVO.getName());
  }

  /** Fast & simple file copy. */
  public static void copy(File source, File dest) throws IOException
  {
    FileChannel in = null, out = null;
    try
    {
      in = new FileInputStream(source).getChannel();
      out = new FileOutputStream(dest).getChannel();
      in.transferTo(0, in.size(), out);
    }
    finally
    {
      if (in != null)
        in.close();
      if (out != null)
        out.close();
    }
  }


  private void backupFile(File file) throws IOException
  {
    File backupDir = new File(appDir, "backup");
    if (!backupDir.exists())
      backupDir.mkdirs();

    File backupFile = new File(backupDir, file.getName());
    if (!backupFile.exists()
        || backupFile.length() != file.length())
    {
      int maxfiles = 9;
      for (int i = maxfiles; i > 0; i--)
      {
        File backup = new File(backupDir, file.getName() + "." + i);
        if (backup.exists() && i == maxfiles)
          backup.delete();
        else
          backup.renameTo(new File(backupDir, file.getName() + "." + (i + 1)));
      }

      if (backupFile.exists())
        backupFile.renameTo(new File(backupDir, file.getName() + ".1"));

      copy(file, backupFile);
    }

  }

  public void load() throws Exception
  {
    loadGeneral();
    loadHistory();
    loadFavorites();
    //checkDates();
  }

  public void loadGeneral() throws Exception
  {
    if (dataFile.exists())
    {
      backupFile(dataFile);
    }

    if (dataFile.exists())
    {
      BufferedReader br = new BufferedReader(new FileReader(dataFile));
      String line = null;
      StringBuffer sbuf = new StringBuffer();
      while ((line = br.readLine()) != null)
      {
        sbuf.append(line);
      }
      br.close();

      Document doc = DocumentHelper.parseText(sbuf.toString());

      List lst = doc.selectNodes("/stella/connectioncollection/connection");
      for (Iterator it = lst.iterator(); it.hasNext();)
      {
        Node node = (Node)it.next();
        AliasVO aliasVO = new AliasVO();
        aliasVO.setXML(node);

        aliasList.add(aliasVO);
      }

      Collections.sort(aliasList);

      loadDrivers(doc);
      if (driverList.size() == 0)
        loadDefaultDrivers();

      // windows settings
      Node node = doc.selectSingleNode("/stella/window");
      if (node != null)
      {
        maximized = XMLUtils.getNodeAsBoolean(node, node.getUniquePath() + "/maximized");
        positionX = XMLUtils.getNodeAsInt(node, node.getUniquePath() + "/positionX");
        positionY = XMLUtils.getNodeAsInt(node, node.getUniquePath() + "/positionY");
        width = XMLUtils.getNodeAsInt(node, node.getUniquePath() + "/width");
        height = XMLUtils.getNodeAsInt(node, node.getUniquePath() + "/height");
      }

      // state settings
      node = doc.selectSingleNode("/stella/state");
      if (node != null)
      {
        selectedAlias = XMLUtils.getNodeText(node, node.getUniquePath() + "/selectedconnection");
      }

      // font settings
      Display display = Display.getDefault();
      node = doc.selectSingleNode("/stella/fonts/general");
      if (node != null)
      {
        generalFont = getFontXML(display, node);
      }
      node = doc.selectSingleNode("/stella/fonts/querytext");
      if (node != null)
      {
        queryTextFont = getFontXML(display, node);
        queryTextFontSet = true;
      }
      node = doc.selectSingleNode("/stella/fonts/results");
      if (node != null)
      {
        resultsFont = getFontXML(display, node);
        resultsFontSet = true;
      }
      node = doc.selectSingleNode("/stella/fonts/tree");
      if (node != null)
      {
        treeFont = getFontXML(display, node);
        treeFontSet = true;
      }

      // syntax settings
      getSyntax(doc, node, "linecomment", TokenScanner.LINECOMMENT);
      getSyntax(doc, node, "blockcomment", TokenScanner.BLOCKCOMMENT);
      getSyntax(doc, node, "string", TokenScanner.STRING);
      getSyntax(doc, node, "number", TokenScanner.NUMBER);
      getSyntax(doc, node, "keyword", TokenScanner.KEYWORD);
      getSyntax(doc, node, "operator", TokenScanner.OPERATOR);
      getSyntax(doc, node, "queryseparator", TokenScanner.SEPARATOR);

      // sql settings
      node = doc.selectSingleNode("/stella/sql");
      if (node != null)
      {
        autoCommit = XMLUtils.getNodeAsBoolean(node, node.getUniquePath() + "/autocommit");
        limitResults = XMLUtils.getNodeAsBoolean(node, node.getUniquePath() + "/limitresults");
        maxRows = XMLUtils.getNodeAsInt(node, node.getUniquePath() + "/limitresultsmaxrows");
        maxQueryHistory  = XMLUtils.getNodeAsInt(node, node.getUniquePath() + "/maxqueryhistory", -1);
        if (maxQueryHistory < 0)
          maxQueryHistory = 100;
        querySeparator = XMLUtils.getNodeText(node, node.getUniquePath() + "/queryseparator");
        stripComments = XMLUtils.getNodeAsBoolean(node, node.getUniquePath() + "/stripcomments");
      }
    }
    else
      loadDefaultDrivers();
  }

  public void loadHistory() throws Exception
  {
    if (historyFile.exists())
    {
      backupFile(historyFile);
    }

    if (historyFile.exists())
    {
      BufferedReader br = new BufferedReader(new FileReader(historyFile));
      String line = null;
      StringBuffer sbuf = new StringBuffer();
      while ((line = br.readLine()) != null)
      {
        sbuf.append(line);
      }
      br.close();

      Document doc = DocumentHelper.parseText(sbuf.toString());

      List lst = doc.selectNodes("/stella/connectionhistory");
      for (Iterator it = lst.iterator(); it.hasNext();)
      {
        Node node = (Node)it.next();
        String aliasName = node.valueOf("@name");

        List queryList = new LinkedList();
        queryHistoryMap.put(aliasName, queryList);

        List queryNodesList = node.selectNodes("query");
        for (Iterator queryIt = queryNodesList.iterator(); queryIt.hasNext();)
        {
          Node queryNode = (Node)queryIt.next();

          String query = XMLUtils.getNodeText(queryNode, queryNode.getUniquePath());
          query = query.replaceAll("&#xA;", "\n");
          long time = queryNode.numberValueOf("@time").longValue();
          QueryHistoryItem qhi = new QueryHistoryItem(aliasName, time, query);
          queryList.add(qhi);
        }

        Collections.sort(queryList, new QueryHistoryComparator(QueryHistoryComparator.ASCENDING));
      }
    }
  }

  public void loadFavorites() throws Exception
  {
    if (favoriteFile.exists())
    {
      backupFile(favoriteFile);
    }

    if (favoriteFile.exists())
    {
      BufferedReader br = new BufferedReader(new FileReader(favoriteFile));
      String line = null;
      StringBuffer sbuf = new StringBuffer();
      while ((line = br.readLine()) != null)
      {
        sbuf.append(line);
      }
      br.close();

      Document doc = DocumentHelper.parseText(sbuf.toString());

      // old --------------------------------------------------------------------------
      List lst = doc.selectNodes("/stella/favoritequeries");
      for (Iterator it = lst.iterator(); it.hasNext();)
      {
        Node node = (Node)it.next();
        String aliasName = node.valueOf("@name");

        List queryList = new LinkedList();
        queryFavoriteMap.put(aliasName, queryList);

        List queryNodesList = node.selectNodes("favorite");
        for (Iterator queryIt = queryNodesList.iterator(); queryIt.hasNext();)
        {
          Node favoriteNode = (Node)queryIt.next();

          Node descriptionNode = favoriteNode.selectSingleNode("description");
          String description = XMLUtils.getNodeText(descriptionNode, descriptionNode.getUniquePath());

          Node queryNode = favoriteNode.selectSingleNode("query");
          String query = XMLUtils.getNodeText(queryNode, queryNode.getUniquePath());
          query = query.replaceAll("&#xA;", "\n");

          QueryFavoriteItem qfi = new QueryFavoriteItem(aliasName, description, query);
          queryList.add(qfi);


          QueryFavoriteItem2 qfi2 = new QueryFavoriteItem2(description, query);
          favoritesRootFolder.addChild(qfi2);
        }
      }



      // new --------------------------------------------------------------------------
      lst = doc.selectNodes("/stella/favorites/folder");

      // read folder names and ids
      Map favoriteFoldersMap = new HashMap();
      favoriteFoldersMap.put(new Integer(0), favoritesRootFolder);
      for (Iterator it = lst.iterator(); it.hasNext();)
      {
        Node node = (Node)it.next();
        String name = node.valueOf("@name");
        int id = Integer.parseInt(node.valueOf("@id"));
        int order = Integer.parseInt(node.valueOf("@order"));
        QueryFavoriteFolder qff = new QueryFavoriteFolder(id, name);
        qff.setOrder(order);
        favoriteFoldersMap.put(new Integer(id), qff);
      }
      // link up folder parent/children
      for (Iterator it = lst.iterator(); it.hasNext();)
      {
        Node node = (Node)it.next();
        String parent = node.valueOf("@parent");
        int parentId = parent.length() > 0 ? Integer.parseInt(parent) : 0;
        int id = Integer.parseInt(node.valueOf("@id"));
        QueryFavoriteFolder qffParent = (QueryFavoriteFolder)favoriteFoldersMap.get(new Integer(parentId));
        QueryFavoriteFolder qff = (QueryFavoriteFolder)favoriteFoldersMap.get(new Integer(id));
        qffParent.addChild(qff);
      }
      // read in favorites
      lst = doc.selectNodes("/stella/favorites/favorite");
      for (Iterator queryIt = lst.iterator(); queryIt.hasNext();)
      {
        Node node = (Node)queryIt.next();

        Node descriptionNode = node.selectSingleNode("description");
        String description = XMLUtils.getNodeText(descriptionNode, descriptionNode.getUniquePath());

        Node queryNode = node.selectSingleNode("query");
        String query = XMLUtils.getNodeText(queryNode, queryNode.getUniquePath());
        query = query.replaceAll("&#xA;", "\n");

        QueryFavoriteItem2 qfi = new QueryFavoriteItem2(description, query);
        String parent = node.valueOf("@parent");
        int order = Integer.parseInt(node.valueOf("@order"));
        qfi.setOrder(order);
        int parentId = parent.length() > 0 ? Integer.parseInt(parent) : 0;
        QueryFavoriteFolder qffParent = (QueryFavoriteFolder)favoriteFoldersMap.get(new Integer(parentId));
        qffParent.addChild(qfi);
      }

      // compress the ids (fill in gaps to prevent an ever incrementing situation)
      int id = 1;
      for (Iterator it = favoriteFoldersMap.values().iterator(); it.hasNext();)
      {
        QueryFavoriteFolder qff = (QueryFavoriteFolder)it.next();
        if (qff.getId() != 0)
          qff.setId(id++);
      }
      QueryFavoriteFolder.setNextId(id);

      sortFavorites(favoritesRootFolder);
    }
  }

  private void sortFavorites(QueryFavoriteFolder qff)
  {
    Collections.sort(qff.getChildren(), QueryFavoriteObject.getOrderComparator());
    for (Iterator it = qff.getChildren().iterator(); it.hasNext();)
    {
      QueryFavoriteObject qfo = (QueryFavoriteObject)it.next();
      if (qfo instanceof QueryFavoriteFolder)
        sortFavorites((QueryFavoriteFolder)qfo);
    }
  }

  private void getSyntax(Document doc, Node node, String elemName, String tokenType)
  {
    SyntaxContainer syntaxCon = null;
    node = doc.selectSingleNode("/stella/syntax/" + elemName);
    if (node != null)
      syntaxCon = getSyntaxXML(Display.getCurrent(), node);
    else
      syntaxCon = new SyntaxContainer(tokenType);
    setSyntax(tokenType, syntaxCon);
  }

  public void setSyntax(Object key, SyntaxContainer syntaxCon)
  {
    SyntaxContainer old = (SyntaxContainer)syntaxMap.get(key);
    if (old != null)
      old.dispose();
    syntaxMap.put(key, syntaxCon);
  }

  private void loadDrivers(Document doc)
  {
    List lst = doc.selectNodes("/stella/drivercollection/driver");
    for (Iterator it = lst.iterator(); it.hasNext();)
    {
      Node node = (Node)it.next();
      DriverVO driverVO = new DriverVO();
      driverVO.setXML(node);
      if (!driverList.contains(driverVO))
        driverList.add(driverVO);
    }

    Collections.sort(driverList);
  }

  public void loadDefaultDrivers() throws IOException, DocumentException
  {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("drivers.xml");
    if (is == null)
    {
      logger.error("drivers.xml file not found");
    }
    else
    {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line = null;
      StringBuffer sbuf = new StringBuffer();
      while ((line = br.readLine()) != null)
      {
        sbuf.append(line);
      }
      br.close();
  
      Document doc = DocumentHelper.parseText(sbuf.toString());
  
      loadDrivers(doc);
    }

  }

  public void save() throws IOException
  {
    saveGeneral();
    saveHistory();
    saveFavorites();
  }

  public void saveGeneral() throws IOException
  {
    if (!appDir.exists())
      appDir.mkdirs();

    Document doc = DocumentHelper.createDocument();
    Element rootElem = DocumentHelper.createElement("stella");
    doc.add(rootElem);

    Element elem = DocumentHelper.createElement("version");
    elem.addText(VERSION);
    rootElem.add(elem);

    Element aliasElem = DocumentHelper.createElement("connectioncollection");
    rootElem.add(aliasElem);
    for (Iterator it = aliasList.iterator(); it.hasNext();)
    {
      AliasVO aliasVO = (AliasVO)it.next();
      aliasElem.add(aliasVO.getXML());
    }

    aliasElem = DocumentHelper.createElement("drivercollection");
    rootElem.add(aliasElem);
    for (Iterator it = driverList.iterator(); it.hasNext();)
    {
      DriverVO driverVO = (DriverVO)it.next();
      aliasElem.add(driverVO.getXML());
    }

    // windows settings
    elem = DocumentHelper.createElement("window");
    rootElem.add(elem);
    Element item = DocumentHelper.createElement("maximized");
    item.setText(Boolean.toString(maximized));
    elem.add(item);

    item = DocumentHelper.createElement("positionX");
    item.setText("" + positionX);
    elem.add(item);

    item = DocumentHelper.createElement("positionY");
    item.setText("" + positionY);
    elem.add(item);

    item = DocumentHelper.createElement("width");
    item.setText("" + width);
    elem.add(item);

    item = DocumentHelper.createElement("height");
    item.setText("" + height);
    elem.add(item);

    // state settings
    elem = DocumentHelper.createElement("state");
    rootElem.add(elem);
    item = DocumentHelper.createElement("selectedconnection");
    item.setText(selectedAlias);
    elem.add(item);

    // fonts settings
    elem = DocumentHelper.createElement("fonts");
    rootElem.add(elem);

    addFontXML(elem, "general", true, generalFont);
    addFontXML(elem, "querytext", queryTextFontSet, queryTextFont);
    addFontXML(elem, "results", resultsFontSet, resultsFont);
    addFontXML(elem, "tree", treeFontSet, treeFont);

    // syntax settings
    elem = DocumentHelper.createElement("syntax");
    rootElem.add(elem);

    addSyntaxXML(elem, "linecomment", getSyntax(TokenScanner.LINECOMMENT));
    addSyntaxXML(elem, "blockcomment", getSyntax(TokenScanner.BLOCKCOMMENT));
    addSyntaxXML(elem, "string", getSyntax(TokenScanner.STRING));
    addSyntaxXML(elem, "number", getSyntax(TokenScanner.NUMBER));
    addSyntaxXML(elem, "keyword", getSyntax(TokenScanner.KEYWORD));
    addSyntaxXML(elem, "operator", getSyntax(TokenScanner.OPERATOR));
    addSyntaxXML(elem, "queryseparator", getSyntax(TokenScanner.SEPARATOR));


    // sql settings
    elem = DocumentHelper.createElement("sql");
    rootElem.add(elem);

    item = DocumentHelper.createElement("autocommit");
    item.setText("" + autoCommit);
    elem.add(item);

    item = DocumentHelper.createElement("limitresults");
    item.setText("" + limitResults);
    elem.add(item);

    item = DocumentHelper.createElement("limitresultsmaxrows");
    item.setText( "" + maxRows);
    elem.add(item);

    item = DocumentHelper.createElement("maxqueryhistory");
    item.setText( "" + maxQueryHistory);
    elem.add(item);

    item = DocumentHelper.createElement("queryseparator");
    item.setText(querySeparator);
    elem.add(item);

    item = DocumentHelper.createElement("stripcomments");
    item.setText("" + stripComments);
    elem.add(item);


    XMLWriter xmlWriter = new XMLWriter(new FileWriter(dataFile), OutputFormat.createPrettyPrint());
    xmlWriter.write(doc);
    xmlWriter.close();
  }

  public void saveHistory() throws IOException
  {
    if (!appDir.exists())
      appDir.mkdirs();

    // history file
    Document doc = DocumentHelper.createDocument();
    Element rootElem = DocumentHelper.createElement("stella");
    doc.add(rootElem);

    Element elem = DocumentHelper.createElement("version");
    elem.addText(VERSION);
    rootElem.add(elem);

    Set aliasNameSet = new HashSet();
    for (Iterator it = aliasList.iterator(); it.hasNext();)
    {
      AliasVO aliasVO = (AliasVO)it.next();
      aliasNameSet.add(aliasVO.getName());
    }

    for (Iterator it = queryHistoryMap.keySet().iterator(); it.hasNext();)
    {
      String aliasName = (String)it.next();
      if (aliasNameSet.contains(aliasName))
      {
        List items = (List)queryHistoryMap.get(aliasName);
        if (items != null && items.size() > 0)
        {
          Element aliasElem = DocumentHelper.createElement("connectionhistory");
          aliasElem.add(DocumentHelper.createAttribute(aliasElem, "name", aliasName));
          rootElem.add(aliasElem);

          for (Iterator itItems = items.iterator(); itItems.hasNext();)
          {
            QueryHistoryItem qhi = (QueryHistoryItem)itItems.next();
            Element queryElem = DocumentHelper.createElement("query");
            CDATA cdata = DocumentHelper.createCDATA(qhi.getQuery().replaceAll("\n", "&#xA;"));
            queryElem.add(cdata);
            queryElem.add(DocumentHelper.createAttribute(queryElem, "time", "" + qhi.getTime()));
            aliasElem.add(queryElem);
          }
        }
      }
    }

    OutputFormat of = OutputFormat.createPrettyPrint();
    XMLWriter xmlWriter = new XMLWriter(new FileWriter(historyFile), of);
    xmlWriter.write(doc);
    xmlWriter.close();
  }

  public void saveFavorites() throws IOException
  {
    if (!appDir.exists())
      appDir.mkdirs();

    // favorites file
    Document doc = DocumentHelper.createDocument();
    Element rootElem = DocumentHelper.createElement("stella");
    doc.add(rootElem);

    Element elem = DocumentHelper.createElement("version");
    elem.addText(VERSION);
    rootElem.add(elem);

    // old ---------------------------------------------------------------------
    /*
    for (Iterator it = queryFavoriteMap.keySet().iterator(); it.hasNext();)
    {
      String aliasName = (String)it.next();

      if (aliasNameSet.contains(aliasName)
        || aliasName.length() == 0)
      {
        List items = (List)queryFavoriteMap.get(aliasName);
        if (items != null && items.size() > 0)
        {
          aliasElem = DocumentHelper.createElement("favoritequeries");
          aliasElem.add(DocumentHelper.createAttribute(aliasElem, "name", aliasName));
          rootElem.add(aliasElem);

          for (Iterator itItems = items.iterator(); itItems.hasNext();)
          {
            QueryFavoriteItem qfi = (QueryFavoriteItem)itItems.next();
            Element favoriteElem = DocumentHelper.createElement("favorite");
            aliasElem.add(favoriteElem);

            Element descriptionElem = DocumentHelper.createElement("description");
            favoriteElem.add(descriptionElem);
            descriptionElem.addText(qfi.getDescription());

            Element queryElem = DocumentHelper.createElement("query");
            favoriteElem.add(queryElem);
            CDATA cdata = DocumentHelper.createCDATA(qfi.getQuery().replaceAll("\n", "&#xA;"));
            queryElem.add(cdata);
          }
        }
      }
    }
    */

    // new -----------------------------------------------------------------------------
    Element favoritesElem = DocumentHelper.createElement("favorites");
    rootElem.add(favoritesElem);

    addFavoritesFolderXML(favoritesElem, favoritesRootFolder.getChildren());
    addFavoritesXML(favoritesElem, favoritesRootFolder);

    OutputFormat of = OutputFormat.createPrettyPrint();
    XMLWriter xmlWriter = new XMLWriter(new FileWriter(favoriteFile), of);
    xmlWriter.write(doc);
    xmlWriter.close();
  }

  private void addFavoritesFolderXML(Element root, List children)
  {
    for (int index = 0; index < children.size(); index++)
    {
      QueryFavoriteObject qfo = (QueryFavoriteObject)children.get(index);
      if (qfo instanceof QueryFavoriteFolder)
      {
        QueryFavoriteFolder qff = (QueryFavoriteFolder)qfo;
        Element folderElem = DocumentHelper.createElement("folder");
        root.add(folderElem);
        folderElem.add(DocumentHelper.createAttribute(folderElem, "name", qff.getName()));
        folderElem.add(DocumentHelper.createAttribute(folderElem, "id", "" + qff.getId()));
        folderElem.add(DocumentHelper.createAttribute(folderElem, "parent", "" + qff.getParent().getId()));
        folderElem.add(DocumentHelper.createAttribute(folderElem, "order", "" + index));

        addFavoritesFolderXML(root, qff.getChildren());
      }
    }
  }

  private void addFavoritesXML(Element root, QueryFavoriteFolder qff)
  {
    List children = qff.getChildren();
    for (int index = 0; index < children.size(); index++)
    {
      QueryFavoriteObject qfo = (QueryFavoriteObject)children.get(index);
      if (qfo instanceof QueryFavoriteItem2)
      {
        QueryFavoriteItem2 qfi = (QueryFavoriteItem2)qfo;
        Element favoriteElem = DocumentHelper.createElement("favorite");
        root.add(favoriteElem);
        favoriteElem.add(DocumentHelper.createAttribute(favoriteElem, "parent", "" + qff.getId()));
        favoriteElem.add(DocumentHelper.createAttribute(favoriteElem, "order", "" + index));
        Element descriptionElem = DocumentHelper.createElement("description");
        favoriteElem.add(descriptionElem);
        descriptionElem.addText(qfi.getName());

        Element queryElem = DocumentHelper.createElement("query");
        favoriteElem.add(queryElem);
        CDATA cdata = DocumentHelper.createCDATA(qfi.getQuery().replaceAll("\n", "&#xA;"));
        queryElem.add(cdata);
      }
    }

    for (Iterator it = children.iterator(); it.hasNext();)
    {
      QueryFavoriteObject qfo = (QueryFavoriteObject)it.next();
      if (qfo instanceof QueryFavoriteFolder)
      {
        addFavoritesXML(root, (QueryFavoriteFolder)qfo);
      }

    }
  }


  private FontContainer getFontXML(Display display, Node fontNode)
  {
    Color color = null;
    Font font = null;

    Node colorNode = fontNode.selectSingleNode("color");
    if (colorNode != null)
    {
      int red = colorNode.numberValueOf("@red").intValue();
      int green = colorNode.numberValueOf("@green").intValue();
      int blue = colorNode.numberValueOf("@blue").intValue();
      color = new Color(display, red, green, blue);
    }

    List lst = fontNode.selectNodes("fontdata");
    List fontDataList = new ArrayList();
    for (Iterator it = lst.iterator(); it.hasNext();)
    {
      Node node = (Node)it.next();
      String name = node.valueOf("@name");
      int height = node.numberValueOf("@height").intValue();
      int style = SWT.NORMAL;
      if (node.valueOf("@bold") != null && node.valueOf("@bold").equals("true"))
        style = style | SWT.BOLD;
      if (node.valueOf("@italic") != null && node.valueOf("@italic").equals("true"))
        style = style | SWT.ITALIC;

      FontData fd = new FontData(name, height, style);
      fontDataList.add(fd);
    }

    if (fontDataList.size() > 0)
    {
      FontData[] fdArray = new FontData[fontDataList.size()];
      for (int index = 0; index < fontDataList.size(); index++)
      {
        fdArray[index] = (FontData)fontDataList.get(index);
      }
      font = new Font(display, fdArray);
    }

    if (font == null && color == null)
      return null;

    return new FontContainer(font, color);
  }

  private void addFontXML(Element root, String name, boolean set, FontContainer fontCon)
  {
    if (set)
    {
      Element elem = DocumentHelper.createElement(name);
      root.add(elem);

      if (fontCon != null && fontCon.getColor() != null)
      {
        Element item = DocumentHelper.createElement("color");
        elem.add(item);

        item.add(DocumentHelper.createAttribute(item, "red", "" + fontCon.getColor().getRed()));
        item.add(DocumentHelper.createAttribute(item, "green", "" + fontCon.getColor().getGreen()));
        item.add(DocumentHelper.createAttribute(item, "blue", "" + fontCon.getColor().getBlue()));
      }

      if (fontCon != null && fontCon.getFont() != null)
      {
        FontData[] dataArray = fontCon.getFont().getFontData();
        for (int index = 0; index < dataArray.length; index++)
        {
          Element item = DocumentHelper.createElement("fontdata");
          elem.add(item);

          item.add(DocumentHelper.createAttribute(item, "name", dataArray[index].getName()));
          item.add(DocumentHelper.createAttribute(item, "height", "" + dataArray[index].getHeight()));

          if ((dataArray[index].getStyle() & SWT.BOLD) > 0)
          {
            item.add(DocumentHelper.createAttribute(item, "bold", "true"));
          }
          if ((dataArray[index].getStyle() & SWT.ITALIC) > 0)
          {
            item.add(DocumentHelper.createAttribute(item, "italic", "true"));
          }
        }
      }

    }
  }

  private SyntaxContainer getSyntaxXML(Display display, Node syntaxNode)
  {
    Color color = null;
    boolean bold = false;
    boolean italic = false;

    Node colorNode = syntaxNode.selectSingleNode("color");
    if (colorNode != null)
    {
      int red = colorNode.numberValueOf("@red").intValue();
      int green = colorNode.numberValueOf("@green").intValue();
      int blue = colorNode.numberValueOf("@blue").intValue();
      color = new Color(display, red, green, blue);
    }

    if (syntaxNode.valueOf("@bold") != null && syntaxNode.valueOf("@bold").equals("true"))
      bold = true;
    if (syntaxNode.valueOf("@italic") != null && syntaxNode.valueOf("@italic").equals("true"))
      italic = true;

    return new SyntaxContainer(color, bold, italic);
  }

  private void addSyntaxXML(Element root, String name, SyntaxContainer syntaxCon)
  {
    if (syntaxCon != null)
    {
      Element elem = DocumentHelper.createElement(name);
      root.add(elem);

      if (syntaxCon.getColor() != null)
      {
        Element item = DocumentHelper.createElement("color");
        elem.add(item);

        item.add(DocumentHelper.createAttribute(item, "red", "" + syntaxCon.getColor().getRed()));
        item.add(DocumentHelper.createAttribute(item, "green", "" + syntaxCon.getColor().getGreen()));
        item.add(DocumentHelper.createAttribute(item, "blue", "" + syntaxCon.getColor().getBlue()));
      }

      if (syntaxCon.getBold())
        elem.add(DocumentHelper.createAttribute(elem, "bold", "true"));
      if (syntaxCon.getItalic())
        elem.add(DocumentHelper.createAttribute(elem, "italic", "true"));
    }
  }

  private void removeQueryHistoryAndFavoritesAlias(String aliasName)
  {
    queryHistoryMap.remove(aliasName);
    queryFavoriteMap.remove(aliasName);
  }

  private void updateQueryHistoryAndFavoritesAliasName(String oldName, String newName)
  {
    List queryList = (List)queryHistoryMap.remove(oldName);
    if (queryList != null)
    {
      queryHistoryMap.put(newName, queryList);
      for (Iterator it = queryList.iterator(); it.hasNext();)
      {
        QueryHistoryItem qhi = (QueryHistoryItem)it.next();
        qhi.setAliasName(newName);
      }
    }

    queryList = (List)queryFavoriteMap.remove(oldName);
    if (queryList != null)
    {
      queryFavoriteMap.put(newName, queryList);
      for (Iterator it = queryList.iterator(); it.hasNext();)
      {
        QueryFavoriteItem qfi = (QueryFavoriteItem)it.next();
        qfi.setAliasName(newName);
      }
    }

    /*
    List listenerList = (List)queryHistoryListenerMap.remove(oldName);
    if (listenerList != null)
      queryHistoryListenerMap.put(newName, listenerList);
   */
  }

  public void addQueryFavorite(QueryFavoriteItem qfi)
  {
    List queryList = (List)queryFavoriteMap.get(qfi.getAliasName());
    if (queryList == null)
    {
      queryList = new LinkedList();
      queryFavoriteMap.put(qfi.getAliasName(), queryList);
    }

    if (queryList.contains(qfi))
      queryList.remove(qfi);

    queryList.add(qfi);
  }

  public boolean getQueryFavoriteExists(QueryFavoriteItem qfi)
  {
    List queryList = (List)queryFavoriteMap.get(qfi.getAliasName());
    if (queryList == null)
      return false;

    if (queryList.contains(qfi))
      return true;

    return false;
  }

  public void removeQueryFavorite(QueryFavoriteItem qfi)
  {
    List queryList = (List)queryFavoriteMap.get(qfi.getAliasName());
    if (queryList != null)
    {
      queryList.remove(qfi);
    }
  }

  public List getQueryFavorites(String aliasName)
  {
    List queryList = (List)queryFavoriteMap.get(aliasName);
    if (queryList == null)
    {
      queryList = new LinkedList();
      queryFavoriteMap.put(aliasName, queryList);
    }

    return queryList;
  }

  public QueryFavoriteFolder getFavoritesRootFolder()
  {
    return favoritesRootFolder;
  }

  public void removeQueryHistory(QueryHistoryItem qhi)
  {
    List queryList = (List)queryHistoryMap.get(qhi.getAliasName());
    if (queryList != null)
    {
      queryList.remove(qhi);
    }
  }

  public void addQueryHistory(List newQueryList)
  {
    for (Iterator it = newQueryList.iterator(); it.hasNext();)
    {
      QueryHistoryItem qhi = (QueryHistoryItem)it.next();
      List queryList = (List)queryHistoryMap.get(qhi.getAliasName());
      if (queryList == null)
      {
        queryList = new LinkedList();
        queryHistoryMap.put(qhi.getAliasName(), queryList);
      }

      if (queryList.contains(qhi))
        queryList.remove(qhi);

      queryList.add(qhi);
      while (queryList.size() > maxQueryHistory)
      {
        queryList.remove(0);
      }
    }
  }

  public List getQueryHistory(String aliasName)
  {
    List queryList = (List)queryHistoryMap.get(aliasName);
    if (queryList == null)
    {
      queryList = new LinkedList();
      queryHistoryMap.put(aliasName, queryList);
    }

    return queryList;
  }

  public List getQueryHistory(AliasVO alias)
  {
    return getQueryHistory(alias.getName());
  }

  /*
  public void addQueryHistoryListener(QueryHistoryListener listener, AliasVO alias)
  {
    List listenerList = (List)queryHistoryListenerMap.get(alias.getName());
    if (listenerList == null)
    {
      listenerList = new LinkedList();
      queryHistoryListenerMap.put(alias.getName(), listenerList);
    }

    listenerList.add(listener);
  }
  */

/*
  public void removeQueryHistoryListener(QueryHistoryListener listener, AliasVO alias)
  {
    List listenerList = (List)queryHistoryListenerMap.get(alias.getName());
    if (listenerList != null)
    {
      listenerList.remove(listener);
    }
  }

  private void fireQueryHistoryChange(String name)
  {
    List listenerList = (List)queryHistoryListenerMap.get(name);
    for (Iterator it = listenerList.iterator(); it.hasNext();)
    {
      QueryHistoryListener listener = (QueryHistoryListener)it.next();
      listener.queryHistoryChanged();
    }
  }
*/


  public void addDriverChangeListener(DriverChangeListener listener)
  {
    driverListenerList.add(listener);
  }

  public void removeDriverChangeListener(DriverChangeListener listener)
  {
    driverListenerList.remove(listener);
  }

  private void fireDriverChanged(String oldName, String newName)
  {
    for (Iterator it = driverListenerList.iterator(); it.hasNext();)
    {
      DriverChangeListener listener = (DriverChangeListener)it.next();
      listener.driverChanged(oldName, newName);
    }
  }

  private void fireDriverAdded(String newName)
  {
    for (Iterator it = driverListenerList.iterator(); it.hasNext();)
    {
      DriverChangeListener listener = (DriverChangeListener)it.next();
      listener.driverAdded(newName);
    }
  }

  private void fireDriverRemoved(String name)
  {
    for (Iterator it = driverListenerList.iterator(); it.hasNext();)
    {
      DriverChangeListener listener = (DriverChangeListener)it.next();
      listener.driverRemoved(name);
    }
  }


  public void addAliasChangeListener(AliasChangeListener listener)
  {
    aliasListenerList.add(listener);
  }

  public void removeAliasChangeListener(AliasChangeListener listener)
  {
    aliasListenerList.remove(listener);
  }

  public void addFontChangeListener(FontChangeListener listener)
  {
    fontChangeListenerList.add(listener);
  }

  public void removeFontChangeListener(FontChangeListener listener)
  {
    fontChangeListenerList.remove(listener);
  }

  public void addSyntaxListener(SyntaxListener listener)
  {
    syntaxListenerList.add(listener);
  }

  public void removeSyntaxListener(SyntaxListener listener)
  {
    syntaxListenerList.remove(listener);
  }

  private void fireAliasChange(String oldName, String newName)
  {
    for (Iterator it = aliasListenerList.iterator(); it.hasNext();)
    {
      AliasChangeListener listener = (AliasChangeListener)it.next();
      listener.connectionChanged(oldName, newName);
    }
  }

  private void fireAliasAdd(String name)
  {
    for (Iterator it = aliasListenerList.iterator(); it.hasNext();)
    {
      AliasChangeListener listener = (AliasChangeListener)it.next();
      listener.connectionAdded(name);
    }
  }

  private void fireAliasRemoved(String name)
  {
    for (Iterator it = aliasListenerList.iterator(); it.hasNext();)
    {
      AliasChangeListener listener = (AliasChangeListener)it.next();
      listener.connectionRemoved(name);
    }
  }

  private void fireSyntaxChange()
  {
    for (Iterator it = syntaxListenerList.iterator(); it.hasNext();)
    {
      SyntaxListener listener = (SyntaxListener)it.next();
      listener.syntaxChanged();
    }
  }

  private void fireFontChange()
  {
    for (Iterator it = fontChangeListenerList.iterator(); it.hasNext();)
    {
      FontChangeListener listener = (FontChangeListener)it.next();
      listener.fontChanged();
    }

    for (Iterator it = disposeFonts.iterator(); it.hasNext();)
    {
      FontContainer fontCon = (FontContainer)it.next();
      fontCon.dispose();
    }
  }

  public void updateSyntax()
  {
    SyntaxHighlighter.initializeColors();
    fireSyntaxChange();
  }

  public void updateFonts()
  {
    fireFontChange();
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
  }

  public boolean isMaximized()
  {
    return maximized;
  }

  public void setMaximized(boolean maximized)
  {
    this.maximized = maximized;
  }

  public int getPositionX()
  {
    return positionX;
  }

  public void setPositionX(int positionX)
  {
    this.positionX = positionX;
  }

  public int getPositionY()
  {
    return positionY;
  }

  public void setPositionY(int positionY)
  {
    this.positionY = positionY;
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
  }

  public void setSelectedAlias(String aliasName)
  {
    selectedAlias = aliasName;
  }

  public String getSelectedAlias()
  {
    return selectedAlias;
  }

  public FontContainer getGeneralFont()
  {
    return generalFont;
  }

  public void setGeneralFont(FontContainer fontCon)
  {
    if (generalFont != null && generalFont != fontCon)
      disposeFonts.add(generalFont);
    generalFont = fontCon;
  }

  public FontContainer getQueryTextFont()
  {
    if (!queryTextFontSet)
      return generalFont;
    else
      return queryTextFont;
  }

  public void setQueryTextFont(FontContainer fontCon)
  {
    if (queryTextFont != null && queryTextFont != fontCon)
      disposeFonts.add(queryTextFont);
    queryTextFont = fontCon;
  }

  public void setResultsFont(FontContainer fontCon)
  {
    if (resultsFont != null && resultsFont != fontCon)
      disposeFonts.add(resultsFont);
    resultsFont = fontCon;
  }

  public FontContainer getResultsFont()
  {
    if (!resultsFontSet)
      return generalFont;
    else
      return resultsFont;
  }

  public FontContainer getTreeFont()
  {
    if (!treeFontSet)
      return generalFont;
    else
      return treeFont;
  }

  public void setTreeFont(FontContainer fontCon)
  {
    if (treeFont != null && treeFont != fontCon)
      disposeFonts.add(treeFont);
    treeFont = fontCon;
  }

  public void setQueryTextFontSet(boolean set)
  {
    queryTextFontSet = set;
  }

  public void setResultsFontSet(boolean set)
  {
    resultsFontSet = set;
  }

  public void setTreeFontSet(boolean set)
  {
    treeFontSet = set;
  }

  public boolean isQueryTextFontSet()
  {
    return queryTextFontSet;
  }

  public boolean isResultsFontSet()
  {
    return resultsFontSet;
  }

  public boolean isTreeFontSet()
  {
    return treeFontSet;
  }

  public SyntaxContainer getSyntax(Object key)
  {
    SyntaxContainer con = (SyntaxContainer)syntaxMap.get(key);

    if (con == null)
    {
      con = new SyntaxContainer(key);
      syntaxMap.put(key, con);
    }

    return con;
  }

  public int getMaxRows()
  {
    return maxRows;
  }

  public void setMaxRows(int rows)
  {
    maxRows = rows;
  }

  public int getMaxQueryHistory()
  {
    return maxQueryHistory;
  }

  public void setMaxQueryHistory(int maxQueryHistory)
  {
    setMaxQueryHistory(maxQueryHistory, false);
  }

  public void setMaxQueryHistory(int maxQueryHistory, boolean trimIfLess)
  {
    if (maxQueryHistory < this.maxQueryHistory)
    {
      for (Iterator it = queryHistoryMap.values().iterator(); it.hasNext();)
      {
        List queryList = (List)it.next();
        while (queryList.size() > maxQueryHistory)
        {
          queryList.remove(0);
        }
      }
    }
    this.maxQueryHistory = maxQueryHistory;
  }

  public boolean getLimitResults()
  {
    return limitResults;
  }

  public void setLimitResults(boolean limit)
  {
    limitResults = limit;
  }

  public boolean getStripComments()
  {
    return stripComments;
  }

  public void setStripComments(boolean stripComments)
  {
    this.stripComments = stripComments;
  }

  public boolean getAutoCommit()
  {
    return autoCommit;
  }

  public void setAutoCommit(boolean autoCommit)
  {
    this.autoCommit = autoCommit;
  }

  public String getQuerySeparator()
  {
    return querySeparator;
  }

  public void setQuerySeparator(String querySeparator)
  {
    this.querySeparator = querySeparator;
  }



}
