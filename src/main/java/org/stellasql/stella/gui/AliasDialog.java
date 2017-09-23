package org.stellasql.stella.gui;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.DriverChangeListener;
import org.stellasql.stella.DriverParameter;
import org.stellasql.stella.DriverVO;
import org.stellasql.stella.connection.ConnectionManager;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.BusyManager;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.WorkerRunnable;
import org.stellasql.stella.util.AliasUtil;

public class AliasDialog extends StellaDialog implements SelectionListener, DisposeListener, DriverChangeListener
{
  private AliasVO returnVal = null;
  private Label nameLabel = null;
  private Text nameText = null;
  private Label driverLabel = null;
  private Combo driverCombo = null;
  private Button driverBtn = null;
  private Label urlLabel = null;
  private Text urlText = null;
  private Label urlExampleLabel = null;
  private Text urlExampleText = null;
  private Label usernameLabel = null;
  private Button promptButton = null;
  private Text usernameText = null;
  private Label passwordLabel = null;
  private Text passwordText = null;
  private Button cancelBtn = null;
  private Button testBtn = null;
  private Button okBtn = null;
  private AliasVO aliasVO = null;
  private boolean edit = false;
  private Group driverPropertiesGroup = null;
  private Composite easySetupComposite = null;
  private Composite advancedSetupComposite = null;
  private Button basicButton = null;
  private Button advancedButton = null;
  private boolean firstAdvanced = false;

  private List mnemonics = new LinkedList();
  private List driverParamTextList = new LinkedList();
  private Map driverParamMap = new HashMap();


