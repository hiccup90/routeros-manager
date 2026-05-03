package com.routeros.manager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.routeros.manager.ui.theme.AccentMagenta
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.SurfaceGlassBorder
import com.routeros.manager.ui.theme.SurfaceGlassMedium
import com.routeros.manager.ui.theme.SurfaceGlassOverlay

val GlassCardShape = RoundedCornerShape(24.dp)

@Composable
fun glassContainerColor(): Color {
    return if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xD9111A2C)
    } else {
        Color.White.copy(alpha = 0.18f)
    }
}

@Composable
fun glassBorderBrush(): Brush {
    val light = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        SurfaceGlassBorder
    } else {
        Color.White.copy(alpha = 0.46f)
    }
    val soft = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        PrimaryTeal.copy(alpha = 0.14f)
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
            SurfaceGlassMedium.copy(alpha = 0.28f),
            Color.White.copy(alpha = 0.008f),
            Color.Transparent
        )
    )
    val depthBrush = Brush.verticalGradient(
        colors = listOf(
            SurfaceGlassOverlay,
            Color.Black.copy(alpha = 0.06f),
            Color.Black.copy(alpha = 0.16f)
        )
    )
    val accentGlow = Brush.linearGradient(
        colors = listOf(
            PrimaryTeal.copy(alpha = 0.08f),
            AccentMagenta.copy(alpha = 0.04f),
            Color.Transparent
        )
    )

    Card(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = GlassCardShape,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = PrimaryTeal.copy(alpha = 0.10f)
            )
            .clip(GlassCardShape)
            .background(glassContainerColor(), GlassCardShape)
            .border(width = 1.dp, brush = glassBorderBrush(), shape = GlassCardShape)
            .then(clickableModifier),
        shape = GlassCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.background(depthBrush)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(highlightBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(accentGlow)
            )
            content()
        }
    }
}

@Composable
fun glassTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = Color(0xFF87D6FF),
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.92f),
        focusedContainerColor = Color(0x331A2740),
        unfocusedContainerColor = Color(0x26131C31),
        disabledContainerColor = Color(0x1A131C31),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = RoundedCornerShape(18.dp),
    colors: TextFieldColors = glassTextFieldColors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        readOnly = readOnly,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = shape,
        colors = colors
    )
}

@Composable
fun glassButtonColors(primary: Boolean): ButtonColors {
    return if (primary) {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFF66D9FF),
            contentColor = Color(0xFF04111E),
            disabledContainerColor = Color(0xFF66D9FF).copy(alpha = 0.38f),
            disabledContentColor = Color(0xFF04111E).copy(alpha = 0.48f)
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color(0x2AFFFFFF),
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = Color(0x16FFFFFF),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.48f)
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentDescription: String? = null,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = glassButtonColors(primary),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (content != null) {
            content()
        } else {
            if (leadingIcon != null) {
                Icon(imageVector = leadingIcon, contentDescription = contentDescription)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun glassFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color(0x20141E33),
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = Color(0x2E6AD7FF),
    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
    selectedTrailingIconColor = MaterialTheme.colorScheme.primary
)

@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f),
            selectedBorderColor = Color(0x667ED7FF)
        ),
        colors = glassFilterChipColors()
    )
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
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .heightIn(min = 36.dp)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
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
    val background = Brush.linearGradient(
        colors = listOf(
            Color(0xFF09111F),
            Color(0xFF151F39),
            Color(0xFF222B58),
            Color(0xFF3A2353),
            Color(0xFF0B213B)
        ),
        start = Offset(120f, 0f),
        end = Offset(1280f, 2200f)
    )

    val glow = Brush.radialGradient(
        colors = listOf(
            PrimaryTeal.copy(alpha = 0.34f),
            Color(0x305E7CFF),
            AccentMagenta.copy(alpha = 0.12f),
            Color.Transparent
        ),
        center = Offset(320f, 360f),
        radius = 960f
    )

    Box(modifier = modifier.fillMaxSize().background(background))
    Box(modifier = modifier.fillMaxSize().background(glow))
}

fun Modifier.animateGlassSize(): Modifier = this

@Composable
fun GlassPageTransition(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 110))
    ) {
        content()
    }
}
