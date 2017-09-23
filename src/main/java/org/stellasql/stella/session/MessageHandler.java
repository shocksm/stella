package org.stellasql.stella.session;

public interface MessageHandler
{
  public void addErrorMessage(String text, boolean onGuiThread);
  public void addSuccessMessage(String text, boolean onGuiThread);
  public void addMessage(String text, boolean onGuiThread);

}
