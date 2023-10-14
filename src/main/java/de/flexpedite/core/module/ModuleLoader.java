package de.flexpedite.core.module;

import com.google.common.collect.Lists;
import de.flexpedite.core.CoreModule;
import de.flexpedite.core.action.ActionDatabaseTable;
import de.flexpedite.core.trigger.TriggerDatabaseTable;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "create")
public final class ModuleLoader {
  private final String directory;
  private final List<RegisteredModule> modules = Lists.newArrayList();

  public void loadModules(CoreModule coreModule) throws Exception {
    var files = jarsInDirectory();
    for (var moduleFile : files) {
      findModule(moduleFile, coreModule);
    }
    modules.sort(Comparator.comparingInt(module -> module.priority().value()));
    Collections.reverse(modules);
    for (var module : modules) {
      module.module().enable();
    }
  }

  private List<File> jarsInDirectory() {
    return Arrays.stream(new File(directory).listFiles())
      .filter(file -> !file.isDirectory())
      .filter(file -> file.getName().endsWith(".jar"))
      .collect(Collectors.toList());
  }

  public void reloadModule(String name, CoreModule coreModule) throws Exception {
    var moduleOptional = modules.stream()
      .filter(module -> module.name().equals(name))
      .findFirst();
    if (moduleOptional.isEmpty()) {
      return;
    }
    var registeredModule = moduleOptional.get();
    unloadModule(registeredModule);
    findModule(registeredModule.file(), coreModule).module().enable();
  }

  private RegisteredModule findModule(File file, CoreModule coreModule) throws Exception {
    var jarFile = new JarFile(file);
    var classLoader = new URLClassLoader(new URL[] {file.toURI().toURL()},
      this.getClass().getClassLoader());
    var entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      var entry = entries.nextElement();
      var optionalModuleClass = findModuleClass(entry, classLoader);
      if (optionalModuleClass.isEmpty()) {
        continue;
      }
      var registeredModule = createRegisteredModule(
        optionalModuleClass.get(), coreModule, file);
      modules.add(registeredModule);
      return registeredModule;
    }
    return null;
  }

  private RegisteredModule createRegisteredModule(
    Class<?> moduleClass, CoreModule coreModule, File file
  ) throws Exception {
    var module = createModule(moduleClass, coreModule);
    var annotation = findModuleAnnotation(moduleClass).get();
    return RegisteredModule.create(module, findAnnotationField(annotation, "name"),
      findAnnotationField(annotation, "version"),
      findAnnotationField(annotation, "priority"), file);
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
    var entryName = entry.getName();
    if (entry.isDirectory() || !entryName.endsWith(".class") ||
      !entryName.startsWith("de/flexpedite")
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

  public void unloadModule(String name) throws Exception {
    var moduleOptional = modules.stream()
      .filter(module -> module.name().equals(name))
      .findFirst();
    if (moduleOptional.isEmpty()) {
      return;
    }
    var registeredModule = moduleOptional.get();
    registeredModule.module().disable();
    unloadModule(registeredModule);
  }

  public void unloadModule(RegisteredModule registeredModule) throws Exception {
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

  public URL[] moduleFileUrls() {
    return modules.stream().map(RegisteredModule::file)
      .map(this::findUrl).toArray(URL[]::new);
  }

  private URL findUrl(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Module> allModules() {
    return modules.stream()
      .map(RegisteredModule::module)
      .collect(Collectors.toList());
  }
}
