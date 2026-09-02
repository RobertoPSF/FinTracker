package com.rfilh.fintracker.ui.nav

import android.net.Uri
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
import com.rfilh.fintracker.ui.screens.AlertsScreen
import com.rfilh.fintracker.ui.screens.CategoriesScreen
import com.rfilh.fintracker.ui.screens.ComparativoScreen
import com.rfilh.fintracker.ui.screens.EditScreen
import com.rfilh.fintracker.ui.screens.HomeScreen
import com.rfilh.fintracker.ui.screens.InsightsScreen
import com.rfilh.fintracker.ui.screens.ManualPixScreen
import com.rfilh.fintracker.ui.screens.MetaScreen
import com.rfilh.fintracker.ui.screens.PredictedBillsScreen
import com.rfilh.fintracker.ui.screens.RankingScreen
import com.rfilh.fintracker.ui.screens.ReceivablesScreen
import com.rfilh.fintracker.ui.screens.ReviewScreen
import com.rfilh.fintracker.ui.screens.SettingsScreen
import com.rfilh.fintracker.ui.screens.StatementScreen
import com.rfilh.fintracker.ui.vm.AlertsViewModel
import com.rfilh.fintracker.ui.vm.CategoriesViewModel
import com.rfilh.fintracker.ui.vm.ComparativoViewModel
import com.rfilh.fintracker.ui.vm.EditViewModel
import com.rfilh.fintracker.ui.vm.HomeViewModel
import com.rfilh.fintracker.ui.vm.InsightsViewModel
import com.rfilh.fintracker.ui.vm.ManualPixViewModel
import com.rfilh.fintracker.ui.vm.MetaViewModel
import com.rfilh.fintracker.ui.vm.PredictedBillsViewModel
import com.rfilh.fintracker.ui.vm.RankingViewModel
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
    const val CATEGORIES = "categories"
    const val RANKING = "ranking"
    const val COMPARATIVO = "comparativo"
    const val PREDICTED = "predicted"
    const val ALERTS = "alerts"
    const val META = "meta"

    fun statement(bankPackage: String) = "$STATEMENT?bank=${Uri.encode(bankPackage)}"
    fun statementByCategory(category: String) = "$STATEMENT?bank=&category=${Uri.encode(category)}"
    fun edit(id: Long) = "$EDIT/$id"
}

@Composable
fun FinTrackerNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val categoryOptions = container.rules().generic.categories.map { it.name }

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
                onOpenManualPix = { navController.navigate(Routes.MANUAL_PIX) },
                onOpenCategorias = { navController.navigate(Routes.CATEGORIES) },
                onOpenRanking = { navController.navigate(Routes.RANKING) },
                onOpenComparativo = { navController.navigate(Routes.COMPARATIVO) },
                onOpenPredicted = { navController.navigate(Routes.PREDICTED) },
                onOpenAlerts = { navController.navigate(Routes.ALERTS) },
                onOpenMeta = { navController.navigate(Routes.META) }
            )
        }

        composable(Routes.MANUAL_PIX) {
            val vm: ManualPixViewModel = viewModel(
                factory = viewModelFactory { initializer { ManualPixViewModel(container.repository, container) } }
            )
            ManualPixScreen(vm = vm, onDone = { navController.popBackStack() })
        }

        composable(
            route = "${Routes.STATEMENT}?bank={bank}&category={category}",
            arguments = listOf(
                navArgument("bank") { type = NavType.StringType; defaultValue = "" },
                navArgument("category") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val bank = entry.arguments?.getString("bank").orEmpty()
            val category = entry.arguments?.getString("category").orEmpty()
            val vm: StatementViewModel = viewModel(
                key = "statement-$bank-$category",
                factory = viewModelFactory {
                    initializer { StatementViewModel(container.repository, bank, category) }
                }
            )
            StatementScreen(
                vm = vm,
                categoryOptions = categoryOptions,
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

        composable(Routes.CATEGORIES) {
            val vm: CategoriesViewModel = viewModel(
                factory = viewModelFactory { initializer { CategoriesViewModel(container.repository) } }
            )
            CategoriesScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onOpenCategory = { navController.navigate(Routes.statementByCategory(it)) }
            )
        }

        composable(Routes.RANKING) {
            val vm: RankingViewModel = viewModel(
                factory = viewModelFactory { initializer { RankingViewModel(container.repository) } }
            )
            RankingScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.COMPARATIVO) {
            val vm: ComparativoViewModel = viewModel(
                factory = viewModelFactory { initializer { ComparativoViewModel(container.repository) } }
            )
            ComparativoScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.PREDICTED) {
            val vm: PredictedBillsViewModel = viewModel(
                factory = viewModelFactory { initializer { PredictedBillsViewModel(container.repository) } }
            )
            PredictedBillsScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.ALERTS) {
            val vm: AlertsViewModel = viewModel(
                factory = viewModelFactory { initializer { AlertsViewModel(container.repository) } }
            )
            AlertsScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.META) {
            val vm: MetaViewModel = viewModel(
                factory = viewModelFactory { initializer { MetaViewModel(container.repository, container) } }
            )
            MetaScreen(vm = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                factory = viewModelFactory { initializer { SettingsViewModel(container.repository, container) } }
            )
            SettingsScreen(vm = vm, onBack = { navController.popBackStack() })
        }
    }
}
