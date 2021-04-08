// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Header {
  public static final String ValueWildcardAny = "*";
  public static final String ValueBr = "br";
  public static final String ValueClose = "close";
  public static final String ValueCompress = "compress";
  public static final String ValueDeflate = "deflate";
  public static final String ValueGZip = "gzip";
  public static final String ValueIdentity = "identity";
  public static final String ValueISO_8859_15 = "iso-8859-15";
  public static final String ValueKeepAlive = "keep-alive";
  public static final String ValueUTF_8 = "utf-8";

  public final String name;
  public final String value;

  protected Header(final String name, final String value) {
    this.name = name;
    this.value = value;
  }

  public boolean matchesNameOf(final Header header) {
    return this.name.equalsIgnoreCase(header.name);
  }

  public boolean matchesNameOf(final String name) {
    return this.name.equalsIgnoreCase(name);
  }

  public boolean matchesValueOf(final Header header) {
    return this.value.equals(header.value);
  }

  public boolean matchesValueOf(final String value) {
    return this.value.equals(value);
  }

  @Override
  public int hashCode() {
    return 31 * name.hashCode() + value.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != getClass()) {
      return false;
    }

    final Header otherHeader = (Header) other;

    return matchesNameOf(otherHeader) && matchesValueOf(otherHeader);
  }

  @Override
  public String toString() {
    return "" + name + ": " + value;
  }

  public static class Headers<T extends Header> extends ArrayList<T> implements List<T> {
    private static final long serialVersionUID = 1L;

    public static Headers<RequestHeader> of(final RequestHeader... requestHeaders) {
      final Headers<RequestHeader> headers = new Headers<>(requestHeaders.length);
      for (final RequestHeader requestHeader : requestHeaders) {
        headers.add(requestHeader);
      }
      return headers;
    }

    public static Headers<ResponseHeader> of(final ResponseHeader... responseHeaders) {
      final Headers<ResponseHeader> headers = new Headers<>(responseHeaders.length);
      for (final ResponseHeader responseHeader : responseHeaders) {
        headers.add(responseHeader);
      }
      return headers;
    }

    public T headerOf(final String name) {
      return headerOfOrDefault(name, null);
    }

    public T headerOfOrDefault(final String name, final T defaultHeader) {
      final Iterator<T> iter = this.iterator();
      while (iter.hasNext()) {
        final T header = iter.next();
        if (header.matchesNameOf(name)) {
          return header;
        }
      }
      return defaultHeader;
    }

    public static <T extends Header> Headers<T> empty() {
      return new Headers<T>(0);
    }

    public Headers<T> and(final Headers<? extends Header> headers) {
      for (final Header header : headers) {
        and(header);
      }
      return this;
    }

    @SuppressWarnings("unchecked")
    public Headers<T> and(final Header header) {
      Header modified = null;
      final int size = size();
      for (int index = 0; index < size; ++index) {
        if (get(index).matchesNameOf(header)) {
          modified = set(index, (T) header);
          break;
        }
      }

      if (modified == null) {
        add((T) header);
      }

      return this;
    }

    public Headers<T> and(final String name, final String value) {
      and(new Header(name, value));
      return this;
    }

    public Headers<T> copy() {
      final Headers<T> headers = new Headers<>(this.size());
      for (final T header : this) {
        headers.add(header);
      }
      return headers;
    }

    @Override
    public void add(int index, T element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      super.clear();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      final Iterator<T> iter = this.iterator();
      while (iter.hasNext()) {
        builder.append(iter.next()).append("\n");
      }
      return builder.toString();
    }

    Headers(final int initialCapactiy) {
      super(initialCapactiy);
    }
  }
}
