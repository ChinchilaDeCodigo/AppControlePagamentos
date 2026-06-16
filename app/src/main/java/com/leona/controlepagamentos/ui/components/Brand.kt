package com.leona.controlepagamentos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.leona.controlepagamentos.ui.theme.Blue
import com.leona.controlepagamentos.ui.theme.Navy
import com.leona.controlepagamentos.ui.theme.PlusJakartaSans

/**
 * Símbolo do Pingo — ondas concêntricas com ponto central.
 * Funciona como ícone, badge e decoração em qualquer tamanho.
 */
@Composable
fun PingMark(modifier: Modifier = Modifier, color: Color = Blue) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        drawCircle(color.copy(alpha = .22f), r * 0.92f, center, style = Stroke(r * 0.085f))
        drawCircle(color.copy(alpha = .50f), r * 0.62f, center, style = Stroke(r * 0.135f))
        drawCircle(color,                    r * 0.34f, center, style = Stroke(r * 0.165f))
        drawCircle(color,                    r * 0.12f, center)
    }
}

/**
 * Wordmark: símbolo PingMark + texto "pingo" em tipografia normal.
 */
@Composable
fun PingoWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
    textColor: Color = Navy,
    pingColor: Color = Blue,
) {
    val density = LocalDensity.current
    val markSize = with(density) { (fontSize.toPx() * 0.85f).toDp() }
    val gap = with(density) { (fontSize.toPx() * 0.18f).toDp() }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        PingMark(modifier = Modifier.size(markSize), color = pingColor)
        Spacer(Modifier.width(gap))
        Text(
            text = "pingo",
            color = textColor,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = (-0.035).em
        )
    }
}
