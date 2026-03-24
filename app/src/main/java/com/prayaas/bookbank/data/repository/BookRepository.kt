package com.prayaas.bookbank.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.prayaas.bookbank.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BookRepository {

    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()

    private val booksCollection = db.collection("books")
    private val copiesCollection = db.collection("bookCopies")
    private val ordersCollection = db.collection("orders")

    // ─── BOOKS ────────────────────────────────────────────────────────────────

    fun getBooksFlow(): Flow<List<Book>> = callbackFlow {
        val listener = booksCollection
            .orderBy("title")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookRepo", "getBooksFlow error", error)
                    return@addSnapshotListener
                }
                val books = snapshot?.documents?.mapNotNull {
                    it.toObject(Book::class.java)
                } ?: emptyList()
                trySend(books)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getBookById(bookId: String): Book? {
        return try {
            booksCollection.document(bookId).get().await().toObject(Book::class.java)
        } catch (e: Exception) {
            Log.e("BookRepo", "getBookById error", e)
            null
        }
    }

    suspend fun searchBooks(query: String): List<Book> {
        return try {
            // Firestore doesn't support full-text search natively.
            // For production, integrate Algolia or Typesense.
            // This does a prefix search on title.
            booksCollection
                .orderBy("title")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .get()
                .await()
                .toObjects(Book::class.java)
        } catch (e: Exception) {
            Log.e("BookRepo", "searchBooks error", e)
            emptyList()
        }
    }

    // ─── BOOK COPIES ──────────────────────────────────────────────────────────

    suspend fun getAvailableCopy(bookId: String): BookCopy? {
        return try {
            copiesCollection
                .whereEqualTo("bookId", bookId)
                .whereEqualTo("isAvailable", true)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(BookCopy::class.java)
        } catch (e: Exception) {
            Log.e("BookRepo", "getAvailableCopy error", e)
            null
        }
    }

    suspend fun getCopyByQr(qrData: String): BookCopy? {
        return try {
            copiesCollection
                .whereEqualTo("qrData", qrData)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(BookCopy::class.java)
        } catch (e: Exception) {
            Log.e("BookRepo", "getCopyByQr error", e)
            null
        }
    }

    // ─── ORDERS ───────────────────────────────────────────────────────────────

    suspend fun placeOrder(
        studentName: String,
        studentId: String,
        mobile: String,
        email: String,
        cartItems: List<Book>
    ): Result<String> {
        return try {
            val orderBooks = cartItems.map { book ->
                mapOf(
                    "bookId" to book.id,
                    "bookTitle" to book.title,
                    "bookAuthor" to book.author,
                    "copyId" to null,
                    "qrCode" to null,
                    "isReturned" to false,
                    "issuedAt" to null,
                    "returnedAt" to null
                )
            }

            val orderData = hashMapOf(
                "studentName" to studentName,
                "studentId" to studentId,
                "mobileNumber" to mobile,
                "email" to email,
                "books" to orderBooks,
                "status" to OrderStatus.PENDING.name,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            val ref = ordersCollection.add(orderData).await()

            // Decrement available copies for each book
            cartItems.forEach { book ->
                val newCount = maxOf(0, book.availableCopies - 1)
                booksCollection.document(book.id)
                    .update("availableCopies", newCount)
                    .await()
            }

            // Send confirmation email via Firebase Function
            sendConfirmationEmail(email, studentName, cartItems)

            Result.success(ref.id)
        } catch (e: Exception) {
            Log.e("BookRepo", "placeOrder error", e)
            Result.failure(e)
        }
    }

    fun getOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookRepo", "getOrdersFlow error", error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull {
                    it.toObject(Order::class.java)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("status", status.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val orders = snapshot?.documents?.mapNotNull {
                    it.toObject(Order::class.java)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Admin: Issue a book copy to a student after scanning QR.
     * Validates the qr copy belongs to the correct book in the order.
     */
    suspend fun issueBookCopy(
        orderId: String,
        bookId: String,
        scannedQrPayload: QrPayload
    ): Result<Unit> {
        return try {
            // Verify the scanned copy belongs to the expected book
            if (scannedQrPayload.bookId != bookId) {
                return Result.failure(Exception("QR code does not match the expected book!"))
            }

            val orderRef = ordersCollection.document(orderId)
            val orderDoc = orderRef.get().await()
            val order = orderDoc.toObject(Order::class.java)
                ?: return Result.failure(Exception("Order not found"))

            // Update the specific book entry in the order
            val updatedBooks = order.books.map { ob ->
                if (ob.bookId == bookId && ob.copyId == null) {
                    ob.copy(
                        copyId = scannedQrPayload.copyId,
                        qrCode = scannedQrPayload.copyId,
                        issuedAt = Timestamp.now()
                    )
                } else ob
            }

            // Check if all books have been issued
            val allIssued = updatedBooks.all { it.copyId != null }
            val newStatus = if (allIssued) OrderStatus.ISSUED else OrderStatus.PARTIAL

            val booksData = updatedBooks.map { ob ->
                mapOf(
                    "bookId" to ob.bookId,
                    "bookTitle" to ob.bookTitle,
                    "bookAuthor" to ob.bookAuthor,
                    "copyId" to ob.copyId,
                    "qrCode" to ob.qrCode,
                    "isReturned" to ob.isReturned,
                    "issuedAt" to ob.issuedAt,
                    "returnedAt" to ob.returnedAt
                )
            }

            orderRef.update(
                mapOf(
                    "books" to booksData,
                    "status" to newStatus.name,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            // Mark copy as unavailable
            copiesCollection
                .whereEqualTo("qrData", scannedQrPayload.copyId)
                .get().await()
                .documents.firstOrNull()
                ?.reference?.update(
                    mapOf(
                        "isAvailable" to false,
                        "currentOrderId" to orderId
                    )
                )?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BookRepo", "issueBookCopy error", e)
            Result.failure(e)
        }
    }

    /**
     * Admin: Mark a book as returned after scanning QR on return.
     * Validates the returned copy is the same one issued to this student.
     */
    suspend fun returnBookCopy(
        orderId: String,
        scannedQrPayload: QrPayload
    ): Result<Unit> {
        return try {
            val orderRef = ordersCollection.document(orderId)
            val orderDoc = orderRef.get().await()
            val order = orderDoc.toObject(Order::class.java)
                ?: return Result.failure(Exception("Order not found"))

            // Find the matching book entry by copyId
            val matchingBook = order.books.find { it.copyId == scannedQrPayload.copyId }
                ?: return Result.failure(Exception("This copy was not issued under this order!"))

            if (matchingBook.isReturned) {
                return Result.failure(Exception("This book has already been marked as returned."))
            }

            val updatedBooks = order.books.map { ob ->
                if (ob.copyId == scannedQrPayload.copyId) {
                    ob.copy(isReturned = true, returnedAt = Timestamp.now())
                } else ob
            }

            val allReturned = updatedBooks.all { it.isReturned }
            val newStatus = if (allReturned) OrderStatus.RETURNED else OrderStatus.ISSUED

            val booksData = updatedBooks.map { ob ->
                mapOf(
                    "bookId" to ob.bookId,
                    "bookTitle" to ob.bookTitle,
                    "bookAuthor" to ob.bookAuthor,
                    "copyId" to ob.copyId,
                    "qrCode" to ob.qrCode,
                    "isReturned" to ob.isReturned,
                    "issuedAt" to ob.issuedAt,
                    "returnedAt" to ob.returnedAt
                )
            }

            orderRef.update(
                mapOf(
                    "books" to booksData,
                    "status" to newStatus.name,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            // Re-increment available copies and free the copy
            booksCollection.document(matchingBook.bookId)
                .get().await()
                .toObject(Book::class.java)?.let { book ->
                    booksCollection.document(matchingBook.bookId)
                        .update("availableCopies", book.availableCopies + 1)
                        .await()
                }

            copiesCollection
                .whereEqualTo("qrData", scannedQrPayload.copyId)
                .get().await()
                .documents.firstOrNull()
                ?.reference?.update(
                    mapOf(
                        "isAvailable" to true,
                        "currentOrderId" to null
                    )
                )?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BookRepo", "returnBookCopy error", e)
            Result.failure(e)
        }
    }

    // ─── EMAIL ────────────────────────────────────────────────────────────────

    private fun sendConfirmationEmail(
        email: String,
        studentName: String,
        books: List<Book>
    ) {
        val bookList = books.joinToString(separator = "<br>") {
            "• <b>${it.title}</b> by ${it.author}"
        }
        val data = hashMapOf(
            "to" to email,
            "studentName" to studentName,
            "bookList" to bookList
        )
        functions.getHttpsCallable("sendBookConfirmationEmail")
            .call(data)
            .addOnSuccessListener { Log.d("BookRepo", "Email sent to $email") }
            .addOnFailureListener { Log.e("BookRepo", "Email failed", it) }
    }

    // ─── ADMIN SEED (call once to populate test data) ─────────────────────────

    suspend fun seedSampleBooks() {
        val sampleBooks = listOf(
            hashMapOf(
                "title" to "Data Structures and Algorithms",
                "author" to "Thomas H. Cormen",
                "isbn" to "978-0262033848",
                "subject" to "Computer Science",
                "summary" to "The definitive reference on algorithms covering sorting, searching, graph algorithms, and dynamic programming with rigorous mathematical foundations. Used worldwide in CS programs.",
                "coverImageUrl" to "",
                "totalCopies" to 6,
                "availableCopies" to 4,
                "edition" to "4th Edition",
                "pages" to 1292,
                "publisher" to "MIT Press"
            ),
            hashMapOf(
                "title" to "Operating System Concepts",
                "author" to "Abraham Silberschatz",
                "isbn" to "978-1119800361",
                "subject" to "Computer Science",
                "summary" to "Known as the Dinosaur Book. Covers process management, memory management, file systems, storage, and security. The standard OS textbook used globally.",
                "coverImageUrl" to "",
                "totalCopies" to 4,
                "availableCopies" to 1,
                "edition" to "10th Edition",
                "pages" to 976,
                "publisher" to "Wiley"
            ),
            hashMapOf(
                "title" to "Computer Networks",
                "author" to "Andrew S. Tanenbaum",
                "isbn" to "978-0132126953",
                "subject" to "Computer Science",
                "summary" to "A comprehensive guide to computer networking covering TCP/IP, routing algorithms, the application layer, and network security with practical real-world examples.",
                "coverImageUrl" to "",
                "totalCopies" to 8,
                "availableCopies" to 6,
                "edition" to "5th Edition",
                "pages" to 960,
                "publisher" to "Pearson"
            ),
            hashMapOf(
                "title" to "Database System Concepts",
                "author" to "Henry F. Korth",
                "isbn" to "978-0078022159",
                "subject" to "Computer Science",
                "summary" to "Covers relational algebra, SQL, database design, normalization, query optimization, transactions, and distributed databases. Essential for DBMS courses.",
                "coverImageUrl" to "",
                "totalCopies" to 3,
                "availableCopies" to 0,
                "edition" to "7th Edition",
                "pages" to 1376,
                "publisher" to "McGraw-Hill"
            ),
            hashMapOf(
                "title" to "Compilers: Principles, Techniques, and Tools",
                "author" to "Alfred V. Aho",
                "isbn" to "978-0321486813",
                "subject" to "Computer Science",
                "summary" to "The Dragon Book — the definitive compiler construction text covering lexical analysis, parsing, semantic analysis, intermediate code generation, and optimization.",
                "coverImageUrl" to "",
                "totalCopies" to 5,
                "availableCopies" to 3,
                "edition" to "2nd Edition",
                "pages" to 1009,
                "publisher" to "Pearson"
            )
        )

        sampleBooks.forEach { book ->
            val ref = booksCollection.add(book).await()
            // Create 2 physical copies per book with unique QR codes
            val total = (book["totalCopies"] as Int)
            repeat(total) { i ->
                val copyId = "${ref.id.take(6).uppercase()}-${(i + 1).toString().padStart(3, '0')}"
                copiesCollection.add(
                    hashMapOf(
                        "bookId" to ref.id,
                        "qrData" to copyId,
                        "isAvailable" to (i < (book["availableCopies"] as Int)),
                        "currentOrderId" to null
                    )
                ).await()
            }
        }
    }
}
