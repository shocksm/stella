package org.stellasql.stella.gui.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.ApplicationData;

public class MessageDialog extends StellaDialog implements SelectionListener
{
  private Label messageLabel = null;
  private Button okBtn = null;
  private Button cancelBtn = null;
  private int style = 0;
  private int returnVal = SWT.CANCEL;

  public MessageDialog(Shell parent, int style)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    this.style = style;

    Composite composite = createComposite(1, 10, 10);

    messageLabel = new Label(composite, SWT.NONE);
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.CENTER;
    gridData.verticalIndent = 10;
    messageLabel.setLayoutData(gridData);

    if ((this.style & SWT.OK) > 0)
    {
      okBtn = createButton(0);
      okBtn.setText("&OK");
      okBtn.addSelectionListener(this);
      okBtn.setFocus();
    }
    if ((this.style & SWT.CANCEL) > 0)
    {
      cancelBtn = createButton(0);
      cancelBtn.setText("&Cancel");
      cancelBtn.addSelectionListener(this);
      cancelBtn.setFocus();
    }

    setFonts(ApplicationData.getInstance().getGeneralFont());
  }

  public int open()
  {
    getShell().pack();
    if (getShell().getBounds().width < 200)
    {
      getShell().setSize(200, getShell().getBounds().height);
    }

    super.openInternal(-1, -1, false);

    Display display = getShell().getDisplay();
    while (!getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return returnVal;
  }

  public void setMessage(String msg)
  {
    messageLabel.setText(msg);
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == okBtn)
    {
      returnVal = SWT.OK;
      getShell().dispose();
    }
    else if (e.widget == cancelBtn)
    {
      returnVal = SWT.CANCEL;
      getShell().dispose();
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  public void setOkText(String text)
  {
    okBtn.setText(text);
  }

  public void setCancelText(String text)
  {
    cancelBtn.setText(text);
  }

}
