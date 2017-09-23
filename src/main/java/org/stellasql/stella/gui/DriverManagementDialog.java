package org.stellasql.stella.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.DriverVO;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.WebSiteOpener;

public class DriverManagementDialog extends StellaDialog implements SelectionListener, DisposeListener
{
  private final static Logger logger = LogManager.getLogger(DriverManagementDialog.class);

  private static DriverManagementDialog dmd = null;

  private ToolBar toolBar = null;
  private ToolItem editButton = null;
  private ToolItem copyButton = null;
  private ToolItem newButton = null;
  private ToolItem deleteButton = null;
  private ToolItem openWebsiteButton = null;
  private ToolItem restoreDriversButton = null;
  private Table table = null;

  private DriverManagementDialog(Shell parent)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
    getShell().setImage(StellaImages.getInstance().getDriverManagerImage());
    getShell().addDisposeListener(this);

    setText("Driver Manager");

    Composite composite = createComposite(1);
    GridLayout gridLayout = (GridLayout)composite.getLayout();
    gridLayout.verticalSpacing = 2;

    toolBar = new ToolBar(composite, SWT.FLAT | SWT.WRAP);

    newButton = new ToolItem(toolBar, SWT.PUSH);
    newButton.setToolTipText("New Driver");
    newButton.setImage(StellaImages.getInstance().getNewImage());
    newButton.addSelectionListener(this);

    new ToolItem(toolBar, SWT.SEPARATOR);

    editButton = new ToolItem(toolBar, SWT.PUSH);
    editButton.setToolTipText("Edit the selected Driver");
    editButton.setImage(StellaImages.getInstance().getEditImage());
    editButton.addSelectionListener(this);
    editButton.setEnabled(false);

    copyButton = new ToolItem(toolBar, SWT.PUSH);
    copyButton.setToolTipText("Copy the selected Driver");
    copyButton.setImage(StellaImages.getInstance().getCopyImage());
    copyButton.addSelectionListener(this);
    copyButton.setEnabled(false);

    deleteButton = new ToolItem(toolBar, SWT.PUSH);
    deleteButton.setToolTipText("Delete the selected Driver");
    deleteButton.setImage(StellaImages.getInstance().getDeleteImage());
    deleteButton.addSelectionListener(this);
    deleteButton.setEnabled(false);

    new ToolItem(toolBar, SWT.SEPARATOR);

    openWebsiteButton = new ToolItem(toolBar, SWT.PUSH);
    openWebsiteButton.setToolTipText("Open the selected Driver's website");
    openWebsiteButton.setImage(StellaImages.getInstance().getWebsiteImage());
    openWebsiteButton.addSelectionListener(this);
    openWebsiteButton.setEnabled(false);

    new ToolItem(toolBar, SWT.SEPARATOR);

    restoreDriversButton = new ToolItem(toolBar, SWT.PUSH);
    restoreDriversButton.setToolTipText("Add the default driver entries to the driver list");
    restoreDriversButton.setImage(StellaImages.getInstance().getRestoreDriversImage());
    restoreDriversButton.addSelectionListener(this);

