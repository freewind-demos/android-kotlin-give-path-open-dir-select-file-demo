package com.example.givepathopendirselectfiledemo.domain.store

import com.example.givepathopendirselectfiledemo.domain.model.PathOpenResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PathOpenStore {

    private val mutableState = MutableStateFlow(PathOpenState())
    val state: StateFlow<PathOpenState> = mutableState.asStateFlow()

    fun setInputPath(inputPath: String) {
        mutableState.update { current ->
            current.copy(inputPath = inputPath)
        }
    }

    fun setOpenResult(result: PathOpenResult) {
        mutableState.update { current ->
            current.copy(
                status = result.status,
                resolvedUri = result.resolvedUri,
            )
        }
    }
}
