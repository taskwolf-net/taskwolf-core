package net.taskwolf.core.module;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.log.Log;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleLoader {
  public static ModuleLoader create(
    Log log, String directory, Distribution distribution, Injector injector
  ) {
    var jars = findJarsInDirectory(directory);
    var urls = jars.stream().map(ModuleLoader::findFileUrl).toArray(URL[]::new);
    var classLoader = new URLClassLoader(urls, ModuleLoader.class.getClassLoader());
    return new ModuleLoader(log, jars, classLoader, distribution, injector);
  }

  private static List<File> findJarsInDirectory(String directory) {
    return Arrays.stream(new File(directory).listFiles())
      .filter(file -> !file.isDirectory())
      .filter(file -> file.getName().endsWith(".jar"))
      .collect(Collectors.toList());
  }

  private static URL findFileUrl(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private final Log log;
  private final List<File> jars;
  @Getter
  private final ClassLoader classLoader;
  private final List<RegisteredModule> modules = Lists.newArrayList();
  private final Distribution distribution;
  private final Injector injector;

  public void loadModules() throws Exception {
    for (var moduleFile : jars) {
      findModule(moduleFile, classLoader);
    }
    modules.sort(Comparator.comparingInt(module -> module.priority().value()));
    Collections.reverse(modules);
    for (var module : modules) {
      module.module().enable();
      distribution.registerModule(module.name());
      log.info("Successfully loaded module " + module.name());
    }
  }

  public boolean loadModule(File file) throws Exception {
    if (modules.stream().anyMatch(module -> module.file().equals(file))) {
      return false;
    }
    var module = findModule(file, classLoader);
    module.module().enable();
    distribution.registerModule(module.name());
    log.info("Successfully loaded module " + module.name());
    return true;
  }

  public boolean reloadModule(String name) throws Exception {
    var moduleOptional = modules.stream()
      .filter(module -> module.name().equals(name))
      .findFirst();
    if (moduleOptional.isEmpty()) {
      return false;
    }
    var registeredModule = moduleOptional.get();
    unloadModule(registeredModule);
    findModule(registeredModule.file(), classLoader)
      .module().enable();
    log.info("Successfully reloaded module " + registeredModule.name());
    return true;
  }

  private RegisteredModule findModule(
    File file, ClassLoader classLoader
  ) throws Exception {
    var jarFile = new JarFile(file);
    var entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      var entry = entries.nextElement();
      var optionalModuleClass = findModuleClass(entry, classLoader);
      if (optionalModuleClass.isEmpty()) {
        continue;
      }
      var registeredModule = createRegisteredModule(optionalModuleClass.get(), file);
      modules.add(registeredModule);
      return registeredModule;
    }
    log.log(Level.SEVERE, "Could not find module class for " + file.getName());
    return null;
  }

  private RegisteredModule createRegisteredModule(
    Class<?> moduleClass, File file
  ) throws Exception {
    var module = createModule(moduleClass);
    var annotation = findModuleAnnotation(moduleClass).get();
    return RegisteredModule.create(module, findAnnotationField(annotation, "name"),
      findAnnotationField(annotation, "version"),
      findAnnotationField(annotation, "priority"), file);
  }

  private Module createModule(Class<?> moduleClass) throws Exception {
    return (Module) moduleClass.getConstructor(Injector.class)
      .newInstance(injector);
  }

  private Optional<Class<?>> findModuleClass(
    JarEntry entry, ClassLoader classLoader
  ) throws Exception {
    var entryName = entry.getName();
    if (entry.isDirectory() || !entryName.endsWith(".class") ||
      !entryName.startsWith("net/taskwolf")
    ) {
      return Optional.empty();
    }
    var className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
    var entryClass = Class.forName(className, true, classLoader);
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

  private <T> T findAnnotationField(
    Annotation annotation, String fieldName
  ) throws Exception {
    var method = Arrays.stream(annotation.annotationType().getDeclaredMethods())
      .filter(declaredMethod -> declaredMethod.getName().equals(fieldName))
      .findFirst().get();
    return (T) method.invoke(annotation, (Object[])null);
  }

  private Optional<Annotation> findModuleAnnotation(Class<?> suspect) {
    return Arrays.stream(suspect.getAnnotations())
      .filter(annotation -> annotation.annotationType()
        .equals(ModuleDescription.class)).findFirst();
  }

  public boolean unloadModule(String name) throws Exception {
    var moduleOptional = modules.stream()
      .filter(module -> module.name().equals(name))
      .findFirst();
    if (moduleOptional.isEmpty()) {
      return false;
    }
    var registeredModule = moduleOptional.get();
    registeredModule.module().disable();
    unloadModule(registeredModule);
    log.info("Successfully unloaded module " + registeredModule.name());
    return true;
  }

  public void unloadModule(RegisteredModule registeredModule) throws Exception {
    distribution.unregisterModule(registeredModule.name());
    registeredModule.module().disable();
    modules.remove(registeredModule);
  }

  public Optional<RegisteredModule> findRegisteredModule(String name) {
    return modules.stream()
      .filter(module -> module.name().equals(name))
      .findFirst();
  }

  public Optional<Module> findModule(String name) {
    return findRegisteredModule(name)
      .map(RegisteredModule::module);
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
