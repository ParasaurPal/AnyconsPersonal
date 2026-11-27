package com.example.rendering

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils
import com.example.data.model.GlyphKind
import com.example.data.model.GlyphMode
import com.example.data.model.IconPackStyle
import com.example.data.model.IconPresetStyle
import com.example.data.model.IconShape
import com.example.data.model.PackIconSpec

object NiagaraIconRenderer {

    fun renderToBitmap(
        icon: PackIconSpec,
        style: IconPackStyle,
        sizePx: Int = 192
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawIcon(
            canvas = canvas,
            width = sizePx.toFloat(),
            height = sizePx.toFloat(),
            glyphKind = icon.glyphKind,
            style = style,
            customTintArgb = icon.customTintArgb,
            customDotArgb = icon.customDotColorArgb
        )
        return bitmap
    }

    fun drawIcon(
        canvas: Canvas,
        width: Float,
        height: Float,
        glyphKind: GlyphKind,
        style: IconPackStyle,
        customTintArgb: Long? = null,
        customDotArgb: Long? = null
    ) {
        val size = minOf(width, height)
        if (size <= 0f) return
        val cx = width / 2f
        val cy = height / 2f
        val pad = size * 0.08f
        val left = cx - size / 2f + pad
        val top = cy - size / 2f + pad
        val right = cx + size / 2f - pad
        val bottom = cy + size / 2f - pad
        val rect = RectF(left, top, right, bottom)
        val containerSize = rect.width()

        val baseBgColor = (customTintArgb ?: style.backgroundArgb).toInt()
        val accentColor = (customDotArgb ?: style.accentArgb).toInt()
        val glyphColor = style.glyphArgb.toInt()
        val bgAlphaInt = (style.backgroundAlpha.coerceIn(0f, 1f) * 255f).toInt()

        val shapePath = buildShapePath(style.shape, rect)

        if (style.shape != IconShape.FRAMELESS) {
            drawContainer(
                canvas = canvas,
                shapePath = shapePath,
                rect = rect,
                style = style,
                baseBgColor = baseBgColor,
                accentColor = accentColor,
                bgAlphaInt = bgAlphaInt
            )
        }

        // Draw the inner glyph centered in the container
        val glyphBoxSize = containerSize * style.iconScale.coerceIn(0.32f, 0.82f)
        val scale = glyphBoxSize / 24f
        val glyphLeft = cx - glyphBoxSize / 2f
        val glyphTop = cy - glyphBoxSize / 2f

        val strokePx = (size / 64f) * style.strokeWidthDp.coerceIn(1.2f, 5.0f)

        val effectiveGlyphMode = style.glyphMode
        val primaryGlyphColor = when {
            style.shape == IconShape.FRAMELESS && style.presetStyle == IconPresetStyle.NORDIC_LINE -> accentColor
            else -> glyphColor
        }

        canvas.save()
        canvas.translate(glyphLeft, glyphTop)
        canvas.scale(scale, scale)

        val scaledStroke = strokePx / scale

        if (effectiveGlyphMode == GlyphMode.DUOTONE) {
            canvas.save()
            canvas.translate(0.9f, 0.9f)
            val duotonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ColorUtils.setAlphaComponent(accentColor, 145)
                this.style = Paint.Style.FILL
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                strokeWidth = scaledStroke
            }
            drawGlyphGeometry(canvas, glyphKind, duotonePaint, outlined = false)
            canvas.restore()
        }

        val isOutlined = effectiveGlyphMode == GlyphMode.OUTLINED ||
            style.presetStyle == IconPresetStyle.NORDIC_LINE

