package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.util.FontSetter;

public class PrefsGeneralComposite extends Composite implements VerifyListener, ModifyListener
{
  private boolean changed = false;
  private Text maxQueryHistory = null;

  public PrefsGeneralComposite(Composite parent)
  {
    super(parent, SWT.NONE);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    this.setLayout(gridLayout);

    Composite composite = new Composite(this, SWT.NONE);
    gridLayout = new GridLayout(3, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    composite.setLayout(gridLayout);

    Label label = new Label(composite, SWT.RIGHT);
    label.setText("Maximum history size(per connection): ");

    maxQueryHistory = new Text(composite, SWT.BORDER);
    maxQueryHistory.setText("" + ApplicationData.getInstance().getMaxQueryHistory());
    maxQueryHistory.addVerifyListener(this);
    maxQueryHistory.addModifyListener(this);
    GridData gd = new GridData();
    gd.widthHint = 35;
    maxQueryHistory.setLayoutData(gd);

    FontSetter.setAllControlFonts(this, ApplicationData.getInstance().getGeneralFont());
  }


  public void okPressed()
  {
    if (changed)
    {
      int max = 100;
      if (maxQueryHistory.getText().length() > 0)
        max = Integer.parseInt(maxQueryHistory.getText());
      ApplicationData.getInstance().setMaxQueryHistory(max, true);
    }
  }

  @Override
  public void verifyText(VerifyEvent e)
  {
    if (e.widget == maxQueryHistory)
    {
      if (e.text != null)
      {
        for (int index = 0; index < e.text.length(); index++)
        {
          if (!Character.isDigit(e.text.charAt(index)))
          {
            e.doit = false;
            break;
          }
        }
      }
    }
  }

  @Override
  public void modifyText(ModifyEvent e)
  {
    changed = true;
  }



}
