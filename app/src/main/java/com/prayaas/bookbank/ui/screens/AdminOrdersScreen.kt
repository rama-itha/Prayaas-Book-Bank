package com.prayaas.bookbank.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayaas.bookbank.data.model.*
import com.prayaas.bookbank.ui.components.*
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.viewmodel.AdminTab
import com.prayaas.bookbank.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onScanClick: (orderId: String, bookId: String, mode: String) -> Unit,
    onInventoryClick: () -> Unit
) {
    val orders by adminViewModel.filteredOrders.collectAsState()
    val stats by adminViewModel.dashboardStats.collectAsState()
    val selectedTab by adminViewModel.selectedTab.collectAsState()

    Scaffold(
        topBar = {
            PrayaasTopBar(
                title = "Admin Panel",
                subtitle = "Book Bank Management",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onInventoryClick) {
                        Icon(Icons.Default.Inventory, "Inventory", tint = Color.White)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(stats.totalOrders, "Total", modifier = Modifier.weight(1f))
                    StatCard(stats.pendingOrders, "Pending", color = PrayaasColors.Warning, modifier = Modifier.weight(1f))
                    StatCard(stats.issuedOrders, "Issued", color = PrayaasColors.Info, modifier = Modifier.weight(1f))
                    StatCard(stats.returnedOrders, "Returned", color = PrayaasColors.Success, modifier = Modifier.weight(1f))
                }
            }

            // Tab filter
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(AdminTab.values()) { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { adminViewModel.selectTab(tab) },
                            label = {
                                Text(
                                    tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrayaasColors.Orange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (orders.isEmpty()) {
                item {
                    EmptyState(
                        icon = "📋",
                        title = "No orders",
                        subtitle = "Orders will appear here once students place them"
                    )
                }
            } else {
                items(orders, key = { it.orderId }) { order ->
                    AdminOrderCard(
                        order = order,
                        onIssueBook = { bookId ->
                            onScanClick(order.orderId, bookId, "issue")
                        },
                        onReturnBook = { bookId ->
                            onScanClick(order.orderId, bookId, "return")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    order: Order,
    onIssueBook: (String) -> Unit,
    onReturnBook: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.orderId.take(8).uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Text(
                        text = order.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "ID: ${order.studentId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📱", fontSize = 11.sp)
                        Text(
                            order.mobileNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                        Text("📧", fontSize = 11.sp)
                        Text(
                            order.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrayaasColors.Info,
                            maxLines = 1
                        )
                    }
                }
                OrderStatusChip(order.status)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Books list
            Text(
                "Books",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            order.books.forEach { orderBook ->
                AdminBookRow(
                    orderBook = orderBook,
                    orderStatus = order.status,
                    onIssue = { onIssueBook(orderBook.bookId) },
                    onReturn = { onReturnBook(orderBook.bookId) }
                )
            }
        }
    }
}

@Composable
private fun AdminBookRow(
    orderBook: OrderBook,
    orderStatus: OrderStatus,
    onIssue: () -> Unit,
    onReturn: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                orderBook.bookTitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                orderBook.bookAuthor,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }

        // QR code chip if assigned
        orderBook.copyId?.let { copyId ->
            Surface(
                shape = SmallCardShape,
                color = MaterialTheme.colorScheme.onSurface
            ) {
                Text(
                    text = copyId,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Action button
        when {
            orderBook.isReturned -> {
                Surface(shape = ChipShape, color = PrayaasColors.SuccessBg) {
                    Text(
                        "✓ Returned",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrayaasColors.Success,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
            orderBook.copyId != null -> {
                // Issued, can be returned
                Button(
                    onClick = onReturn,
                    colors = ButtonDefaults.buttonColors(containerColor = PrayaasColors.SuccessBg),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp),
                    shape = ChipShape
                ) {
                    Text(
                        "Return",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrayaasColors.Success,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            else -> {
                // Pending, needs to be issued
                Button(
                    onClick = onIssue,
                    colors = ButtonDefaults.buttonColors(containerColor = PrayaasColors.OrangeLight),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp),
                    shape = ChipShape
                ) {
                    Text(
                        "Issue",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrayaasColors.OrangeDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
