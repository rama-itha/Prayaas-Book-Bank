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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prayaas.bookbank.data.model.Book
import com.prayaas.bookbank.ui.components.*
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.viewmodel.CatalogueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventoryScreen(
    catalogueViewModel: CatalogueViewModel = viewModel(),
    onBackClick: () -> Unit,
    onQrGeneratorClick: () -> Unit = {}
) {
    val books by catalogueViewModel.filteredBooks.collectAsState()
    val searchQuery by catalogueViewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Inventory", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Manage book catalogue", style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onQrGeneratorClick) {
                        Icon(Icons.Default.QrCode, "Generate QR Codes", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrayaasColors.Orange)
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
            // Search
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
                            Text("Search books…", color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = Color.White)
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

            item {
                SectionHeader(
                    title = "Book Catalogue",
                    subtitle = "${books.size} book(s) · Tap to view copy details"
                )
            }

            items(books, key = { it.id }) { book ->
                InventoryBookCard(book = book)
            }
        }
    }
}

@Composable
private fun InventoryBookCard(book: Book) {
    val coverColor = bookCoverColor(book.id)
    val availabilityFraction = if (book.totalCopies > 0)
        book.availableCopies.toFloat() / book.totalCopies.toFloat()
    else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Cover
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(coverColor),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.coverImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    book.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(8.dp))

                // Progress bar for availability
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { availabilityFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            availabilityFraction == 0f -> PrayaasColors.Error
                            availabilityFraction < 0.4f -> PrayaasColors.Warning
                            else -> PrayaasColors.Success
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        "${book.availableCopies}/${book.totalCopies}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            book.availableCopies == 0 -> PrayaasColors.Error
                            book.availableCopies == 1 -> PrayaasColors.Warning
                            else -> PrayaasColors.Success
                        }
                    )
                }

                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AvailabilityBadge(book.availableCopies)
                    if (book.edition.isNotBlank()) {
                        Surface(
                            shape = ChipShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                book.edition,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
