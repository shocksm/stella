package org.stellasql.stella.gui;

import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.gui.custom.DateTimeBox;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.statement.DataTypeUtil;

public class DateLiteralDialog extends StellaDialog implements SelectionListener
{
  private Composite composite = null;

  private DateTimeBox dtb = null;
  private Button cancelBtn = null;
  private Button dateBtn = null;
  private Button timeBtn = null;
  private Button timestampBtn = null;
  private String returnVal = null;

  public DateLiteralDialog(Shell parent)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);

    setText("Date Literal");

    composite = createComposite(1, 20, 0);


    dtb = new DateTimeBox(composite);
    dtb.setDateTime(Calendar.getInstance());
    GridData gd = new GridData();
    gd.horizontalAlignment = SWT.CENTER;
    gd.grabExcessHorizontalSpace = true;
    dtb.setLayoutData(gd);

    dateBtn = createButton(0);
    dateBtn.setText("&Date");
    dateBtn.addSelectionListener(this);

    timeBtn = createButton(0);
    timeBtn.setText("&Time");
    timeBtn.addSelectionListener(this);

    timestampBtn = createButton(0);
    timestampBtn.setText("Time&stamp");
    timestampBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);
  }

  public String open(int x, int y)
  {
    super.openInternal(x, y);

    returnVal = null;

    Display display = getShell().getDisplay();
    while (!getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return returnVal;
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
    {
      getShell().dispose();
    }
    else if (e.widget == dateBtn)
    {
      Calendar cal = dtb.getDateTime();
      if (cal != null)
        returnVal = DataTypeUtil.formatAsDate(cal);
      getShell().dispose();
    }
    else if (e.widget == timeBtn)
    {
      Calendar cal = dtb.getDateTime();
      if (cal != null)
        returnVal = DataTypeUtil.formatAsTime(cal);
      getShell().dispose();
    }
    else if (e.widget == timestampBtn)
    {
      Calendar cal = dtb.getDateTime();
      if (cal != null)
        returnVal = DataTypeUtil.formatAsTimestamp(cal);
      getShell().dispose();
    }
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }


}
