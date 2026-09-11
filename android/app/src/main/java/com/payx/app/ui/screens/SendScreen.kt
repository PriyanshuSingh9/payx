package com.payx.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.data.AddressBookRecipient
import com.payx.app.data.OffRampQuoteDto
import com.payx.app.payments.SendUiState
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.theme.PayxPalette
import java.text.DecimalFormat

@Composable
fun SendScreen(
    state: SendUiState,
    onSearchQueryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onRefreshRecipients: () -> Unit,
    onSubmit: (AddressBookRecipient) -> Unit,
    onBack: () -> Unit = {}
) {
    var selectedRecipient by remember { mutableStateOf<AddressBookRecipient?>(null) }

    val ambientBackground = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF191328),
            0.35f to Color(0xFF110E1A),
            0.70f to PayxPalette.Obsidian,
            1.0f to PayxPalette.Obsidian
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientBackground)
    ) {
        // Subtle topography lines matching LoginScreen
        Canvas(modifier = Modifier.fillMaxSize()) {
            val purpleGlow = Color(0x14B866FC)
            val path1 = Path().apply {
                moveTo(0f, size.height * 0.18f)
                cubicTo(
                    size.width * 0.35f, size.height * 0.12f,
                    size.width * 0.7f, size.height * 0.28f,
                    size.width, size.height * 0.22f
                )
            }
            drawPath(path1, color = purpleGlow, style = Stroke(width = 1.5.dp.toPx()))
        }

        AnimatedContent(
            targetState = selectedRecipient,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SendFlowTransition"
        ) { recipient ->
            if (recipient == null) {
                SendToScreen(
                    recipients = state.filteredRecipients,
                    isLoading = state.recipientsLoading,
                    error = state.recipientsError,
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onRefresh = onRefreshRecipients,
                    onBack = onBack,
                    onRecipientSelected = { selectedRecipient = it }
                )
            } else {
                SendMoneyScreen(
                    recipient = recipient,
                    amountInput = state.amountInput,
                    quote = state.quote,
                    quoteLoading = state.quoteLoading,
                    isSubmitting = state.isSubmitting,
                    submitError = state.submitError,
                    onAmountChange = onAmountChange,
                    onBack = { selectedRecipient = null },
                    onConfirm = { onSubmit(recipient) }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 1: "Send To" (Image 2 in Login Purple Theme)
// -----------------------------------------------------------------------------
@Composable
private fun SendToScreen(
    recipients: List<AddressBookRecipient>,
    isLoading: Boolean,
    error: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onRecipientSelected: (AddressBookRecipient) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top App Bar with back button and centered "Send To" serif title
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PayxPalette.TextPrimary
                )
            }

            Text(
                text = "Send To",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = PayxPalette.TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar Capsule: "Search by name or email"
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1C172B),
            border = BorderStroke(1.dp, Color(0xFF2E2544)),
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
                    tint = PayxPalette.SoftLavender,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = PayxPalette.TextPrimary
                    ),
                    cursorBrush = SolidColor(PayxPalette.VividPurple),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search by name or UPI",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    color = PayxPalette.TextSecondary
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PayxPalette.VividPurple)
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error,
                        style = TextStyle(fontSize = 14.sp, color = PayxPalette.TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Retry",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PayxPalette.SoftLavender
                        ),
                        modifier = Modifier.clickable(onClick = onRefresh)
                    )
                }
            }
            recipients.isEmpty() -> {
                Text(
                    text = "No recipients yet.",
                    style = TextStyle(fontSize = 14.sp, color = PayxPalette.TextSecondary),
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(recipients, key = { it.id }) { recipient ->
                        RecipientItemRow(
                            recipient = recipient,
                            onClick = { onRecipientSelected(recipient) }
                        )
                        HorizontalDivider(
                            color = Color(0xFF1E1A2C),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 66.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipientItemRow(
    recipient: AddressBookRecipient,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Avatar with Login Theme Purple Gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                        )
                    )
                    .border(1.dp, Color(0x44B55CF8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recipient.initials,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Column {
                Text(
                    text = recipient.name,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PayxPalette.TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = recipient.subtitle,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = PayxPalette.TextSecondary
                    )
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IndiaFlag(width = 22.dp, height = 15.dp)

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = PayxPalette.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 2: "Send Money" (Image 3 with Centered $ 2000 and Login Purple Theme)
// -----------------------------------------------------------------------------
@Composable
private fun SendMoneyScreen(
    recipient: AddressBookRecipient,
    amountInput: String,
    quote: OffRampQuoteDto?,
    quoteLoading: Boolean,
    isSubmitting: Boolean,
    submitError: String?,
    onAmountChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val inrFormatter = remember { DecimalFormat("#,##,##0.00") }
    val usdFormatter = remember { DecimalFormat("#,##0.00") }
    val receiveInr = quote?.recipientAmount ?: 0.0
    val feeInr = quote?.offRampFee ?: 0.0
    val rate = quote?.exchangeRate ?: 0.0
    val etaMin = quote?.estimatedMinutesMin ?: 5
    val etaMax = quote?.estimatedMinutesMax ?: 30
    val canSubmit = !isSubmitting && amountDouble > 0.0 && quote != null

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

            // Top Bar with back button and centered "Send Money" serif title
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PayxPalette.TextPrimary
                    )
                }

                Text(
                    text = "Send Money",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recipient Profile Avatar with Login Theme Purple Gradient
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
                    .background(
                        Brush.linearGradient(
                            listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                        )
                    )
                    .border(2.dp, Color(0x55B55CF8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recipient.initials,
                    style = TextStyle(
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
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PayxPalette.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Recipient Email
            Text(
                text = recipient.subtitle,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = PayxPalette.TextSecondary
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
                        fontFamily = FontFamily.Serif,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = amountInput,
                    onValueChange = onAmountChange,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextPrimary
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

            // FEE BREAKDOWN Card in Login Dark Theme
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF171324),
                border = BorderStroke(1.dp, Color(0xFF2B223E)),
                shadowElevation = 10.dp,
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = PayxPalette.TextSecondary
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
                                text = if (quoteLoading) "UPDATING" else "LIVE",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = PayxPalette.SoftLavender
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    BreakdownRow(
                        label = "You send",
                        value = "$${usdFormatter.format(amountDouble)} USDC"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total fees row inside highlighted purple capsule
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF221A36),
                        border = BorderStroke(1.dp, Color(0xFF382A56)),
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
                                text = "Off-ramp fee (0.50%)",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PayxPalette.TextPrimary
                                )
                            )

                            Text(
                                text = if (quote == null) "—" else "-₹${inrFormatter.format(feeInr)}",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PayxPalette.TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exchange rate row
                    BreakdownRow(
                        label = "Exchange rate",
                        value = if (quote == null) "—" else "1 USDC = ₹${inrFormatter.format(rate)}"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        color = Color(0xFF282038),
                        thickness = 0.5.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Priya receives row (Prominent)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${recipient.name.substringBefore(" ")} receives",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PayxPalette.TextPrimary
                            )
                        )

                        Text(
                            text = if (quote == null) "—" else "₹${inrFormatter.format(receiveInr)}",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = PayxPalette.TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footnote: Estimated delivery
                    Text(
                        text = "Estimated delivery: $etaMin-$etaMax minutes via ${recipient.railLabel}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = PayxPalette.TextSecondary
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!submitError.isNullOrBlank()) {
                Text(
                    text = submitError,
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFFF87171)),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(29.dp),
                        spotColor = Color(0x66A855F7),
                        ambientColor = Color(0x33A855F7)
                    )
                    .clip(RoundedCornerShape(29.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                        )
                    )
                    .clickable(enabled = canSubmit, onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "SEND MONEY",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    )
                }
            }
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
                fontSize = 14.sp,
                color = PayxPalette.TextSecondary
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PayxPalette.TextPrimary
            )
        )
    }
}
