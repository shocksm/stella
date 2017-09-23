package org.stellasql.stella.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.connection.QueryListener;
import org.stellasql.stella.export.ExportOptions;
import org.stellasql.stella.gui.util.DraggableTabHelper;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.session.SQLResultHandler;
import org.stellasql.stella.session.SessionData;
import org.stellasql.stella.session.SessionReadyListener;

public class QueryComposite extends Composite implements SelectionListener, MouseListener, QueryListener, MenuListener, CTabFolder2Listener, DisposeListener, FontChangeListener, SessionReadyListener, SQLResultHandler
{
  private static final String STATUS_KEY = "STATUS_KEY";
  private static final String STATUS_RUNNING = "RUNNING";
  private static final String STATUS_COMPLETE = "COMPLETE";
  private static final String QUERY_COUNT = "QUERY_COUNT";
  private static final String RESULTS_COUNT = "RESULTS_COUNT";

  private CTabFolder tabFolder = null;
  private String sessionName = "";
  private int queryCount = 1;
  private Composite tabComposite = null;
  private SessionComposite sessionComposite = null;
  private ToolItem maximizeButton = null;
  private boolean maximized = false;
  private QueryTextComposite qtc = null;
  private Menu menu = null;
  private MenuItem closeTabMI = null;
  private MenuItem closeOtherTabsMI = null;
  private MenuItem closeAllTabsMI = null;
  private CTabItem menuSelectedItem = null;
  private int lastSashPosition = 0;
  private Sash sash = null;
  private int tabClosedTime = 0;

  public QueryComposite(Composite parent, SessionComposite sessionComposite, String sessionName)
  {
    super(parent, SWT.NONE);

    addDisposeListener(this);

    this.sessionComposite = sessionComposite;
    this.sessionName = sessionName;
    SessionData.getSessionData(this.sessionName).setQueryHandler(this);
    SessionData.getSessionData(this.sessionName).addSessionReadyListener(this);
    SessionData.getSessionData(this.sessionName).setSQLResultHandler(this);

    this.setLayout(new FormLayout());

    qtc = new QueryTextComposite(this, this, this.sessionName);

    sash = new Sash(this, SWT.HORIZONTAL | SWT.SMOOTH);
    sash.addSelectionListener(this);

    tabComposite = new Composite(this, SWT.NONE);

    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(sash, 0);
    qtc.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 250);
    fd.right = new FormAttachment(100, 0);
    fd.height = 4;
    sash.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(sash, 0);
    fd.bottom = new FormAttachment(100, 0);
    tabComposite.setLayoutData(fd);



    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.verticalSpacing = 0;
    tabComposite.setLayout(gridLayout);

    tabFolder = new CTabFolder(tabComposite, SWT.BORDER);
    tabFolder.setSimple(false);
    tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    tabFolder.addCTabFolder2Listener(this);
    Color clr1 = this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
    Color clr2 = this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    Color clr3 = this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    Color[] gradient = new Color[] { clr1, clr2, clr3, clr3};
    int[] percentages = new int[] { 50, 60, 100};
    tabFolder.setSelectionBackground(gradient, percentages, true);


    ToolBar toolBar = new ToolBar(tabFolder, SWT.FLAT);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.END;
    gridData.verticalAlignment = SWT.BEGINNING;
    toolBar.setLayoutData(gridData);
    maximizeButton = new ToolItem(toolBar, SWT.PUSH);
    maximizeButton.setToolTipText("Maximize");
    maximizeButton.setImage(StellaImages.getInstance().getMaximizeImage());
    maximizeButton.addSelectionListener(this);
    maximizeButton.setEnabled(false);

    new DraggableTabHelper(tabFolder);

    tabFolder.setTabHeight(22);
    tabFolder.setTopRight(toolBar);
    tabFolder.addMouseListener(this);

    menu = new Menu(tabFolder);
    menu.addMenuListener(this);
    // show the menu manually
    // so we can only show it when a tab is clicked on
    // tabFolder.setMenu(menu);

