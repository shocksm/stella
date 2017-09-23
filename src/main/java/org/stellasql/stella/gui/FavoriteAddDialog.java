package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.QueryFavoriteItem;
import org.stellasql.stella.gui.custom.MessageDialog;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.SqlTextAdditions;
import org.stellasql.stella.gui.util.StellaClipBoard;
import org.stellasql.stella.gui.util.StellaImages;
import org.stellasql.stella.gui.util.SyntaxHighlighter;

public class FavoriteAddDialog extends StellaDialog implements SelectionListener
{
  protected Composite composite = null;
  private Button allButton = null;
  protected StyledText statementText = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private SyntaxHighlighter syntaxHighlighter;
  private Combo aliasCombo = null;
  private Text descriptionText = null;
  private QueryFavoriteItem qfiNew = null;
  private boolean edit = false;
  private String origAliasName = "";
  private String origDescription = "";

  public FavoriteAddDialog(Shell parent, boolean edit)
  {
    super(parent, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
    getShell().setImage(StellaImages.getInstance().getFavoritesNewImage());

    this.edit = edit;
    if (this.edit)
      setText("Edit Favorite Query");
    else
      setText("Add Favorite Query");
    Composite composite = createComposite(1);

    this.composite = new Composite(composite, SWT.NONE);
    this.composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    gridLayout.marginHeight = 1;
    gridLayout.marginWidth = 1;
    this.composite.setLayout(gridLayout);

    // alias combo
    Label label = new Label(this.composite, SWT.RIGHT);
    label.setText("C&onnection:");
    aliasCombo = new Combo(this.composite, SWT.SINGLE | SWT.READ_ONLY);
    aliasCombo.setVisibleItemCount(25);
    aliasCombo.addSelectionListener(this);

    allButton = new Button(this.composite, SWT.CHECK);
    allButton.setText("Available for all connections");
    allButton.addSelectionListener(this);

    label = new Label(this.composite, SWT.RIGHT);
    label.setText("&Description:");
    descriptionText = new Text(this.composite, SWT.BORDER);
    GridData gd = new GridData();
    gd.horizontalSpan = 2;
    gd.horizontalAlignment = SWT.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.widthHint = 100; // without this the control can cause the whole composite to expand out of the window when a new driver is selected
    descriptionText.setLayoutData(gd);

    // sql text
    statementText = new StyledText(this.composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    statementText.setIndent(2);
    SqlTextAdditions sqlTextAdditions = new SqlTextAdditions(statementText, "");
    syntaxHighlighter = sqlTextAdditions.getSyntaxHighlighter();

    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.heightHint = 100;
    gd.minimumHeight = 100;
    gd.widthHint = 500;
    gd.horizontalSpan = 3;
    statementText.setLayoutData(gd);

    //add dispose if using this statementBg = new Color(getShell().getDisplay(), 220, 220, 220);
    //statementText.setBackground(statementBg);

    okBtn = createButton(0);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    setFonts(ApplicationData.getInstance().getGeneralFont());

    String selectedName = "";
    if (Stella.getInstance().getSelectedSessionData() != null)
      selectedName = Stella.getInstance().getSelectedSessionData().getAlias().getName();
    int count = ApplicationData.getInstance().getAliasCount();
    for (int index = 0; index < count; index++)
    {
      AliasVO aliasVO = ApplicationData.getInstance().getAlias(index);
      aliasCombo.add(aliasVO.getName());
      if (aliasVO.getName().equals(selectedName))
      {
        aliasCombo.select(index);
        String querySeperator = ApplicationData.getInstance().getAlias(selectedName).getQuerySeperator();
        syntaxHighlighter.querySeparatorChanged(querySeperator);
      }
    }
    if (aliasCombo.getSelectionIndex() < 0
        && aliasCombo.getItemCount() > 0)
    {
      aliasCombo.select(0);
    }
  }

  public void setAliasName(String aliasName)
  {
    origAliasName = aliasName;
    if (aliasName.length() == 0)
    {
      allButton.setSelection(true);
      aliasCombo.setEnabled(false);
    }
    else
    {
      for (int index = 0; index < aliasCombo.getItemCount(); index++)
      {
        if (aliasCombo.getItem(index).equals(aliasName))
        {
          aliasCombo.select(index);
          break;
        }
      }
    }
  }

  public void setDescription(String description)
  {
    origDescription = description;
    descriptionText.setText(description);
  }

  public void setQueryText(String query)
  {
    statementText.setText(query);
  }

  public void open()
  {
    openInternal(-1, -1);
  }

  public QueryFavoriteItem openAndWait()
  {
    open();
    Display display = getShell().getDisplay();
    while (!getShell().isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }

    return qfiNew;
  }

  public boolean getValue()
  {
    return true;
  }

  private void okPressed()
  {
    boolean valid = true;

    if (descriptionText.getText().trim().length() == 0)
    {
      valid = false;
      MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
      messageDlg.setText("Description required");
      messageDlg.setMessage("Description can not be blank");
      messageDlg.open();
      descriptionText.setFocus();
    }

    if (valid)
    {
      String aliasName = "";
      if (!allButton.getSelection())
        aliasName = aliasCombo.getText();
      QueryFavoriteItem qfi = new QueryFavoriteItem(aliasName, descriptionText.getText(), statementText.getText());
      if (ApplicationData.getInstance().getQueryFavoriteExists(qfi))
      {
        if (!edit
            || edit && (!origAliasName.equals(qfi.getAliasName()) || !origDescription.equals(qfi.getDescription())))
        {
          valid = false;
          MessageDialog messageDlg = new MessageDialog(getShell(), SWT.OK);
          messageDlg.setText("Duplicate not allowed");
          messageDlg.setMessage("A favorite with the same connection and description already exists");
          messageDlg.open();
          descriptionText.setFocus();
        }
      }

      if (valid)
      {
        qfiNew = qfi;
        if (!edit)
        {
          ApplicationData.getInstance().addQueryFavorite(qfi);
          Stella.getInstance().saveFavorites();
        }
        getShell().dispose();
      }
    }
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == cancelBtn)
      getShell().dispose();
    else if (e.widget == okBtn)
    {
      okPressed();
    }
    else if (e.widget == aliasCombo)
    {
      String alias = aliasCombo.getText();
      String querySeperator = ApplicationData.getInstance().getAlias(alias).getQuerySeperator();
      syntaxHighlighter.querySeparatorChanged(querySeperator);
    }
    else if (e.widget == allButton)
    {
      aliasCombo.setEnabled(!allButton.getSelection());
    }
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {

  }

  public static void main(String[] args)
  {

    try
    {
      Display display = new Display();
      ApplicationData.getInstance().load();
      StellaClipBoard.init(display);
      Stella.init(display);
      Stella.getInstance().open();
      FavoriteAddDialog hd = new FavoriteAddDialog(Stella.getInstance().getShell(), false);
      hd.setQueryText("select *\nfrom tablename");
      hd.open();
      Shell shell = Stella.getInstance().getShell();
      while (!shell.isDisposed())
      {
        if (!display.readAndDispatch())
          display.sleep();
      }
      display.dispose();
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

}

