// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.util.ArrayList;
import java.util.Collection;
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

    @SuppressWarnings("rawtypes")
    private static Headers empty = new Headers(0);
    
    @SuppressWarnings("unchecked")
    public static <T extends Header> Headers<T> empty() {
      return (Headers<T>) empty;
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
      throw new UnsupportedOperationException();
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

    Headers(final int initialCapactiy) {
      super(initialCapactiy);
    }
  }
}
