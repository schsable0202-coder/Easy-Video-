package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.VideoStudioViewModel
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val studioViewModel: VideoStudioViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "projects",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("projects") {
                            ProjectsScreen(
                                viewModel = studioViewModel,
                                onNavigateToStudio = { projectId ->
                                    navController.navigate("studio/$projectId")
                                }
                            )
                        }
                        composable(
                            route = "studio/{projectId}",
                            arguments = listOf(navArgument("projectId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
                            StudioScreen(
                                viewModel = studioViewModel,
                                projectId = projectId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
