package org.stellasql.stella.gui;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.WebSiteOpener;
import org.stellasql.stella.util.StreamToString;

public class AboutDialog extends StellaDialog implements SelectionListener
{
  private final static Logger logger = LogManager.getLogger(AboutDialog.class);
  private Button okBtn = null;

  public AboutDialog(Shell parent)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

    setText("About Stella SQL");

    Composite composite = createComposite(2);

    Label label = new Label(composite, SWT.NONE);
    label.setImage(StellaImages.getInstance().getAppBigImage());
    GridData gridData = new GridData();
    gridData.verticalIndent = 5;
    gridData.verticalAlignment = SWT.TOP;
    label.setLayoutData(gridData);

    Composite rightComposite = new Composite(composite, SWT.NONE);
    rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    GridLayout gridLayout = new GridLayout();
    rightComposite.setLayout(gridLayout);

    label = new Label(rightComposite, SWT.NONE);
    label.setText("Stella SQL");

    String version = "";
    
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("version.txt");
    if (is == null)
    {
      logger.error("version.txt file not found");
    }
    else
    {
      StreamToString sts = new StreamToString();
      version = sts.read(is);
    }
    
    label = new Label(rightComposite, SWT.NONE);
    label.setText("Version: " + version);

    Link link = new Link(rightComposite, SWT.NONE);
    link.setText("<a>https://github.com/shocksm/stella</a>");
    link.setData("https://github.com/shocksm/stella");
    link.addSelectionListener(this);

    label = new Label(rightComposite, SWT.NONE);
    label.setText("Copyright " + (char)169 + " 2007 Samuel Shockey." );
    gridData = new GridData();
    gridData.verticalIndent = 10;
    gridData.verticalAlignment = SWT.END;
    label.setLayoutData(gridData);

    label = new Label(rightComposite, SWT.NONE);
    label.setText("Java Version: " + System.getProperty("java.version"));
    gridData = new GridData();
    gridData.verticalIndent = 10;
    gridData.verticalAlignment = SWT.END;
    label.setLayoutData(gridData);


    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    okBtn.setFocus();

    setFonts(ApplicationData.getInstance().getGeneralFont());
  }

  public void open(int x, int y)
  {
    super.openInternal(x, y);
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == okBtn)
    {
      getShell().dispose();
    }
    else if (e.widget instanceof Link)
    {
      String urlString = (String)((Link)e.widget).getData();
      WebSiteOpener.openURL(urlString, getShell());
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

}
