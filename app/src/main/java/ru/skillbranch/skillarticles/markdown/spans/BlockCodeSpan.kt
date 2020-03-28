package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.markdown.Element


class BlockCodeSpan(
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val bgColor: Int,
    @Px
    private val cornerRadius: Float,
    @Px
    private val padding: Float,
    private val type: Element.BlockCode.Type
) : ReplacementSpan() {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect = RectF()
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint.forBackground {
            when(type) {
                Element.BlockCode.Type.SINGLE -> {
                    rect.set(0f, top.toFloat() + padding, canvas.width.toFloat(), bottom.toFloat() - padding)
                }
                Element.BlockCode.Type.START -> {
                    rect.set(0f, top.toFloat() + padding, canvas.width.toFloat(), bottom.toFloat())
                    drawBottomCorners(canvas, paint)
                }
                Element.BlockCode.Type.MIDDLE -> {
                    rect.set(0f, top.toFloat(), canvas.width.toFloat(), bottom.toFloat())
                    drawBottomCorners(canvas, paint)
                    drawTopCorners(canvas, paint)
                }
                Element.BlockCode.Type.END -> {
                    rect.set(0f, top.toFloat(), canvas.width.toFloat(), bottom.toFloat() - padding)
                    drawTopCorners(canvas, paint)
                }
            }

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }

        paint.forText {
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        fm ?: return 0
        when (type) {
            Element.BlockCode.Type.SINGLE -> {
                fm.ascent = fm.ascent - 2 * padding.toInt()
                fm.descent= fm.descent + 2 * padding.toInt()
            }
            Element.BlockCode.Type.START -> {
                fm.ascent = fm.ascent - 2 * padding.toInt()
                fm.descent = paint.descent().toInt()
            }
            Element.BlockCode.Type.MIDDLE -> {
                fm.ascent = paint.ascent().toInt()
                fm.descent = paint.descent().toInt()
            }
            Element.BlockCode.Type.END -> {
                fm.ascent = paint.ascent().toInt()
                fm.descent= fm.descent + 2 * padding.toInt()
            }
        }
        return 0
    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style

        color = bgColor
        style = Paint.Style.FILL

        block()

        color = oldColor
        style = oldStyle
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldSize = textSize
        val oldStyle = typeface?.style ?: 0
        val oldFont = typeface
        val oldColor = color

        color = textColor
        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)
        textSize *=0.85f

        block()

        color = oldColor
        typeface = oldFont
        textSize = oldSize
    }

    private fun drawBottomCorners(canvas: Canvas, paint: Paint) {
        path.reset()
        path.moveTo(rect.left, rect.bottom)
        path.lineTo(rect.left, rect.bottom - cornerRadius)
        path.lineTo(rect.left + cornerRadius, rect.bottom)
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(rect.right, rect.bottom)
        path.lineTo(rect.right, rect.bottom - cornerRadius)
        path.lineTo(rect.right - cornerRadius, rect.bottom)
        canvas.drawPath(path, paint)

    }

    private fun drawTopCorners(canvas: Canvas, paint: Paint) {
        path.reset()
        path.moveTo(rect.left, rect.top)
        path.lineTo(rect.left, rect.top + cornerRadius)
        path.lineTo(rect.left + cornerRadius, rect.top)
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(rect.right, rect.top)
        path.lineTo(rect.right, rect.top + cornerRadius)
        path.lineTo(rect.right - cornerRadius, rect.top)
        canvas.drawPath(path, paint)

    }
}
