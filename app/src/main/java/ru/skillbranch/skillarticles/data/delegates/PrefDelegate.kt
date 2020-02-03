package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import java.lang.IllegalArgumentException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        return when(defaultValue) {
            is Boolean -> thisRef.preferences.getBoolean(property.name, defaultValue) as? T
            is String -> thisRef.preferences.getString(property.name, defaultValue) as? T
            is Int -> thisRef.preferences.getInt(property.name, defaultValue) as? T
            is Float -> thisRef.preferences.getFloat(property.name, defaultValue) as? T
            is Long -> thisRef.preferences.getLong(property.name, defaultValue) as? T
            else -> throw IllegalArgumentException("Unknown type")
        }
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        val edit = thisRef.preferences.edit()
        when(value) {
            is Boolean -> edit.putBoolean(property.name, value)
            is String -> edit.putString(property.name, value)
            is Int -> edit.putInt(property.name, value)
            is Float -> edit.putFloat(property.name, value)
            is Long -> edit.putLong(property.name, value)
            else -> throw IllegalArgumentException("Unknown type")
        }
        edit.apply()
    }

}