package org.stellasql.stella.gui.custom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaImages;

public class StellaDialog extends Dialog
{
  private Shell shell = null;
  private Composite buttonComposite = null;
  private List buttonList = new LinkedList();

  public StellaDialog(Shell parent, int style)
  {
    super(parent);

    shell = new Shell(parent, style);
    GridLayout gridLayout = new GridLayout();
    shell.setLayout(gridLayout);
    shell.setImage(StellaImages.getInstance().getAppSmallImage());
  }

  public void setEnabled(boolean enabled)
  {
    for (int index = 0; index < shell.getChildren().length; index++)
    {
      shell.getChildren()[index].setEnabled(enabled);
    }
  }

  @Override
  public void setText(String text)
  {
    shell.setText(text);
  }

  @Override
  public String getText()
  {
    return shell.getText();
  }

  protected Shell getShell()
  {
    return shell;
  }

  protected void setFonts(FontContainer fc)
  {
    setFonts(shell, fc);
  }

  private void setFonts(Composite composite, FontContainer fc)
  {
    FontSetter.setFont(composite, ApplicationData.getInstance().getGeneralFont());
    Control[] controls = composite.getChildren();
    for (int i = 0; i < controls.length; i++)
    {
      if (controls[i] instanceof Composite)
        setFonts((Composite)controls[i], fc);
      else
        FontSetter.setFont(controls[i], ApplicationData.getInstance().getGeneralFont());
    }
  }

  protected Composite createComposite(int columns)
  {
    return createComposite(columns, 0, 0);
  }

  protected Composite createComposite(int columns, int marginHeight, int marginWidth)
  {
    Composite composite = new Composite(shell, SWT.NONE);
    GridLayout gridLayout  = new GridLayout();
    gridLayout.marginHeight = marginHeight;
    gridLayout.marginWidth = marginWidth;
    gridLayout.numColumns = columns;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    return composite;
  }

  protected Button createButton(int indent)
  {
    if (buttonComposite == null)
    {
      buttonComposite = new Composite(shell, SWT.NONE);
      GridLayout gridLayout = new GridLayout();
      gridLayout.marginHeight = 0;
      gridLayout.marginWidth = 0;
      gridLayout.marginTop = 10;
      gridLayout.marginBottom = 5;
      buttonComposite.setLayout(gridLayout);
      buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
    }

    GridLayout gridLayout = (GridLayout)buttonComposite.getLayout();
    gridLayout.numColumns = gridLayout.numColumns + 1;


    GridData gridData = new GridData();
    gridData = new GridData();
    gridData.horizontalAlignment = SWT.END;
    gridData.horizontalIndent = indent;

    if (buttonList.size() == 0)
      gridData.grabExcessHorizontalSpace = true;

    Button button = new Button(buttonComposite, SWT.PUSH);
    button.setLayoutData(gridData);

    buttonList.add(button);

    return button;
  }

  protected void openInternal(int x, int y)
  {
    openInternal(x, y, -1, -1);
  }

  protected void openInternal(int x, int y, boolean pack)
  {
    openInternal(x, y, -1, -1, pack);
  }

  protected void openInternal(int x, int y, int width, int height)
  {
    openInternal(x, y, width, height, true);
  }

  protected void openInternal(int x, int y, int width, int height, boolean pack)
  {
    setButtonWidths();
    if (pack)
      shell.pack();
    else
      shell.layout(true, true);

    if (width > 0)
      shell.setSize(width, height);

    Shell parent = getParent();
    if (x < 0)
      x = parent.getBounds().x + ((parent.getSize().x / 2) - (shell.getSize().x / 2));
    if (y < 0)
      y = parent.getBounds().y + ((parent.getSize().y / 2) - (shell.getSize().y / 2));

    if (shell.getSize().x + x > shell.getDisplay().getBounds().width)
      x = shell.getDisplay().getBounds().width - shell.getSize().x;
    if (shell.getSize().y + y > shell.getDisplay().getBounds().height)
      y = shell.getDisplay().getBounds().height - shell.getSize().y;
    if (x < 0)
      x = 0;
    if (y < 0)
      y = 0;

    shell.setLocation(x, y);
    shell.open();
  }

  private void setButtonWidths()
  {
    int max = 75;
    for (Iterator it = buttonList.iterator(); it.hasNext();)
    {
      Button button = (Button)it.next();
      Point pt = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      if (pt.x > max)
        max = pt.x;
    }

    for (Iterator it = buttonList.iterator(); it.hasNext();)
    {
      Button button = (Button)it.next();
      GridData gridData = (GridData)button.getLayoutData();
      gridData.widthHint = max;
    }
  }

}
