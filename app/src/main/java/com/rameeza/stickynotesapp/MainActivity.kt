package com.rameeza.stickynotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rameeza.stickynotesapp.ui.add_edit_note.AddEditNoteScreen
import com.rameeza.stickynotesapp.ui.notes.NotesScreen
import com.rameeza.stickynotesapp.ui.theme.StickyNotesAppTheme
import com.rameeza.stickynotesapp.ui.util.Screen
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as StickyNotesApp).appComponent.inject(this)
        enableEdgeToEdge()
        setContent {
            StickyNotesAppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.NotesScreen.route
                    ) {
                        composable(route = Screen.NotesScreen.route) {
                            NotesScreen(
                                navController = navController,
                                viewModelFactory = viewModelFactory
                            )
                        }
                        composable(
                            route = Screen.AddEditNoteScreen.route + "?noteId={noteId}",
                            arguments = listOf(
                                navArgument(
                                    name = "noteId"
                                ) {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) {
                            val noteId = it.arguments?.getInt("noteId")
                            AddEditNoteScreen(
                                navController = navController,
                                noteId = noteId,
                                viewModelFactory = viewModelFactory
                            )
                        }
                    }
                }
            }
        }
    }
}