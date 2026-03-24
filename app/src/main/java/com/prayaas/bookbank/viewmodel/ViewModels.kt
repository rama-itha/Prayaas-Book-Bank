package com.prayaas.bookbank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayaas.bookbank.data.model.*
import com.prayaas.bookbank.data.repository.BookRepository
import com.prayaas.bookbank.data.repository.LocalDataSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ─── Catalogue ViewModel ──────────────────────────────────────────────────────
class CatalogueViewModel : ViewModel() {
    private val repo = BookRepository()

    private val _localBooks = MutableStateFlow(LocalDataSource.sampleBooks)

    val books: StateFlow<List<Book>> = try {
        repo.getBooksFlow()
            .catch { emit(LocalDataSource.sampleBooks) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDataSource.sampleBooks)
    } catch (e: Exception) {
        _localBooks
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredBooks: StateFlow<List<Book>> = combine(books, searchQuery) { all, q ->
        if (q.isBlank()) all
        else all.filter {
            it.title.contains(q, ignoreCase = true) ||
                    it.author.contains(q, ignoreCase = true) ||
                    it.subject.contains(q, ignoreCase = true) ||
                    it.isbn.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDataSource.sampleBooks)

    fun onSearchQueryChange(q: String) {
        _searchQuery.value = q
    }

    fun seedData() = viewModelScope.launch {
        try { repo.seedSampleBooks() } catch (e: Exception) { /* ignore if offline */ }
    }
}

// ─── Cart ViewModel ───────────────────────────────────────────────────────────
class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<Book>>(emptyList())
    val cartItems: StateFlow<List<Book>> = _cartItems.asStateFlow()

    val cartCount: StateFlow<Int> = _cartItems
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addToCart(book: Book) {
        if (_cartItems.value.none { it.id == book.id }) {
            _cartItems.value = _cartItems.value + book
        }
    }

    fun removeFromCart(bookId: String) {
        _cartItems.value = _cartItems.value.filter { it.id != bookId }
    }

    fun isInCart(bookId: String): Boolean =
        _cartItems.value.any { it.id == bookId }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}

// ─── Checkout ViewModel ───────────────────────────────────────────────────────
class CheckoutViewModel : ViewModel() {
    private val repo = BookRepository()

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    var lastStudentName: String = ""
        private set
    var lastStudentEmail: String = ""
        private set

    fun placeOrder(
        name: String,
        studentId: String,
        mobile: String,
        email: String,
        books: List<Book>
    ) {
        lastStudentName = name
        lastStudentEmail = email
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            val result = try {
                repo.placeOrder(name, studentId, mobile, email, books)
            } catch (e: Exception) {
                // Demo / offline mode: generate a local order ID so the flow continues
                val localOrderId = "DEMO-${System.currentTimeMillis().toString().takeLast(8)}"
                Result.success(localOrderId)
            }
            _orderState.value = if (result.isSuccess) {
                OrderState.Success(result.getOrThrow())
            } else {
                OrderState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _orderState.value = OrderState.Idle
    }
}

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    data class Success(val orderId: String) : OrderState()
    data class Error(val message: String) : OrderState()
}

// ─── Admin ViewModel ──────────────────────────────────────────────────────────
class AdminViewModel : ViewModel() {
    private val repo = BookRepository()

    private val _localOrders = MutableStateFlow(LocalDataSource.sampleOrders)

    val allOrders: StateFlow<List<Order>> = try {
        repo.getOrdersFlow()
            .catch { emit(LocalDataSource.sampleOrders) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDataSource.sampleOrders)
    } catch (e: Exception) {
        _localOrders
    }

    private val _selectedTab = MutableStateFlow(AdminTab.ALL)
    val selectedTab: StateFlow<AdminTab> = _selectedTab.asStateFlow()

