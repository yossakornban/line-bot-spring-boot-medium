package com.pico.communication.utils;


import java.util.Collection;
import java.util.Map;

import org.springframework.util.StringUtils;

public class BeanUtils {

	public static boolean isNull(Object object) {
		return object == null;
	}

	public static boolean isNotNull(Object object) {
		return !BeanUtils.isNull(object);
	}

	public static boolean isEmpty(String string) {
		string = StringUtils.trimWhitespace(string);
		return BeanUtils.isNull(string) || string.isEmpty();
	}

	public static <E> boolean isEmpty(E[] array) {
		return BeanUtils.isNull(array) || array.length == 0;
	}

	public static <E> boolean isEmpty(Collection<E> collection) {
		return BeanUtils.isNull(collection) || collection.isEmpty();
	}

	public static <K, V> boolean isEmpty(Map<K, V> map) {
		return BeanUtils.isNull(map) || map.isEmpty();
	}

	public static <E> boolean isNotEmpty(E[] array) {
		return !BeanUtils.isEmpty(array);
	}

	public static boolean isNotEmpty(String string) {
		return !BeanUtils.isEmpty(string);
	}

	public static <E> boolean isNotEmpty(Collection<E> collection) {
		return !BeanUtils.isEmpty(collection);
	}

	public static <K, V> boolean isNotEmpty(Map<K, V> map) {
		return !BeanUtils.isEmpty(map);
	}

}
