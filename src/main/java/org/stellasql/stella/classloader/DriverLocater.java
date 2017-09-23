package org.stellasql.stella.classloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Driver;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DriverLocater
{
  private CustomClassLoader ccl = null;
  private List pathFileList = null;
  private DriverLocaterListener listener = null;

  public DriverLocater(List pathFileList, DriverLocaterListener listener)
  {
    this.pathFileList  = pathFileList;
    ccl = new CustomClassLoader(this.getClass().getClassLoader(), pathFileList);
    this.listener = listener;
  }

  public List getDriverClassNames() throws IOException
  {
    List driverList = new LinkedList();

    for (Iterator it = pathFileList.iterator(); it.hasNext();)
    {
      File pathFile = (File)it.next();
      if (pathFile.isFile())
        getDriverClassNamesFromJar(driverList, pathFile);
      else
        getDriverClassNamesFromPath(driverList, pathFile);
    }

    return driverList;
  }

  private void buildFileList(List list, File file, String currentPath, boolean topLevel)
  {
    if (file.isFile())
    {
      list.add(currentPath + file.getName());
    }
    else if (file.isDirectory())
    {
      String path = "";
      if (!topLevel)
        path = currentPath + file.getName() + "/";
      File[] files = file.listFiles();
      for (int index = 0; index < files.length; index++)
        buildFileList(list, files[index], path, false);
    }
  }

  private void getDriverClassNamesFromPath(List driverList, File pathFile) throws IOException
  {
    List fileList = new LinkedList();
    buildFileList(fileList, pathFile, "", true);
    listener.filesToProcess(fileList.size());
    int count = 0;
    for (Iterator it = fileList.iterator(); it.hasNext();)
    {
      String fileName = (String)it.next();

      if (fileName.endsWith(".class"))
      {
        String className = fileName.substring(0, fileName.indexOf(".class"));
        if (className.indexOf("/") >= 0)
          className = className.replaceAll("/", ".");

        checkClass(className, driverList);
      }

      listener.filesProcessed(++count);
    }
  }

  private void getDriverClassNamesFromJar(List driverList, File pathFile) throws IOException
  {
    JarFile jarFile = new JarFile(pathFile);
    listener.filesToProcess(jarFile.size());
    int count = 0;
    Enumeration enumer = jarFile.entries();
    while (enumer.hasMoreElements())
    {
      JarEntry entry = (JarEntry)enumer.nextElement();
      if (entry.getName().endsWith(".class"))
      {
        String entryName = entry.getName();
        String className = entryName.substring(0, entryName.indexOf(".class"));
        if (className.indexOf("/") >= 0)
          className = className.replaceAll("/", ".");

        checkClass(className, driverList);
      }
      listener.filesProcessed(++count);
    }
  }

  private void checkClass(String className, List driverList)
  {
    try
    {
      Class classInstance = ccl.loadClass(className);
      if (!classInstance.isInterface() && isDriver(classInstance)
          && hasPublicEmptyConstructor(classInstance))
      {
        driverList.add(className);
      }
    }
    catch (Exception e)
    {
    }
    catch (Error e)
    {
    }
  }

  private boolean hasPublicEmptyConstructor(Class classInstance)
  {
    if (classInstance.getConstructors().length == 0)
      return false;

    for (int index = 0; index < classInstance.getConstructors().length; index++)
    {
      Constructor constructor = classInstance.getConstructors()[index];
      if (constructor.getParameterTypes().length == 0)
        return true;
    }

    return false;
  }

  private boolean isDriver(Class classInstance)
  {
    for (int index = 0; index < classInstance.getInterfaces().length; index++)
    {
      if (Driver.class.equals(classInstance.getInterfaces()[index]))
        return true;
      else if (isDriver(classInstance.getInterfaces()[index]))
        return true;
    }

    return false;
  }

}
