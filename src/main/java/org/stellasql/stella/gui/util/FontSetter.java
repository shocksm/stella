package org.stellasql.stella.gui.util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.stellasql.stella.gui.custom.FontContainer;

public class FontSetter
{
  public static void setFont(Control control, FontContainer fc)
  {
    if (fc == null)
    {
      control.setFont(null);
      control.setForeground(null);
    }
    else
    {
      control.setFont(fc.getFont());
      control.setForeground(fc.getColor());
    }
  }


  public static void setAllControlFonts(Composite composite, FontContainer fc)
  {
    FontSetter.setFont(composite, fc);
    Control[] controls = composite.getChildren();
    for (int i = 0; i < controls.length; i++)
    {
      if (controls[i] instanceof Composite)
      {
        setAllControlFonts((Composite)controls[i], fc);
      }
      else
      {
        FontSetter.setFont(controls[i], fc);
      }
    }
  }

}
