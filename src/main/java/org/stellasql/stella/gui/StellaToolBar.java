package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.AliasChangeListener;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaImages;

public class StellaToolBar extends Composite implements SelectionListener, ControlListener, DisposeListener, FontChangeListener, AliasChangeListener
{
  private CoolBar bar = null;
  private ToolBar sessionToolBar = null;
  private CoolItem sessionItem = null;
  private Composite sessionComposite = null;

  private Label label = null;
  private ToolItem connectButton = null;
  private ToolItem editButton = null;
  private ToolItem copyButton = null;
  private ToolItem newButton = null;
  private ToolItem deleteButton = null;
  private ToolItem preferencesButton = null;
  private ToolItem driverButton = null;
  private Combo combo = null;


  public StellaToolBar(Composite parent)
  {
    super(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginTop = 2;
    gridLayout.marginBottom = 0;
    gridLayout.marginWidth = 0;
    this.setLayout(gridLayout);

    bar = new CoolBar(this, SWT.FLAT);
    bar.setLayoutData(new GridData(GridData.FILL_BOTH));

    bar.addDisposeListener(this);

    sessionComposite = new Composite(bar, SWT.NONE);
    gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    gridLayout.horizontalSpacing = 2;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    sessionComposite.setLayout(gridLayout);

    label = new Label(sessionComposite, SWT.RIGHT);
    label.setText("C&onnection:");

    combo = new Combo(sessionComposite, SWT.SINGLE | SWT.READ_ONLY);
    combo.setVisibleItemCount(25);
    combo.addSelectionListener(this);

    sessionToolBar = new ToolBar(sessionComposite, SWT.FLAT | SWT.WRAP);

    connectButton = new ToolItem(sessionToolBar, SWT.PUSH);
    connectButton.setToolTipText("Open a session to the selected connection");
    connectButton.setImage(StellaImages.getInstance().getConnectImage());
    connectButton.setDisabledImage(StellaImages.getInstance().getConnectDisImage());
    connectButton.addSelectionListener(this);

    new ToolItem(sessionToolBar, SWT.SEPARATOR);

    newButton = new ToolItem(sessionToolBar, SWT.PUSH);
    newButton.setToolTipText("New connection");
    newButton.setImage(StellaImages.getInstance().getNewImage());
    newButton.addSelectionListener(this);

    new ToolItem(sessionToolBar, SWT.SEPARATOR);

    editButton = new ToolItem(sessionToolBar, SWT.PUSH);
    editButton.setToolTipText("Edit the selected connection");
    editButton.setImage(StellaImages.getInstance().getEditImage());
    editButton.setDisabledImage(StellaImages.getInstance().getEditDisImage());
    editButton.addSelectionListener(this);

    copyButton = new ToolItem(sessionToolBar, SWT.PUSH);
    copyButton.setToolTipText("Copy the selected connection");
    copyButton.setImage(StellaImages.getInstance().getCopyImage());
    copyButton.setDisabledImage(StellaImages.getInstance().getCopyDisImage());
    copyButton.addSelectionListener(this);

    deleteButton = new ToolItem(sessionToolBar, SWT.PUSH);
    deleteButton.setToolTipText("Delete the selected connection");
    deleteButton.setImage(StellaImages.getInstance().getDeleteImage());
    deleteButton.setDisabledImage(StellaImages.getInstance().getDeleteDisImage());
    deleteButton.addSelectionListener(this);

    sessionItem = new CoolItem(bar, SWT.DROP_DOWN);
    sessionItem.setControl(sessionComposite);




    ToolBar toolBar = new ToolBar(bar, SWT.FLAT | SWT.WRAP);
    driverButton = new ToolItem(toolBar, SWT.PUSH);
    driverButton.setToolTipText("Driver Manager");
    driverButton.setImage(StellaImages.getInstance().getDriverManagerImage());
    driverButton.addSelectionListener(this);

    preferencesButton = new ToolItem(toolBar, SWT.PUSH);
    preferencesButton.setToolTipText("Preferences");
    preferencesButton.setImage(StellaImages.getInstance().getSettingsImage());
    preferencesButton.addSelectionListener(this);

    CoolItem coolItem = new CoolItem(bar, SWT.DROP_DOWN);
    coolItem.setControl(toolBar);
    Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    coolItem.setMinimumSize(size);
    coolItem.setPreferredSize(size);
    coolItem.setSize(size);


    bar.addControlListener(this);

    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);

    updateList();
    String lastSelected = ApplicationData.getInstance().getSelectedAlias();
    String[] values = combo.getItems();
    for (int index = 0; index < values.length; index++)
    {
      if (values[index].equals(lastSelected))
      {
        combo.select(index);
        break;
      }
    }

    ApplicationData.getInstance().addAliasChangeListener(this);
  }

