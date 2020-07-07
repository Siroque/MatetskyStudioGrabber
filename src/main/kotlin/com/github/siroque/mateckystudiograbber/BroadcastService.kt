package com.github.siroque.mateckystudiograbber

import com.github.siroque.mateckystudiograbber.Mp3TagWriter.writeTags
import com.github.siroque.mateckystudiograbber.utils.DateTimeUtils
import com.github.siroque.mateckystudiograbber.utils.FileUtilities
import com.github.siroque.mateckystudiograbber.utils.rangeTo
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.trimSubstring
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.logging.Logger
import kotlin.math.*

class BroadcastService(
    val outputPath: String
) {
    private var imageStream: ByteArray? = null

    fun fetchBroadcasts() {
        FileUtilities.makeSureDirectoryExists(outputPath)
        val lastSaturdayDate = DateTimeUtils.getLastDayOfWeekDate(DayOfWeek.SATURDAY)
        imageStream = fetchImageStream("https://cdn-st2.rtr-vesti.ru/vh/pictures/q/717/169.jpg", client)
        val dayCount = ChronoUnit.DAYS.between(lowerDateRangeBoundary, lastSaturdayDate)
        var progressCounter = 0
        for ((counter, date) in (lastSaturdayDate..lowerDateRangeBoundary).withIndex()) {
            val currentProgress = floor(counter / (dayCount / 100.0)).toInt()
            if (progressCounter != currentProgress) {
                progressCounter = currentProgress
                println("Progress: $currentProgress% ($date)")
            }
            if(!broadcastsAtDateAlreadySnatched(date)) extractBroadcast(date)
        }
    }

    private fun broadcastsAtDateAlreadySnatched(date: LocalDate): Boolean {
        return try {
            val result = File(outputPath)
                    .listFiles()
                    ?.filter{ !it.isDirectory }
                    ?.any {
                        it.name.contains(date.format(filePrefixDateFormatter))
                    }
            result ?: false
        } catch (ex: Exception){
            false
        }
    }

    fun extractBroadcast(date: LocalDate) {
        val url = schedulerPath + "/" + date.format(schedulerPathDateFormatter)
        Jsoup.connect(url).get().run {
            if (select(".b-schedule__error").text().isNotEmpty()) return
            try {
                select(".b-schedule__list-item").forEach { element ->
                    val time: String = element.select(".b-schedule__list-item__inner .b-schedule__list-item__time").text()
                    val startTime = time.split("-")[0].trim().trimSubstring(0,2)
                    val endTime = time.split("-")[1].trim().trimSubstring(0,2)

                    val descriptionColumnAnchor = element.select(".b-schedule__list-item__inner .b-schedule__list-item__info").first()
                    val title = descriptionColumnAnchor.select(".b-schedule__list-item__header .b-schedule__list-item__link").text()
                    val streamUrl = descriptionColumnAnchor.select(".b-schedule__list-item__header .b-schedule__list-item__listen").attr("data-url")
                    val description = descriptionColumnAnchor.select(".b-schedule__list-item__description").text()
                    if (streamUrl.isNotEmpty()) {
                        fetchBroadcast(Broadcast(title, "Студия Владимира Матецкого", url, streamUrl, description, date, startTime, endTime))
                    } else {
                        println("No error page but no stream URL: $url")
                    }
                }
            } catch (e:Exception){
                print("No error page but null pointer for property: $url")
            }
        }
    }

    private fun fetchBroadcast(broadcast: Broadcast) {
        val targetMp3Path = filePathForBroadcast(broadcast)
        println("New Broadcast found: $broadcast")
        val request = Request.Builder().url(broadcast.streamUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) logger.severe("     Failed to fetch audio file for broadcast $broadcast")
            else {
                if (writeBroadcastToFile(response.body!!.byteStream(), targetMp3Path)){
                    if(writeTags(broadcastToTagModel(broadcast), targetMp3Path)) {
                        println("File saved at path: $targetMp3Path")
                    }
                }
            }
        }
    }

    private fun broadcastToTagModel(broadcast: Broadcast): TagModel{
        return TagModel(
                broadcast.streamTitle,
                broadcast.broadcastTitle,
                "Владимир Матецкий",
                broadcast.description,
                broadcast.broadcastPageUrl,
                broadcast.streamUrl,
                "Государственная радиовещательная компания «Маяк»",
                "https://radiomayak.ru/",
                "https://radiomayak.ru/",
                broadcast.streamUrl,
                "© Государственная радиовещательная компания «Маяк»",
                "Russian",
                broadcast.date.atTime(broadcast.startTime?.toInt() ?: 0, 0),
                imageStream
        )
    }

    private fun filePathForBroadcast(broadcast: Broadcast): String {
        val filenamePrefix = "${broadcast.date.format(filePrefixDateFormatter)}_${broadcast.startTime}-${broadcast.endTime}"
        val legalFileName= ("$filenamePrefix ${broadcast.streamTitle}").replace(Regex("[:\\\\/*\"?|<>]"), "_")
        return "${outputPath}/${legalFileName}.mp3"
    }

    private fun writeBroadcastToFile(inputStream: InputStream, outputFilePath: String): Boolean {
        val outputFile = FileUtilities.flushFile(outputFilePath)
        logger.info("   Going to store audio file at path: ${outputFile.absolutePath}")
        val outputStream = outputFile.outputStream()

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return outputFile.exists()
    }

    private fun fetchImageStream(imgUrl: String?, client: OkHttpClient): ByteArray? {
        imgUrl?.let{
            client.newCall(Request.Builder().url(imgUrl).build()).execute().use { response ->
                return if (!response.isSuccessful) null
                else response.body!!.byteStream().readBytes()
            }
        }
        return null
    }

    private companion object {
        val lowerDateRangeBoundary = LocalDate.of(2012, 12,31)
        val schedulerPath = "https://radiomayak.ru/shows/show/id/59088/date"
        val schedulerPathDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale("ru"))
        val filePrefixDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale("ru"))
        val logger: Logger = Logger.getLogger("com.github.siroque.mateckystudiograbber.BroadcastService")
        val client = OkHttpClient()
    }
}

data class Broadcast(
    val streamTitle: String,
    val broadcastTitle: String,
    val broadcastPageUrl: String,
    val streamUrl: String,
    val description: String,
    val date: LocalDate,
    val startTime: String?,
    val endTime: String?
)