package com.prayaas.bookbank.ui

sealed class Screen(val route: String) {
    object RoleSelect : Screen("role_select")
    object Catalogue : Screen("catalogue")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: String) = "book_detail/$bookId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: String) = "order_success/$orderId"
    }
    object AdminOrders : Screen("admin_orders")
    object AdminScan : Screen("admin_scan/{orderId}/{bookId}/{mode}") {
        fun createRoute(orderId: String, bookId: String, mode: String) =
            "admin_scan/$orderId/$bookId/$mode"
    }
    object AdminInventory : Screen("admin_inventory")
    object QrGenerator : Screen("qr_generator")
}
