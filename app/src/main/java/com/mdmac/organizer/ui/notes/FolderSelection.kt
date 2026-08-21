package com.mdmac.organizer.ui.notes

sealed class FolderSelection {
    object All : FolderSelection()
    object Unfiled : FolderSelection()
    data class Specific(val folderId: Long, val name: String) : FolderSelection()
}
