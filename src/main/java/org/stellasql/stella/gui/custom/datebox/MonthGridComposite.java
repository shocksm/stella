package org.stellasql.stella.gui.custom.datebox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MonthGridComposite extends Composite implements DisposeListener
{
  private Calendar selectedCal = null;
  private DayOfMonthComposite daysArray[] = null;
  private List listenerList = new LinkedList();

  private Color dayNameBg = null;
  private Color dayNameFg = null;
  private Color selectedBg = null;
  private Color selectedFg = null;
  private Color weekdayBg = null;
  private Color weekdayFg = null;
  private Color weekendBg = null;
  private Color weekendFg = null;
  private Color hoverBg = null;
  private Color hoverFg = null;

  public MonthGridComposite(Composite parent)
  {
    this(parent, Calendar.getInstance(TimeZone.getDefault()));
  }

  public MonthGridComposite(Composite parent, Calendar calendar)
  {
    super(parent, SWT.NONE);

    addDisposeListener(this);

    dayNameBg = new Color(this.getDisplay(), 122, 150, 253);
    dayNameFg = new Color(this.getDisplay(), 255, 255, 255);
    selectedBg = new Color(this.getDisplay(), 0, 0, 255);
    selectedFg = new Color(this.getDisplay(), 255, 255, 255);
    weekdayBg = new Color(this.getDisplay(), 240, 240, 240);
    weekdayFg = new Color(this.getDisplay(), 0, 0, 0);
    weekendBg = new Color(this.getDisplay(), 220, 220, 220);
    weekendFg = new Color(this.getDisplay(), 0, 0, 0);
    hoverBg = new Color(this.getDisplay(), 255, 255, 0);
    hoverFg = new Color(this.getDisplay(), 0, 0, 0);

    selectedCal = calendar;

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 7;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 1;
    gridLayout.verticalSpacing = 1;
    setLayout(gridLayout);

    setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    createDaysOfWeek();
    daysArray = createDaysOfMonth();

    calcDays();
    setSelectedDay(selectedCal.get(Calendar.DAY_OF_MONTH));
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    dayNameBg.dispose();
    dayNameFg.dispose();
    selectedBg.dispose();
    selectedFg.dispose();
    weekdayBg.dispose();
    weekdayFg.dispose();
    weekendBg.dispose();
    weekendFg.dispose();
    hoverBg.dispose();
    hoverFg.dispose();
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
    for (Iterator it = listenerList.iterator(); it.hasNext();)
    {
      DateSelectedListener listener = (DateSelectedListener)it.next();
      listener.dateSelected(selectedCal);
    }
  }

  public void setDayOfMonth(int day)
  {
    selectedCal.set(Calendar.DAY_OF_MONTH, day);
    setSelectedDay(day);
  }

  private void setSelectedDay(int day)
  {
    for (int index = 0; index < daysArray.length; index++)
    {
      if (daysArray[index].getDayType() != DayOfMonthComposite.NONMONTH
         && daysArray[index].getDay() == day)
      {
        daysArray[index].setSelected(true);
      }
      else if (daysArray[index].isSelected())
      {
        daysArray[index].setSelected(false);
      }

    }
  }

  public void setMonth(int month)
  {
    int day = selectedCal.get(Calendar.DAY_OF_MONTH);
    int max = 0;
    selectedCal.set(Calendar.DAY_OF_MONTH, 1);
    selectedCal.set(Calendar.MONTH, month);
    max = selectedCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    if (day > max)
       day = max;
    selectedCal.set(Calendar.DAY_OF_MONTH, day);

    calcDays();
    setSelectedDay(day);
  }

  public void setYear(int year)
  {
    int day = selectedCal.get(Calendar.DAY_OF_MONTH);
    int max = 0;
    selectedCal.set(Calendar.DAY_OF_MONTH, 1);
    selectedCal.set(Calendar.YEAR, year);
    max = selectedCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    if (day > max)
       day = max;
    selectedCal.set(Calendar.DAY_OF_MONTH, day);

    calcDays();
    setSelectedDay(day);
  }

  private void createDaysOfWeek()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

    DayNameComposite dayName[] = new DayNameComposite[7];
    int minWidth = 0;

    for (int index = 0; index < 7; index++)
    {
      dayName[index] = new DayNameComposite(this, sdf.format(calendar.getTime()));
      if (dayName[index].computeSize(SWT.DEFAULT, SWT.DEFAULT).x > minWidth)
         minWidth = dayName[index].computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

      calendar.add(Calendar.DAY_OF_WEEK, 1);
    }

    Point pt = new Point(minWidth, dayName[0].computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    for (int index = 0; index < 7; index++)
    {
      GridData gridData = new GridData();
      gridData.widthHint = pt.x;
      gridData.heightHint = pt.y;
      dayName[index].setLayoutData(gridData);
    }
  }

  private DayOfMonthComposite[] createDaysOfMonth()
  {
    DayOfMonthComposite dayOfMonth[] = new DayOfMonthComposite[42];
    for (int iCount = 0; iCount < dayOfMonth.length; iCount++)
    {
      dayOfMonth[iCount] = new DayOfMonthComposite(this);
    }
    return dayOfMonth;
  }


  private void calcDays()
  {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.MONTH, selectedCal.get(Calendar.MONTH));
    calendar.set(Calendar.YEAR, selectedCal.get(Calendar.YEAR));

    if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
    {
      int daysBefore = this.getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)) - 2;
      calendar.add(Calendar.MONTH, -1);
      calendar.set(Calendar.DAY_OF_MONTH,
      calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - daysBefore);
    }

    for (int index = 0; index < daysArray.length; index++)
    {
      daysArray[index].setDay(calendar.get(Calendar.DAY_OF_MONTH));

      if (calendar.get(Calendar.MONTH) != selectedCal.get(Calendar.MONTH))
        daysArray[index].setDayType(DayOfMonthComposite.NONMONTH);
      else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
           || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
        daysArray[index].setDayType(DayOfMonthComposite.WEEKEND);
      else
        daysArray[index].setDayType(DayOfMonthComposite.WEEKDAY);

      calendar.add(Calendar.DAY_OF_MONTH, 1);
    }


  }

  private int getDayOfWeek(int day)
  {
    int retVal = 0;
    switch (day)
    {
      case Calendar.SUNDAY:
        retVal = 1;
        break;
      case Calendar.MONDAY:
        retVal = 2;
        break;
      case Calendar.TUESDAY:
        retVal = 3;
        break;
      case Calendar.WEDNESDAY:
        retVal = 4;
        break;
      case Calendar.THURSDAY:
        retVal = 5;
        break;
      case Calendar.FRIDAY:
        retVal = 6;
        break;
      case Calendar.SATURDAY:
        retVal = 7;
        break;
    }

    return retVal;
  }

  private class DayNameComposite extends Composite
  {
    private Label label = null;

    public DayNameComposite(Composite parent, String text)
    {
      super(parent, SWT.NONE);

      GridLayout gridLayout = new GridLayout();
      gridLayout.marginHeight = 2;
      gridLayout.marginWidth = 2;
      this.setLayout(gridLayout);

      this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      this.setBackground(dayNameBg);

      label = new Label(this, SWT.NONE);
      label.setAlignment(SWT.CENTER);
      label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      label.setText(text);
      label.setBackground(dayNameBg);
      label.setForeground(dayNameFg);
    }
  }

  private class DayOfMonthComposite extends Composite implements MouseListener, MouseTrackListener
  {
    private Label label = null;
    private int day = 0;
    private int type = 0;
    private boolean selected = false;

    public final static int NONMONTH = 0;
    public final static int WEEKDAY = 1;
    public final static int WEEKEND = 2;

    public DayOfMonthComposite(Composite parent)
    {
      super(parent, SWT.NONE);

      GridLayout gridLayout = new GridLayout();
      gridLayout.marginHeight = 1;
      gridLayout.marginWidth = 1;
      this.setLayout(gridLayout);

      this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      setBackground(this.getDisplay().getSystemColor(SWT.COLOR_BLACK));

      label = new Label(this, SWT.NONE);
      label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      label.setText("*");

      label.addMouseListener(this);
      addMouseListener(this);

      label.addMouseTrackListener(this);
      addMouseTrackListener(this);
    }

    public void setSelected(boolean bSelected)
    {
      selected = bSelected;
      if (selected)
      {
        label.setBackground(selectedBg);
        label.setForeground(selectedFg);
      }
      else
      {
        resetColors();
      }
    }

    public boolean isSelected()
    {
      return selected;
    }

    public void setDay(int day)
    {
      this.day = day;
      label.setText(" " + day);
    }

    public int getDay()
    {
      return day;
    }

    public void setDayType(int type)
    {
      this.type = type;
      if (this.type == NONMONTH)
      {
        this.setVisible(false);
      }
      else if (this.type == WEEKDAY)
      {
        label.setBackground(weekdayBg);
        label.setForeground(weekdayFg);
        this.setVisible(true);
      }
      else if (this.type == WEEKEND)
      {
        label.setBackground(weekendBg);
        label.setForeground(weekendFg);
        this.setVisible(true);
      }
    }

    public void setHover()
    {
      label.setBackground(hoverBg);
      label.setForeground(hoverFg);
    }

    public int getDayType()
    {
      return type;
    }

    public void resetColors()
    {
      setDayType(type);
    }

    @Override
    public void mouseDoubleClick(MouseEvent e)
    {
    }
    @Override
    public void mouseDown(MouseEvent e)
    {
      if (type != NONMONTH)
      {
        setDayOfMonth(day);
        notifyDateListeners();
      }
    }
    @Override
    public void mouseUp(MouseEvent e)
    {
    }

    @Override
    public void mouseEnter(MouseEvent e)
    {
      setHover();
    }

    @Override
    public void mouseExit(MouseEvent e)
    {
      setSelected(selected);
    }

    @Override
    public void mouseHover(MouseEvent e)
    {
    }
  }


}
