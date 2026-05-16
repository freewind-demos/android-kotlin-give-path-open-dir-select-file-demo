package com.example.givepathopendirselectfiledemo.domain.handler

import com.example.givepathopendirselectfiledemo.domain.model.PathOpenRequest
import com.example.givepathopendirselectfiledemo.domain.model.PathOpenResult
import com.example.givepathopendirselectfiledemo.domain.store.PathOpenStore
import com.example.givepathopendirselectfiledemo.infra.system.PathOpenSystemApi

class PathOpenHandler(
    private val store: PathOpenStore,
    private val pathOpenSystemApi: PathOpenSystemApi,
) {

    fun onPathInputChanged(inputPath: String) {
        store.setInputPath(inputPath)
    }

    fun onOpenParentDirClick() {
        val inputPath = store.state.value.inputPath.trim()
        if (inputPath.isEmpty()) {
            store.setOpenResult(PathOpenResult(status = "路径为空"))
            return
        }

        val result = pathOpenSystemApi.open(
            request = PathOpenRequest(absolutePath = inputPath),
        )
        store.setOpenResult(result)
    }
}
