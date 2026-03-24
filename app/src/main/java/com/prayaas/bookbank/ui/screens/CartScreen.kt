package com.prayaas.bookbank.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.prayaas.bookbank.data.model.Book
import com.prayaas.bookbank.ui.components.*
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBackClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onCheckoutClick: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount by cartViewModel.cartCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Cart", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "$cartCount book(s) selected",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrayaasColors.Orange)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Books to check out:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                            Text(
                                "$cartCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrayaasColors.Orange
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        PrimaryButton(
                            text = "Proceed to Checkout →",
                            onClick = onCheckoutClick
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = "🛒",
                    title = "Your cart is empty",
                    subtitle = "Browse the catalogue and add books you want to borrow",
                    actionLabel = "Browse Catalogue",
                    onAction = onBackClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Selected Books",
                        subtitle = "Tap a book to view details, or remove it from cart"
                    )
                }

                items(cartItems, key = { it.id }) { book ->
                    CartBookRow(
                        book = book,
                        onBookClick = { onBookClick(book.id) },
                        onRemove = { cartViewModel.removeFromCart(book.id) }
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = SmallCardShape,
                        colors = CardDefaults.cardColors(containerColor = PrayaasColors.OrangeLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("📧")
                            Text(
                                "A confirmation email will be sent to you after checkout with all book details.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrayaasColors.OrangeDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartBookRow(
    book: Book,
    onBookClick: () -> Unit,
    onRemove: () -> Unit
) {
    val coverColor = bookCoverColor(book.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onBookClick),
        shape = SmallCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 42.dp, height = 56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(coverColor),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.coverImageUrl,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrayaasColors.ErrorBg)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = PrayaasColors.Error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
