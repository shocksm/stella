package org.stellasql.stella.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stellasql.stella.connection.ConnectionManager;


public class QueryParser
{
  private final static Logger logger = LogManager.getLogger(QueryParser.class);

  public final static String QUERY_TYPE_UNKNOWN = "UNKNOWN";
  public final static String QUERY_TYPE_SELECT = "SELECT";
  public final static String QUERY_TYPE_SELECT_INTO = "SELECT INTO";
  public final static String QUERY_TYPE_INSERT = "INSERT";
  public final static String QUERY_TYPE_UPDATE = "UPDATE";
  public final static String QUERY_TYPE_DELETE = "DELETE";
  public final static String QUERY_TYPE_CREATE = "CREATE";
  public final static String QUERY_TYPE_ALTER = "ALTER";
  public final static String QUERY_TYPE_DROP = "DROP";
  public final static String QUERY_TYPE_COMMIT = "COMMIT";
  public final static String QUERY_TYPE_ROLLBACK = "ROLLBACK";
  public final static String QUERY_TYPE_EMPTY = "EMPTY";

  private TokenScanner tokenScanner = null;
  private ArrayList tokenList = null;
  private ArrayList tokenListWithWhiteSpace = null;
  private String schemaSeparator = null;
  private String identifierQuote = null;
  private boolean useSchema = true;
  private List tables = null;
  private List columns = null;

  private String queryType = QUERY_TYPE_UNKNOWN;

  public QueryParser(String schemaSeperator, String identifierQuote, boolean useSchema)
  {
    tokenScanner = new TokenScanner();
    tokenList = new ArrayList();
    tokenListWithWhiteSpace = new ArrayList();
    schemaSeparator = schemaSeperator;
    this.identifierQuote = identifierQuote;
    this.useSchema = useSchema;
  }

  public QueryParser(ConnectionManager conManager)
  {
    tokenScanner = new TokenScanner();
    tokenList = new ArrayList();
    tokenListWithWhiteSpace = new ArrayList();
    schemaSeparator = conManager.getSeparator();
    identifierQuote = conManager.getIdentifierQuote();
    useSchema = conManager.getUseSchemas();
  }

  public String getQueryType()
  {
    return queryType;
  }

  public List getTables()
  {
    return tables;
  }

  public List getColumns()
  {
    return columns;
  }

  public void clear()
  {
    tokenList.clear();
    tokenListWithWhiteSpace.clear();
    tables = null;
    columns = null;
    queryType = QUERY_TYPE_UNKNOWN;
  }

  public void parseQuery(String query)
  {
    tokenList.clear();
    tokenListWithWhiteSpace.clear();

    tokenScanner.setText(query);
    Object tokenType = null;

    while ((tokenType = tokenScanner.nextToken()) != null)
    {
        int start = tokenScanner.getStartPosition();
        int end = tokenScanner.getEndPosition();
        String text = query.substring(start, end+1);

        if (tokenType == TokenScanner.WHITESPACE)
        {
          tokenListWithWhiteSpace.add(new Token(new Location(start, end), tokenType, text));
          continue;
        }

        if (text.toLowerCase().equals("by") && tokenList.size() > 0)
        {
          Token lastToken = (Token)tokenList.get(tokenList.size() - 1);
          if (lastToken.text.toLowerCase().equals("group"))
          {
            lastToken.text = lastToken.text + " " + text;
            lastToken.loc = new Location(lastToken.loc.start, end);
            continue;
          }
        }

        if (text.equals(identifierQuote))
        {
          int first = start;
          StringBuffer sbuf = new StringBuffer(text);
          while ((tokenType = tokenScanner.nextToken()) != null)
          {
            start = tokenScanner.getStartPosition();
            end = tokenScanner.getEndPosition();
            text = query.substring(start, end+1);
            sbuf.append(text);
            if (text.equals(identifierQuote))
            {
              break;
            }
          }

          Token token = new Token(new Location(first, end), TokenScanner.UNKNOWN, sbuf.toString());
          tokenList.add(token);
          tokenListWithWhiteSpace.add(token);
        }
        else
        {
          Token token = new Token(new Location(start, end), tokenType, text);
          tokenList.add(token);
          tokenListWithWhiteSpace.add(token);
        }
    }
    tokenScanner.clear();

    for (Iterator it = tokenList.iterator(); it.hasNext();)
    {
      Token token = (Token)it.next();
      logger.debug("Token: " + token);
    }

    if (checkEmpty())
    {
      queryType = QUERY_TYPE_EMPTY;
    }
    else
    {
      determineQueryType();
      if (queryType == QUERY_TYPE_SELECT)
        processSelect();
    }
  }

