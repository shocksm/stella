package org.stellasql.stella.gui.util;

import org.eclipse.swt.widgets.Display;

public class WorkerRunnable
{
  private Display display = null;

  public void startTask()
  {
    display = Display.getCurrent();

    Thread thread = new Thread(new Runnable() {
      @Override
      public void run()
      {
        doTask();

        if (!display.isDisposed())
        {
          display.syncExec(new Runnable()
          {
            @Override
            public void run()
            {
              doUITask();
            }
          });
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  protected void doTask()
  {

  }

  protected void doUITask()
  {

  }

}
