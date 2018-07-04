// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Header {
  public final String name;
  public final String value;
  
  protected Header(final String name, final String value) {
    this.name = name;
    this.value = value;
  }

  public boolean matchesName(final Header header) {
    return name.equals(header.name);
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

    return name.equals(otherHeader.name) && value.equals(otherHeader.value);
  }

  @Override
  public String toString() {
    return "" + name + ": " + value;
  }

  public static class Headers<T extends Header> extends ArrayList<T> implements List<T> {
    private static final long serialVersionUID = 1L;
    
    public static Headers<ResponseHeader> of(final ResponseHeader... responseHeaders) {
      final Headers<ResponseHeader> headers = new Headers<>(responseHeaders.length);
      for (final ResponseHeader responseHeader : responseHeaders) {
        headers.add(responseHeader);
      }
      return headers;
    }

    public T headerOf(final String name) {
      final Iterator<T> iter = this.iterator();
      while (iter.hasNext()) {
        final T header = iter.next();
        if (header.name.equals(name)) {
          return header;
        }
      }
      return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends Header> Headers<T> empty() {
      return new Headers(0);
    }
    
    @SuppressWarnings("unchecked")
    public Headers<T> and(final Headers<? extends Header> headers) {
      super.addAll((Collection<T>) headers);
      return this;
    }
    
    @SuppressWarnings("unchecked")
    public Headers<T> and(final Header header) {
      add((T) header);
      return this;
    }
    
    @SuppressWarnings("unchecked")
    public Headers<T> and(final String name, final String value) {
      add((T) new Header(name, value));
      return this;
    }
    
    @Override
    public T set(int index, T element) {
      throw new UnsupportedOperationException();
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
