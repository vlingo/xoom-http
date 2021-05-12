package io.vlingo.xoom.http.resource;

public class SinglePageApplicationConfiguration {

  private final String rootPath;
  private final String contextPath;

  private SinglePageApplicationConfiguration() {
    this("/frontend", "/app");
  }

  private SinglePageApplicationConfiguration(String rootPath, String contextPath) {
    this.rootPath = rootPath;
    this.contextPath = contextPath;
  }

  public String rootPath() {
    return rootPath;
  }

  public String contextPath() {
    return contextPath;
  }

  public static SinglePageApplicationConfiguration define() {
    return new SinglePageApplicationConfiguration();
  }

  public static SinglePageApplicationConfiguration defineWith(String rootPath, String contextPath) {
    return new SinglePageApplicationConfiguration(rootPath, contextPath);
  }
}
