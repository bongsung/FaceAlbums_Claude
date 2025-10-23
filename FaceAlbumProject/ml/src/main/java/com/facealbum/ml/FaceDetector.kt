package com.facealbum.ml

import android.graphics.Bitmap
import com.facealbum.common.Result
import com.facealbum.domain.model.BoundingBox

/**
 * Interface for face detection operations
 */
interface FaceDetector {
    /**
     * Detect faces in an image
     * @return List of detected face bounding boxes with confidence scores
     */
    suspend fun detectFaces(bitmap: Bitmap): Result<List<DetectedFace>>
    
    /**
     * Release resources
     */
    fun close()
}

data class DetectedFace(
    val boundingBox: BoundingBox,
    val confidence: Float,
    val faceBitmap: Bitmap? = null
)
