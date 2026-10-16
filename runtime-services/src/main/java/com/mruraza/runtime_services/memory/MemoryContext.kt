package com.mruraza.runtime_services.memory

import com.mruraza.common.model.ConversationTurn
import com.mruraza.common.model.LongTermMemoEntity

/** Everything the ContextManager needs from memory for one prompt. */
data class MemoryContext(
    val recentTurns: List<ConversationTurn>,
    val relevantMemories: List<LongTermMemoEntity>,
    val importantMemories: List<LongTermMemoEntity>
) {
    /** Long-term memories (relevant first, then important), de-duplicated. */
    val allLongTerm: List<LongTermMemoEntity>
        get() = (relevantMemories + importantMemories).distinctBy { it.memoryId }

    /** Ready-to-paste text block for the system prompt. Empty string if nothing to add. */
    fun longTermAsPrompt(maxChars: Int = 2000): String {
        if (allLongTerm.isEmpty()) return ""
        val sb = StringBuilder("Things you remember about the user:\n")
        for (m in allLongTerm) {
            val line = "- ${m.content.trim()}\n"
            if (sb.length + line.length > maxChars) break
            sb.append(line)
        }
        return sb.toString().trimEnd()
    }
}