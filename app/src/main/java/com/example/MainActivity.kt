package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.ProjectSelectorSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LaborScheduleScreen
import com.example.ui.screens.RabDetailScreen
import com.example.ui.screens.RegionalPricesScreen
import com.example.ui.theme.QsEstimatorTheme
import com.example.viewmodel.QsViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Analytics)
    object RabDetail : Screen("rab_detail", "RAB", Icons.Default.ListAlt)
    object LaborSchedule : Screen("labor_schedule", "Tenaga Kerja", Icons.Default.Engineering)
    object RegionalPrices : Screen("regional_prices", "Harga AHSP", Icons.Default.MonetizationOn)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QsEstimatorTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: QsViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showProjectSelectorSheet by remember { mutableStateOf(false) }
    var showNewProjectDialog by remember { mutableStateOf(false) }

    if (showProjectSelectorSheet) {
        ProjectSelectorSheet(
            viewModel = viewModel,
            onDismiss = { showProjectSelectorSheet = false },
            onAddNewProject = { showNewProjectDialog = true }
        )
    }

    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreate = { title, client, region, type, workers, wage, ppn, overhead ->
                viewModel.createNewProject(title, client, region, type, workers, wage, ppn, overhead)
            }
        )
    }

    val screens = listOf(
        Screen.Dashboard,
        Screen.RabDetail,
        Screen.LaborSchedule,
        Screen.RegionalPrices
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(text = screen.title) },
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRab = { navController.navigate(Screen.RabDetail.route) },
                    onNavigateToLabor = { navController.navigate(Screen.LaborSchedule.route) },
                    onNavigateToRegional = { navController.navigate(Screen.RegionalPrices.route) },
                    onOpenProjectSelector = { showProjectSelectorSheet = true },
                    onAddNewProject = { showNewProjectDialog = true }
                )
            }

            composable(Screen.RabDetail.route) {
                RabDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LaborSchedule.route) {
                LaborScheduleScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.RegionalPrices.route) {
                RegionalPricesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
