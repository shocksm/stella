package org.stellasql.stella.gui;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.export.ExcelExportOptions;
import org.stellasql.stella.export.ExportOptions;
import org.stellasql.stella.export.HtmlExportOptions;
import org.stellasql.stella.export.TextExportOptions;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.session.SessionData;

public class ExportDialog extends StellaDialog implements SelectionListener
{
  private Composite composite = null;
  private Button okBtn = null;
  private Button cancelBtn = null;

  private Text fileText = null;
  private Button fileButton = null;

  private Combo formatCombo = null;

  private GeneralOptions generalOptions = null;
  private CSVOptions csvOptions = null;
  private HTMLOptions htmlOptions = null;
  private TextOptions textOptions = null;
  private ExcelOptions excelOptions = null;
  private BaseOptions selectedOptions = null;

  private ExportOptions exportOptions = null;
  private SessionData sessionData = null;

  private static final String FORMAT_CSV = "CSV";
  private static final String FORMAT_HTML = "HTML";
  private static final String FORMAT_TEXT = "Text";
  private static final String FORMAT_EXCEL = "Excel";

  public ExportDialog(Shell parent, SessionData sd)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    sessionData = sd;

    setText("Export Results");

    composite = createComposite(2, 5, 10);

    Label label = new Label(composite, SWT.RIGHT);
    label.setText("&Export Format:");
    label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

    formatCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
    formatCombo.add(FORMAT_CSV);
    formatCombo.add(FORMAT_HTML);
    formatCombo.add(FORMAT_TEXT);
    formatCombo.add(FORMAT_EXCEL);
    formatCombo.select(3);
    formatCombo.addSelectionListener(this);

    label = new Label(composite, SWT.RIGHT);
    label.setText("&File:");
    label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

    Composite fileComposite = new Composite(composite, SWT.NONE);
    GridLayout gl = new GridLayout();
    gl.numColumns = 2;
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    gl.horizontalSpacing = 0;
    fileComposite.setLayout(gl);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    fileComposite.setLayoutData(gridData);

    fileText = new Text(fileComposite, SWT.BORDER);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.widthHint = 350;
    fileText.setLayoutData(gridData);

    fileButton = new Button(fileComposite, SWT.PUSH);
    fileButton.setText("...");
    fileButton.addSelectionListener(this);

    Composite options = new Composite(composite, SWT.NONE);
    gl = new GridLayout();
    gl.numColumns = 1;
    gl.marginHeight = 0;
    gl.marginWidth = 0;
    options.setLayout(gl);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    options.setLayoutData(gridData);

    generalOptions = new GeneralOptions(options);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    generalOptions.setLayoutData(gridData);

    csvOptions = new CSVOptions(options);
    gridData = new GridData();
    gridData.heightHint = 0;
    csvOptions.setLayoutData(gridData);

    selectedOptions = csvOptions;

    htmlOptions = new HTMLOptions(options);
    gridData = new GridData();
    gridData.heightHint = 0;
    gridData.exclude = true;
    htmlOptions.setLayoutData(gridData);
    htmlOptions.setVisible(false);

    textOptions = new TextOptions(options);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.exclude = true;
    textOptions.setLayoutData(gridData);
    textOptions.setVisible(false);

