package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssistantMessage
import com.example.data.model.HealthNotebook
import com.example.data.model.LabTest
import com.example.data.model.NotebookEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthNotebookDao {
    @Query("SELECT * FROM health_notebooks ORDER BY id ASC")
    fun getAllNotebooks(): Flow<List<HealthNotebook>>

    @Query("SELECT * FROM health_notebooks WHERE id = :notebookId")
    suspend fun getNotebookById(notebookId: Long): HealthNotebook?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotebook(notebook: HealthNotebook): Long

    @Update
    suspend fun updateNotebook(notebook: HealthNotebook)

    @Delete
    suspend fun deleteNotebook(notebook: HealthNotebook)
}

@Dao
interface NotebookEntryDao {
    @Query("SELECT * FROM notebook_entries WHERE notebookId = :notebookId ORDER BY timestamp DESC")
    fun getEntriesForNotebook(notebookId: Long): Flow<List<NotebookEntry>>

    @Query("SELECT * FROM notebook_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<NotebookEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: NotebookEntry): Long

    @Delete
    suspend fun deleteEntry(entry: NotebookEntry)

    @Query("DELETE FROM notebook_entries WHERE notebookId = :notebookId")
    suspend fun deleteEntriesForNotebook(notebookId: Long)
}

@Dao
interface LabTestDao {
    @Query("SELECT * FROM lab_tests ORDER BY timestamp DESC")
    fun getAllLabTests(): Flow<List<LabTest>>

    @Query("SELECT * FROM lab_tests WHERE id = :testId")
    suspend fun getLabTestById(testId: Long): LabTest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabTest(labTest: LabTest): Long

    @Delete
    suspend fun deleteLabTest(labTest: LabTest)
}

@Dao
interface AssistantMessageDao {
    @Query("SELECT * FROM assistant_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AssistantMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AssistantMessage): Long

    @Query("DELETE FROM assistant_messages")
    suspend fun clearChat()
}
