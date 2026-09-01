package com.rfilh.fintracker.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rfilh.fintracker.AppContainer
import com.rfilh.fintracker.ui.screens.EditScreen
import com.rfilh.fintracker.ui.screens.HomeScreen
import com.rfilh.fintracker.ui.screens.InsightsScreen
import com.rfilh.fintracker.ui.screens.ManualPixScreen
import com.rfilh.fintracker.ui.screens.ReceivablesScreen
import com.rfilh.fintracker.ui.screens.ReviewScreen
import com.rfilh.fintracker.ui.screens.SettingsScreen
import com.rfilh.fintracker.ui.screens.StatementScreen
import com.rfilh.fintracker.ui.vm.EditViewModel
import com.rfilh.fintracker.ui.vm.HomeViewModel
import com.rfilh.fintracker.ui.vm.InsightsViewModel
import com.rfilh.fintracker.ui.vm.ManualPixViewModel
import com.rfilh.fintracker.ui.vm.ReceivablesViewModel
import com.rfilh.fintracker.ui.vm.ReviewViewModel
import com.rfilh.fintracker.ui.vm.SettingsViewModel
import com.rfilh.fintracker.ui.vm.StatementViewModel

object Routes {
    const val HOME = "home"
    const val STATEMENT = "statement"
    const val REVIEW = "review"
    const val EDIT = "edit"
    const val RECEIVABLES = "receivables"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
    const val MANUAL_PIX = "manual_pix"

    fun statement(bankPackage: String) = "$STATEMENT?bank=$bankPackage"
    fun edit(id: Long) = "$EDIT/$id"
}

@Composable
fun FinTrackerNavHost(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(
                factory = viewModelFactory { initializer { HomeViewModel(container.repository) } }
            )
            HomeScreen(
                vm = vm,
                onOpenBank = { navController.navigate(Routes.statement(it)) },
                onOpenAll = { navController.navigate("${Routes.STATEMENT}?bank=") },
                onOpenReview = { navController.navigate(Routes.REVIEW) },
                onOpenReceivables = { navController.navigate(Routes.RECEIVABLES) },
                onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenManualPix = { navController.navigate(Routes.MANUAL_PIX) }
            )
        }

        composable(Routes.MANUAL_PIX) {
            val vm: ManualPixViewModel = viewModel(
                factory = viewModelFactory { initializer { ManualPixViewModel(container.repository, container) } }
            )
            ManualPixScreen(vm = vm, onDone = { navController.popBackStack() })
        }

        composable(
            route = "${Routes.STATEMENT}?bank={bank}",
            arguments = listOf(navArgument("bank") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            val bank = entry.arguments?.getString("bank").orEmpty()
            val vm: StatementViewModel = viewModel(
                key = "statement-$bank",
                factory = viewModelFactory { initializer { StatementViewModel(container.repository, bank) } }
            )
            StatementScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(it)) }
            )
        }

        composable(Routes.REVIEW) {
            val vm: ReviewViewModel = viewModel(
                factory = viewModelFactory { initializer { ReviewViewModel(container.repository) } }
            )
            ReviewScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(it)) }
            )
        }

        composable(
            route = "${Routes.EDIT}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val vm: EditViewModel = viewModel(
                key = "edit-$id",
                factory = viewModelFactory { initializer { EditViewModel(container.repository, container, id) } }
            )
            EditScreen(vm = vm, onDone = { navController.popBackStack() })
        }

        composable(Routes.RECEIVABLES) {
            val vm: ReceivablesViewModel = viewModel(
                factory = viewModelFactory { initializer { ReceivablesViewModel(container.repository) } }
            )
            ReceivablesScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.INSIGHTS) {
            val vm: InsightsViewModel = viewModel(
                factory = viewModelFactory { initializer { InsightsViewModel(container.repository) } }
            )
            InsightsScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                factory = viewModelFactory { initializer { SettingsViewModel(container.repository, container) } }
            )
            SettingsScreen(vm = vm, onBack = { navController.popBackStack() })
        }
    }
}
