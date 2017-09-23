package org.stellasql.stella.gui.custom;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.gui.util.StellaClipBoard;


public class CustomCombo extends Composite implements MouseListener, FocusListener, KeyListener, ShellListener, MouseTrackListener
{
  private int maxLines = 15;
  private List text = null;
  private Button dropButton = null;
  private Shell dropShell = null;
  private List list = null;
  private int lines = maxLines;
  private int currentItemIndex = -1;
  private ArrayList selectionListeners = new ArrayList();
  private Composite textwrapper = null;

  public CustomCombo(Composite parent)
  {
    super(parent, SWT.BORDER);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 0;
    this.setLayout(gridLayout);

    textwrapper = new Composite(this, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 0;
    textwrapper.setLayout(gridLayout);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    gridData.grabExcessVerticalSpace = true;
    textwrapper.setLayoutData(gridData);

    text = new List(textwrapper, SWT.SINGLE);
    text.add("");
    Color color = text.getBackground();
    //text.setEditable(false);
    //text.setBackground(color);
    textwrapper.setBackground(color);
    text.addKeyListener(this);
    text.addMouseListener(this);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    gridData.grabExcessVerticalSpace = true;
    text.setLayoutData(gridData);
    text.addFocusListener(this);

    dropButton = new Button(this, SWT.ARROW | SWT.DOWN);
    dropButton.addMouseListener(this);
    gridData = new GridData();
    gridData.verticalAlignment = SWT.FILL;
    dropButton.setLayoutData(gridData);

    dropShell = new Shell(this.getShell(), SWT.TOOL);
    dropShell.setLayout(null);
    dropShell.addShellListener(this);

    list = new List(dropShell, SWT.SINGLE | SWT.V_SCROLL);
    list.addFocusListener(this);
    list.addMouseListener(this);
    list.addKeyListener(this);
    list.addMouseTrackListener(this);
    list.setToolTipText("");
  }

  public void setVisibleItemCount(int lines)
  {
    maxLines = lines;
  }

  public String[] getItems()
  {
    return list.getItems();
  }

  public String getItem(int index)
  {
    return list.getItem(index);
  }

  @Override
  public void setFont(Font font)
  {
    super.setFont(font);
    text.setFont(font);
    list.setFont(font);
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    text.setEnabled(enabled);
    dropButton.setEnabled(enabled);
  }

  @Override
  public void setForeground(Color color)
  {
    super.setForeground(color);
    text.setForeground(color);
    list.setForeground(color);
  }

  public void addSelectionListener(SelectionListener listener)
  {
    selectionListeners.add(listener);
  }

  private void listItemSelected()
  {
    if (list.getSelectionIndex() >= 0)
    {
      currentItemIndex = list.getSelectionIndex();
      text.removeAll();
      //text.setText(list.getItem(currentItemIndex));
      text.add(list.getItem(currentItemIndex));
      text.setToolTipText(list.getItem(currentItemIndex));
      //catalogText.selectAll();
      dropShell.setVisible(false);
      text.setFocus();

      for (Iterator it = selectionListeners.iterator(); it.hasNext();)
      {
        SelectionListener listener = (SelectionListener)it.next();
        Event e = new Event();
        e.widget = this;
        SelectionEvent se = new SelectionEvent(e);
        listener.widgetSelected(se);
      }
    }
    else
      currentItemIndex = -1;
  }

  public String getText()
  {
    return text.getItem(0);
  }

  public int getSelectionIndex()
  {
    return currentItemIndex;
  }

  public void select(int index)
  {
    list.setSelection(index);
    currentItemIndex = index;
    text.removeAll();
    //text.setText(list.getItem(currentItemIndex));
    text.add(list.getItem(currentItemIndex));
    text.setToolTipText(list.getItem(currentItemIndex));
    //text.select(0);
    text.deselectAll();
  }

  public int getItemCount()
  {
    return list.getItemCount();
  }

  public void add(String item)
  {
    list.add(item);
    if (list.getSelectionIndex() < 0)
    {
      list.setSelection(0);
      text.removeAll();
      //text.setText(list.getItem(0));
      text.add(list.getItem(0));
      text.setToolTipText(list.getItem(0));
    }
    else
    {
      int x = list.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
      GridData gridData = (GridData)text.getLayoutData();
      gridData.widthHint = x;
      text.setLayoutData(gridData);
    }

    if (list.getItemCount() < maxLines)
      lines = list.getItemCount();
    else
      lines = maxLines;
  }

  public void removeAll()
  {
    list.removeAll();
    text.removeAll();
    text.add("");
  }

  public boolean isDropped()
  {
    return dropShell.getVisible();
  }

  public void dropDown(boolean drop)
  {
    if (drop == isDropped())
      return;
    if (!drop)
    {
      dropShell.setVisible(false);
      text.setFocus();
      return;
    }

    text.deselectAll();
    list.select(currentItemIndex);
    list.setTopIndex(currentItemIndex);

    Point pt = this.getParent().toDisplay(this.getLocation());
    pt.y = pt.y + this.getSize().y;

    int sizeX = this.getSize().x;
    list.setSize(sizeX, list.getItemHeight() * lines);

    dropShell.setSize(sizeX,
        list.getItemHeight() * lines + dropShell.getBorderWidth() * 2);

    if (dropShell.getSize().x + pt.x > dropShell.getDisplay().getBounds().width)
      pt.x = dropShell.getDisplay().getBounds().width - dropShell.getSize().x;
    if (dropShell.getSize().y + pt.y > dropShell.getDisplay().getBounds().height)
      pt.y = this.getParent().toDisplay(this.getLocation()).y
         - dropShell.getSize().y;

    if (pt.x < 0)
      pt.x = 0;
    if (pt.y < 0)
      pt.y = 0;

    dropShell.setLocation(pt.x, pt.y);

    dropShell.open();
  }


  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
    if ((e.widget == text || e.widget == dropButton) && e.button == 1)
    {
      dropDown(!isDropped());
    }
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
    if (e.widget == list)
    {
      listItemSelected();
    }
  }

