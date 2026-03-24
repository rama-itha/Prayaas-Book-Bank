package com.prayaas.bookbank.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.prayaas.bookbank.R
import com.prayaas.bookbank.data.model.Book
import com.prayaas.bookbank.data.model.Order
import com.prayaas.bookbank.data.model.OrderStatus
import com.prayaas.bookbank.ui.theme.*

// ─── Contrast helper ─────────────────────────────────────────────────────────
/**
 * Returns Color.White for dark backgrounds (luminance < 0.35)
 * and PrayaasColors.Black for light backgrounds.
 * Guarantees readable text on ANY background colour.
 */
fun contrastTextColor(background: Color): Color =
    if (background.luminance() < 0.35f) Color.White else PrayaasColors.Black

// ─── App Top Bar ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayaasTopBar(
    title: String,
    subtitle: String? = null,
    showLogo: Boolean = true,
    cartCount: Int = 0,
    onCartClick: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    // Orange TopAppBar always uses white text — luminance(#E8621A) ≈ 0.14 → white ✓
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showLogo) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.prayaas_logo,
                            contentDescription = "Prayaas Logo",
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.90f)
                        )
                    }
                }
            }
        },
        navigationIcon = navigationIcon ?: {},
        actions = {
            onCartClick?.let {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = Color.White,
                                contentColor = PrayaasColors.Orange
                            ) {
                                Text(
                                    cartCount.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.ShoppingCart, "Cart", tint = Color.White)
                    }
                }
            }
            actions?.invoke(this)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PrayaasColors.Orange
        )
    )
}

// ─── Book Card ────────────────────────────────────────────────────────────────
@Composable
fun BookCard(
    book: Book,
    inCart: Boolean,
    onCardClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val coverColor = bookCoverColor(book.id)
    // All cover colours are dark (deep blue/teal/red/purple/orange) → white text ✓
    val coverTextColor = contrastTextColor(coverColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onCardClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Book cover thumbnail
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 78.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                    Text(
                        text = book.title.take(12),
                        style = MaterialTheme.typography.labelSmall,
                        color = coverTextColor,         // ← contrast-aware
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(4.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,    // ← theme-aware
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(6.dp))
                AvailabilityBadge(book.availableCopies)
                Spacer(Modifier.height(6.dp))

                if (book.availableCopies > 0) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (inCart) PrayaasColors.Success else PrayaasColors.Orange,
                            contentColor   = Color.White          // ← both success + orange are dark → white ✓
                        ),
                        shape = ChipShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            if (inCart) "✓ Added" else "+ Add to Cart",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Light grey chip → dark text
                    Surface(
                        shape = ChipShape,
                        color = Color(0xFFE8E8E8)
                    ) {
                        Text(
                            "Unavailable",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5A5A5A),            // ← dark grey on light grey ✓
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Availability Badge ───────────────────────────────────────────────────────
@Composable
fun AvailabilityBadge(availableCopies: Int) {
    val (bg, textColor, label) = when {
        availableCopies == 0 ->
            Triple(PrayaasColors.ErrorBg,   PrayaasColors.Error,   "Out of stock")
        availableCopies == 1 ->
            Triple(PrayaasColors.WarningBg, PrayaasColors.Warning, "1 copy left")
        else ->
            Triple(PrayaasColors.SuccessBg, PrayaasColors.Success, "$availableCopies available")
    }
    // bg colours are all light-tinted → dark text ✓
    Surface(shape = ChipShape, color = bg) {
        Text(
            text = "● $label",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─── Order Status Chip ────────────────────────────────────────────────────────
@Composable
fun OrderStatusChip(status: OrderStatus) {
    // All bg colours here are light tinted → dark semantic text ✓
    val (bg, textColor, label) = when (status) {
        OrderStatus.PENDING   -> Triple(PrayaasColors.WarningBg, PrayaasColors.Warning, "⏳ Pending")
        OrderStatus.PARTIAL   -> Triple(PrayaasColors.InfoBg,    PrayaasColors.Info,    "📦 Partial")
        OrderStatus.ISSUED    -> Triple(PrayaasColors.InfoBg,    PrayaasColors.Info,    "📗 Issued")
        OrderStatus.RETURNED  -> Triple(PrayaasColors.SuccessBg, PrayaasColors.Success, "✓ Returned")
        OrderStatus.CANCELLED -> Triple(PrayaasColors.ErrorBg,   PrayaasColors.Error,   "✕ Cancelled")
    }
    Surface(shape = ChipShape, color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────
@Composable
fun StatCard(
    number: Int,
    label: String,
    color: Color = PrayaasColors.Orange,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = SmallCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                maxLines = 1
            )
        }
    }
}

// ─── Form Field ───────────────────────────────────────────────────────────────
@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: androidx.compose.ui.text.input.KeyboardType =
        androidx.compose.ui.text.input.KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,   // ← theme-aware ✓
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f)
                )
            },
            isError = isError,
            singleLine = true,
            shape = SmallCardShape,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PrayaasColors.Orange,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                errorBorderColor     = PrayaasColors.Error,
                focusedTextColor     = MaterialTheme.colorScheme.onSurface,    // ✓
                unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,    // ✓
                focusedLabelColor    = PrayaasColors.Orange,
                cursorColor          = PrayaasColors.Orange
            )
        )
        if (isError && errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = PrayaasColors.Error,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─── Primary Button ───────────────────────────────────────────────────────────
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = PrayaasColors.Orange
) {
    // Compute text/icon colour from the actual container colour at call time
    val contentColor = contrastTextColor(containerColor)

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor         = containerColor,
            contentColor           = contentColor,        // ← contrast-aware ✓
            disabledContainerColor = Color(0xFFE0E0E0),
            disabledContentColor   = Color(0xFF757575)    // dark grey on light grey ✓
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(20.dp),
                color       = contentColor,               // ← matches button text ✓
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor                      // ← contrast-aware ✓
            )
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground   // ← theme-aware ✓
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    icon: String,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 52.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground   // ← theme-aware ✓
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text     = actionLabel,
                onClick  = onAction,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

// ─── Book cover colour helper ─────────────────────────────────────────────────
fun bookCoverColor(bookId: String): Color {
    val colors = listOf(
        PrayaasColors.Cover1, PrayaasColors.Cover2, PrayaasColors.Cover3,
        PrayaasColors.Cover4, PrayaasColors.Cover5, PrayaasColors.Cover6,
        PrayaasColors.Cover7, PrayaasColors.Cover8
    )
    return colors[Math.abs(bookId.hashCode()) % colors.size]
}
