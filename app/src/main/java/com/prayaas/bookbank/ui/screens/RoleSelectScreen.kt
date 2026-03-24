package com.prayaas.bookbank.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.prayaas.bookbank.R
import com.prayaas.bookbank.ui.theme.*

@Composable
fun RoleSelectScreen(
    onStudentSelected: () -> Unit,
    onAdminSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative orange arc at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(PrayaasColors.Orange)
                .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo card
            Card(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = R.drawable.prayaas_logo,
                        contentDescription = "Prayaas Logo",
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Prayaas Book Bank",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your college library, simplified",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(56.dp))

            Text(
                text = "Select your role to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Student card
            RoleCard(
                icon = "🎓",
                iconBg = PrayaasColors.OrangeLight,
                title = "Student",
                description = "Browse catalogue, checkout books, track orders",
                onClick = onStudentSelected
            )

            Spacer(Modifier.height(12.dp))

            // Admin card
            RoleCard(
                icon = "🛡",
                iconBg = Color(0xFFE8EAF6),
                title = "Admin",
                description = "Manage orders, issue & receive books via QR scan",
                onClick = onAdminSelected
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Prayaas Book Bank · College Library System",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RoleCard(
    icon: String,
    iconBg: Color,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 26.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PrayaasColors.Gray
            )
        }
    }
}
