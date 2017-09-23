package org.stellasql.stella.gui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.SyntaxListener;
import org.stellasql.stella.query.Location;
import org.stellasql.stella.query.TokenScanner;
import org.stellasql.stella.session.QuerySeparatorChangeListener;
import org.stellasql.stella.session.SessionData;

public class SyntaxHighlighter implements LineStyleListener, ExtendedModifyListener, DisposeListener, SyntaxListener, QuerySeparatorChangeListener
{
  private final static Logger logger = LogManager.getLogger(SyntaxHighlighter.class);

  private static final int CHANGED = 1;
  private static final int RESCAN = 1<<1;

  private static final int INSERT = 1;
  private static final int REMOVE = 2;
  private static final int REPLACE = 3;

  private static HashMap tokenSyntaxContainerMap = null;

  private StyledText styledText = null;
  private SessionData sessionData = null;
  private ArrayList blockCommentStart = new ArrayList();
  private ArrayList blockCommentPaired = new ArrayList();
  private ArrayList lineComment = new ArrayList();
  private int quoteSinglePos = -1;
  private ArrayList quotePaired = new ArrayList();
  private TokenScanner tokenScanner = null;

  public SyntaxHighlighter(StyledText styledText, String querySeparator)
  {
    init(styledText, querySeparator);
  }

  public SyntaxHighlighter(StyledText styledText, SessionData sessionData)
  {
    this.sessionData = sessionData;
    sessionData.addQuerySeparatorChangeListener(this);

    init(styledText, sessionData.getQuerySeparator());
  }

  private void init(StyledText styledText, String querySeparator)
  {
    this.styledText = styledText;
    this.styledText.addLineStyleListener(this);
    this.styledText.addExtendedModifyListener(this);
    this.styledText.addDisposeListener(this);

    synchronized (SyntaxHighlighter.class)
    {
      if (tokenSyntaxContainerMap == null)
      {
        initializeColors();
      }
    }

    tokenScanner = new TokenScanner(querySeparator);
  }

  @Override
  public void querySeparatorChanged(String querySeparator)
  {
    tokenScanner.setQuerySeparator(querySeparator);
    syntaxChanged();
  }

  public static void initializeColors()
  {
    tokenSyntaxContainerMap = new HashMap();
    tokenSyntaxContainerMap.put(TokenScanner.KEYWORD, ApplicationData.getInstance().getSyntax(TokenScanner.KEYWORD));
    tokenSyntaxContainerMap.put(TokenScanner.LINECOMMENT, ApplicationData.getInstance().getSyntax(TokenScanner.LINECOMMENT));
    tokenSyntaxContainerMap.put(TokenScanner.BLOCKCOMMENT, ApplicationData.getInstance().getSyntax(TokenScanner.BLOCKCOMMENT));
    tokenSyntaxContainerMap.put(TokenScanner.STRING, ApplicationData.getInstance().getSyntax(TokenScanner.STRING));
    tokenSyntaxContainerMap.put(TokenScanner.NUMBER, ApplicationData.getInstance().getSyntax(TokenScanner.NUMBER));
    tokenSyntaxContainerMap.put(TokenScanner.OPERATOR, ApplicationData.getInstance().getSyntax(TokenScanner.OPERATOR));
    tokenSyntaxContainerMap.put(TokenScanner.SEPARATOR, ApplicationData.getInstance().getSyntax(TokenScanner.SEPARATOR));
  }

  private void fullTokenScan()
  {
    blockCommentStart.clear();
    blockCommentPaired.clear();
    lineComment.clear();
    quoteSinglePos = -1;
    quotePaired.clear();

    // TODO getText on styled text with large text seems expensive, look for a way to
    // change the call below
    String text = styledText.getText();
    tokenScanner.setText(text);
    Object token = null;
    while ((token = tokenScanner.nextToken()) != null)
    {
      if (token == TokenScanner.BLOCKCOMMENT)
      {
        int start = tokenScanner.getStartPosition();
        int end = tokenScanner.getEndPosition();
        if (!text.startsWith("*/", end - 1)) // only a start token
        {
          blockCommentStart.add(new Location(start));
        }
        else
        {
          blockCommentPaired.add(new Location(start, end));
        }
      }
      else if (token == TokenScanner.LINECOMMENT)
      {
        lineComment.add(new Location(tokenScanner.getStartPosition()));
      }
      else if (token == TokenScanner.STRING)
      {
        int start = tokenScanner.getStartPosition();
        int end = tokenScanner.getEndPosition();
        if (end == start || (!text.startsWith("'", end) || text.startsWith("\\'", end-1))) // only a start token
        {
          quoteSinglePos = start;
        }
        else
        {
          quotePaired.add(new Location(start, end));
        }
      }
    }

  }

