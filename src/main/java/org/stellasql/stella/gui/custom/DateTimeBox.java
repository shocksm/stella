package org.stellasql.stella.gui.custom;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.gui.custom.datebox.DateBox;
import org.stellasql.stella.gui.custom.datebox.DateSelectedListener;

public class DateTimeBox extends Composite implements DateSelectedListener
{
  private List listenerList = new LinkedList();
  private TimeBox timeBox = null;
  private DateBox dateBox = null;

  public DateTimeBox(Composite parent)
  {
    super(parent, SWT.NONE);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    //gridLayout.horizontalSpacing = 0;
    setLayout(gridLayout);

    dateBox = new DateBox(this);
    dateBox.addDateSelectedListener(this);
    timeBox = new TimeBox(this);
    timeBox.addDateSelectedListener(this);
  }

  public static void main(String[] args)
  {
    Display display = new Display();

    Shell shell = new Shell(display);
    shell.setText("Test");
    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 5;
    gridLayout.marginWidth = 5;
    gridLayout.marginBottom = 5;
    gridLayout.horizontalSpacing = 0;
    shell.setLayout(gridLayout);

    new DateTimeBox(shell);

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
  public boolean setFocus()
  {
    return dateBox.setFocus();
  }

  public void addDateSelectedListener(DateSelectedListener listener)
  {
    listenerList.add(listener);
  }

  public void removeDateSelectedListener(DateSelectedListener listener)
  {
    listenerList.remove(listener);
  }

  public void notifyDateListeners()
  {
    Calendar cal = getDateTime();
    for (Iterator it = listenerList.iterator(); it.hasNext();)
    {
      DateSelectedListener listener = (DateSelectedListener)it.next();
      listener.dateSelected(cal);
    }
  }

  @Override
  public void dateSelected(Calendar calendar)
  {
    notifyDateListeners();
  }

  @Override
  public void addFocusListener(FocusListener listener)
  {
    dateBox.addFocusListener(listener);
    timeBox.addFocusListener(listener);
  }

  public void setDateTime(Calendar cal)
  {
    dateBox.setDate(cal);
    timeBox.setTime(cal);
  }

  public Calendar getDateTime()
  {
    Calendar cal = dateBox.getDate();
    if (cal == null)
      return null;

    Calendar time = timeBox.getTime();
    if (time == null)
      return null;

    cal.set(Calendar.AM_PM, time.get(Calendar.AM_PM));
    cal.set(Calendar.SECOND, time.get(Calendar.SECOND));
    cal.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
    cal.set(Calendar.HOUR, time.get(Calendar.HOUR));

    return cal;
  }

}
