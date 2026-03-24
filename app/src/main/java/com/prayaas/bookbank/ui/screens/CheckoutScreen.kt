package com.prayaas.bookbank.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayaas.bookbank.data.model.Book
import com.prayaas.bookbank.ui.components.*
import com.prayaas.bookbank.ui.theme.*
import com.prayaas.bookbank.viewmodel.CheckoutViewModel
import com.prayaas.bookbank.viewmodel.OrderState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<Book>,
    checkoutViewModel: CheckoutViewModel,
    onBackClick: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var idError by remember { mutableStateOf("") }
    var mobileError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    val orderState by checkoutViewModel.orderState.collectAsState()

    // Navigate on success
    LaunchedEffect(orderState) {
        if (orderState is OrderState.Success) {
            onOrderPlaced((orderState as OrderState.Success).orderId)
            checkoutViewModel.resetState()
        }
    }

    fun validate(): Boolean {
        nameError = if (name.isBlank()) "Name is required" else ""
        idError = if (studentId.isBlank()) "College ID is required" else ""
        mobileError = when {
            mobile.isBlank() -> "Mobile number is required"
            mobile.length < 10 -> "Enter a valid 10-digit number"
            else -> ""
        }
        emailError = when {
            email.isBlank() -> "Email is required"
            !email.contains("@") -> "Enter a valid email address"
            else -> ""
        }
        return listOf(nameError, idError, mobileError, emailError).all { it.isBlank() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Checkout", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Fill in your details", style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Student details section
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Your Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    FormField(
                        label = "Full Name *",
                        value = name,
                        onValueChange = { name = it; nameError = "" },
                        placeholder = "e.g. Rahul Sharma",
                        isError = nameError.isNotBlank(),
                        errorMessage = nameError
                    )

                    FormField(
                        label = "College ID Number *",
                        value = studentId,
                        onValueChange = { studentId = it; idError = "" },
                        placeholder = "e.g. 2021CS001",
                        isError = idError.isNotBlank(),
                        errorMessage = idError
                    )

                    FormField(
                        label = "Mobile Number *",
                        value = mobile,
                        onValueChange = { mobile = it; mobileError = "" },
                        placeholder = "e.g. 9876543210",
                        isError = mobileError.isNotBlank(),
                        errorMessage = mobileError,
                        keyboardType = KeyboardType.Phone
                    )

                    FormField(
                        label = "Email Address *",
                        value = email,
                        onValueChange = { email = it; emailError = "" },
                        placeholder = "you@college.edu",
                        isError = emailError.isNotBlank(),
                        errorMessage = emailError,
                        keyboardType = KeyboardType.Email
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Order summary section
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = PrayaasColors.OrangeLight),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrayaasColors.OrangeDark,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    cartItems.forEach { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        bookCoverColor(book.id),
                                        androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    book.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    book.author,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            }
                        }
                        if (book != cartItems.last()) {
                            HorizontalDivider(
                                color = PrayaasColors.Orange.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = PrayaasColors.Orange.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "${cartItems.size} book(s) to be issued",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PrayaasColors.OrangeDark
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Email notice
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("📧", fontSize = 13.sp)
                Text(
                    "A confirmation email will be sent to your email address with book details and pickup instructions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Error state
            if (orderState is OrderState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrayaasColors.ErrorBg),
                    shape = SmallCardShape
                ) {
                    Text(
                        "Error: ${(orderState as OrderState.Error).message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrayaasColors.Error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Submit button
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PrimaryButton(
                    text = "✓ Place Order",
                    onClick = {
                        if (validate()) {
                            checkoutViewModel.placeOrder(name, studentId, mobile, email, cartItems)
                        }
                    },
                    loading = orderState is OrderState.Loading
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Order Success Screen ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSuccessScreen(
    orderId: String,
    studentName: String,
    studentEmail: String,
    cartItems: List<Book>,
    onBackToCatalogue: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(32.dp))

            // Success icon
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(PrayaasColors.SuccessBg, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 40.sp, color = PrayaasColors.Success, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Order Placed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Your books have been reserved",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Confirmation card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Order #${orderId.take(8).uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Hi, $studentName!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Confirmation sent to $studentEmail",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        "Books checked out:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    cartItems.forEach { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(bookCoverColor(book.id), androidx.compose.foundation.shape.CircleShape)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(book.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text(book.author, style = MaterialTheme.typography.labelSmall, color = PrayaasColors.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pickup info
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = SmallCardShape,
                colors = CardDefaults.cardColors(containerColor = PrayaasColors.InfoBg)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📍", fontSize = 14.sp)
                    Column {
                        Text("Pickup Instructions", style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold, color = PrayaasColors.Info)
                        Text(
                            "Collect your books from the Prayaas Book Bank counter during working hours (10:00 AM – 5:00 PM, Mon–Sat).",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrayaasColors.Info.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                PrimaryButton(
                    text = "← Back to Catalogue",
                    onClick = onBackToCatalogue
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