        val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryGlyphColor
            this.style = if (isOutlined) Paint.Style.STROKE else Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = scaledStroke
        }

        drawGlyphGeometry(canvas, glyphKind, glyphPaint, outlined = isOutlined)
        canvas.restore()

        // Signature Niagara Dot Accent orb
        if (style.dotAccentEnabled && (effectiveGlyphMode == GlyphMode.DOT_ACCENT || style.presetStyle == IconPresetStyle.NIAGARA_DOTS || style.presetStyle == IconPresetStyle.PEBBLE_GLASS)) {
            val dotRadius = containerSize * 0.115f
            val dotCx = right - containerSize * 0.15f
            val dotCy = top + containerSize * 0.15f

            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ColorUtils.setAlphaComponent(baseBgColor, 235)
                this.style = Paint.Style.FILL
            }
            if (style.shape != IconShape.FRAMELESS) {
                canvas.drawCircle(dotCx, dotCy, dotRadius * 1.42f, haloPaint)
            }
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                this.style = Paint.Style.FILL
            }
            canvas.drawCircle(dotCx, dotCy, dotRadius, dotPaint)
        }
    }

    private fun drawContainer(
        canvas: Canvas,
        shapePath: Path,
        rect: RectF,
        style: IconPackStyle,
        baseBgColor: Int,
        accentColor: Int,
        bgAlphaInt: Int
    ) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }

        when (style.presetStyle) {
            IconPresetStyle.NIAGARA_DOTS -> {
                val topColor = ColorUtils.blendARGB(baseBgColor, Color.WHITE, 0.08f)
                val bottomColor = ColorUtils.blendARGB(baseBgColor, Color.BLACK, 0.12f)
                fillPaint.shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    ColorUtils.setAlphaComponent(topColor, bgAlphaInt),
                    ColorUtils.setAlphaComponent(bottomColor, bgAlphaInt),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(shapePath, fillPaint)

                // Soft inner glow
                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        rect.left + rect.width() * 0.3f,
                        rect.top + rect.height() * 0.25f,
                        rect.width() * 0.75f,
                        ColorUtils.setAlphaComponent(accentColor, (bgAlphaInt * 0.16f).toInt()),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawPath(shapePath, glowPaint)
            }

            IconPresetStyle.ANYCON_DUOTONE -> {
                val blendedAccent = ColorUtils.blendARGB(baseBgColor, accentColor, 0.28f)
                fillPaint.shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    ColorUtils.setAlphaComponent(baseBgColor, bgAlphaInt),
                    ColorUtils.setAlphaComponent(blendedAccent, bgAlphaInt),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(shapePath, fillPaint)

                val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    strokeWidth = rect.width() * 0.025f
                    color = ColorUtils.setAlphaComponent(accentColor, 90)
                }
                canvas.drawPath(shapePath, rimPaint)
            }

            IconPresetStyle.NORDIC_LINE -> {
                fillPaint.color = ColorUtils.setAlphaComponent(baseBgColor, (bgAlphaInt * 0.35f).toInt())
                canvas.drawPath(shapePath, fillPaint)

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    strokeWidth = rect.width() * (0.022f + (style.strokeWidthDp - 1.5f) * 0.005f)
                    color = ColorUtils.setAlphaComponent(accentColor, 220)
                }
                canvas.drawPath(shapePath, borderPaint)
            }

            IconPresetStyle.PEBBLE_GLASS -> {
                fillPaint.shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    ColorUtils.setAlphaComponent(
                        ColorUtils.blendARGB(baseBgColor, Color.WHITE, 0.18f),
                        bgAlphaInt.coerceAtLeast(65)
                    ),
                    ColorUtils.setAlphaComponent(baseBgColor, (bgAlphaInt * 0.72f).toInt().coerceAtLeast(45)),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(shapePath, fillPaint)

                val glassRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    strokeWidth = rect.width() * 0.028f
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        ColorUtils.setAlphaComponent(Color.WHITE, 145),
                        ColorUtils.setAlphaComponent(accentColor, 55),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawPath(shapePath, glassRim)
            }

            IconPresetStyle.TACTILE_CLAY -> {
                val highlight = ColorUtils.blendARGB(baseBgColor, Color.WHITE, 0.14f)
                val shadow = ColorUtils.blendARGB(baseBgColor, Color.BLACK, 0.18f)
                fillPaint.shader = LinearGradient(
                    rect.left, rect.top, rect.right, rect.bottom,
                    ColorUtils.setAlphaComponent(highlight, bgAlphaInt),
                    ColorUtils.setAlphaComponent(shadow, bgAlphaInt),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(shapePath, fillPaint)

                val innerRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    strokeWidth = rect.width() * 0.03f
                    shader = LinearGradient(
                        rect.left, rect.top, rect.right, rect.bottom,
                        ColorUtils.setAlphaComponent(Color.WHITE, 85),
                        ColorUtils.setAlphaComponent(Color.BLACK, 55),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawPath(shapePath, innerRim)
            }
        }
    }

    private fun buildShapePath(shape: IconShape, rect: RectF): Path {
        val path = Path()
        val w = rect.width()
        val h = rect.height()
        when (shape) {
            IconShape.CIRCLE -> {
                path.addOval(rect, Path.Direction.CW)
            }
            IconShape.ROUNDED_SQUARE -> {
                val r = w * 0.25f
                path.addRoundRect(rect, r, r, Path.Direction.CW)
            }
            IconShape.SQUIRCLE -> {
                val cx = rect.centerX()
                val cy = rect.centerY()
                val rx = w / 2f
                val ry = h / 2f
                val k = 0.88f
                path.moveTo(cx, rect.top)
                path.cubicTo(cx + rx * k, rect.top, rect.right, cy - ry * k, rect.right, cy)
                path.cubicTo(rect.right, cy + ry * k, cx + rx * k, rect.bottom, cx, rect.bottom)
                path.cubicTo(cx - rx * k, rect.bottom, rect.left, cy + ry * k, rect.left, cy)
                path.cubicTo(rect.left, cy - ry * k, cx - rx * k, rect.top, cx, rect.top)
                path.close()
            }
            IconShape.PEBBLE -> {
                val cx = rect.centerX()
                val cy = rect.centerY()
                val rx = w / 2f
                val ry = h / 2f
                path.moveTo(cx - rx * 0.08f, rect.top)
                path.cubicTo(cx + rx * 0.82f, rect.top, rect.right, cy - ry * 0.52f, rect.right - w * 0.02f, cy + ry * 0.06f)
                path.cubicTo(rect.right - w * 0.04f, cy + ry * 0.86f, cx + rx * 0.55f, rect.bottom, cx - rx * 0.06f, rect.bottom)
                path.cubicTo(cx - rx * 0.86f, rect.bottom, rect.left, cy + ry * 0.65f, rect.left, cy - ry * 0.04f)
                path.cubicTo(rect.left, cy - ry * 0.82f, cx - rx * 0.58f, rect.top, cx - rx * 0.08f, rect.top)
                path.close()
            }
            IconShape.PILL -> {
                val pillRect = RectF(
                    rect.left,
                    rect.top + h * 0.11f,
                    rect.right,
                    rect.bottom - h * 0.11f
                )
                val r = pillRect.height() / 2f
                path.addRoundRect(pillRect, r, r, Path.Direction.CW)
            }
            IconShape.FRAMELESS -> Unit
        }
        return path
    }

    private fun drawGlyphGeometry(
        canvas: Canvas,
        kind: GlyphKind,
        paint: Paint,
        outlined: Boolean
    ) {
        val strokePaint = if (outlined) paint else Paint(paint).apply {
            this.style = Paint.Style.STROKE
            if (strokeWidth < 1.8f) strokeWidth = 2.0f
        }
        val fillPaint = if (!outlined) paint else Paint(paint).apply {
            this.style = Paint.Style.FILL
        }

        when (kind) {
            GlyphKind.PHONE -> {
                val p = Path().apply {
                    moveTo(6.5f, 4.5f)
                    lineTo(9.8f, 4.5f)
                    lineTo(11f, 8.6f)
                    lineTo(8.8f, 10.4f)
                    quadTo(10.5f, 13.7f, 13.6f, 15.2f)
                    lineTo(15.4f, 13f)
                    lineTo(19.5f, 14.2f)
                    lineTo(19.5f, 17.5f)
                    quadTo(19.5f, 19.6f, 17.3f, 19.6f)
                    cubicTo(10.2f, 19.2f, 4.8f, 13.8f, 4.4f, 6.7f)
                    quadTo(4.4f, 4.5f, 6.5f, 4.5f)
                    close()
                }
                canvas.drawPath(p, paint)
            }

            GlyphKind.MESSAGES -> {
                val p = Path().apply {
                    moveTo(4.5f, 5.5f)
                    lineTo(19.5f, 5.5f)
                    quadTo(20.5f, 5.5f, 20.5f, 6.5f)
                    lineTo(20.5f, 15.8f)
                    quadTo(20.5f, 16.8f, 19.5f, 16.8f)
                    lineTo(9.5f, 16.8f)
                    lineTo(5.2f, 20.2f)
                    lineTo(5.2f, 16.8f)
                    lineTo(4.5f, 16.8f)
                    quadTo(3.5f, 16.8f, 3.5f, 15.8f)
                    lineTo(3.5f, 6.5f)
                    quadTo(3.5f, 5.5f, 4.5f, 5.5f)
                    close()
                }
                canvas.drawPath(p, paint)
            }

            GlyphKind.MAIL -> {
                val rect = RectF(3.5f, 5.5f, 20.5f, 18.5f)
                canvas.drawRoundRect(rect, 2.2f, 2.2f, strokePaint)
                val flap = Path().apply {
                    moveTo(4.2f, 6.5f)
                    lineTo(12f, 12.6f)
                    lineTo(19.8f, 6.5f)
                }
                canvas.drawPath(flap, strokePaint)
            }

            GlyphKind.CHAT_BUBBLE -> {
                canvas.drawCircle(12f, 11.2f, 7.6f, paint)
                val tail = Path().apply {
                    moveTo(6.8f, 16.5f)
                    lineTo(5.0f, 20.2f)
                    lineTo(10.0f, 18.2f)
                    close()
                }
                canvas.drawPath(tail, paint)
            }

            GlyphKind.VIDEO_CALL -> {
                canvas.drawRoundRect(RectF(3.5f, 6.5f, 15.2f, 17.5f), 2.5f, 2.5f, paint)
                val lens = Path().apply {
                    moveTo(16.2f, 9.8f)
                    lineTo(20.6f, 7.2f)
                    lineTo(20.6f, 16.8f)
                    lineTo(16.2f, 14.2f)
                    close()
                }
                canvas.drawPath(lens, paint)
            }

            GlyphKind.SIGNAL_WAVE -> {
                canvas.drawCircle(12f, 12f, 2.2f, fillPaint)
                canvas.drawArc(RectF(7f, 7f, 17f, 17f), -45f, 90f, false, strokePaint)
                canvas.drawArc(RectF(7f, 7f, 17f, 17f), 135f, 90f, false, strokePaint)
                canvas.drawArc(RectF(3.5f, 3.5f, 20.5f, 20.5f), -45f, 90f, false, strokePaint)
                canvas.drawArc(RectF(3.5f, 3.5f, 20.5f, 20.5f), 135f, 90f, false, strokePaint)
            }

            GlyphKind.CAMERA -> {
                val body = Path().apply {
                    moveTo(4f, 8f)
                    lineTo(8f, 8f)
                    lineTo(9.6f, 5.5f)
                    lineTo(14.4f, 5.5f)
                    lineTo(16f, 8f)
                    lineTo(20f, 8f)
                    quadTo(21f, 8f, 21f, 9f)
                    lineTo(21f, 18f)
                    quadTo(21f, 19f, 20f, 19f)
                    lineTo(4f, 19f)
                    quadTo(3f, 19f, 3f, 18f)
                    lineTo(3f, 9f)
                    quadTo(3f, 8f, 4f, 8f)
                    close()
                }
                canvas.drawPath(body, strokePaint)
                canvas.drawCircle(12f, 13.5f, 3.4f, strokePaint)
            }

            GlyphKind.PHOTOS -> {
                canvas.drawRoundRect(RectF(3.8f, 4.5f, 20.2f, 19.5f), 2.5f, 2.5f, strokePaint)
                val hills = Path().apply {
                    moveTo(5.2f, 17.8f)
                    lineTo(9.6f, 11.8f)
                    lineTo(13.2f, 15.6f)
                    lineTo(15.6f, 13.0f)
                    lineTo(18.8f, 17.8f)
                }
                canvas.drawPath(hills, strokePaint)
                canvas.drawCircle(15.6f, 8.6f, 1.5f, fillPaint)
            }

            GlyphKind.MUSIC -> {
                val stem = Path().apply {
                    moveTo(9.5f, 16.5f)
                    lineTo(9.5f, 5.8f)
                    lineTo(18.5f, 4.2f)
                    lineTo(18.5f, 14.5f)
                }
                canvas.drawPath(stem, strokePaint)
                canvas.drawCircle(7.4f, 17.2f, 2.5f, paint)
                canvas.drawCircle(16.4f, 15.2f, 2.5f, paint)
            }

            GlyphKind.PLAY -> {
                val tri = Path().apply {
                    moveTo(7.2f, 5.2f)
                    lineTo(19.2f, 12f)
                    lineTo(7.2f, 18.8f)
                    close()
                }
                canvas.drawPath(tri, paint)
            }

            GlyphKind.HEADPHONES -> {
                canvas.drawArc(RectF(4.5f, 4.5f, 19.5f, 19.5f), 180f, 180f, false, strokePaint)
                canvas.drawRoundRect(RectF(4f, 12f, 7.8f, 18.5f), 1.8f, 1.8f, paint)
                canvas.drawRoundRect(RectF(16.2f, 12f, 20f, 18.5f), 1.8f, 1.8f, paint)
            }

            GlyphKind.PODCAST -> {
                canvas.drawCircle(12f, 10.5f, 2.2f, fillPaint)
                canvas.drawArc(RectF(7.2f, 5.7f, 16.8f, 15.3f), 150f, 240f, false, strokePaint)
                canvas.drawArc(RectF(4.2f, 2.7f, 19.8f, 18.3f), 150f, 240f, false, strokePaint)
                val base = Path().apply {
                    moveTo(10.5f, 19.5f)
                    lineTo(12f, 14.2f)
                    lineTo(13.5f, 19.5f)
                    close()
                }
                canvas.drawPath(base, fillPaint)
            }

            GlyphKind.TV -> {
                canvas.drawRoundRect(RectF(3.5f, 5.5f, 20.5f, 16.5f), 2.2f, 2.2f, strokePaint)
                canvas.drawLine(8.5f, 19.5f, 15.5f, 19.5f, strokePaint)
                canvas.drawLine(12f, 16.5f, 12f, 19.5f, strokePaint)
            }

            GlyphKind.CALENDAR -> {
                canvas.drawRoundRect(RectF(4f, 5.5f, 20f, 19.5f), 2.2f, 2.2f, strokePaint)
                canvas.drawLine(4f, 9.8f, 20f, 9.8f, strokePaint)
                canvas.drawLine(8.2f, 3.8f, 8.2f, 6.8f, strokePaint)
                canvas.drawLine(15.8f, 3.8f, 15.8f, 6.8f, strokePaint)
                canvas.drawCircle(9f, 14.2f, 1.3f, fillPaint)
                canvas.drawCircle(15f, 14.2f, 1.3f, fillPaint)
            }

            GlyphKind.CLOCK -> {
                canvas.drawCircle(12f, 12f, 8.2f, strokePaint)
                canvas.drawLine(12f, 12f, 12f, 7.2f, strokePaint)
                canvas.drawLine(12f, 12f, 15.8f, 14.2f, strokePaint)
            }

            GlyphKind.NOTES -> {
                canvas.drawRoundRect(RectF(5f, 3.8f, 19f, 20.2f), 2f, 2f, strokePaint)
                canvas.drawLine(8.2f, 8.5f, 15.8f, 8.5f, strokePaint)
                canvas.drawLine(8.2f, 12f, 15.8f, 12f, strokePaint)
                canvas.drawLine(8.2f, 15.5f, 13.2f, 15.5f, strokePaint)
            }

            GlyphKind.CHECKLIST -> {
                val p = Path().apply {
                    moveTo(5.2f, 12.2f)
                    lineTo(9.8f, 16.8f)
                    lineTo(18.8f, 7.2f)
                }
                canvas.drawPath(p, strokePaint)
            }

            GlyphKind.FOLDER -> {
                val p = Path().apply {
                    moveTo(3.8f, 6.2f)
                    lineTo(9.5f, 6.2f)
                    lineTo(11.3f, 8.4f)
                    lineTo(20.2f, 8.4f)
                    lineTo(20.2f, 18.2f)
                    lineTo(3.8f, 18.2f)
                    close()
                }
                canvas.drawPath(p, paint)
            }

            GlyphKind.DOCUMENT -> {
                val p = Path().apply {
                    moveTo(6f, 3.8f)
                    lineTo(14.5f, 3.8f)
                    lineTo(18f, 7.3f)
                    lineTo(18f, 20.2f)
                    lineTo(6f, 20.2f)
                    close()
                }
                canvas.drawPath(p, strokePaint)
                canvas.drawLine(9f, 11.5f, 15f, 11.5f, strokePaint)
                canvas.drawLine(9f, 15.2f, 15f, 15.2f, strokePaint)
            }

            GlyphKind.CALCULATOR -> {
                canvas.drawRoundRect(RectF(5f, 3.8f, 19f, 20.2f), 2f, 2f, strokePaint)
                canvas.drawLine(8f, 7.5f, 16f, 7.5f, strokePaint)
                canvas.drawCircle(8.8f, 12f, 1.1f, fillPaint)
                canvas.drawCircle(12f, 12f, 1.1f, fillPaint)
                canvas.drawCircle(15.2f, 12f, 1.1f, fillPaint)
                canvas.drawCircle(8.8f, 16f, 1.1f, fillPaint)
                canvas.drawCircle(12f, 16f, 1.1f, fillPaint)
                canvas.drawCircle(15.2f, 16f, 1.1f, fillPaint)
            }

            GlyphKind.TERMINAL -> {
                canvas.drawRoundRect(RectF(3.5f, 4.8f, 20.5f, 19.2f), 2.2f, 2.2f, strokePaint)
                val chevron = Path().apply {
                    moveTo(7.2f, 9.2f)
                    lineTo(10.5f, 12f)
                    lineTo(7.2f, 14.8f)
                }
                canvas.drawPath(chevron, strokePaint)
                canvas.drawLine(12.2f, 15f, 16.5f, 15f, strokePaint)
            }

            GlyphKind.GLOBE -> {
                canvas.drawCircle(12f, 12f, 8.2f, strokePaint)
                canvas.drawOval(RectF(8.2f, 3.8f, 15.8f, 20.2f), strokePaint)
                canvas.drawLine(3.8f, 12f, 20.2f, 12f, strokePaint)
            }

            GlyphKind.COMPASS -> {
                canvas.drawCircle(12f, 12f, 8.2f, strokePaint)
                val needle = Path().apply {
                    moveTo(15.5f, 8.5f)
                    lineTo(13.2f, 13.2f)
                    lineTo(8.5f, 15.5f)
                    lineTo(10.8f, 10.8f)
                    close()
                }
                canvas.drawPath(needle, fillPaint)
            }

            GlyphKind.SHARE -> {
                canvas.drawCircle(16.8f, 6.2f, 2.2f, fillPaint)
                canvas.drawCircle(6.8f, 12f, 2.2f, fillPaint)
                canvas.drawCircle(16.8f, 17.8f, 2.2f, fillPaint)
                canvas.drawLine(8.8f, 10.9f, 14.8f, 7.3f, strokePaint)
                canvas.drawLine(8.8f, 13.1f, 14.8f, 16.7f, strokePaint)
            }

            GlyphKind.HEART -> {
                val heart = Path().apply {
                    moveTo(12f, 19.2f)
                    cubicTo(4.2f, 14.4f, 3.5f, 8.5f, 7.2f, 6.1f)
                    cubicTo(9.4f, 4.7f, 11.4f, 6.2f, 12f, 7.6f)
                    cubicTo(12.6f, 6.2f, 14.6f, 4.7f, 16.8f, 6.1f)
                    cubicTo(20.5f, 8.5f, 19.8f, 14.4f, 12f, 19.2f)
                    close()
                }
                canvas.drawPath(heart, paint)
            }

            GlyphKind.STAR -> {
                val star = buildStarPath(12f, 12f, 8.6f, 3.8f, 5)
                canvas.drawPath(star, paint)
            }

            GlyphKind.SPARKLE -> {
                val spark = buildStarPath(12f, 12f, 8.8f, 2.4f, 4)
                canvas.drawPath(spark, paint)
            }

            GlyphKind.BOOKMARK -> {
                val bm = Path().apply {
                    moveTo(6.8f, 4.2f)
                    lineTo(17.2f, 4.2f)
                    lineTo(17.2f, 19.8f)
                    lineTo(12f, 15.8f)
                    lineTo(6.8f, 19.8f)
                    close()
                }
                canvas.drawPath(bm, paint)
            }

            GlyphKind.PIN -> {
                val pin = Path().apply {
                    moveTo(12f, 20.2f)
                    cubicTo(12f, 20.2f, 18f, 14.2f, 18f, 9.8f)
                    arcTo(RectF(6f, 3.8f, 18f, 15.8f), 0f, -180f, false)
                    cubicTo(6f, 14.2f, 12f, 20.2f, 12f, 20.2f)
                    close()
                }
                canvas.drawPath(pin, strokePaint)
                canvas.drawCircle(12f, 9.8f, 2.1f, fillPaint)
            }

            GlyphKind.NAVIGATION -> {
                val nav = Path().apply {
                    moveTo(12f, 4.2f)
                    lineTo(18.8f, 19.2f)
                    lineTo(12f, 15.8f)
                    lineTo(5.2f, 19.2f)
                    close()
                }
                canvas.drawPath(nav, paint)
            }

            GlyphKind.CAR -> {
                val car = Path().apply {
                    moveTo(5.5f, 11.5f)
                    lineTo(7.4f, 6.8f)
                    lineTo(16.6f, 6.8f)
                    lineTo(18.5f, 11.5f)
                    lineTo(19.5f, 11.5f)
                    lineTo(19.5f, 16.5f)
                    lineTo(4.5f, 16.5f)
                    lineTo(4.5f, 11.5f)
                    close()
                }
                canvas.drawPath(car, strokePaint)
                canvas.drawCircle(7.5f, 17.5f, 1.5f, fillPaint)
                canvas.drawCircle(16.5f, 17.5f, 1.5f, fillPaint)
            }

            GlyphKind.FLIGHT -> {
                val plane = Path().apply {
                    moveTo(12f, 3.8f)
                    lineTo(13.8f, 9.6f)
                    lineTo(19.8f, 13.2f)
                    lineTo(19.8f, 15.2f)
                    lineTo(13.8f, 13.5f)
                    lineTo(13.8f, 17.8f)
                    lineTo(16.2f, 19.6f)
                    lineTo(16.2f, 20.8f)
                    lineTo(12f, 19.6f)
                    lineTo(7.8f, 20.8f)
                    lineTo(7.8f, 19.6f)
                    lineTo(10.2f, 17.8f)
                    lineTo(10.2f, 13.5f)
                    lineTo(4.2f, 15.2f)
                    lineTo(4.2f, 13.2f)
                    lineTo(10.2f, 9.6f)
                    close()
                }
                canvas.drawPath(plane, paint)
            }

            GlyphKind.SUN_WEATHER -> {
                canvas.drawCircle(12f, 12f, 4.2f, paint)
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val x1 = 12f + (6.4f * Math.cos(angle)).toFloat()
                    val y1 = 12f + (6.4f * Math.sin(angle)).toFloat()
                    val x2 = 12f + (8.6f * Math.cos(angle)).toFloat()
                    val y2 = 12f + (8.6f * Math.sin(angle)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, strokePaint)
                }
            }

            GlyphKind.SETTINGS -> {
                canvas.drawCircle(12f, 12f, 3.0f, strokePaint)
                canvas.drawCircle(12f, 12f, 6.4f, strokePaint)
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60).toDouble())
                    val x1 = 12f + (6.4f * Math.cos(angle)).toFloat()
                    val y1 = 12f + (6.4f * Math.sin(angle)).toFloat()
                    val x2 = 12f + (8.6f * Math.cos(angle)).toFloat()
                    val y2 = 12f + (8.6f * Math.sin(angle)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, strokePaint)
                }
            }

            GlyphKind.LOCK -> {
                canvas.drawArc(RectF(8f, 4.2f, 16f, 12.2f), 180f, 180f, false, strokePaint)
                canvas.drawRoundRect(RectF(5.5f, 10f, 18.5f, 19.5f), 2.2f, 2.2f, paint)
            }

            GlyphKind.WALLET -> {
                canvas.drawRoundRect(RectF(4f, 6f, 20f, 18f), 2.2f, 2.2f, strokePaint)
                canvas.drawRoundRect(RectF(14f, 10.2f, 20.5f, 13.8f), 1.4f, 1.4f, fillPaint)
            }

            GlyphKind.FLASHLIGHT -> {
                val bolt = Path().apply {
                    moveTo(13.4f, 3.8f)
                    lineTo(6.5f, 13f)
                    lineTo(11.6f, 13f)
                    lineTo(10.6f, 20.2f)
                    lineTo(17.5f, 11f)
                    lineTo(12.4f, 11f)
                    close()
                }
                canvas.drawPath(bolt, paint)
            }

            GlyphKind.DOWNLOAD -> {
                canvas.drawLine(12f, 4.5f, 12f, 15.2f, strokePaint)
                val arrow = Path().apply {
                    moveTo(7.8f, 11.4f)
                    lineTo(12f, 15.8f)
                    lineTo(16.2f, 11.4f)
                }
                canvas.drawPath(arrow, strokePaint)
                canvas.drawLine(5.5f, 19f, 18.5f, 19f, strokePaint)
            }

            GlyphKind.SEARCH -> {
                canvas.drawCircle(10.5f, 10.5f, 5.8f, strokePaint)
                canvas.drawLine(14.8f, 14.8f, 19.2f, 19.2f, strokePaint)
            }

            GlyphKind.CODE -> {
                val leftBr = Path().apply {
                    moveTo(9f, 7.5f)
                    lineTo(4.8f, 12f)
                    lineTo(9f, 16.5f)
                }
                val rightBr = Path().apply {
                    moveTo(15f, 7.5f)
                    lineTo(19.2f, 12f)
                    lineTo(15f, 16.5f)
                }
                canvas.drawPath(leftBr, strokePaint)
                canvas.drawPath(rightBr, strokePaint)
                canvas.drawLine(13.2f, 6.2f, 10.8f, 17.8f, strokePaint)
            }

            GlyphKind.FITNESS -> {
                val pulse = Path().apply {
                    moveTo(3.5f, 12.5f)
                    lineTo(7.8f, 12.5f)
                    lineTo(10.2f, 6.5f)
                    lineTo(13.6f, 17.5f)
                    lineTo(16.2f, 12.5f)
                    lineTo(20.5f, 12.5f)
                }
                canvas.drawPath(pulse, strokePaint)
            }

            GlyphKind.BOOK -> {
                val book = Path().apply {
                    moveTo(12f, 6.5f)
                    quadTo(8f, 4.5f, 4f, 6.5f)
                    lineTo(4f, 18.2f)
                    quadTo(8f, 16.2f, 12f, 18.2f)
                    quadTo(16f, 16.2f, 20f, 18.2f)
                    lineTo(20f, 6.5f)
                    quadTo(16f, 4.5f, 12f, 6.5f)
                    close()
                }
                canvas.drawPath(book, strokePaint)
                canvas.drawLine(12f, 6.5f, 12f, 18.2f, strokePaint)
            }
        }
    }

    private fun buildStarPath(cx: Float, cy: Float, outerR: Float, innerR: Float, points: Int): Path {
        val path = Path()
        val step = Math.PI / points
        var angle = -Math.PI / 2.0
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val x = cx + (r * Math.cos(angle)).toFloat()
            val y = cy + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            angle += step
        }
        path.close()
        return path
    }
}
