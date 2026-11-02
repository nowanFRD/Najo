package com.najo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------------- */
/*  Palette — a calm, deep-blue identity.                                     */
/* ------------------------------------------------------------------------- */

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE6FF),
    onPrimaryContainer = Color(0xFF0A2A66),
    secondary = Color(0xFF4C6188),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E9F8),
    onSecondaryContainer = Color(0xFF16233A),
    tertiary = Color(0xFF0E7490),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFF4FF),
    onTertiaryContainer = Color(0xFF003543),
    background = Color(0xFFF5F7FC),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE6EAF4),
    onSurfaceVariant = Color(0xFF5A6478),
    outline = Color(0xFFB4BDD0),
    outlineVariant = Color(0xFFDDE3EF),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color(0xFF0B1220),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DBBFF),
    onPrimary = Color(0xFF00296B),
    primaryContainer = Color(0xFF1D4ED8),
    onPrimaryContainer = Color(0xFFDBE6FF),
    secondary = Color(0xFFB7C6E6),
    onSecondary = Color(0xFF1D2A42),
    secondaryContainer = Color(0xFF2B3A55),
    onSecondaryContainer = Color(0xFFDBE6FF),
    tertiary = Color(0xFF67D3F0),
    onTertiary = Color(0xFF003640),
    tertiaryContainer = Color(0xFF004E5C),
    onTertiaryContainer = Color(0xFFB8EAFA),
    background = Color(0xFF0B101B),
    onBackground = Color(0xFFE8EDF7),
    surface = Color(0xFF121826),
    onSurface = Color(0xFFE8EDF7),
    surfaceVariant = Color(0xFF232C3D),
    onSurfaceVariant = Color(0xFFA5B0C4),
    outline = Color(0xFF445068),
    outlineVariant = Color(0xFF2A3446),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color(0xFF000000),
)

/** Extra brand colours that Material 3 does not model directly. */
@Immutable
data class NajoExtras(
    val success: Color,
    val warning: Color,
    val heroGradient: List<Color>,
    val cardOutline: Color,
    val backgroundGradient: List<Color>,
)

private val LightExtras = NajoExtras(
    success = Color(0xFF15803D),
    warning = Color(0xFFC2410C),
    heroGradient = listOf(Color(0xFF1D4ED8), Color(0xFF2563EB), Color(0xFF60A5FA)),
    cardOutline = Color(0xFFE2E8F4),
    backgroundGradient = listOf(Color(0xFFE4EDFF), Color(0xFFF5F7FC), Color(0xFFF5F7FC)),
)

private val DarkExtras = NajoExtras(
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFDBA74),
    heroGradient = listOf(Color(0xFF122C66), Color(0xFF1D4ED8), Color(0xFF3B82F6)),
    cardOutline = Color(0xFF263144),
    backgroundGradient = listOf(Color(0xFF101A30), Color(0xFF0B101B), Color(0xFF0B101B)),
)

private val LocalNajoExtras = staticCompositionLocalOf { LightExtras }

object NajoPalette {
    val extras: NajoExtras
        @Composable @ReadOnlyComposable
        get() = LocalNajoExtras.current

    @Composable
    fun heroBrush(): Brush = Brush.linearGradient(extras.heroGradient)
}

/* ------------------------------------------------------------------------- */
/*  Typography & shapes                                                       */
/* ------------------------------------------------------------------------- */

private fun najoTypography(): Typography = Typography().let { base ->
    base.copy(
        displaySmall = base.displaySmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.8).sp,
        ),
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.5).sp,
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.3).sp,
        ),
        headlineSmall = base.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp,
            lineHeight = 24.sp,
        ),
        titleLarge = base.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
        ),
        titleMedium = base.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.5.sp,
        ),
        titleSmall = base.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        ),
        bodyLarge = base.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = base.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
        bodySmall = base.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
        labelLarge = base.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium = base.labelMedium.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 11.5.sp,
            letterSpacing = 0.2.sp,
        ),
        labelSmall = base.labelSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            letterSpacing = 0.6.sp,
        ),
    )
}

private val NajoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val NajoTypography = najoTypography()

@Composable
fun NajoTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNajoExtras provides if (darkTheme) DarkExtras else LightExtras) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = NajoTypography,
            shapes = NajoShapes,
            content = content,
        )
    }
}
