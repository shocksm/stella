package org.stellasql.stella.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class StyledTextContextMenu implements MenuListener, SelectionListener, KeyListener
{
  private StyledText styledText = null;
  private Menu menu = null;
  private MenuItem copyMI;
  private MenuItem selectAllMI;

  public StyledTextContextMenu(StyledText st)
  {
    styledText = st;
    menu = new Menu(styledText);
    menu.addMenuListener(this);

    styledText.setMenu(menu);
    styledText.addKeyListener(this);

    copyMI = new MenuItem(menu, SWT.PUSH);
    copyMI.addSelectionListener(this);
    copyMI.setText("&Copy\tCtrl+C");

    selectAllMI = new MenuItem(menu, SWT.PUSH);
    selectAllMI.addSelectionListener(this);
    selectAllMI.setText ("Select &All\tCtrl+A");
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }
  @Override
  public void menuShown(MenuEvent e)
  {
    copyMI.setEnabled(styledText.getSelectionCount() > 0);
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }
  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == copyMI)
      styledText.copy();
    else if (e.widget == selectAllMI)
      styledText.selectAll();
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL)
    {
      styledText.selectAll();
    }
  }
  @Override
  public void keyReleased(KeyEvent e)
  {
  }

}
