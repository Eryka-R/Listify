package com.erika.listify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.erika.listify.ui.screens.editor.ListEditorScreen
import com.erika.listify.ui.screens.export.ExportScreen
import com.erika.listify.ui.screens.home.HomeScreen
import com.erika.listify.ui.screens.import.ImportScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME // La app comienza en HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenList = { listId ->
                    navController.navigate("${Routes.EDITOR}/$listId")
                },
                onImport = {
                    navController.navigate(Routes.IMPORT)
                }
            )
        }

        composable(
            route = "${Routes.EDITOR}/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: return@composable
            ListEditorScreen(
                listId = listId,
                onExport = { navController.navigate("${Routes.EXPORT}/$listId") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.IMPORT) {
            ImportScreen(
                onCreated = { listId ->
                    navController.navigate("${Routes.EDITOR}/$listId") {
                        popUpTo(Routes.IMPORT) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.EXPORT}/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: return@composable
            ExportScreen(
                listId = listId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}