  @Override
  public void modifyText(ExtendedModifyEvent e)
  {
    // Keep track of block comment tokens and the other tokens
    // that can goble them up (line comments and strings)
    boolean redraw = false;

    if (e.length == 0)
    { // removing text
      int len = e.replacedText.length();
      int update = updateTokens(REMOVE, e.start, -len, len);
      if (update > 0)
        redraw = true;

      if ((update & RESCAN) > 0)
      {
        fullTokenScan();
      }
      else
      {
        int start = e.start - 1;
        if (start < 0)
          start = 0;

        if (tokenScan(start, start+1))
        {
          fullTokenScan();
          redraw = true;
        }
      }
    }
    else if (e.replacedText.length() == 0)
    { // inserting text
      int update = updateTokens(INSERT, e.start, e.length, e.length);
      if (update > 0)
        redraw = true;

      if ((update & RESCAN) > 0)
      {
        fullTokenScan();
      }
      else
      {
        int start = e.start - 1;
        int end = e.start + e.length;
        if (start < 0)
          start = 0;

        if (tokenScan(start, end))
        {
          fullTokenScan();
          redraw = true;
        }
      }
    }
    else
    { // replacing text
      int len = e.length - e.replacedText.length();

      int update = updateTokens(REPLACE, e.start, len, e.replacedText.length());
      if (update > 0)
        redraw = true;
      if ((update & RESCAN) > 0)
      {
        fullTokenScan();
      }
      else
      {
        int start = e.start - 1;
        int end = e.start + e.length;
        if (start < 0)
          start = 0;

        if (tokenScan(start, end))
        {
          fullTokenScan();
          redraw = true;
        }
      }
    }


    if (redraw)
      styledText.redrawRange(0, styledText.getCharCount(), true);
  }


