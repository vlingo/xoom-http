// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vlingo.common.Tuple2;
import io.vlingo.http.Method;
import io.vlingo.http.Request;

public final class Action {
  static final MatchResults unmatchedResults = new MatchResults(null, null, Collections.emptyList(), "", false);

  public final List<MappedParameter> additionalParameters;
  public final boolean disallowPathParametersWithSlash;
  public final int id;
  public final Method method;
  public final String uri;
  public final String originalTo;
  public final ToSpec to;
  public final Mapper mapper;
  private final Matchable matchable;

  public Action(final int id, final String method, final String uri, final String to, final String mapper, final boolean disallowPathParametersWithSlash) {
    this(id, method, uri, to, mapper, disallowPathParametersWithSlash, Collections.emptyList());
  }

  public Action(final int id, final String method, final String uri, final String to, final String mapper, final boolean disallowPathParametersWithSlash, final List<MappedParameter> additionalParameters) {
    this.id = id;
    this.method = Method.from(method);
    this.uri = uri;
    this.to = new ToSpec(to);
    this.originalTo = to;
    this.mapper = mapper == null ? DefaultMapper.instance : mapperFrom(mapper);
    this.disallowPathParametersWithSlash = disallowPathParametersWithSlash;
    this.additionalParameters = additionalParameters;
    this.matchable = new Matchable(uri);
  }

  MappedParameters map(final Request request, final List<RawPathParameter> parameters) {
    final List<MappedParameter> mapped = new ArrayList<>(parameters.size());
    for (final MethodParameter typed : to.parameters) {
      if (typed.isBody()) {
        final Object body = mapBodyFrom(request);
        mapped.add(new MappedParameter(typed.type, body));
      } else {
        final RawPathParameter raw = RawPathParameter.named(typed.name, parameters);
        if (raw == null) break;
        final Object other = mapOtherFrom(raw);
        mapped.add(new MappedParameter(typed.type, other));
      }
    }
    mapped.addAll(additionalParameters);
    return new MappedParameters(this.id, this.method, to.methodName, mapped);
  }

  private int indexOfNextSegmentStart(int currentIndex, String path) {
    int nextSegmentStart = path.indexOf("/", currentIndex);
    if (nextSegmentStart < currentIndex) {
      return path.length();
    }
    return nextSegmentStart;
  }

  MatchResults matchWith(final Method method, final URI uri) {
    if (this.method.equals(method)) {
      final String path = uri.getPath();
      int pathCurrentIndex = 0;
      final int totalSegments = matchable.totalSegments();
      final RunningMatchSegments running = new RunningMatchSegments(totalSegments);
      for (int idx = 0; idx < totalSegments; ++idx) {
        final PathSegment segment = matchable.pathSegment(idx);
        if (segment.isPathParameter()) {
          running.keepParameterSegment(pathCurrentIndex);
          pathCurrentIndex = indexOfNextSegmentStart(pathCurrentIndex, path);
        } else {
          final int indexOfSegment = path.indexOf(segment.value, pathCurrentIndex);
          if (indexOfSegment == -1 || (pathCurrentIndex == 0 && indexOfSegment != 0)) {
            return unmatchedResults;
          }
          final int lastIndex = segment.lastIndexOf(indexOfSegment);
          running.keepPathSegment(indexOfSegment, lastIndex);
          pathCurrentIndex = lastIndex;
        }
      }
      int nextPathSegmentIndex = indexOfNextSegmentStart(pathCurrentIndex, path);
      if ( nextPathSegmentIndex != path.length()) {
        if (disallowPathParametersWithSlash || nextPathSegmentIndex < path.length() - 1) {
          return unmatchedResults;
        }
      }
      final MatchResults matchResults = new MatchResults(this, running, parameterNames(), path, disallowPathParametersWithSlash);
      return matchResults;
    }
    return unmatchedResults;
  }

  @Override
  public int hashCode() {
    return 31 * (method.hashCode() + uri.hashCode() + to.hashCode() + mapper.hashCode() + matchable.hashCode());
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != Action.class) {
      return false;
    }

    final Action otherAction = (Action) other;
    