  @Override
  public void focusGained(FocusEvent e)
  {
    if (e.widget == text && text.getItemCount() > 0)
      text.select(0);
  }
  @Override
  public void focusLost(FocusEvent e)
  {
    dropDown(false);
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.widget == list && e.keyCode == SWT.CR)
    {
      listItemSelected();
    }
    else if (e.widget == text)
    {
      if (e.keyCode == SWT.ARROW_UP
          || e.keyCode == SWT.ARROW_DOWN)
      {
        dropDown(true);
        e.doit = false;
      }
    }
  }
  @Override
  public void keyReleased(KeyEvent e)
  {
  }

  @Override
  public void shellActivated(ShellEvent e)
  {
  }
  @Override
  public void shellClosed(ShellEvent e)
  {
    dropDown(false);
    e.doit = false;
  }
  @Override
  public void shellDeactivated(ShellEvent e)
  {
    dropDown(false);
    e.doit = false;
  }
  @Override
  public void shellDeiconified(ShellEvent e)
  {
  }
  @Override
  public void shellIconified(ShellEvent e)
  {
  }

  public static void main(String[] args)
  {
    Display display = new Display();
    StellaClipBoard.init(display);

    Shell shell = new Shell(display);
    shell.setText("Test");
    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 5;
    gridLayout.marginWidth = 5;
    gridLayout.marginBottom = 5;
    gridLayout.horizontalSpacing = 0;
    shell.setLayout(gridLayout);

    CustomCombo test = new CustomCombo(shell);
    for (int i = 1; i < 100; i++)
      test.add("Item " + i);

    shell.pack();

    shell.open();

    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  @Override
  public void mouseEnter(MouseEvent e)
  {
  }

  @Override
  public void mouseExit(MouseEvent e)
  {
  }

  @Override
  public void mouseHover(MouseEvent e)
  {
    if (list.getItemCount() > 0)
    {
      int index = list.getTopIndex() + (e.y / list.getItemHeight());
      if (index >= 0 && index < list.getItemCount())
        list.setToolTipText(list.getItem(index).replaceAll("\t", "  "));
      else
        list.setToolTipText("");
    }
    else
      list.setToolTipText("");
  }

}
