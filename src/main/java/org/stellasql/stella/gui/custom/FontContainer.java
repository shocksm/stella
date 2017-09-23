package org.stellasql.stella.gui.custom;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class FontContainer
{
  private Font font = null;
  private Color color = null;

  public FontContainer(Font font, Color color)
  {
    this.font = font;
    this.color = color;
  }

  public void dispose()
  {
    if (font != null)
      font.dispose();
    if (color != null)
      color.dispose();
  }

  public Font getFont()
  {
    return font;
  }

  public Color getColor()
  {
    return color;
  }

}
