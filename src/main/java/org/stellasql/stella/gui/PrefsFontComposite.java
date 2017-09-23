package org.stellasql.stella.gui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.gui.custom.FontContainer;
import org.stellasql.stella.gui.util.FontSetter;

public class PrefsFontComposite extends Composite implements SelectionListener, DisposeListener
{
  private static final String SYSTEBUTTON = "SYSTEBUTTON";
  private static final String CHANGE_BUTTON = "CHANGE_BUTTON";
  private static final String FONTCHANGER = "FONTCHANGER";

  private List fontChangers = new LinkedList();
  private FontChanger generalFC = null;
  private FontChanger queryTextFC = null;
  private FontChanger resultsFC = null;
  private FontChanger treeFC = null;

  public PrefsFontComposite(Composite parent)
  {
    super(parent, SWT.NONE);
    this.addDisposeListener(this);

    GridLayout gridLayout  = new GridLayout();
    //gridLayout.marginHeight = 0;
    //gridLayout.marginWidth = 0;
    gridLayout.numColumns = 4;
    this.setLayout(gridLayout);

    generalFC  = new FontChanger(this, ApplicationData.getInstance().getGeneralFont(), "&General Font", false, false);
    queryTextFC = new FontChanger(this, ApplicationData.getInstance().getQueryTextFont(), "&Query Text Font", true, ApplicationData.getInstance().isQueryTextFontSet());
    resultsFC = new FontChanger(this, ApplicationData.getInstance().getResultsFont(), "&Results Table Font", true, ApplicationData.getInstance().isResultsFontSet());
    treeFC = new FontChanger(this, ApplicationData.getInstance().getTreeFont(), "&Database Table Tree Font", true, ApplicationData.getInstance().isTreeFontSet());
  }

