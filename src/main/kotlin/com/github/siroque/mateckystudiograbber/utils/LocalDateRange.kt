package com.github.siroque.mateckystudiograbber.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class LocalDateRange(override val start: LocalDate, override val endInclusive: LocalDate)
    : ClosedRange<LocalDate>, Iterable<LocalDate> {
    override fun iterator(): Iterator<LocalDate> = DateIterator(start, endInclusive)
}

class DateIterator(start: LocalDate, private val endInclusive: LocalDate)
    : Iterator<LocalDate> {
    private var step: Long = 1
    private var current = start.plusDays(step)

    override fun hasNext() = endInclusive <= current && ChronoUnit.DAYS.between(endInclusive, current) >= step

    override fun next(): LocalDate {
        current = current.minusDays(step)
        return current
    }
}

operator fun LocalDate.rangeTo(other: LocalDate): LocalDateRange = LocalDateRange(this, other)