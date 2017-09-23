package org.stellasql.stella.gui.util;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class StellaImages
{
  private static StellaImages jqtImages = null;
  private Image maximize = null;
  private Image restore = null;
  private Image arrow = null;
  private Image settings = null;
  private Cursor tabDragCursor = null;
  private Image edit;
  private Image editDis;
  private Image copy;
  private Image copyDis;
  private Image newImage;
  private Image delete;
  private Image deleteDis;
  private Image connect;
  private Image connectDis;
  private Image disconnect;
  private Image disconnectDis;
  private Image run;
  private Image appSmall;
  private Image appBig;
  private Image appMedium;
  private Image splash;
  private Image refresh;
  private Image insert;
  private Image update;
  private Image tableInfo;
  private Image tableView;
  private Image active;
  private Image inactive;
  private Image manager;
  private Image website;
  private Image table;
  private Image database;
  private Image folder;
  private Image folderNew;
  private Image column;
  private Image folderOpen;
  private Image restoreDrivers;
  private Image plus;
  private Image minus;
  private Image insertText;
  private Image tableDelete;
  private Image history;
  private Image favorites;
  private Image favoritesNew;

  private StellaImages()
  {
  }

  public static synchronized StellaImages getInstance()
  {
    if (jqtImages == null)
      jqtImages = new StellaImages();

    return jqtImages;
  }

  public Image getAppSmallImage()
  {
    if (appSmall == null)
    {
      appSmall = ImageLoader.loadImage("images/stella-16.png");
    }
    return appSmall;
  }

  public Image getAppMediumImage()
  {
    if (appMedium == null)
    {
      appMedium = ImageLoader.loadImage("images/stella-32.png");
    }
    return appMedium;
  }

  public Image getAppBigImage()
  {
    if (appBig == null)
    {
      appBig = ImageLoader.loadImage("images/stella-64.png");
    }
    return appBig;
  }

  public Image getRefreshImage()
  {
    if (refresh == null)
    {
      refresh = ImageLoader.loadImage("images/refresh.png");
    }
    return refresh;
  }

  public Image getConnectImage()
  {
    if (connect == null)
    {
      connect = ImageLoader.loadImage("images/connect3.png");
    }
    return connect;
  }

  public Image getConnectDisImage()
  {
    if (connectDis == null)
    {
      connectDis = ImageLoader.loadImage("images/connect3_dis.png");
    }
    return connectDis;
  }

  public Image getDisconnectImage()
  {
    if (disconnect == null)
    {
      disconnect = ImageLoader.loadImage("images/disconnect.png");
    }

    return disconnect;
  }

  public Image getDisconnectDisImage()
  {
    if (disconnectDis == null)
    {
      disconnectDis = ImageLoader.loadImage("images/disconnect_dis.png");
    }

    return disconnectDis;
  }

  public Image getEditImage()
  {
    if (edit == null)
    {
      edit = ImageLoader.loadImage("images/edit.png");
    }
    return edit;
  }

  public Image getEditDisImage()
  {
    if (editDis == null)
    {
      editDis = ImageLoader.loadImage("images/edit_dis.png");
    }
    return editDis;
  }

  public Image getCopyImage()
  {
    if (copy == null)
    {
      copy = ImageLoader.loadImage("images/copy.png");
    }
    return copy;
  }

  public Image getCopyDisImage()
  {
    if (copyDis == null)
    {
      copyDis = ImageLoader.loadImage("images/copy_dis.png");
    }
    return copyDis;
  }

  public Image getNewImage()
  {
    if (newImage == null)
    {
      newImage = ImageLoader.loadImage("images/new.png");
    }
    return newImage;
  }

  public Image getDeleteDisImage()
  {
    if (deleteDis == null)
    {
      deleteDis = ImageLoader.loadImage("images/delete_dis.png");
    }
    return deleteDis;
  }

  public Image getDeleteImage()
  {
    if (delete == null)
    {
      delete = ImageLoader.loadImage("images/delete.png");
    }
    return delete;
  }

  public Image getSettingsImage()
  {
    if (settings == null)
    {
      settings = ImageLoader.loadImage("images/settings2.png");
    }
    return settings;
  }

  public Image getRunImage()
  {
    if (run == null)
    {
      run = ImageLoader.loadImage("images/run.png");
    }
    return run;
  }

  public Image getTabArrowImage()
  {
    if (arrow == null)
    {
      arrow = ImageLoader.loadImage("images/tabarrow.png");
    }

    return arrow;
  }

  public Cursor getTabDragCursor()
  {
    if (tabDragCursor == null)
    {
      tabDragCursor = new Cursor(Display.getCurrent(), ImageLoader.loadImageData("images/dragtabcursor.gif"), 0, 0);
    }
    return tabDragCursor;
  }

  public Image getInsertImage()
  {
    if (insert == null)
    {
      insert = ImageLoader.loadImage("images/insert.png");
    }
    return insert;
  }

  public Image getUpdateImage()
  {
    if (update == null)
    {
      update = ImageLoader.loadImage("images/update.png");
    }
    return update;
  }

  public Image getTableInfoImage()
  {
    if (tableInfo == null)
    {
      tableInfo = ImageLoader.loadImage("images/tableinfo.png");
    }
    return tableInfo;
  }

  public Image getTableViewImage()
  {
    if (tableView == null)
    {
      tableView = ImageLoader.loadImage("images/tableview.png");
    }
    return tableView;
  }

  public Image getMaximizeImage()
  {
    if (maximize == null)
    {
      maximize = ImageLoader.loadImage("images/maximize.png");
    }

    return maximize;
  }

  public Image getRestoreImage()
  {
    if (restore == null)
    {
      restore = ImageLoader.loadImage("images/restore.png");
    }

    return restore;
  }

  public Image getSplashImage()
  {
    if (splash == null)
    {
      splash = ImageLoader.loadImage("images/splash.png");
    }

    return splash;
  }

  public Image getActiveImage()
  {
    if (active == null)
    {
      active = ImageLoader.loadImage("images/check.png");
    }

    return active;
  }

  public Image getInactiveImage()
  {
    if (inactive == null)
    {
      inactive = ImageLoader.loadImage("images/circlex.png");
    }

    return inactive;
  }

  public Image getDriverManagerImage()
  {
    if (manager == null)
    {
      manager = ImageLoader.loadImage("images/drivermanager.png");
    }
    return manager;
  }

  public Image getWebsiteImage()
  {
    if (website == null)
    {
      website = ImageLoader.loadImage("images/globe.png");
    }
    return website;
  }

  public Image getTableImage()
  {
    if (table == null)
    {
      table = ImageLoader.loadImage("images/table.png");
    }
    return table;
  }

  public Image getDatabaseImage()
  {
    if (database == null)
    {
      database = ImageLoader.loadImage("images/database.png");
    }
    return database;
  }

  public Image getFolderImage()
  {
    if (folder == null)
    {
      folder = ImageLoader.loadImage("images/folder.gif");
    }
    return folder;
  }

  public Image getFolderNewImage()
  {
    if (folderNew == null)
    {
      folderNew = ImageLoader.loadImage("images/foldernew.png");
    }
    return folderNew;
  }

  public Image getFolderOpenImage()
  {
    if (folderOpen == null)
    {
      folderOpen = ImageLoader.loadImage("images/folderopen.gif");
    }
    return folderOpen;
  }

  public Image getColumnImage()
  {
    if (column == null)
    {
      column = ImageLoader.loadImage("images/column.png");
    }
    return column;
  }

  public Image getRestoreDriversImage()
  {
    if (restoreDrivers == null)
    {
      restoreDrivers = ImageLoader.loadImage("images/restoredrivers.png");
    }
    return restoreDrivers;
  }

  public Image getPlusImage()
  {
    if (plus == null)
    {
      plus = ImageLoader.loadImage("images/plus.png");
    }
    return plus;
  }

  public Image getMinusImage()
  {
    if (minus == null)
    {
      minus = ImageLoader.loadImage("images/minus.png");
    }
    return minus;
  }

  public Image getInsertText()
  {
    if (insertText == null)
    {
      insertText = ImageLoader.loadImage("images/inserttext.png");
    }
    return insertText;
  }

  public Image getTableDeleteImage()
  {
    if (tableDelete == null)
    {
      tableDelete = ImageLoader.loadImage("images/tabledelete.png");
    }
    return tableDelete;
  }

  public Image getHistoryImage()
  {
    if (history == null)
    {
      history = ImageLoader.loadImage("images/history.png");
    }
    return history;
  }

  public Image getFavoritesImage()
  {
    if (favorites == null)
    {
      favorites = ImageLoader.loadImage("images/favorites.gif");
    }
    return favorites;
  }

  public Image getFavoritesNewImage()
  {
    if (favoritesNew == null)
    {
      favoritesNew = ImageLoader.loadImage("images/favoritesnew.png");
    }
    return favoritesNew;
  }

}
