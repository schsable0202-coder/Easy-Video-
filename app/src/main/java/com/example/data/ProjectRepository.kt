package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val dao: VideoProjectDao) {
    val allProjects: Flow<List<VideoProject>> = dao.getAllProjects()

    suspend fun getProjectById(id: Int): VideoProject? {
        return dao.getProjectById(id)
    }

    suspend fun insertProject(project: VideoProject): Int {
        return dao.insertProject(project).toInt()
    }

    suspend fun updateProject(project: VideoProject) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(project: VideoProject) {
        dao.deleteProject(project)
    }

    suspend fun deleteProjectById(id: Int) {
        dao.deleteProjectById(id)
    }
}