    val filteredOrders: StateFlow<List<Order>> = combine(allOrders, selectedTab) { orders, tab ->
        when (tab) {
            AdminTab.ALL      -> orders
            AdminTab.PENDING  -> orders.filter { it.status == OrderStatus.PENDING }
            AdminTab.ISSUED   -> orders.filter {
                it.status == OrderStatus.ISSUED || it.status == OrderStatus.PARTIAL
            }
            AdminTab.RETURNED -> orders.filter { it.status == OrderStatus.RETURNED }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDataSource.sampleOrders)

    val dashboardStats: StateFlow<DashboardStats> = allOrders.map { orders ->
        DashboardStats(
            totalOrders    = orders.size,
            pendingOrders  = orders.count { it.status == OrderStatus.PENDING },
            issuedOrders   = orders.count {
                it.status == OrderStatus.ISSUED || it.status == OrderStatus.PARTIAL
            },
            returnedOrders = orders.count { it.status == OrderStatus.RETURNED }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun selectTab(tab: AdminTab) {
        _selectedTab.value = tab
    }

    fun issueBook(orderId: String, bookId: String, qrPayload: QrPayload) {
        viewModelScope.launch {
            _scanState.value = ScanState.Processing
            val result = try {
                repo.issueBookCopy(orderId, bookId, qrPayload)
            } catch (e: Exception) {
                updateLocalOrderIssued(orderId, bookId, qrPayload)
                Result.success(Unit)
            }
            _scanState.value = if (result.isSuccess) {
                ScanState.Success("Book issued successfully!")
            } else {
                ScanState.Error(result.exceptionOrNull()?.message ?: "Issue failed")
            }
        }
    }

    fun returnBook(orderId: String, qrPayload: QrPayload) {
        viewModelScope.launch {
            _scanState.value = ScanState.Processing
            val result = try {
                repo.returnBookCopy(orderId, qrPayload)
            } catch (e: Exception) {
                updateLocalOrderReturned(orderId, qrPayload)
                Result.success(Unit)
            }
            _scanState.value = if (result.isSuccess) {
                ScanState.Success("Return recorded successfully!")
            } else {
                ScanState.Error(result.exceptionOrNull()?.message ?: "Return failed")
            }
        }
    }

    fun resetScanState() {
        _scanState.value = ScanState.Idle
    }

    private fun updateLocalOrderIssued(orderId: String, bookId: String, payload: QrPayload) {
        val current = _localOrders.value.toMutableList()
        val idx = current.indexOfFirst { it.orderId == orderId }
        if (idx >= 0) {
            val order = current[idx]
            val updatedBooks = order.books.map { ob ->
                if (ob.bookId == bookId && ob.copyId == null)
                    ob.copy(copyId = payload.copyId, qrCode = payload.copyId)
                else ob
            }
            val allIssued = updatedBooks.all { it.copyId != null }
            current[idx] = order.copy(
                books  = updatedBooks,
                status = if (allIssued) OrderStatus.ISSUED else OrderStatus.PARTIAL
            )
            _localOrders.value = current
        }
    }

    private fun updateLocalOrderReturned(orderId: String, payload: QrPayload) {
        val current = _localOrders.value.toMutableList()
        val idx = current.indexOfFirst { it.orderId == orderId }
        if (idx >= 0) {
            val order = current[idx]
            val updatedBooks = order.books.map { ob ->
                if (ob.copyId == payload.copyId) ob.copy(isReturned = true) else ob
            }
            val allReturned = updatedBooks.all { it.isReturned }
            current[idx] = order.copy(
                books  = updatedBooks,
                status = if (allReturned) OrderStatus.RETURNED else OrderStatus.ISSUED
            )
            _localOrders.value = current
        }
    }
}

enum class AdminTab { ALL, PENDING, ISSUED, RETURNED }

sealed class ScanState {
    object Idle       : ScanState()
    object Scanning   : ScanState()
    object Processing : ScanState()
    data class Success(val message: String) : ScanState()
    data class Error(val message: String)   : ScanState()
}