  private int updateTokens(int type, int start, int posChange, int length)
  {
    int changed = 0;


    // update paired quotes
    for (int i = 0; i < quotePaired.size(); i++)
    {
      Location loc = (Location)quotePaired.get(i);

      if ((type == REMOVE || type == REPLACE) && loc.start >= start && loc.start < start + length)
      {
        changed = changed | CHANGED;
        quotePaired.remove(i);
        i--;

        if (loc.end > start + length - 1)
        { // the end is after the removed text
          return RESCAN;
        }
      }
      else if ((type == REMOVE || type == REPLACE) && loc.end >= start && loc.end < start + length)
      {
        return RESCAN;
      }
      else if (loc.start >= start)
      {
        loc.start = loc.start + posChange;
        loc.end = loc.end + posChange;
        changed = changed | CHANGED;
      }
      else if (loc.end >= start)
      {
        loc.end = loc.end + posChange;
        changed = changed | CHANGED;
        if (styledText.getText().charAt(loc.end - 1) == '\\')
        { // the quote was moved next to the escape char
          return RESCAN;
        }
      }
    }


    // update unpaired quote
    if ((type == REMOVE || type == REPLACE) && quoteSinglePos >= start && quoteSinglePos < start + length)
    {
      return RESCAN;
    }
    else if (quoteSinglePos >= start)
    {
      quoteSinglePos = quoteSinglePos + posChange;
      changed = changed | CHANGED;
    }


    // update paired block comments
    for (int i = 0; i < blockCommentPaired.size(); i++)
    {
      Location loc = (Location)blockCommentPaired.get(i);
      if ((type == REMOVE || type == REPLACE) && loc.start >= start && loc.start < start + length)
      {
        changed = changed | CHANGED;
        blockCommentPaired.remove(i);
        i--;
        if (loc.end - 1 > start + length - 1)
        { // the end is after the removed text
          return RESCAN;
        }
      }
      else if ((type == REMOVE || type == REPLACE) && loc.end - 1 >= start && loc.end - 1 <= start + length-1)
      {
        return RESCAN;
      }
      else if (loc.start + 1 == start)
      { // check to see if block comment start has been split
        return RESCAN;
      }
      else if (loc.end == start)
      { // check to see if block comment end has been split
        return RESCAN;
      }
      else if (loc.start >= start)
      {
        loc.start = loc.start + posChange;
        loc.end = loc.end + posChange;
        changed = changed | CHANGED;
      }
      else if (loc.end - 1 >= start || loc.end < 0)
      {
        loc.end = loc.end + posChange;
        changed = changed | CHANGED;
      }
    }

    // update unpaired block comment starts
    for (int i = 0; i < blockCommentStart.size(); i++)
    {
      Location loc = (Location)blockCommentStart.get(i);
      if ((type == REMOVE || type == REPLACE) && loc.start >= start && loc.start < start + length)
      {
        return RESCAN;
      }
      else if (loc.start >= start)
      {
        loc.start = loc.start + posChange;
        changed = changed | CHANGED;
      }
      else if (loc.start + 1 == start)
      { // check to see if block comment has been split
        return RESCAN;
      }
    }

    // update line comments
    for (int i = 0; i < lineComment.size(); i++)
    {
      Location loc = (Location)lineComment.get(i);
      if ((type == REMOVE || type == REPLACE) && loc.start >= start && loc.start < start + length)
      {
        return RESCAN;
      }
      else if (loc.start >= start)
      {
        loc.start = loc.start + posChange;
        changed = changed | CHANGED;
      }
      else if (loc.start + 1 == start)
      { // check to see if line comment has been split
        return RESCAN;
      }
    }

    return changed;
  }

  private boolean inQuote(int start, int end)
  {
    if (quoteSinglePos >= 0 && start >= quoteSinglePos)
      return true;

    Location loc = new Location(start, end);
    int pos = Collections.binarySearch(quotePaired, loc);
    if (pos < -1)
      pos = (-pos) - 1 - 1;
    if (pos >= 0)
    {
      Location foundLoc = (Location)quotePaired.get(pos);
      if (foundLoc.end >= end)
        return true;
    }

    return false;
  }

  private int quoteEnds(int start, int end)
  {
    int value = -1;

    Location loc = new Location(start, end);
    int pos = Collections.binarySearch(quotePaired, loc);
    if (pos < -1)
      pos = (-pos) - 1 - 1;
    if (pos >= 0)
    {
      Location foundLoc = (Location)quotePaired.get(pos);
      if (foundLoc.end >= start && foundLoc.end <= end)
        value = foundLoc.end + 1;
    }

    return value;
  }

  private boolean inBlockComment(int start, int end)
  {
    Location loc = new Location(start, end);

    int pos = Collections.binarySearch(blockCommentStart, loc);
    if (pos < -1)
      pos = (-pos) - 1 - 1;
    if (pos >= 0)
    {
      // there is at least one unpaired block start before
      return true;
    }

    pos = Collections.binarySearch(blockCommentPaired, loc);
    if (pos < -1)
      pos = (-pos) - 1 - 1;
    if (pos >= 0)
    {
      Location foundLoc = (Location)blockCommentPaired.get(pos);
      if (foundLoc.end > end)
        return true;
    }

    return false;
  }

  private int blockCommentEnds(int start, int end)
  {
    int value = -1;

    Location loc = new Location(start, end);
    int pos = Collections.binarySearch(blockCommentPaired, loc);
    if (pos < -1)
      pos = (-pos) - 1 - 1;
    if (pos >= 0)
    {
      Location foundLoc = (Location)blockCommentPaired.get(pos);
      if (foundLoc.end >= start && foundLoc.end <= end)
        value = foundLoc.end + 1;
    }

    return value;
  }

