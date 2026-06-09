package com.rameeza.stickynotesapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rameeza.stickynotesapp.ui.add_edit_note.AddEditNoteViewModel
import com.rameeza.stickynotesapp.ui.notes.NotesViewModel
import com.rameeza.stickynotesapp.ui.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(NotesViewModel::class)
    abstract fun bindNotesViewModel(viewModel: NotesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddEditNoteViewModel::class)
    abstract fun bindAddEditNoteViewModel(viewModel: AddEditNoteViewModel): ViewModel
}
