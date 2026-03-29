package org.emakotlin

internal object JsTruthiness {
    fun isTruthy(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is Number -> {
                val number = value.toDouble()
                number != 0.0 && !number.isNaN()
            }
            is String -> value.isNotEmpty()
            else -> true
        }
    }

    fun toNumber(value: Any?): Double {
        return when (value) {
            null -> 0.0
            is Number -> value.toDouble()
            is Boolean -> if (value) 1.0 else 0.0
            is String -> value.toDoubleOrNull() ?: Double.NaN
            else -> throw IllegalArgumentException("Cannot convert '$value' to number")
        }
    }

    fun toJsString(value: Any?): String {
        return when (value) {
            null -> "null"
            else -> value.toString()
        }
    }
}
