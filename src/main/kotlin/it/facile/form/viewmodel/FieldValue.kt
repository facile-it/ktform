package it.facile.form.viewmodel

import java.util.*

sealed class FieldValue {
    object Missing : FieldValue()
    class Text(val text: String = "") : FieldValue()
    class Bool(val bool: Boolean = false) : FieldValue()
    class DateValue(val date: Date?): FieldValue()
    class Object(val value: DescribableWithKey?) : FieldValue()

    override fun toString(): String = when (this) {
        is Text -> text.toString()
        is Bool -> bool.toString()
        is Object -> value?.describe() ?: NO_VALUE
        is DateValue -> date?.toString() ?: NO_VALUE
        is Missing -> "Missing value"
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else when(this) {
            is Missing -> true
            is Text -> other is Text && text.equals(other.text)
            is Bool -> other is Bool && bool == other.bool
            is DateValue -> other is Object && date?.equals(other.value) ?: other.value == null
            is Object -> other is Object && value?.equals(other.value) ?: other.value == null
        }
    }

    companion object {
        private val NO_VALUE: String = "No selected value"
    }
}