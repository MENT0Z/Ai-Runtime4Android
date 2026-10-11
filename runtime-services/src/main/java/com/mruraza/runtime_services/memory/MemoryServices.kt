package com.mruraza.runtime_services.memory

import com.mruraza.common.model.ConversationTurn
import com.mruraza.common.model.LongTermMemoEntity
import javax.inject.Inject

// now context manager have to call this class before building its context
class MemoryServices @Inject constructor(
    private val shortTermMemory: ShortTermMemory,
    private val longTermMemory: LongTermMemory
){
    suspend fun remember(memory: LongTermMemoEntity){

    }
    suspend fun recall(memoryId:String): LongTermMemoEntity?{
        TODO()
    }
    suspend fun getRecentConversation(
        sessionId: String,
        limit: Int
    ): List<ConversationTurn>{
        TODO()
    }
}