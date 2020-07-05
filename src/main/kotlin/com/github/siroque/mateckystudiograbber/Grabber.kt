package com.github.siroque.mateckystudiograbber

object Grabber {
    @JvmStatic
    fun main(args: Array<String>) {
        val service = BroadcastService("D:\\Multimedia\\Vladimir Matecky Studio")
        service.fetchBroadcasts()
    }
}