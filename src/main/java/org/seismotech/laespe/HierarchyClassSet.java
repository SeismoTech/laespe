package org.seismotech.laespe;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a hierarchy starting from a set of roots.
 * Contains all strict subtypes of the roots.
 */
public class HierarchyClassSet implements ClassSet {

  private final ClassLoader clref;
  private final List<Class<?>> roots;

  public HierarchyClassSet(Class<?>... roots) {
    this((ClassLoader) null, roots);
  }

  public HierarchyClassSet(ClassLoader clref, Class<?>... roots) {
    this.clref = clref != null ? clref : roots[0].getClassLoader();
    this.roots = new ArrayList<>(roots.length);
    for (final Class<?> root: roots) this.roots.add(root);
  }

  @Override
  public ClassLoader classLoader() {return clref;}

  @Override
  public boolean contains(Class<?> klass) {
    for (final Class<?> root: roots) {
      if (root != klass && root.isAssignableFrom(klass)) return true;
    }
    return false;
  }

  @Override
  public boolean contains(String classname) {
    final Class<?> klass;
    try {
      klass = clref.loadClass(classname);
    } catch (ClassNotFoundException e) {
      return false;
    }
    return contains(klass);
  }
}