    excelOptions = new ExcelOptions(options);
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.exclude = true;
    excelOptions.setLayoutData(gridData);
    excelOptions.setVisible(false);


    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());

    if (sessionData.getExportFormat() != null)
    {
      for (int index = 0; index < formatCombo.getItemCount(); index++)
      {
        if (formatCombo.getItem(index).equals(sessionData.getExportFormat()))
        {
          formatCombo.select(index);
          break;
        }
      }
      fileText.setText(sessionData.getExportFile());
      generalOptions.setIncludeColumnNames(sessionData.getExportInlcudeColumnName());
      generalOptions.setIncludeSql(sessionData.getExportInlcudeSql());
      generalOptions.setDateFormat(sessionData.getExportDateFormat());
      generalOptions.setTimeFormat(sessionData.getExportTimeFormat());
      textOptions.setDelimiter(sessionData.getExportTextDelimiter());
      excelOptions.setSplitWorksheet(sessionData.getExportExcelSplitWorksheet());
      excelOptions.setSplitRowCount(sessionData.getExportExcelSplitRowCount());
    }
    layoutFormat();
  }

  public ExportOptions open()
  {
    Shell parent = getParent();

    int y = parent.getBounds().y + (parent.getSize().y / 4);

    super.openInternal(-1, y);

    while (!getShell().isDisposed())
    {
      if (!getShell().getDisplay().readAndDispatch())
        getShell().getDisplay().sleep();
    }

    return exportOptions;
  }

  private void okPressed()
  {
    if (fileText.getText().trim().length() == 0)
    {
      MessageDialog md = new MessageDialog(this.getShell(), SWT.OK);
      md.setText("Invalid File");
      md.setMessage("A file must be entered");
      md.open();
      fileText.setFocus();
      return;
    }

    File file = new File(fileText.getText());
    if (file.exists() && !file.isFile())
    {
      MessageDialog md = new MessageDialog(this.getShell(), SWT.OK);
      md.setText("Invalid File");
      md.setMessage("The file entered is not a valid file");
      md.open();
      fileText.setFocus();
      return;
    }

    boolean usePath = true;
    if (file.exists())
    {
      MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK | SWT.CANCEL);
      messageDlg.setText("File exists");
      messageDlg.setMessage("The file '" + file.toString() + "' already exists.\nOverwrite it?");
      if (messageDlg.open() == SWT.CANCEL)
        usePath = false;
    }

    if (usePath)
    {
      exportOptions = selectedOptions.getExportOptions();
      generalOptions.getExportOptions(exportOptions);
      exportOptions.setFile(file);

      sessionData.setExportFormat(formatCombo.getItem(formatCombo.getSelectionIndex()));
      sessionData.setExportFile(fileText.getText());
      sessionData.setExportInlcudeColumnName(exportOptions.getIncludeColumnNames());
      sessionData.setExportInlcudeSql(exportOptions.getIncludeSql());
      sessionData.setExportDateFormat(exportOptions.getDateFormat());
      sessionData.setExportTimeFormat(exportOptions.getTimeFormat());
      sessionData.setExportTextDelimiter(textOptions.getDelimiter());

      sessionData.setExportExcelSplitWorksheet(excelOptions.getSplitWorksheet());
      sessionData.setExportExcelSplitRowCount(excelOptions.getSplitRowCount());

      getShell().dispose();
    }
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == okBtn)
    {
      okPressed();
    }
    else if (e.widget == cancelBtn)
    {
      exportOptions = null;
      getShell().dispose();
    }
    else if (e.widget == fileButton)
    {
      FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
      if (fileText.getText().trim().length() > 0)
        dialog.setFileName(fileText.getText());

      dialog.setFilterExtensions(selectedOptions.getFilterExtendsions());
      dialog.setFilterNames(selectedOptions.getFilterNames());

      String path = dialog.open();
      if (path != null)
      {
        fileText.setText(path);
      }
    }
    else if (e.widget == formatCombo)
    {
      layoutFormat();
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  private void layoutFormat()
  {
    GridData gd = (GridData)selectedOptions.getLayoutData();
    gd.exclude = true;
    selectedOptions.setVisible(false);

    if (formatCombo.getItem(formatCombo.getSelectionIndex()).equals(FORMAT_CSV))
      selectedOptions = csvOptions;
    else if (formatCombo.getItem(formatCombo.getSelectionIndex()).equals(FORMAT_HTML))
      selectedOptions = htmlOptions;
    else if (formatCombo.getItem(formatCombo.getSelectionIndex()).equals(FORMAT_TEXT))
      selectedOptions = textOptions;
    else if (formatCombo.getItem(formatCombo.getSelectionIndex()).equals(FORMAT_EXCEL))
      selectedOptions = excelOptions;

    selectedOptions.setVisible(true);
    gd = (GridData)selectedOptions.getLayoutData();
    gd.exclude = false;

    composite.layout(true, true);
    getShell().pack();
  }


  private class GeneralOptions extends Composite implements SelectionListener
  {
    private Button includeNamesButton = null;
    private Button includeSqlButton = null;
    private Combo dateFormat = null;
    private Combo timeFormat = null;
    private Label dateExample = null;
    private Label timeExample = null;

    private static final String DF_1 = "MM/dd/yyyy";
    private static final String DF_2 = "MM.dd.yyyy";
    private static final String DF_3 = "MM-dd-yyyy";
    private static final String DF_4 = "dd/MM/yyyy";
    private static final String DF_5 = "dd.MM.yyyy";
    private static final String DF_6 = "dd-MM-yyyy";
    private static final String DF_7 = "yyyy/MM/dd";
    private static final String DF_8 = "yyyy-MM-dd";
    private static final String DF_9 = "yyyy.MM.dd";

    private static final String TF_1 = "HH:mm:ss";
    private static final String TF_2 = "hh:mm:ss a";
    private static final String TF_3 = "HH:mm:ss.SSS a";
    private static final String TF_4 = "hh:mm:ss.SSS a";
    private static final String TF_5 = "HH:mm";
    private static final String TF_6 = "hh:mm a";


    public GeneralOptions(Composite parent)
    {
      super(parent, SWT.NONE);
      GridLayout gl = new GridLayout();
      gl.numColumns = 1;
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      this.setLayout(gl);


      Group optionsGroup = new Group(this, SWT.NONE);
      optionsGroup.setText("Options");
      gl = new GridLayout();
      gl.numColumns = 3;
      gl.verticalSpacing = 10;
      optionsGroup.setLayout(gl);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      optionsGroup.setLayoutData(gridData);



      includeNamesButton = new Button(optionsGroup, SWT.CHECK);
      includeNamesButton.setText("&Include Column Names");
      includeNamesButton.setSelection(true);
      gridData = new GridData();
      gridData.horizontalSpan = 3;
      includeNamesButton.setLayoutData(gridData);

      includeSqlButton = new Button(optionsGroup, SWT.CHECK);
      includeSqlButton.setText("Include &SQL");
      includeSqlButton.setSelection(false);
      gridData = new GridData();
      gridData.horizontalSpan = 3;
      includeSqlButton.setLayoutData(gridData);

      Label label = new Label(optionsGroup, SWT.NONE);
      label.setText("&Date Format:");
      dateFormat = new Combo(optionsGroup, SWT.BORDER | SWT.READ_ONLY);
      dateFormat.addSelectionListener(this);
      dateFormat.add(DF_1);
      dateFormat.add(DF_2);
      dateFormat.add(DF_3);
      dateFormat.add(DF_4);
      dateFormat.add(DF_5);
      dateFormat.add(DF_6);
      dateFormat.add(DF_7);
      dateFormat.add(DF_8);
      dateFormat.add(DF_9);
      dateFormat.select(0);
      dateExample = new Label(optionsGroup, SWT.NONE);

      label = new Label(optionsGroup, SWT.NONE);
      label.setText("&Time Format:");
      timeFormat = new Combo(optionsGroup, SWT.BORDER | SWT.READ_ONLY);
      timeFormat.addSelectionListener(this);
      timeFormat.add(TF_1);
      timeFormat.add(TF_2);
      timeFormat.add(TF_3);
      timeFormat.add(TF_4);
      timeFormat.add(TF_5);
      timeFormat.add(TF_6);
      timeFormat.select(0);

      timeExample = new Label(optionsGroup, SWT.NONE);

      setDateExampleLabel();
      setTimeExampleLabel();
    }

    public void setIncludeColumnNames(boolean include)
    {
      includeNamesButton.setSelection(include);
    }

    public void setIncludeSql(boolean include)
    {
      includeSqlButton.setSelection(include);
    }

    public void setDateFormat(String format)
    {
      for (int index = 0; index < dateFormat.getItemCount(); index++)
      {
        if (dateFormat.getItem(index).equals(format))
        {
          dateFormat.select(index);
          break;
        }
      }
    }

    public void setTimeFormat(String format)
    {
      for (int index = 0; index < timeFormat.getItemCount(); index++)
      {
        if (timeFormat.getItem(index).equals(format))
        {
          timeFormat.select(index);
          break;
        }
      }
    }

    public void getExportOptions(ExportOptions exportOptions)
    {
      exportOptions.setDateFormat(getDateFormat());
      exportOptions.setTimeFormat(getTimeFormat());
      exportOptions.setIncludeColumnNames(includeNamesButton.getSelection());
      exportOptions.setIncludeSql(includeSqlButton.getSelection());
    }

    private String getDateFormat()
    {
      String item = dateFormat.getItem(dateFormat.getSelectionIndex());
      return item;
    }

    private void setDateExampleLabel()
    {
      DateFormat df = new SimpleDateFormat(getDateFormat());
      dateExample.setText("Example: " + df.format(new Date()));
      dateExample.pack();
    }

    private String getTimeFormat()
    {
      String item = timeFormat.getItem(timeFormat.getSelectionIndex());
      return item;
    }

    private void setTimeExampleLabel()
    {
      DateFormat df = new SimpleDateFormat(getTimeFormat());
      timeExample.setText("Example: " + df.format(new Date()));
      timeExample.pack();
    }


    @Override
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }
    @Override
    public void widgetSelected(SelectionEvent e)
    {
      if (e.widget == dateFormat)
      {
        setDateExampleLabel();
      }
      else if (e.widget == timeFormat)
      {
        setTimeExampleLabel();
      }

    }

  }

  private abstract class BaseOptions extends Composite
  {
    public BaseOptions(Composite parent)
    {
      super(parent, SWT.NONE);
    }

    public abstract ExportOptions getExportOptions();
    public abstract String[] getFilterExtendsions();
    public abstract String[] getFilterNames();

  }

  private class CSVOptions extends BaseOptions
  {

    public CSVOptions(Composite parent)
    {
      super(parent);
      setSize(0, 0);
    }

    @Override
    public String[] getFilterExtendsions()
    {
      return new String[]{"*.csv", "*"};
    }

    @Override
    public String[] getFilterNames()
    {
      return new String[]{"CSV files (*.csv)", "all files (*)"};
    }

    @Override
    public ExportOptions getExportOptions()
    {
      return new TextExportOptions(",");
    }

  }

  private class HTMLOptions extends BaseOptions
  {
    public HTMLOptions(Composite parent)
    {
      super(parent);
    }

    @Override
    public String[] getFilterExtendsions()
    {
      return new String[]{"*.html", "*.htm", "*"};
    }

    @Override
    public String[] getFilterNames()
    {
      return new String[]{"HTML files (*.html)", "HTML files (*.htm)", "all files (*)"};
    }

    @Override
    public ExportOptions getExportOptions()
    {
      return new HtmlExportOptions();
    }

  }

  private class TextOptions extends BaseOptions
  {
    private Combo delimiterCombo = null;

    private static final String DELIMITER_TAB = "TAB";
    private static final String DELIMITER_COMA = ",";
    private static final String DELIMITER_SEMICOLON = ";";
    private static final String DELIMITER_PIPE = "|";


    public TextOptions(Composite parent)
    {
      super(parent);

      GridLayout gl = new GridLayout();
      gl.numColumns = 1;
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      this.setLayout(gl);

      Group optionsGroup = new Group(this, SWT.NONE);
      optionsGroup.setText("Text Options");
      gl = new GridLayout();
      gl.numColumns = 2;
      gl.verticalSpacing = 10;
      optionsGroup.setLayout(gl);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      optionsGroup.setLayoutData(gridData);

      Label label = new Label(optionsGroup, SWT.NONE);
      label.setText("&Column Delimiter");

      delimiterCombo = new Combo(optionsGroup, SWT.BORDER | SWT.READ_ONLY);
      delimiterCombo.add(DELIMITER_TAB);
      delimiterCombo.add(DELIMITER_COMA);
      delimiterCombo.add(DELIMITER_SEMICOLON);
      delimiterCombo.add(DELIMITER_PIPE);
      delimiterCombo.select(0);
    }

    public String getDelimiter()
    {
      return  delimiterCombo.getItem(delimiterCombo.getSelectionIndex());
    }

    public void setDelimiter(String delimiter)
    {
      for (int index = 0; index < delimiterCombo.getItemCount(); index++)
      {
        if (delimiterCombo.getItem(index).equals(delimiter))
        {
          delimiterCombo.select(index);
          break;
        }
      }
    }

    @Override
    public ExportOptions getExportOptions()
    {
      String item = delimiterCombo.getItem(delimiterCombo.getSelectionIndex());
      String delim = item;
      if (item.equals(DELIMITER_TAB))
        delim = "\t";
      else if (item.equals(DELIMITER_COMA))
        delim = ",";
      else if (item.equals(DELIMITER_SEMICOLON))
        delim = ";";
      else if (item.equals(DELIMITER_PIPE))
        delim = "|";

      return new TextExportOptions(delim);
    }

    @Override
    public String[] getFilterExtendsions()
    {
      return new String[]{"*.txt", "*"};
    }

    @Override
    public String[] getFilterNames()
    {
      return new String[]{"Text files (*.txt)", "all files (*)"};
    }

  }

  private class ExcelOptions extends BaseOptions implements SelectionListener, VerifyListener
  {
    private Button splitWorksheetButton = null;
    private Button excel64kRadioButton = null;
    private Button excel1024kRadioButton = null;
    private Button customRadioButton = null;
    private Text customText = null;

    public ExcelOptions(Composite parent)
    {
      super(parent);

      GridLayout gl = new GridLayout();
      gl.numColumns = 1;
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      this.setLayout(gl);

      Group optionsGroup = new Group(this, SWT.NONE);
      optionsGroup.setText("Excel Options");
      gl = new GridLayout();
      gl.numColumns = 2;
      gl.verticalSpacing = 10;
      optionsGroup.setLayout(gl);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      optionsGroup.setLayoutData(gridData);


      splitWorksheetButton = new Button(optionsGroup, SWT.CHECK);
      splitWorksheetButton.setText("&Split rows across multiple worksheets");
      splitWorksheetButton.setSelection(true);
      gridData = new GridData();
      gridData.horizontalSpan = 2;
      splitWorksheetButton.setLayoutData(gridData);
      splitWorksheetButton.addSelectionListener(this);

      excel64kRadioButton = new Button(optionsGroup, SWT.RADIO);
      excel64kRadioButton.setText("Split at 65,535 rows (Excel 2000-2003 limitation)");
      excel64kRadioButton.setSelection(true);
      gridData = new GridData();
      gridData.horizontalSpan = 2;
      gridData.horizontalIndent = 20;
      excel64kRadioButton.setLayoutData(gridData);
      excel64kRadioButton.addSelectionListener(this);

      excel1024kRadioButton = new Button(optionsGroup, SWT.RADIO);
      excel1024kRadioButton.setText("Split at 1,048,576 rows (Excel XP and later limitation)");
      gridData = new GridData();
      gridData.horizontalSpan = 2;
      gridData.horizontalIndent = 20;
      excel1024kRadioButton.setLayoutData(gridData);
      excel1024kRadioButton.addSelectionListener(this);


      customRadioButton = new Button(optionsGroup, SWT.RADIO);
      customRadioButton.setText("Split at");
      gridData = new GridData();
      gridData.horizontalIndent = 20;
      customRadioButton.setLayoutData(gridData);
      customRadioButton.addSelectionListener(this);

      customText = new Text(optionsGroup, SWT.BORDER);
      gridData = new GridData();
      gridData.widthHint = 100;
      customText.setLayoutData(gridData);
      customText.setEnabled(false);
      customText.addVerifyListener(this);
    }

    public boolean getSplitWorksheet()
    {
      return splitWorksheetButton.getSelection();
    }

    public void setSplitWorksheet(boolean split)
    {
      splitWorksheetButton.setSelection(split);
      doSelections();
    }

    public int getSplitRowCount()
    {
      int value = 65535;
      if (excel64kRadioButton.getSelection())
        value = 65535;
      else if (excel1024kRadioButton.getSelection())
        value = 1048576;
      else if (customRadioButton.getSelection())
      {
        try
        {
          int cvalue = Integer.parseInt(customText.getText());
          if (cvalue > 0)
            value = cvalue;
        }
        catch (NumberFormatException e)
        {
        }
      }

      return value;
    }

    public void setSplitRowCount(int split)
    {
      excel64kRadioButton.setSelection(false);
      excel1024kRadioButton.setSelection(false);
      customRadioButton.setSelection(false);
      if (split == 65535)
        excel64kRadioButton.setSelection(true);
      else if (split == 1048576)
        excel1024kRadioButton.setSelection(true);
      else
      {
        customRadioButton.setSelection(true);
        customText.setText("" + split);
      }

      doSelections();
    }

    @Override
    public String[] getFilterExtendsions()
    {
      return new String[]{"*.xls", "*"};
    }

    @Override
    public String[] getFilterNames()
    {
      return new String[]{"Excel files (*.xls)", "all files (*)"};
    }

    @Override
    public ExportOptions getExportOptions()
    {
      if (getSplitWorksheet())
        return new ExcelExportOptions(getSplitRowCount());
      else
        return new ExcelExportOptions();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }

    @Override
    public void widgetSelected(SelectionEvent e)
    {
      doSelections();
    }

    private void doSelections()
    {
      boolean enabled = splitWorksheetButton.getSelection();
      excel64kRadioButton.setEnabled(enabled);
      excel1024kRadioButton.setEnabled(enabled);
      customRadioButton.setEnabled(enabled);
      if (customRadioButton.getSelection() && customRadioButton.getEnabled())
        customText.setEnabled(true);
      else
        customText.setEnabled(false);
    }

    @Override
    public void verifyText(VerifyEvent e)
    {
      if (e.widget == customText)
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
    }

  }

}
