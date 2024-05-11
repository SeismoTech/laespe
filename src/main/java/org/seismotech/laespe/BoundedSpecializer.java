package org.seismotech.laespe;

/**
 * A specializer for all the classes belonging to a hierarchy.
 * A call to {@link #specialized} with a class belonging to the hierarchy
 * will produce a newly loaded class, recursively for any dependency on the
 * hierarchy.
 *
 * <p>Observe that each call to {@link #specialized} produces a new class,
 * no mater that we are requesting the same class more than once.
 * This is intended behaviour, because there are use cases that need multiple
 * version of the same class.
 * But this should not be abused, otherwise the performance will be bad:
 * if you want to use a class for multiple instantiations,
 * or if you want to use a class instance for multiple executions,
 * please cache it instead of requesting each time a new specialization
 * to this class.
 */
public class BoundedSpecializer {

  private final ClassSet toSpecialize;

  public BoundedSpecializer(Class<?>... root) {
    this(new HierarchyClassSet(root));
  }

  public BoundedSpecializer(ClassSet toSpecialize) {
    this.toSpecialize = toSpecialize;
  }

  public Class<?> specialized(Class<?> klass)
  throws ClassNotFoundException {
    return toSpecialize.contains(klass) ? reload(klass.getName())
      : unmanagedClassError(klass.getName());
  }

  public Class<?> specialized(String classname)
  throws ClassNotFoundException {
    return toSpecialize.contains(classname) ? reload(classname)
      : unmanagedClassError(classname);
  }

  private Class<?> reload(String classname)
  throws ClassNotFoundException {
    return new SpecializingClassLoader(toSpecialize).loadClass(classname);
  }

  private <T> T unmanagedClassError(String classname)
  throws ClassNotFoundException {
    throw new ClassNotFoundException("Class `" + classname
        + "` doesn't belong to the hierarchy this specializer manages");
  }
}
