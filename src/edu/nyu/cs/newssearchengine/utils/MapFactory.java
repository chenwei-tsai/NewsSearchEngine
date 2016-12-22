package edu.nyu.cs.newssearchengine.utils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MapFactory is a mechanism for specifying what kind of map is to be used
 * by some object. For example, if you want a Counter which is backed by an
 * IdentityHashMap instead of the defaul HashMap, you can pass in an
 * IdentityHashMapFactory.
 */

public abstract class MapFactory<K, V> implements Serializable {
	private static final long serialVersionUID = 5724671156522771657L;

  public static class HashMapFactory<K, V> extends MapFactory<K, V> {
    private static final long serialVersionUID = 1L;

    public Map<K, V> buildMap() {
      return new HashMap<K, V>();
    }
  }

  public static class ConcurrentHashMapFactory<K, V> extends MapFactory<K, V> {
    private static final long serialVersionUID = 1L;

    public Map<K, V> buildMap() {
      return new ConcurrentHashMap<>();
    }
  }

	public static class IdentityHashMapFactory<K, V> extends MapFactory<K, V> {
		private static final long serialVersionUID = 1L;

		public Map<K, V> buildMap() {
			return new IdentityHashMap<K, V>();
		}
	}

	public static class TreeMapFactory<K, V> extends MapFactory<K, V> {
		private static final long serialVersionUID = 1L;

		public Map<K, V> buildMap() {
			return new TreeMap<K, V>();
		}
	}

	public static class WeakHashMapFactory<K, V> extends MapFactory<K, V> {
		private static final long serialVersionUID = 1L;

		public Map<K, V> buildMap() {
			return new WeakHashMap<K, V>();
		}
	}

	public abstract Map<K, V> buildMap();
}
