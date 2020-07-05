package com.github.siroque.mateckystudiograbber.utils

import org.apache.commons.io.FileUtils
import java.io.File

object FileUtilities {
    fun makeSureDirectoryExists(localDirectoryPath: String){
        val outputDirectory = File(localDirectoryPath)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
    }

    fun flushFile(filePath: String): File {
        val outputFile = File(filePath)
        if (outputFile.exists()) {
            outputFile.delete()
        }
        outputFile.createNewFile()
        return outputFile
    }

    fun dropDirectoryContent(directoryPath: String) {
        val dir = File(directoryPath)
        if (dir.exists() ){
            FileUtils.cleanDirectory(dir)
        }
    }
}