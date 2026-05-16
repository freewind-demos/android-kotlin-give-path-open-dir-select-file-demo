package com.example.givepathopendirselectfiledemo.domain.store

data class PathOpenState(
    val inputPath: String = "/sdcard/Download/test.txt",
    val status: String = "输入绝对路径，点按钮打开父目录",
    val resolvedUri: String = "",
)