    return this.method.equals(otherAction.method) && this.uri.equals(otherAction.uri) && this.to.equals(otherAction.to);
  }

  @Override
  public String toString() {
    return "Action[id=" + id + ", method=" + method + ", uri=" + uri + ", to=" + to + "" + "]";
  }

  @SuppressWarnings("unchecked")
  private Mapper mapperFrom(final String mapper) {
    try {
      final Class<Mapper> mapperClass = (Class<Mapper>) Class.forName(mapper);
      return mapperClass.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot load mapper class: " + mapper);
    }
  }

  private int parameterCount() {
    int count = 0;
    for (final PathSegment segment : matchable.pathSegments) {
      if (segment.pathParameter) {
        ++count;
      }
    }
    return count;
  }

  private Object mapBodyFrom(final Request request) {
    final MethodParameter body = to.body();
    if (body != null) {
      return mapper.from(request.body.toString(), body.bodyType);
    }
    return null;
  }

  private Object mapOtherFrom(final RawPathParameter parameter) {
    final String type = this.to.parameterOf(parameter.name).type;
      
    switch (type) {
    case "String":
      return parameter.value;
    case "int":
    case "Integer":
      return Integer.parseInt(parameter.value);
    case "long":
    case "Long":
      return Long.parseLong(parameter.value);
    case "boolean":
    case "Boolean":
      return Boolean.parseBoolean(parameter.value);
    case "double":
    case "Double":
      return Double.parseDouble(parameter.value);
    case "short":
    case "Short":
      return Short.parseShort(parameter.value);
    case "float":
    case "Float":
      return Float.parseFloat(parameter.value);
    case "char":
    case "Character":
      return parameter.value.charAt(0);
    case "byte":
    case "Byte":
      return Byte.parseByte(parameter.value);
    default:
      return null;
    }
  }

  private List<String> parameterNames() {
    final List<String> parameterNames = new ArrayList<>(parameterCount());
    for (final PathSegment segment : matchable.pathSegments) {
      if (segment.pathParameter) {
        parameterNames.add(segment.value);
      }
    }
    return parameterNames;
  }

  //=====================================
  // MappedParameters
  //=====================================

  public static class MappedParameters {
    public final int actionId;
    public final List<MappedParameter> mapped;
    public final Method httpMethod;
    public final String methodName;

    MappedParameters(final int actionId, final Method httpMethod, final String methodName, final List<MappedParameter> mapped) {
      this.actionId = actionId;
      this.httpMethod = httpMethod;
      this.methodName = methodName;
      this.mapped = mapped;
    }

    @Override
    public String toString() {
      return "MappedParameters[actionId=" + actionId + ", httpMethod=" + httpMethod + ", methodName=" + methodName + ", mapped=" + mapped + "]";
    }
  }

  public static class MappedParameter {
    public final String type;
    public final Object value;
    
    MappedParameter(final String type, final Object value) {
      this.type = type;
      this.value = value;
    }

    @Override
    public String toString() {
      return "MappedParameter[type=" + type + ", value=" + value + "]";
    }
  }

  //=====================================
  // MatchResults
  //=====================================

  public static class MatchResults {
    public final Action action;
    public final boolean matched;
    private final List<RawPathParameter> parameters;

    @Override
    public String toString() {
      return "MatchResults[action=" + action + ", matched=" + matched + ", parameters=" + parameters + "]";
    }

    MatchResults(
            final Action action,
            final RunningMatchSegments running,
            final List<String> parameterNames,
            final String path,
            final boolean disallowPathParametersWithSlash) {
      
      this.action = action;
      this.parameters = new ArrayList<>(parameterNames.size());
      
      if (running == null) {
        this.matched = false;
      } else {
        int pathLength = 0;
        final int total = running.total();
        for (int idx = 0, parameterIndex = 0; idx < total; ++idx) {
          final MatchSegment segment = running.matchSegment(idx);
          
          if (segment.isPathParameter()) {
            final int pathStartIndex = segment.pathStartIndex();
            final int pathEndIndex = running.nextSegmentStartIndex(idx, path.length());
            if (pathStartIndex >= pathEndIndex) {
              this.matched = false;
              return;
            }
            final String value = path.substring(pathStartIndex, pathEndIndex);
            if (disallowPathParametersWithSlash && value.indexOf("/") >= 0) {
              this.matched = false;
              return;
            }
            pathLength += value.length();
            parameters.add(new RawPathParameter(parameterNames.get(parameterIndex++), value));
          } else {
            pathLength += action.matchable.pathSegment(idx).value.length();
          }
        }
        this.matched = pathLength == path.length();
      }
    }

    public boolean isMatched() {
      return matched;
    }

    public int parameterCount() {
      return parameters.size();
    }

    public List<RawPathParameter> parameters() {
      return parameters;
    }
  }

  //=====================================
  // BodyTypedParameter
  //=====================================

  public static class BodyTypedParameter {
    public final String name;
    public final Class<?> type;
    
    public BodyTypedParameter(final Class<?> type, final String name) {
      this.type = type;
      this.name = name;
    }

    @Override
    public String toString() {
      return "BodyParameter[type=" + type.getName() + ", name=" + name + "]";
    }
  }

  //=====================================
  // RawPathParameter
  //=====================================

  public static class RawPathParameter {
    public final String name;
    public final String value;

    static RawPathParameter named(final String name, final List<RawPathParameter> parameters) {
      for (final RawPathParameter param : parameters) {
        if (param.name.equals(name)) {
          return param;
        }
      }
      return null;
    }
    
    public RawPathParameter(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public String toString() {
      return "Parameter[name=" + name + ", value=" + value + "]";
    }
  }

  //=====================================
  // MethodParameter
  //=====================================

  public static class MethodParameter {
    public final Class<?> bodyType;
    public final String name;
    public final String type;

    public MethodParameter(final String type, final String name) {
      this(type, name, null);
    }

    public MethodParameter(final String type, final String name, final Class<?> bodyClass) {
      this.type = type;
      this.name = name;
      this.bodyType = bodyClass;
    }

    public boolean isBody() {
      return bodyType != null;
    }

    @Override
    public String toString() {
      return "MethodParameter[type=" + type + ", name=" + name + "]";
    }
  }

  //=====================================
  // RunningMatchSegments
  //=====================================

  private static class RunningMatchSegments {
    private final List<MatchSegment> matchSegments;

    @Override
    public String toString() {
      return "RunningMatchSegments[matchSegments=" + matchSegments + "]";
    }

    RunningMatchSegments(final int totalSegments) {
      this.matchSegments = new ArrayList<>(totalSegments + 1);
    }

    void keepParameterSegment(final int pathStartIndex) {
      matchSegments.add(new MatchSegment(true, pathStartIndex));
    }

    void keepPathSegment(final int pathStartIndex, final int pathEndIndex) {
      matchSegments.add(new MatchSegment(false, pathStartIndex));
    }

    int nextSegmentStartIndex(final int index, final int maxIndex) {
      return index < (total() - 1) ? matchSegment(index + 1).pathStartIndex : maxIndex;
    }

    MatchSegment matchSegment(final int index) {
      return matchSegments.get(index);
    }

    int total() {
      return matchSegments.size();
    }
  }

  //=====================================
  // MatchSegment
  //=====================================

  private static class MatchSegment {
    private final boolean pathParameter;
    private final int pathStartIndex;

    @Override
    public String toString() {
      return "MatchSegment[pathParameter=" + pathParameter + ", pathStartIndex=" + pathStartIndex + "]";
    }

    MatchSegment(final boolean pathParameter, final int pathStartIndex) {
      this.pathParameter = pathParameter;
      this.pathStartIndex = pathStartIndex;
    }

    int pathStartIndex() {
      return pathStartIndex;
    }

    boolean isPathParameter() {
      return pathParameter;
    }
  }

  //=====================================
  // Matchable
  //=====================================

  private static class Matchable {
    private final List<PathSegment> pathSegments;
    
    @Override
    public int hashCode() {
      return 31 * pathSegments.hashCode();
    }

    @Override
    public String toString() {
      return "Matchable[pathSegments=" + pathSegments + "]";
    }

    Matchable(final String uri) {
      this.pathSegments = segmented(uri);
    }

    PathSegment pathSegment(final int index) {
      return pathSegments.get(index);
    }

    int totalSegments() {
      return pathSegments.size();
    }

    private List<PathSegment> segmented(final String uri) {
      final List<PathSegment> segments = new ArrayList<>();
      String start = uri;
      while (true) {
        final int openBrace = start.indexOf("{");
        if (openBrace >= 0) {
          final int closeBrace = start.indexOf("}", openBrace);
          if (closeBrace > openBrace) {
            final String segment = start.substring(0, openBrace);
            segments.add(new PathSegment(segment, false));
            final String parameter = start.substring(openBrace + 1, closeBrace);
            segments.add(new PathSegment(parameter, true));
            start = start.substring(closeBrace + 1);
            if (start.isEmpty()) {
              break;
            }
          } else {
            throw new IllegalStateException("URI has unbalanced brace: " + uri);
          }
        } else {
          segments.add(new PathSegment(start, false));
          break;
        }
      }

      return segments;
    }
  }

  //=====================================
  // PathSegment
  //=====================================

  private static class PathSegment {
    private final boolean pathParameter;
    private final String value;

    @Override
    public String toString() {
      return "PathSegment[pathParameter=" + pathParameter + ", value=" + value + "]";
    }

    PathSegment(final String value, final boolean pathParameter) {
      this.value = value;
      this.pathParameter = pathParameter;
    }
    
    public int lastIndexOf(final int startIndex) {
      return startIndex + value.length();
    }

    boolean isPathParameter() {
      return pathParameter;
    }
  }

  //=====================================
  // ToSpec
  //=====================================

  public static class ToSpec {
    private final String methodName;
    private final List<MethodParameter> parameters;
    
    public final String methodName() {
      return methodName;
    }

    public final List<MethodParameter> parameters() {
      return Collections.unmodifiableList(parameters);
    }
    
    @Override
    public String toString() {
      return "ToSpec[methodName=" + methodName + ", parameters=" + parameters + "]";
    }

    ToSpec(final String to) {
      final Tuple2<String, List<MethodParameter>> parsed = parse(to);
      this.methodName = parsed._1;
      this.parameters = parsed._2;
    }

    MethodParameter body() {
      for (final MethodParameter parameter : parameters) {
        if (parameter.isBody()) {
          return parameter;
        }
      }
      return null;
    }

    MethodParameter parameterOf(final String name) {
      for (final MethodParameter parameter : parameters) {
        if (parameter.name.equals(name)) {
          return parameter;
        }
      }
      return null;
    }

    private Class<?> load(final String classname) {
      try {
        return Class.forName(classname);
      } catch (Exception e) {
        throw new IllegalStateException("Cannot load class for: " + classname);
      }
    }

    private Tuple2<String, List<MethodParameter>> parse(final String to) {
      final String bad = "Invalid to declaration: " + to;
      
      final int openParen = to.indexOf("(");
      final int closeParen = to.lastIndexOf(")");
      
      if (openParen < 0 || closeParen < 0) {
        throw new IllegalStateException(bad);
      }
      
      final String methodName = to.substring(0, openParen);
      final String[] rawParameters = to.substring(openParen + 1, closeParen).split(",");
      final List<MethodParameter> parameters = new ArrayList<>(rawParameters.length);
      
      for (String rawParameter : rawParameters) {
        rawParameter = rawParameter.trim();
        if (!rawParameter.isEmpty()) {
          if (rawParameter.startsWith("body:")) {
            final String[] body = typeAndName(rawParameter.substring(5));
            parameters.add(new MethodParameter(body[0], body[1], load(qualifiedType(body[0]))));
          } else {
            final String[] other = typeAndName(rawParameter);
            parameters.add(new MethodParameter(other[0], other[1]));
          }
        }
      }
      
      return Tuple2.from(methodName, parameters);
    }

    private String qualifiedType(String possiblyUnqualifiedType) {
      switch (possiblyUnqualifiedType) {
        case "String":
          return "java.lang.String";
        case "int":
        case "Integer":
          return "java.lang.Integer";
        case "long":
        case "Long":
          return "java.lang.Long";
        case "boolean":
        case "Boolean":
          return "java.lang.Boolean";
        case "double":
        case "Double":
          return "java.lang.Double";
        case "short":
        case "Short":
          return "java.lang.Short";
        case "float":
        case "Float":
          return "java.lang.Float";
        case "char":
        case "Character":
          return "java.lang.Character";
        case "byte":
        case "Byte":
          return "java.lang.Byte";
        default:
          return possiblyUnqualifiedType;
      }
    }

    private String[] typeAndName(final String rawParameter) {
      final int space = rawParameter.lastIndexOf(' ');
      if (space == -1) {
        throw new IllegalStateException("Parameter type and name must be separated by space: " + rawParameter);
      }
      final String[] type_name = new String[2];
      type_name[0] = rawParameter.substring(0, space).trim();
      type_name[1] = rawParameter.substring(space + 1).trim();
      return type_name;
    }
  }
}
