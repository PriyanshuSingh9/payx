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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payx.app.R
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.theme.PayxPalette
import java.text.DecimalFormat
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
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
        email = "priya.sharma@payx.demo",
        avatarInitials = "PS",
        gradientColors = listOf(Color(0xFFE91E63), Color(0xFFF43F5E))
    ),
    Recipient(
        id = "2",
        name = "Rahul Verma",
        email = "rahul.verma@payx.demo",
        avatarInitials = "RV",
        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
    ),
    Recipient(
        id = "3",
        name = "Sarah Smith",
        email = "sarah.smith@payx.demo",
        avatarInitials = "SS",
        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFA855F7))
    )
)

@Composable
fun SendScreen(
    onBack: () -> Unit = {},
    onSubmitted: (transferId: String, recipientName: String, inrAmount: String, usdAmount: String) -> Unit
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
                    onConfirm = { inr, usd ->
                        onSubmitted(
                            "tx_${recipient.id}_${System.currentTimeMillis()}",
                            recipient.name,
                            inr,
                            usd
                        )
                    }
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
    onConfirm: (inrAmount: String, usdAmount: String) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var isAmountConfirmed by remember { mutableStateOf(false) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    BackHandler(enabled = isAmountConfirmed) {
        isAmountConfirmed = false
    }

    LaunchedEffect(isAmountConfirmed) {
        scrollState.scrollTo(0)
        if (!isAmountConfirmed) {
            delay(200)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {
                delay(150)
                try {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } catch (_: Exception) {
                }
            }
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    alpha = if (isProcessingPayment) 0f else 1f
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Top Bar with back button and centered "Send Money" title
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (isAmountConfirmed) {
                            isAmountConfirmed = false
                        } else {
                            onBack()
                        }
                    },
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

            Spacer(modifier = Modifier.height(24.dp))

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

                // India Flag Badge
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

            Spacer(modifier = Modifier.height(32.dp))

            // EXACTLY CENTERED DYNAMIC AMOUNT DISPLAY
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        if (isAmountConfirmed) {
                            isAmountConfirmed = false
                        }
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
            ) {
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
                        .padding(vertical = 12.dp, horizontal = 20.dp)
                        .graphicsLayer {
                            scaleX = amountScale
                            scaleY = amountScale
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isEntered = amountInput.isNotEmpty() && amountInput != "0"
                    val amountColor = if (isEntered) Color.White else Color(0xFF5A5672)

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

                    if (!isAmountConfirmed) {
                        BasicTextField(
                            value = amountInput,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }
                                if (digitsOnly.length <= 6) {
                                    amountInput = if (digitsOnly.startsWith("0") && digitsOnly.length > 1) {
                                        digitsOnly.trimStart('0')
                                    } else {
                                        digitsOnly
                                    }
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (amountDouble > 0.0) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        isAmountConfirmed = true
                                    }
                                }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .widthIn(min = 28.dp)
                                .width(IntrinsicSize.Min)
                                .focusRequester(focusRequester),
                            decorationBox = { innerTextField ->
                                if (amountInput.isEmpty()) {
                                    Text(
                                        text = "0",
                                        style = TextStyle(
                                            fontFamily = MontaguSlab,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color(0xFF5A5672)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    } else {
                        Text(
                            text = if (amountInput.isNotEmpty()) amountInput else "0",
                            style = TextStyle(
                                fontFamily = MontaguSlab,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // FEE BREAKDOWN CARD & SWIPE TO SEND SLIDER (Revealed once amount is confirmed via keyboard)
            AnimatedVisibility(
                visible = isAmountConfirmed,
                enter = slideInVertically(
                    animationSpec = tween(340, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 3 }
                ) + fadeIn(animationSpec = tween(340)),
                exit = slideOutVertically(
                    animationSpec = tween(220, easing = LinearEasing),
                    targetOffsetY = { it / 3 }
                ) + fadeOut(animationSpec = tween(220))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                            // Priya receives row (Prominent) - Neon Green
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
                                        color = Color(0xFF34D399)
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Slide to Send Interaction
                    SwipeToConfirmButton(
                        onConfirm = { isProcessingPayment = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Fast Arrow & Confirmed Message Overlay
        AnimatedVisibility(
            visible = isProcessingPayment,
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            FastArrowPaymentOverlay(
                recipient = recipient,
                inrAmount = inrFormatter.format(receiveInr),
                usdAmount = usdFormatter.format(amountDouble),
                onComplete = {
                    onConfirm(inrFormatter.format(receiveInr), usdFormatter.format(amountDouble))
                }
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

    val arrowWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrowWavePhase"
    )

    val thumbIdleNudge by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thumbIdleNudge"
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
            )
            .pointerInput(maxDrag, isConfirmed) {
                if (maxDrag > 0 && !isConfirmed) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (!isConfirmed) {
                                if (dragOffset.value > maxDrag * 0.65f) {
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

                            if (newOffset >= maxDrag * 0.85f && !isConfirmed) {
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

                        Spacer(modifier = Modifier.width(10.dp))

                        // Modern Animated Chevrons (Directional Wave)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0..2) {
                                val distance = (arrowWavePhase - i).let { d ->
                                    val mod = d % 3f
                                    if (mod < 0f) mod + 3f else mod
                                }
                                val waveProximity = (1f - (distance / 1.1f)).coerceIn(0f, 1f)
                                val chevronAlpha = (0.42f + waveProximity * 0.58f).coerceIn(0f, 1f)
                                val shiftX = waveProximity * 1.5f

                                Canvas(
                                    modifier = Modifier
                                        .size(width = 8.dp, height = 12.dp)
                                        .graphicsLayer {
                                            translationX = shiftX
                                        }
                                ) {
                                    val strokeWidth = 2.dp.toPx()
                                    val path = Path().apply {
                                        moveTo(1.dp.toPx(), 1.dp.toPx())
                                        lineTo(size.width - 1.dp.toPx(), size.height / 2f)
                                        lineTo(1.dp.toPx(), size.height - 1.dp.toPx())
                                    }

                                    // Subtle bloom halo when wave crests
                                    if (waveProximity > 0.35f) {
                                        drawPath(
                                            path = path,
                                            color = Color(0xFFC084FC).copy(alpha = waveProximity * 0.5f),
                                            style = Stroke(
                                                width = strokeWidth + 2.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }

                                    // Sharp crisp chevron
                                    val color = if (waveProximity > 0.3f) {
                                        Color.White.copy(alpha = chevronAlpha)
                                    } else {
                                        PayxPalette.VividPurple.copy(alpha = chevronAlpha)
                                    }

                                    drawPath(
                                        path = path,
                                        color = color,
                                        style = Stroke(
                                            width = strokeWidth,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
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
                ),
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
                        // Single Crisp Aerodynamic Thumb Arrow
                        Canvas(
                            modifier = Modifier
                                .size(24.dp, 18.dp)
                                .graphicsLayer {
                                    val idle = if (progress < 0.05f) thumbIdleNudge else 0f
                                    translationX = idle
                                    rotationZ = progress * 20f
                                }
                        ) {
                            val centerY = size.height / 2f
                            val strokeWidth = 2.6.dp.toPx()
                            val arrowTipX = size.width - 2.5.dp.toPx()
                            val arrowHeadWidth = 7.dp.toPx()
                            val arrowHeadHeight = 6.5.dp.toPx()
                            val shaftStartX = 2.5.dp.toPx()

                            // Main Precision Shaft
                            drawLine(
                                color = Color.White,
                                start = Offset(shaftStartX, centerY),
                                end = Offset(arrowTipX, centerY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )

                            // Clean Arrowhead
                            val headPath = Path().apply {
                                moveTo(arrowTipX - arrowHeadWidth, centerY - arrowHeadHeight)
                                lineTo(arrowTipX, centerY)
                                lineTo(arrowTipX - arrowHeadWidth, centerY + arrowHeadHeight)
                            }
                            drawPath(
                                path = headPath,
                                color = Color.White,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREMIUM SUPERSONIC ARROW OVERLAY & ANIMATED CONFIRMED TICK
// -----------------------------------------------------------------------------
@Composable
private fun FastArrowPaymentOverlay(
    recipient: Recipient,
    inrAmount: String,
    usdAmount: String,
    onComplete: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var isConfirmedPhase by remember { mutableStateOf(false) }
    val flightProgress = remember { Animatable(0f) }
    val flashAnim = remember { Animatable(0f) }
    val tickAnim = remember { Animatable(0f) }

    // Continuous engine plasma & warp tunnel oscillations
    val infiniteTransition = rememberInfiniteTransition(label = "SupersonicInfinite")
    val enginePulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "enginePulse"
    )
    val warpFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "warpFlow"
    )
    val microFlutter by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(75, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "microFlutter"
    )

    // Complete payment sequence:
    // 1. Liftoff from launch pad to center (400ms)
    // 2. Supersonic Cruise Hold in place at center (1800ms) with warp speed tunnel
    // 3. Hypersonic blast-off through top (350ms)
    // 4. Apex flash (120ms)
    // 5. Confirmed message & animated tick checkmark stroke (1600ms)
    // 6. Direct automatic dispatch into thermal receipt printer
    LaunchedEffect(Unit) {
        // Stage 1: Smooth Liftoff to center
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        flightProgress.animateTo(
            targetValue = 0.20f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        // Stage 2: Supersonic Cruise Hold in place at center (held for 1800ms)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        flightProgress.animateTo(
            targetValue = 0.80f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )

        // Stage 3: Hypersonic Blast-Off through the top
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        flightProgress.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 350,
                easing = CubicBezierEasing(0.35f, 0f, 0.1f, 1f)
            )
        )

        // Stage 4: Apex shockwave flash
        flashAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 120, easing = LinearEasing)
        )

        // Stage 5: Enter Confirmed Phase with Animated Tick
        isConfirmedPhase = true
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)

        // Animate checkmark drawing stroke-by-stroke
        tickAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )

        // Hold confirmed state with tick animation for optimal satisfaction
        delay(1600)

        // Directly print the receipt in TrackerScreen
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090D))
            .clickable(enabled = false) {}, // Scrim captures touches
        contentAlignment = Alignment.Center
    ) {
        if (!isConfirmedPhase) {
            val p = flightProgress.value

            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2f

                val startY = canvasHeight * 0.75f
                val centerY = canvasHeight * 0.48f
                val targetY = -320f

                val currentY = when {
                    p <= 0.20f -> {
                        val t = p / 0.20f
                        startY + (centerY - startY) * t
                    }
                    p <= 0.80f -> {
                        centerY + microFlutter.dp.toPx()
                    }
                    else -> {
                        val t = (p - 0.80f) / 0.20f
                        centerY + (targetY - centerY) * t
                    }
                }

                // 1. Hyperspace Warp Tunnel Streaks streaming down across the screen
                val streakDefs = listOf(
                    Triple(-145f, 0.12f, 1.2f),
                    Triple(-115f, 0.45f, 1.6f),
                    Triple(-85f, 0.78f, 1.1f),
                    Triple(-60f, 0.25f, 1.4f),
                    Triple(-38f, 0.60f, 1.8f),
                    Triple(-18f, 0.05f, 2.0f),
                    Triple(18f, 0.35f, 2.0f),
                    Triple(38f, 0.70f, 1.8f),
                    Triple(60f, 0.15f, 1.4f),
                    Triple(85f, 0.50f, 1.1f),
                    Triple(115f, 0.85f, 1.6f),
                    Triple(145f, 0.30f, 1.2f),
                    Triple(-130f, 0.90f, 1.5f),
                    Triple(-48f, 0.40f, 1.7f),
                    Triple(48f, 0.80f, 1.7f),
                    Triple(130f, 0.20f, 1.5f)
                )

                streakDefs.forEach { (xOffsetDp, phaseOffset, speedMult) ->
                    val streakY = ((warpFlow * speedMult + phaseOffset) % 1f) * canvasHeight
                    val streakLength = 80.dp.toPx() * speedMult
                    val streakX = centerX + xOffsetDp.dp.toPx()
                    val distFromCenterY = (streakY - canvasHeight / 2f).absoluteValue
                    val edgeAlpha = (1f - (distFromCenterY / (canvasHeight * 0.52f))).coerceIn(0f, 1f)

                    if (edgeAlpha > 0f) {
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF6EE7B7).copy(alpha = 0.8f * edgeAlpha),
                                    Color.Transparent
                                ),
                                startY = streakY,
                                endY = streakY + streakLength
                            ),
                            start = Offset(streakX, streakY),
                            end = Offset(streakX, streakY + streakLength),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 2. Ambient Plasma Aura around Engine
                val auraRadius = 140.dp.toPx() * enginePulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x6634D399),
                            Color(0x33A855F7),
                            Color.Transparent
                        ),
                        center = Offset(centerX, currentY + 30.dp.toPx()),
                        radius = auraRadius
                    ),
                    center = Offset(centerX, currentY + 30.dp.toPx()),
                    radius = auraRadius
                )

                // 3. Wide Diffuse Plume Trail
                val plumeLength = 460.dp.toPx()
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x8834D399),
                            Color(0x66A855F7),
                            Color.Transparent
                        ),
                        startY = currentY + 28.dp.toPx(),
                        endY = currentY + plumeLength
                    ),
                    start = Offset(centerX, currentY + 28.dp.toPx()),
                    end = Offset(centerX, currentY + plumeLength),
                    strokeWidth = 44.dp.toPx() * enginePulse,
                    cap = StrokeCap.Round
                )

                // 4. Focused Supersonic Exhaust Core Beam
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFF67E8F9),
                            Color(0xFF34D399),
                            PayxPalette.VividPurple,
                            Color.Transparent
                        ),
                        startY = currentY + 38.dp.toPx(),
                        endY = currentY + plumeLength * 0.85f
                    ),
                    start = Offset(centerX, currentY + 38.dp.toPx()),
                    end = Offset(centerX, currentY + plumeLength * 0.85f),
                    strokeWidth = 9.dp.toPx() * enginePulse,
                    cap = StrokeCap.Round
                )

                // 5. Supersonic Mach Shock Diamonds
                val diamondDistances = listOf(48.dp, 82.dp, 122.dp, 168.dp, 220.dp)
                diamondDistances.forEachIndexed { index, dist ->
                    val dy = currentY + dist.toPx()
                    val discRadius = (16.dp - (index * 2.2).dp).toPx() * enginePulse
                    val alpha = (1f - (index * 0.18f)).coerceIn(0f, 1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = alpha * 0.95f),
                                Color(0xFF34D399).copy(alpha = alpha * 0.55f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, dy),
                            radius = discRadius
                        ),
                        center = Offset(centerX, dy),
                        radius = discRadius
                    )
                }

                // 6. Supersonic Aerospace Interceptor Craft
                val arrowHalfWidth = 26.dp.toPx()
                val arrowLength = 54.dp.toPx()
                val notchDepth = 15.dp.toPx()
                val canardHalfWidth = 14.dp.toPx()
                val canardY = currentY + 19.dp.toPx()
                val canardNotchY = currentY + 24.dp.toPx()
                val fuselageNotchWidth = 8.dp.toPx()
                val engineNozzleOffset = 7.dp.toPx()

                // Atmospheric Hypersonic Bow Shock Wave ahead of needle tip
                val shockWavePath = Path().apply {
                    moveTo(centerX - 28.dp.toPx(), currentY + 12.dp.toPx())
                    quadraticTo(
                        centerX, currentY - 8.dp.toPx(),
                        centerX + 28.dp.toPx(), currentY + 12.dp.toPx()
                    )
                }
                drawPath(
                    path = shockWavePath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.75f * enginePulse),
                            Color(0xFF6EE7B7).copy(alpha = 0.35f * enginePulse),
                            Color.Transparent
                        ),
                        center = Offset(centerX, currentY),
                        radius = 32.dp.toPx()
                    ),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Left Specular Hull Facet
                val leftFacetPath = Path().apply {
                    moveTo(centerX, currentY) // Needle tip
                    lineTo(centerX - canardHalfWidth, canardY) // Left forward canard
                    lineTo(centerX - fuselageNotchWidth, canardNotchY) // Canard inset
                    lineTo(centerX - arrowHalfWidth, currentY + arrowLength) // Left swept wingtip
                    lineTo(centerX - 12.dp.toPx(), currentY + arrowLength - 5.dp.toPx()) // Inboard cut
                    lineTo(centerX - engineNozzleOffset, currentY + arrowLength - 3.dp.toPx()) // Left nozzle
                    lineTo(centerX, currentY + arrowLength - notchDepth) // Center keel notch
                    close()
                }

                drawPath(
                    path = leftFacetPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFA7F3D0),
                            Color(0xFF34D399),
                            Color(0xFF0D9488)
                        ),
                        start = Offset(centerX - arrowHalfWidth, currentY),
                        end = Offset(centerX, currentY + arrowLength)
                    )
                )

                // Right Shadow Hull Facet
                val rightFacetPath = Path().apply {
                    moveTo(centerX, currentY) // Needle tip
                    lineTo(centerX + canardHalfWidth, canardY) // Right forward canard
                    lineTo(centerX + fuselageNotchWidth, canardNotchY) // Canard inset
                    lineTo(centerX + arrowHalfWidth, currentY + arrowLength) // Right swept wingtip
                    lineTo(centerX + 12.dp.toPx(), currentY + arrowLength - 5.dp.toPx()) // Inboard cut
                    lineTo(centerX + engineNozzleOffset, currentY + arrowLength - 3.dp.toPx()) // Right nozzle
                    lineTo(centerX, currentY + arrowLength - notchDepth) // Center keel notch
                    close()
                }

                drawPath(
                    path = rightFacetPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6EE7B7),
                            Color(0xFF10B981),
                            Color(0xFF047857),
                            Color(0xFF064E3B)
                        ),
                        start = Offset(centerX, currentY),
                        end = Offset(centerX + arrowHalfWidth, currentY + arrowLength)
                    )
                )

                // Outer Laser Rim Contour (Razor Bevel)
                val fullHullOutline = Path().apply {
                    moveTo(centerX, currentY)
                    lineTo(centerX + canardHalfWidth, canardY)
                    lineTo(centerX + fuselageNotchWidth, canardNotchY)
                    lineTo(centerX + arrowHalfWidth, currentY + arrowLength)
                    lineTo(centerX + 12.dp.toPx(), currentY + arrowLength - 5.dp.toPx())
                    lineTo(centerX + engineNozzleOffset, currentY + arrowLength - 3.dp.toPx())
                    lineTo(centerX, currentY + arrowLength - notchDepth)
                    lineTo(centerX - engineNozzleOffset, currentY + arrowLength - 3.dp.toPx())
                    lineTo(centerX - 12.dp.toPx(), currentY + arrowLength - 5.dp.toPx())
                    lineTo(centerX - arrowHalfWidth, currentY + arrowLength)
                    lineTo(centerX - fuselageNotchWidth, canardNotchY)
                    lineTo(centerX - canardHalfWidth, canardY)
                    close()
                }
                drawPath(
                    path = fullHullOutline,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color(0xFF6EE7B7).copy(alpha = 0.6f),
                            Color(0x3334D399)
                        ),
                        startY = currentY,
                        endY = currentY + arrowLength
                    ),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Raised Dorsal Titanium Spine
                val dorsalSpinePath = Path().apply {
                    moveTo(centerX, currentY)
                    lineTo(centerX + 3.dp.toPx(), currentY + 16.dp.toPx())
                    lineTo(centerX + 2.5.dp.toPx(), currentY + arrowLength - notchDepth)
                    lineTo(centerX - 2.5.dp.toPx(), currentY + arrowLength - notchDepth)
                    lineTo(centerX - 3.dp.toPx(), currentY + 16.dp.toPx())
                    close()
                }
                drawPath(
                    path = dorsalSpinePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFE6FFFA),
                            Color(0xFF6EE7B7),
                            Color(0xFF10B981)
                        ),
                        startY = currentY,
                        endY = currentY + arrowLength - notchDepth
                    )
                )

                // High-Intensity Cockpit Canopy Slit (Cyber Visor)
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFF67E8F9),
                            Color(0xFF06B6D4)
                        ),
                        startY = currentY + 8.dp.toPx(),
                        endY = currentY + 24.dp.toPx()
                    ),
                    start = Offset(centerX, currentY + 8.dp.toPx()),
                    end = Offset(centerX, currentY + 24.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Central Razor Spine Line
                drawLine(
                    color = Color.White.copy(alpha = 0.95f),
                    start = Offset(centerX, currentY),
                    end = Offset(centerX, currentY + arrowLength - notchDepth),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Twin Vector Thruster Nozzles Core Flames
                listOf(-engineNozzleOffset, engineNozzleOffset).forEach { nozzleX ->
                    val nozzleCenter = Offset(centerX + nozzleX, currentY + arrowLength - 3.dp.toPx())
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFF67E8F9),
                                Color(0xFF34D399),
                                Color.Transparent
                            ),
                            center = nozzleCenter,
                            radius = 6.dp.toPx() * enginePulse
                        ),
                        center = nozzleCenter,
                        radius = 6.dp.toPx() * enginePulse
                    )
                }

                // Wingtip Beacons & Supersonic Contrail Streamers
                listOf(
                    Pair(-arrowHalfWidth, -2.dp.toPx()),
                    Pair(arrowHalfWidth, 2.dp.toPx())
                ).forEach { (tipOffset, trailDrift) ->
                    val tipX = centerX + tipOffset
                    val tipY = currentY + arrowLength

                    // Trailing Contrail Ribbon
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6EE7B7).copy(alpha = 0.85f),
                                Color(0x6634D399),
                                Color.Transparent
                            ),
                            startY = tipY,
                            endY = tipY + 48.dp.toPx() * enginePulse
                        ),
                        start = Offset(tipX, tipY),
                        end = Offset(tipX + trailDrift, tipY + 48.dp.toPx() * enginePulse),
                        strokeWidth = 1.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Beacon Glow & Core
                    drawCircle(
                        color = Color(0xFF34D399).copy(alpha = 0.5f),
                        center = Offset(tipX, tipY),
                        radius = 5.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White,
                        center = Offset(tipX, tipY),
                        radius = 2.dp.toPx()
                    )
                }
            }

            // Apex Shockwave Flash
            val flashVal = flashAnim.value
            if (flashVal > 0f && flashVal < 1f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = (1f - flashVal) * 0.5f))
                )
            }
        } else {
            // Confirmed Message & Animated Tick Display
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Status Tag Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
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
                                    .background(Color(0xFF34D399))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PAYMENT CONFIRMED • SOLANA ESCROW",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFF34D399)
                                )
                            )
                        }
                    }
                }

                // Center Confirmed Details & Animated Drawn Tick Circle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedCheckmarkCircle(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                spotColor = Color(0xFF34D399),
                                ambientColor = Color(0xFF34D399)
                            ),
                        tickProgress = tickAnim.value
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Sent to ${recipient.name}",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E0EC)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "₹$inrAmount",
                        style = TextStyle(
                            fontFamily = MontaguSlab,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            color = Color(0xFF34D399)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "($$usdAmount USD via PayX Escrow)",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF7E7B94)
                        )
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Verified Corridor Reference Capsule
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF14131C),
                        border = BorderStroke(1.dp, Color(0xFF282538))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IndiaFlag(width = 20.dp, height = 14.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "UPI Ref: 4829 1048 2910 • Instant Settlement",
                                style = TextStyle(
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE2E0EC)
                                )
                            )
                        }
                    }
                }

                // Bottom Automatic Thermal Printing Indicator
                Row(
                    modifier = Modifier.padding(bottom = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34D399))
                    )
                    Text(
                        text = "PRINTING THERMAL RECEIPT...",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF88849E)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Animated checkmark orb that draws the checkmark stroke dynamically in emerald/white.
 */