  private boolean checkEmpty()
  {
    boolean empty = true;

    for (Iterator it = tokenList.iterator(); it.hasNext();)
    {
      Token token = (Token)it.next();
      if (!token.isComment())
      {
        empty = false;
        break;
      }
    }

    return empty;
  }

/*
  SELECT [ ALL | DISTINCT [ ON ( expression [, ...] ) ] ]
           * | expression [ AS output_name ] [, ...]
           [ FROM froitem [, ...] ]
           [ WHERE condition ]
           [ GROUP BY expression [, ...] ]
           [ HAVING condition [, ...] ]
           [ { UNION | INTERSECT | EXCEPT } [ ALL ] select ]
           [ ORDER BY expression [ ASC | DESC | USING operator ] [, ...] ]
           [ LIMIT { count | ALL } ]
           [ OFFSET start ]
           [ FOR { UPDATE | SHARE } [ OF table_name [, ...] ] [ NOWAIT ] ]

       where froitem can be one of:

           [ ONLY ] table_name [ * ] [ [ AS ] alias [ ( column_alias [, ...] ) ] ]
           ( select ) [ AS ] alias [ ( column_alias [, ...] ) ]
           function_name ( [ argument [, ...] ] ) [ AS ] alias [ ( column_alias [, ...] | column_definition [, ...] ) ]
           function_name ( [ argument [, ...] ] ) AS ( column_definition [, ...] )
           froitem [ NATURAL ] join_type froitem [ ON join_condition | USING ( join_column [, ...] ) ]
*/
  private void processSelect()
  {
    boolean fromFound = false;
    boolean intoFound = false;

    ArrayList columnList = new ArrayList();
    ArrayList intoTableList = new ArrayList();
    ArrayList tableList = new ArrayList();
    for (Iterator it = tokenList.iterator(); it.hasNext();)
    {
      Token token = (Token)it.next();
      String text = token.text.toLowerCase();

      if (text.equals("from"))
        fromFound = true;
      else if (text.equals("into"))
      {
        intoFound = true;
        queryType = QUERY_TYPE_SELECT_INTO;
      }
      else if (text.equals("where") || text.equals("group by") || text.equals("having")
          || text.equals("union") || text.equals("intersect") || text.equals("except"))
        break;
      else if (!fromFound && !intoFound)
      {
        columnList.add(token);
      }
      else if (!fromFound)
      {
        intoTableList.add(token);
      }
      else
        tableList.add(token);
    }

    tables = processTables(tableList);
    HashMap map = new HashMap();
    for (Iterator it = tables.iterator(); it.hasNext();)
    {
      Table table = (Table)it.next();
      if (table.alias != null && !map.containsKey(table.alias))
        map.put(table.alias, table);
      map.put(table.table, table);
    }

    columns = processColumns(columnList);

    // map the column tables to the table object
    for (Iterator it = columns.iterator(); it.hasNext();)
    {
      Column column = (Column)it.next();
      if (column.tablename != null)
      {
        Table table = (Table)map.get(column.tablename);
        column.table = table;
      }
    }

    for (Iterator iter = tables.iterator(); iter.hasNext();)
    {
      Table table = (Table)iter.next();
      logger.debug("Select Table: " + table);
    }

    for (Iterator iter = columns.iterator(); iter.hasNext();)
    {
      Column column = (Column)iter.next();
      logger.debug("Select Column: " + column);
    }

  }