    closeTabMI = new MenuItem(menu, SWT.PUSH);
    closeTabMI.addSelectionListener(this);
    closeTabMI.setText("&Close Tab");

    closeOtherTabsMI = new MenuItem(menu, SWT.PUSH);
    closeOtherTabsMI.addSelectionListener(this);
    closeOtherTabsMI.setText("Close &Other Tabs");

    closeAllTabsMI = new MenuItem(menu, SWT.PUSH);
    closeAllTabsMI.addSelectionListener(this);
    closeAllTabsMI.setText("Close &All Tabs");

    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);

    SessionData.getSessionData(this.sessionName).setResultsTab(tabFolder);
  }

  protected void setFonts()
  {
    FontSetter.setFont(tabFolder, ApplicationData.getInstance().getGeneralFont());
  }

  public void setMaximized(Control control)
  {
    if (control != null)
    {
      FormData fd = (FormData)sash.getLayoutData();
      lastSashPosition = sash.getLocation().y;

      if (control == qtc)
        fd.top = new FormAttachment(100, 0);
      else
        fd.top = new FormAttachment(0, -sash.getSize().y);

      sash.setVisible(false);
      sash.getParent().layout();
    }
    else
    {
      FormData fd = (FormData)sash.getLayoutData();
      fd.top = new FormAttachment(0, lastSashPosition);
      sash.setVisible(true);
      sash.getParent().layout();
    }

    if (control != null)
      sessionComposite.setMaximized(this);
    else
      sessionComposite.setMaximized(null);
  }

  public void restoreResultsTabs()
  {
    if (qtc.isMaximized())
    {
      qtc.setMaximized(false);
    }
  }

  public void connectionOpened()
  {
    qtc.connectionOpened();
  }

  public void rerun(ResultTableComposite rtc)
  {
    CTabItem[] items = tabFolder.getItems();
    for (int index = 0; index < items.length; index++)
    {
      if (items[index].getControl() == rtc)
      {
        String query = rtc.getQuery();
        closeTab(items[index]);

        int maxResults = 0;
        if (SessionData.getSessionData(sessionName).getLimitResults())
          maxResults = SessionData.getSessionData(sessionName).getMaxRows();
        runQuery(query, maxResults, false);
        break;
      }
    }
  }

  public void runQuery(String query, int maxResults, boolean transaction)
  {
    CTabItem item = createTab(1);
    processQuery(query, item, maxResults, transaction, null);
    QueryStatusComposite qsc = (QueryStatusComposite)item.getControl();
    qsc.finishedAdding();
  }

  private CTabItem createTab(int queryCount)
  {
    CTabItem item = new CTabItem(tabFolder, SWT.NONE);
    int count = this.queryCount++;
    item.setText("Running Query " + count);
    addItemData(item, STATUS_KEY, STATUS_RUNNING);
    addItemData(item, QUERY_COUNT, new Integer(count));

    QueryStatusComposite qsc = new QueryStatusComposite(tabFolder, sessionName, this, item, "Query " + count, queryCount);
    item.setControl(qsc);

    tabFolder.setSelection(item);
    tabFolder.update();

    return item;
  }

  private void processQuery(String query, CTabItem item, int maxResults, boolean transaction, ExportOptions eo)
  {
    QueryStatusComposite qsc = (QueryStatusComposite)item.getControl();
    Object key = SessionData.getSessionData(sessionName).getQuerySequencer().runQuery(this, query, item, maxResults, transaction, eo);
    qsc.addQuery(query, key);
  }

  public void runQueries(final List queryList, int maxResults, ExportOptions eo)
  {
    CTabItem item = createTab(queryList.size());
    QueryStatusComposite qsc = (QueryStatusComposite)item.getControl();

    Iterator it = queryList.iterator();
    while (it.hasNext())
    {
      String query = (String)it.next();
      processQuery(query, item, maxResults, true, eo);
    }
    qsc.finishedAdding();
  }

  private void addItemData(CTabItem item, String key, Object value)
  {
    Map map = (Map)item.getData();
    if (map == null)
    {
      map = new HashMap();
      item.setData(map);
    }
    map.put(key, value);
  }

  private Object getItemData(CTabItem item, String key)
  {
    Map map = (Map)item.getData();
    if (map != null)
    {
      return map.get(key);
    }
    return null;
  }

  @Override
  public void queryBeingExecuted(final String query, final Object data, final Object key)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        CTabItem item = (CTabItem)data;
        if (!item.isDisposed())
        {
          QueryStatusComposite qsc = (QueryStatusComposite)item.getControl();
          qsc.queryBeingExecuted(query, key);
        }
      }});
  }

  @Override
  public void queryCanceled(String query, final Object data, final Object key)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        CTabItem item = (CTabItem)data;
        if (!item.isDisposed())
        {
          QueryStatusComposite qsc = (QueryStatusComposite)item.getControl();
          qsc.queryCanceled(key);
        }
      }});
  }

  @Override
  public void queryRan(final String query, final boolean valid, final Object data, final Object key, final List resultsList, final int limitedRows, final long queryMillis, final long formatMillis)
  {
    this.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
          CTabItem item = (CTabItem)data;
          if (!item.isDisposed())
          {
            int resultsCount = 1;
            Integer resultsInteger = ((Integer)getItemData(item, RESULTS_COUNT));
            if (resultsInteger == null)
            {
              addItemData(item, RESULTS_COUNT, new Integer(1));
            }
            else
            {
              resultsCount = resultsInteger.intValue() + 1;
              addItemData(item, RESULTS_COUNT, new Integer(resultsCount));
            }

            QueryStatusComposite qsc = (QueryStatusComposite)item.getControl();

            if (resultsList.size() > 0)
            {
              int count = 1;
              int queryNumber = ((Integer)getItemData(item, QUERY_COUNT)).intValue();
              boolean first = true;
              for (Iterator resultIt = resultsList.iterator(); resultIt.hasNext();)
              {
                Object resultObject = resultIt.next();
                if (resultObject instanceof ResultData)
                {
                  ResultData rd = (ResultData)resultObject;
                  ResultTableComposite rtc = new ResultTableComposite(tabFolder, sessionName, QueryComposite.this);

                  CTabItem newItem = new CTabItem(tabFolder, SWT.CLOSE);
                  String text = "Results "+ queryNumber;
                  if (resultsCount > 1)
                    text += " - " + resultsCount;
                  if (count > 1)
                    text += " - " + count;
                  count++;
                  newItem.setText(text);
                  newItem.setControl(rtc);
                  addItemData(newItem, STATUS_KEY, STATUS_COMPLETE);
                  if (first)
                  {
                    tabFolder.setSelection(newItem);
                    first = false;
                  }

                  restoreResultsTabs();

                  rtc.setResultData(rd, limitedRows, queryMillis, formatMillis);
                }
              }
            }

            qsc.queryComplete(query, key, resultsList, queryMillis, formatMillis);
          }

      }
    });
  }

  public void makeTabClosable(CTabItem item)
  {
    if (!item.isDisposed())
    {
      CTabItem[] items = tabFolder.getItems();
      int index = 0;
      for (int i = 0; i < items.length; i++)
      {
        if (items[i] == item)
        {
          index = i;
          break;
        }
      }

      boolean select = false;
      if (tabFolder.getSelection() == item)
        select = true;

      Integer count = (Integer)getItemData(item, QUERY_COUNT);
      QueryStatusComposite qsc = (QueryStatusComposite)item.getControl();

      item.dispose();
      item = new CTabItem(tabFolder, SWT.CLOSE, index);

      item.setText("Query " + count);
      addItemData(item, STATUS_KEY, STATUS_COMPLETE);
      item.setControl(qsc);
      if (select)
        tabFolder.setSelection(item);
    }
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == maximizeButton)
    {
      toggleMaximize();
    }
    else if (e.widget == closeTabMI && menuSelectedItem != null)
    {
      closeTab(menuSelectedItem);
      menuSelectedItem = null;
    }
    else if (e.widget == closeOtherTabsMI && menuSelectedItem != null)
    {
      CTabItem[] items = tabFolder.getItems();
      for (int index = 0; index < items.length; index++)
      {
        if (items[index] != menuSelectedItem)
        {
          if (getItemData(items[index], STATUS_KEY) == STATUS_COMPLETE)
            closeTab(items[index]);
        }
      }
      menuSelectedItem = null;
    }
    else if (e.widget == closeAllTabsMI && menuSelectedItem != null)
    {
      CTabItem[] items = tabFolder.getItems();
      for (int index = 0; index < items.length; index++)
      {
        if (getItemData(items[index], STATUS_KEY) == STATUS_COMPLETE)
          closeTab(items[index]);
      }
      menuSelectedItem = null;
    }
    else if (e.widget == sash)
    {
      int limit = 150;
      Rectangle sashRect = sash.getBounds();
      Rectangle shellRect = sash.getParent().getClientArea();
      int bottom = shellRect.height - sashRect.height - limit;
      e.y = Math.max(Math.min(e.y, bottom), limit);
      if (e.y != sashRect.y)
      {
        FormData fd = (FormData)sash.getLayoutData();
        fd.top = new FormAttachment(0, e.y);
        lastSashPosition = e.y;
        sash.getParent().layout();
      }
    }

  }

  private void toggleMaximize()
  {
    if (maximized)
    {
      maximizeButton.setToolTipText("Maximize");
      maximizeButton.setImage(StellaImages.getInstance().getMaximizeImage());
      setMaximized(null);
    }
    else
    {
      maximizeButton.setToolTipText("Restore");
      maximizeButton.setImage(StellaImages.getInstance().getRestoreImage());
      setMaximized(tabComposite);
    }

    maximized = !maximized;
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
    // if the user is quickly closing tabs, don't maximize
    if (e.time - tabClosedTime > 500)
      toggleMaximize();
  }
  @Override
  public void mouseDown(MouseEvent e)
  {
    if (e.widget == tabFolder && e.button == 3)
    {
      menuSelectedItem = null;
      CTabItem item = tabFolder.getItem(new Point(e.x, e.y));
      if (item != null)
      {
        menuSelectedItem = item;
        menu.setLocation(tabFolder.toDisplay(e.x, e.y));
        menu.setVisible(true);
      }
    }
  }
  @Override
  public void mouseUp(MouseEvent e)
  {
  }

  @Override
  public void menuHidden(MenuEvent e)
  {
  }

  @Override
  public void menuShown(MenuEvent e)
  {
    if (getItemData(tabFolder.getSelection(), STATUS_KEY) == STATUS_COMPLETE)
      closeTabMI.setEnabled(true);
    else
      closeTabMI.setEnabled(false);

    if (tabFolder.getItemCount() == 1)
    {
      closeAllTabsMI.setEnabled(false);
      closeOtherTabsMI.setEnabled(false);
    }
    else
    {
      closeAllTabsMI.setEnabled(true);
      closeOtherTabsMI.setEnabled(true);
    }
  }

  protected void closeTab(CTabItem item)
  {
    if (item.getControl() != null)
      item.getControl().dispose();
    item.dispose();
  }

  @Override
  public void close(CTabFolderEvent e)
  {
    tabClosedTime  = e.time;
    closeTab((CTabItem)e.item);
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

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    ApplicationData.getInstance().removeFontChangeListener(this);
  }

  @Override
  public void fontChanged()
  {
    setFonts();
    layout(true, true);
  }

  @Override
  public void sessionReady()
  {
    maximizeButton.setEnabled(true);
  }


  @Override
  public ResultTableComposite getSelectedResults()
  {
    ResultTableComposite rtc = null;
    CTabItem item = tabFolder.getSelection();
    if (item != null)
    {
      Control control = item.getControl();
      if (control instanceof ResultTableComposite)
      {
        rtc = (ResultTableComposite)control;
      }
    }

    return rtc;
  }

}
