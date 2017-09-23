package org.stellasql.stella.session;

public class SessionMessage
{
  public final static int NORMAL = 1;
  public final static int ERROR = 2;
  public final static int SUCCESS = 3;

  private int type = NORMAL;
  private String text = "";
  private boolean onGuiThread = false;

  public SessionMessage(int type, String text, boolean onGuiThread)
  {
    this.type = type;
    this.text = text;
    this.onGuiThread  = onGuiThread;
  }

  public String getText()
  {
    return text;
  }

  public int getType()
  {
    return type;
  }

  public boolean onGuiThread()
  {
    return onGuiThread;
  }

}
