package com.dulno.core.module;

import com.dulno.core.locale.Locale;
import com.dulno.core.log.Log;
import com.dulno.core.worker.WorkerDistribution;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleLoader {
  public static ModuleLoader create(
    Log log, String directory, WorkerDistribution distribution,
    Locale englishLocale, Locale germanLocale, Injector injector
  ) {
    var jars = findJarsInDirectory(directory);
    var urls = jars.stream().map(ModuleLoader::findFileUrl).toArray(URL[]::new);
    var classLoader = new URLClassLoader(urls, ModuleLoader.class.getClassLoader());
    return new ModuleLoader(log, jars, classLoader, distribution, englishLocale,
      germanLocale, injector);
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
  private final WorkerDistribution distribution;
  private final Locale englishLocale;
  private final Locale germanLocale;
  private Injector injector;

  /**
   * Loads all modules that are contained in the module folder
   * @throws Exception
   */
  public void loadModules() throws Exception {
    for (var moduleClass : findAllModuleClasses()) {
      modules.add(createRegisteredModule(moduleClass));
    }
    Collections.reverse(modules);
    for (var module : modules) {
      log.info("Start loading module " + module.name());
      module.module().preEnable();
      module.module().enable();
      module.module().postEnable();
      applyModuleLocales(module.name());
      log.info("Successfully loaded module " + module.name());
    }
    distribution.registerMultipleModules(modules.stream()
      .map(RegisteredModule::name).toList());
  }

  /**
   * Is used to collected and sort the module classes of all jar files
   * @return The module classes
   * @throws Exception
   */
  private List<Class<?>> findAllModuleClasses() throws Exception {
    var moduleClassPriorities = Maps.<Class<?>, ModuleLoadPriority>newHashMap();
    for (var moduleFile : jars) {
      for (var moduleClass : findJarModuleClasses(moduleFile, classLoader)) {
        ModuleLoadPriority priority = findAnnotationField(
          findModuleAnnotation(moduleClass).get(), "priority");
        moduleClassPriorities.put(moduleClass, priority);
      }
    }
    List<Class<?>> moduleClasses = moduleClassPriorities.entrySet().stream()
      .sorted(Comparator.comparingInt(entry -> entry.getValue().value()))
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
    Collections.reverse(moduleClasses);
    return moduleClasses;
  }

  /**
   * Is used to find the module classes of a single jar file
   * @param file The jar file
   * @param classLoader The class loader that should be used
   * @return The module classes of the jar
   * @throws Exception
   */
  private List<Class<?>> findJarModuleClasses(
    File file, ClassLoader classLoader
  ) throws Exception {
    var jarFile = new JarFile(file);
    var entries = jarFile.entries();
    var moduleClasses = Lists.<Class<?>>newArrayList();
    while (entries.hasMoreElements()) {
      var entry = entries.nextElement();
      var optionalModuleClass = findModuleClass(entry, classLoader);
      if (optionalModuleClass.isEmpty()) {
        continue;
      }
      moduleClasses.add(optionalModuleClass.get());
    }
    if (moduleClasses.isEmpty()) {
      log.log(Level.SEVERE, "Could not find module class for " + file.getName());
    }
    return moduleClasses;
  }

  /**
   * Creates a new registered module
   * @param moduleClass The class of the module
   * @return The new registered module
   * @throws Exception
   */
  private RegisteredModule createRegisteredModule(
    Class<?> moduleClass
  ) throws Exception {
    var module = createModule(moduleClass);
    injector = module.injector();
    var annotation = findModuleAnnotation(moduleClass).get();
    return RegisteredModule.create(module, findAnnotationField(annotation, "name"),
      findAnnotationField(annotation, "version"),
      findAnnotationField(annotation, "priority"));
  }

  /**
   * Calls the constructor of a module
   * @param moduleClass The class of the module
   * @return The called module
   * @throws Exception
   */
  private Module createModule(Class<?> moduleClass) throws Exception {
    return (Module) moduleClass.getConstructor(Injector.class)
      .newInstance(injector);
  }

  /**
   * Searches for the module class inside module jar entry
   * @param entry The jar file entry of the module
   * @param classLoader The regarding class loader
   * @return The module class if it could be found
   * @throws Exception
   */
  private Optional<Class<?>> findModuleClass(
    JarEntry entry, ClassLoader classLoader
  ) throws Exception {
    var entryName = entry.getName();
    if (entry.isDirectory() || !entryName.endsWith(".class") ||
      !entryName.startsWith("com/dulno")
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
    return Module.class.isAssignableFrom(suspect.getSuperclass());
  }

  private void applyModuleLocales(String module) throws Exception {
    var englishModuleLocale = com.dulno.core.locale.Locale.create(module, "en");
    if (englishModuleLocale.exists()) {
      englishModuleLocale.load();
      englishLocale.addLocale(englishModuleLocale.locale());
    }
    var germanModuleLocale = Locale.create(module, "de");
    if (germanModuleLocale.exists()) {
      germanModuleLocale.load();
      germanLocale.addLocale(germanModuleLocale.locale());
    }
    if (englishModuleLocale.exists() && germanModuleLocale.exists()) {
      log.info("Successfully loaded locales of module " + module);
    }
  }

  /**
   * Used to find values of {@link ModuleDescription}
   * @param annotation The specific annotation that is to be examine
   * @param fieldName The name of the target field
   * @return The value of the annotation field
   * @param <T> The generic type fo the field
   * @throws Exception
   */
  private <T> T findAnnotationField(
    Annotation annotation, String fieldName
  ) throws Exception {
    var method = Arrays.stream(annotation.annotationType().getDeclaredMethods())
      .filter(declaredMethod -> declaredMethod.getName().equals(fieldName))
      .findFirst().get();
    return (T) method.invoke(annotation, (Object[])null);
  }

  /**
   * Used to find the {@link ModuleDescription} annotation of a module
   * @param suspect The class from which the {@link ModuleDescription} is to be
   *                retrieved
   * @return The {@link ModuleDescription} annotation if it could be found
   */
  private Optional<Annotation> findModuleAnnotation(Class<?> suspect) {
    return Arrays.stream(suspect.getAnnotations())
      .filter(annotation -> annotation.annotationType()
        .equals(ModuleDescription.class)).findFirst();
  }

  /**
   * Is used to find module information
   * @param moduleName The name of the module
   * @return The information of the module
   */
  public Optional<ModuleInformation> findModuleInformation(String moduleName) {
    var moduleOptional = findRegisteredModuleById(moduleName);
    if (moduleOptional.isEmpty()) {
      return Optional.empty();
    }
    return moduleOptional.map(RegisteredModule::module).map(Module::moduleInformation);
  }

  /**
   * Used to find a registered module
   * @param id The {@link ModuleDescription} name of the module you are
   *          searching for in
   * @return The {@link RegisteredModule} if it could be found
   */
  public Optional<RegisteredModule> findRegisteredModuleById(String id) {
    return modules.stream().filter(module -> module.name().equals(id)).findFirst();
  }

  /**
   * Used to find a registered module
   * @param name The name of the module you are searching for
   * @return The {@link RegisteredModule} if it could be found
   */
  public Optional<RegisteredModule> findRegisteredModule(String name) {
    return modules.stream().filter(module ->
        module.module().moduleInformation().name().equalsIgnoreCase(name))
      .findFirst();
  }

  /**
   * Used to find module of a registered module
   * @param name The name of the module
   * @return The {@link Module} if it could be found
   */
  public Optional<Module> findModule(String name) {
    return findRegisteredModule(name)
      .map(RegisteredModule::module);
  }

  /**
   * Used to get all registered modules (immutable)
   * @return The list of all {@link RegisteredModule}s
   */
  public List<RegisteredModule> allRegisteredModules() {
    return List.copyOf(modules);
  }

  /**
   * Is called to find all modules
   * @return The list of all {@link Module}s
   */
  public List<Module> allModules() {
    return modules.stream()
      .map(RegisteredModule::module)
      .collect(Collectors.toList());
  }
}
