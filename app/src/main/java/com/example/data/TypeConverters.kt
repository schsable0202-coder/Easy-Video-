package com.example.data

import androidx.room.TypeConverter
import com.example.domain.ThumbnailOption
import com.example.domain.VideoScene
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val sceneListType = Types.newParameterizedType(List::class.java, VideoScene::class.java)
    private val thumbnailListType = Types.newParameterizedType(List::class.java, ThumbnailOption::class.java)
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)

    @TypeConverter
    fun fromSceneList(value: List<VideoScene>?): String? {
        if (value == null) return null
        return moshi.adapter<List<VideoScene>>(sceneListType).toJson(value)
    }

    @TypeConverter
    fun toSceneList(value: String?): List<VideoScene>? {
        if (value.isNullOrEmpty()) return null
        return moshi.adapter<List<VideoScene>>(sceneListType).fromJson(value)
    }

    @TypeConverter
    fun fromThumbnailList(value: List<ThumbnailOption>?): String? {
        if (value == null) return null
        return moshi.adapter<List<ThumbnailOption>>(thumbnailListType).toJson(value)
    }

    @TypeConverter
    fun toThumbnailList(value: String?): List<ThumbnailOption>? {
        if (value.isNullOrEmpty()) return null
        return moshi.adapter<List<ThumbnailOption>>(thumbnailListType).fromJson(value)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        return moshi.adapter<List<String>>(stringListType).toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrEmpty()) return null
        return moshi.adapter<List<String>>(stringListType).fromJson(value)
    }
}
