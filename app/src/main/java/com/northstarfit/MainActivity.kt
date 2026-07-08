package com.northstarfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.northstarfit.ui.history.HistoryScreen
import com.northstarfit.ui.history.WorkoutDetailScreen
import com.northstarfit.ui.home.HomeScreen
import com.northstarfit.ui.theme.NorthStarFitTheme
import com.northstarfit.ui.workout.WorkoutScreen

/**
 * Single-activity Compose app. Navigation between screens is handled by
 * Navigation Compose instead of separate activities.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NorthStarFitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onStartWorkout = { navController.navigate("workout") },
                                onOpenHistory = { navController.navigate("history") },
                            )
                        }
                        composable("workout") {
                            WorkoutScreen(
                                onFinished = { navController.popBackStack() },
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                onBack = { navController.popBackStack() },
                                onOpenWorkout = { workoutId ->
                                    navController.navigate("history/$workoutId")
                                },
                            )
                        }
                        composable(
                            "history/{workoutId}",
                            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
                        ) { entry ->
                            WorkoutDetailScreen(
                                workoutId = entry.arguments?.getLong("workoutId") ?: 0L,
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
