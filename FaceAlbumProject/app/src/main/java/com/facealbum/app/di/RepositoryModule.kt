package com.facealbum.app.di

import com.facealbum.data.repository.*
import com.facealbum.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindPersonRepository(
        impl: PersonRepositoryImpl
    ): PersonRepository
    
    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        impl: PhotoRepositoryImpl
    ): PhotoRepository
    
    // TODO: Add bindings for other repositories
    // @Binds
    // @Singleton
    // abstract fun bindFaceRepository(impl: FaceRepositoryImpl): FaceRepository
    
    // @Binds
    // @Singleton
    // abstract fun bindSuggestionRepository(impl: SuggestionRepositoryImpl): SuggestionRepository
    
    // @Binds
    // @Singleton
    // abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
