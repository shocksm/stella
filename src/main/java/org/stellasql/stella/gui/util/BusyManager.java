package org.stellasql.stella.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BusyManager
{
  static final String BUSYCOUNT_KEY = "BusyCount";

  /**
   * set the whole application to a busy state
   */
  public static synchronized void setBusy(Display display)
  {
    Shell[] shells = display.getShells();
    for (int i = 0; i < shells.length; i++)
    {
      setBusy(shells[i]);
    }
  }

  /**
   * set the control to a busy state
   */
  public static synchronized void setBusy(Control control)
  {
    Integer count = (Integer)control.getData(BUSYCOUNT_KEY);
    if (count == null)
      count = new Integer(1);
    else
      count = new Integer(count.intValue() + 1);

    control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
    control.setData(BUSYCOUNT_KEY, count);
  }

  public static synchronized void setNotBusy(Control control)
  {
    Integer count = (Integer)control.getData(BUSYCOUNT_KEY);
    if (count != null)
    {
      if (count.intValue() <= 1)
        count = null;
      else
        count = new Integer(count.intValue() - 1);
    }

    if (count == null)
      control.setCursor(null);
    control.setData(BUSYCOUNT_KEY, count);
  }


  public static synchronized void setNotBusy(Display display)
  {
    Shell[] shells = display.getShells();
    for (int i = 0; i < shells.length; i++)
    {
      setNotBusy(shells[i]);
    }
  }

}
