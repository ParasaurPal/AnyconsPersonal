package com.example.rendering

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.GlyphKind
import com.example.data.model.IconPackStyle

@Composable
fun NiagaraStyleIcon(
    glyphKind: GlyphKind,
    style: IconPackStyle,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    customTintArgb: Long? = null,
    customDotArgb: Long? = null,
    contentDescription: String? = null
) {
    val semanticsModifier = if (contentDescription != null) {
        modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription }
    } else {
        modifier.size(size)
    }

    Canvas(modifier = semanticsModifier) {
        drawIntoCanvas { canvas ->
            NiagaraIconRenderer.drawIcon(
                canvas = canvas.nativeCanvas,
                width = this.size.width,
                height = this.size.height,
                glyphKind = glyphKind,
                style = style,
                customTintArgb = customTintArgb,
                customDotArgb = customDotArgb
            )
        }
    }
}
