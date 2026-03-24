package com.prayaas.bookbank.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.compose.foundation.Image
import com.prayaas.bookbank.data.model.Book
import com.prayaas.bookbank.ui.components.*
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.utils.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrGeneratorScreen(
    books: List<Book>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("QR Code Generator", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Generate codes for physical copies",
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = SmallCardShape,
                    colors = CardDefaults.cardColors(containerColor = PrayaasColors.InfoBg)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ℹ️", fontSize = 14.sp)
                        Text(
                            "Each QR code encodes a unique copy ID. Print these codes and stick one on each physical book. The admin scans these codes when issuing and receiving returns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrayaasColors.Info
                        )
                    }
                }
            }

            itemsIndexed(books) { _, book ->
                BookQrSection(book = book)
            }
        }
    }
}

@Composable
private fun BookQrSection(book: Book) {
    val context = LocalContext.current
    val coverColor = bookCoverColor(book.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Book header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(coverColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        book.title.take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        book.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        "${book.author} · ${book.totalCopies} copies",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }

            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            // QR grid — one per copy
            val columns = 3
            val copies = (1..book.totalCopies).toList()
            copies.chunked(columns).forEach { rowCopies ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    rowCopies.forEach { copyNum ->
                        val copyId = buildCopyId(book, copyNum)
                        BookCopyQrCard(
                            copyId  = copyId,
                            bookId  = book.id,
                            title   = book.title,
                            author  = book.author,
                            copyNum = copyNum,
                            modifier = Modifier.weight(1f),
                            onShare = { bitmap ->
                                shareBitmap(context, bitmap, copyId)
                            }
                        )
                    }
                    // Fill remaining columns if row is partial
                    repeat(columns - rowCopies.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCopyQrCard(
    copyId: String,
    bookId: String,
    title: String,
    author: String,
    copyNum: Int,
    modifier: Modifier = Modifier,
    onShare: (Bitmap) -> Unit
) {
    val qrBitmap = remember(copyId) {
        QrCodeGenerator.generateQrBitmap(
            copyId = copyId,
            bookId = bookId,
            title  = title,
            author = author,
            size   = 256
        )
    }

    val isAvailableColor = if (copyNum <= 4) PrayaasColors.SuccessBg else PrayaasColors.WarningBg

    Card(
        modifier = modifier,
        shape = SmallCardShape,
        colors = CardDefaults.cardColors(containerColor = isAvailableColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                bitmap             = qrBitmap.asImageBitmap(),
                contentDescription = "QR code for $copyId",
                modifier           = Modifier.size(80.dp)
            )
            Text(
                text      = copyId,
                style     = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color     = PrayaasColors.Black,
                textAlign = TextAlign.Center,
                maxLines  = 2,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick  = { onShare(qrBitmap) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share QR",
                    tint   = PrayaasColors.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun buildCopyId(book: Book, copyNum: Int): String {
    val prefix = book.title
        .split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercaseChar() ?: 'X' }
        .joinToString("")
    val shortId = book.id.take(4).uppercase()
    return "$prefix-$shortId-${copyNum.toString().padStart(3, '0')}"
}

private fun shareBitmap(context: android.content.Context, bitmap: Bitmap, copyId: String) {
    try {
        val file = File(context.cacheDir, "qr_$copyId.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type  = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "QR Code for book copy: $copyId")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        android.util.Log.e("QrGenerator", "Share failed", e)
    }
}
