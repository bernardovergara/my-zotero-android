package org.zotero.android.plugins

import android.content.Context
import androidx.annotation.VisibleForTesting
import java.io.File
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginManager @Inject constructor(
    private val context: Context,
) {
    private val attachmentStoragePlugins = LinkedHashMap<String, AttachmentStoragePlugin>()

    init {
        PluginRegistry.registerDefaultPlugins(context, this)
    }

    fun register(plugin: AttachmentStoragePlugin) {
        plugin.initialize(context)
        attachmentStoragePlugins[plugin.id] = plugin
    }

    fun unregister(pluginId: String) {
        val plugin = attachmentStoragePlugins.remove(pluginId)
        plugin?.shutdown()
    }

    fun getAttachmentStoragePlugins(): List<AttachmentStoragePlugin> =
        attachmentStoragePlugins.values.toList()

    fun resolveAttachmentStoragePlugin(
        item: ZoteroItem,
        attachment: AttachmentMetadata,
    ): AttachmentStoragePlugin? {
        return attachmentStoragePlugins.values.firstOrNull {
            it.supports(item, attachment)
        }
    }

    fun resolvePreferredAttachmentFile(
        item: ZoteroItem,
        attachment: AttachmentMetadata,
        defaultFile: File,
    ): File {
        val plugin = resolveAttachmentStoragePlugin(item, attachment)
            ?: return defaultFile

        val settings = PluginSettings().apply {
            setString(
                "baseDirectory",
                defaultFile.parentFile?.absolutePath ?: defaultFile.absolutePath,
            )
        }

        val target = plugin.resolveDestination(item, attachment, settings)
        val targetDirectory = File(target.directoryPath)
        targetDirectory.mkdirs()

        return File(target.fullPath)
    }

    @VisibleForTesting
    fun isRegistered(pluginId: String): Boolean = attachmentStoragePlugins.containsKey(pluginId)
}
