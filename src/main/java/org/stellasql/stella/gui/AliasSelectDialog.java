package org.stellasql.stella.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.stellasql.stella.AliasVO;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.StellaDialog;
import org.stellasql.stella.gui.util.StellaClipBoard;

public class AliasSelectDialog extends StellaDialog implements SelectionListener
{
  private List list = null;
  private Button cancelBtn = null;
  private Button okBtn = null;
  private AliasVO aliasVO = null;
  private static String lastSelected = null;

  public final static int OPEN = 1;
  public final static int EDIT = 2;
  public final static int DELETE = 3;
  public final static int COPY = 4;


  public AliasSelectDialog(Shell parent, int type)
  {
    super(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    setText("Select connection");

    Composite composite = createComposite(1, 10, 10);

    String msg = "Select the connection to ";
    if (type == OPEN)
      msg += "open";
    else if (type == EDIT)
      msg += "edit";
    else if (type == DELETE)
      msg += "delete";
    else if (type == COPY)
      msg += "copy";

    Label label = new Label(composite, SWT.NONE);
    label.setText(msg);

    list = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
    list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    list.addSelectionListener(this);


    //---------------------------------------------------------------------------------

    okBtn = createButton(20);
    okBtn.setText("&OK");
    okBtn.addSelectionListener(this);

    cancelBtn = createButton(0);
    cancelBtn.setText("&Cancel");
    cancelBtn.addSelectionListener(this);

    if (lastSelected == null)
    {
      lastSelected = ApplicationData.getInstance().getSelectedAlias();
    }

    boolean selected = false;
    int count = ApplicationData.getInstance().getAliasCount();
    for (int index = 0; index < count; index++)
    {
      AliasVO aliasVO = ApplicationData.getInstance().getAlias(index);
      list.add(aliasVO.getName());
      if (lastSelected != null && aliasVO.getName().equals(lastSelected))
      {
        list.select(index);
        selected = true;
      }
    }
    if (list.getItemCount() > 0 && !selected)
      list.select(0);

    setFonts(ApplicationData.getInstance().getGeneralFont());
  }

  public AliasVO open(int x, int y)
  {
    Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    if (size.x > 800)
      size.x = 800;
    if (size.y > 500)
      size.y = 500;

    super.openInternal(x, y, size.x, size.y);

    while (!getShell().isDisposed())
    {
      if (!getShell().getDisplay().readAndDispatch())
        getShell().getDisplay().sleep();
    }

    return aliasVO;
  }

  private void okPressed()
  {
    String name = list.getItem(list.getSelectionIndex());
    lastSelected = name;
    aliasVO = ApplicationData.getInstance().getAlias(name);

    ApplicationData.getInstance().setSelectedAlias(name);

    getShell().dispose();
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    okPressed();
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
      aliasVO = null;
      getShell().dispose();
    }
  }

  public static void main(String[] args)
  {

    Display display = new Display();
    try
    {
      ApplicationData.getInstance().load();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    StellaClipBoard.init(display);

    Shell shell = new Shell(display);
    shell.setText("Test");


    shell.setSize(500, 400);
    shell.open();

    AliasSelectDialog asd = new AliasSelectDialog(shell, OPEN);
    asd.open(-1, -1);

    //while (!shell.isDisposed())
    //{
      //if (!display.readAndDispatch())
        //display.sleep();
    //}
    display.dispose();
  }

}
