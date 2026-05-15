package com.nitish.auraassistant.di



import com.nitish.auraassistant.data.repository.ChatRepositoryImpl
import com.nitish.auraassistant.data.repository.UserRepositoryImpl
import com.nitish.auraassistant.domain.repository.ChatRepository
import com.nitish.auraassistant.domain.repository.UserRepository
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
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}