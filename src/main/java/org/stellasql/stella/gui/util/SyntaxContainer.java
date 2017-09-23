package org.stellasql.stella.gui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.stellasql.stella.query.TokenScanner;


public class SyntaxContainer
{
  private boolean bold = false;
  private boolean italic = false;
  private Color color = null;

  public SyntaxContainer(Object key)
  {
    bold = false;
    italic = false;

    Display display = Display.getCurrent();
    if (key == TokenScanner.LINECOMMENT)
      color = new Color(display, 0, 230, 0);
    else if (key == TokenScanner.BLOCKCOMMENT)
      color = new Color(display, 0, 230, 0);
    else if (key == TokenScanner.STRING)
      color = new Color(display, 255, 0, 0);
    else if (key == TokenScanner.NUMBER)
      color = new Color(display, 128, 0, 255);
    else if (key == TokenScanner.KEYWORD)
    {
      color = new Color(display, 0, 0, 255);
      bold = true;
    }
    else if (key == TokenScanner.OPERATOR)
      color = new Color(display, 0, 0, 0);
    else if (key == TokenScanner.SEPARATOR)
    {
      color = new Color(display, 255, 128, 0);
      bold = true;
    }

  }

  public SyntaxContainer(Color color, boolean bold, boolean italic)
  {
    this.color = color;
    this.bold = bold;
    this.italic = italic;
  }

  public SyntaxContainer(SyntaxContainer syntaxCon)
  {
    color = new Color(syntaxCon.getColor().getDevice(), syntaxCon.getColor().getRGB());
    bold = syntaxCon.getBold();
    italic = syntaxCon.getItalic();
  }

  public void dispose()
  {
    if (color != null)
      color.dispose();
  }

  public boolean getBold()
  {
    return bold;
  }

  public boolean getItalic()
  {
    return italic;
  }

  public Color getColor()
  {
    return color;
  }

  public void setBold(boolean bold)
  {
    this.bold = bold;
  }

  public void setColor(Color color)
  {
    this.color = color;
  }

  public void setItalic(boolean italic)
  {
    this.italic = italic;
  }

}
