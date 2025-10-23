package com.facealbum.common

object Constants {
    // Database
    const val DATABASE_NAME = "face_album_database"
    const val DATABASE_VERSION = 1
    
    // WorkManager
    const val WORK_TAG_FACE_DETECTION = "face_detection"
    const val WORK_TAG_EMBEDDING_GENERATION = "embedding_generation"
    const val WORK_UNIQUE_NAME_MEDIA_SYNC = "media_sync"
    
    // Preferences
    const val PREF_NAME = "face_album_prefs"
    const val PREF_WATCH_FOLDERS = "watch_folders"
    const val PREF_EXPORT_TO_EXTERNAL_GALLERY = "export_to_external_gallery"
    const val PREF_AUTO_CLUSTER_ENABLED = "auto_cluster_enabled"
    
    // Face Detection
    const val MIN_FACE_SIZE = 0.1f
    const val FACE_DETECTION_CONFIDENCE_THRESHOLD = 0.7f
    
    // Embedding
    const val EMBEDDING_DIMENSION = 128
    const val SIMILARITY_THRESHOLD = 0.6f
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "face_album_channel"
    const val NOTIFICATION_ID_SYNC = 1001
}
