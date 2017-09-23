package org.stellasql.stella.gui;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.FontChangeListener;
import org.stellasql.stella.QueryHistoryItem;
import org.stellasql.stella.connection.QueryError;
import org.stellasql.stella.gui.custom.CustomTable;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.session.SessionData;
import org.stellasql.stella.util.TimeFormatter;

public class QueryStatusComposite extends Composite implements DisposeListener, FontChangeListener, SelectionListener
{
  private String sessionName = null;
  private Map queryMap = new HashMap();
  private List keyList = new LinkedList();
  private QueryComposite queryComposite = null;
  private CTabItem item = null;
  private Button cancelButton = null;
  private CustomTable customTable = null;
  private Map itemKeyMap = new HashMap();
  private boolean updateCountReturned = false;
  private boolean errorReturned = false;
  private boolean executingDisplayed = false;
  private String tabName = null;
  private boolean singleQuery = false;
  private ScrolledComposite sc = null;
  private Composite composite = null;
  private Text singleResultsLabel = null;
  private Text singleQueryLabel = null;
  private LinkedList addToHistory = new LinkedList();
  private Label processingCountLabel;
  private int completeCount = 0;

  public QueryStatusComposite(Composite parent, String sessionName, QueryComposite qc, CTabItem item, String tabName, int queryCount)
  {
    super(parent, SWT.NONE);
    this.setBackgroundMode(SWT.INHERIT_DEFAULT);
    //this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

    this.sessionName = sessionName;
    this.tabName  = tabName;
    queryComposite = qc;
    this.item = item;
    addDisposeListener(this);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 1;
    //gridLayout.marginHeight = 0;
    //gridLayout.marginWidth = 0;
    this.setLayout(gridLayout);

    cancelButton = new Button(this, SWT.PUSH);
    FontSetter.setFont(cancelButton, ApplicationData.getInstance().getGeneralFont());
    cancelButton.setText("Cancel");
    cancelButton.addSelectionListener(this);


    if (queryCount == 1)
    {
      singleQuery = true;

      sc = new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
      sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      sc.setBackground(this.getBackground());

      composite = new Composite(sc, SWT.NONE);
      gridLayout = new GridLayout();
      gridLayout.numColumns = 1;
      composite.setLayout(gridLayout);
      composite.setBackground(this.getBackground());
      composite.setBackgroundMode(SWT.INHERIT_DEFAULT);

      sc.setContent(composite);

      singleResultsLabel = new Text(composite, SWT.READ_ONLY | SWT.MULTI);
      singleResultsLabel.setBackground(this.getBackground());
      singleQueryLabel = new Text(composite, SWT.READ_ONLY | SWT.MULTI);
      singleQueryLabel.setBackground(this.getBackground());
    }
    else
    {
      singleQuery = false;

      processingCountLabel = new Label(this, SWT.NONE);

      customTable = new CustomTable(this, true, false, false);
      customTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      customTable.setBackground(this.getBackground());
      customTable.setLinesVisible(false);
      customTable.createTableColumn();
      customTable.createTableColumn();

      // disable the standard tooltip
      customTable.setToolTipText("");
      // Implement a "fake" tooltip so that newlines will work
      final Listener labelListener = new Listener () {
        @Override
        public void handleEvent (Event event) {
          Label label = (Label)event.widget;
          Shell shell = label.getShell ();
          switch (event.type) {
            case SWT.MouseDown:
              Event e = new Event ();
              e.item = (TableItem) label.getData ("_TABLEITEM");
              // Assuming table is single select, set the selection as if
              // the mouse down event went through to the table
              customTable.setSelection(new TableItem [] {(TableItem) e.item});
              customTable.notifyListeners(SWT.Selection, e);
              // fall through
            case SWT.MouseExit:
              shell.dispose ();
              break;
          }
        }
      };

      Listener tableListener = new Listener () {
        Shell tip = null;
        Label label = null;
        @Override
        public void handleEvent (Event event) {
          switch (event.type) {
            case SWT.Dispose:
            case SWT.KeyDown:
            case SWT.MouseMove: {
              if (tip == null) break;
              tip.dispose ();
              tip = null;
              label = null;
              break;
            }
            case SWT.MouseHover: {
              TableItem item = customTable.getItem (new Point (event.x, event.y));

              if (item != null)
              {
                // only show if on the first column
                int column = 0;
                if (item.getBounds().x + customTable.getColumn(0).getWidth() < event.x)
                  column = 1;

                if (tip != null  && !tip.isDisposed ()) tip.dispose ();
                tip = new Shell (QueryStatusComposite.this.getShell(), SWT.ON_TOP | SWT.TOOL);
                tip.setBackground(QueryStatusComposite.this.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
                GridLayout gridLayout = new GridLayout();
                gridLayout.marginWidth = 2;
                gridLayout.marginHeight = 2;
                tip.setLayout(gridLayout);
                label = new Label (tip, SWT.NONE);
                label.setForeground (QueryStatusComposite.this.getDisplay().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
                label.setBackground (QueryStatusComposite.this.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
                label.setData ("_TABLEITEM", item);
                if (column == 0)
                  label.setText((String)item.getData());
                else
                  label.setText(item.getText(1));
                label.addListener (SWT.MouseExit, labelListener);
                label.addListener (SWT.MouseDown, labelListener);
                Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
                Point pt = customTable.toDisplay(event.x, event.y);
                Rectangle rect = QueryStatusComposite.this.getDisplay().getBounds();
                if (pt.x + size.x > rect.width)
                  pt.x = rect.width - size.x;
                if (pt.y + size.y > rect.height)
                  pt.y = rect.height - size.y;
                pt.x = pt.x < 0 ? 0 : pt.x;
                pt.y = pt.y < 0 ? 0 : pt.y;

                tip.setBounds (pt.x, pt.y, size.x, size.y);
                tip.setVisible (true);
              }
            }
          }
        }
      };
      customTable.addListener(SWT.Dispose, tableListener);
      customTable.addListener(SWT.KeyDown, tableListener);
      customTable.addListener(SWT.MouseMove, tableListener);
      customTable.addListener(SWT.MouseHover, tableListener);
    }

    setFonts();
    ApplicationData.getInstance().addFontChangeListener(this);

  }

  public void addQuery(String query, Object key)
  {
    queryMap.put(key, query);
    keyList.add(key);

    if (singleQuery)
    {
      singleResultsLabel.setText("waiting");
      singleQueryLabel.setText(query);
      composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
    else
    {
      TableItem item = customTable.createItem();
      item.setData(query); // put query with newlines in data section for tooltip
      item.setText(0, query.replaceAll("\n", " "));
      item.setText(1, "   waiting");
      itemKeyMap.put(key, item);
    }
  }

  public void finishedAdding()
  {
    if (!singleQuery)
    {
      processingCountLabel.setText("Complete: 0 / " + keyList.size());
      processingCountLabel.pack();

      TableColumn tableColumn = customTable.getColumn(0);
      tableColumn.pack();
      if (tableColumn.getWidth() > 200)
        tableColumn.setWidth(200);

      int width = customTable.getSize().x - tableColumn.getWidth();
      //width = width - customTable.getBorderWidth();

      tableColumn = customTable.getColumn(1);
      tableColumn.setWidth(width);
    }
  }

  public void queryBeingExecuted(String query, Object key)
  {
    if (singleQuery)
    {
      singleResultsLabel.setText("running");
      updateSingleLabel();
    }
    else
    {
      TableItem item = (TableItem)itemKeyMap.get(key);
      item.setText(1, "   running");
    }

    if (!executingDisplayed )
      SessionData.getSessionData(sessionName).addMessage("executing " + tabName, true);
  }

  private void finished(Object key)
  {
    queryMap.remove(key);
    if (queryMap.size() == 0)
    {
      synchronized (this)
      {
        cancelButton.setEnabled(false);
        //cancelButton.dispose();
        //this.layout();
        queryMap = null;

        keyList.clear();
        keyList = null;

        if (errorReturned)
        {
          SessionData.getSessionData(sessionName).addErrorMessage(tabName+ " finished with errors present", true);
        }
        else
        {
          SessionData.getSessionData(sessionName).addSuccessMessage(tabName + " complete", true);
        }

        if (updateCountReturned || errorReturned)
        {
          if (!singleQuery)
          {
            TableColumn tableColumn = customTable.getColumn(1);
            tableColumn.pack();
          }
          queryComposite.makeTabClosable(item);
        }
        else
          queryComposite.closeTab(item);
      }

      if (addToHistory.size() > 0)
        ApplicationData.getInstance().addQueryHistory(addToHistory);

    }
  }

  public void queryCanceled(Object key)
  {
    if (singleQuery)
    {
      singleResultsLabel.setText("canceled");
      singleResultsLabel.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_RED));
      updateSingleLabel();
    }
    else
    {
      TableItem item = (TableItem)itemKeyMap.get(key);
      item.setText(1, "canceled");
      item.setForeground(1, this.getDisplay().getSystemColor(SWT.COLOR_RED));
    }

    //SessionData.getSessionData(sessionName).addMessage("Query canceled", true);

    finished(key);
  }

  public void queryComplete(String query, Object key, List resultsList, long queryMillis, long formatMillis)
  {
    StringBuffer sbuf = new StringBuffer();
    boolean error = false;
    boolean first = true;

    completeCount++;
    if (!singleQuery)
    {
      processingCountLabel.setText("Complete: " + completeCount + " / " + keyList.size());
      processingCountLabel.pack();
    }

    for (Iterator it = resultsList.iterator(); it.hasNext();)
    {
      Object resultObject = it.next();
      if (resultObject instanceof ResultData)
      {
        if (first)
          sbuf.append("complete - ");
        sbuf.append("Results returned");
        if (first)
          sbuf.append(" - time: " + TimeFormatter.format(queryMillis + formatMillis));
      }
      else if (resultObject instanceof Integer)
      {
        if (first)
          sbuf.append("complete - ");
        sbuf.append(NumberFormat.getInstance().format(resultObject));
        sbuf.append(" rows have been affected");
        if (first)
          sbuf.append(" - time: " + TimeFormatter.format(queryMillis + formatMillis));

        updateCountReturned = true;
      }
      else if (resultObject instanceof QueryError)
      {
        String msg = ((QueryError)resultObject).getMessage();
        if (msg != null)
          msg = msg.trim();
        sbuf.append("Error - ").append(msg);
        error = true;
        errorReturned = true;
      }
      else if (resultObject instanceof String)
      {
        String msg = (String)resultObject;
        if (msg != null)
          msg = msg.trim();
        sbuf.append(msg);
        updateCountReturned = true;
      }

      first = false;

      if (it.hasNext())
        sbuf.append("\n");
    }

    if (!errorReturned)
      addToHistory.addFirst(new QueryHistoryItem(SessionData.getSessionData(sessionName).getAlias().getName(), System.currentTimeMillis(), query));


    if (singleQuery)
    {
      singleResultsLabel.setText(sbuf.toString().trim());

      if (error)
        singleResultsLabel.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_RED));
      else
        singleResultsLabel.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));

