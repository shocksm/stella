package org.stellasql.stella.gui.custom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class CustomSpinner extends Canvas implements PaintListener, MouseMoveListener, ControlListener, DisposeListener, MouseListener, MouseTrackListener
{
  private final static int WIDTH = 15;
  private final static int HEIGHT = 17;

  private Color arrow = null;

  private Color border = null;
  private Color borderHover = null;
  private Color borderMouseDown = null;

  private Color bgLeft = null;
  private Color bgRight = null;

  private Color bgLeftHover = null;
  private Color bgRightHover = null;

  private Color bgLeftMouseDown = null;
  private Color bgRightMouseDown = null;

  private int height = 0;
  private int heightCenter = 0;
  private int width = 0;
  private int mouseY = 0;
  private boolean mouseIn = false;
  private boolean mouseDownTop = false;
  private boolean mouseDownBottom = false;

  private ScrollTask scrollTask = null;
  private List valueChangeListeners = new LinkedList();

  private static Timer timer = new Timer(true);

  public CustomSpinner(Composite parent)
  {
    super(parent, SWT.NONE);

    arrow = new Color(getDisplay(), 77, 97, 133);

    border = new Color(getDisplay(), 180, 200, 240);
    borderHover = new Color(getDisplay(), 155, 175, 224);
    borderMouseDown = new Color(getDisplay(), 140, 151, 220);

    bgLeft = new Color(getDisplay(), 201, 216, 252);
    bgRight = new Color(getDisplay(), 185, 203, 243);

    bgLeftHover = new Color(getDisplay(), 229, 246, 255);
    bgRightHover = new Color(getDisplay(), 202, 226, 253);

    bgLeftMouseDown = new Color(getDisplay(), 145, 155, 218);
    bgRightMouseDown = new Color(getDisplay(), 155, 175, 239);

    addPaintListener(this);
    addMouseListener(this);
    addMouseMoveListener(this);
    addMouseTrackListener(this);
    addControlListener(this);

    addDisposeListener(this);
  }

  @Override
  public Point computeSize(int wHint, int hHint)
  {
    if (hHint < HEIGHT)
      hHint = HEIGHT;
    return new Point(WIDTH, hHint);
  }

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed)
  {
    return computeSize(wHint, hHint);
  }

  private void drawButton(GC gc, int y, int width, int height, boolean top, boolean mouseIn, boolean mouseDown)
  {
    if (mouseDown)
      gc.setForeground(borderMouseDown);
    else if (mouseIn)
      gc.setForeground(borderHover);
    else
      gc.setForeground(border);

    gc.drawRectangle(0, y, width - 1, height);

    if (mouseDown)
    {
      gc.setForeground(bgLeftMouseDown);
      gc.setBackground(bgRightMouseDown);
    }
    else if (mouseIn)
    {
      gc.setForeground(bgLeftHover);
      gc.setBackground(bgRightHover);
    }
    else
    {
      gc.setForeground(bgLeft);
      gc.setBackground(bgRight);
    }
    gc.fillGradientRectangle(1, y + 1, width - 2, height - 1, false);


    int inc = top ? 1 : -1;
    int centerX = width / 2;
    int centerY = height / 2 - 1 + y;

    if (!top)
      centerY += 3;

    if (top && centerY <= y)
      centerY = y;
    else if (!top && centerY <= y + 4)
      centerY = y + 4;


    gc.setForeground(arrow);
    gc.drawLine(centerX, centerY, centerX, centerY);
    centerY += inc;
    gc.drawLine(centerX-1, centerY, centerX+1, centerY);
    centerY += inc;
    gc.drawLine(centerX-2, centerY, centerX+2, centerY);
    centerY += inc;
    gc.drawLine(centerX-3, centerY, centerX-1, centerY);
    gc.drawLine(centerX+1, centerY, centerX+3, centerY);
  }

  private void drawTop(GC gc)
  {
    boolean mouseIn = false;

    if (this.mouseIn && mouseY < heightCenter)
      mouseIn = true;

    drawButton(gc, 0, width, heightCenter - 1, true, mouseIn, mouseIn && mouseDownTop);
  }

  private void drawBottom(GC gc)
  {
    int y = heightCenter + 1;
    int height = this.height - y - 1;
    boolean mouseIn = false;
    if (this.mouseIn && mouseY > heightCenter)
      mouseIn = true;

    drawButton(gc, y, width, height, false, mouseIn, mouseIn && mouseDownBottom);
  }

  @Override
  public void paintControl(PaintEvent e)
  {
    drawTop(e.gc);
    drawBottom(e.gc);
  }


  @Override
  public void mouseMove(MouseEvent e)
  {
    boolean draw = false;
    if (mouseIn)
    {
      if (e.y < heightCenter && mouseY >= heightCenter
          || e.y > heightCenter && mouseY <= heightCenter)
      {
        draw = true;
      }
    }

    mouseY = e.y;

    if (e.x >= 0 && e.x < width
        && e.y >= 0 && e.y < height)
    {
      if (!mouseIn)
        draw = true;
      mouseIn = true;
    }
    else
    {
      if (mouseIn)
        draw = true;
      mouseIn = false;
      cancelIncrementing();
    }

    if (mouseDownTop && mouseIn && scrollTask != null
        && e.y > heightCenter)
    {
      cancelIncrementing();
    }
    else if (mouseDownTop && mouseIn && scrollTask == null
        && e.y < heightCenter)
    {
      startIncrementing(1);
    }

    if (mouseDownBottom && mouseIn && scrollTask != null
        && e.y < heightCenter)
    {
      cancelIncrementing();
    }
    else if (mouseDownBottom && mouseIn && scrollTask == null
        && e.y > heightCenter)
    {
      startIncrementing(-1);
    }

    if (draw)
    {
      redraw();
    }
  }

  @Override
  public void controlMoved(ControlEvent e)
  {
  }

  @Override
  public void controlResized(ControlEvent e)
  {
    Rectangle bounds = getBounds();
    heightCenter = bounds.height / 2;
    height = bounds.height;
    width = bounds.width;
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
    if (mouseY < heightCenter)
    {
      mouseDownTop = true;
      startIncrementing(1);
      redraw();
    }
    else if (mouseY > heightCenter)
    {
      mouseDownBottom = true;
      startIncrementing(-1);

      redraw();
    }
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
    cancelIncrementing();
    mouseDownTop = false;
    mouseDownBottom = false;
    redraw();
  }

  @Override
  public void mouseEnter(MouseEvent e)
  {
    mouseIn = true;
    redraw();
  }
  @Override
  public void mouseExit(MouseEvent e)
  {
    mouseIn = false;
    cancelIncrementing();
    redraw();
  }
  @Override
  public void mouseHover(MouseEvent e)
  {
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    arrow.dispose();
    border.dispose();
    borderHover.dispose();
    borderMouseDown.dispose();
    bgLeft.dispose();
    bgRight.dispose();
    bgLeftHover.dispose();
    bgRightHover.dispose();
    bgLeftMouseDown.dispose();
    bgRightMouseDown.dispose();
  }

  public void addValueChangeListener(ValueChangeListener listener)
  {
    valueChangeListeners.add(listener);
  }

  private void startIncrementing(int amount)
  {
    cancelIncrementing();
    scrollTask = new ScrollTask(amount);
    timer.schedule(scrollTask, 200, 150);
    notifyValueChanged(amount);
  }

  private void cancelIncrementing()
  {
    if (scrollTask != null)
    {
      scrollTask.cancel();
      scrollTask = null;
    }
  }

  private void notifyValueChanged(int amount)
  {
    for (Iterator it = valueChangeListeners.iterator(); it.hasNext();)
    {
      ValueChangeListener listener = (ValueChangeListener)it.next();
      listener.valueChanged(amount);
    }

  }

  private class ScrollTask extends TimerTask
  {
    private int amount;
    private int change = 1;
    public ScrollTask(int amount)
    {
      this.amount = amount;
    }

    @Override
    public void run()
    {
      if (change >= 0)
      {
        if (change >= 60)
        {
          amount = amount * 2;
          change = -1;
        }
        else if (change == 40)
        {
          amount = amount * 2;
        }
        else if (change == 20)
        {
          amount = amount * 5;
        }
        change++;
      }
      getDisplay().syncExec(new Runnable(){
        @Override
        public void run() {
          notifyValueChanged(amount);
        }
      });
    }
  }


}
