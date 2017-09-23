package org.stellasql.stella.gui.util;

import java.util.LinkedList;

import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;

public class TextUndoer implements ExtendedModifyListener
{
  private static final int MAX_UNDOS = 100;
  private boolean changing = false;
  private LinkedList undoStack = new LinkedList();
  private LinkedList redoStack = new LinkedList();
  private StyledText styledText;

  public TextUndoer(StyledText styledText)
  {
    this.styledText = styledText;
    this.styledText.addExtendedModifyListener(this);
  }

  public boolean undoAvailable()
  {
    return undoStack.size() > 0;
  }

  public boolean redoAvailable()
  {
    return redoStack.size() > 0;
  }

  @Override
  public void modifyText(ExtendedModifyEvent e)
  {
    if (!changing)
    {
      String newText = "";
      if (e.length > 0)
        newText = styledText.getText(e.start, e.start + e.length - 1);

      undoStack.addFirst(new TextChange(e.start, e.length, e.replacedText, newText));
      if (undoStack.size() > MAX_UNDOS)
        undoStack.removeLast();
      redoStack.clear();
    }
  }

  public void undo()
  {
    if (undoStack.size() > 0)
    {
      changing = true;

      TextChange change = (TextChange)undoStack.removeFirst();
      styledText.replaceTextRange(change.getStart(), change.getLength(), change.getReplacedText());
      styledText.setCaretOffset(change.getStart() + change.getReplacedText().length());
      redoStack.addFirst(change);

      changing = false;
    }
  }

  public void redo()
  {
    if (redoStack.size() > 0)
    {
      changing = true;

      TextChange change = (TextChange)redoStack.removeFirst();
      styledText.replaceTextRange(change.getStart(), change.getReplacedText().length(), change.getNewText());
      styledText.setCaretOffset(change.getStart() + change.getNewText().length());
      undoStack.addFirst(change);

      changing = false;
    }
  }

  private class TextChange
  {
    public final static int INSERT = 1;
    public final static int REMOVE = 2;
    public final static int REPLACE = 3;

    private int start;
    private int length;
    private String newText;
    private String replacedText;
    private int type;

    public TextChange(int start, int length, String replacedText, String newText)
    {
      this.start = start;
      this.length = length;
      this.newText = newText;
      this.replacedText = replacedText;

      if (this.replacedText.length() == 0)
        type = INSERT;
      else if (newText.length() == 0)
        type = REMOVE;
      else
        type = REPLACE;
    }

    public int getLength()
    {
      return length;
    }

    public int getStart()
    {
      return start;
    }

    public String getReplacedText()
    {
      return replacedText;
    }

    public String getNewText()
    {
      return newText;
    }

    public int getType()
    {
      return type;
    }
  }

}
