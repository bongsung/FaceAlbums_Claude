package com.facealbum.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.facealbum.domain.model.Photo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Enhanced scanner that can scan specific folders
 */
class FolderAwareMediaScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Scan images in specific folders
     */
    suspend fun scanFolders(folderPaths: List<String>): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<Photo>()
        
        for (folderPath in folderPaths) {
            photos.addAll(scanFolder(folderPath))
        }
        
        photos
    }
    
    /**
     * Scan images in a specific folder
     */
    suspend fun scanFolder(folderPath: String): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<Photo>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATA
        )
        
        // Build selection to filter by folder
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ? AND ${MediaStore.Images.Media.MIME_TYPE} LIKE ?"
        } else {
            "${MediaStore.Images.Media.DATA} LIKE ? AND ${MediaStore.Images.Media.MIME_TYPE} LIKE ?"
        }
        
        val selectionArgs = arrayOf("$folderPath%", "image/%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val filePath = cursor.getString(dataColumn)
                    
                    // Check if file is actually in the specified folder
                    if (!filePath.startsWith(folderPath)) {
                        continue
                    }
                    
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    // Compute SHA-256 hash for tracking file renames/moves
                    val contentHash = computeFileHash(filePath)
                    
                    if (contentHash != null) {
                        photos.add(
                            Photo(
                                mediaStoreId = id,
                                uri = uri.toString(),
                                displayName = name,
                                dateAdded = dateAdded,
                                dateModified = dateModified,
                                size = size,
                                mimeType = mimeType,
                                contentHash = contentHash,
                                width = width,
                                height = height
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing image", e)
                }
            }
        }
        
        Log.d(TAG, "Scanned ${photos.size} images from folder: $folderPath")
        photos
    }
    
    /**
     * Monitor folder for changes using File API
     */
    suspend fun getFolderModificationTime(folderPath: String): Long = withContext(Dispatchers.IO) {
        try {
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                folder.lastModified()
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking folder modification time", e)
            0L
        }
    }
    
    /**
     * Get new or modified photos in folder since last check
     */
    suspend fun getNewPhotosInFolder(
        folderPath: String, 
        lastCheckTime: Long
    ): List<Photo> = withContext(Dispatchers.IO) {
        val photos = scanFolder(folderPath)
        photos.filter { photo ->
            photo.dateAdded > lastCheckTime || photo.dateModified > lastCheckTime
        }
    }
    
    /**
     * Compute SHA-256 hash of file content for tracking renames/moves
     */
    private fun computeFileHash(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (!file.exists() || !file.canRead()) {
                Log.w(TAG, "Cannot read file: $filePath")
                return null
            }
            
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                // Only hash first 1MB for performance
                var totalRead = 0
                val maxBytes = 1024 * 1024
                
                while (fis.read(buffer).also { bytesRead = it } != -1 && totalRead < maxBytes) {
                    val toProcess = minOf(bytesRead, maxBytes - totalRead)
                    digest.update(buffer, 0, toProcess)
                    totalRead += toProcess
                }
            }
            
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error computing file hash for $filePath", e)
            null
        }
    }
    
    companion object {
        private const val TAG = "FolderAwareMediaScanner"
    }
}
