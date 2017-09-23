package org.stellasql.stella.gui.custom;

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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.gui.custom.datebox.DateSelectedListener;

public class TimeBox extends Composite implements VerifyListener, ModifyListener, FocusListener, MouseListener, KeyListener, ValueChangeListener
{
  private final static String AM = "AM";
  private final static String PM = "PM";

  private List listenerList = new LinkedList();
  private Text hourText = null;
  private Text minuteText = null;
  private Text secondText = null;
  private Text amPmText = null;
  private Text lastFocus = null;
  private CustomSpinner spinner = null;

  public TimeBox(Composite parent)
  {
    super(parent, SWT.BORDER);

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 9;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    setLayout(gridLayout);

    hourText = new Text(this, SWT.NONE);
    hourText.setText("HH");
    hourText.setTextLimit(2);
    hourText.addVerifyListener(this);
    hourText.addModifyListener(this);
    hourText.addFocusListener(this);
    hourText.addMouseListener(this);
    hourText.addKeyListener(this);
    hourText.setLayoutData(new GridData());

    lastFocus = hourText;

    setBackground(hourText.getBackground());

    Label label = new Label(this, SWT.NONE);
    label.setText(":");
    label.setBackground(hourText.getBackground());

    minuteText = new Text(this, SWT.NONE);
    minuteText.setText("MM");
    minuteText.setTextLimit(2);
    minuteText.addVerifyListener(this);
    minuteText.addModifyListener(this);
    minuteText.addFocusListener(this);
    minuteText.addMouseListener(this);
    minuteText.addKeyListener(this);
    minuteText.setLayoutData(new GridData());

    label = new Label(this, SWT.NONE);
    label.setText(":");
    label.setBackground(hourText.getBackground());

    secondText = new Text(this, SWT.NONE);
    secondText.setText("SS");
    secondText.setTextLimit(2);
    secondText.addVerifyListener(this);
    secondText.addModifyListener(this);
    secondText.addFocusListener(this);
    secondText.addMouseListener(this);
    secondText.addKeyListener(this);
    secondText.setLayoutData(new GridData());

    amPmText = new Text(this, SWT.NONE);
    amPmText.setText(AM);
    amPmText.setEditable(false);
    amPmText.setBackground(secondText.getBackground());
    amPmText.setForeground(secondText.getForeground());
    amPmText.addModifyListener(this);
    amPmText.addFocusListener(this);
    amPmText.addMouseListener(this);
    amPmText.addKeyListener(this);
    amPmText.setLayoutData(new GridData());

    spinner = new CustomSpinner(this);
    GridData gd = new GridData();
    gd.verticalAlignment = SWT.FILL;
    gd.grabExcessVerticalSpace = true;
    spinner.setLayoutData(gd);
    spinner.addValueChangeListener(this);

    Control[] tablist = {hourText, minuteText, secondText, amPmText};
    setTabList(tablist);
  }

  @Override
  public boolean setFocus()
  {
    return hourText.setFocus();
  }

  @Override
  public void addFocusListener(FocusListener listener)
  {
    hourText.addFocusListener(listener);
    minuteText.addFocusListener(listener);
    secondText.addFocusListener(listener);
    amPmText.addFocusListener(listener);
  }

  public void addDateSelectedListener(DateSelectedListener listener)
  {
    listenerList.add(listener);
  }

  public void removeDateSelectedListener(DateSelectedListener listener)
  {
    listenerList.remove(listener);
  }

  public void notifyTimeListeners(Calendar cal)
  {
    for (Iterator it = listenerList.iterator(); it.hasNext();)
    {
      DateSelectedListener listener = (DateSelectedListener)it.next();
      listener.dateSelected(cal);
    }
  }

  public Calendar getTime()
  {
    int hour = -1;
    int minute = -1;
    int second = -1;

    if (hourText.getText().length() > 0)
    {
      try
      {
        hour = Integer.parseInt(hourText.getText());
        if (hour == 12)
          hour = 0;
      }
      catch (NumberFormatException e)
      {
      }
    }
    if (minuteText.getText().length() > 0)
    {
      try
      {
        minute = Integer.parseInt(minuteText.getText());
      }
      catch (NumberFormatException e)
      {
      }
    }
    if (secondText.getText().length() > 0)
    {
      try
      {
        second = Integer.parseInt(secondText.getText());
      }
      catch (NumberFormatException e)
      {
      }
    }

    if (hour < 0 || minute < 0 || second < 0)
      return null;

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.HOUR, hour);
    if (amPmText.getText().equals(AM))
      calendar.set(Calendar.AM_PM, Calendar.AM);
    else
      calendar.set(Calendar.AM_PM, Calendar.PM);