  private void setFonts()
  {
    FontSetter.setFont(label, ApplicationData.getInstance().getGeneralFont());
    FontSetter.setFont(combo, ApplicationData.getInstance().getGeneralFont());
  }

  private void updateSize()
  {
    sessionComposite.pack();
    Point size = sessionComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    sessionItem.setMinimumSize(size);
    sessionItem.setPreferredSize(size);
    sessionItem.setSize(size);
  }

  private void updateList()
  {
    combo.removeAll();

    int count = ApplicationData.getInstance().getAliasCount();
    for (int index = 0; index < count; index++)
    {
      AliasVO aliasVO = ApplicationData.getInstance().getAlias(index);
      combo.add(aliasVO.getName());
    }
    if (combo.getSelectionIndex() < 0 && combo.getItemCount() > 0)
    {
      combo.select(0);
    }



    if (combo.getItemCount() > 0 && combo.getSelectionIndex() >= 0)
    {
      connectButton.setEnabled(true);
      editButton.setEnabled(true);
      copyButton.setEnabled(true);
      deleteButton.setEnabled(true);
    }
    else
    {
      connectButton.setEnabled(false);
      editButton.setEnabled(false);
      copyButton.setEnabled(false);
      deleteButton.setEnabled(false);
    }

    updateSize();
  }

  public boolean getItemSelected()
  {
    return combo.getSelectionIndex() >= 0;
  }

  private void connect()
  {
    AliasVO aliasVO = ApplicationData.getInstance().getAlias(combo.getSelectionIndex());
    Stella.getInstance().connect(aliasVO);
  }

  public void newAlias()
  {
    Stella.getInstance().newAlias();
  }

  public void editAlias()
  {
    AliasVO aliasVO = ApplicationData.getInstance().getAlias(combo.getSelectionIndex());
    Stella.getInstance().editAlias(aliasVO);
  }

  public void copyAlias()
  {
    AliasVO aliasVO = ApplicationData.getInstance().getAlias(combo.getSelectionIndex());
    aliasVO = Stella.getInstance().copyAlias(aliasVO);
    if (aliasVO != null)
      selectAlias(aliasVO.getName());
  }

  public void deleteAlias()
  {
    AliasVO aliasVO = ApplicationData.getInstance().getAlias(combo.getSelectionIndex());
    Stella.getInstance().deleteAlias(aliasVO);
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == connectButton)
    {
      connect();
    }
    else if (e.widget == newButton)
    {
      newAlias();
    }
    else if (e.widget == editButton)
    {
      editAlias();
    }
    else if (e.widget == copyButton)
    {
      copyAlias();
    }
    else if (e.widget == deleteButton)
    {
      deleteAlias();
    }
    else if (e.widget == driverButton)
    {
      openDriverManager();
    }
    else if (e.widget == preferencesButton)
    {
      Stella.getInstance().openPreferencesDialog();
    }
    else if (e.widget == combo)
    {
      if (combo.getSelectionIndex() >= 0)
      {
        String value = combo.getItem(combo.getSelectionIndex());
        ApplicationData.getInstance().setSelectedAlias(value);
      }
    }
  }

  public void openDriverManager()
  {
    Stella.getInstance().openDriverManager();
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
    bar.getShell().layout();
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    ApplicationData.getInstance().removeFontChangeListener(this);
  }

  @Override
  public void fontChanged()
  {
    setFonts();
    updateSize();
    bar.layout(true, true);
  }

  @Override
  public void connectionAdded(String name)
  {
    updateList();
    selectAlias(name);
  }

  @Override
  public void connectionRemoved(String name)
  {
    String selected = null;
    if (combo.getItemCount() > 0)
      selected = combo.getItem(combo.getSelectionIndex());

    updateList();

    if (selected != null)
      selectAlias(selected);
  }

  @Override
  public void connectionChanged(String oldName, String newName)
  {
    String selected = null;
    if (combo.getItemCount() > 0)
      selected = combo.getItem(combo.getSelectionIndex());
    updateList();

    if (selected != null)
    {
      if (selected.equals(oldName))
        selected = newName;
      selectAlias(selected);
    }
  }

  private void selectAlias(String name)
  {
    if (combo.getItemCount() > 0)
    {
      String[] items = combo.getItems();
      for (int i = 0; i < items.length; i++)
      {
        if (items[i].equals(name))
        {
          combo.select(i);
          break;
        }
      }
    }
  }
}