      updateSingleLabel();
    }
    else
    {
      TableItem item = (TableItem)itemKeyMap.get(key);
      if (error)
        item.setForeground(1, this.getDisplay().getSystemColor(SWT.COLOR_RED));
      else
        item.setForeground(1, this.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
      item.setText(1, sbuf.toString());
    }

    finished(key);
  }

  private void updateSingleLabel()
  {
    singleResultsLabel.pack();
    composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  protected void setFonts()
  {
    FontSetter.setAllControlFonts(this, ApplicationData.getInstance().getResultsFont());
    /*
    FontSetter.setFont(cancelButton, ApplicationData.getInstance().getResultsFont());
    if (singleQuery)
    {
      FontSetter.setFont(singleQueryLabel, ApplicationData.getInstance().getResultsFont());
      FontSetter.setFont(singleResultsLabel, ApplicationData.getInstance().getResultsFont());
    }
    else
    {
      FontSetter.setFont(customTable, ApplicationData.getInstance().getResultsFont());
    }
    */
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
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelButton)
    {
      List cancelList = new LinkedList();
      synchronized (this)
      {
        cancelButton.setEnabled(false);
        if (keyList != null)
        {
          cancelList.addAll(keyList);
        }
      }

      SessionData.getSessionData(sessionName).getQuerySequencer().cancelQueries(cancelList);
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

}
