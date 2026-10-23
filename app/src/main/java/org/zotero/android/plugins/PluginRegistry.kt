package org.zotero.android.plugins

import android.content.Context
import android.os.Environment
import java.io.File
import java.util.Locale

class LocalPdfStoragePlugin(
    private val appContext: Context,
) : AttachmentStoragePlugin {
    override val id: String = "local-pdf-storage"
    override val name: String = "Local PDF Storage"
    override val version: String = "1.0.0"
    override val type: PluginType = PluginType.ATTACHMENT_STORAGE
    override val description: String =
        "Stores Zotero PDFs in a local Android folder and remembers the location for later access."

    override fun initialize(context: Context) {
        // no-op for the default local implementation
    }

    override fun shutdown() {
        // no-op for the default local implementation
    }

    override fun supports(item: ZoteroItem, attachment: AttachmentMetadata): Boolean {
        return attachment.mimeType.equals("application/pdf", ignoreCase = true)
            || attachment.title.endsWith(".pdf", ignoreCase = true)
    }

    override fun resolveDestination(
        item: ZoteroItem,
        attachment: AttachmentMetadata,
        settings: PluginSettings,
    ): StorageTarget {
        val baseDirectory = settings.getString("baseDirectory")
            ?: defaultDirectory().absolutePath

        val directory = File(baseDirectory)
        val safeCollection = sanitizeSegment(item.collectionName)
        val safeTitle = sanitizeSegment(attachment.title.removeSuffix(".pdf"))
        val yearSegment = item.year?.toString() ?: "unsorted"

        val targetDirectory = File(
            directory,
            listOfNotNull(
                safeCollection,
                yearSegment,
                safeTitle,
            ).joinToString(File.separator),
        )

        val fileName = if (attachment.title.endsWith(".pdf", ignoreCase = true)) {
            attachment.title
        } else {
            "${attachment.title}.pdf"
        }

        return StorageTarget(
            providerId = id,
            scheme = "file",
            directoryPath = targetDirectory.absolutePath,
            fileName = fileName,
            relativePath = targetDirectory.relativeTo(directory).path
                .ifBlank { "attachments" },
        )
    }

    override fun moveToTarget(
        sourceFile: File,
        target: StorageTarget,
    ): Result<StorageTarget> {
        return try {
            val destinationFile = File(target.fullPath)
            val destinationDirectory = destinationFile.parentFile
            if (destinationDirectory != null && !destinationDirectory.exists()) {
                destinationDirectory.mkdirs()
            }

            if (!sourceFile.exists()) {
                return Result.failure(IllegalStateException("Source file does not exist: ${sourceFile.absolutePath}"))
            }

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            sourceFile.copyTo(destinationFile, overwrite = true)
            Result.success(target)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override fun restore(target: StorageTarget): Result<File> {
        return try {
            val restoredFile = File(target.fullPath)
            if (!restoredFile.exists()) {
                return Result.failure(IllegalStateException("Stored PDF not found: ${restoredFile.absolutePath}"))
            }
            Result.success(restoredFile)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun defaultDirectory(): File {
        val externalFilesRoot = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: appContext.filesDir

        return File(externalFilesRoot, "Zotero/Attachments")
    }

    private fun sanitizeSegment(value: String): String {
        val sanitized = value
            .trim()
            .replace("\\".toRegex(), "-")
            .replace("/".toRegex(), "-")
            .replace("[\\s]+".toRegex(), "-")
            .replace("[^A-Za-z0-9._-]".toRegex(), "-")
            .lowercase(Locale.getDefault())

        return sanitized.ifBlank { "untitled" }
    }
}
