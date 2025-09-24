package com.moneyweather.util

import java.lang.reflect.InvocationTargetException

object ValueOf {
    // EnumClass -> FieldName -> FieldValue
    private val map: MutableMap<Class<out Enum<*>>, Map<String, Map<*, Enum<*>>>> = HashMap()
    fun <T : Enum<T>?> valueOf(enumType: Class<T>, comparable: Comparable<T>): T {
        for (v in values(enumType)) {
            if (comparable.compareTo(v) == 0) {
                return v
            }
        }
        throw IllegalArgumentException(
            "No enum const $enumType.$comparable"
        )
    }

    fun <T : Enum<T>, V> valueOf(
        enumType: Class<T>,
        fieldName: String,
        value: V
    ): T {
        var enumMap: Map<String, Map<*, Enum<*>>>? =
            map.get(enumType)
        synchronized(map) {
            if (enumMap == null) {
                enumMap =
                    LinkedHashMap()
                map[enumType] = enumMap!!
                val values = values(enumType)
                for (field in enumType.declaredFields) {
                    val fieldMap: MutableMap<Any, Enum<*>> =
                        LinkedHashMap()
                    (enumMap as LinkedHashMap<String, Map<*, Enum<*>>>)[field.name] = fieldMap
                    for (v in values) {
                        fieldMap[getFieldValue(enumType, v, fieldName)] = v
                    }
                }
            }
        }
        enumMap = map.get(enumType)
        checkNotNull(enumMap) { "Enum $enumType is not initialized" }
        val fieldMap = enumMap!![fieldName]!!
        requireNotNull(enumMap) { "Field $fieldName is not defined in Enum $enumType" }
        return getField<T, V>(fieldMap, value)
            ?: throw IllegalArgumentException("No enum const $enumType")
    }

    private fun <T, V> getField(fieldMap: Map<*, Enum<*>>, value: V): T? {
        return fieldMap[value] as T?
    }

    fun <T : Enum<T>?> values(enumType: Class<T>): Array<T> {
        var t: Throwable? = null
        t = try {
            val values = enumType.getMethod("values")
            return values.invoke(null) as Array<T>
        } catch (e: SecurityException) {
            e
        } catch (e: NoSuchMethodException) {
            e
        } catch (e: IllegalArgumentException) {
            e
        } catch (e: IllegalAccessException) {
            e
        } catch (e: InvocationTargetException) {
            e
        }
        throw IllegalArgumentException("No enum const $enumType", t)
    }

    private fun <T> getFieldValue(enumType: Class<*>, enumElem: T, fieldName: String): Any {
        var t: Throwable? = null
        try {
            val field = enumType.getDeclaredField(fieldName)
            field.isAccessible = true
            return field[enumElem]
        } catch (e: SecurityException) {
            t = e
        } catch (e: IllegalArgumentException) {
            t = e
        } catch (e: IllegalAccessException) {
            t = e
        } catch (e: NoSuchFieldException) {
            t = e
        }
        throw IllegalArgumentException("No enum const $enumType", t)
    }
}
