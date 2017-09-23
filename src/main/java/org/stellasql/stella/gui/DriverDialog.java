package org.stellasql.stella.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.DriverVO;
import org.stellasql.stella.classloader.DriverLocater;
import org.stellasql.stella.classloader.DriverLocaterListener;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.WebSiteOpener;
import org.stellasql.stella.gui.util.WorkerRunnable;

public class DriverDialog extends StellaDialog implements SelectionListener, DriverLocaterListener
{
  private Composite composite = null;

  private Label nameLabel = null;
  private Text nameText = null;
  private Label driverClassLabel = null;
  private Label jdbcUrlLabel = null;
  private Combo driverClassCombo = null;
  private Button driverClassDetectButton = null;
  private Label driverListLabel = null;
  private Button driverListRemoveButton = null;
  private org.eclipse.swt.widgets.List driverPathList = null;
  private Button driverArchiveFileButton = null;
  private Button driverArchiveDirButton = null;
  private Text jdbcUrlText = null;
  private Label websiteUrlLabel = null;
  private Text websiteUrlText = null;
  private Button websiteUrlOpenButton = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private Button useBasicBtn = null;
  private DriverVO returnVal = null;
  private boolean edit = false;
  private String initialName = "";
  private Shell progressShell = null;
  private ProgressBar progressBar = null;

