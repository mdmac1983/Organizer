package com.mdmac.organizer.data.notes

import org.json.JSONArray
import org.json.JSONObject

enum class NoteBlockType { TEXT, CHECKLIST_ITEM }

data class NoteBlock(
    var type: NoteBlockType,
    var text: String = "",
    var checked: Boolean = false
)

// Notes are stored as a JSON array of blocks in Note.contentJson.
object NoteContentSerializer {
    fun serialize(blocks: List<NoteBlock>): String {
        val array = JSONArray()
        blocks.forEach { block ->
            array.put(JSONObject().apply {
                put("type", block.type.name)
                put("text", block.text)
                put("checked", block.checked)
            })
        }
        return array.toString()
    }

    fun deserialize(json: String): MutableList<NoteBlock> {
        if (json.isBlank()) return mutableListOf()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                NoteBlock(
                    type = NoteBlockType.valueOf(obj.optString("type", "TEXT")),
                    text = obj.optString("text", ""),
                    checked = obj.optBoolean("checked", false)
                )
            }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun previewText(json: String): String =
        deserialize(json).joinToString(" ") { it.text }.take(80)
}
