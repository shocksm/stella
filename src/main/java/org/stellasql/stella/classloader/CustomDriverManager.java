package org.stellasql.stella.classloader;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class CustomDriverManager
{
  public synchronized Connection getConnection(ClassLoader cl, String driverClassName, String url, String user, String password) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
  {
    java.util.Properties info = new java.util.Properties();

    if (user != null)
    {
      info.put("user", user);
    }
    if (password != null)
    {
      info.put("password", password);
    }

    return (getConnection(cl, driverClassName, url, info));
  }


  private synchronized Connection getConnection(ClassLoader cl, String driverClassName, String url, Properties info)
      throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException
  {
    Class driverClass = cl.loadClass(driverClassName);
    Driver driver = (Driver)driverClass.newInstance();


    Connection con = driver.connect(url, info);

    return con;
  }

}
