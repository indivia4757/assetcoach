package com.assetcoach.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assetcoach.data.db.entity.ConversationEntity
import com.assetcoach.data.db.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM conversations ORDER BY started_at DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createConversation(conversation: ConversationEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun appendMessage(message: MessageEntity): Long

    @Query("UPDATE conversations SET ended_at = :endedAt, summary = :summary WHERE id = :id")
    suspend fun closeConversation(id: Long, endedAt: Long, summary: String?)
}