  public AliasDialog(Shell parent, AliasVO aliasVO, boolean edit)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);

    getShell().addDisposeListener(this);

    this.aliasVO = aliasVO;
    this.edit = edit;

    Composite composite = createComposite(1, 10, 10);


    //---------------------------------------------------------------------------------
    Composite nameComposite = new Composite(composite, SWT.NONE);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    nameComposite.setLayoutData(gridData);
    GridLayout gl = new GridLayout();
    gl.numColumns = 2;
    gl.marginWidth = 0;
    gl.marginHeight = 0;
    nameComposite.setLayout(gl);

    nameLabel = new Label(nameComposite, SWT.RIGHT);
    nameLabel.setText("&Connection Alias:");
    nameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

    nameText = new Text(nameComposite, SWT.BORDER);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.widthHint = 350;
    nameText.setLayoutData(gridData);


    //---------------------------------------------------------------------------------
    Composite driverComposite = new Composite(composite, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    driverComposite.setLayoutData(gridData);
    gl = new GridLayout();
    gl.numColumns = 3;
    gl.marginWidth = 0;
    gl.marginHeight = 0;
    gl.marginTop = 10;
    driverComposite.setLayout(gl);

    driverLabel = new Label(driverComposite, SWT.RIGHT);
    driverLabel.setText("JDBC &Driver:");
    driverLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

    driverCombo = new Combo(driverComposite, SWT.BORDER | SWT.READ_ONLY);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    driverCombo.setLayoutData(gridData);
    driverCombo.addSelectionListener(this);

    driverBtn = new Button(driverComposite, SWT.PUSH);
    driverBtn.setText("Driver &Manager");
    driverBtn.addSelectionListener(this);

    //---------------------------------------------------------------------------------
    driverPropertiesGroup  = new Group(composite, SWT.NONE);
    driverPropertiesGroup.setText("Driver parameters");
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    //gridData.horizontalSpan = 3;
    driverPropertiesGroup.setLayoutData(gridData);
    gl = new GridLayout();
    gl.marginWidth = 10;
    gl.marginHeight = 10;
    driverPropertiesGroup.setLayout(gl);

    Composite buttonComposite = new Composite(driverPropertiesGroup, SWT.NONE);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    buttonComposite.setLayoutData(gridData);
    gl = new GridLayout();
    gl.numColumns = 2;
    gl.marginWidth = 0;
    gl.marginHeight = 0;
    buttonComposite.setLayout(gl);

    basicButton = new Button(buttonComposite, SWT.RADIO);
    basicButton.setText("&Basic");
    basicButton.setSelection(true);

    advancedButton = new Button(buttonComposite, SWT.RADIO);
    advancedButton.setText("&Advanced");
    advancedButton.addSelectionListener(this);
    gridData = new GridData();
    advancedButton.setLayoutData(gridData);


    //---------------------------------------------------------------------------------
    easySetupComposite = new Composite(driverPropertiesGroup, SWT.NONE);
    gridData = new GridData();
    gridData.exclude = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    easySetupComposite.setLayoutData(gridData);
    gl = new GridLayout();
    gl.marginWidth = 0;
    gl.marginHeight = 0;
    gl.numColumns = 3;
    easySetupComposite.setLayout(gl);

    //---------------------------------------------------------------------------------
    advancedSetupComposite = new Composite(driverPropertiesGroup, SWT.NONE);
    gridData = new GridData();
    gridData.exclude = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    advancedSetupComposite.setLayoutData(gridData);
    gl = new GridLayout();
    gl.marginWidth = 0;
    gl.marginHeight = 0;
    gl.numColumns = 2;
    advancedSetupComposite.setLayout(gl);

    urlExampleLabel = new Label(advancedSetupComposite, SWT.RIGHT);
    urlExampleLabel.setText("URL Example:");
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.RIGHT;
    gridData.horizontalIndent = 20;
    urlExampleLabel.setLayoutData(gridData);

    urlExampleText = new Text(advancedSetupComposite, SWT.BORDER);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.widthHint = 100; // without this the control can cause the whole composite to expand out of the window when a new driver is selected
    urlExampleText.setLayoutData(gridData);
    urlExampleText.setEditable(false);
    urlExampleText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

    urlLabel = new Label(advancedSetupComposite, SWT.RIGHT);
    urlLabel.setText("&JDBC URL:");
    gridData = new GridData();
    gridData.horizontalIndent = 20;
    gridData.horizontalAlignment = SWT.RIGHT;
    urlLabel.setLayoutData(gridData);

    urlText = new Text(advancedSetupComposite, SWT.BORDER);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.widthHint = 100; // without this the control can cause the whole composite to expand out of the window when a new driver is selected
    urlText.setLayoutData(gridData);

    //---------------------------------------------------------------------------------

    Group loginGroup  = new Group(composite, SWT.NONE);
    loginGroup.setText("Logon Credentials");
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    //gridData.horizontalSpan = 3;
    loginGroup.setLayoutData(gridData);
    gl = new GridLayout();
    gl.marginWidth = 10;
    gl.marginHeight = 10;
    gl.numColumns = 2;
    loginGroup.setLayout(gl);

    promptButton = new Button(loginGroup, SWT.CHECK);
    promptButton.setText("P&rompt for Username and/or password");
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    promptButton.setLayoutData(gridData);

    usernameLabel = new Label(loginGroup, SWT.RIGHT);
    usernameLabel.setText("&Username:");
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.RIGHT;
    gridData.horizontalIndent = 20;
    usernameLabel.setLayoutData(gridData);

    usernameText = new Text(loginGroup, SWT.BORDER);
    gridData = new GridData();
    gridData.widthHint = 150;
    usernameText.setLayoutData(gridData);

    passwordLabel = new Label(loginGroup, SWT.RIGHT);
    passwordLabel.setText("&Password:");
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.RIGHT;
    gridData.horizontalIndent = 20;
    passwordLabel.setLayoutData(gridData);

    passwordText = new Text(loginGroup, SWT.BORDER | SWT.PASSWORD);
    gridData = new GridData();
    gridData.widthHint = 150;
    passwordText.setLayoutData(gridData);

    new Label(loginGroup, SWT.NONE); // skip a column

    Label label = new Label(loginGroup, SWT.NONE);
    label.setText("Note: Passwords are stored as plain text");
    gridData = new GridData();
    label.setLayoutData(gridData);
    //---------------------------------------------------------------------------------

    testBtn = createButton(0);
    testBtn.setText("&Test");
    testBtn.addSelectionListener(this);

    okBtn = createButton(20);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());

    mnemonics.add("c");
    mnemonics.add("d");
    mnemonics.add("b");
    mnemonics.add("a");
    mnemonics.add("j");
    mnemonics.add("u");
    mnemonics.add("p");

    updateList();

    ApplicationData.getInstance().addDriverChangeListener(this);

    showEasySettings(false);
    basicButton.setEnabled(false);
    advancedButton.setEnabled(false);

    if (this.aliasVO != null)
      setAliasVO(this.aliasVO);
  }

  public void setAliasVO(AliasVO aliasVO)
  {
    DriverVO driver = ApplicationData.getInstance().getDriver(aliasVO.getDriverName());
    if (driver != null && driver.getUseBasicParameters())
    {
      List parameters = driver.getUrlParameters();
      if (parameters.size() > 0)
      {
        Map map = driver.parseUrlParameters(aliasVO.getURL());
        driverParamMap.putAll(map);
      }
    }

    if ((driver == null || !driver.isActive())
        && aliasVO.getURL().length() > 0)
    {
      // no driver is setup for this alias or it is no longer active
      // so make sure the advanced driver params are used for the first
      // driver that gets selected (to preserver the JDBC URL)
      firstAdvanced = true;
    }

    nameText.setText(aliasVO.getName());
    urlText.setText(aliasVO.getURL());
    usernameText.setText(aliasVO.getUsername());
    passwordText.setText(aliasVO.getPassword());
    select(aliasVO.getDriverName());
    promptButton.setSelection(aliasVO.getPrompt());
  }

  public AliasVO getAliasVO()
  {
    AliasVO aliasVO = new AliasVO();

    aliasVO.setName(nameText.getText());
    aliasVO.setDriverName(driverCombo.getText());
    aliasVO.setURL(urlText.getText());
    aliasVO.setUsername(usernameText.getText());
    aliasVO.setPassword(passwordText.getText());
    aliasVO.setPrompt(promptButton.getSelection());

    return aliasVO;
  }

  public AliasVO open(int x, int y)
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
      boolean exists = false;
      if (edit)
      {
        // edit being done
        if (!getAliasVO().getName().equalsIgnoreCase(aliasVO.getName()))
        {
          // name was changed
          exists = ApplicationData.getInstance().getAliasExists(getAliasVO());
        }
      }
      else
        exists = ApplicationData.getInstance().getAliasExists(getAliasVO());

      if (exists)
      {
        MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
        messageDlg.setText("Already Exists");
        messageDlg.setMessage("A connection with that connection alias already exists");
        messageDlg.open();
        nameText.setFocus();
      }
      else if (getAliasVO().getName().trim().length() == 0)
      {
        MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
        messageDlg.setText("Invalid Connection Alias");
        messageDlg.setMessage("Conneciton Alias can not be blank");
        messageDlg.open();
        nameText.setFocus();
      }
      else
      {
        if (basicButton.getSelection())
          buildUrl();
        returnVal = getAliasVO();
        getShell().dispose();
      }
    }
    else if (e.widget == testBtn)
    {
      if (basicButton.getSelection())
        buildUrl();
      final AliasVO aliasVO = getAliasVO();
      String username = aliasVO.getUsername();
      String password = aliasVO.getPassword();
      boolean open = true;

      if (!AliasUtil.validateAlias(aliasVO, getShell()))
        open = false;
      else if (aliasVO.getPrompt())
      {
        UserInfoDialog uid = new UserInfoDialog(getShell(), aliasVO.getName());
        uid.setUsername(username);
        uid.setPassword(password);
        if (uid.open(-1, -1) == SWT.OK)
        {
          username = uid.getUsernameText();
          password = uid.getPasswordText();
        }
        else
          open = false;
      }

      if (open)
      {
        final String usernameFinal = username;
        final String passwordFinal = password;
        WorkerRunnable worker = new WorkerRunnable()
        {
          Exception ex = null;

          @Override
          public void doTask()
          {
            ConnectionManager cc = new ConnectionManager(null, aliasVO, usernameFinal, passwordFinal);
            try
            {
              cc.open();
            }
            catch (Exception e)
            {
              ex = e;
            }
            finally
            {
              try
              {
                cc.closeAndShutdown();
              }
              catch (SQLException ex2)
              {
                ex = ex2;
              }
            }
          }

          @Override
          public void doUITask()
          {
            if (ex != null)
            {
              MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
              messageDlg.setText("Failed");
              messageDlg.setMessage(ex.getClass().getName() + "\n\n" + ex.getMessage());
              messageDlg.open();
            }
            else
            {
              MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
              messageDlg.setText("Success");
              messageDlg.setMessage("Connection opened successfully");
              messageDlg.open();
            }

            getShell().setEnabled(true);
            BusyManager.setNotBusy(getShell());
          }
        };

        getShell().setEnabled(false);
        BusyManager.setBusy(getShell());
        worker.startTask();
      }
    }
    else if (e.widget == driverCombo)
    {
      setDriverProperties();
    }
    else if (e.widget == driverBtn)
    {
      DriverManagementDialog dd = DriverManagementDialog.getInstance(Stella.getInstance().getShell());
      dd.open(-1, -1);
      dd.select(driverCombo.getText());
    }
    else if (e.widget == advancedButton)
    {
      if (advancedButton.getSelection())
      {
        buildUrl();
      }
      else
      {
        DriverVO driver = ApplicationData.getInstance().getDriver(driverCombo.getText());
        if (driver != null && driver.getUseBasicParameters())
        {
          List parameters = driver.getUrlParameters();
          if (parameters.size() > 0)
          {
            Map map = driver.parseUrlParameters(urlText.getText());

            for (Iterator it = driverParamTextList.iterator(); it.hasNext();)
            {
              Text text = (Text)it.next();
              DriverParameter dp = (DriverParameter)text.getData();
              if (map.containsKey(dp.getName()))
                  text.setText((String)map.get(dp.getName()));
            }
          }
        }
      }

      showEasySettings(!advancedButton.getSelection());
    }
  }

  private void disposeEasySettings()
  {
    Control[] children = easySetupComposite.getChildren();
    for (int index = 0; index < children.length; index++)
    {
      children[index].dispose();
    }
  }

  private String getLabelText(String name, List mnemonics)
  {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(Character.toUpperCase(name.charAt(0)));
    if (name.length() > 1)
      sbuf.append(name.substring(1));

    boolean found = false;
    for (int index = 0; index < name.length(); index++)
    {
      String c = "" + Character.toLowerCase(name.charAt(index));
      if (!mnemonics.contains(c))
      {
        found = true;
        mnemonics.add(c);
        sbuf.insert(index, "&");
        break;
      }
    }

    if (!found)
      sbuf.insert(0, "&");

    sbuf.append(":");

    return sbuf.toString();
  }

  private void showControl(Control control, boolean show)
  {
    GridData gd = (GridData)control.getLayoutData();
    gd.exclude = !show;
    control.setVisible(show);
  }

  private void showEasySettings(boolean show)
  {
    showControl(easySetupComposite, show);
    showControl(advancedSetupComposite, !show);

    basicButton.setSelection(show);
    advancedButton.setSelection(!show);

    getShell().layout(true, true);

    Point preferred = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    Point size = getShell().getSize();
    if (preferred.x > size.x || preferred.y > size.y)
    {
      if (preferred.x > size.x)
        size.x = preferred.x;
      if (preferred.y > size.y)
        size.y = preferred.y;
      getShell().setSize(size);
    }

  }

  private void preserveProperties()
  {
    for (Iterator it = driverParamTextList.iterator(); it.hasNext();)
    {
      Text text = (Text)it.next();
      DriverParameter dp = (DriverParameter)text.getData();
      if (!text.getText().equals(dp.getValue()))
        driverParamMap.put(dp.getName(), text.getText());
    }
  }

  private void buildUrl()
  {
    DriverVO driver = ApplicationData.getInstance().getDriver(driverCombo.getText());
    if (driver != null)
    {
      Map map = new HashMap();
      for (Iterator it = driverParamTextList.iterator(); it.hasNext();)
      {
        Text text = (Text)it.next();
        DriverParameter dp = (DriverParameter)text.getData();

        if (text.getText().trim().length() > 0)
        {
          map.put(dp.getName(), text.getText());
        }
      }

      String url = driver.buildUrl(map);

      urlText.setText(url);
    }
  }

  private void setDriverProperties()
  {
    if (driverCombo.getSelectionIndex() >= 0)
    {
      preserveProperties();
      driverParamTextList.clear();
      disposeEasySettings();



      DriverVO driver = ApplicationData.getInstance().getDriver(driverCombo.getText());
      if (driver != null)
      {
        urlExampleText.setText(driver.getExampleJdbcUrl());
        if (urlText.getText().trim().length() > 0 && driver.getUseBasicParameters())
        {
          // there is a URL entered so parse it with the newly selected
          // driver and add any params it find to the map
          Map map = driver.parseUrlParameters(urlText.getText());
          driverParamMap.putAll(map);
        }

        List mnemonics = new LinkedList(this.mnemonics);
        List parameters = driver.getUrlParameters();
        if (driver.getUseBasicParameters()
            && parameters.size() > 0)
        {

          for (Iterator it = parameters.iterator(); it.hasNext();)
          {
            DriverParameter dp = (DriverParameter)it.next();
            Label label = new Label(easySetupComposite, SWT.NONE);
            label.setText(getLabelText(dp.getName(), mnemonics));
            GridData gridData = new GridData();
            gridData.horizontalAlignment = SWT.RIGHT;
            gridData.horizontalIndent = 20;
            label.setLayoutData(gridData);

            Text text = new Text(easySetupComposite, SWT.BORDER);
            if (driverParamMap.containsKey(dp.getName().toLowerCase()))
              text.setText((String)driverParamMap.get(dp.getName().toLowerCase()));
            else
              text.setText(dp.getValue());
            text.setData(dp);
            gridData = new GridData();
            gridData.widthHint = 200;
            text.setLayoutData(gridData);

            driverParamTextList.add(text);

            label = new Label(easySetupComposite, SWT.NONE);
            if (dp.getRequired())
              label.setText("required");
            else
              label.setText("optional");
          }
          FontSetter.setAllControlFonts(easySetupComposite, ApplicationData.getInstance().getGeneralFont());

          advancedButton.setEnabled(true);
          basicButton.setEnabled(true);


          if (firstAdvanced)
          {
            firstAdvanced = false;
            showEasySettings(false);
          }
          else
            showEasySettings(true);
        }
        else
        {
          showEasySettings(false);
          basicButton.setEnabled(false);
          advancedButton.setEnabled(false);
        }
      }

    }
  }


  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    ApplicationData.getInstance().removeDriverChangeListener(this);
  }

  private void updateList()
  {
    driverCombo.removeAll();
    int count = ApplicationData.getInstance().getDriverCount();
    for (int index = 0; index < count; index++)
    {
      DriverVO driverVO = ApplicationData.getInstance().getDriver(index);
      if (driverVO.isActive())
        driverCombo.add(driverVO.getName());
    }
  }

  private void select(String name)
  {
    if (driverCombo.indexOf(name) >= 0)
      driverCombo.select(driverCombo.indexOf(name));

    setDriverProperties();
  }

  @Override
  public void driverChanged(String oldName, String newName)
  {
    String selection = driverCombo.getText();
    if (selection.equals(oldName))
      selection = newName;

    updateList();
    select(selection);
  }

  @Override
  public void driverRemoved(String newName)
  {
    String selection = driverCombo.getText();
    updateList();
    select(selection);
  }

  @Override
  public void driverAdded(String newName)
  {
    String selection = driverCombo.getText();
    updateList();
    select(selection);
  }

}
