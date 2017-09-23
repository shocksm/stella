package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaImages;

public class PreferencesDialog extends StellaDialog implements SelectionListener, ControlListener
{
  private Composite composite = null;
  private Button okBtn = null;
  private Button cancelBtn = null;
  private TabFolder tabFolder = null;

  private ScrolledComposite sc = null;
  private PrefsGeneralComposite pgc = null;
  private PrefsFontComposite pfc = null;
  private PrefsSyntaxComposite psc = null;
  private PrefsSqlComposite psqlc = null;

  public PreferencesDialog(Shell parent)
  {
    super(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
    getShell().setImage(StellaImages.getInstance().getSettingsImage());
    setText("Preferences");

    composite = createComposite(1);

    tabFolder = new TabFolder(composite, SWT.NONE);
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    tabFolder.setLayoutData(gridData);


    pgc = new PrefsGeneralComposite(tabFolder);


    sc = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
    sc.setExpandHorizontal(true);
    sc.setExpandVertical(true);
    pfc = new PrefsFontComposite(sc);
    pfc.addControlListener(this);
    sc.setMinSize(pfc.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    sc.setContent(pfc);

    psc = new PrefsSyntaxComposite(tabFolder);

    psqlc = new PrefsSqlComposite(tabFolder);

    TabItem item = new TabItem (tabFolder, SWT.NONE);
    item.setText ("&General");
    item.setControl(pgc);

    item = new TabItem (tabFolder, SWT.NONE);
    item.setText ("&Fonts");
    item.setControl(sc);

    item = new TabItem (tabFolder, SWT.NONE);
    item.setText ("Syntax &Highlighting");
    item.setControl(psc);

    item = new TabItem (tabFolder, SWT.NONE);
    item.setText ("&SQL");
    item.setControl(psqlc);


    FontSetter.setFont(tabFolder, ApplicationData.getInstance().getGeneralFont());





    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);
    FontSetter.setFont(okBtn, ApplicationData.getInstance().getGeneralFont());

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);
    FontSetter.setFont(cancelBtn, ApplicationData.getInstance().getGeneralFont());
  }

  public void open(int x, int y)
  {
    super.openInternal(x, y);
  }

  private void okPressed()
  {
    pgc.okPressed();
    pfc.okPressed();
    psc.okPressed();
    psqlc.okPressed();

    ApplicationData.getInstance().updateFonts();
    getShell().dispose();
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
    {
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
  }

  @Override
  public void controlMoved(ControlEvent e)
  {
  }
  @Override
  public void controlResized(ControlEvent e)
  {
    if (e.widget == pfc)
    {
      sc.setMinSize(pfc.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

  }

}

