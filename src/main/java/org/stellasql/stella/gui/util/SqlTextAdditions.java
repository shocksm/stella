package org.stellasql.stella.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.stellasql.stella.gui.DateLiteralDialog;

public class SqlTextAdditions implements MenuListener, SelectionListener, MouseListener, KeyListener
{
  private StyledText sqlText = null;
  private Menu menu = null;
  private MenuItem cutMI;
  private MenuItem copyMI;
  private MenuItem pasteMI;
  private MenuItem selectAllMI;
  private MenuItem undoMI;
  private MenuItem redoMI;
  private MenuItem removeQuotesMI;
  private MenuItem addQuotesMI;
  private MenuItem addDateLiteralMI;
  private TextUndoer textUndoer = null;
  private SyntaxHighlighter syntaxHighlighter = null;

  public SqlTextAdditions(StyledText styledText, String querySeperator)
  {
    sqlText = styledText;
    menu = new Menu(styledText);
    menu.addMenuListener(this);
    sqlText.setMenu(menu);
    sqlText.addKeyListener(this);
    sqlText.setDoubleClickEnabled(false);
    sqlText.addMouseListener(this);
    textUndoer = new TextUndoer(sqlText);
    syntaxHighlighter = new SyntaxHighlighter(sqlText, querySeperator);

    cutMI = new MenuItem(menu, SWT.PUSH);
    cutMI.addSelectionListener(this);
    cutMI.setText("Cu&t\tCtrl+X");

    copyMI = new MenuItem(menu, SWT.PUSH);
    copyMI.addSelectionListener(this);
    copyMI.setText("&Copy\tCtrl+C");

    pasteMI = new MenuItem(menu, SWT.PUSH);
    pasteMI.addSelectionListener(this);
    pasteMI.setText("&Paste\tCtrl+V");

    new MenuItem(menu, SWT.SEPARATOR);

    selectAllMI = new MenuItem(menu, SWT.PUSH);
    selectAllMI.addSelectionListener(this);
    selectAllMI.setText("Select &All\tCtrl+A");

    new MenuItem(menu, SWT.SEPARATOR);

    undoMI = new MenuItem(menu, SWT.PUSH);
    undoMI.addSelectionListener(this);
    undoMI.setText("&Undo\tCtrl+Z");

    redoMI = new MenuItem(menu, SWT.PUSH);
    redoMI.addSelectionListener(this);
    redoMI.setText("&Redo\tCtrl+Y");

    new MenuItem(menu, SWT.SEPARATOR);

    removeQuotesMI = new MenuItem(menu, SWT.PUSH);
    removeQuotesMI.addSelectionListener(this);
    removeQuotesMI.setText("R&emove Quotes (Java syntax)");

    addQuotesMI = new MenuItem(menu, SWT.PUSH);
    addQuotesMI.addSelectionListener(this);
    addQuotesMI.setText("Add &Quotes (Java syntax)");

    new MenuItem(menu, SWT.SEPARATOR);

    addDateLiteralMI = new MenuItem(menu, SWT.PUSH);
    addDateLiteralMI.addSelectionListener(this);
    addDateLiteralMI.setText("Insert &Date Literal");
  }

  public SyntaxHighlighter getSyntaxHighlighter()
  {
    return syntaxHighlighter;
  }

  private void selectAll()
  {
    sqlText.selectAll();
  }

  private void deleteLine()
  {
  	int line = sqlText.getLineAtOffset(sqlText.getCaretOffset());
  	int lineOffset = sqlText.getOffsetAtLine(line);
  	int lineLength = sqlText.getLine(sqlText.getLineAtOffset(sqlText.getCaretOffset())).length();


  	if (sqlText.getLineCount() > line + 1) {
  		// delete line break since there is a next line
  		sqlText.setSelectionRange(lineOffset, lineLength + sqlText.getLineDelimiter().length());
  		sqlText.invokeAction(ST.DELETE_NEXT);
  	}
  	else if (sqlText.getLineCount() > 1 && sqlText.getLineCount() == line + 1) {
  		// this is the last line and a line is above so delete the preceding line break
  		sqlText.setSelectionRange(lineOffset - sqlText.getLineDelimiter().length(), lineLength + sqlText.getLineDelimiter().length());
  		sqlText.invokeAction(ST.DELETE_NEXT);
  	}
  	else {
  		// this is the only line so just blank out the text
  		sqlText.replaceTextRange(lineOffset, lineLength, "");
  	}
  }