  public DriverDialog(Shell parent, boolean edit)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);

    this.edit = edit;

    setText("Driver Information");

    composite = createComposite(4);

    nameLabel = new Label(composite, SWT.RIGHT);
    nameLabel.setText("Driver &Name:");
    nameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

    nameText = new Text(composite, SWT.BORDER);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.widthHint = 350;
    gridData.horizontalSpan = 3;

    nameText.setLayoutData(gridData);

    // spacer
    new Label(composite, SWT.RIGHT);

    driverArchiveFileButton = new Button(composite, SWT.PUSH);
    driverArchiveFileButton.setText("Add &File");
    driverArchiveFileButton.addSelectionListener(this);
    gridData = new GridData();
    gridData.verticalIndent = 10;
    driverArchiveFileButton.setLayoutData(gridData);

    driverArchiveDirButton = new Button(composite, SWT.PUSH);
    driverArchiveDirButton.setText("Add &Directory");
    driverArchiveDirButton.addSelectionListener(this);
    gridData = new GridData();
    gridData.verticalIndent = 10;
    driverArchiveDirButton.setLayoutData(gridData);

    driverListRemoveButton = new Button(composite, SWT.PUSH);
    driverListRemoveButton.setText("&Remove Selected");
    driverListRemoveButton.addSelectionListener(this);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.RIGHT;
    gridData.verticalIndent = 10;
    driverListRemoveButton.setLayoutData(gridData);
    driverListRemoveButton.setEnabled(false);

    driverListLabel = new Label(composite, SWT.RIGHT);
    driverListLabel.setText("Driver &Path(s):");
    driverListLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.RIGHT;
    gridData.verticalAlignment = SWT.TOP;
    driverListLabel.setLayoutData(gridData);

    driverPathList = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
    driverPathList.addSelectionListener(this);
    gridData = new GridData();
    gridData.widthHint = 250;
    gridData.heightHint = 100;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 3;
    driverPathList.setLayoutData(gridData);

    driverClassLabel = new Label(composite, SWT.RIGHT);
    driverClassLabel.setText("Driver C&lass:");
    driverClassLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

    driverClassCombo = new Combo(composite, SWT.BORDER);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 2;
    gridData.widthHint = 100;
    driverClassCombo.setLayoutData(gridData);

    driverClassDetectButton = new Button(composite, SWT.PUSH);
    driverClassDetectButton.setText("D&etect");
    driverClassDetectButton.addSelectionListener(this);


    jdbcUrlLabel = new Label(composite, SWT.RIGHT);
    jdbcUrlLabel.setText("Example JDBC &URL:");
    jdbcUrlLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

    jdbcUrlText = new Text(composite, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = 350;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 3;
    jdbcUrlText.setLayoutData(gridData);

    new Label(composite, SWT.NONE); // spacer

    useBasicBtn = new Button(composite, SWT.CHECK);
    useBasicBtn.setText("Use &Basic Driver Parameters");
    useBasicBtn.addSelectionListener(this);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 3;
    useBasicBtn.setLayoutData(gridData);


    websiteUrlLabel = new Label(composite, SWT.RIGHT);
    websiteUrlLabel.setText("Driver &Website URL:");
    websiteUrlText = new Text(composite, SWT.BORDER);
    gridData.widthHint = 250;
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 2;
    websiteUrlText.setLayoutData(gridData);
    websiteUrlOpenButton =new Button(composite, SWT.PUSH);
    websiteUrlOpenButton.setText("O&pen");
    websiteUrlOpenButton.addSelectionListener(this);

    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());
  }

  public void setDriver(DriverVO driver)
  {
    nameText.setText(driver.getName());
    driverClassCombo.setText(driver.getDriverClass());
    for (Iterator it = driver.getDriverPathList().iterator(); it.hasNext();)
    {
      String path = (String)it.next();
      driverPathList.add(path);
    }
    jdbcUrlText.setText(driver.getExampleJdbcUrl());
    useBasicBtn.setSelection(driver.getUseBasicParameters());
    websiteUrlText.setText(driver.getWebsiteUrl());

    initialName = driver.getName();
  }

  private void okPressed()
  {
    DriverVO driver = new DriverVO();
    driver.setName(nameText.getText().trim());
    driver.setDriverClass(driverClassCombo.getText().trim());
    driver.setDriverPathList(Arrays.asList(driverPathList.getItems()));
    driver.setExampleJdbcUrl(jdbcUrlText.getText().trim());
    driver.setUseBasicParameters(useBasicBtn.getSelection());
    driver.setWebsiteUrl(websiteUrlText.getText().trim());

    boolean exists = false;
    if (edit)
    {
      // edit being done
      if (!initialName.trim().equalsIgnoreCase(nameText.getText().trim()))
      {
        // name was changed
        exists = ApplicationData.getInstance().getDriverExists(driver);
      }
    }
    else
      exists = ApplicationData.getInstance().getDriverExists(driver);

    if (exists)
    {
      MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
      messageDlg.setText("Already Exists");
      messageDlg.setMessage("A driver with that name already exists");
      messageDlg.open();
    }
    else if (driver.getName().length() == 0)
    {
      MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
      messageDlg.setText("Invalid Name");
      messageDlg.setMessage("Driver Name is required");
      messageDlg.open();
    }
    else
    {
      returnVal = driver;
      getShell().dispose();
    }
  }

  private boolean validate(String path)
  {
    boolean valid = false;
    if (validatePath(path))
    {
      detectDrivers();
      valid = true;
    }

    return valid;
  }

  private boolean validatePath(String path)
  {
    if (path.trim().length() > 0)
    {
      path = path.trim();
      File file = new File(path);
      if (!file.exists())
      {
        MessageDialog md = new MessageDialog(getShell(), SWT.OK);
        md.setMessage("The Driver Path entered does not exist");
        md.setText("Error");
        md.open();
        return false;
      }

      boolean found = false;
      for (int index = 0; index < driverPathList.getItemCount(); index++)
      {
        if (driverPathList.getItem(index).equals(path))
        {
          found = true;
          break;
        }
      }
      if (!found)
      {
        driverPathList.add(path);
        return true;
      }
    }

    return false;
  }

  private void detectDrivers()
  {
    if (driverPathList.getItemCount() == 0)
      return;

    progressShell = new Shell(getShell(), SWT.TOOL);
    progressShell.setLayout(new GridLayout());
    Label label = new Label(progressShell, SWT.NONE);
    label.setText("Detecting JDBC drivers");
    FontSetter.setFont(label, ApplicationData.getInstance().getGeneralFont());
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.CENTER;
    label.setLayoutData(gridData);

    progressBar = new ProgressBar(progressShell, SWT.SMOOTH);
    gridData = new GridData();
    gridData.widthHint = 250;
    gridData.heightHint = 30;
    gridData.horizontalAlignment = SWT.CENTER;
    progressBar.setLayoutData(gridData);
    progressShell.pack();
    int x = getShell().getLocation().x + (getShell().getSize().x - progressShell.getSize().x) / 2;
    int y = getShell().getLocation().y + (getShell().getSize().y - progressShell.getSize().y) / 2;
    progressShell.setLocation(x, y);
    progressShell.open();

    String[] pathArray = driverPathList.getItems();
    final List pathList = new ArrayList();
    for (int index = 0; index < pathArray.length; index++)
    {
      pathList.add(new File(pathArray[index]));
    }

    WorkerRunnable worker = new WorkerRunnable()
    {
      Exception ex = null;
      List driverNames = null;

      @Override
      public void doTask()
      {
        DriverLocater dl = new DriverLocater(pathList, DriverDialog.this);
        try
        {
          driverNames = dl.getDriverClassNames();
        }
        catch (IOException e)
        {
          ex = e;
        }
      }

      @Override
      public void doUITask()
      {
        progressShell.dispose();
        progressShell = null;
        progressBar = null;

        getShell().setActive();

        if (ex != null)
        {
          MessageDialog md = new MessageDialog(getShell(), SWT.OK);
          md.setMessage(ex.getMessage());
          md.setText("Error");
          md.open();
        }
        else
        {
          if (driverNames.size() == 0)
          {
            MessageDialog md = new MessageDialog(getShell(), SWT.OK);
            md.setMessage("No JDBC Drivers could be found in the specified Driver Path(s)");
            md.setText("Warning");
            md.open();
          }
          else
          {
            driverClassCombo.removeAll();
            for (Iterator it = driverNames.iterator(); it.hasNext();)
            {
              String name = (String)it.next();
              driverClassCombo.add(name);
            }
            if (driverClassCombo.getItemCount() > 0)
            {
              driverClassCombo.select(0);
            }
          }
        }

        getShell().setEnabled(true);
        BusyManager.setNotBusy(getShell());
      }
    };

    getShell().setEnabled(false);
    BusyManager.setBusy(getShell());
    worker.startTask();
  }

  public DriverVO open(int x, int y)
  {
    super.openInternal(x, y);

    Display display = getShell().getDisplay();
    while (!getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return returnVal;
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
    {
      returnVal = null;
      getShell().dispose();
    }
    else if (e.widget == okBtn)
    {
      okPressed();
    }
    else if (e.widget == driverListRemoveButton)
    {
      if (driverPathList.getSelectionCount() == 1)
      {
        driverPathList.remove(driverPathList.getSelectionIndex());
        driverListRemoveButton.setEnabled(false);
      }
    }
    else if (e.widget == driverPathList)
    {
      if (driverPathList.getSelectionCount() == 1)
        driverListRemoveButton.setEnabled(true);
      else
        driverListRemoveButton.setEnabled(false);
    }
    else if (e.widget == useBasicBtn)
    {
      if (useBasicBtn.getSelection())
      {
        String msg = DriverVO.validateBasicParameters(jdbcUrlText.getText());
        if (msg != null)
        {
          useBasicBtn.setSelection(false);
          MessageDialog md = new MessageDialog(this.getParent(), SWT.OK);
          md.setText("JDBC URL not in Basic format");
          md.setMessage(msg);
          md.open();
        }
      }
    }
    else if (e.widget == driverArchiveFileButton)
    {
      FileDialog dialog = new FileDialog(driverArchiveFileButton.getShell(), SWT.OPEN);
      dialog.setFilterExtensions(new String[]{"*", "*.jar", "*.zip"});
      dialog.setFilterNames(new String[]{"all files (*.*)", "jar files (*.jar)", "zip files (*.zip)"});
      String path = dialog.open();
      if (path != null)
      {
        validate(path);
      }
    }
    else if (e.widget == driverArchiveDirButton)
    {
      DirectoryDialog dirDialog = new DirectoryDialog(driverArchiveFileButton.getShell());
      dirDialog.setMessage("Select a directory that contains the driver classes");
      String path = dirDialog.open();
      if (path != null)
      {
        validate(path);
      }
    }
    else if (e.widget == driverClassDetectButton)
    {
      detectDrivers();
    }
    else if (e.widget == websiteUrlOpenButton)
    {
      String urlString = websiteUrlText.getText();
      WebSiteOpener.openURL(urlString, getShell());
    }
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  @Override
  public void filesToProcess(final int count)
  {
    getShell().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
      if (!progressBar.isDisposed())
      {
        progressBar.setMaximum(count);
      }
    }});
  }

  @Override
  public void filesProcessed(final int count)
  {
    getShell().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
      if (!progressBar.isDisposed())
      {
        progressBar.setSelection(count);
      }
    }});
  }

}