  private void removeCommentTokens(List tokenList)
  {
    // remove comment tokens
    for (Iterator it = tokenList.iterator(); it.hasNext();)
    {
      Token token = (Token)it.next();
      if (token.tokenType == TokenScanner.BLOCKCOMMENT
          || token.tokenType == TokenScanner.LINECOMMENT)
      {
        it.remove();
      }
    }
  }

  private List processColumns(List tokenList)
  {
    removeCommentTokens(tokenList);

    ArrayList columnList = new ArrayList();
    ArrayList tempList = new ArrayList();
    boolean lookingForStart = false;
    for (int index = 0; index < tokenList.size(); index++)
    {
      Token token = (Token)tokenList.get(index);
      String lower = token.text.toLowerCase();

      if (token.text.equals(","))
      {
        lookingForStart = false;
        Column column = processColumnTokens(tempList);
        tempList.clear();
        if (column != null)
          columnList.add(column);
      }
      else if (lower.equals(")") && lookingForStart)
        lookingForStart = false;
      else if (lower.equals("distinct"))
      {
        if (index+1 < tokenList.size())
        {
          index++;
          token = (Token)tokenList.get(index);
          lower = token.text.toLowerCase();
          if (lower.equals("on"))
            lookingForStart = true;
          else
            index--;
        }
      }
      else if (lower.equals("all")
          || lower.equals("unique"))
      {

      }
      else if (!lookingForStart)
        tempList.add(token);
    }

    Column column = processColumnTokens(tempList);
    if (column != null)
      columnList.add(column);

    /*

    for (Iterator it = columnList.iterator(); it.hasNext();)
    {
      column = (Column)it.next();
      System.out.println(column);
    }
    */

    return columnList;

  }

  private Column processColumnTokens(List tokenList)
  {
    if (tokenList.size() == 0)
      return null;

    Column column = new Column();


    boolean tableFound = false;
    boolean columnFound = false;

    for (int index = 0; index < tokenList.size(); index++)
    {
      Token token = (Token)tokenList.get(index);
      String lower = token.text.toLowerCase();

      if (lower.equals("."))
      {
        if (index+1 >= tokenList.size()) // invalid
          return null;

        index++;
        token = (Token)tokenList.get(index);

        if (!tableFound)
        {
          tableFound = true;
          column.tablename = column.column;
          column.column = token.text;
        }
        else // invalid
          return null;
      }
      else if (token.tokenType == TokenScanner.OPERATOR)
      {
        // invalid - may be the '(' meaning function or +-*/ or some such
        return null;
      }
      else if (lower.equals("as"))
      {
        if (!tableFound || index == tokenList.size() - 1) // invalid
          return null;
      }
      else if (!columnFound)
      {
        columnFound = true;
        column.column = token.text;
      }
      else if (columnFound)
      {
        column.alias = token.text;
      }
    }

    /*
    column.tablename = removeIdentifierQuote(column.tablename);
    column.column = removeIdentifierQuote(column.column);
    column.alias  = removeIdentifierQuote(column.alias);
    */

    return column;
  }


  private List processTables(List tokenList)
  {
    removeCommentTokens(tokenList);

    ArrayList tableList = new ArrayList();
    boolean lookingForStart = false;
    ArrayList tempList = new ArrayList();
    for (int index = 0; index < tokenList.size(); index++)
    {
      Token token = (Token)tokenList.get(index);
      String lower = token.text.toLowerCase();

      if (token.text.equals(","))
      {
        lookingForStart = false;
        Table table = processTableTokens(tempList);
        tempList.clear();
        if (table != null)
          tableList.add(table);
      }
      else if (lower.equals("inner")
          || lower.equals("outer")
          || lower.equals("left")
          || lower.equals("right")
          || lower.equals("full")
          || lower.equals("cross")
          || lower.equals("join"))
      {
        lookingForStart = false;
        Table table = processTableTokens(tempList);
        tempList.clear();
        if (table != null)
          tableList.add(table);
      }
      else if (lower.equals("on") ||
          lower.equals("using"))
      {
        lookingForStart = true;
      }
      else if (!lookingForStart)
        tempList.add(token);
    }

    Table table = processTableTokens(tempList);
    if (table != null)
      tableList.add(table);

    return tableList;
  }