  private void cut()
  {
    sqlText.cut();
  }

  private void copy()
  {
    sqlText.copy();
  }

  private void paste()
  {
    sqlText.paste();
  }

  public void insertDateLiteral()
  {
    DateLiteralDialog dld = new DateLiteralDialog(sqlText.getShell());
    String value = dld.open(-1, -1);
    if (value != null)
    {
      insertQueryText(value);
    }
  }

  public void dequoteText()
  {
    int start = -1;
    int end = -1;
    if (sqlText.getSelectionText().length() > 0)
    {
      start = sqlText.getSelection().x;
      end = sqlText.getSelection().y;
    }
    else
    {
      Point lineRange = getCurrentLineRange();
      if (lineRange.x < sqlText.getLineCount())
      {
        start = sqlText.getOffsetAtLine(lineRange.x);
        end = sqlText.getCharCount();
        if (lineRange.y < sqlText.getLineCount() - 1)
        {
          end = sqlText.getOffsetAtLine(lineRange.y + 1) - sqlText.getLineDelimiter().length();
        }
      }
    }
    int length = end - start;

    if (length > 0)
    {
      String text = sqlText.getTextRange(start, length);
      if (text.indexOf('"') < 0)
        return;

      StringBuffer sbuf = new StringBuffer();
      boolean inQuote = false;
      for (int index = 0; index < text.length(); index++)
      {
        char c = text.charAt(index);
        if (c == '"')
          inQuote = !inQuote;
        else if (c == '+' && !inQuote)
          sbuf.append("");
        else if (c == ';' && !inQuote)
          sbuf.append("");
        else if (c == ' ' && !inQuote)
          sbuf.append("");
        else if (c == '\\')
        {
          if (index == text.length() - 1)
            sbuf.append(c);
          else if (text.charAt(index+1) == '\\')
          {
            sbuf.append('\\');
            index++;
          }
          else if (text.charAt(index+1) == '"')
          {
            sbuf.append('"');
            index++;
          }
        }
        else
          sbuf.append(c);
      }

      sqlText.replaceTextRange(start, length, sbuf.toString());
    }
  }

  public void quoteText()
  {
    int start = -1;
    int end = -1;
    if (sqlText.getSelectionText().length() > 0)
    {
      start = sqlText.getSelection().x;
      end = sqlText.getSelection().y;
    }
    else
    {
      Point lineRange = getCurrentLineRange();
      if (lineRange.x < sqlText.getLineCount())
      {
        start = sqlText.getOffsetAtLine(lineRange.x);
        end = sqlText.getCharCount();
        if (lineRange.y < sqlText.getLineCount() - 1)
        {
          end = sqlText.getOffsetAtLine(lineRange.y + 1) - sqlText.getLineDelimiter().length();
        }
      }
    }
    int length = end - start;

    if (length > 0)
    {
      String text = sqlText.getTextRange(start, length).replaceAll(sqlText.getLineDelimiter(), "\n");
      StringBuffer sbuf = new StringBuffer("\"");
      for (int index = 0; index < text.length(); index++)
      {
        char c = text.charAt(index);
        if (c == '\n')
        {
          if (sbuf.charAt(sbuf.length() - 1) != ' ')
            sbuf.append(" ");
          sbuf.append("\" +\n\"");
        }
        else if (c == '\\')
          sbuf.append("\\\\");
        else if (c == '"')
          sbuf.append("\\\"");
        else
          sbuf.append(c);
      }
      if (sbuf.charAt(sbuf.length() - 1) != ' ')
        sbuf.append(" ");
      sbuf.append("\";");

      sqlText.replaceTextRange(start, length, sbuf.toString());
    }
  }

  private Point getCurrentLineRange()
  {
    Point range = new Point(0, -1);
    int currentLine = sqlText.getLineAtOffset(sqlText.getCaretOffset());
    range.y = sqlText.getLineCount() - 1;

    // find startLine
    for (int index = currentLine; index > 0; index--)
    {
      if (getTextOnLine(index).trim().length() == 0)
      {
        range.x = index + 1;
        break;
      }
    }

    // find endLine
    for (int index = currentLine; index < sqlText.getLineCount(); index++)
    {
      if (getTextOnLine(index).trim().length() == 0)
      {
        range.y = index - 1;
        break;
      }
    }

    return range;
  }


