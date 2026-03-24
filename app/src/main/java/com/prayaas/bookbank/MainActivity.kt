package com.prayaas.bookbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.prayaas.bookbank.ui.Screen
import com.prayaas.bookbank.ui.screens.*
import com.prayaas.bookbank.ui.theme.PrayaasBookBankTheme
import com.prayaas.bookbank.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrayaasBookBankTheme {
                PrayaasApp()
            }
        }
    }
}

@Composable
fun PrayaasApp() {
    val navController = rememberNavController()

    // Shared ViewModels — one instance for the whole app lifetime
    val catalogueViewModel: CatalogueViewModel = viewModel()
    val cartViewModel: CartViewModel           = viewModel()
    val checkoutViewModel: CheckoutViewModel   = viewModel()
    val adminViewModel: AdminViewModel         = viewModel()

    NavHost(
        navController  = navController,
        startDestination = Screen.RoleSelect.route
    ) {

        // ── Role Select ───────────────────────────────────────────────────────
        composable(Screen.RoleSelect.route) {
            RoleSelectScreen(
                onStudentSelected = { navController.navigate(Screen.Catalogue.route) },
                onAdminSelected   = { navController.navigate(Screen.AdminOrders.route) }
            )
        }

        // ── Catalogue ─────────────────────────────────────────────────────────
        composable(Screen.Catalogue.route) {
            CatalogueScreen(
                catalogueViewModel = catalogueViewModel,
                cartViewModel      = cartViewModel,
                onBookClick        = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Book Detail ───────────────────────────────────────────────────────
        composable(
            route     = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            val books  by catalogueViewModel.books.collectAsState()
            val book   = books.find { it.id == bookId } ?: return@composable

            BookDetailScreen(
                book          = book,
                cartViewModel = cartViewModel,
                onBack        = { navController.popBackStack() },
                onGoToCart    = { navController.navigate(Screen.Cart.route) }
            )
        }

        // ── Cart ──────────────────────────────────────────────────────────────
        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel    = cartViewModel,
                onBackClick      = { navController.popBackStack() },
                onBookClick      = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onCheckoutClick  = { navController.navigate(Screen.Checkout.route) }
            )
        }

        // ── Checkout ──────────────────────────────────────────────────────────
        composable(Screen.Checkout.route) {
            val cartItems by cartViewModel.cartItems.collectAsState()

            CheckoutScreen(
                cartItems         = cartItems,
                checkoutViewModel = checkoutViewModel,
                onBackClick       = { navController.popBackStack() },
                onOrderPlaced     = { orderId ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                        // Remove checkout from back stack so Back goes to Catalogue
                        popUpTo(Screen.Catalogue.route)
                    }
                }
            )
        }

        // ── Order Success ─────────────────────────────────────────────────────
        composable(
            route     = Screen.OrderSuccess.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId   = backStackEntry.arguments?.getString("orderId") ?: ""
            val cartItems by cartViewModel.cartItems.collectAsState()

            OrderSuccessScreen(
                orderId           = orderId,
                studentName       = checkoutViewModel.lastStudentName,
                studentEmail      = checkoutViewModel.lastStudentEmail,
                cartItems         = cartItems,
                onBackToCatalogue = {
                    cartViewModel.clearCart()
                    checkoutViewModel.resetState()
                    navController.navigate(Screen.Catalogue.route) {
                        popUpTo(Screen.Catalogue.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Admin Orders ──────────────────────────────────────────────────────
        composable(Screen.AdminOrders.route) {
            AdminOrdersScreen(
                adminViewModel   = adminViewModel,
                onBackClick      = { navController.popBackStack() },
                onScanClick      = { orderId, bookId, mode ->
                    navController.navigate(Screen.AdminScan.createRoute(orderId, bookId, mode))
                },
                onInventoryClick = { navController.navigate(Screen.AdminInventory.route) }
            )
        }

        // ── Admin QR Scanner ──────────────────────────────────────────────────
        composable(
            route     = Screen.AdminScan.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("bookId")  { type = NavType.StringType },
                navArgument("mode")    { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val bookId  = backStackEntry.arguments?.getString("bookId")  ?: ""
            val mode    = backStackEntry.arguments?.getString("mode")    ?: "issue"

            QrScannerScreen(
                orderId        = orderId,
                bookId         = bookId,
                mode           = mode,
                adminViewModel = adminViewModel,
                onBack         = { navController.popBackStack() },
                onComplete     = { navController.popBackStack() }
            )
        }

        // ── Admin Inventory ───────────────────────────────────────────────────
        composable(Screen.AdminInventory.route) {
            AdminInventoryScreen(
                catalogueViewModel = catalogueViewModel,
                onBackClick        = { navController.popBackStack() },
                onQrGeneratorClick = { navController.navigate(Screen.QrGenerator.route) }
            )
        }

        // ── QR Code Generator ─────────────────────────────────────────────────
        composable(Screen.QrGenerator.route) {
            val books by catalogueViewModel.books.collectAsState()
            QrGeneratorScreen(
                books       = books,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
