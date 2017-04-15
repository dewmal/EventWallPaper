package com.juniperphoton.myersplash.utils

import java.io.File

fun java.io.File.getFolderLengthInMb(): Long {
    if (!this.exists()) return 0
    return this.listFiles()
            .map(File::length)
            .sum() / 1024 / 1024
}