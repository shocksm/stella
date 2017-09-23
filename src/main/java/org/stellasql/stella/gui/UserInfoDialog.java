package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.FontSetter;

public class UserInfoDialog extends StellaDialog implements SelectionListener
{
  private Button cancelBtn = null;
  private Button okBtn = null;
  private Text usernameText = null;
  private Text passwordText = null;
  private String username = "";
  private String password = "";
  private int returnVal = SWT.CANCEL;

  public UserInfoDialog(Shell parent, String aliasName)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

    setText(aliasName + " Login");

    Composite composite = createComposite(2);

    Label label = new Label(composite, SWT.RIGHT);
    FontSetter.setFont(label, ApplicationData.getInstance().getGeneralFont());
    label.setText("&Username:");
    label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

    usernameText = new Text(composite, SWT.BORDER);
    FontSetter.setFont(usernameText, ApplicationData.getInstance().getGeneralFont());
    GridData gridData = new GridData();
    gridData.widthHint = 150;
    usernameText.setLayoutData(gridData);
    usernameText.addSelectionListener(this);

    label = new Label(composite, SWT.RIGHT);
    FontSetter.setFont(label, ApplicationData.getInstance().getGeneralFont());
    label.setText("&Password:");
    label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

    passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
    FontSetter.setFont(passwordText, ApplicationData.getInstance().getGeneralFont());
    gridData = new GridData();
    gridData.widthHint = 150;
    passwordText.setLayoutData(gridData);
    passwordText.addSelectionListener(this);

    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());
  }

  public int open(int x, int y)
  {
    if (usernameText.getText().length() > 0)
      passwordText.setFocus();

    super.openInternal(x, y);

    Display display = getShell().getDisplay();
    while (!getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return returnVal;
  }

  public void setUsername(String username)
  {
    usernameText.setText(username);
  }

  public String getUsernameText()
  {
    return username;
  }

  public void setPassword(String password)
  {
    passwordText.setText(password);
  }

  public String getPasswordText()
  {
    return password;
  }

  private void okPressed()
  {
    username = usernameText.getText();
    password = passwordText.getText();
    returnVal = SWT.OK;
    getShell().dispose();
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
    {
      returnVal = SWT.CANCEL;
      getShell().dispose();
    }
    else if (e.widget == okBtn)
    {
      okPressed();
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    if (e.widget == usernameText || e.widget == passwordText)
      okPressed();
  }

}
