package org.zotero.android.plugins

import android.content.Context
import androidx.annotation.VisibleForTesting
import java.util.LinkedHashMap

class PluginManager(
    private val context: Context,
) {
    private val attachmentStoragePlugins = LinkedHashMap<String, AttachmentStoragePlugin>()

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

    @VisibleForTesting
    fun isRegistered(pluginId: String): Boolean = attachmentStoragePlugins.containsKey(pluginId)
}
