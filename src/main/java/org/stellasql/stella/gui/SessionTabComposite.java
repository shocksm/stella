package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.gui.util.DraggableTabHelper;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.session.SessionData;

public class SessionTabComposite extends Composite implements DisposeListener, FontChangeListener, CTabFolder2Listener
{
  private CTabFolder tabFolder = null;

  public SessionTabComposite(Composite parent)
  {
    super(parent, SWT.NONE);

    addDisposeListener(this);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    this.setLayout(gridLayout);

    tabFolder = new CTabFolder(this, SWT.BORDER | SWT.CLOSE);
    tabFolder.addCTabFolder2Listener(this);
    tabFolder.setSimple(false);
    tabFolder.setTabHeight(22);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    gridData.widthHint = 350;
    gridData.heightHint = 100;
    tabFolder.setLayoutData(gridData);

    Color clr1 = this.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
    Color clr2 = this.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
    Color clr3 = this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    Color[] gradient = new Color[] { clr1, clr2, clr3};
    int[] percentages = new int[] { 50, 100};
    tabFolder.setSelectionBackground(gradient, percentages, true);
    tabFolder.setSelectionForeground(this.getDisplay().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));

    new DraggableTabHelper(tabFolder);

    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);

    this.pack();
  }

  public int getSessionCount()
  {
    return tabFolder.getItemCount();
  }

  private void setFonts()
  {
    FontSetter.setFont(tabFolder, ApplicationData.getInstance().getGeneralFont());
  }

  public void addSession(AliasVO aliasVO, String username, String password)
  {
    CTabItem item = new CTabItem(tabFolder, SWT.NONE);
    int count = 2;
    String sessionName = aliasVO.getName();
    while (SessionData.getSessionData(sessionName) != null)
      sessionName = aliasVO.getName() + " " + count++;

    item.setText(sessionName);
    item.setToolTipText("As user: " + aliasVO.getUsername());

    boolean autoCommit = ApplicationData.getInstance().getAutoCommit();
    boolean limitResults = ApplicationData.getInstance().getLimitResults();
    int maxRows = ApplicationData.getInstance().getMaxRows();
    String querySeparator = ApplicationData.getInstance().getQuerySeparator();
    boolean stripComments = ApplicationData.getInstance().getStripComments();

    SessionData.createSessionData(aliasVO, sessionName, username, password, autoCommit, limitResults, maxRows, querySeparator, stripComments);

    SessionComposite sessionComposite = new SessionComposite(tabFolder, sessionName);
    item.setData(sessionName);
    item.setControl(sessionComposite);
    tabFolder.setSelection(item);

    sessionComposite.init();
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    if (e.widget == this)
    {
      ApplicationData.getInstance().removeFontChangeListener(this);
    }
  }

  @Override
  public void fontChanged()
  {
    setFonts();
    layout(true, true);
  }

  public CTabFolder getTabFolder()
  {
    return tabFolder;
  }

  public boolean closeAll()
  {
    CTabItem[] items = tabFolder.getItems();
    // check to see if it is ok to close
    for (int index = 0; index < items.length; index++)
    {
      String sessionName = (String)items[index].getData();
      if (!SessionData.getSessionData(sessionName).sessionPreparingToEnd())
        return false;
    }

    // close the tabs
    for (int index = 0; index < items.length; index++)
    {
      String sessionName = (String)items[index].getData();
      SessionData.getSessionData(sessionName).sessionEnded();
      if (items[index].getControl() != null)
        items[index].getControl().dispose();
      items[index].dispose();
    }

    return true;
  }

  public boolean closeTab(String sessionName)
  {
    CTabItem[] items = tabFolder.getItems();
    // check to see if it is ok to close
    for (int index = 0; index < items.length; index++)
    {
      String currentSessionName = (String)items[index].getData();
      if (sessionName.equals(currentSessionName))
      {
        if (!SessionData.getSessionData(sessionName).sessionPreparingToEnd())
          return false;

        SessionData.getSessionData(sessionName).sessionEnded();
        if (items[index].getControl() != null)
          items[index].getControl().dispose();
        items[index].dispose();

        return true;
      }
    }

    return false;
  }

  public void closeSelected()
  {
    closeTab(tabFolder.getSelection());
  }

  public SessionData getSelectedSessionData()
  {
    SessionData sessionData = null;

    if (tabFolder.getSelection() != null)
    {
      CTabItem item = tabFolder.getSelection();
      String sessionName = (String)item.getData();
      sessionData = SessionData.getSessionData(sessionName);
    }

    return sessionData;
  }

  private boolean closeTab(CTabItem item)
  {
    String sessionName = (String)item.getData();

    if (SessionData.getSessionData(sessionName).sessionPreparingToEnd())
    {
      SessionData.getSessionData(sessionName).sessionEnded();
      if (item.getControl() != null)
        item.getControl().dispose();
      item.dispose();

      return true;
    }

    return false;
  }

  @Override
  public void close(CTabFolderEvent e)
  {
    CTabItem item = (CTabItem)e.item;

    if (!closeTab(item))
      e.doit = false;
  }

  @Override
  public void minimize(CTabFolderEvent event)
  {
  }
  @Override
  public void maximize(CTabFolderEvent event)
  {
  }
  @Override
  public void restore(CTabFolderEvent event)
  {
  }
  @Override
  public void showList(CTabFolderEvent event)
  {
  }

  public static void main(String[] args)
  {

    Display display = new Display();
    StellaClipBoard.init(display);

    Shell shell = new Shell(display);
    shell.setText("Test");
    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 5;
    gridLayout.marginWidth = 5;
    gridLayout.marginBottom = 5;
    gridLayout.horizontalSpacing = 0;
    shell.setLayout(gridLayout);

    CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER | SWT.CLOSE);
    tabFolder.setSimple(false);
    tabFolder.setTabHeight(22);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    gridData.widthHint = 350;
    gridData.heightHint = 100;
    tabFolder.setLayoutData(gridData);

    Color clr1 = tabFolder.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
    Color clr2 = tabFolder.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
    Color[] gradient = new Color[] { clr1, clr2};
    int[] percentages = new int[] { 100};

    tabFolder.setSelectionBackground(gradient, percentages, true);
    tabFolder.setSelectionForeground(tabFolder.getDisplay().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));

    CTabItem item = new CTabItem(tabFolder, SWT.NONE);
    item.setText("Tab One");

    item = new CTabItem(tabFolder, SWT.NONE);
    item.setText("Tab Two");

    item = new CTabItem(tabFolder, SWT.NONE);
    item.setText("Tab Three");


    shell.setSize(500, 400);

    shell.open();

    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }






}
