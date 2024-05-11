package org.seismotech.laespe;

/**
 * Represents a class set.
 * Probably an intensional set defined with some general criteria.
 */
public interface ClassSet {
  ClassLoader classLoader();
  boolean contains(Class<?> klass);
  boolean contains(String classname);
}
