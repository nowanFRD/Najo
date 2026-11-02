package com.najo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A small hand-drawn icon set. Keeping these as Canvas drawings avoids pulling
 * in an icon font and keeps the visuals consistent at every size.
 */
private fun DrawScope.iconStroke(width: Dp): Stroke = Stroke(
    width = size.width * (width.value / 24f),
    cap = StrokeCap.Round,
    join = StrokeJoin.Round,
)

@Composable
fun CheckIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 2.2.dp) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.53f)
            lineTo(size.width * 0.41f, size.height * 0.76f)
            lineTo(size.width * 0.84f, size.height * 0.26f)
        }
        drawPath(path, color, style = iconStroke(strokeWidth))
    }
}

@Composable
fun PlusIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 2.2.dp) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = iconStroke(strokeWidth)
        drawLine(color, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.8f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun CloseIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 2.dp) {
    Canvas(modifier) {
        val stroke = iconStroke(strokeWidth)
        drawLine(color, Offset(size.width * 0.25f, size.height * 0.25f), Offset(size.width * 0.75f, size.height * 0.75f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.75f, size.height * 0.25f), Offset(size.width * 0.25f, size.height * 0.75f), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun TrashIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 1.8.dp) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = iconStroke(strokeWidth)
        drawLine(color, Offset(w * 0.2f, h * 0.3f), Offset(w * 0.8f, h * 0.3f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.38f, h * 0.3f), Offset(w * 0.4f, h * 0.18f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.4f, h * 0.18f), Offset(w * 0.6f, h * 0.18f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.6f, h * 0.18f), Offset(w * 0.62f, h * 0.3f), stroke.width, StrokeCap.Round)
        val body = Path().apply {
            moveTo(w * 0.28f, h * 0.3f)
            lineTo(w * 0.32f, h * 0.84f)
            quadraticBezierTo(w * 0.34f, h * 0.9f, w * 0.42f, h * 0.9f)
            lineTo(w * 0.58f, h * 0.9f)
            quadraticBezierTo(w * 0.66f, h * 0.9f, w * 0.68f, h * 0.84f)
            lineTo(w * 0.72f, h * 0.3f)
        }
        drawPath(body, color, style = stroke)
        drawLine(color, Offset(w * 0.44f, h * 0.44f), Offset(w * 0.46f, h * 0.76f), stroke.width * 0.8f, StrokeCap.Round)
        drawLine(color, Offset(w * 0.56f, h * 0.44f), Offset(w * 0.54f, h * 0.76f), stroke.width * 0.8f, StrokeCap.Round)
    }
}

@Composable
fun PencilIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 1.8.dp) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = iconStroke(strokeWidth)
        val body = Path().apply {
            moveTo(w * 0.24f, h * 0.76f)
            lineTo(w * 0.62f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.34f)
            lineTo(w * 0.42f, h * 0.88f)
            close()
        }
        drawPath(body, color, style = stroke)
        drawLine(color, Offset(w * 0.24f, h * 0.76f), Offset(w * 0.2f, h * 0.94f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.2f, h * 0.94f), Offset(w * 0.42f, h * 0.88f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.58f, h * 0.26f), Offset(w * 0.75f, h * 0.4f), stroke.width * 0.8f, StrokeCap.Round)
    }
}

@Composable
fun SunIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 1.9.dp) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = iconStroke(strokeWidth)
        val center = Offset(w / 2f, h / 2f)
        drawCircle(color, radius = w * 0.22f, center = center, style = stroke)
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            val inner = w * 0.34f
            val outer = w * 0.44f
            drawLine(
                color = color,
                start = Offset(center.x + cos(angle).toFloat() * inner, center.y + sin(angle).toFloat() * inner),
                end = Offset(center.x + cos(angle).toFloat() * outer, center.y + sin(angle).toFloat() * outer),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun MoonIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val crescent = Path().apply {
            moveTo(w * 0.72f, h * 0.1f)
            cubicTo(w * 0.3f, h * 0.16f, w * 0.14f, h * 0.58f, w * 0.34f, h * 0.82f)
            cubicTo(w * 0.54f, h * 0.98f, w * 0.82f, h * 0.9f, w * 0.9f, h * 0.72f)
            cubicTo(w * 0.58f, h * 0.8f, w * 0.4f, h * 0.46f, w * 0.72f, h * 0.1f)
            close()
        }
        drawPath(crescent, color)
    }
}

@Composable
fun ClockIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 1.7.dp) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = iconStroke(strokeWidth)
        drawCircle(color, radius = w * 0.38f, center = Offset(w / 2f, h / 2f), style = stroke)
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.28f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.66f, h * 0.58f), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun ChevronDownIcon(modifier: Modifier = Modifier, color: Color, strokeWidth: Dp = 1.8.dp) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = iconStroke(strokeWidth)
        drawLine(color, Offset(w * 0.28f, h * 0.42f), Offset(w * 0.5f, h * 0.64f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.72f, h * 0.42f), Offset(w * 0.5f, h * 0.64f), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun SparkIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val star = Path().apply {
            moveTo(w * 0.5f, h * 0.06f)
            cubicTo(w * 0.58f, h * 0.34f, w * 0.66f, h * 0.42f, w * 0.94f, h * 0.5f)
            cubicTo(w * 0.66f, h * 0.58f, w * 0.58f, h * 0.66f, w * 0.5f, h * 0.94f)
            cubicTo(w * 0.42f, h * 0.66f, w * 0.34f, h * 0.58f, w * 0.06f, h * 0.5f)
            cubicTo(w * 0.34f, h * 0.42f, w * 0.42f, h * 0.34f, w * 0.5f, h * 0.06f)
            close()
        }
        drawPath(star, color)
    }
}

@Composable
fun NajoMark(modifier: Modifier = Modifier, background: Color, foreground: Color) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(foreground, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = w * 0.075f))
        drawLine(
            foreground,
            Offset(w * 0.5f, h * 0.5f),
            Offset(w * 0.5f, h * 0.26f),
            strokeWidth = w * 0.075f,
            cap = StrokeCap.Round,
        )
        drawLine(
            foreground,
            Offset(w * 0.5f, h * 0.5f),
            Offset(w * 0.68f, h * 0.6f),
            strokeWidth = w * 0.075f,
            cap = StrokeCap.Round,
        )
        drawCircle(foreground, radius = w * 0.045f, center = Offset(w * 0.5f, h * 0.5f))
    }
}
