package com.autentia.tnt.binnacle.core.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.EnumSet
import java.util.GregorianCalendar

fun LocalDate.isWeekend(): Boolean {
    val weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    return weekend.contains(this.dayOfWeek)
}

fun LocalDate.isHoliday(holidays: List<LocalDate>): Boolean {
    return holidays.any { it == this }
}


class LocalDateIterator(val start: LocalDate, val endInclusive: LocalDate) : Iterator<LocalDate> {
    private var next = start
    private val finalElement = endInclusive
    private var hasNext = !next.isAfter(endInclusive)
    override fun hasNext(): Boolean = hasNext

    override fun next(): LocalDate {
        val value = next

        if (value == finalElement) {
            hasNext = false
        } else {
            next = next.plusDays(1)
        }

        return value
    }
}

class LocalDateRange(
    override val start: LocalDate,
    override val endInclusive: LocalDate
) : ClosedRange<LocalDate>, Iterable<LocalDate> {

    override fun iterator(): Iterator<LocalDate> = LocalDateIterator(start, endInclusive).iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalDateRange

        if (start != other.start) return false
        if (endInclusive != other.endInclusive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + endInclusive.hashCode()
        return result
    }

    override fun toString(): String = "$start..$endInclusive"

}

fun LocalDate.myDatesUntil(endInclusive: LocalDate) = LocalDateRange(this, endInclusive)

fun Date.takeYear(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this

    return calendar.get(Calendar.YEAR)
}

fun Date.takeMonth(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this

    return calendar.get(Calendar.MONTH) + 1
}

fun ClosedRange<LocalDateTime>.overlaps(other: ClosedRange<LocalDateTime>): Boolean {
    return start < other.endInclusive && endInclusive > other.start
}

fun minDate(start: LocalDate, end: LocalDate) = listOf(start, end).min()
fun maxDate(start: LocalDate, end: LocalDate) = listOf(start, end).max()
