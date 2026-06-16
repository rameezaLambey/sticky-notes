package com.rameeza.stickynotesapp.di

import android.app.Application
import androidx.room.Room
import com.rameeza.stickynotesapp.data.local.NoteDatabase
import com.rameeza.stickynotesapp.data.repository.NoteRepositoryImpl
import com.rameeza.stickynotesapp.domain.repository.NoteRepository
import com.rameeza.stickynotesapp.domain.use_case.*
import com.rameeza.stickynotesapp.util.VoiceToTextParser
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    fun provideVoiceToTextParser(app: Application): VoiceToTextParser {
        return VoiceToTextParser(app)
    }

    @Provides
    @Singleton
    fun provideNoteDatabase(app: Application): NoteDatabase {
        return Room.databaseBuilder(
            app,
            NoteDatabase::class.java,
            "note_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(db: NoteDatabase): NoteRepository {
        return NoteRepositoryImpl(db.noteDao)
    }

    @Provides
    @Singleton
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases {
        return NoteUseCases(
            getNotes = GetNotes(repository),
            deleteNote = DeleteNote(repository),
            addNote = AddNote(repository),
            getNote = GetNote(repository)
        )
    }
}
