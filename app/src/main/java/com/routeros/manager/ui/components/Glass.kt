package com.routeros.manager.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

val GlassCardShape = RoundedCornerShape(24.dp)

@Composable
fun glassContainerColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xCC121826)
    } else {
        Color.White.copy(alpha = 0.18f)
    }
}

@Composable
fun glassBorderBrush(): Brush {
    val light = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.White.copy(alpha = 0.22f)
    } else {
        Color.White.copy(alpha = 0.46f)
    }
    val soft = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF8FA8FF).copy(alpha = 0.10f)
    } else {
        Color.White.copy(alpha = 0.18f)
    }
    return Brush.linearGradient(
        colors = listOf(light, soft, light.copy(alpha = light.alpha * 0.72f)),
        start = Offset.Zero,
        end = Offset.Infinite
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    val highlightBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.03f),
            Color.Transparent
        )
    )
    val depthBrush = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.05f),
            Color.Black.copy(alpha = 0.12f)
        )
    )

    Card(
        modifier = modifier
            .shadow(
                elevation = 24.dp,
                shape = GlassCardShape,
                ambientColor = Color.Black.copy(alpha = 0.26f),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .clip(GlassCardShape)
            .background(glassContainerColor(), GlassCardShape)
            .border(width = 1.dp, brush = glassBorderBrush(), shape = GlassCardShape)
            .then(clickableModifier),
        shape = GlassCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.animateGlassSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(highlightBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(depthBrush)
            )
            content()
        }
    }
}

@Composable
fun GlassTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.14f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.05f),
                        Color(0xFF8FA8FF).copy(alpha = 0.10f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "ROUTEROS MANAGER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
        }
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            content = trailing
        )
    }
}

@Composable
fun GlassScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
            content = content
        )
    }
}

@Composable
fun GlassScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
            .systemBarsPadding(),
        content = content
    )
}

@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "glass-bg")
    val shiftA = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift-a"
    )
    val shiftB = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift-b"
    )

    val background = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0B1020),
            Color(0xFF171E3B),
            Color(0xFF2A2E68),
            Color(0xFF3B1F4A),
            Color(0xFF10243D)
        ),
        start = Offset(shiftA.value * 1200f, shiftB.value * 400f),
        end = Offset(1600f - shiftB.value * 900f, 2400f - shiftA.value * 800f)
    )

    val glow = Brush.radialGradient(
        colors = listOf(
            Color(0x664DD0E1),
            Color(0x445E7CFF),
            Color(0x22FF8AB3),
            Color.Transparent
        ),
        center = Offset(280f + shiftB.value * 500f, 240f + shiftA.value * 900f),
        radius = 1100f
    )

    Box(modifier = modifier.fillMaxSize().background(background))
    Box(modifier = modifier.fillMaxSize().background(glow))
}

fun Modifier.animateGlassSize(): Modifier = this.animateContentSize()
