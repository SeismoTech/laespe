package org.seismotech.laespe;

import java.io.IOException;
import java.net.URL;

import org.seismotech.ground.lang.XClass;
import org.seismotech.ground.io.XStream;

public class SpecializingClassLoader extends ClassLoader {

  private final ClassSet toLoad;

  public SpecializingClassLoader(final ClassSet toLoad) {
    super(toLoad.classLoader());
    this.toLoad = toLoad;
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve)
  throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      Class<?> klass = findLoadedClass(name);
      if (klass == null) {
        klass = toLoad.contains(name) ? loadCopy(name)
          : super.loadClass(name, false);
      }
      if (resolve) resolveClass(klass);
      //System.err.println(klass.getName() + "@" + klass.hashCode());
      return klass;
    }
  }

  private Class<?> loadCopy(String name)
  throws ClassNotFoundException {
    //System.err.println("Loading specialization for " + name);
    final URL code = getResource(XClass.classResourceName(name));
    if (code == null) throw new ClassNotFoundException(
      "Resource for class " + name + " not found");
    final byte[] bytecode;
    try {
      bytecode = XStream.read(code);
    } catch (IOException e) {
      throw new ClassNotFoundException("Resource for class " + name
          + " cannot be read: " + e.getMessage(), e);
    }
    final Class<?> klass = defineClass(name, bytecode, 0, bytecode.length);
    //System.err.println(klass.getName() + "@" + klass.hashCode());
    return klass;
  }
}