  private String getTextOnLine(int line)
  {
    int start = sqlText.getOffsetAtLine(line);
    int stop = sqlText.getCharCount();
    if (line < sqlText.getLineCount() - 1)
      stop = sqlText.getOffsetAtLine(line + 1) - sqlText.getLineDelimiter().length();

    String text = "";
    if (stop > start)
    {
      text = sqlText.getTextRange(start, stop - start).replaceAll(sqlText.getLineDelimiter(), "");
    }

    return text;
  }

  public void insertQueryText(String text)
  {
    if (sqlText.getSelectionCount() == 0)
    {
      sqlText.insert(text);
      sqlText.setCaretOffset(sqlText.getCaretOffset() + text.length());
    }
    else
    {
      Point pt = sqlText.getSelection();
      sqlText.insert(text);
      sqlText.setCaretOffset(pt.x + text.length());
    }

    sqlText.setFocus();
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL)
    {
      selectAll();
    }
    else if (e.keyCode == 'd' && e.stateMask == SWT.CONTROL)
    {
      deleteLine();
    }
    else if (e.keyCode == 'z' && e.stateMask == SWT.CONTROL)
    {
      textUndoer.undo();
    }
    else if (e.keyCode == 'y' && e.stateMask == SWT.CONTROL)
    {
      textUndoer.redo();
    }
  }
  @Override
  public void keyReleased(KeyEvent e)
  {
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {
    cutMI.setEnabled(sqlText.getSelectionCount() > 0);
    copyMI.setEnabled(sqlText.getSelectionCount() > 0);

    undoMI.setEnabled(textUndoer.undoAvailable());
    redoMI.setEnabled(textUndoer.redoAvailable());
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }
  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cutMI)
    {
      cut();
    }
    else if (e.widget == copyMI)
    {
      copy();
    }
    else if (e.widget == pasteMI)
    {
      paste();
    }
    else if (e.widget == selectAllMI)
    {
      selectAll();
    }
    else if (e.widget == undoMI)
    {
      undo();
    }
    else if (e.widget == redoMI)
    {
      redo();
    }
    else if (e.widget == removeQuotesMI)
    {
      dequoteText();
    }
    else if (e.widget == addQuotesMI)
    {
      quoteText();
    }
    else if (e.widget == addDateLiteralMI)
    {
      insertDateLiteral();
    }
  }

  private boolean isStopChar(char c)
  {
    if (!Character.isLetterOrDigit(c) && c != '_')
      return true;

    return false;
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
    if (e.widget == sqlText)
    {
      // default double click for StyledText will not select words containing '_'s.
      // I don't like that
      int max = sqlText.getCharCount();
      int offset = sqlText.getCaretOffset();

      if (max == 0)
        return;

      if (offset >= max)
        offset = max - 1;

      if (Character.isWhitespace(sqlText.getText(offset, offset).charAt(0)))
      {
        String delim = sqlText.getLineDelimiter();
        if (offset + (delim.length() - 1) < max)
        {
          String text = sqlText.getText(offset, offset + (delim.length() - 1));
          if (text.equals(delim))
            offset--;
        }
      }

      if (offset < 0)
        offset = 0;

      int start = offset;
      int end = offset;

      // find start
      for (int pos = offset; pos >= 0; pos--)
      {
        char c = sqlText.getText(pos, pos).charAt(0);
        if (isStopChar(c))
          break;
        start = pos;
      }
      // find end
      for (int pos = offset; pos < max; pos++)
      {
        char c = sqlText.getText(pos, pos).charAt(0);
        if (isStopChar(c))
          break;
        end = pos;
      }

      if (start < end || !Character.isWhitespace(sqlText.getText(start, start).charAt(0)))
      {
        sqlText.setSelection(start, end+1);
      }
    }
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
  }

  public void undo()
  {
    textUndoer.undo();
  }

  public void redo()
  {
    textUndoer.redo();
  }

  public boolean undoAvailable()
  {
    return textUndoer.undoAvailable();
  }
  public boolean redoAvailable()
  {
    return textUndoer.redoAvailable();
  }

}
