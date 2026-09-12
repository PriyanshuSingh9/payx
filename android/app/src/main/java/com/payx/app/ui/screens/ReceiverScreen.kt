package com.payx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payx.app.R
import com.payx.app.data.PaymentDto
import com.payx.app.payments.ReceiverViewModel
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.theme.PayxPalette

private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

private val MontaguSlab = FontFamily(Font(R.font.montagu_slab))

@Composable
fun ReceiverScreen(
    onBack: () -> Unit = {},
    onTrack: (paymentId: String) -> Unit = {},
    viewModel: ReceiverViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchInput by remember { mutableStateOf(state.searchQuery) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val totalInrStr = "₹${"%,.2f".format(state.dashboard.totalReceivedInr)}"
    val totalUsdStr = "≈ $${"%,.2f".format(state.dashboard.totalReceivedUsd)} USDC"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Top App Bar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color(0xFF14131C),
                    border = BorderStroke(1.dp, Color(0xFF1F1D2B)),
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Inbound Transfers",
                        style = TextStyle(
                            fontFamily = MontaguSlab,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Live Receiver Dashboard",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = PayxPalette.SoftLavender
                        )
                    )
                }

                IconButton(
                    onClick = { viewModel.refresh(searchInput) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = PayxPalette.SoftLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Total Received Metric Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF14131C),
                border = BorderStroke(1.dp, Color(0xFF22202E)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL RECEIVED",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp,
                                color = Color(0xFF8E8B9C)
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34D399))
                            )
                            IndiaFlag(width = 16.dp, height = 11.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = totalInrStr,
                        style = TextStyle(
                            fontFamily = MontaguSlab,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = totalUsdStr,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF34D399)
                            )
                        )
                        Text(
                            text = "• ${state.dashboard.count} transfers",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.sp,
                                color = Color(0xFF7E7B94)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Search Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF14131C),
                border = BorderStroke(1.dp, Color(0xFF1F1D2B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF6B6880),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchInput,
                        onValueChange = {
                            searchInput = it
                            viewModel.onSearchQueryChange(it)
                        },
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.5.sp,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(PayxPalette.VividPurple),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (searchInput.isEmpty()) {
                                Text(
                                    text = "Filter by recipient name or UPI handle...",
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 13.sp,
                                        color = Color(0xFF5A5672)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Inbound Transactions Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Incoming Transfers",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                        color = Color.White
                    )
                )

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = PayxPalette.VividPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transactions List
            val payments = state.dashboard.payments
            if (payments.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No incoming payments yet",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF7E7B94)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Inbound transfers settled via Solana will appear here.",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                color = Color(0xFF5A5672)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(payments, key = { it.id }) { payment ->
                        ReceiverPaymentCard(
                            payment = payment,
                            onClick = { onTrack(payment.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiverPaymentCard(
    payment: PaymentDto,
    onClick: () -> Unit
) {
    val initial = payment.recipient.name.firstOrNull()?.uppercaseChar()?.toString() ?: "R"
    val avatarColor = when (initial) {
        "P" -> Color(0xFFE91E63)
        "R" -> Color(0xFF2E7D32)
        "S" -> Color(0xFFF4511E)
        "A" -> Color(0xFF3B82F6)
        else -> Color(0xFF8B5CF6)
    }

    val inrFormatted = payment.destinationAmount?.let { "₹${"%,.2f".format(it)}" }
        ?: "₹${"%,.2f".format(payment.sourceAmount * (payment.exchangeRate ?: 92.9))}"

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF14131C),
        border = BorderStroke(1.dp, Color(0xFF1F1D2B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Initial Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.2f))
                        .border(1.dp, avatarColor.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = avatarColor
                        )
                    )
                }

                Column {
                    Text(
                        text = payment.recipient.name.ifBlank { "Inbound Transfer" },
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = payment.recipient.upiId ?: "Sender: ${payment.senderWallet.take(4)}...${payment.senderWallet.takeLast(4)}",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.5.sp,
                            color = Color(0xFF7E7B94)
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+$inrFormatted",
                    style = TextStyle(
                        fontFamily = MontaguSlab,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$${"%,.2f".format(payment.sourceAmount)} USDC",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        color = Color(0xFF7E7B94)
                    )
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View receipt",
                tint = Color(0xFF4A4660),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
