    fun attachmentFile(
        libraryId: LibraryIdentifier,
        key: String,
        filename: String,
    ): File {
        val defaultFile = File(getRootDirectory(), "downloads/${libraryId.folderName}/$key")
        val folderPath = defaultFile
        folderPath.mkdirs()

        val file = File(folderPath, filename)
        if (file.exists() && !file.isFile) {
            //We must ensure attachmentFile points to an actual file, not a folder
            file.delete()
        }

        val item = ZoteroItem(
            id = key,
            title = filename.substringBeforeLast('.', filename.ifBlank { "attachment" }),
            collectionName = libraryId.folderName,
        )

        val attachment = AttachmentMetadata(
            id = key,
            mimeType = if (filename.endsWith(".pdf", ignoreCase = true)) {
                "application/pdf"
            } else {
                "application/octet-stream"
            },
            title = filename,
            sourcePath = file.absolutePath,
        )

        return pluginManager.resolvePreferredAttachmentFile(
            item = item,
            attachment = attachment,
            defaultFile = file,
        )
    }
