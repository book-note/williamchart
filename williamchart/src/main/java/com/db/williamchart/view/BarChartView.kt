package com.db.williamchart.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import com.db.williamchart.ChartContract
import com.db.williamchart.ExperimentalFeature
import com.db.williamchart.R
import com.db.williamchart.animation.NoAnimation
import com.db.williamchart.data.*
import com.db.williamchart.data.configuration.BarChartConfiguration
import com.db.williamchart.data.configuration.ChartConfiguration
import com.db.williamchart.extensions.drawChartBar
import com.db.williamchart.extensions.obtainStyledAttributes
import com.db.williamchart.renderer.BarChartRenderer

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AxisChartView(context, attrs, defStyleAttr), ChartContract.BarView {

    @Suppress("MemberVisibilityCanBePrivate")
    var spacing = defaultSpacing

    @ColorInt
    @Suppress("MemberVisibilityCanBePrivate")
    var barsColor: Int = defaultBarsColor

    @ExperimentalFeature
    @Suppress("MemberVisibilityCanBePrivate")
    var barsColorsList: List<Int>? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var barRadius: Float = defaultBarsRadius

    @Suppress("MemberVisibilityCanBePrivate")
    var barsBackgroundColor: Int = -1

    override val chartConfiguration: ChartConfiguration
        get() =
            BarChartConfiguration(
                width = measuredWidth,
                height = measuredHeight,
                paddings = Paddings(
                    paddingLeft.toFloat(),
                    paddingTop.toFloat(),
                    paddingRight.toFloat(),
                    paddingBottom.toFloat()
                ),
                axis = axis,
                labelsSize = labelsSize,
                scale = scale,
                barsBackgroundColor = barsBackgroundColor,
                barsSpacing = spacing,
                labelsFormatter = labelsFormatter
            )

    init {
        renderer = BarChartRenderer(this, painter, NoAnimation())
        handleAttributes(obtainStyledAttributes(attrs, R.styleable.BarChartAttrs))
        handleEditMode()
    }

    override fun drawBars(frames: List<Frame>) {

        if (barsColorsList == null)
            barsColorsList = List(frames.size) { barsColor }.toList()

        if (barsColorsList!!.size != frames.size)
            throw IllegalArgumentException("Colors provided do not match the number of datapoints.")

        frames.forEachIndexed { index, frame ->
            painter.prepare(color = barsColorsList!![index], style = Paint.Style.FILL)
            val r = getNewBarRadius(barRadius, frame.toRect().width())
            canvas.drawChartBar(
                frame.toRectF(),
                r,
                painter.paint
            )
        }
    }

    override fun drawBarsBackground(frames: List<Frame>) {
        painter.prepare(color = barsBackgroundColor, style = Paint.Style.FILL)
        frames.forEach {
            val r = getNewBarRadius(barRadius, it.toRect().width())
            canvas.drawChartBar(
                it.toRectF(),
                r,
                painter.paint
            )
        }
    }

    private fun getNewBarRadius(barRadius: Float, barWidth: Int): Float {
        return if (barRadius < 0.01) {
            barWidth / 2f
        } else {
            barRadius
        }
    }

    override fun drawLabels(xLabels: List<Label>) {
        painter.prepare(textSize = labelsSize, color = labelsColor, font = labelsFont)
        labels.draw(canvas, painter.paint, xLabels)
    }

    override fun drawGrid(
        innerFrame: Frame,
        xLabelsPositions: List<Float>,
        yLabelsPositions: List<Float>
    ) {
        grid.draw(canvas, innerFrame, xLabelsPositions, yLabelsPositions)
    }

    override fun drawDebugFrame(frames: List<Frame>) {
        painter.prepare(color = -0x1000000, style = Paint.Style.STROKE)
        frames.forEach { canvas.drawRect(it.toRect(), painter.paint) }
    }

    private fun handleAttributes(typedArray: TypedArray) {
        typedArray.apply {
            spacing = getDimension(R.styleable.BarChartAttrs_chart_spacing, spacing)
            barsColor = getColor(R.styleable.BarChartAttrs_chart_barsColor, barsColor)
            barRadius = getDimension(R.styleable.BarChartAttrs_chart_barsRadius, barRadius)
            barsBackgroundColor =
                getColor(R.styleable.BarChartAttrs_chart_barsBackgroundColor, barsBackgroundColor)
            val resourceId = getResourceId(R.styleable.BarChartAttrs_chart_barsColorsList, -1)
            if (resourceId != -1)
                barsColorsList = resources.getIntArray(resourceId).toList()
            recycle()
        }
    }

    companion object {
        private const val defaultSpacing = 10f
        private const val defaultBarsColor = Color.BLACK
        private const val defaultBarsRadius = 0F
    }
}
