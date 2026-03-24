package com.prayaas.bookbank.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayaas.bookbank.ui.components.*
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.viewmodel.CatalogueViewModel
import com.prayaas.bookbank.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(
    catalogueViewModel: CatalogueViewModel = viewModel(),
    cartViewModel: CartViewModel,
    onBookClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val books by catalogueViewModel.filteredBooks.collectAsState()
    val searchQuery by catalogueViewModel.searchQuery.collectAsState()
    val cartCount by cartViewModel.cartCount.collectAsState()

    Scaffold(
        topBar = {
            PrayaasTopBar(
                title = "Prayaas Book Bank",
                subtitle = "Student Catalogue",
                cartCount = cartCount,
                onCartClick = onCartClick,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
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
            // Search bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrayaasColors.Orange)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = catalogueViewModel::onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Search by title, author, subject…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, "Search", tint = Color.White)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { catalogueViewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, "Clear", tint = Color.White)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            // Section header
            item {
                SectionHeader(
                    title = if (searchQuery.isBlank()) "All Books" else "Search Results",
                    subtitle = if (searchQuery.isBlank())
                        "Tap a book to view details"
                    else
                        "${books.size} book(s) found for \"$searchQuery\""
                )
            }

            // Books list
            if (books.isEmpty()) {
                item {
                    EmptyState(
                        icon = "📚",
                        title = "No books found",
                        subtitle = if (searchQuery.isBlank())
                            "The catalogue is being loaded…"
                        else
                            "Try a different search term",
                        actionLabel = if (searchQuery.isNotBlank()) "Clear Search" else null,
                        onAction = if (searchQuery.isNotBlank()) {
                            { catalogueViewModel.onSearchQueryChange("") }
                        } else null
                    )
                }
            } else {
                items(books, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        inCart = cartViewModel.isInCart(book.id),
                        onCardClick = { onBookClick(book.id) },
                        onAddToCart = {
                            if (book.availableCopies > 0) {
                                cartViewModel.addToCart(book)
                            }
                        }
                    )
                }
            }
        }
    }
}
