package org.stellasql.stella.classloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CustomClassLoader extends ClassLoader
{
  private final static Logger logger = LogManager.getLogger("CustomClassLoader");

  private List pathList = new ArrayList();

  public CustomClassLoader(ClassLoader parent, List pathFileList)
  {
    super(parent);
    pathList.addAll(pathFileList);
  }

  public void addPath(File file)
  {
    pathList.add(file);
  }

  @Override
  public Class loadClass(String name) throws ClassNotFoundException
  {
    logger.debug("CustomClassLoader: loadClass " + name);

    Class loadedClass = findLoadedClass(name);

    if (loadedClass == null)
    {

      if (getParent() != null)
      {
        try
        {
          loadedClass = getParent().loadClass(name);
          logger.debug("CustomClassLoader: found by parent loader: " + name);
        }
        catch (ClassNotFoundException e)
        {
          logger.debug("CustomClassLoader: NOT found by parent loader: " + name);
        }
      }

      if (loadedClass == null)
      {
        // If still not found try to find it with this class loader
        loadedClass = findClass(name);
        if (loadedClass != null)
          logger.debug("CustomClassLoader: loadClass found : " + name);
        else
          logger.debug("CustomClassLoader: loadClass NOT found : " + name);

        if (loadedClass == null)
          throw new ClassNotFoundException("Class could not be found: " + name);
      }
    }

    return loadedClass;
  }

  @Override
  public synchronized Class findClass(String name) throws ClassNotFoundException
  {
    logger.debug("CustomClassLoader: findClass " + name);
    Class foundClass = null;
    String classFileName = name.replace('.', '/').concat(".class");

    try
    {
      for (Iterator it = pathList.iterator(); it.hasNext();)
      {
        File file = (File)it.next();
        byte[] classBytes = null;
        if (file.isFile())
        {
          // try to load from jar
          classBytes = getClassFromJar(file, classFileName);
        }
        else if (file.isDirectory())
        {
          // try to load from directory
          File check = new File(file, classFileName);
          if (check.exists())
          {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buffer[] = new byte[8192];
            FileInputStream is = new FileInputStream(check);
            int bytesRead = 0;
            while ((bytesRead = is.read(buffer, 0, buffer.length)) != -1)
            {
              baos.write(buffer, 0, bytesRead);
            }
            is.close();
            baos.close();
            classBytes = baos.toByteArray();
          }
        }

        if (classBytes != null)
        {
          foundClass = defineClass(name, classBytes, 0, classBytes.length);
          break;
        }
      }
    }
    catch (Exception e)
    {
      throw new ClassNotFoundException(name, e);
    }

    if (foundClass != null)
      logger.debug("CustomClassLoader: findClass found : " + name);
    else if (foundClass == null)
    {
      logger.debug("CustomClassLoader: findClass NOT found : " + name);
      throw new ClassNotFoundException(name);
    }

    return foundClass;
  }

  private byte[] getClassFromJar(File file, String path) throws IOException
  {
    byte[] classBytes = null;
    JarFile jarFile = new JarFile(file);
    JarEntry jarEntry = jarFile.getJarEntry(path);
    if (jarEntry != null)
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte buffer[] = new byte[8192];
      InputStream is = jarFile.getInputStream(jarEntry);
      int bytesRead = 0;
      while ((bytesRead = is.read(buffer, 0, buffer.length)) != -1)
      {
        baos.write(buffer, 0, bytesRead);
      }
      is.close();
      baos.close();
      classBytes = baos.toByteArray();
    }
    jarFile.close();

    return classBytes;
  }

  @Override
  protected synchronized URL findResource(String name)
  {
    URL resourceURL = null;

    logger.debug("CustomClassLoader: findResource " + name);

    try
    {
      for (Iterator it = pathList.iterator(); it.hasNext();)
      {
        File file = (File)it.next();
        if (file.isFile())
        {
          // try to load from jar
          if (isResourceInJar(file, name))
          {
            resourceURL = new URL("jar:file:/"
                + file.getCanonicalPath()
                + "!/" + name);
          }

        }
        else if (file.isDirectory())
        {
          // try to load from directory
          File check = new File(file, name);
          if (check.exists())
          {
            resourceURL = new URL("file:/" + check.getCanonicalPath());
          }
        }

        if (resourceURL != null)
          break;
      }
    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }

    if (resourceURL != null)
      logger.debug("CustomClassLoader: findResource found : " + name);
    else
      logger.debug("CustomClassLoader: findResource NOT found : " + name);

    return resourceURL;
  }

  @Override
  protected synchronized Enumeration findResources(String name)
  {
    logger.debug("CustomClassLoader: findResources " + name);

    List resourceList = new ArrayList();

    try
    {
      for (Iterator it = pathList.iterator(); it.hasNext();)
      {
        File file = (File)it.next();
        URL resourceURL = null;
        if (file.isFile())
        {
          // try to load from jar
          if (isResourceInJar(file, name))
          {
            resourceURL = new URL("jar:file:/"
                + file.getCanonicalPath()
                + "!/" + name);
          }

        }
        else if (file.isDirectory())
        {
          // try to load from directory
          File check = new File(file, name);
          if (check.exists())
          {
            resourceURL = new URL("file:/" + check.getCanonicalPath());
          }
        }

        if (resourceURL != null)
          resourceList.add(resourceURL);
      }

    }
    catch (Exception e)
    {
      logger.error(e.getMessage(), e);
    }

    return new URLEnumeration(resourceList);
  }

  private boolean isResourceInJar(File file, String path) throws IOException
  {
    boolean found = false;

    JarFile jarFile = new JarFile(file);
    JarEntry jarEntry = jarFile.getJarEntry(path);
    if (jarEntry != null)
    {
      found = true;
    }
    jarFile.close();

    return found;
  }

}
