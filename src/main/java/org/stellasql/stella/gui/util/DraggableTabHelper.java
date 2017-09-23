package org.stellasql.stella.gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 *
 * This will lose any data stored on the tab with the setData(key, value) methods when a tab is moved.
 * I couldn't find a way to get to that data without knowing the keys.
 *
 * The data stored with setData(value) will be preserved.
 *
 * @author Sam Shockey
 *
 */
public class DraggableTabHelper implements MouseListener, MouseMoveListener, ShellListener
{
  private CTabFolder tabFolder = null;
  private CTabItem dragSource = null;
  private GC displayGC = null;
  private Image arrowImage = null;
  private int arrowX = -100;
  private int arrowY;
  private int mouseX;
  private int mouseY;
  private int arrowHeight = 11;
  private int arrowWidth = 9;
  private Cursor dragCursor = null;
  private boolean mouseDown = false;
  private boolean cursorNo = false;

  public DraggableTabHelper(CTabFolder tabFolder)
  {
    this.tabFolder = tabFolder;
    displayGC = new GC(this.tabFolder.getDisplay());
    arrowImage = StellaImages.getInstance().getTabArrowImage();
    dragCursor = StellaImages.getInstance().getTabDragCursor();

    this.tabFolder.addMouseListener(this);
    this.tabFolder.addMouseMoveListener(this);
    this.tabFolder.getShell().addShellListener(this);
  }

  private void drawArrow()
  {
    Point pt = new Point(mouseX, mouseY);
    CTabItem item = tabFolder.getItem(pt);
    if (item != null && item != dragSource)
    {
      int arrowX = 0;
      Rectangle rect = item.getBounds();
      if (rect.x + (rect.width / 2) > pt.x)
      {
        // draw to left
        arrowX = rect.x - arrowWidth / 2;
      }
      else
      {
        // draw to right
        arrowX = rect.x + rect.width  - arrowWidth / 2;
      }

      if (arrowX != this.arrowX)
      {
        int oldArrowX = this.arrowX;
        int oldArrowY = arrowY;
        this.arrowX = arrowX;
        arrowY = rect.y - arrowHeight - 1;

        // tell shell to update area of last arrow
        Point displayPt = tabFolder.toDisplay(oldArrowX, oldArrowY);
        pt = tabFolder.getShell().toControl(displayPt.x, displayPt.y);
        tabFolder.getShell().redraw(pt.x, pt.y, arrowWidth, arrowHeight, true);
        tabFolder.getShell().update();

        // draw new arrow
        pt = tabFolder.toDisplay(this.arrowX, arrowY);
        displayGC.drawImage(arrowImage, pt.x, pt.y);
      }
    }
  }

  private void clearArrow()
  {
    if (arrowX > -100)
    {
      Point pt = tabFolder.toDisplay(arrowX, arrowY);
      pt = tabFolder.getShell().toControl(pt.x, pt.y);
      tabFolder.getShell().redraw(pt.x, pt.y, arrowWidth, arrowHeight, true);
      tabFolder.getShell().update();
    }
    arrowX = -100;
  }

  private void dragStop()
  {
    mouseDown = false;
    tabFolder.getShell().setCursor(null);
    clearArrow();
    dragSource = null;
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
    dragSource = tabFolder.getItem(new Point(e.x, e.y));
    if (dragSource != null && e.button == 1)
    {
      mouseDown = true;
      cursorNo = false;
      tabFolder.getShell().setCursor(dragCursor);
    }
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
    if (mouseDown)
    {
      Point pt = new Point(e.x, e.y);
      CTabItem dropItem = tabFolder.getItem(pt);

      if (dropItem == dragSource)
      {
        dropItem = null;
      }

      if (dropItem != null && dragSource != null
          && !dropItem.isDisposed() && !dragSource.isDisposed())
      {
        int dropIndex = 0;
        int dragIndex = 0;

        CTabItem[] items = tabFolder.getItems();
        for (int i = 0; i < items.length; i++)
        {
          if (items[i] == dropItem)
          {
            dropIndex = i;
            break;
          }
        }

        for (int i = 0; i < items.length; i++)
        {
          if (items[i] == dragSource)
          {
            dragIndex = i;
            break;
          }
        }

        Rectangle rect = dropItem.getBounds();
        if (rect.x + (rect.width / 2) <= pt.x)
          dropIndex++;

        if (dragIndex < dropIndex)
          dropIndex--;

        if (dropIndex != dragIndex)
        {
          String text = dragSource.getText();
          Image image = dragSource.getImage();
          Control control = dragSource.getControl();
          Object itemData = dragSource.getData();
          dragSource.dispose();

          CTabItem item = new CTabItem(tabFolder, SWT.CLOSE, dropIndex);
          item.setData(itemData);
          item.setText(text);
          item.setImage(image);
          item.setControl(control);

          tabFolder.setSelection(item);
        }
      }

      dragStop();
    }
  }

  @Override
  public void mouseMove(MouseEvent e)
  {
    if (!mouseDown)
      return;

    if ((e.x < 0 || e.y < 0)
        || (e.x > tabFolder.getSize().x || e.y > tabFolder.getSize().y))
    {
      if (!cursorNo)
      {
        cursorNo = true;
        Cursor cursor = tabFolder.getDisplay().getSystemCursor(SWT.CURSOR_NO);
        tabFolder.getShell().setCursor(cursor);
      }
    }
    else if (cursorNo)
    {
      cursorNo = false;
      tabFolder.getShell().setCursor(dragCursor);
    }

    mouseX = e.x;
    mouseY = e.y;

    if (tabFolder.getItem(new Point(e.x, e.y)) != null)
    {
      drawArrow();
    }
    else
    {
      clearArrow();
    }
  }


  @Override
  public void shellActivated(ShellEvent e)
  {
  }
  @Override
  public void shellClosed(ShellEvent e)
  {
  }
  @Override
  public void shellDeactivated(ShellEvent e)
  {
    if (mouseDown)
    {
      dragStop();
    }
  }
  @Override
  public void shellDeiconified(ShellEvent e)
  {
  }
  @Override
  public void shellIconified(ShellEvent e)
  {
  }


}
