package com.roomie.salesapp.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roomie.salesapp.presentation.screens.home.HomeScreen
import com.roomie.salesapp.presentation.screens.opportunities.OpportunitiesScreen
import com.roomie.salesapp.presentation.screens.opportunities.OpportunityDetailScreen
import com.roomie.salesapp.presentation.screens.profile.ProfileScreen
import com.roomie.salesapp.presentation.screens.route.RouteScreen
import com.roomie.salesapp.presentation.screens.visit.VisitReportScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Route : Screen("route")
    object Opportunities : Screen("opportunities")
    object Profile : Screen("profile")
    object VisitReport : Screen("visit_report/{hotelId}/{hotelName}") {
        fun createRoute(hotelId: String, hotelName: String) =
            "visit_report/$hotelId/${hotelName.replace("/", "-")}"
    }
    object OpportunityDetail : Screen("opportunity_detail/{opportunityId}") {
        fun createRoute(opportunityId: String) = "opportunity_detail/$opportunityId"
    }
}

private data class Tab(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    Tab(Screen.Home, "Inicio", Icons.Default.Home),
    Tab(Screen.Route, "Ruta", Icons.Default.Map),
    Tab(Screen.Opportunities, "Ventas", Icons.Default.TrendingUp),
    Tab(Screen.Profile, "Perfil", Icons.Default.Person),
)

@Composable
fun NavGraph(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // La barra inferior solo se muestra en las 4 pestañas principales
    val showBottomBar = currentRoute in bottomTabs.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute == tab.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(tab.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        // Solo aplicamos el padding INFERIOR (del bottom nav). El superior (status bar)
        // lo maneja cada pantalla con su propio TopAppBar, evitando el doble inset.
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onGoToRoute = { navController.navigate(Screen.Route.route) { launchSingleTop = true } },
                    onGoToOpportunities = { navController.navigate(Screen.Opportunities.route) { launchSingleTop = true } }
                )
            }
            composable(Screen.Route.route) {
                RouteScreen(
                    onNavigateToVisit = { hotelId, hotelName ->
                        navController.navigate(Screen.VisitReport.createRoute(hotelId, hotelName))
                    }
                )
            }
            composable(Screen.Opportunities.route) {
                OpportunitiesScreen(
                    onOpportunityClick = { id ->
                        navController.navigate(Screen.OpportunityDetail.createRoute(id))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.VisitReport.route) { backStackEntry ->
                val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
                val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
                VisitReportScreen(
                    hotelId = hotelId,
                    hotelName = hotelName,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.OpportunityDetail.route) {
                OpportunityDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
