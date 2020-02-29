package org.stellasql.stella.session;

public interface ResultTabHandler
{
  public void closeSelectedTab();

	public void selectNextTab();

	public void selectPreviousTab();
}
