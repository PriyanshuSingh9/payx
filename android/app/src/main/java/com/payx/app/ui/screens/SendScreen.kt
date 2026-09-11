package com.payx.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.Random
private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

private val MontaguSlab = FontFamily(Font(R.font.montagu_slab))

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val isCircle: Boolean,
    val rotation: Float,
    val rotationSpeed: Float
)

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
    var isProcessingPayment by remember { mutableStateOf(false) }
    
    // Animate scale on input change
    var scaleTrigger by remember { mutableStateOf(false) }
    val amountScale by animateFloatAsState(
        targetValue = if (scaleTrigger) 1.12f else 1f,
        animationSpec = tween(durationMillis = 100),
        finishedListener = { scaleTrigger = false },
        label = "amountScale"
    )

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val feeRate = 0.0325
    val feeAmount = amountDouble * feeRate
    val exchangeRate = 92.90
    val netSend = (amountDouble - feeAmount).coerceAtLeast(0.0)
    val receiveInr = netSend * exchangeRate

    val inrFormatter = remember { DecimalFormat("#,##,##0.00") }
    val usdFormatter = remember { DecimalFormat("#,##0.00") }
    
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Scrollable Upper Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
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

            // Recipient Profile Avatar with India Flag Badge
            Box {
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

                // India Flag Badge (Idea 5)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF09090D),
                    border = BorderStroke(2.dp, Color(0xFF09090D)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                ) {
                    IndiaFlag(width = 24.dp, height = 16.dp)
                }
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

            // EXACTLY CENTERED DYNAMIC AMOUNT DISPLAY (Idea 6)
            // Soft ambient glow behind the amount text (Idea 4)
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x33A855F7),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .graphicsLayer {
                            scaleX = amountScale
                            scaleY = amountScale
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val amountColor = if (amountDouble > 0) Color.White else Color(0xFF4A4660)
                    
                    Text(
                        text = "$",
                        style = TextStyle(
                            fontFamily = MontaguSlab,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Normal,
                            color = amountColor
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    BasicTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 6) {
                                amountInput = digitsOnly
                                scaleTrigger = true
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = MontaguSlab,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Normal,
                            color = amountColor
                        ),
                        cursorBrush = SolidColor(PayxPalette.VividPurple),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .widthIn(min = 28.dp)
                            .width(IntrinsicSize.Min),
                        decorationBox = { innerTextField ->
                            if (amountInput.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = TextStyle(
                                        fontFamily = MontaguSlab,
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF4A4660)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
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
                        value = "$${usdFormatter.format(amountDouble)}",
                        valueColor = Color.White
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
                        value = "1 USD = ₹92.90",
                        valueColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Priya receives row (Prominent) - Neon Green (Idea 3)
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
                                color = Color(0xFF34D399) // Vivid Neon Green
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
            
            Spacer(modifier = Modifier.height(20.dp))
        }

            // Slide to Send Interaction
            SwipeToConfirmButton(
                onConfirm = { isProcessingPayment = true },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            )
        }

        // Paytm Style Payment Sending Animation Overlay
        AnimatedVisibility(
            visible = isProcessingPayment,
            enter = fadeIn(animationSpec = tween(260)) + scaleIn(initialScale = 0.94f, animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            PaytmPaymentSendingOverlay(
                recipient = recipient,
                inrAmount = inrFormatter.format(receiveInr),
                usdAmount = usdFormatter.format(amountDouble),
                onComplete = onConfirm
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    valueColor: Color
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
                color = valueColor
            )
        )
    }
}



@Composable
private fun SwipeToConfirmButton(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var width by remember { mutableStateOf(0) }
    val dragOffset = remember { Animatable(0f) }
    var isConfirmed by remember { mutableStateOf(false) }
    var hasPassedMidpoint by remember { mutableStateOf(false) }

    val thumbSize = 56.dp
    val thumbSizePx = with(LocalDensity.current) { thumbSize.toPx() }
    val maxDrag = (width - thumbSizePx).coerceAtLeast(0f)

    val progress = if (maxDrag > 0) (dragOffset.value / maxDrag).coerceIn(0f, 1f) else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .onSizeChanged { width = it.width }
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF14131C))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF282538),
                        if (progress > 0.4f) PayxPalette.VividPurple.copy(alpha = progress.coerceIn(0.2f, 1f)) else Color(0xFF282538)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Dynamic Glowing Progress Trail behind the thumb
        if (progress > 0.01f) {
            val trailWidth = with(LocalDensity.current) {
                (dragOffset.value + thumbSizePx + 4.dp.toPx()).toDp()
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(trailWidth)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                PayxPalette.CardGradientStart.copy(alpha = 0.45f + progress * 0.35f),
                                if (isConfirmed) Color(0xFF34D399).copy(alpha = 0.85f) else PayxPalette.VividPurple.copy(alpha = 0.65f + progress * 0.35f)
                            )
                        )
                    )
            )
        }

        // Center Animated Text / State Indicator
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isConfirmed,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TrackTextTransition"
            ) { confirmed ->
                if (confirmed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00BAF2))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SENDING...",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                color = Color.White
                            )
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = (1f - progress * 1.5f).coerceIn(0f, 1f)
                            translationX = progress * 35f
                        }
                    ) {
                        val shimmerColors = listOf(
                            Color(0xFF6B6880),
                            Color(0xFFE2E0EC),
                            Color(0xFF6B6880)
                        )
                        val shimmerBrush = Brush.linearGradient(
                            colors = shimmerColors,
                            start = Offset(shimmerTranslate - 250f, 0f),
                            end = Offset(shimmerTranslate, 0f)
                        )

                        Text(
                            text = "SWIPE TO SEND",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp,
                                brush = shimmerBrush
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "›››",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PayxPalette.SoftLavender.copy(alpha = 0.7f),
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }
            }
        }

        // Draggable Elevated Thumb
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                .padding(4.dp)
                .size(thumbSize)
                .shadow(
                    elevation = if (isConfirmed) 16.dp else 10.dp,
                    shape = CircleShape,
                    spotColor = if (isConfirmed) Color(0xFF34D399) else PayxPalette.VividPurple,
                    ambientColor = if (isConfirmed) Color(0xFF34D399) else PayxPalette.VividPurple
                )
                .pointerInput(maxDrag, isConfirmed) {
                    if (maxDrag > 0 && !isConfirmed) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (!isConfirmed) {
                                    if (dragOffset.value > maxDrag * 0.72f) {
                                        coroutineScope.launch {
                                            isConfirmed = true
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            dragOffset.animateTo(
                                                targetValue = maxDrag,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                            delay(120)
                                            onConfirm()
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            hasPassedMidpoint = false
                                            dragOffset.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            onDragCancel = {
                                if (!isConfirmed) {
                                    coroutineScope.launch {
                                        hasPassedMidpoint = false
                                        dragOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = (dragOffset.value + dragAmount).coerceIn(0f, maxDrag)
                                if (!hasPassedMidpoint && newOffset > maxDrag * 0.5f) {
                                    hasPassedMidpoint = true
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else if (hasPassedMidpoint && newOffset < maxDrag * 0.4f) {
                                    hasPassedMidpoint = false
                                }
                                dragOffset.snapTo(newOffset)

                                if (newOffset >= maxDrag * 0.94f && !isConfirmed) {
                                    isConfirmed = true
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dragOffset.animateTo(
                                        targetValue = maxDrag,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                    delay(120)
                                    onConfirm()
                                }
                            }
                        }
                    }
                },
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            if (isConfirmed) {
                                listOf(Color(0xFF34D399), Color(0xFF059669))
                            } else {
                                listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                            }
                        )
                    )
                    .border(
                        1.5.dp,
                        if (isConfirmed) Color(0x6634D399) else Color(0x44FFFFFF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isConfirmed,
                    transitionSpec = {
                        (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                    },
                    label = "ThumbIconTransition"
                ) { confirmed ->
                    if (confirmed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Swipe to Send",
                            tint = Color.White,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    rotationZ = progress * 45f
                                }
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PAYTM STYLE PAYMENT SENDING & SUCCESS OVERLAY
// -----------------------------------------------------------------------------
@Composable
private fun PaytmPaymentSendingOverlay(
    recipient: Recipient,
    inrAmount: String,
    usdAmount: String,
    onComplete: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var phase by remember { mutableStateOf(0) } // 0: UPI connecting, 1: Solana escrow lock, 2: Payment Successful!

    // Confetti particles initialized once with stable random seed
    val particles = remember {
        val colors = listOf(
            Color(0xFF34D399), // Emerald Green
            Color(0xFF00BAF2), // Paytm Cyan
            Color(0xFFA855F7), // Vivid Purple
            Color(0xFFFBBF24), // Gold
            Color(0xFFFFFFFF)  // Crisp White
        )
        val random = Random(1337)
        List(48) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 300f + random.nextFloat() * 650f
            ConfettiParticle(
                x = 0f,
                y = 0f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                color = colors[random.nextInt(colors.size)],
                size = 5f + random.nextFloat() * 7f,
                isCircle = random.nextBoolean(),
                rotation = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 900f
            )
        }
    }

    // Sequence of progression
    LaunchedEffect(Unit) {
        // Stage 0: Initiating UPI connection
        delay(900)
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        phase = 1

        // Stage 1: Solana Blockchain Escrow
        delay(1100)
        phase = 2
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)

        // Stage 2: Success Celebration & Auto-advance
        delay(2900)
        onComplete()
    }

    // Radar Soundwave continuous looping animation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarWaves")
    val radarPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPhase"
    )
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    // Success Shockwave expansion
    val shockwaveProgress by animateFloatAsState(
        targetValue = if (phase == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "shockwave"
    )

    // Confetti flight progression
    val confettiProgress by animateFloatAsState(
        targetValue = if (phase == 2) 1f else 0f,
        animationSpec = tween(durationMillis = 1400, easing = LinearOutSlowInEasing),
        label = "confetti"
    )

    // Center Hero Orb scale bounce on success
    val orbScale by animateFloatAsState(
        targetValue = if (phase == 2) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "orbScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF209090D))
            .clickable(enabled = false) {}, // Scrim captures touches
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial glow behind the radar
        Box(
            modifier = Modifier
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (phase == 2) Color(0x3334D399) else Color(0x3300BAF2),
                            Color.Transparent
                        )
                    )
                )
        )

        // Canvas for Radar Waves, Rotating Sweep, Shockwave, and Confetti Explosion
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = center

            // 1. Paytm Concentric Soundwave Radar Rings
            val waveOffsets = listOf(0f, 0.25f, 0.5f, 0.75f)
            waveOffsets.forEach { offset ->
                val wave = (radarPhase + offset) % 1f
                val radius = 54.dp.toPx() + (165.dp.toPx() - 54.dp.toPx()) * sqrt(wave)
                val alpha = (1f - wave).coerceIn(0f, 1f) * (if (phase == 2) 0.8f else 0.5f)
                val waveColor = if (phase == 2) Color(0xFF34D399) else Color(0xFF00BAF2)
                drawCircle(
                    color = waveColor.copy(alpha = alpha),
                    radius = radius,
                    center = canvasCenter,
                    style = Stroke(width = (3.dp - 1.5.dp * wave).toPx())
                )
            }

            // 2. Rotating Radar Sweep Beam (during processing)
            if (phase < 2) {
                rotate(radarSweepAngle, pivot = canvasCenter) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.75f to Color.Transparent,
                            1f to Color(0x6600BAF2)
                        ),
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = true,
                        topLeft = Offset(canvasCenter.x - 110.dp.toPx(), canvasCenter.y - 110.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(220.dp.toPx(), 220.dp.toPx())
                    )
                }
            }

            // 3. Shockwave Ring on Success
            if (phase == 2 && shockwaveProgress > 0f) {
                val shockwaveRadius = 60.dp.toPx() + 240.dp.toPx() * shockwaveProgress
                val shockwaveAlpha = (1f - shockwaveProgress).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFF34D399).copy(alpha = shockwaveAlpha * 0.9f),
                    radius = shockwaveRadius,
                    center = canvasCenter,
                    style = Stroke(width = (6.dp * (1f - shockwaveProgress)).toPx())
                )
            }

            // 4. Celebratory Confetti Particle Explosion
            if (phase == 2 && confettiProgress > 0f) {
                val pProg = confettiProgress
                particles.forEach { p ->
                    val px = p.vx * pProg
                    val py = p.vy * pProg + (240f * pProg * pProg) // downward gravity curve
                    val pAlpha = (1f - pProg).coerceIn(0f, 1f)
                    val particleCenter = Offset(canvasCenter.x + px, canvasCenter.y + py)

                    rotate(p.rotation + p.rotationSpeed * pProg, pivot = particleCenter) {
                        if (p.isCircle) {
                            drawCircle(
                                color = p.color.copy(alpha = pAlpha),
                                radius = p.size,
                                center = particleCenter
                            )
                        } else {
                            drawRect(
                                color = p.color.copy(alpha = pAlpha),
                                topLeft = Offset(particleCenter.x - p.size, particleCenter.y - p.size * 0.6f),
                                size = androidx.compose.ui.geometry.Size(p.size * 2f, p.size * 1.2f)
                            )
                        }
                    }
                }
            }
        }

        // Foreground Content Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Encrypted Rail Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF14131C),
                    border = BorderStroke(1.dp, Color(0xFF282538))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (phase == 2) Color(0xFF34D399) else Color(0xFF00BAF2))
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = if (phase == 2) "PAYMENT COMPLETED" else "INSTANT UPI SETTLEMENT",
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = if (phase == 2) Color(0xFF34D399) else Color(0xFFE2E0EC)
                            )
                        )
                    }
                }
            }

            // Center Hero Content (Badge + Amounts + Status)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Center Glowing Orb (Recipient Avatar -> Spring Checkmark)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer {
                            scaleX = orbScale
                            scaleY = orbScale
                        }
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            spotColor = if (phase == 2) Color(0xFF34D399) else Color(0xFF00BAF2),
                            ambientColor = if (phase == 2) Color(0xFF34D399) else Color(0xFF00BAF2)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (phase == 2) {
                                    listOf(Color(0xFF34D399), Color(0xFF059669))
                                } else {
                                    listOf(Color(0xFF1B1926), Color(0xFF0E0D14))
                                }
                            )
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                if (phase == 2) {
                                    listOf(Color(0xFF6EE7B7), Color(0xFF10B981))
                                } else {
                                    listOf(Color(0xFF00BAF2), PayxPalette.VividPurple)
                                }
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = phase == 2,
                        transitionSpec = {
                            (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                                    (scaleOut() + fadeOut())
                        },
                        label = "OrbIconTransition"
                    ) { isSuccess ->
                        if (isSuccess) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Payment Successful",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = recipient.avatarInitials,
                                    style = TextStyle(
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                // Corridor flag badge
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF09090D),
                                    border = BorderStroke(1.5.dp, Color(0xFF09090D)),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp)
                                ) {
                                    IndiaFlag(width = 20.dp, height = 14.dp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recipient Name
                Text(
                    text = if (phase == 2) "Sent to ${recipient.name}" else "Sending to ${recipient.name}",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Huge Typographic Rupee Amount in MontaguSlab
                Text(
                    text = "₹$inrAmount",
                    style = TextStyle(
                        fontFamily = MontaguSlab,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = if (phase == 2) Color(0xFF34D399) else Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // USD Conversion reference
                Text(
                    text = "($$usdAmount USD via PayX Escrow)",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF7E7B94)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Dynamic Status Pill
                AnimatedContent(
                    targetState = phase,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "StatusStepTransition"
                ) { currentPhase ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentPhase) {
                            2 -> Color(0xFF12281E)
                            1 -> Color(0xFF221A36)
                            else -> Color(0xFF0D2332)
                        },
                        border = BorderStroke(
                            1.dp,
                            when (currentPhase) {
                                2 -> Color(0xFF059669)
                                1 -> Color(0xFF382A56)
                                else -> Color(0xFF00779E)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (currentPhase) {
                                    0 -> "Connecting to Indian Banking Rails..."
                                    1 -> "Securing Escrow on Solana Blockchain..."
                                    else -> "UPI Ref: 4829 1048 2910 • Instant Transfer"
                                },
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when (currentPhase) {
                                        2 -> Color(0xFF34D399)
                                        1 -> PayxPalette.SoftLavender
                                        else -> Color(0xFF38BDF8)
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Action (View in Tracker button on success)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (phase == 2) {
                    Surface(
                        onClick = onComplete,
                        shape = RoundedCornerShape(28.dp),
                        color = PayxPalette.VividPurple,
                        shadowElevation = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "VIEW IN TRACKER",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Do not close the app while payment is in progress",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF5E5B70)
                        )
                    )
                }
            }
        }
    }
}
