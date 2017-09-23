package org.stellasql.stella.connection;

public class QueryError
{
  private String message = null;

  public QueryError(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

}
