// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.common.compiler.DynaFile.GeneratedSources;
import static io.vlingo.common.compiler.DynaFile.GeneratedTestSources;
import static io.vlingo.common.compiler.DynaFile.RootOfMainClasses;
import static io.vlingo.common.compiler.DynaFile.RootOfTestClasses;
import static io.vlingo.common.compiler.DynaFile.toFullPath;
import static io.vlingo.common.compiler.DynaFile.toPackagePath;
import static io.vlingo.common.compiler.DynaNaming.classnameFor;
import static io.vlingo.common.compiler.DynaNaming.fullyQualifiedClassnameFor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.List;

import io.vlingo.common.compiler.DynaFile;
import io.vlingo.common.compiler.DynaType;
import io.vlingo.http.resource.Action.MethodParameter;
import io.vlingo.http.resource.Action.ToSpec;

public class ResourceDispatcherGenerator implements AutoCloseable {
  public static class Result {
    public final String classname;
    public final String fullyQualifiedClassname;
    public final String source;
    public final File sourceFile;
    
    private Result(final String fullyQualifiedClassname, final String classname, final String source, final File sourceFile) {
      this.fullyQualifiedClassname = fullyQualifiedClassname;
      this.classname = classname;
      this.source = source;
      this.sourceFile = sourceFile;
    }
  }

  private final List<Action> actions;
  private final boolean persist;
  private final String rootOfClasses;
  private final String rootOfGenerated;
  private final File targetClassesPath;
  private final DynaType type;
  private final URLClassLoader urlClassLoader;

  static ResourceDispatcherGenerator forMain(final List<Action> actions, final boolean persist) throws Exception {
    final String root = Properties.properties.getProperty("resource.dispatcher.generated.classes.main", RootOfMainClasses);
    return new ResourceDispatcherGenerator(actions, root, DynaType.Main, persist);
  }

  static ResourceDispatcherGenerator forTest(final List<Action> actions, final boolean persist) throws Exception {
    final String root = Properties.properties.getProperty("resource.dispatcher.generated.classes.test", RootOfTestClasses);
    return new ResourceDispatcherGenerator(actions, root, DynaType.Test, persist);
  }

  @Override
  public void close() throws Exception {
    urlClassLoader.close();
  }


  Result generateFor(final String handlerProtocol) {
    System.out.println("vlingo/http: Generating handler dispatcher for " + (type == DynaType.Main ? "main":"test") + ": " + handlerProtocol);
    
    final String relativePathToClass = toFullPath(handlerProtocol);
    final String relativePathToClassFile = rootOfClasses + relativePathToClass + ".class";
    final File targetClassesRelativePathToClass = new File(relativePathToClassFile);
    
    if (targetClassesRelativePathToClass.exists()) {
      try {
        final Class<?> handlerInterface = readHandlerInterface(handlerProtocol);
        final String dispatcherClassSource = dispatcherClassSource(handlerInterface);
        final String fullyQualifiedClassname = fullyQualifiedClassnameFor(handlerInterface, ConfigurationResource.DispatcherSuffix);
        final String relativeTargetFile = toFullPath(fullyQualifiedClassname);
        final File sourceFile = persist ? persistDispatcherClassSource(handlerProtocol, relativeTargetFile, dispatcherClassSource) : new File(relativeTargetFile);
        return new Result(fullyQualifiedClassname, classnameFor(handlerInterface, ConfigurationResource.DispatcherSuffix), dispatcherClassSource, sourceFile);
      } catch (Exception e) {
        throw new IllegalArgumentException("Cannot generate resource dispatcher class for: " + handlerProtocol, e);
      }
    } else {
      throw new IllegalArgumentException("Cannot generate resource dispatcher class for " + handlerProtocol + " because there is no corresponding:\n" + relativePathToClassFile);
    }
  }

  DynaType type() {
    return type;
  }

  URLClassLoader urlClassLoader() {
    return urlClassLoader;
  }

  private ResourceDispatcherGenerator(final List<Action> actions, final String rootOfClasses, final DynaType type, final boolean persist) throws Exception {
    this.actions = actions;
    this.rootOfClasses = rootOfClasses;
    this.rootOfGenerated = rootOfGeneratedSources(type);
    this.type = type;
    this.persist = persist;
    this.targetClassesPath = new File(rootOfClasses);
    this.urlClassLoader = initializeClassLoader(targetClassesPath);
  }

  private String actionCase(final Action action) {
    final StringBuilder builder = new StringBuilder();
    
    builder.append("      case ").append(action.id).append(": // ").append(action.method.toString()).append(" ").append(action.uri).append(" ").append(action.originalTo).append("\n");
    builder.append("        consumer = (handler) -> handler.").append(asExpression(action.to)).append(";\n");
    builder.append("        pooledHandler().handleFor(context, consumer);\n");
    builder.append("        break;\n");
    
    return builder.toString();
  }

