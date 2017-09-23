package org.stellasql.stella.gui.custom.datebox;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DateBox extends Composite implements VerifyListener, ModifyListener, FocusListener, MouseListener, DateSelectedListener, KeyListener, ShellListener
{
  private List listenerList = new LinkedList();
  private Text monthText = null;
  private Text dayText = null;
  private Text yearText = null;
  private Button dropButton = null;
  private Shell dropShell = null;
  private DatePicker datePicker = null;

  public DateBox(Composite parent)
  {
    super(parent, SWT.BORDER);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 7;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 0;
    setLayout(gridLayout);

    monthText = new Text(this, SWT.NONE);
    monthText.setText("MM");
    monthText.setTextLimit(2);
    monthText.addVerifyListener(this);
    monthText.addModifyListener(this);
    monthText.addFocusListener(this);
    monthText.addMouseListener(this);
    monthText.addKeyListener(this);

    this.setBackground(monthText.getBackground());

    Label label = new Label(this, SWT.NONE);
    label.setText("/");
    label.setBackground(monthText.getBackground());

    dayText = new Text(this, SWT.NONE);
    dayText.setText("DD");
    dayText.setTextLimit(2);
    dayText.addVerifyListener(this);
    dayText.addModifyListener(this);
    dayText.addFocusListener(this);
    dayText.addMouseListener(this);
    dayText.addKeyListener(this);

    label = new Label(this, SWT.NONE);
    label.setText("/");
    label.setBackground(monthText.getBackground());

    yearText = new Text(this, SWT.NONE);
    yearText.setText("YYYY");
    yearText.setTextLimit(4);
    yearText.addVerifyListener(this);
    yearText.addModifyListener(this);
    yearText.addFocusListener(this);
    yearText.addMouseListener(this);
    yearText.addKeyListener(this);

    dropButton = new Button(this, SWT.ARROW | SWT.DOWN);
    dropButton.addMouseListener(this);

    dropShell = new Shell(this.getShell(), SWT.BORDER | SWT.CLOSE | SWT.APPLICATION_MODAL);
    gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    dropShell.setLayout(gridLayout);
    dropShell.addShellListener(this);


    datePicker = new DatePicker(dropShell);
    datePicker.addDateSelectedListener(this);
    dropShell.pack();
  }


  @Override
  public boolean setFocus()
  {
    return monthText.setFocus();
  }

  @Override
  public void addFocusListener(FocusListener listener)
  {
    monthText.addFocusListener(listener);
    dayText.addFocusListener(listener);
    yearText.addFocusListener(listener);
    dropButton.addFocusListener(listener);
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

  public Calendar getDate()
  {
    int month = -1;
    int day = -1;
    int year = -1;

    if (monthText.getText().length() > 0)
    {
      try
      {
        month = Integer.parseInt(monthText.getText()) - 1;
      }
      catch (NumberFormatException e)
      {
      }
    }
    if (dayText.getText().length() > 0)
    {
      try
      {
        day = Integer.parseInt(dayText.getText());
      }
      catch (NumberFormatException e)
      {
      }
    }
    if (yearText.getText().length() > 0)
    {
      try
      {
        year = Integer.parseInt(yearText.getText());
      }
      catch (NumberFormatException e)
      {
      }
    }

    if (month < 0 || day < 0 || year < 0)
      return null;

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, day);

    return calendar;
  }

  public void dropDown()
  {
    Calendar calendar = Calendar.getInstance();

    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    int year = calendar.get(Calendar.YEAR);

    if (monthText.getText().length() > 0)
    {
      try
      {
        month = Integer.parseInt(monthText.getText()) - 1;
      }
      catch (NumberFormatException e)
      {
      }
    }
    if (dayText.getText().length() > 0)
    {
      try
      {
        day = Integer.parseInt(dayText.getText());
      }
      catch (NumberFormatException e)
      {
      }
    }
    if (yearText.getText().length() > 0)
    {
      try
      {
        year = Integer.parseInt(yearText.getText());
      }
      catch (NumberFormatException e)
      {
      }
    }

    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, day);
    datePicker.setCalendar(calendar);


    Point pt = this.getParent().toDisplay(this.getLocation());
    pt.y = pt.y + this.getSize().y;


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
  }


  @Override
  public void modifyText(ModifyEvent e)
  {
    if (e.widget ==  monthText
        || e.widget == dayText
        || e.widget == yearText)
    {
      Calendar cal = getDate();
      notifyDateListeners(cal);
    }
  }

  @Override
  public void focusGained(FocusEvent e)
  {
    if (e.widget == monthText)
      monthText.selectAll();
    else if (e.widget == dayText)
      dayText.selectAll();
    else if (e.widget == yearText)
      yearText.selectAll();
  }
  @Override
  public void focusLost(FocusEvent e)
  {
  }


  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
    if (e.widget == monthText)
      monthText.selectAll();
    else if (e.widget == dayText)
      dayText.selectAll();
    else if (e.widget == yearText)
      yearText.selectAll();
    else if (e.widget == dropButton)
      dropDown();
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
  }

  public void setDate(Calendar calendar)
  {
    dateSelected(calendar);
  }

  @Override
  public void dateSelected(Calendar calendar)
  {
    int month = (calendar.get(Calendar.MONTH) + 1);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    int year = calendar.get(Calendar.YEAR);

    // don't want to fire 3 date change notifications
    // so remove the listener from the text fields, change text,
    // then readd listeners
    monthText.removeModifyListener(this);
    dayText.removeModifyListener(this);
    yearText.removeModifyListener(this);

    monthText.setText((month < 10 ? "0":"") + month);
    dayText.setText((day < 10 ? "0":"") + day);
    if (year < 10)
      yearText.setText("000" + year);
    else if (year < 100)
      yearText.setText("00" + year);
    else if (year < 1000)
      yearText.setText("0" + year);
    else
      yearText.setText("" + year);

    monthText.addModifyListener(this);
    dayText.addModifyListener(this);
    yearText.addModifyListener(this);

    notifyDateListeners(calendar);

    dropShell.setVisible(false);
  }


  @Override
  public void keyPressed(KeyEvent e)
  {
    if (e.widget == monthText
        || e.widget == dayText
        || e.widget == yearText)
    {
      if (e.keyCode == SWT.ARROW_DOWN)
        dropDown();
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
    e.doit = false;
    dropShell.setVisible(false);
  }
  @Override
  public void shellDeactivated(ShellEvent e)
  {
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
