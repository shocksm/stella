package org.stellasql.stella.gui.statement;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.gui.custom.DateTimeBox;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.custom.TimeBox;
import org.stellasql.stella.gui.custom.datebox.DateBox;
import org.stellasql.stella.gui.custom.datebox.DateSelectedListener;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.StyledTextContextMenu;
import org.stellasql.stella.gui.util.SyntaxHighlighter;
import org.stellasql.stella.session.SessionData;

public abstract class BaseInsertUpdateDialog extends StellaDialog implements SelectionListener, ControlListener, PaintListener, DisposeListener
{
  protected Composite composite = null;
  protected StyledText statementText = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  protected TableInfo tableInfo = null;
  protected Composite inner = null;
  protected ScrolledComposite sc = null;
  protected Composite columnsComposite = null;
  protected SessionData sessionData = null;
  protected LinkedList cwList = new LinkedList();
  protected Sash sash = null;
  private Color colorHeader = null;
  private Color colorOdd = null;
  private Color colorEven = null;
  private Color statementBg = null;

  public BaseInsertUpdateDialog(Shell parent, TableInfo tableInfo, SessionData sessionData)
  {
    super(parent, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE);
    this.sessionData = sessionData;
    this.tableInfo = tableInfo;

    getShell().addDisposeListener(this);
  }

