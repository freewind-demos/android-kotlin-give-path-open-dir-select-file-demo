package com.example.givepathopendirselectfiledemo.infra.system

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.example.givepathopendirselectfiledemo.domain.model.PathOpenRequest
import com.example.givepathopendirselectfiledemo.domain.model.PathOpenResult
import java.io.File

class PathOpenSystemApi(
    private val activity: Activity,
) {

    fun open(request: PathOpenRequest): PathOpenResult {
        val normalizedPath = normalizeAbsolutePath(request.absolutePath)
            ?: return PathOpenResult(status = "仅支持绝对路径")

        val targetFile = File(normalizedPath)
        val parentFile = targetFile.parentFile ?: if (targetFile.isDirectory) targetFile else null
            ?: return PathOpenResult(status = "该路径无父目录")

        val targetUri = buildExternalStorageDocumentUri(targetFile.absolutePath)
        val parentUri = buildExternalStorageDocumentUri(parentFile.absolutePath)
            ?: return PathOpenResult(status = "仅支持共享存储路径")

        val opened = openWithView(parentUri) || openWithDocumentTree(targetUri ?: parentUri)
        if (!opened) {
            return PathOpenResult(status = "系统无可处理文件管理器", resolvedUri = parentUri.toString())
        }

        val status = if (targetFile.isDirectory) {
            "已尝试打开目录"
        } else {
            "已尝试打开父目录；高亮选中取决系统文件管理器"
        }

        return PathOpenResult(
            status = status,
            resolvedUri = (targetUri ?: parentUri).toString(),
        )
    }

    private fun openWithView(uri: Uri): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return launchIntent(intent)
    }

    private fun openWithDocumentTree(initialUri: Uri): Boolean {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        return launchIntent(intent)
    }

    private fun launchIntent(intent: Intent): Boolean {
        return try {
            activity.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private fun normalizeAbsolutePath(rawPath: String): String? {
        val path = rawPath.trim().removePrefix("file://")
        if (!path.startsWith("/")) {
            return null
        }
        return path
    }

    private fun buildExternalStorageDocumentUri(absolutePath: String): Uri? {
        val (volumeId, relativePath) = splitSharedStoragePath(absolutePath) ?: return null
        val documentId = if (relativePath.isEmpty()) {
            "$volumeId:"
        } else {
            "$volumeId:$relativePath"
        }

        return DocumentsContract.buildDocumentUri(
            EXTERNAL_STORAGE_AUTHORITY,
            documentId,
        )
    }

    private fun splitSharedStoragePath(absolutePath: String): Pair<String, String>? {
        val normalizedPath = absolutePath.trimEnd('/').ifEmpty { "/" }
        val knownPrefixes = listOf(
            "/sdcard" to "primary",
            "/storage/emulated/0" to "primary",
            "/storage/self/primary" to "primary",
        )

        knownPrefixes.forEach { (prefix, volumeId) ->
            if (normalizedPath == prefix) {
                return volumeId to ""
            }
            if (normalizedPath.startsWith("$prefix/")) {
                return volumeId to normalizedPath.removePrefix("$prefix/")
            }
        }

        val storagePrefix = "/storage/"
        if (!normalizedPath.startsWith(storagePrefix)) {
            return null
        }

        val remaining = normalizedPath.removePrefix(storagePrefix)
        val volumeId = remaining.substringBefore('/')
        if (volumeId.isBlank() || volumeId == "emulated" || volumeId == "self") {
            return null
        }

        val relativePath = remaining.substringAfter('/', "")
        return volumeId to relativePath
    }

    private companion object {
        const val EXTERNAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents"
    }
}