  private boolean onSameLine(int start, int end)
  {
    int lineBreak = styledText.getText().indexOf(styledText.getLineDelimiter(), start);
    if (lineBreak < 0)
      return true;
    else if (start < lineBreak && lineBreak < end)
      return false;
    else
      return true;
  }

  private boolean inLineComment(int start)
  {
    Location loc = new Location(start);
    int pos = Collections.binarySearch(lineComment, loc);
    if (pos < -1)
      pos = (-pos) - 1 - 1;
    if (pos >= 0)
    {
      Location foundLoc = (Location)lineComment.get(pos);
      if (onSameLine(foundLoc.start, start))
        return true;
      else
        return false;
    }

    return false;
  }


  private boolean tokenScan(int start, int end)
  {
    String text = null;
    boolean found = false;

    if (styledText.getCharCount() == 0)
      return true;

    if (start < 0)
      start = 0;
    if (end >= styledText.getCharCount())
      end = styledText.getCharCount() - 1;


    text = styledText.getText(start, end);

    int length = text.length();
    for (int index = 0; index < length; index++)
    {
      int offset = start + index;

      if (text.startsWith("/*", index) && !inBlockComment(offset, offset) && !inQuote(offset, offset) && !inLineComment(offset))
      {
        found = true;
      }
      else if (text.startsWith("*/", index) && !inQuote(offset, offset))
      {
        found = true;
      }
      else if (text.startsWith("--", index) && !inQuote(offset, offset) && !inBlockComment(offset, offset) && !inLineComment(offset))
      {
        found = true;
      }
      else if (text.startsWith("'", index) && !inBlockComment(offset, offset) && !inLineComment(offset))
      {
        // if the single character token deliminator is moved with the cursor right in front
        // of it tokenScan will get invoked.  Do a check first since it's quick before initiated
        // the full token scan
        if (!quoteExists(offset))
        {
          found = true;
        }
      }
    }

    return found;
  }

  private boolean quoteExists(int start)
  {
    if (start == quoteSinglePos)
      return true;

    Location loc = new Location(start);
    int pos = Collections.binarySearch(quotePaired, loc);
    if (pos < -1)
      pos = (-pos) - 1 - 1;
    if (pos >= 0)
    {
      Location foundLoc = (Location)quotePaired.get(pos);
      if (foundLoc.start == start || foundLoc.end == start)
        return true;
    }

    return false;
  }






  private SyntaxContainer getSyntaxContainer(Object tokenType)
  {
    return (SyntaxContainer)tokenSyntaxContainerMap.get(tokenType);
  }

  private void setFontStyle(StyleRange style, SyntaxContainer syntaxCon)
  {
    if (syntaxCon.getBold())
      style.fontStyle = SWT.BOLD;
    if (syntaxCon.getItalic())
      style.fontStyle = style.fontStyle | SWT.ITALIC;
  }