  protected void init(boolean editable, boolean forceChecks, boolean hasWhere)
  {
    createControls(editable, forceChecks, hasWhere);

    setFonts(ApplicationData.getInstance().getGeneralFont());
    FontSetter.setAllControlFonts(columnsComposite, ApplicationData.getInstance().getGeneralFont());

    columnsComposite.setSize(columnsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    columnsComposite.pack();
    sc.setMinSize(columnsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    layoutControls();
  }

  protected void createControls(boolean editable, boolean forceChecks, boolean hasWhere)
  {
    Composite composite = createComposite(1);

    this.composite = new Composite(composite, SWT.NONE);
    this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    this.composite.setLayout(new FormLayout());


    inner = new Composite(this.composite, SWT.NONE);
    inner.addControlListener(this);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    inner.setLayout(gridLayout);
    inner.setBackground(inner.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

    sc = new ScrolledComposite(inner, SWT.H_SCROLL | SWT.V_SCROLL);
    sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    sc.setExpandHorizontal(true);
    sc.setExpandVertical(true);


    createColumnWidgets(editable, forceChecks, hasWhere);

    sash = new Sash(this.composite, SWT.HORIZONTAL | SWT.SMOOTH);
    sash.addSelectionListener(this);

    statementText = new StyledText(this.composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    statementText.setEditable(false);
    statementText.setIndent(2);
    new SyntaxHighlighter(statementText, sessionData);
    new StyledTextContextMenu(statementText);

    statementBg = new Color(getShell().getDisplay(), 220, 220, 220);
    statementText.setBackground(statementBg);

    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);
  }

  private void createColumnWidgets(boolean editable, boolean forceChecks, boolean hasWhere)
  {
    columnsComposite = new Composite(sc, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = editable ? 6 : 5;
    gridLayout.verticalSpacing = 4;
    gridLayout.horizontalSpacing = 10;
    gridLayout.marginHeight = 2;
    columnsComposite.setLayout(gridLayout);
    columnsComposite.addPaintListener(this);

    sc.setContent(columnsComposite);

    colorHeader = new Color(getShell().getDisplay(), 192, 192, 192);
    colorEven = new Color(getShell().getDisplay(), 241, 241, 241);
    colorOdd = new Color(getShell().getDisplay(), 220, 220, 220);

    Label headerLabel = new Label(columnsComposite, SWT.NONE);
    headerLabel.setBackground(colorHeader);
    headerLabel.setText("Column Name");
    headerLabel = new Label(columnsComposite, SWT.NONE);
    headerLabel.setBackground(colorHeader);
    if (editable)
    {
      headerLabel.setText("Value");
      headerLabel = new Label(columnsComposite, SWT.NONE);
      headerLabel.setBackground(colorHeader);
    }
    headerLabel.setText("Data Type");
    headerLabel = new Label(columnsComposite, SWT.NONE);
    headerLabel.setBackground(colorHeader);
    headerLabel.setText("Size");
    headerLabel = new Label(columnsComposite, SWT.NONE);
    headerLabel.setBackground(colorHeader);
    headerLabel.setText("Nullable");
    headerLabel = new Label(columnsComposite, SWT.NONE);
    headerLabel.setBackground(colorHeader);
    headerLabel.setText("Default");

    boolean first = true;
    int count = 0;
    LinkedList tabList = new LinkedList();
    for (Iterator it = tableInfo.getColumns().iterator(); it.hasNext();)
    {
      ColumnInfo colInfo = (ColumnInfo)it.next();
      Color color = count++ % 2 == 0 ? colorEven : colorOdd;
      ColumnWidget cw = new ColumnWidget(columnsComposite, colInfo, editable, forceChecks, color, first, hasWhere);
      if (editable)
      {
        tabList.add(cw.getValueControl());
        if (first)
        {
          cw.setFocus();
          first = false;
        }
      }
    }

    if (editable)
    {
      Control[] taborder = new Control[tabList.size()];
      tabList.toArray(taborder);
      columnsComposite.setTabList(taborder);
    }
  }

  protected abstract void layoutControls();

  private void setScrollIncrements()
  {
    if (cwList.size() > 0)
    {
      int height = ((ColumnWidget)cwList.get(0)).getHeight();
      height += ((GridLayout)columnsComposite.getLayout()).verticalSpacing;
      sc.getVerticalBar().setIncrement(height);
      sc.getVerticalBar().setPageIncrement(inner.getClientArea().height);
      sc.getHorizontalBar().setIncrement(20);
      sc.getHorizontalBar().setPageIncrement(inner.getClientArea().width);
    }
  }

  protected abstract void buildStatement();

  public void open()
  {
    buildStatement();

    int height = 600;
    if (getShell().getDisplay().getBounds().height < height)
      height = getShell().getDisplay().getBounds().height;

    getShell().setSize(600, height);

    Point colHeight = columnsComposite.getSize();
    Point colPref = columnsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    if (sc.getClientArea().width < colPref.x) // add the height of the scroll bar if it will be shown
      colPref.y += sc.getHorizontalBar().getSize().y;

    if (colHeight.y > colPref.y)
    {
      height = height - (colHeight.y - colPref.y);
    }

    super.openInternal(-1, -1, 600, height);
    setScrollIncrements();
  }

  public void addToWhere(String text)
  {
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
      getShell().dispose();
    else if (e.widget == okBtn)
    {
      sessionData.addQueryText(statementText.getText());
      getShell().dispose();
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
    setScrollIncrements();
  }

  @Override
  public void paintControl(PaintEvent e)
  {
    GridLayout gridLayout = (GridLayout)columnsComposite.getLayout();
    int extraHeight = gridLayout.verticalSpacing / 2;

    int count = 0;
    for (Iterator it = cwList.iterator(); it.hasNext();)
    {
      Color color = count++ % 2 == 0 ? colorEven : colorOdd;
      ColumnWidget cw = (ColumnWidget)it.next();
      int y = cw.getTallControl().getLocation().y - extraHeight;
      int height = cw.getTallControl().getSize().y + extraHeight + extraHeight;

      if (count == 1)
      {
        e.gc.setBackground(colorHeader);
        e.gc.fillRectangle(0, 0, columnsComposite.getSize().x, y);
      }

      e.gc.setBackground(color);
      e.gc.fillRectangle(0, y, columnsComposite.getSize().x, height);
    }
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    colorHeader.dispose();
    colorOdd.dispose();
    colorEven.dispose();
    statementBg.dispose();
  }


  protected class ColumnWidget implements SelectionListener, ModifyListener, MouseListener, FocusListener, DateSelectedListener
  {
    private ColumnInfo columnInfo = null;
    private Button check = null;
    private Button literalCheck = null;
    private Label typeLabel = null;
    private Label sizeLabel = null;
    private Label nullableLabel = null;
    private Label defaultLabel = null;
    private Composite valueComposite = null;
    private Composite checkComposite = null;
    private Control valueControl = null;
    private ToolItem pasteButton = null;

    private Text valueText = null;
    private DateTimeBox dtb = null;
    private DateBox db = null;
    private TimeBox tb = null;
    private boolean first = false;

    public ColumnWidget(Composite composite, ColumnInfo colInfo, boolean editable, boolean forceChecks, Color color, boolean first, boolean hasWhere)
    {
      columnInfo = colInfo;
      this.first  = first;

      checkComposite = new Composite(composite, SWT.NONE);
      GridLayout gl = new GridLayout();
      gl.numColumns = hasWhere ? 3 : 2;
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      gl.horizontalSpacing = 3;
      checkComposite.setLayout(gl);
      checkComposite.setBackground(color);

      if (editable)
      {
        check = new Button(checkComposite, SWT.CHECK);
        check.addSelectionListener(this);
        check.setBackground(color);
        check.addMouseListener(this);
        if (forceChecks && !colInfo.getNullable() && colInfo.getDefault() == null)
        {
          check.setSelection(true);
          check.setEnabled(false);
          Label label = new Label(checkComposite, SWT.NONE);
          label.setText(colInfo.getColumnName());
          label.setBackground(color);
        }
        else
          check.setText(colInfo.getColumnName());
      }
      else
      {
        Label label = new Label(checkComposite, SWT.NONE);
        label.setText(colInfo.getColumnName());
        label.setBackground(color);
      }

      if (hasWhere)
      {
        ToolBar toolBar = new ToolBar(checkComposite, SWT.FLAT);
        toolBar.setBackground(color);
        pasteButton = new ToolItem(toolBar, SWT.PUSH);
        pasteButton.setToolTipText("Insert column name into the WHERE text");
        pasteButton.setImage(StellaImages.getInstance().getInsertText());
        pasteButton.addSelectionListener(this);
      }

      if (editable)
      {
        valueComposite = new Composite(composite, SWT.NONE);
        gl = new GridLayout();
        gl.numColumns = 2;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        valueComposite.setLayout(gl);
        valueComposite.setBackground(color);


        valueText = new Text(valueComposite, SWT.BORDER );
        valueText.addModifyListener(this);
        GridData gridData = new GridData();
        gridData.widthHint = 100;
        valueText.setLayoutData(gridData);
        valueText.addFocusListener(this);

        valueControl = valueComposite;

        LinkedList tabList = new LinkedList();
        tabList.add(valueText);
        if (DataTypeUtil.isTimestampType(columnInfo.getDataType()))
        {
          valueText.setVisible(false);
          gridData = (GridData)valueText.getLayoutData();
          gridData.exclude = true;

          dtb = new DateTimeBox(valueComposite);
          dtb.setDateTime(Calendar.getInstance());
          dtb.setBackground(color);
          dtb.addFocusListener(this);
          dtb.addDateSelectedListener(this);
          gridData = new GridData();
          dtb.setLayoutData(gridData);

          tabList.add(dtb);
        }
        else if (DataTypeUtil.isDateType(columnInfo.getDataType()))
        {
          valueText.setVisible(false);
          gridData = (GridData)valueText.getLayoutData();
          gridData.exclude = true;

          db = new DateBox(valueComposite);
          db.setDate(Calendar.getInstance());
          db.addFocusListener(this);
          db.addDateSelectedListener(this);
          gridData = new GridData();
          db.setLayoutData(gridData);

          tabList.add(db);
        }
        else if (DataTypeUtil.isTimeType(columnInfo.getDataType()))
        {
          valueText.setVisible(false);
          gridData = (GridData)valueText.getLayoutData();
          gridData.exclude = true;

          tb = new TimeBox(valueComposite);
          tb.setTime(Calendar.getInstance());
          tb.addFocusListener(this);
          tb.addDateSelectedListener(this);
          gridData = new GridData();
          tb.setLayoutData(gridData);

          tabList.add(tb);
        }

        Control[] taborder = new Control[tabList.size()];
        tabList.toArray(taborder);
        valueComposite.setTabList(taborder);

        if (isLiteralType(columnInfo.getDataType()))
        {
          literalCheck = new Button(valueComposite, SWT.CHECK);
          literalCheck.addSelectionListener(this);
          literalCheck.setBackground(color);
          literalCheck.addSelectionListener(this);
          literalCheck.setText("Literal");
          literalCheck.setSelection(true);
          literalCheck.addMouseListener(this);
        }
      }

      typeLabel = new Label(composite, SWT.NONE);
      typeLabel.setText(colInfo.getTypeName());
      typeLabel.setBackground(color);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.CENTER;
      typeLabel.setLayoutData(gridData);

      sizeLabel = new Label(composite, SWT.NONE);
      sizeLabel.setText("" + colInfo.getColumnSize());
      sizeLabel.setBackground(color);
      gridData = new GridData();
      gridData.horizontalAlignment = SWT.END;
      sizeLabel.setLayoutData(gridData);

      nullableLabel = new Label(composite, SWT.NONE);
      nullableLabel.setText("" + colInfo.getNullable());
      nullableLabel.setBackground(color);
      gridData = new GridData();
      gridData.horizontalAlignment = SWT.CENTER;
      nullableLabel.setLayoutData(gridData);

      defaultLabel = new Label(composite, SWT.NONE);
      defaultLabel.setBackground(color);
      if (colInfo.getDefault() != null)
        defaultLabel.setText(colInfo.getDefault());

      cwList.add(this);
    }

    public Control getTallControl()
    {
      if (valueComposite == null || checkComposite.getBounds().height > valueComposite.getBounds().height)
      {
        if (checkComposite.getBounds().height > typeLabel.getBounds().height)
          return checkComposite;
        else
          return typeLabel;
      }
      else if (valueComposite.getBounds().height > typeLabel.getBounds().height)
      {
        return valueComposite;
      }
      else
        return typeLabel;
    }

    public void setFocus()
    {
      if (valueText != null && valueText.getVisible())
        valueText.setFocus();
      else if (dtb != null)
        dtb.setFocus();
      else if (db != null)
        db.setFocus();
      else if (tb != null)
        tb.setFocus();
    }

    public Control getValueControl()
    {
      return valueControl;
    }

    public int getHeight()
    {
      int height = 0;
      if (valueComposite != null)
        height = valueComposite.getBounds().height;
      height = height < checkComposite.getBounds().height ? checkComposite.getBounds().height : height;
      height = height < typeLabel.getBounds().height ? typeLabel.getBounds().height : height;

      return height;
    }

    public void setValue(Object obj)
    {
      if (obj == null)
        return;

      if (DataTypeUtil.isTimestampType(columnInfo.getDataType()))
      {
        if (obj instanceof Date)
        {
          Calendar cal = Calendar.getInstance();
          cal.setTime((Date)obj);
          dtb.setDateTime(cal);
        }
      }
      else if (DataTypeUtil.isDateType(columnInfo.getDataType()))
      {
        if (obj instanceof Date)
        {
          Calendar cal = Calendar.getInstance();
          cal.setTime((Date)obj);
          db.setDate(cal);
        }
      }
      else if (DataTypeUtil.isTimeType(columnInfo.getDataType())
          && literalCheck.getSelection())
      {
        if (obj instanceof Date)
        {
          Calendar cal = Calendar.getInstance();
          cal.setTime((Date)obj);
          tb.setTime(cal);
        }
      }
      else
        valueText.setText(obj.toString());
    }


    public String getValue()
    {
      if (DataTypeUtil.isCharacterType(columnInfo.getDataType())
          && literalCheck.getSelection())
      {
        return "'" + valueText.getText() + "'";
      }
      else if (DataTypeUtil.isTimestampType(columnInfo.getDataType())
          && literalCheck.getSelection())
      {
        Calendar cal = dtb.getDateTime();
        if (cal == null)
          return "";
        return DataTypeUtil.formatAsTimestamp(cal);
      }
      else if (DataTypeUtil.isDateType(columnInfo.getDataType())
          && literalCheck.getSelection())
      {
        Calendar cal = db.getDate();
        if (cal == null)
          return "";
        return DataTypeUtil.formatAsDate(cal);
      }
      else if (DataTypeUtil.isTimeType(columnInfo.getDataType())
          && literalCheck.getSelection())
      {
        Calendar cal = tb.getTime();
        if (cal == null)
          return "";
        return DataTypeUtil.formatAsTime(cal);
      }

      return valueText.getText().trim();
    }

    public boolean getChecked()
    {
      return check.getSelection();
    }

    public ColumnInfo getColumnInfo()
    {
      return columnInfo;
    }

    private void checked()
    {
      buildStatement();
    }

    @Override
    public void widgetSelected(SelectionEvent e)
    {
      if (e.widget == check)
        checked();
      else if (e.widget == literalCheck)
        toggleLiteral(literalCheck.getSelection());
      else if (e.widget == pasteButton)
        addToWhere(columnInfo.getColumnName());
    }
    @Override
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }

    @Override
    public void modifyText(ModifyEvent e)
    {
      if (!check.getSelection())
        check.setSelection(true);
      buildStatement();
    }

    @Override
    public void dateSelected(Calendar calendar)
    {
      if (!check.getSelection())
        check.setSelection(true);
      buildStatement();
    }

    @Override
    public void mouseDoubleClick(MouseEvent e)
    {
    }
    @Override
    public void mouseDown(MouseEvent e)
    {
    }

    @Override
    public void mouseUp(MouseEvent e)
    {
      valueComposite.setFocus();
    }

    @Override
    public void focusGained(FocusEvent e)
    {
      Point loc = valueComposite.getLocation();
      Point size = valueComposite.getSize();

      Point origin = sc.getOrigin();
      Rectangle area = sc.getClientArea();

      if (loc.y > origin.y)
      {
        if ((loc.y + size.y) - origin.y > area.height)
        {
          int dif = ((loc.y + size.y) - origin.y) - area.height;
          origin.y = origin.y + dif;
          sc.setOrigin(origin);
        }
      }
      else if (loc.y < origin.y)
      {
        if (first)
          sc.setOrigin(new Point(0, 0));
        else
        {
          int dif = origin.y - loc.y;
          origin.y = origin.y - dif;
          sc.setOrigin(origin);
        }
      }
    }
    @Override
    public void focusLost(FocusEvent e)
    {
    }

    private void toggleLiteral(boolean shown)
    {
      Control control = null;

      if (DataTypeUtil.isTimestampType(columnInfo.getDataType()))
        control = dtb;
      else if (DataTypeUtil.isDateType(columnInfo.getDataType()))
        control = db;
      else if (DataTypeUtil.isTimeType(columnInfo.getDataType()))
        control = tb;

      if (control != null)
      {
        if (shown)
        {
          control.setVisible(true);
          valueText.setVisible(false);

          GridData gridData = (GridData)control.getLayoutData();
          gridData.exclude = false;
          gridData = (GridData)valueText.getLayoutData();
          gridData.exclude = true;
        }
        else
        {
          control.setVisible(false);
          valueText.setVisible(true);

          GridData gridData = (GridData)control.getLayoutData();
          gridData.exclude = true;
          gridData = (GridData)valueText.getLayoutData();
          gridData.exclude = false;
        }

        columnsComposite.layout();
        sc.setMinSize(columnsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      }

      if (check.getSelection())
        buildStatement();
    }
  }





  private boolean isLiteralType(int type)
  {
    if (DataTypeUtil.isCharacterType(type)
      || DataTypeUtil.isTimestampType(type)
      || DataTypeUtil.isDateType(type)
      || DataTypeUtil.isTimeType(type))
      return true;

    return false;
  }


}
