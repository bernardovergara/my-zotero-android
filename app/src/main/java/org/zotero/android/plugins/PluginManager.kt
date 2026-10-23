package org.zotero.android.plugins

import android.content.Context
import java.io.File

enum class PluginType {
    TRANSLATOR,
    STYLE,
    ATTACHMENT_STORAGE,
    EXTENSION,
}

data class PluginSettings(
    private val values: MutableMap<String, String> = linkedMapOf(),
) {
    fun setString(key: String, value: String) {
        values[key] = value
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return values[key] ?: defaultValue
    }

    fun setBoolean(key: String, value: Boolean) {
        values[key] = value.toString()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return values[key]?.toBooleanStrictOrNull() ?: defaultValue
    }

    fun setLong(key: String, value: Long) {
        values[key] = value.toString()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return values[key]?.toLongOrNull() ?: defaultValue
    }

    fun asMap(): Map<String, String> = values.toMap()
}

data class ZoteroItem(
    val id: String,
    val title: String,
    val collectionName: String = "Unfiled",
    val year: Int? = null,
)

data class AttachmentMetadata(
    val id: String,
    val mimeType: String,
    val title: String,
    val sourcePath: String? = null,
)

data class StorageTarget(
    val providerId: String,
    val scheme: String,
    val directoryPath: String,
    val fileName: String,
    val relativePath: String? = null,
) {
    val fullPath: String
        get() = if (directoryPath.endsWith("/")) {
            directoryPath + fileName
        } else {
            "$directoryPath/$fileName"
        }
}

interface ZoteroPlugin {
    val id: String
    val name: String
    val version: String
    val type: PluginType
    val description: String

    fun initialize(context: Context)
    fun shutdown()
}

interface AttachmentStoragePlugin : ZoteroPlugin {
    fun supports(item: ZoteroItem, attachment: AttachmentMetadata): Boolean

    fun resolveDestination(
        item: ZoteroItem,
        attachment: AttachmentMetadata,
        settings: PluginSettings,
    ): StorageTarget

    fun moveToTarget(
        sourceFile: File,
        target: StorageTarget,
    ): Result<StorageTarget>

    fun restore(target: StorageTarget): Result<File>
}
