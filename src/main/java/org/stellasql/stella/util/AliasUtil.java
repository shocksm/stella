package org.stellasql.stella.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.MessageDialog;

public class AliasUtil
{
  public static boolean validateAlias(AliasVO aliasVO, Shell shell)
  {
    boolean valid = true;

    if (aliasVO.getDriverName().length() == 0)
    {
      MessageDialog messageDlg = new MessageDialog(shell, SWT.OK);
      messageDlg.setText("No Driver");
      messageDlg.setMessage("No driver has been selected for this connection");
      messageDlg.open();
      valid = false;
    }
    else if (ApplicationData.getInstance().getDriver(aliasVO.getDriverName()) == null)
    {
      MessageDialog messageDlg = new MessageDialog(shell, SWT.OK);
      messageDlg.setText("No Driver");
      messageDlg.setMessage("The selected driver no longer exists");
      messageDlg.open();
      valid = false;
    }
    else if (!ApplicationData.getInstance().getDriver(aliasVO.getDriverName()).isActive())
    {
      MessageDialog messageDlg = new MessageDialog(shell, SWT.OK);
      messageDlg.setText("Driver not configured");
      messageDlg.setMessage("The selected driver is not configured\n"
          + "or the Driver Path no longer exists");
      messageDlg.open();
      valid = false;
    }

    return valid;
  }
}
