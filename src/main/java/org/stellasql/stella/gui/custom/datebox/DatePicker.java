package org.stellasql.stella.gui.custom.datebox;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.gui.custom.CustomSpinner;
import org.stellasql.stella.gui.custom.ValueChangeListener;

public class DatePicker extends Composite implements DateSelectedListener, SelectionListener, VerifyListener, ModifyListener, ValueChangeListener, KeyListener
{
  private List listenerList = new LinkedList();
  private Combo monthCombo = null;

  private GregorianCalendar calendar = null;
  private MonthGridComposite monthGrid = null;

  private Text yearText = null;
  private int minYear;
  private int maxYear;

  public DatePicker(Composite parent)
  {
    this(parent, 1, 9999);
  }

  public DatePicker(Composite parent, int minYear, int maxYear)
  {
    super(parent, SWT.NONE);
    this.minYear = minYear;
    this.maxYear = maxYear;

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    //gridLayout.marginHeight = 0;
    //gridLayout.marginWidth = 0;
    //gridLayout.horizontalSpacing = 1;
    gridLayout.verticalSpacing = 2;
    setLayout(gridLayout);

    listenerList = new Vector();
    calendar = new GregorianCalendar();

    monthCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
    monthCombo.setVisibleItemCount(12);
    addMonthValues();
    monthCombo.select(calendar.get(Calendar.MONTH));
    monthCombo.addSelectionListener(this);
    monthCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

    // Year Spinner
    Composite composite = new Composite(this, SWT.BORDER);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

    yearText = new Text(composite, SWT.NONE);
    yearText.setText("" + calendar.get(Calendar.YEAR));
    yearText.setTextLimit(4);
    yearText.addVerifyListener(this);
    yearText.addModifyListener(this);
    yearText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    yearText.addKeyListener(this);

    composite.setBackground(yearText.getBackground());

    CustomSpinner spinner = new CustomSpinner(composite);
    GridData gd = new GridData();
    gd.verticalAlignment = SWT.FILL;
    gd.grabExcessVerticalSpace = true;
    spinner.setLayoutData(gd);
    spinner.addValueChangeListener(this);
    gd = new GridData();
    gd.verticalAlignment = SWT.FILL;
    gd.grabExcessVerticalSpace = true;
    spinner.setLayoutData(gd);


    monthGrid = new MonthGridComposite(this);
    monthGrid.addDateSelectedListener(this);
    monthGrid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

    setBackground(monthGrid.getBackground());
  }


  private void addMonthValues()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    for (int month = 0; month < 12; month++)
    {
      cal.set(Calendar.MONTH, month);
      monthCombo.add(sdf.format(cal.getTime()));
    }
  }

  public void setCalendar(Calendar cal)
  {
    calendar.setTime(cal.getTime());
    monthCombo.select(calendar.get(Calendar.MONTH));
    yearText.setText("" + calendar.get(Calendar.YEAR));
    monthGrid.setYear(calendar.get(Calendar.YEAR));
    monthGrid.setMonth(calendar.get(Calendar.MONTH));
    monthGrid.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
  }


  @Override
  public void dateSelected(Calendar cal)
  {
    notifyDateListeners(cal);
  }

  public void addDateSelectedListener(DateSelectedListener listener)
  {
    listenerList.add(listener);
  }

  public void removeDateSelectedListener(DateSelectedListener listener)
  {
    listenerList.remove(listener);
  }

  public void notifyDateListeners(Calendar cal)
  {
    for (Iterator it = listenerList.iterator(); it.hasNext();)
    {
      DateSelectedListener listener = (DateSelectedListener)it.next();
      listener.dateSelected(cal);
    }
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == monthCombo)
    {
      calendar.set(Calendar.MONTH, monthCombo.getSelectionIndex());
      monthGrid.setMonth(calendar.get(Calendar.MONTH));
    }
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    widgetSelected(e);
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.widget == yearText)
    {
      if (e.keyCode == SWT.ARROW_DOWN)
      {
        valueChanged(-1, false);
        e.doit = false;
      }
      else if (e.keyCode == SWT.ARROW_UP)
      {
        valueChanged(1, false);
        e.doit = false;
      }
      else if (e.keyCode == SWT.PAGE_DOWN)
      {
        valueChanged(-10, false);
        e.doit = false;
      }
      else if (e.keyCode == SWT.PAGE_UP)
      {
        valueChanged(10, false);
        e.doit = false;
      }
    }

  }
  @Override
  public void keyReleased(KeyEvent e)
  {
  }

  private void valueChanged(int amount, boolean select)
  {
    int year = calendar.get(Calendar.YEAR);
    year += amount;
    if (year > maxYear)
      year = minYear;
    if (year < minYear)
      year = maxYear;

    yearText.setText("" + year);
    calendar.set(Calendar.YEAR, year);
    monthGrid.setYear(calendar.get(Calendar.YEAR));

    yearText.setFocus();
    if (select)
      yearText.selectAll();
  }

  @Override
  public void valueChanged(int amount)
  {
    valueChanged(amount, true);
  }

  @Override
  public void modifyText(ModifyEvent e)
  {
    if (e.widget ==  yearText)
    {
      if (yearText.getText().length() > 0)
      {
        int year = Integer.parseInt(yearText.getText());
        calendar.set(Calendar.YEAR, year);
        monthGrid.setYear(calendar.get(Calendar.YEAR));
      }
    }
  }

  @Override
  public void verifyText(VerifyEvent e)
  {
    for (int index = 0; index < e.text.length(); index++)
    {
      char c = e.text.charAt(index);
      if (!Character.isDigit(c))
      {
        e.doit = false;
        break;
      }
    }

    if (e.doit == true)
    {
      StringBuffer sbuf = new StringBuffer(((Text)e.widget).getText());
      sbuf.replace(e.start, e.end, e.text);
      if (sbuf.length() > 0)
      {
        try
        {
          int value = Integer.parseInt(sbuf.toString());
          if (e.widget == yearText)
          {
            if (value < minYear || value > maxYear)
              e.doit = false;
          }
        }
        catch (NumberFormatException e1)
        {
          e.doit = false;
        }
      }
    }
  }


  public static void main(String[] args)
  {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new GridLayout(2, false));

    new DatePicker(shell);
    //tb.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

    //new Spinner(shell, SWT.BORDER);

    shell.pack();
    shell.open();


    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }





}
