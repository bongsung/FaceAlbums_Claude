package com.facealbum.media

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes MediaStore for new or modified images
 */
@Singleton
class MediaStoreObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _mediaChanges = MutableSharedFlow<MediaChange>(replay = 0)
    val mediaChanges: SharedFlow<MediaChange> = _mediaChanges.asSharedFlow()
    
    private var contentObserver: ContentObserver? = null
    
    fun startObserving() {
        if (contentObserver != null) {
            Log.d(TAG, "Already observing MediaStore")
            return
        }
        
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                Log.d(TAG, "MediaStore changed: $uri")
                
                scope.launch {
                    uri?.let {
                        handleMediaChange(it)
                    } ?: run {
                        // Full scan if URI is null
                        _mediaChanges.emit(MediaChange.FullScan)
                    }
                }
            }
        }
        
        // Register observer for external images
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver!!
        )
        
        Log.d(TAG, "Started observing MediaStore")
    }
    
    fun stopObserving() {
        contentObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
            contentObserver = null
            Log.d(TAG, "Stopped observing MediaStore")
        }
    }
    
    private suspend fun handleMediaChange(uri: Uri) {
        try {
            // Extract media ID from URI
            val mediaId = uri.lastPathSegment?.toLongOrNull()
            
            if (mediaId != null) {
                // Query MediaStore for this specific item
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT
                )
                
                context.contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayName = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                        )
                        
                        _mediaChanges.emit(
                            MediaChange.ItemChanged(
                                mediaStoreId = mediaId,
                                uri = uri.toString(),
                                displayName = displayName
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling media change", e)
        }
    }
    
    companion object {
        private const val TAG = "MediaStoreObserver"
    }
}

/**
 * Represents a change in MediaStore
 */
sealed class MediaChange {
    data class ItemChanged(
        val mediaStoreId: Long,
        val uri: String,
        val displayName: String
    ) : MediaChange()
    
    data object FullScan : MediaChange()
}
