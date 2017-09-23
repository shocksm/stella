package org.stellasql.stella.gui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class StellaColors
{
  private static StellaColors stellaColors = null;
  private Color lightGray = null;

  private StellaColors()
  {
  }

  public static synchronized StellaColors getInstance()
  {
    if (stellaColors == null)
      stellaColors = new StellaColors();

    return stellaColors;
  }

  public Color getLightGray()
  {
    if (lightGray == null)
      lightGray = new Color(Display.getCurrent(), 240, 240, 240);
    return lightGray;
  }


}
