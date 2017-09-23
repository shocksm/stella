package org.stellasql.stella.gui.util;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;

public class StellaClipBoard
{
  private static Clipboard cb = null;

  public static void init(Display display)
  {
    cb = new Clipboard(display);
  }

  public static Clipboard getClipBoard()
  {
    return cb;
  }

}