  private Table processTableTokens(List tokenList)
  {
    if (tokenList.size() == 0)
      return null;

    Table table = new Table();

    boolean catalogFound = false;
    boolean schemaFound = false;
    boolean tableFound = false;

    for (int index = 0; index < tokenList.size(); index++)
    {
      Token token = (Token)tokenList.get(index);
      String lower = token.text.toLowerCase();

      if (token.text.equals(schemaSeparator))
      {
        if (index+1 >= tokenList.size()) // invalid
          return null;

        index++;
        token = (Token)tokenList.get(index);

        if (!schemaFound && useSchema)
        {
          schemaFound = true;
          if (token.text.equals(schemaSeparator))
          {
            index--;
            table.schema = table.table;
            table.table = null;
          }
          else
          {
            table.schema = table.table;
            table.table = token.text;
          }
        }
        else if (!catalogFound)
        {
          catalogFound = true;
          if (useSchema)
          {
            table.catalog = table.schema;
            table.schema = table.table;
            table.table = token.text;
          }
          else
          {
            table.catalog = table.table;
            table.table = token.text;
          }
        }
        else // invalid
          return null;
      }
      else if (token.tokenType == TokenScanner.OPERATOR)
      {
        // invalid - may be the '(' starting a sub query
        return null;
      }
      else if (lower.equals("as"))
      {
        if (!tableFound || index == tokenList.size() - 1) // invalid
          return null;
      }
      else if (!tableFound)
      {
        tableFound = true;
        table.table = token.text;
      }
      else if (tableFound)
      {
        table.alias = token.text;
      }
    }

    /*
    table.catalog = removeIdentifierQuote(table.catalog);
    table.schema = removeIdentifierQuote(table.schema);
    table.table = removeIdentifierQuote(table.table);
    table.alias  = removeIdentifierQuote(table.alias);
    */

    return table;
  }

  /*
  private String removeIdentifierQuote(String value)
  {
    if (value == null)
      return null;

    if (value.startsWith(identifierQuote))
      value = value.substring(identifierQuote.length());
    if (value.endsWith(identifierQuote))
      value = value.substring(0, value.length() - identifierQuote.length());

    if (value.length() == 0)
      return null;

    return value;
  }
  */

  private void determineQueryType()
  {
    Token token = null;
    for (Iterator it = tokenList.iterator(); it.hasNext();)
    {
      Token currentToken = (Token)it.next();
      if (currentToken.isKeyword())
      {
        token = currentToken;
        break;
      }
    }

    if (token != null)
    {
      String text = token.text.toLowerCase();
      if (text.equals("select"))
        queryType = QUERY_TYPE_SELECT;
      else if (text.equals("insert"))
        queryType = QUERY_TYPE_INSERT;
      else if (text.equals("update"))
        queryType = QUERY_TYPE_UPDATE;
      else if (text.equals("delete"))
        queryType = QUERY_TYPE_DELETE;
      else if (text.equals("create"))
        queryType = QUERY_TYPE_CREATE;
      else if (text.equals("alter"))
        queryType = QUERY_TYPE_ALTER;
      else if (text.equals("drop"))
        queryType = QUERY_TYPE_DROP;
      else if (text.equals("commit"))
        queryType = QUERY_TYPE_COMMIT;
      else if (text.equals("rollback"))
        queryType = QUERY_TYPE_ROLLBACK;
    }
  }


