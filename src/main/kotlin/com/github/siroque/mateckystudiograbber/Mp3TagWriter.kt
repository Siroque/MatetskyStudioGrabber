package com.github.siroque.mateckystudiograbber

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagOptionSingleton
import org.jaudiotagger.tag.id3.ID3v24FieldKey
import org.jaudiotagger.tag.id3.ID3v24Frames.*
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.id3.framebody.*
import org.jaudiotagger.tag.reference.ID3V2Version
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

object Mp3TagWriter {
    private var logger: Logger = Logger.getLogger("com.github.siroque.mateckystudiograbber.Mp3TagWriter")

    fun writeTags(tag: TagModel, filePath: String): Boolean {
        try {
            val file = File(filePath)
            val mp3File = AudioFileIO.read(file) as MP3File
            TagOptionSingleton.getInstance().iD3V2Version = ID3V2Version.ID3_V24
            val id3v2Tag: ID3v24Tag = mp3File.tagAndConvertOrCreateAndSetDefault as ID3v24Tag

            tag.title?.let { addTagField(ID3v24FieldKey.TITLE, it, id3v2Tag) }
            tag.album?.let { addTagField(ID3v24FieldKey.ALBUM, it, id3v2Tag) }
            tag.artist?.let { addTagField(ID3v24FieldKey.ARTIST, it, id3v2Tag) }
            tag.comment?.let { addTagField(ID3v24FieldKey.COMMENT, it, id3v2Tag) }
            tag.artistUrl?.let { addArtistUrl(it, id3v2Tag) }
            tag.broadcastSourceUrl?.let { addBroadcastSourceUrl(it, id3v2Tag) }
            tag.broadcastSourceUrl?.let { addBroadcastSourceUrl(it, id3v2Tag) }
            tag.publisher?.let { addPublisher(it, id3v2Tag) }
            tag.publisherUrl?.let { addPublisherUrl(it, id3v2Tag) }
            tag.radioUrl?.let { addRadioUrl(it, id3v2Tag) }
            tag.audioFileUrl?.let { addAudioFileUrl(it, id3v2Tag) }
            tag.copyrightInfo?.let { addCopyrightInfo(it, id3v2Tag) }
            tag.language?.let { addLanguage(it, id3v2Tag) }
            tag.releaseTime?.let {
                addReleaseTime(it, id3v2Tag)
                addTagField(ID3v24FieldKey.YEAR, it.year.toString(), id3v2Tag)
            }
            tag.coverArt?.let {
                id3v2Tag.deleteField(FieldKey.COVER_ART)
                id3v2Tag.addField(id3v2Tag.createArtworkField(it, "image/jpeg"))
            }
            mp3File.tag = id3v2Tag
            mp3File.save()
            return true
        } catch (e: Exception){
            logger.severe("Failed to write mp3 tags for file at path $filePath for broadcast $tag with exception: ${e.message}")
            return false
        }
    }

    private fun addReleaseTime(date: LocalDateTime, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_ORIGINAL_RELEASE_TIME)
        val body = FrameBodyTDOR()
        body.text = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        frame.body = body
        tag.removeFrame(FRAME_ID_ORIGINAL_RELEASE_TIME)
        tag.addFrame(frame)
    }

    private fun addLanguage(lang: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_LANGUAGE)
        val body = FrameBodyTLAN()
        body.text = lang
        frame.body = body
        tag.removeFrame(FRAME_ID_LANGUAGE)
        tag.addFrame(frame)
    }

    private fun addCopyrightInfo(info: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_COPYRIGHTINFO)
        val body = FrameBodyTCOP()
        body.text = info
        frame.body = body
        tag.removeFrame(FRAME_ID_COPYRIGHTINFO)
        tag.addFrame(frame)
    }

    private fun addBroadcastSourceUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_SOURCE_WEB)
        val body = FrameBodyWOAS()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_SOURCE_WEB)
        tag.addFrame(frame)
    }

    private fun addAudioFileUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_FILE_WEB)
        val body = FrameBodyWOAF()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_FILE_WEB)
        tag.addFrame(frame)
    }

    private fun addRadioUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_OFFICIAL_RADIO)
        val body = FrameBodyWORS()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_OFFICIAL_RADIO)
        tag.addFrame(frame)
    }

    private fun addPublisherUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_PUBLISHERS)
        val body = FrameBodyWPUB()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_PUBLISHERS)
        tag.addFrame(frame)
    }

    private fun addPublisher(name: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_PUBLISHER)
        val body = FrameBodyTPUB()
        body.text = name
        frame.body = body
        tag.removeFrame(FRAME_ID_PUBLISHER)
        tag.addFrame(frame)
    }

    private fun addArtistUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_ARTIST_WEB)
        val body = FrameBodyWOAR()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_ARTIST_WEB)
        tag.addFrame(frame)
    }

    private fun addTagField(key: ID3v24FieldKey, value: String, tag: ID3v24Tag) {
        tag.deleteField(key)
        tag.addField(tag.createField(key, value))
    }
}

data class TagModel(
    val title: String?,
    val album: String?,
    val artist: String?,
    val comment: String?,
    val artistUrl: String?,
    val broadcastSourceUrl: String?,
    val publisher: String?,
    val publisherUrl: String?,
    val radioUrl: String?,
    val audioFileUrl: String?,
    val copyrightInfo: String?,
    val language: String?,
    val releaseTime: LocalDateTime?,
    val coverArt: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TagModel

        if (title != other.title) return false
        if (album != other.album) return false
        if (artist != other.artist) return false
        if (comment != other.comment) return false
        if (artistUrl != other.artistUrl) return false
        if (broadcastSourceUrl != other.broadcastSourceUrl) return false
        if (publisher != other.publisher) return false
        if (publisherUrl != other.publisherUrl) return false
        if (radioUrl != other.radioUrl) return false
        if (audioFileUrl != other.audioFileUrl) return false
        if (copyrightInfo != other.copyrightInfo) return false
        if (language != other.language) return false
        if (releaseTime != other.releaseTime) return false
        if (coverArt != null) {
            if (other.coverArt == null) return false
            if (!coverArt.contentEquals(other.coverArt)) return false
        } else if (other.coverArt != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (artistUrl?.hashCode() ?: 0)
        result = 31 * result + (broadcastSourceUrl?.hashCode() ?: 0)
        result = 31 * result + (publisher?.hashCode() ?: 0)
        result = 31 * result + (publisherUrl?.hashCode() ?: 0)
        result = 31 * result + (radioUrl?.hashCode() ?: 0)
        result = 31 * result + (audioFileUrl?.hashCode() ?: 0)
        result = 31 * result + (copyrightInfo?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (releaseTime?.hashCode() ?: 0)
        result = 31 * result + (coverArt?.contentHashCode() ?: 0)
        return result
    }
}