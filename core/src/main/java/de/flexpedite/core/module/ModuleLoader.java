package de.flexpedite.core.module;

import com.google.common.collect.Lists;
import de.flexpedite.core.CoreModule;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public final class ModuleLoader {
  private final String directory;
  private final List<RegisteredModule> modules = Lists.newArrayList();

  public void loadModules(CoreModule coreModule) throws Exception {
    List<File> files = jarsInDirectory();
    for (File moduleFile : files) {
      loadModule(moduleFile, coreModule);
    }
  }

  private List<File> jarsInDirectory() {
    return Arrays.stream(new File(directory).listFiles())
      .filter(file -> !file.isDirectory())
      .filter(file -> file.getName().endsWith(".jar"))
      .collect(Collectors.toList());
  }

  public void reloadModule(String name, CoreModule coreModule) throws Exception {
    Optional<RegisteredModule> moduleOptional = modules.stream()
      .filter(module -> module.name().equals(name))
      .findFirst();
    if (moduleOptional.isEmpty()) {
      return;
    }
    RegisteredModule registeredModule = moduleOptional.get();
    unloadModule(registeredModule);
    loadModule(registeredModule.file(), coreModule);
  }

  private void loadModule(File file, CoreModule coreModule) throws Exception {
    JarFile jarFile = new JarFile(file);
    URLClassLoader classLoader = new URLClassLoader(new URL[] {file.toURI().toURL()},
      this.getClass().getClassLoader());
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      Optional<Class<?>> optionalModuleClass = findModuleClass(entry, classLoader);
      if (optionalModuleClass.isEmpty()) {
        continue;
      }
      Module module = createModule(optionalModuleClass.get(), coreModule);
      Annotation annotation = findModuleAnnotation(optionalModuleClass.get()).get();
      modules.add(RegisteredModule.create(module, findAnnotationField(annotation, "name"),
        findAnnotationField(annotation, "version"), file));
    }
  }

  private Module createModule(
    Class<?> moduleClass, CoreModule coreModule
  ) throws Exception {
    return (Module) moduleClass.getConstructor(CoreModule.class)
      .newInstance(coreModule);
  }

  private Optional<Class<?>> findModuleClass(
    JarEntry entry, URLClassLoader classLoader
  ) throws Exception {
    String entryName = entry.getName();
    if (entry.isDirectory() || !entryName.endsWith(".class")) {
      return Optional.empty();
    }
    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
    Class<?> entryClass = Class.forName(className, true, classLoader);
    if (!isDescendedOfModule(entryClass)) {
      return Optional.empty();
    }
    if (findModuleAnnotation(entryClass).isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(entryClass);
  }

  private boolean isDescendedOfModule(Class<?> suspect) {
    if (suspect.getSuperclass() == null) {
      return false;
    }
      return suspect.getSuperclass().equals(Module.class);
  }

  private String findAnnotationField(Annotation annotation, String fieldName) throws Exception {
    Method method = Arrays.stream(annotation.annotationType().getDeclaredMethods())
      .filter(declaredMethod -> declaredMethod.getName().equals(fieldName))
      .findFirst().get();
    return (String) method.invoke(annotation, (Object[])null);
  }

  private Optional<Annotation> findModuleAnnotation(Class<?> suspect) {
    return Arrays.stream(suspect.getAnnotations())
      .filter(annotation -> annotation.annotationType()
        .equals(ModuleDescription.class)).findFirst();
  }

  public void unloadModule(String name) {
    Optional<RegisteredModule> moduleOptional = modules.stream()
      .filter(module -> module.name().equals(name))
      .findFirst();
    if (moduleOptional.isEmpty()) {
      return;
    }
    RegisteredModule registeredModule = moduleOptional.get();
    registeredModule.module().disable();
    unloadModule(registeredModule);
  }

  public void unloadModule(RegisteredModule registeredModule) {
    registeredModule.module().disable();
    modules.remove(registeredModule);
  }

  public Module findModule(String name) {
    return modules.stream()
      .filter(module -> module.name().equals(name))
      .map(RegisteredModule::module)
      .findFirst().get();
  }

  public List<RegisteredModule> allRegisteredModules() {
    return List.copyOf(modules);
  }

  public List<Module> allModules() {
    return modules.stream()
      .map(RegisteredModule::module)
      .collect(Collectors.toList());
  }
}