@Composable
private fun AnimatedCheckmarkCircle(
    modifier: Modifier = Modifier,
    tickProgress: Float
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w / 2f

        // 1. Glowing Emerald Background Disc
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF34D399), Color(0xFF059669)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            radius = radius
        )

        // 2. High-glow outer rim
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF6EE7B7), Color(0xFF10B981))
            ),
            radius = radius,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // 3. Expanding Pulse Halo
        if (tickProgress > 0.4f) {
            val haloFrac = (tickProgress - 0.4f) / 0.6f
            drawCircle(
                color = Color(0xFF34D399).copy(alpha = (1f - haloFrac) * 0.45f),
                radius = radius + 14.dp.toPx() * haloFrac,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 4. Animated Checkmark Stroke
        val p0 = Offset(cx - 15.dp.toPx(), cy + 1.dp.toPx())
        val p1 = Offset(cx - 4.dp.toPx(), cy + 12.dp.toPx())
        val p2 = Offset(cx + 17.dp.toPx(), cy - 10.dp.toPx())

        val strokeW = 4.5.dp.toPx()

        if (tickProgress > 0f) {
            if (tickProgress <= 0.35f) {
                val frac = tickProgress / 0.35f
                val currentP = Offset(
                    p0.x + (p1.x - p0.x) * frac,
                    p0.y + (p1.y - p0.y) * frac
                )
                drawLine(
                    color = Color.White,
                    start = p0,
                    end = currentP,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            } else {
                // First segment fully drawn
                drawLine(
                    color = Color.White,
                    start = p0,
                    end = p1,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                // Second segment animating
                val frac = ((tickProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                val currentP = Offset(
                    p1.x + (p2.x - p1.x) * frac,
                    p1.y + (p2.y - p1.y) * frac
                )
                drawLine(
                    color = Color.White,
                    start = p1,
                    end = currentP,
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
