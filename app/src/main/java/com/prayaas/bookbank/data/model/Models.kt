package com.prayaas.bookbank.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// ─── Book Catalogue Entry ────────────────────────────────────────────────────
data class Book(
    @DocumentId val id: String = "",
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val subject: String = "",
    val summary: String = "",
    val coverImageUrl: String = "",
    val totalCopies: Int = 0,
    val availableCopies: Int = 0,
    val edition: String = "",
    val pages: Int = 0,
    val publisher: String = ""
)

// ─── Physical Book Copy (individual copy with unique QR) ────────────────────
data class BookCopy(
    @DocumentId val copyId: String = "",
    val bookId: String = "",
    val qrData: String = "",          // JSON: {copyId, bookId, title, author}
    val isAvailable: Boolean = true,
    val currentOrderId: String? = null
)

// ─── Student Order ───────────────────────────────────────────────────────────
data class Order(
    @DocumentId val orderId: String = "",
    val studentName: String = "",
    val studentId: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val books: List<OrderBook> = emptyList(),
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class OrderBook(
    val bookId: String = "",
    val bookTitle: String = "",
    val bookAuthor: String = "",
    val copyId: String? = null,          // assigned when admin scans QR
    val qrCode: String? = null,
    val isReturned: Boolean = false,
    val issuedAt: Timestamp? = null,
    val returnedAt: Timestamp? = null
)

enum class OrderStatus {
    PENDING,    // order placed, not yet issued
    ISSUED,     // all books scanned and given to student
    PARTIAL,    // some books issued, some pending
    RETURNED,   // all books returned
    CANCELLED
}

// ─── Cart Item (local, not persisted) ────────────────────────────────────────
data class CartItem(
    val book: Book
)

// ─── QR Code Payload ─────────────────────────────────────────────────────────
data class QrPayload(
    val copyId: String = "",
    val bookId: String = "",
    val title: String = "",
    val author: String = ""
)

// ─── Admin stats ─────────────────────────────────────────────────────────────
data class DashboardStats(
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val issuedOrders: Int = 0,
    val returnedOrders: Int = 0,
    val totalBooks: Int = 0
)
