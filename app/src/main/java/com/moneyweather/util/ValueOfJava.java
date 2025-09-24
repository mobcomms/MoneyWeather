package com.moneyweather.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValueOfJava {
    // EnumClass -> FieldName -> FieldValue
    private final static Map<Class<? extends Enum<?>>, Map<String, Map<?, Enum<?>>>> map =
            new HashMap<Class<? extends Enum<?>>, Map<String, Map<?, Enum<?>>>>();

    public static <T extends Enum<T>> T valueOf(Class<T> enumType, Comparable<T> comparable) {
        for (T v : values(enumType)) {
            if (comparable.compareTo(v) == 0) {
                return v;
            }
        }
        throw new IllegalArgumentException(
                "No enum const " + enumType + "." + comparable);
    }


    public static <T extends Enum<T>, V> T valueOf(Class<T> enumType, String fieldName, V value) {
        Map<String, Map<?, Enum<?>>> enumMap = map.get(enumType);
        synchronized(map) {
            if (enumMap == null) {
                enumMap = new LinkedHashMap<String, Map<?, Enum<?>>>();
                map.put(enumType, enumMap);
                T[] values = values(enumType);
                for (Field field : enumType.getDeclaredFields()) {
                    Map<Object, Enum<?>> fieldMap = new LinkedHashMap<Object, Enum<?>>();
                    enumMap.put(field.getName(), fieldMap);
                    for (T v : values) {
                        fieldMap.put(getFieldValue(enumType, v, fieldName), v);
                    }
                }
            }
        }

        enumMap = map.get(enumType);
        if (enumMap == null) {
            throw new IllegalStateException("Enum " + enumType + " is not initialized");
        }
        Map<?, Enum<?>> fieldMap = enumMap.get(fieldName);
        if (enumMap == null) {
            throw new IllegalArgumentException("Field " + fieldName + " is not defined in Enum " + enumType);
        }
        T v = getField(fieldMap, value);
        if (v == null) {
            throw new IllegalArgumentException("No enum const " + enumType);
        }

        return v;
    }


    @SuppressWarnings("unchecked")
    private static <T, V> T getField(Map<?, Enum<?>> fieldMap, V value) {
        return (T)fieldMap.get(value);
    }


    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T[] values(Class<T> enumType) {
        Throwable t = null;
        try {
            Method values = enumType.getMethod("values");
            return (T[])values.invoke(null);
        } catch (SecurityException e) {
            t = e;
        } catch (NoSuchMethodException e) {
            t = e;
        } catch (IllegalArgumentException e) {
            t = e;
        } catch (IllegalAccessException e) {
            t = e;
        } catch (InvocationTargetException e) {
            t = e;
        }

        throw new IllegalArgumentException("No enum const " + enumType, t);
    }


    private static <T> Object getFieldValue(Class<?> enumType, T enumElem, String fieldName) {
        Throwable t = null;
        try {
            Field field = enumType.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(enumElem);
        } catch (SecurityException e) {
            t = e;
        } catch (IllegalArgumentException e) {
            t = e;
        } catch (IllegalAccessException e) {
            t = e;
        } catch (NoSuchFieldException e) {
            t = e;
        }

        throw new IllegalArgumentException("No enum const " + enumType, t);
    }
}