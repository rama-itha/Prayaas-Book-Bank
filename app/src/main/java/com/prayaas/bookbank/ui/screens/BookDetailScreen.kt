package com.prayaas.bookbank.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.prayaas.bookbank.data.model.Book
import com.prayaas.bookbank.ui.components.*
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {
    val inCart = cartViewModel.isInCart(book.id)
    val coverColor = bookCoverColor(book.id)
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Book Details", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrayaasColors.Orange)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Hero header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrayaasColors.Orange)
                    .padding(top = 16.dp, bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Cover image
                    Box(
                        modifier = Modifier
                            .size(width = 100.dp, height = 136.dp)
                            .clip(RoundedCornerShape(10.dp))
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
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("📖", fontSize = 28.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = book.title.take(20),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // White content card overlapping the header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 16.dp),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Quick chips row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        InfoChip(icon = "📖", text = book.subject.ifBlank { "General" })
                        if (book.edition.isNotBlank()) InfoChip(icon = "🔖", text = book.edition)
                        if (book.pages > 0) InfoChip(icon = "📄", text = "${book.pages} pages")
                        AvailabilityBadge(book.availableCopies)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Summary
                    Text(
                        text = "About this book",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = book.summary.ifBlank { "No description available." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 22.sp
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Details table
                    Text(
                        text = "Book Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DetailRow("ISBN", book.isbn.ifBlank { "N/A" })
                    DetailRow("Publisher", book.publisher.ifBlank { "N/A" })
                    DetailRow("Edition", book.edition.ifBlank { "N/A" })
                    DetailRow("Pages", if (book.pages > 0) "${book.pages}" else "N/A")
                    DetailRow("Total Copies", "${book.totalCopies}")
                    DetailRow("Available", "${book.availableCopies}")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // QR info section
                    Text(
                        text = "QR Code System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // QR icon placeholder
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrayaasColors.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⬛", fontSize = 28.sp)
                        }
                        Column {
                            Text(
                                "Each physical copy has a unique QR code",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Scanned by admin when issuing & returning",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                "Ensures the correct copy is matched to each student",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }

            // CTA buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-12).dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (book.availableCopies > 0) {
                    PrimaryButton(
                        text = if (inCart) "✓ Already in Cart" else "🛒 Add to Cart",
                        onClick = {
                            if (!inCart) cartViewModel.addToCart(book)
                            else onGoToCart()
                        },
                        containerColor = if (inCart) PrayaasColors.Success else PrayaasColors.Orange
                    )
                    if (inCart) {
                        OutlinedButton(
                            onClick = onGoToCart,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = ButtonShape,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp, PrayaasColors.Orange
                            )
                        ) {
                            Text(
                                "View Cart →",
                                style = MaterialTheme.typography.titleMedium,
                                color = PrayaasColors.Orange
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = ButtonShape
                    ) {
                        Text("Currently Unavailable", style = MaterialTheme.typography.titleMedium)
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ButtonShape
                ) {
                    Text("← Back to Catalogue", color = PrayaasColors.Gray)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoChip(icon: String, text: String) {
    Surface(
        shape = com.prayaas.bookbank.ui.theme.ChipShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 11.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
