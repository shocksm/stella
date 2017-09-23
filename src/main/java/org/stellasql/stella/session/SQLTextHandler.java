package org.stellasql.stella.session;

public interface SQLTextHandler
{
  public boolean undoAvailable();
  public boolean redoAvailable();
  public boolean textSelected();
  public void undo();
  public void redo();
  public void cut();
  public void copy();
  public void paste();
  public void clear();
  public void selectAll();
  public void removeQuotes();
  public void addQuotes();
  public void insertDateLiteral();
  public void setText(String text);
  public void appendText(String text);
  public String getText();
  public boolean hasText();
}
