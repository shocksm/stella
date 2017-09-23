package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.util.FontSetter;

public class PrefsSqlComposite extends Composite implements SelectionListener, DisposeListener, VerifyListener, ModifyListener
{
  private boolean changed = false;
  private Button autoCommit = null;
  private Button limitResults = null;
  private Text limitText = null;
  private Text querySeparatorText = null;
  private Button stripComments = null;

  public PrefsSqlComposite(Composite parent)
  {
    super(parent, SWT.NONE);

    this.addDisposeListener(this);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    this.setLayout(gridLayout);

    Label label = new Label(this, SWT.NONE);
    label.setText("Changes to these settings will not affect open sessions");

    autoCommit = new Button(this, SWT.CHECK);
    autoCommit.setText("&Auto Commit");
    autoCommit.setSelection(ApplicationData.getInstance().getAutoCommit());
    autoCommit.addSelectionListener(this);

    Composite composite = new Composite(this, SWT.NONE);
    gridLayout = new GridLayout(3, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    composite.setLayout(gridLayout);

    limitResults = new Button(composite, SWT.CHECK);
    limitResults.setText("&Limit results:");
    limitResults.setSelection(ApplicationData.getInstance().getLimitResults());
    limitResults.addSelectionListener(this);

    limitText = new Text(composite, SWT.BORDER);
    limitText.setText("" + ApplicationData.getInstance().getMaxRows());
    limitText.addVerifyListener(this);
    limitText.setEnabled(limitResults.getSelection());
    limitText.addModifyListener(this);
    GridData gd = new GridData();
    gd.widthHint = 35;
    limitText.setLayoutData(gd);

    label = new Label(composite, SWT.NONE);
    label.setText("rows");

    composite = new Composite(this, SWT.NONE);
    gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    composite.setLayout(gridLayout);

    label = new Label(composite, SWT.NONE);
    label.setText("&Query Statement Separator:");

    querySeparatorText = new Text(composite, SWT.BORDER);
    querySeparatorText.setText(ApplicationData.getInstance().getQuerySeparator());
    querySeparatorText.addVerifyListener(this);
    querySeparatorText.addModifyListener(this);
    gd = new GridData();
    gd.widthHint = 35;
    querySeparatorText.setLayoutData(gd);

    stripComments = new Button(composite, SWT.CHECK);
    stripComments.setText("&Strip Comments:");
    stripComments.addSelectionListener(this);
    stripComments.setSelection(ApplicationData.getInstance().getStripComments());
    stripComments.setToolTipText("Strip comments from queries before submitting them to the database");

    FontSetter.setAllControlFonts(this, ApplicationData.getInstance().getGeneralFont());
  }


  public void okPressed()
  {
    if (changed)
    {
      ApplicationData.getInstance().setAutoCommit(autoCommit.getSelection());
      ApplicationData.getInstance().setLimitResults(limitResults.getSelection());
      int max = 100;
      if (limitText.getText().length() > 0)
        max = Integer.parseInt(limitText.getText());
      ApplicationData.getInstance().setMaxRows(max);
      ApplicationData.getInstance().setQuerySeparator(querySeparatorText.getText().trim());
      ApplicationData.getInstance().setStripComments(stripComments.getSelection());
    }
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    changed = true;
    if (e.widget == limitResults)
    {
      limitText.setEnabled(limitResults.getSelection());
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {

  }


  @Override
  public void verifyText(VerifyEvent e)
  {
    if (e.widget == limitText)
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
    else if (e.widget == querySeparatorText)
    {
      if (e.text != null)
      {
        for (int index = 0; index < e.text.length(); index++)
        {
          if (Character.isWhitespace(e.text.charAt(index)))
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
