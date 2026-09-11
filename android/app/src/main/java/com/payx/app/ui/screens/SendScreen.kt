package com.payx.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.R
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.theme.PayxPalette
import java.text.DecimalFormat

private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

private val MontaguSlab = FontFamily(Font(R.font.montagu_slab))

data class Recipient(
    val id: String,
    val name: String,
    val email: String,
    val avatarInitials: String,
    val gradientColors: List<Color>
)

private val sampleRecipients = listOf(
    Recipient(
        id = "1",
        name = "Priya Sharma",
        email = "priya.sharma@remitflow.demo",
        avatarInitials = "PS",
        gradientColors = listOf(Color(0xFFE91E63), Color(0xFFF43F5E))
    ),
    Recipient(
        id = "2",
        name = "Rahul Verma",
        email = "rahul.verma@remitflow.demo",
        avatarInitials = "RV",
        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
    ),
    Recipient(
        id = "3",
        name = "Sarah Smith",
        email = "sarah.smith@remitflow.demo",
        avatarInitials = "SS",
        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))
    )
)

@Composable
fun SendScreen(
    onBack: () -> Unit = {},
    onSubmitted: (String) -> Unit
) {
    var selectedRecipient by remember { mutableStateOf<Recipient?>(null) }
    val screenBackground = Color(0xFF09090D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {
        AnimatedContent(
            targetState = selectedRecipient,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SendFlowTransition"
        ) { recipient ->
            if (recipient == null) {
                // Step 1: Send To
                SendToScreen(
                    onBack = onBack,
                    onRecipientSelected = { selectedRecipient = it }
                )
            } else {
                // Step 2: Send Money
                SendMoneyScreen(
                    recipient = recipient,
                    onBack = { selectedRecipient = null },
                    onConfirm = { onSubmitted("tx_${recipient.id}_${System.currentTimeMillis()}") }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 1: "Send To" Screen with Refined Hierarchy & Line-Free Layout
// -----------------------------------------------------------------------------
@Composable
private fun SendToScreen(
    onBack: () -> Unit,
    onRecipientSelected: (Recipient) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecipients = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            sampleRecipients
        } else {
            sampleRecipients.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top App Bar with back button and centered "Send To" title
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

            Text(
                text = "Send To",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar Capsule: "Search by name or email"
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF14131C),
            border = BorderStroke(1.dp, Color(0xFF1F1D2B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF9881F5),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(Color(0xFF9881F5)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search by name or email",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF6B6880)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Typographic Visual Hierarchy Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 12.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT RECIPIENTS",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF6B6880)
                )
            )

            Text(
                text = "${filteredRecipients.size} CONTACTS",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFF4A4660)
                )
            )
        }

        // Recipient List Cards without Lines
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredRecipients, key = { it.id }) { recipient ->
                RecipientItemCard(
                    recipient = recipient,
                    onClick = { onRecipientSelected(recipient) }
                )
            }
        }
    }
}

@Composable
private fun RecipientItemCard(
    recipient: Recipient,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF14131C),
        border = BorderStroke(1.dp, Color(0xFF1F1D2B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Avatar with Unique Gradient
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(recipient.gradientColors))
                        .border(1.dp, Color(0x33A78BFA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = recipient.avatarInitials,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = recipient.name,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = recipient.email,
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF7E7B94)
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IndiaFlag(width = 22.dp, height = 15.dp)

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1B1926),
                    border = BorderStroke(1.dp, Color(0xFF282538)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select",
                            tint = Color(0xFF9881F5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 2: "Send Money" Screen
// -----------------------------------------------------------------------------
@Composable
private fun SendMoneyScreen(
    recipient: Recipient,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    var amountInput by remember { mutableStateOf("2000") }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val feeRate = 0.0325
    val feeAmount = amountDouble * feeRate
    val exchangeRate = 92.90
    val netSend = (amountDouble - feeAmount).coerceAtLeast(0.0)
    val receiveInr = netSend * exchangeRate

    val inrFormatter = remember { DecimalFormat("#,##,##0.00") }
    val usdFormatter = remember { DecimalFormat("#,##0.00") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Top Bar with back button and centered "Send Money" title
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

                Text(
                    text = "Send Money",
                    style = TextStyle(
                        fontFamily = MontaguSlab,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recipient Profile Avatar
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = Color(0x55A855F7),
                        ambientColor = Color(0x33A855F7)
                    )
                    .clip(CircleShape)
                    .background(Brush.linearGradient(recipient.gradientColors))
                    .border(2.dp, Color(0x55B55CF8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recipient.avatarInitials,
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recipient Name
            Text(
                text = recipient.name,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Recipient Email
            Text(
                text = recipient.email,
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF7E7B94)
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            // EXACTLY CENTERED AMOUNT DISPLAY ($ 2000)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$",
                    style = TextStyle(
                        fontFamily = MontaguSlab,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = amountInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            amountInput = it
                        }
                    },
                    textStyle = TextStyle(
                        fontFamily = MontaguSlab,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(PayxPalette.VividPurple),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .widthIn(min = 24.dp)
                        .width(androidx.compose.foundation.layout.IntrinsicSize.Min)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // FEE BREAKDOWN Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF14131C),
                border = BorderStroke(1.dp, Color(0xFF1F1D2B)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Row: FEE BREAKDOWN • LIVE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FEE BREAKDOWN",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFF6B6880)
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PayxPalette.VividPurple)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LIVE",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF9881F5)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // You send row
                    BreakdownRow(
                        label = "You send",
                        value = "$${usdFormatter.format(amountDouble)}"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total fees row inside highlighted capsule
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1B1926),
                        border = BorderStroke(1.dp, Color(0xFF2B273D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total fees (3.25%)",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE2E0EC)
                                )
                            )

                            Text(
                                text = "-$${usdFormatter.format(feeAmount)}",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exchange rate row
                    BreakdownRow(
                        label = "Exchange rate",
                        value = "1 USD = ₹92.90"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Priya receives row (Prominent)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${recipient.name.substringBefore(" ")} receives",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "₹${inrFormatter.format(receiveInr)}",
                            style = TextStyle(
                                fontFamily = MontaguSlab,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footnote: Estimated delivery
                    Text(
                        text = "Estimated delivery: 5-10 minutes via UPI",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF7E7B94)
                        )
                    )
                }
            }
        }

        // Bottom Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(56.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0x66A855F7),
                    ambientColor = Color(0x33A855F7)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                    )
                )
                .clickable(onClick = onConfirm),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SEND MONEY",
                style = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF7E7B94)
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
    }
}
