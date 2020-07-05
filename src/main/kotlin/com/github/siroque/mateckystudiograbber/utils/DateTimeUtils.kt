package com.github.siroque.mateckystudiograbber.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.logging.Logger

object DateTimeUtils {
    private var logger: Logger = Logger.getLogger("com.github.siroque.silverraingrabber.utils.DateTimeUtils")

    fun reformatDateString(date: String, inputFormat: String, outputFormat: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern(inputFormat, Locale("ru"))
        val outputFormatter = DateTimeFormatter.ofPattern(outputFormat, Locale("ru"))
        return try {
            LocalDateTime.parse(date.toLowerCase(), inputFormatter).format(outputFormatter)
        } catch (e: Exception){
            logger.info("Failed to reformat datestring $date with exception: ${e.message}")
            date
        }
    }

    fun getLastDayOfWeekDate(weekDay: DayOfWeek): LocalDate {
        return LocalDate
            .now()
            .with(
                TemporalAdjusters.previousOrSame(weekDay)
            )
    }
}