  public void okPressed()
  {
    if (generalFC.getChanged())
      ApplicationData.getInstance().setGeneralFont(wrapFont(generalFC.getFont(), generalFC.getColor()));
    if (queryTextFC.getChanged())
    {
      ApplicationData.getInstance().setQueryTextFont(wrapFont(queryTextFC.getFont(), queryTextFC.getColor()));
      ApplicationData.getInstance().setQueryTextFontSet(queryTextFC.getChecked());
    }
    if (resultsFC.getChanged())
    {
      ApplicationData.getInstance().setResultsFont(wrapFont(resultsFC.getFont(), resultsFC.getColor()));
      ApplicationData.getInstance().setResultsFontSet(resultsFC.getChecked());
    }
    if (treeFC.getChanged())
    {
      ApplicationData.getInstance().setTreeFont(wrapFont(treeFC.getFont(), treeFC.getColor()));
      ApplicationData.getInstance().setTreeFontSet(treeFC.getChecked());
    }
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget instanceof Button)
    {
      Button btn = (Button)e.widget;
      if (btn.getData() == SYSTEBUTTON)
      {
        FontChanger fc = (FontChanger)btn.getData(FONTCHANGER);
        fc.setFont(null, null);
        this.layout(true, true);
        this.pack();
      }
      else if (btn.getData() == CHANGE_BUTTON)
      {
        FontChanger fc = (FontChanger)btn.getData(FONTCHANGER);

        FontDialog fdialog = new FontDialog(getShell());
        fdialog.setFontList(fc.getCurrentFontData());
        if (fc.getColor() != null)
          fdialog.setRGB(fc.getColor().getRGB());
        FontData fd = fdialog.open();
        if (fd != null)
        {
          fc.setFont(new Font(getShell().getDisplay(), fdialog.getFontList()),
              new Color(getShell().getDisplay(), fdialog.getRGB()));
          this.layout(true, true);
          this.pack();

          /*
          int width = this.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
          width += 20; // padding for the margin around the shell's composite
          width += getShell().getBorderWidth();
          width += ((GridLayout)getShell().getLayout()).marginWidth * 2;
          if (getShell().getSize().x < width)
          {
            getShell().setSize(width, getShell().getSize().y);
          }
          */
        }
      }
    }
  }
  private FontContainer wrapFont(Font font, Color color)
  {
    Font fontNew = null;
    Color colorNew = null;

    if (font != null)
      fontNew = new Font(getShell().getDisplay(), font.getFontData());
    if (color != null)
      colorNew = new Color(getShell().getDisplay(), color.getRGB());

    if (fontNew == null && colorNew == null)
      return null;
    else
      return new FontContainer(fontNew, colorNew);
  }
  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  private class FontChanger implements SelectionListener
  {
    private Label fontLabel = null;
    private Button checkButton = null;
    private Button systemButton = null;
    private Button changeButton = null;
    private Font font = null;
    private Color color = null;
    private boolean changed = false;

    public FontChanger(Composite parent, FontContainer fcon, String text, boolean createCheckBox, boolean checked)
    {
      fontChangers.add(this);

      if (!createCheckBox)
      {
        Label textLabel = new Label(parent, SWT.NONE);
        FontSetter.setFont(textLabel, ApplicationData.getInstance().getGeneralFont());
        textLabel.setText(text);
        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        textLabel.setLayoutData(gridData);
      }
      else
      {
        checkButton = new Button(parent, SWT.CHECK);
        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        checkButton.setLayoutData(gridData);
        checkButton.addSelectionListener(this);
        checkButton.setText(text);
        FontSetter.setFont(checkButton, ApplicationData.getInstance().getGeneralFont());
      }


      systemButton = new Button(parent, SWT.PUSH);
      FontSetter.setFont(systemButton, ApplicationData.getInstance().getGeneralFont());
      systemButton.setData(SYSTEBUTTON);
      systemButton.setData(FONTCHANGER, this);
      systemButton.setText("System");
      systemButton.addSelectionListener(PrefsFontComposite.this);
      GridData gridData = new GridData();
      gridData.verticalAlignment = SWT.CENTER;
      systemButton.setLayoutData(gridData);

      changeButton = new Button(parent, SWT.PUSH);
      FontSetter.setFont(changeButton, ApplicationData.getInstance().getGeneralFont());
      changeButton.setData(CHANGE_BUTTON);
      changeButton.setData(FONTCHANGER, this);
      changeButton.setText("Change");
      changeButton.addSelectionListener(PrefsFontComposite.this);
      gridData = new GridData();
      gridData.verticalAlignment = SWT.CENTER;
      changeButton.setLayoutData(gridData);

      Font font = null;
      Color color = null;

      if (fcon != null)
      {
        font = fcon.getFont();
        color = fcon.getColor();
      }

      if (createCheckBox && !checked)
      {
        font = null;
        color = null;
      }

      fontLabel = new Label(parent, SWT.NONE);
      fontLabel.setFont(font);
      if (font == null)
      {
        fontLabel.setText("System");
      }
      else
      {
        fontLabel.setText(getFontString(font));
        fontLabel.setForeground(color);
      }

      if (color != null)
        this.color = new Color(color.getDevice(), color.getRGB());

      if (checkButton != null)
      {
        checkButton.setSelection(checked);
        systemButton.setEnabled(checked);
        changeButton.setEnabled(checked);

        if (!checked)
          fontLabel.setVisible(false);
      }
    }

    public Color getColor()
    {
      return color;
    }

    public String getFontString(Font font)
    {
      StringBuffer sbuf = new StringBuffer();

      FontData[] fdArray = font.getFontData();
      for (int index = 0; index < fdArray.length; index++)
      {
        sbuf.append(fdArray[index].getName());
        sbuf.append(" ");
        sbuf.append(fdArray[index].getHeight());
        sbuf.append(" ");
        if (fdArray[index].getStyle() == 0)
          sbuf.append("Normal").append(" ");
        if ((fdArray[index].getStyle() & SWT.BOLD) > 0)
          sbuf.append("Bold").append(" ");
        if ((fdArray[index].getStyle() & SWT.ITALIC) > 0)
          sbuf.append("Italic").append(" ");
      }

      return sbuf.toString();
    }

    public boolean getChecked()
    {
      return checkButton.getSelection();
    }

    public Font getFont()
    {
      return font;
    }

    public boolean getChanged()
    {
      return changed;
    }

    public void dispose()
    {
      if (font != null)
        font.dispose();
      if (color != null)
        color.dispose();
    }

    public void setFont(Font font, Color color)
    {
      if (this.font != null)
        this.font.dispose();
      if (this.color != null)
        this.color.dispose();

      this.font = font;
      this.color = color;

      changed = true;

      fontLabel.setFont(this.font);
      fontLabel.setForeground(this.color);
      if (font == null)
        fontLabel.setText("System");
      else
        fontLabel.setText(getFontString(font));
      fontLabel.pack();
    }

    public FontData[] getCurrentFontData()
    {
      return fontLabel.getFont().getFontData();
    }

    @Override
    public void widgetSelected(SelectionEvent e)
    {
      changed = true;
      systemButton.setEnabled(checkButton.getSelection());
      changeButton.setEnabled(checkButton.getSelection());
      fontLabel.setVisible(checkButton.getSelection());
    }
    @Override
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }
  }



  @Override
  public void widgetDisposed(DisposeEvent e)
  {
    for (Iterator it = fontChangers.iterator(); it.hasNext();)
    {
      FontChanger fc = (FontChanger)it.next();
      fc.dispose();
    }
  }


}
