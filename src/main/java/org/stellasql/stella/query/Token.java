package org.stellasql.stella.query;

public class Token
{
  public Location loc = null;
  public Object tokenType = null;
  public String text = null;


  public Token(Location loc, Object tokenType, String text)
  {
    this.loc = loc;
    this.tokenType = tokenType;
    this.text = text;
  }

  @Override
  public String toString()
  {
    return loc + " " + tokenType + " '" + text + "'";
  }

  public boolean isKeyword()
  {
    return tokenType == TokenScanner.KEYWORD;
  }

  public boolean isComment()
  {
    return (tokenType == TokenScanner.BLOCKCOMMENT || tokenType == TokenScanner.LINECOMMENT);
  }

  public boolean isWhitespace()
  {
    return (tokenType == TokenScanner.WHITESPACE);
  }
}
