package com.routeros.manager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.routeros.manager.ui.theme.DarkSurface
import com.routeros.manager.ui.theme.DarkSurfaceElevated
import com.routeros.manager.ui.theme.PrimaryTeal
import com.routeros.manager.ui.theme.OnDarkSurfaceVariant
import com.routeros.manager.ui.theme.SurfaceGlassBorder

val GlassCardShape = RoundedCornerShape(16.dp)

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

    Card(
        modifier = modifier
            .shadow(
                elevation = 0.dp,
                shape = GlassCardShape,
                ambientColor = Color.Transparent,
                spotColor = Color.Transparent
            )
            .clip(GlassCardShape)
            .then(clickableModifier),
        shape = GlassCardShape,
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceGlassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
fun glassTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = PrimaryTeal,
        unfocusedBorderColor = DarkSurfaceElevated,
        focusedContainerColor = DarkSurface,
        unfocusedContainerColor = DarkSurface,
        disabledContainerColor = DarkSurface,
        cursorColor = PrimaryTeal,
        focusedLabelColor = PrimaryTeal,
        unfocusedLabelColor = OnDarkSurfaceVariant,
        focusedLeadingIconColor = PrimaryTeal,
        unfocusedLeadingIconColor = OnDarkSurfaceVariant,
        focusedTrailingIconColor = PrimaryTeal,
        unfocusedTrailingIconColor = OnDarkSurfaceVariant,
        focusedPlaceholderColor = OnDarkSurfaceVariant.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = OnDarkSurfaceVariant.copy(alpha = 0.4f)
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
    shape: Shape = RoundedCornerShape(12.dp),
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
            containerColor = PrimaryTeal,
            contentColor = Color.White,
            disabledContainerColor = PrimaryTeal.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = DarkSurfaceElevated,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = DarkSurfaceElevated.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
        modifier = modifier.heightIn(min = 50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
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
    containerColor = DarkSurface,
    labelColor = OnDarkSurfaceVariant,
    iconColor = OnDarkSurfaceVariant,
    selectedContainerColor = PrimaryTeal.copy(alpha = 0.2f),
    selectedLabelColor = PrimaryTeal,
    selectedLeadingIconColor = PrimaryTeal,
    selectedTrailingIconColor = PrimaryTeal
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
        shape = RoundedCornerShape(12.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = DarkSurfaceElevated,
            selectedBorderColor = PrimaryTeal.copy(alpha = 0.5f)
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        containerColor = Color.Black,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = content
    )
}

@Composable
fun GlassScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
            .systemBarsPadding(),
        content = content
    )
}

@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    // iOS-style: solid black, no gradients
    Box(modifier = modifier.fillMaxSize().background(Color.Black))
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
        enter = fadeIn(animationSpec = tween(durationMillis = 200))
    ) {
        content()
    }
}
