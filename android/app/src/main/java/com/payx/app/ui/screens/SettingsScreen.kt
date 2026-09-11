package com.payx.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import com.payx.app.data.SessionUser
import com.payx.app.ui.components.IndiaFlag
import com.payx.app.ui.components.UsaFlag
import com.payx.app.ui.theme.PayxPalette

@Composable
fun SettingsScreen(
    user: SessionUser?,
    onBack: () -> Unit = {},
    onSignedOut: () -> Unit
) {
    val scrollState = rememberScrollState()
    var biometricEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var copiedAddress by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.firstName ?: "PayX user"
    val email = user?.email ?: ""
    val walletAddress = user?.walletAddress.orEmpty()
    val walletPreview = shortenAddress(walletAddress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF191328),
                        Color(0xFF110E1A),
                        PayxPalette.Obsidian
                    )
                )
            )
    ) {
        // Decorative ambient curved topography background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val path1 = Path().apply {
                moveTo(0f, h * 0.15f)
                cubicTo(w * 0.35f, h * 0.10f, w * 0.7f, h * 0.22f, w, h * 0.14f)
            }
            drawPath(path1, Color(0x18B55CF8), style = Stroke(width = 1.5f))

            val path2 = Path().apply {
                moveTo(0f, h * 0.48f)
                cubicTo(w * 0.4f, h * 0.54f, w * 0.75f, h * 0.42f, w, h * 0.50f)
            }
            drawPath(path2, Color(0x12B55CF8), style = Stroke(width = 1.5f))

            val path3 = Path().apply {
                moveTo(0f, h * 0.82f)
                cubicTo(w * 0.3f, h * 0.78f, w * 0.65f, h * 0.88f, w, h * 0.84f)
            }
            drawPath(path3, Color(0x10A855F7), style = Stroke(width = 1.5f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    text = "Settings",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = PayxPalette.TextPrimary
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // User Profile Hero Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF171324),
                    border = BorderStroke(1.dp, Color(0xFF2E2445)),
                    shadowElevation = 10.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Purple Gradient Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = CircleShape,
                                    spotColor = Color(0x66A855F7),
                                    ambientColor = Color(0x33A855F7)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PayxPalette.CardGradientStart, PayxPalette.CardGradientEnd)
                                    )
                                )
                                .border(1.5.dp, Color(0x55B55CF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.initial ?: "P",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PayxPalette.TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = email,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = PayxPalette.TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // KYC Tier 2 Pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF221A36),
                                border = BorderStroke(1.dp, Color(0xFF382A56))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(PayxPalette.VividPurple),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(7.dp)
                                        )
                                    }
                                    Text(
                                        text = "Verified KYC • Tier 2",
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PayxPalette.SoftLavender
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 1: Security & Custody
                SectionHeader(title = "SECURITY & CUSTODY")

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF171324),
                    border = BorderStroke(1.dp, Color(0xFF29203D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        // Solana Escrow Key Address
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (walletAddress.isNotBlank()) {
                                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                                        clipboard.setPrimaryClip(ClipData.newPlainText("PayX wallet", walletAddress))
                                        copiedAddress = true
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SettingsIconBox(icon = Icons.Default.Security)
                                Column {
                                    Text(
                                        text = "Escrow Custody Key",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PayxPalette.TextPrimary
                                        )
                                    )
                                    Text(
                                        text = if (copiedAddress) {
                                            "Copied to clipboard!"
                                        } else if (walletPreview.isNotBlank()) {
                                            "$walletPreview • Keystore Enclave"
                                        } else {
                                            "No wallet yet"
                                        },
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            color = if (copiedAddress) PayxPalette.VividPurple else PayxPalette.TextTertiary
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = PayxPalette.SoftLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(
                            color = Color(0xFF241C35),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Biometric Authentication Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SettingsIconBox(icon = Icons.Default.Lock)
                                Column {
                                    Text(
                                        text = "Biometric Authentication",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PayxPalette.TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Require fingerprint to confirm escrow",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            color = PayxPalette.TextTertiary
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { biometricEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PayxPalette.VividPurple,
                                    uncheckedThumbColor = PayxPalette.TextTertiary,
                                    uncheckedTrackColor = Color(0xFF221A34)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Corridor & Remittance Preferences
                SectionHeader(title = "PREFERENCES & CORRIDORS")

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF171324),
                    border = BorderStroke(1.dp, Color(0xFF29203D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        // Default Remittance Corridor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Default Transfer Corridor",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PayxPalette.TextPrimary
                                    )
                                )
                                Text(
                                    text = "Cross-border instant settlement",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = PayxPalette.TextTertiary
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                UsaFlag(width = 20.dp, height = 13.dp)
                                Text(
                                    text = "USD",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PayxPalette.TextPrimary
                                    )
                                )
                                Text(
                                    text = "→",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = PayxPalette.SoftLavender
                                    )
                                )
                                IndiaFlag(width = 20.dp, height = 13.dp)
                                Text(
                                    text = "INR",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PayxPalette.TextPrimary
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFF241C35),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Notification Receipts Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SettingsIconBox(icon = Icons.Default.Notifications)
                                Column {
                                    Text(
                                        text = "Transaction Push Receipts",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PayxPalette.TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Instant notification upon escrow release",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            color = PayxPalette.TextTertiary
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PayxPalette.VividPurple,
                                    uncheckedThumbColor = PayxPalette.TextTertiary,
                                    uncheckedTrackColor = Color(0xFF221A34)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: About & Protocol
                SectionHeader(title = "ABOUT PAYX")

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF171324),
                    border = BorderStroke(1.dp, Color(0xFF29203D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsNavRow(
                            title = "Terms of Service & Privacy Policy",
                            subtitle = "Cross-border regulatory disclosures"
                        )

                        HorizontalDivider(
                            color = Color(0xFF241C35),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        SettingsNavRow(
                            title = "Solana Escrow Program ID",
                            subtitle = "payx_escrow • Edition 2021"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Sign Out Button (Styled Danger Pill)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(Color(0xFF1D1426))
                        .border(1.dp, Color(0xFF3F2148), RoundedCornerShape(27.dp))
                        .clickable(onClick = onSignedOut),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sign Out",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF87171)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PayX Protocol v1.0.4 • Anchor Edition",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = PayxPalette.TextTertiary
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

private fun shortenAddress(address: String): String {
    if (address.length <= 12) return address
    return address.take(5) + "..." + address.takeLast(4)
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = PayxPalette.TextSecondary
        ),
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF221A36))
            .border(1.dp, Color(0xFF352752), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PayxPalette.SoftLavender,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsNavRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PayxPalette.TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = PayxPalette.TextTertiary
                )
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = PayxPalette.TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}
