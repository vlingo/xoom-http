package io.vlingo.http.resource;

import com.google.gson.Gson;

public class TestMapper implements Mapper {

  private Gson gson;

  public TestMapper() {
    this.gson = new Gson();
  }

  @Override
  public <T> T from(String data, Class<T> clazz) {
    return gson.fromJson(data, clazz);
  }

  @Override
  public <T> String from(T data) {
    return gson.toJson(data);
  }
}
