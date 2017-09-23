package org.stellasql.stella.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

public class TextKeyListener implements KeyListener
{
  private Text text = null;

  public TextKeyListener(Text text)
  {
    this.text = text;

    this.text.addKeyListener(this);
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL)
    {
      text.selectAll();
    }
  }
  @Override
  public void keyReleased(KeyEvent e)
  {
  }

}