    return calendar;
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
          if (e.widget == hourText)
          {
            if (value < 1 || value > 12)
              e.doit = false;
          }
          else if (value < 0 || value > 59)
            e.doit = false;
        }
        catch (NumberFormatException e1)
        {
          e.doit = false;
        }
      }
    }

  }

  @Override
  public void modifyText(ModifyEvent e)
  {
    if (e.widget ==  hourText
        || e.widget == minuteText
        || e.widget == secondText
        || e.widget == amPmText)
    {

      Calendar cal = getTime();
      notifyTimeListeners(cal);
    }
  }

  @Override
  public void focusGained(FocusEvent e)
  {
    if (e.widget == hourText)
    {
      hourText.selectAll();
      lastFocus = hourText;
    }
    else if (e.widget == minuteText)
    {
      minuteText.selectAll();
      lastFocus = minuteText;
    }
    else if (e.widget == secondText)
    {
      secondText.selectAll();
      lastFocus = secondText;
    }
    else if (e.widget == amPmText)
    {
      amPmText.selectAll();
      lastFocus = amPmText;
    }

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
  public void valueChanged(int amount)
  {
    changeValue(amount, lastFocus);;
  }

  @Override
  public void mouseDown(MouseEvent e)
  {
    if (e.widget == hourText)
      hourText.selectAll();
    else if (e.widget == minuteText)
      minuteText.selectAll();
    else if (e.widget == secondText)
      secondText.selectAll();
    else if (e.widget == amPmText)
      amPmText.selectAll();
  }

  @Override
  public void mouseUp(MouseEvent e)
  {
  }

  private int getIntValue(String text)
  {
    int value = -1;

    if (text.length() > 0)
    {
      try
      {
        value = Integer.parseInt(text);
      }
      catch (NumberFormatException e)
      {
      }
    }

    return value;
  }

  public void setTime(Calendar calendar)
  {
    setField(calendar.get(Calendar.HOUR), hourText);
    setField(calendar.get(Calendar.MINUTE), minuteText);
    setField(calendar.get(Calendar.SECOND), secondText);
    setField(calendar.get(Calendar.AM_PM), amPmText);
  }

  private void setField(int value, Text textField)
  {
    String newValue = "";

    if (textField == amPmText)
    {
      if (value == Calendar.AM)
        newValue = AM;
      else
        newValue = PM;
    }
    else
    {
      newValue = getStringValue(value, textField);

    }

    textField.removeModifyListener(this);
    textField.setText(newValue);
    textField.addModifyListener(this);

  }

  private String getStringValue(int value, Text textField)
  {
    if (textField == hourText)
    {
      if (value < 1)
        value = 12;
      if (value > 12)
        value = 1;
    }
    else
    {
      if (value < 0)
        value = 59;
      if (value > 59)
        value = 0;
    }

    String newValue = null;
    if (value < 10)
      newValue = "0" + value;
    else
      newValue = "" + value;

    return newValue;
  }




  private void changeValue(int amount, Text textField)
  {
    String newValue = "";
    if (textField == amPmText)
    {
      if (amPmText.getText().equals(AM))
        newValue = PM;
      else
        newValue = AM;
    }
    else
    {
      int value = getIntValue(textField.getText());
      value += amount;
      newValue = getStringValue(value, textField);
    }

    textField.setText(newValue);
    textField.setFocus();
    textField.selectAll();

    Calendar cal = getTime();
    notifyTimeListeners(cal);
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if ((e.widget == hourText
        || e.widget == minuteText || e.widget == secondText || e.widget == amPmText)
        && (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP))
    {
      int amount = e.keyCode == SWT.ARROW_UP ? 1:-1;
      Text text = (Text)e.widget;
      changeValue(amount, text);
      e.doit = false;
    }
  }

  @Override
  public void keyReleased(KeyEvent e)
  {

  }

  public void setWidthHints()
  {
    TextLayout textLayout = new TextLayout(getDisplay());
    TextStyle style1 = new TextStyle(hourText.getFont(), null, null);
    textLayout.setStyle(style1, 0, 20);

    int max = 0;
    textLayout.setText("00");
    Rectangle textLayoutBounds = textLayout.getBounds();
    if (textLayoutBounds.width > max)
      max = textLayoutBounds.width;
    textLayout.setText("55");
    textLayoutBounds = textLayout.getBounds();
    if (textLayoutBounds.width > max)
      max = textLayoutBounds.width;
    textLayout.setText("MM");
    textLayoutBounds = textLayout.getBounds();
    if (textLayoutBounds.width > max)
      max = textLayoutBounds.width;

    GridData gd = (GridData)hourText.getLayoutData();
    gd.widthHint = max;
    gd = (GridData)minuteText.getLayoutData();
    gd.widthHint = max;
    gd = (GridData)secondText.getLayoutData();
    gd.widthHint = max;

    gd = (GridData)amPmText.getLayoutData();
    textLayout.setText(AM);
    gd.widthHint = textLayoutBounds.width + 5;
    textLayout.setText(PM);
    textLayoutBounds = textLayout.getBounds();
    if (textLayoutBounds.width + 5 > gd.widthHint)
      gd.widthHint = textLayoutBounds.width + 5;

    textLayout.dispose();
  }

  @Override
  public Point computeSize(int wHint, int hHint)
  {
    setWidthHints();
    return super.computeSize(wHint, hHint);
  }

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed)
  {
    setWidthHints();
    return super.computeSize(wHint, hHint, changed);
  }

  public static void main(String[] args)
  {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new GridLayout(2, false));

    TimeBox tb = new TimeBox(shell);
    tb.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

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
