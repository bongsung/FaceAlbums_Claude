package com.facealbum.app.di

import com.facealbum.ml.DummyFaceEmbeddingGenerator
import com.facealbum.ml.FaceDetector
import com.facealbum.ml.FaceEmbeddingGenerator
import com.facealbum.ml.MLKitFaceDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MLModule {
    
    @Binds
    @Singleton
    abstract fun bindFaceDetector(
        impl: MLKitFaceDetector
    ): FaceDetector
    
    @Binds
    @Singleton
    abstract fun bindFaceEmbeddingGenerator(
        impl: DummyFaceEmbeddingGenerator
    ): FaceEmbeddingGenerator
    
    // TODO: When implementing real TFLite model, replace DummyFaceEmbeddingGenerator
    // with TFLiteFaceEmbeddingGenerator
}
