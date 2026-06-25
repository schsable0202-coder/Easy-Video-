package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoProjectDao {
    @Query("SELECT * FROM video_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<VideoProject>>

    @Query("SELECT * FROM video_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): VideoProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: VideoProject): Long

    @Update
    suspend fun updateProject(project: VideoProject)

    @Delete
    suspend fun deleteProject(project: VideoProject)

    @Query("DELETE FROM video_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}
