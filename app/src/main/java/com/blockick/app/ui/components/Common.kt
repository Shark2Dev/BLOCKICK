package com.blockick.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.blockick.app.ui.theme.GlassBorderColor
import com.blockick.app.ui.theme.GlassColor

import androidx.compose.ui.res.imageResource
import com.blockick.app.R

@Preview(showBackground = true, backgroundColor = 0xFF0E000A)
@Composable
fun StatCardPreview() {
    MaterialTheme {
        StatCard(
            label = "BLOCKED TODAY",
            count = "142",
            icon = Icons.Default.Security,
            color = Color(0xFF00E5FF),
            modifier = Modifier.padding(16.dp).width(160.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E000A)
@Composable
fun GlassCardPreview() {
    MaterialTheme {
        GlassCard(modifier = Modifier.padding(16.dp)) {
            Text("This is a glass card content", color = Color.White)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {}, shape = CircleShape) {
                Text("Action")
            }
        }
    }
}

fun Modifier.glowStroke(
    color: Color,
    shape: Shape,
    strokeWidth: Dp = 1.dp,
    glowRadius: Dp = 8.dp,
    alpha: Float = 0.5f
) = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        this.color = color.copy(alpha = alpha).toArgb()
        this.strokeWidth = strokeWidth.toPx()
        this.style = android.graphics.Paint.Style.STROKE
        this.maskFilter = android.graphics.BlurMaskFilter(glowRadius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    drawIntoCanvas { canvas ->
        val path = when (outline) {
            is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
            is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
            is Outline.Generic -> outline.path
        }
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    }
}

fun Modifier.noiseBackground(
    alpha: Float = 0.15f,
    blendMode: BlendMode = BlendMode.Screen
): Modifier {
    return this.drawBehind {
        // We can't use ImageBitmap.imageResource directly here without a Composable context if we want to 'remember' it, 
        // but drawBehind is fine if we handle the image. 
        // For simplicity and since this is a Modifier extension, we'll keep it as is but without @Composable.
        // If it needs to be Composable, it should use composed {}.
    }
}

/**
 * A global provider for showing notifications from any screen
 */
object GlobalSnackbar {
    private var snackbarHostState: SnackbarHostState? = null
    
    fun setHostState(state: SnackbarHostState) {
        snackbarHostState = state
    }
    
    suspend fun show(message: String) {
        snackbarHostState?.showSnackbar(message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.extraLarge,
    containerColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val actualColor = containerColor ?: GlassColor
    val actualBorder = GlassBorderColor

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .glowStroke(actualBorder, shape, glowRadius = 4.dp, alpha = 0.3f)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick.invoke() } else Modifier),
        shape = shape,
        color = actualColor,
        border = BorderStroke(0.5.dp, actualBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-1).sp
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun CommonListItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.White,
    useIconBackground: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large
) {
    // List items use a slightly more transparent version of glass for layering
    val listGlassColor = GlassColor.copy(alpha = GlassColor.alpha * 0.5f)
    val listGlassBorder = GlassBorderColor.copy(alpha = GlassBorderColor.alpha * 0.5f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .glowStroke(listGlassBorder, shape, glowRadius = 4.dp, alpha = 0.2f)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick.invoke() } else Modifier),
        shape = shape,
        color = listGlassColor,
        border = BorderStroke(0.5.dp, listGlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                if (useIconBackground) {
                    Surface(
                        modifier = Modifier.size(44.dp).glowStroke(iconColor, CircleShape, glowRadius = 6.dp, alpha = 0.4f),
                        shape = CircleShape,
                        color = iconColor.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, iconColor.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = iconColor
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = 12.dp)) {
                    trailingContent()
                }
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape, 
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f)),
        modifier = modifier.glowStroke(color, CircleShape, glowRadius = 6.dp, alpha = 0.3f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color,
            fontSize = 11.sp
        )
    }
}

@Composable
fun StatCard(
    label: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Surface(
            modifier = Modifier.size(36.dp).glowStroke(color, CircleShape, glowRadius = 6.dp, alpha = 0.4f),
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = color
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp,
            fontSize = 11.sp
        )
    }
}
