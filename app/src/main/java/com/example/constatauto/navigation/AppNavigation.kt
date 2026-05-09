package com.example.constatauto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.constatauto.ui.auth.LoginScreen
import com.example.constatauto.ui.auth.RegisterScreen
import com.example.constatauto.ui.constat.ConstatDetailScreen
import com.example.constatauto.ui.constat.ConstatFormScreen
import com.example.constatauto.ui.home.HomeScreen
import com.example.constatauto.viewmodel.AuthViewModel
import com.example.constatauto.viewmodel.ConstatViewModel

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Home : NavRoutes("home")
    object ConstatForm : NavRoutes("constat/form?id={id}") {
        fun createRoute(id: String? = null) = if (id != null) "constat/form?id=$id" else "constat/form"
    }
    object ConstatDetail : NavRoutes("constat/detail/{id}") {
        fun createRoute(id: String) = "constat/detail/$id"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    constatViewModel: ConstatViewModel = viewModel()
) {
    // Si l'utilisateur est connecté, on peut rediriger. Pour simplifier, on commence par login.
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) NavRoutes.Home.route else NavRoutes.Login.route
    ) {
        composable(NavRoutes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.Register.route)
                }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Home.route) {
            HomeScreen(
                viewModel = constatViewModel,
                authViewModel = authViewModel,
                onNavigateToForm = { id ->
                    navController.navigate(NavRoutes.ConstatForm.createRoute(id))
                },
                onNavigateToDetail = { id ->
                    navController.navigate(NavRoutes.ConstatDetail.createRoute(id))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.ConstatForm.route,
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            ConstatFormScreen(
                id = id,
                viewModel = constatViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.ConstatDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            if (id != null) {
                ConstatDetailScreen(
                    id = id,
                    viewModel = constatViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { editId ->
                        navController.navigate(NavRoutes.ConstatForm.createRoute(editId))
                    }
                )
            }
        }
    }
}
