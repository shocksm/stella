package org.stellasql.stella.gui.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.gui.custom.MessageDialog;

public class WebSiteOpener
{
  private final static Logger logger = LogManager.getLogger(WebSiteOpener.class);

  public static void openURL(String urlString, Shell shell)
  {
    try
    {
      Program.launch(urlString);
    }
    catch (Exception ex)
    {
      logger.error(ex.getMessage(), ex);
      MessageDialog md = new MessageDialog(shell, SWT.OK);
      md.setText("Error");
      md.setMessage("Problem launching browser:\n" + ex.getClass().getName() + "\n" + ex.getMessage());
      md.open();
    }
  }

}