    table = new Table (composite, SWT.BORDER | SWT.SINGLE);
    table.setLinesVisible(false);
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    table.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());

    updateList(null);
    table.setFocus();
  }

  public static DriverManagementDialog getInstance(Shell parent)
  {
    if (dmd == null)
      dmd = new DriverManagementDialog(parent);

    return dmd;
  }

  private void updateList(DriverVO driverSelect)
  {
    table.removeAll();
    int count = ApplicationData.getInstance().getDriverCount();
    for (int index = 0; index < count; index++)
    {
      DriverVO driverVO = ApplicationData.getInstance().getDriver(index);
      TableItem tableItem = new TableItem(table, SWT.NONE);
      tableItem.setText(driverVO.getName());
      tableItem.setData(driverVO);
      if (driverVO.isActive())
        tableItem.setImage(StellaImages.getInstance().getActiveImage());
      else
        tableItem.setImage(StellaImages.getInstance().getInactiveImage());

      if (driverSelect != null && driverVO.getName().equalsIgnoreCase(driverSelect.getName()))
        table.select(index);
    }

    if (driverSelect == null && table.getSelectionIndex() < 0 && table.getItemCount() > 0)
      table.select(0);

    if (table.getSelectionIndex() >= 0)
    {
      editButton.setEnabled(true);
      copyButton.setEnabled(true);
      deleteButton.setEnabled(true);
      openWebsiteButton.setEnabled(true);
    }
    else
    {
      editButton.setEnabled(false);
      copyButton.setEnabled(false);
      deleteButton.setEnabled(false);
      openWebsiteButton.setEnabled(false);
    }
  }

  public void select(String name)
  {
    TableItem[] items = table.getItems();
    for (int index = 0; index < items.length; index++)
    {
      if (items[index].getText().equals(name))
      {
        table.select(index);
        break;
      }
    }
  }

  public void newDriver()
  {
    DriverDialog dd = new DriverDialog(toolBar.getShell(), false);
    DriverVO driver = dd.open(-1, toolBar.toDisplay(0, toolBar.getLocation().y).y + toolBar.getSize().y);
    if (driver != null)
    {
      ApplicationData.getInstance().addDriver(driver);
      updateList(driver);

      save();
    }
  }

  public void editDriver()
  {
    DriverVO driverOrig = (DriverVO)table.getSelection()[0].getData();
    DriverDialog dd = new DriverDialog(toolBar.getShell(), true);
    dd.setDriver(driverOrig);
    DriverVO driver = dd.open(-1, toolBar.toDisplay(0, toolBar.getLocation().y).y + toolBar.getSize().y);

    if (driver != null)
    {
      ApplicationData.getInstance().updateDriver(driverOrig, driver);
      updateList(driver);

      save();
    }
  }

  public void copyDriver()
  {
    DriverVO driver = (DriverVO)table.getSelection()[0].getData();
    driver = new DriverVO(driver);
    driver.setName("Copy of " + driver.getName());

    DriverDialog dd = new DriverDialog(toolBar.getShell(), true);
    dd.setDriver(driver);
    driver = dd.open(-1, toolBar.toDisplay(0, toolBar.getLocation().y).y + toolBar.getSize().y);

    if (driver != null)
    {
      ApplicationData.getInstance().addDriver(driver);
      updateList(driver);

      save();
    }
  }

  public void deleteDriver()
  {
    DriverVO driver = (DriverVO)table.getSelection()[0].getData();

    MessageDialog msgDialog = new MessageDialog(toolBar.getShell(), SWT.OK | SWT.CANCEL);
    msgDialog.setText("Delete Driver");
    msgDialog.setMessage("Delete the '" + driver.getName() + "' driver?");
    if (msgDialog.open() == SWT.OK)
    {
      ApplicationData.getInstance().removeDriver(driver);
      updateList(null);

      save();
    }
  }

  private void save()
  {
    try
    {
      ApplicationData.getInstance().save();
    }
    catch(Exception e)
    {
      logger.error(e.getMessage(), e);

      MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
      messageDlg.setText("Error");
      messageDlg.setMessage(e.getMessage());
      messageDlg.open();
    }
  }

  public void open(int x, int y)
  {
    if (getShell().isVisible())
    {
      if (getShell().getSize().x + x > getShell().getDisplay().getBounds().width)
        x = getShell().getDisplay().getBounds().width - getShell().getSize().x;
      if (getShell().getSize().y + y > getShell().getDisplay().getBounds().height)
        y = getShell().getDisplay().getBounds().height - getShell().getSize().y;
      if (x < 0)
        x = 0;
      if (y < 0)
        y = 0;
      getShell().setActive();
    }
    else
      super.openInternal(x, y, 300, 500);
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == newButton)
    {
      newDriver();
    }
    else if (e.widget == editButton)
    {
      editDriver();
    }
    else if (e.widget == copyButton)
    {
      copyDriver();
    }
    else if (e.widget == deleteButton)
    {
      deleteDriver();
    }
    else if (e.widget == openWebsiteButton)
    {
      DriverVO driver = (DriverVO)table.getSelection()[0].getData();
      WebSiteOpener.openURL(driver.getWebsiteUrl(), getShell());
    }
    else if (e.widget == restoreDriversButton)
    {
      try
      {
        ApplicationData.getInstance().loadDefaultDrivers();
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);

        MessageDialog md = new MessageDialog(getShell(), SWT.OK);
        md.setText("Error");
        md.setMessage("Problem trying to load default drivers: " + ex.getMessage());
      }
      updateList(null);
    }
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    if (e.widget == table)
    {
      if (table.getSelectionCount() > 0)
        editDriver();
    }
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    dmd = null;
  }

}
