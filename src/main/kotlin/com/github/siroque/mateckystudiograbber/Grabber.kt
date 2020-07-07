package com.github.siroque.mateckystudiograbber

import com.github.siroque.mateckystudiograbber.utils.FileUtilities.validateDirectoryPath

object Grabber {
    @JvmStatic
    fun main(args: Array<String>) {
        val service = BroadcastService(validateDirectoryPath("D:\\Multimedia\\Vladimir Matecky Studio"))
        service.fetchBroadcasts()
    }
}