package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.stellasql.stella.session.SessionData;

public class KeyListener implements Listener
{
  private static KeyListener keyListener = null;
  private boolean init = false;
  private boolean keyReleased = true;

  private KeyListener()
  {
  }

  public static synchronized KeyListener getInstance()
  {
    if (keyListener == null)
    {
      keyListener = new KeyListener();
    }

    return keyListener;
  }

  public synchronized void init(Display display)
  {
    if (!init)
    {
      // listen for the traverse keys for the session tabs
      // and the results tabs
      display.addFilter(SWT.Traverse, new TraverseEater());
      display.addFilter(SWT.KeyDown, this);
      display.addFilter(SWT.KeyUp, this);
      init = true;
    }
  }

  @Override
  public void handleEvent(Event e)
  {
    if (e.type == SWT.KeyDown && processMappedKeyEvent(e))
    {
      e.type = SWT.NONE;
      e.doit = false;
    }
    else if (e.type == SWT.KeyUp)
    {
      keyReleased = true;
    }
  }

  private boolean isMappedKeyEvent(Event e)
  {
    boolean mapped = false;
    if (e.keyCode == SWT.TAB
        && (e.stateMask == SWT.MOD1 || e.stateMask == (SWT.MOD1 | SWT.SHIFT)))
    {
      mapped = true;
    }
    else if ((e.keyCode == SWT.PAGE_UP || e.keyCode == SWT.PAGE_DOWN)
        && (e.stateMask == SWT.CONTROL))
    {
      mapped = true;
    }

    return mapped;
  }

  private boolean processMappedKeyEvent(Event e)
  {
    boolean mapped = false;

    // if a model shell is being displayed then don't process
    // any global keys
    for (int i = 0; i < Stella.getInstance().getShell().getShells().length; i++)
    {
      int style = Stella.getInstance().getShell().getShells()[i].getStyle();
      if ((style & SWT.APPLICATION_MODAL) > 0
          || (style & SWT.PRIMARY_MODAL) > 0
          || (style & SWT.SYSTEM_MODAL) > 0)
      {
        return false;
      }
    }

    CTabFolder tabFolder = Stella.getInstance().getSessionTabComposite().getTabFolder();
    if (e.keyCode == SWT.TAB
        && (e.stateMask == SWT.MOD1 || e.stateMask == (SWT.MOD1 | SWT.SHIFT)))
    {
      mapped = true;
      if (tabFolder.getSelection() != null
          && tabFolder.getItemCount() > 1)
      {
        int index = tabFolder.indexOf(tabFolder.getSelection());

        if (e.stateMask == SWT.MOD1)
          index++;
        else
          index--;

        if (index >= tabFolder.getItemCount())
          index = 0;
        if (index < 0)
          index = tabFolder.getItemCount() - 1;

        tabFolder.setSelection(index);
        String sessionName = (String)tabFolder.getSelection().getData();
        SessionData.getSessionData(sessionName).getSQLControl().setFocus();
      }
    }
    else if (e.keyCode == 'w' && e.stateMask == SWT.CONTROL)
    {
      mapped = true;
      if (keyReleased) {
      	if (Stella.getInstance().getSelectedSessionData() != null) {
      		Stella.getInstance().getSelectedSessionData().getResultTabHandler().closeSelectedTab();
      	}
      }
      keyReleased = false;
    }
    else if (e.keyCode == SWT.PAGE_UP && e.stateMask == SWT.CONTROL)
		{
			mapped = true;
			Stella.getInstance().getSelectedSessionData().getResultTabHandler().selectNextTab();
		}
    else if (e.keyCode == SWT.PAGE_DOWN && e.stateMask == SWT.CONTROL)
		{
			mapped = true;
			Stella.getInstance().getSelectedSessionData().getResultTabHandler().selectPreviousTab();
		}
    else if (e.keyCode == SWT.CR
        && e.stateMask == SWT.CONTROL
        || e.keyCode == SWT.F5)
    {
      mapped = true;
      if (keyReleased && Stella.getInstance().getSelectedSessionData() != null)
        Stella.getInstance().getSelectedSessionData().getSQLActionHandler().execute();
      keyReleased = false;
    }
    else if (e.keyCode == SWT.CR
        && (e.stateMask == (SWT.CONTROL | SWT.SHIFT)))
    {
      mapped = true;
      if (keyReleased && Stella.getInstance().getSelectedSessionData() != null)
        Stella.getInstance().getSelectedSessionData().getSQLActionHandler().executeExport();
      keyReleased = false;
    }
    else if (e.keyCode == 'h'
        && (e.stateMask == SWT.CONTROL))
    {
      mapped = true;
      if (keyReleased)
        Stella.getInstance().displayHistory();
      keyReleased = false;
    }
    else if (e.keyCode == 'f'
      && (e.stateMask == SWT.CONTROL))
    {
      mapped = true;
      if (keyReleased)
        Stella.getInstance().displayFavorites();
      keyReleased = false;
    }

//    else if (e.keyCode == 'd'
//      && (e.stateMask == SWT.CONTROL))
//    {
//      mapped = true;
//      if (keyReleased)
//        Stella.getInstance().addFavorite();
//      keyReleased = false;
//    }


    return mapped;
  }

  /**
   * This is needed to eat the traverse key combos that also map to a
   * key event in order for the key events to be
   * passed on to the SWT.KeyDown listener
   *
   */
  private class TraverseEater implements Listener
  {
    @Override
    public void handleEvent(Event e)
    {
      if (isMappedKeyEvent(e))
      {
        e.type = SWT.NONE;
        e.doit = false;
      }
    }
  }

}
