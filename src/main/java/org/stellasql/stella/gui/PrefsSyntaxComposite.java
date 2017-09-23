package org.stellasql.stella.gui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.SyntaxContainer;
import org.stellasql.stella.query.TokenScanner;

public class PrefsSyntaxComposite extends Composite implements SelectionListener, DisposeListener
{
  private CLabel colorLabel = null;
  private Button colorButton = null;
  private Button boldButton = null;
  private Button italicButton = null;
  private Button restoreButton = null;
  private List list = null;
  private Map tokenMap = new HashMap();
  private Map syntaxContainerMap = new HashMap();
  private boolean changed = false;

  public PrefsSyntaxComposite(Composite parent)
  {
    super(parent, SWT.NONE);

    this.addDisposeListener(this);

    GridLayout gridLayout  = new GridLayout();
    gridLayout.numColumns = 3;
    this.setLayout(gridLayout);


    list = new List(this, SWT.BORDER);
    list.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 4));
    list.addSelectionListener(this);

    colorLabel = new CLabel(this, SWT.NONE);
    colorLabel.setText("C&olor:");

    colorButton = new Button(this, SWT.PUSH);
    colorButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1));
    colorButton.addSelectionListener(this);

    boldButton = new Button(this, SWT.CHECK);
    boldButton.setText("&Bold");
    boldButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
    boldButton.addSelectionListener(this);

    italicButton = new Button(this, SWT.CHECK);
    italicButton.setText("&Italic");
    italicButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
    italicButton.addSelectionListener(this);

    restoreButton = new Button(this, SWT.PUSH);
    restoreButton.setText("&Restore Default");
    restoreButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
    restoreButton.addSelectionListener(this);


    String lineComment = "Line Comments";
    String blockComment = "Block Comments";
    String strings = "Strings";
    String numbers = "Numbers";
    String keywords = "Keywords";
    String operators = "Operators and brackets";
    String querySeparator = "Query Separator";

    tokenMap.put(lineComment, TokenScanner.LINECOMMENT);
    tokenMap.put(blockComment, TokenScanner.BLOCKCOMMENT);
    tokenMap.put(strings, TokenScanner.STRING);
    tokenMap.put(numbers, TokenScanner.NUMBER);
    tokenMap.put(keywords, TokenScanner.KEYWORD);
    tokenMap.put(operators, TokenScanner.OPERATOR);
    tokenMap.put(querySeparator, TokenScanner.SEPARATOR);

    syntaxContainerMap.put(lineComment, new SyntaxContainer(ApplicationData.getInstance().getSyntax(TokenScanner.LINECOMMENT)));
    syntaxContainerMap.put(blockComment, new SyntaxContainer(ApplicationData.getInstance().getSyntax(TokenScanner.BLOCKCOMMENT)));
    syntaxContainerMap.put(strings, new SyntaxContainer(ApplicationData.getInstance().getSyntax(TokenScanner.STRING)));
    syntaxContainerMap.put(numbers, new SyntaxContainer(ApplicationData.getInstance().getSyntax(TokenScanner.NUMBER)));
    syntaxContainerMap.put(keywords, new SyntaxContainer(ApplicationData.getInstance().getSyntax(TokenScanner.KEYWORD)));
    syntaxContainerMap.put(operators, new SyntaxContainer(ApplicationData.getInstance().getSyntax(TokenScanner.OPERATOR)));
    syntaxContainerMap.put(querySeparator, new SyntaxContainer(ApplicationData.getInstance().getSyntax(TokenScanner.SEPARATOR)));

    list.add(lineComment);
    list.add(blockComment);
    list.add(strings);
    list.add(numbers);
    list.add(keywords);
    list.add(operators);
    list.add(querySeparator);


    FontSetter.setAllControlFonts(this, ApplicationData.getInstance().getGeneralFont());
    setEnabled(false);
    setItemSelected();
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    colorButton.setEnabled(enabled);
    boldButton.setEnabled(enabled);
    italicButton.setEnabled(enabled);
    restoreButton.setEnabled(enabled);
  }

  private void setItemSelected()
  {
    String item = "";
    if (list.getSelectionCount() == 1)
      item = list.getSelection()[0];

    SyntaxContainer syntaxCon = (SyntaxContainer)syntaxContainerMap.get(item);
    Color color = null;
    if (syntaxCon == null)
    {
      color = this.getDisplay().getSystemColor(SWT.COLOR_BLACK);
      boldButton.setSelection(false);
      italicButton.setSelection(false);
    }
    else
    {
      color = syntaxCon.getColor();
      boldButton.setSelection(syntaxCon.getBold());
      italicButton.setSelection(syntaxCon.getItalic());
    }

    if (colorButton.getImage() != null)
      colorButton.getImage().dispose();

    int width = 30;
    int height = 15;
    Image image = new Image(this.getDisplay(), width, height);
    GC gc = new GC(image);
    gc.setBackground(color);
    gc.fillRectangle(0, 0, width-1, height-1);
    gc.drawRectangle(0, 0, width-1, height-1);
    gc.dispose();
    colorButton.setImage(image);
  }

  public void okPressed()
  {
    if (changed)
    {
      String[] items = list.getItems();
      for (int index = 0; index < items.length; index++)
      {
        Object key = tokenMap.get(items[index]);
        SyntaxContainer syntaxCon = (SyntaxContainer)syntaxContainerMap.get(items[index]);
        ApplicationData.getInstance().setSyntax(key, syntaxCon);
      }

      ApplicationData.getInstance().updateSyntax();
    }
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == colorButton)
    {
      SyntaxContainer syntaxCon = (SyntaxContainer)syntaxContainerMap.get(list.getSelection()[0]);
      ColorDialog cd = new ColorDialog(this.getShell());
      cd.setRGB(syntaxCon.getColor().getRGB());
      RGB rgb = cd.open();
      if (rgb != null)
      {
        changed = true;
        syntaxCon.dispose();
        syntaxCon.setColor(new Color(this.getDisplay(), rgb));
        setItemSelected();
      }
    }
    else if (e.widget == boldButton)
    {
      SyntaxContainer syntaxCon = (SyntaxContainer)syntaxContainerMap.get(list.getSelection()[0]);
      syntaxCon.setBold(boldButton.getSelection());
      changed = true;
    }
    else if (e.widget == italicButton)
    {
      SyntaxContainer syntaxCon = (SyntaxContainer)syntaxContainerMap.get(list.getSelection()[0]);
      syntaxCon.setItalic(italicButton.getSelection());
      changed = true;
    }
    else if (e.widget == list)
    {
      if (list.getSelectionCount() == 1)
      {
        setEnabled(true);
        setItemSelected();
      }
      else
      {
        setEnabled(false);
      }
    }
    else if (e.widget == restoreButton)
    {
      changed = true;
      Object key = tokenMap.get(list.getSelection()[0]);
      SyntaxContainer syntaxCon = (SyntaxContainer)syntaxContainerMap.get(list.getSelection()[0]);
      syntaxCon.dispose();
      syntaxContainerMap.put(list.getSelection()[0], new SyntaxContainer(key));
      setItemSelected();
    }
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    if (colorButton.getImage() != null)
      colorButton.getImage().dispose();
  }



}