  @Override
  public void lineGetStyle(LineStyleEvent event)
  {
    if (tokenScanner == null)
      return;

    LinkedList styleList = new LinkedList();

    if (inBlockComment(event.lineOffset, event.lineOffset + event.lineText.length()))
    {
      SyntaxContainer syntaxCon = getSyntaxContainer(TokenScanner.BLOCKCOMMENT);
      StyleRange style = new StyleRange(event.lineOffset, event.lineText.length(), syntaxCon.getColor(), null);
      setFontStyle(style, syntaxCon);
      styleList.add(style);
      event.styles = new StyleRange[styleList.size()];
      styleList.toArray(event.styles);
      return;
    }

    if (inQuote(event.lineOffset, event.lineOffset + event.lineText.length()))
    {
      SyntaxContainer syntaxCon = getSyntaxContainer(TokenScanner.STRING);
      StyleRange style = new StyleRange(event.lineOffset, event.lineText.length(), syntaxCon.getColor(), null);
      setFontStyle(style, syntaxCon);
      styleList.add(style);
      event.styles = new StyleRange[styleList.size()];
      styleList.toArray(event.styles);
      return;
    }

    int startOffset = event.lineOffset;

    int pos = blockCommentEnds(event.lineOffset, event.lineOffset + event.lineText.length());
    if (pos >= 0)
    {
      SyntaxContainer syntaxCon = getSyntaxContainer(TokenScanner.BLOCKCOMMENT);
      StyleRange style = new StyleRange(event.lineOffset, pos - event.lineOffset, syntaxCon.getColor(), null);
      setFontStyle(style, syntaxCon);
      styleList.add(style);
      startOffset = pos;
      if (startOffset > event.lineOffset + event.lineText.length())
      {
        event.styles = new StyleRange[styleList.size()];
        styleList.toArray(event.styles);
        return;
      }
    }

    pos = quoteEnds(event.lineOffset, event.lineOffset + event.lineText.length());
    if (pos >= 0)
    {
      SyntaxContainer syntaxCon = getSyntaxContainer(TokenScanner.STRING);
      StyleRange style = new StyleRange(event.lineOffset, pos - event.lineOffset, syntaxCon.getColor(), null);
      setFontStyle(style, syntaxCon);
      styleList.add(style);
      startOffset = pos;
      if (startOffset > event.lineOffset + event.lineText.length())
      {
        event.styles = new StyleRange[styleList.size()];
        styleList.toArray(event.styles);
        return;
      }
    }

    Color foregroundColor = styledText.getForeground();
    tokenScanner.setText(event.lineText.substring(startOffset - event.lineOffset));
    Object token = null;
    while ((token = tokenScanner.nextToken()) != null)
    {
      if (token != TokenScanner.WHITESPACE)
      {
        SyntaxContainer syntaxCon = getSyntaxContainer(token);
        if ((syntaxCon != null && !syntaxCon.getColor().equals(foregroundColor)))
        {
          StyleRange style = new StyleRange(startOffset + tokenScanner.getStartPosition(),
              tokenScanner.getLength(), syntaxCon.getColor(), null);

          setFontStyle(style, syntaxCon);

          if (styleList.isEmpty())
          {
            styleList.add(style);
          }
          else
          {
            // Merge similar styles to improve performance.
            StyleRange lastStyle = (StyleRange)styleList.getLast();
            if (lastStyle.similarTo(style) && (lastStyle.start + lastStyle.length == style.start))
            {
              lastStyle.length += style.length;
            }
            else
            {
              styleList.add(style);
            }
          }
        }
      }
    }
    event.styles = new StyleRange[styleList.size()];
    styleList.toArray(event.styles);
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    ApplicationData.getInstance().removeSyntaxListener(this);
    if (sessionData != null)
      sessionData.removeQuerySeparatorChangeListener(this);
  }

  @Override
  public void syntaxChanged()
  {
    styledText.redraw();
  }


  public static void main(String[] args)
  {

    Display display = new Display();
    try
    {
      StellaClipBoard.init(display);
      ApplicationData.getInstance().load();

      Shell shell = new Shell(display);
      shell.setText("");
      GridLayout gridLayout  = new GridLayout();
      gridLayout.numColumns = 2;
      gridLayout.marginHeight = 5;
      gridLayout.marginWidth = 5;
      gridLayout.marginBottom = 5;
      gridLayout.horizontalSpacing = 0;
      shell.setLayout(gridLayout);

      StyledText test = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
      test.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      new SyntaxHighlighter(test, SessionData.createSessionData(null, "SessName", "Ted", "Password", true, true, 100, "go", false));
      final TextUndoer textUndoer = new TextUndoer(test);

      test.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e)
        {
          if (e.keyCode == 'z' && e.stateMask == SWT.CONTROL)
          {
            textUndoer.undo();
          }
          else if (e.keyCode == 'y' && e.stateMask == SWT.CONTROL)
          {
            textUndoer.redo();
          }
        }
      });


      shell.setSize(500, 200);
      shell.setLocation(300, 10);

      shell.open();

      while (!shell.isDisposed())
      {
        if (!display.readAndDispatch())
          display.sleep();
      }
      display.dispose();

    }
    catch (Exception e)
    {
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
    catch (Error e)
    {
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }





}