  public String removeComments()
  {
    StringBuffer sbuf = new StringBuffer();
    for (Iterator it = tokenListWithWhiteSpace.iterator(); it.hasNext();)
    {
      Token token = (Token)it.next();
      if (token.tokenType != TokenScanner.BLOCKCOMMENT && token.tokenType != TokenScanner.LINECOMMENT)
        sbuf.append(token.text);
    }

    return sbuf.toString();
  }

  public static void main(String[] args)
  {
    QueryParser qp = new QueryParser(".", "'", false);
    String sql = "/* SELECT column_a, column_b  \n" +
                "aasdfsadf*/select count(*) from REG_MAIN \n" +
                "where RECEIVED_DTM > (TO_DATE('2004-01-01','YYYY-MM-DD') -2) ";
    qp.parseQuery(sql);

    System.out.println("|" + qp.removeComments() + "|");

  }

  /*
  public static void main(String[] args)
  {
    QueryParser qp = new QueryParser(".", "\"", true);

    String sql = "select test from \"stgenv01\".\"\".\"STT_IM44_USSEC\"";
    String sql1 = "SELECT ALL DISTINCT uniQue LastName,FirstName FROM Persons p group by LastName";
    String sql12 = "SELECT p.LastName,p . FirstName FROM Persons AS p";
    String sql13 = "SELECT p.LastName,p . FirstName FROM (select * from test) AS p";
    String sql2 = "SELECT LastName,FirstName INTO Persons_backup FROM Persons";

    String sql3 = "SELECT Employees.LastName, Employees.FirstName, Supervisors.LastName,\n" +
     "Supervisors.FirstName\n" +
     "FROM \"test\".\"owner\".\"Employees\" LEFT JOIN test..Employees AS Supervisors\n" +
     "ON Employees.EmployeeID = Supervisors.ReportsTo ";


    String sql4 = "SELECT CustomersCOPY.CustomerID, CustomersCOPY.CompanyName, Orders.OrderID " +
    "     FROM CustomersCOPY FULL OUTER JOIN Orders " +
    "     ON CustomersCOPY.CustomerID = Orders.CustomerId " +
    "     ORDER BY CustomersCOPY.CustomerID ";

     String sql5 = "SELECT CustomersCOPY.CustomerID, CustomersCOPY.CompanyName, Orders.OrderID " +
     "     FROM CustomersCOPY FULL OUTER JOIN Orders " +
     "     USING (CustomersCOPY.CustomerID) " +
     "     ORDER BY CustomersCOPY.CustomerID ";

     String sql6 = "SELECT Products.ProductName as prods, Products.UnitPrice price, Temporary.Amount cost " +
     "     FROM Products CROSS JOIN Temporary " +
     "     ORDER BY Products.ProductName, Temporary.Amount ";


     String sql0 = "select cards1.card_id, cards1.set, cards1.in_other_sets, cards1.name, cards1.color, cards1.cost, cards1.type, " +
     "cards1.power, cards1.toughness, cards1.rulestext, cards1.flavortext, " +
     "cards1.artist, cards1.rarity, cards1.normal_price, cards1.foil_price, cardlist.description, " +
     "inv1.quantity as quantity1, sum(inv2.quantity) as quantity2, sets.gatherer_name " +
     "from cards cards1 " +
     "left outer join inventory inv1 on (cards1.card_id = inv1.card_id " +
     "    and inv1.appuser = 'username' ) " +
     "left outer join cards cards3 on (cards1.name = cards3.name) " +
     "left outer join inventory inv2 on (cards3.card_id = inv2.card_id " +
     "    and inv2.appuser = 'username' ) " +
     "left outer join cardlist on (cards1.card_id = cardlist.card_id " +
     "    and cardlist.appuser = 'username' and cardlist.description = 'Misc') " +
     ", sets ";

    qp.parseQuery(sql);
  }
  */

}