  public String asExpression(final ToSpec to) {
    final StringBuilder builder = new StringBuilder();
    
    builder.append(to.methodName()).append("(");
    
    String separator = "";
    int parameterIndex = 0;
    
    for (final MethodParameter parameter : to.parameters()) {
      builder.append(separator).append("(").append(parameter.type).append(") ").append("mappedParameters.mapped.get(" + parameterIndex + ").value");
      ++parameterIndex;
      separator = ", ";
    }
    
    builder.append(")");
    
    return builder.toString();
  }

  private String classStatement(final Class<?> handlerInterface) {
    return MessageFormat.format("public class {0} extends ConfigurationResource<{1}> '{'\n", classnameFor(handlerInterface, ConfigurationResource.DispatcherSuffix), handlerInterface.getSimpleName());
  }

  private String constructor(final Class<?> protocolInterface) {
    final StringBuilder builder = new StringBuilder();

    final String signature0 = MessageFormat.format("  public {0}(\n", classnameFor(protocolInterface, ConfigurationResource.DispatcherSuffix));
    final String signature1 = "          final String name,\n";
    final String signature2 = "          final Class<? extends ResourceHandler> resourceHandlerClass,\n";
    final String signature3 = "          final int handlerPoolSize,\n";
    final String signature4 = "          final List<Action> actions) {\n";
    
    builder
      .append(signature0)
      .append(signature1)
      .append(signature2)
      .append(signature3)
      .append(signature4)
      .append("    super(name, resourceHandlerClass, handlerPoolSize, actions);\n")
      .append("  }\n");

    return builder.toString();
  }
  
  private String dispatcherClassSource(final Class<?> handlerType) {
    final StringBuilder builder = new StringBuilder();
    
    builder
      .append(packageStatement(handlerType)).append("\n\n")
      .append(importStatements(handlerType)).append("\n")
      .append(classStatement(handlerType)).append("\n")
      .append(constructor(handlerType)).append("\n")
      .append(methodDefinition(handlerType))
      .append("}").append("\n");
    
    return builder.toString();
  }

  private String importStatements(final Class<?> handlerInterface) {
    final StringBuilder builder = new StringBuilder();
    
    builder
      .append("import java.util.List;").append("\n")
      .append("import java.util.function.Consumer;").append("\n\n")
      
      .append("import io.vlingo.http.Context;").append("\n")
      .append("import io.vlingo.http.resource.Action;").append("\n")
      .append("import io.vlingo.http.resource.Action.MappedParameters;").append("\n")
      .append("import io.vlingo.http.resource.ConfigurationResource;").append("\n")
      .append("import io.vlingo.http.resource.ResourceHandler;").append("\n");

    final Class<?> outerClass = handlerInterface.getDeclaringClass();
    
    if (outerClass != null) {
      builder.append("import " + outerClass.getName() + "." + handlerInterface.getSimpleName() + ";").append("\n");
    }

    return builder.toString();
  }

  private URLClassLoader initializeClassLoader(final File targetClassesPath) throws MalformedURLException {
    final String classpath = "file://" + targetClassesPath.getAbsolutePath() + "/";
    final URL url = new URL(classpath);
    final URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url });
    return urlClassLoader;
  }

  private String methodDefinition(final Class<?> handlerType) {
    final StringBuilder builder = new StringBuilder();
    
    builder
      .append("  @Override\n")
      .append("  public void dispatchToHandlerWith(final Context context, final MappedParameters mappedParameters) {\n")
      .append("    Consumer<" + handlerType.getSimpleName() + "> consumer = null;\n")
      .append("\n")
      .append("    try {\n")
      .append("      switch (mappedParameters.actionId) {\n");
    
    for (final Action action : actions) {
      builder.append(actionCase(action));
    }
    
    builder
      .append("      }\n")
      .append("    } catch (Exception e) {\n")
      .append("      throw new IllegalArgumentException(\"Action mismatch: Request: \" + context.request + \"Parameters: \" + mappedParameters);\n")
      .append("    }\n")
      .append("  }\n");
    
    return builder.toString();
  }

  private String packageStatement(final Class<?> handlerInterface) {
    return MessageFormat.format("package {0};", handlerInterface.getPackage().getName());
  }

  private File persistDispatcherClassSource(final String handlerProtocol, final String relativePathToClass, final String dispatcherClassSource) throws Exception {
    final String pathToGeneratedSource = toPackagePath(handlerProtocol);
    new File(rootOfGenerated + pathToGeneratedSource).mkdirs();
    final String pathToSource = rootOfGenerated + relativePathToClass + ".java";
    
    return DynaFile.persistDynaClassSource(pathToSource, dispatcherClassSource);
  }

  private Class<?> readHandlerInterface(final String handlerProtocol) throws Exception {
    final Class<?> handlerInterface = urlClassLoader.loadClass(handlerProtocol);
    return handlerInterface;
  }

  private String rootOfGeneratedSources(final DynaType type) {
    final String root = 
            type == DynaType.Main ?
                    Properties.properties.getProperty("resource.dispatcher.generated.sources.main", GeneratedSources) :
                    Properties.properties.getProperty("resource.dispatcher.generated.sources.test", GeneratedTestSources);
    return root;
  }
}
