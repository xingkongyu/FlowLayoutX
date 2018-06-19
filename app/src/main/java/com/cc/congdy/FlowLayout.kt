package com.cc.congdy

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 请描述使用该类使用方法！！！
 *
 * @author 陈聪 2018-06-14 10:17
 */
class FlowLayout(context: Context, attributeSet: AttributeSet) : ViewGroup(context, attributeSet) {

    /**
     * 用于记录child距离顶部距离
     */
    private var childTopOffsets: HashMap<View, Int> = HashMap()
    /**
     * 用于记录child距离左侧距离--用于配置所有标签位置显示
     * 配合gravity使用
     */
    private var childOffsets: HashMap<Int, Int> = HashMap()
    /**
     * 用于记录当前child是第几行的view
     */
    private var childOfLines: HashMap<View, Int> = HashMap()
    /**
     * 用于指定对齐方式
     */
    private var gravity: Int = GRAVITY_LEFT

    companion object {
        val GRAVITY_LEFT = 0//左对齐
        val GRAVITY_CENTER = 1//居中
        val GRAVITY_RIGHT = 2//右对齐
    }

    init {

    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * 设置对齐方式
     * 默认为GRAVITY_LEFT左对齐
     */
    fun setGravity(gravity: Int) {
        this.gravity = gravity
        //这里要注意，必须先调用 requestLayout() 方法再调用 invalidate （）方法。具体原因是，在调用 requestLayout() 方法时，view 只会执行 onMeasure（先）及 onLayout（后）方法，而调用 invalidate （）方法时，view 会调用 onDraw（）方法。调用完这两个方法你的自定义控件就可以重绘及更新了。
        //ps：若只改变宽高调用 requestLayout() 方法即可，若只更新内容调用  invalidate （）方法。
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        childTopOffsets.clear()
        childOfLines.clear()

        //获取主布局宽高
        val pWidth = MeasureSpec.getSize(widthMeasureSpec)
        val pHeight = MeasureSpec.getSize(heightMeasureSpec)
        //获取主布局宽高模式
        val pWMode = MeasureSpec.getMode(widthMeasureSpec)
        val pHMode = MeasureSpec.getMode(heightMeasureSpec)

        var wCur = 0//当前记录的最大宽度
        var hCur = 0//当前记录的最大高度
        var temW = 0//临时行宽度记录
        var temH = 0//临时行高度记录
        var line = 0//记录当前第几行了
        //流布局宽高都为warp_content的情况下
        var childW: Int//子控件宽度
        var childH: Int//子控件高度
        for (i in 0 until childCount) {
            var child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            var param = child.layoutParams as MarginLayoutParams
            childW = child.measuredWidth + param.leftMargin + param.rightMargin
            childH = child.measuredHeight + param.topMargin + param.bottomMargin
            if (temW + childW > pWidth) {
                //出现需要换行的情况的时候
                wCur = Math.max(temW, wCur)
                hCur += temH
                childTopOffsets.put(child, hCur)//记录需要顶部偏移量
                line++
                //初始化下一行数据
                temW = childW
                temH = childH
            } else {
                temW += childW
                temH = Math.max(childH, temH)
            }
            if (gravity != GRAVITY_LEFT)
                childOffsets.put(line, (pWidth - temW) / 2)//记录需要左侧偏移量
            childOfLines.put(child, line)//记录子控件所在行数
        }
        hCur += temH //最后一行的高度记得加上

        //非match_parent 情况下校准左侧偏移量
        if (pWMode != MeasureSpec.EXACTLY && gravity != GRAVITY_LEFT) {
            for (i in 0 until childOffsets.size) {
                childOffsets[i] = childOffsets[i]!!.minus((pWidth - wCur) / 2)
            }
        }
        //=======================================
        //确定最终的宽高
        setMeasuredDimension(if (pWMode == MeasureSpec.EXACTLY) {
            pWidth
        } else {
            wCur
        }, if (pHMode == MeasureSpec.EXACTLY) {
            pHeight
        } else {
            hCur
        })
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = 0
        var currentLine = 0//当前行数
        var left = getOffset(currentLine)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val param = child.layoutParams as MarginLayoutParams
            if (currentLine != childOfLines[child]) {
                currentLine = childOfLines[child]!!
                top += childTopOffsets[child]!!
                left = getOffset(currentLine)
            }
            child.layout(left + param.leftMargin, top + param.topMargin, left + child.measuredWidth + param.leftMargin, top + child.measuredHeight + param.topMargin)
            left += child.measuredWidth + param.leftMargin + param.rightMargin
        }
    }

    /**
     * 根据行数获取当前左侧偏移量
     */
    private fun getOffset(currentLine: Int): Int {
        return when (gravity) {
            GRAVITY_LEFT -> 0
            GRAVITY_CENTER -> childOffsets[currentLine]!!
            GRAVITY_RIGHT -> childOffsets[currentLine]!! * 2
            else -> 0
        }
    }
}