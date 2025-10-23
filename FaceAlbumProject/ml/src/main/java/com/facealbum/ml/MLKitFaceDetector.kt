package com.facealbum.ml

import android.graphics.Bitmap
import com.facealbum.common.Constants
import com.facealbum.common.Result
import com.facealbum.domain.model.BoundingBox
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * ML Kit implementation of FaceDetector
 */
class MLKitFaceDetector @Inject constructor() : FaceDetector {
    
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setMinFaceSize(Constants.MIN_FACE_SIZE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()
    
    private val detector = FaceDetection.getClient(options)
    
    override suspend fun detectFaces(bitmap: Bitmap): Result<List<DetectedFace>> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val faces = detector.process(image).await()
            
            val detectedFaces = faces.mapNotNull { face ->
                convertToDetectedFace(face, bitmap)
            }
            
            Result.Success(detectedFaces)
        } catch (e: Exception) {
            Result.Error(e, "Failed to detect faces")
        }
    }
    
    private fun convertToDetectedFace(face: Face, bitmap: Bitmap): DetectedFace? {
        val bounds = face.boundingBox
        
        // Normalize coordinates to 0-1 range
        val normalizedBox = BoundingBox(
            left = bounds.left.toFloat() / bitmap.width,
            top = bounds.top.toFloat() / bitmap.height,
            right = bounds.right.toFloat() / bitmap.width,
            bottom = bounds.bottom.toFloat() / bitmap.height
        )
        
        // Extract face region from bitmap
        val faceBitmap = try {
            val left = max(0, bounds.left)
            val top = max(0, bounds.top)
            val width = min(bitmap.width - left, bounds.width())
            val height = min(bitmap.height - top, bounds.height())
            
            if (width > 0 && height > 0) {
                Bitmap.createBitmap(bitmap, left, top, width, height)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        
        // ML Kit doesn't provide confidence directly, using a default threshold
        val confidence = if (face.trackingId != null) 0.9f else 0.7f
        
        return if (confidence >= Constants.FACE_DETECTION_CONFIDENCE_THRESHOLD) {
            DetectedFace(
                boundingBox = normalizedBox,
                confidence = confidence,
                faceBitmap = faceBitmap
            )
        } else {
            null
        }
    }
    
    override fun close() {
        detector.close()
    }
}
