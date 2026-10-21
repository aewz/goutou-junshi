package com.aewz.goutou.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 狗头军师视图套件 — one shared builder set for every page (and the overlay).
 *
 * Vocabulary:
 *  - page()        全页容器
 *  - title()       页面大标题
 *  - section()     分区头：紫色竖条 + 小字
 *  - card()        白卡：描边 + 柔和投影
 *  - primaryBtn()  紫渐变主按钮
 *  - ghostBtn()    白底紫描边次按钮
 *  - chip()        小标签
 *  - toggle()      胶囊开关（紫底白钮）
 *  - editBox()     输入框
 *  - divider()     发丝线
 */
object Views {

    fun dp(ctx: Context, v: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), ctx.resources.displayMetrics).toInt()

    fun page(ctx: Context, padTop: Int = 20): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(ctx, 18), dp(ctx, padTop), dp(ctx, 18), dp(ctx, 28))
            setBackgroundColor(Palette.BG)
        }

    fun title(ctx: Context, t: String): TextView =
        TextView(ctx).apply {
            text = t; textSize = 25f; setTextColor(Palette.PAPER)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.01f
            setPadding(0, 0, 0, dp(ctx, 6))
        }

    /** 分区头：左侧 3dp 紫条 + 小字标题。 */
    fun section(ctx: Context, t: String): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(ctx, 2), dp(ctx, 16), 0, dp(ctx, 6))
            addView(View(ctx).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(ctx, 2).toFloat(); setColor(Palette.BRAND)
                }
                layoutParams = LinearLayout.LayoutParams(dp(ctx, 3), dp(ctx, 12))
            })
            addView(TextView(ctx).apply {
                text = t; textSize = 11.5f; setTextColor(Palette.SUB)
                setTypeface(typeface, Typeface.BOLD)
                letterSpacing = 0.12f
                setPadding(dp(ctx, 8), 0, 0, 0)
            })
        }

    /** 白卡：描边 + 柔和投影（elevation 阴影随圆角背景）。 */
    fun card(ctx: Context, raised: Boolean = false): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = round(ctx, 14, if (raised) Palette.CARD_RAISED else Palette.CARD, stroke = true)
            elevation = dp(ctx, if (raised) 2 else 1).toFloat()
            setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12))
        }

    fun text(ctx: Context, t: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(ctx).apply {
            text = t; setTextColor(color); textSize = size
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    fun rowText(ctx: Context, t: String, size: Float = 14f, color: Int = Palette.PAPER, bold: Boolean = false) =
        text(ctx, t, size, color, bold)

    fun hintText(ctx: Context, t: String) = text(ctx, t, 12f, Palette.SUB)

    /** 大标签（输入区标题）。 */
    fun label(ctx: Context, t: String) = text(ctx, t, 13f, Palette.BRAND, bold = true).apply {
        setPadding(0, dp(ctx, 12), 0, dp(ctx, 4))
        letterSpacing = 0.04f
    }

    /** 紫渐变主按钮（315° BRAND_DEEP → BRAND）。 */
    fun primaryBtn(ctx: Context, label: String, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            text = label; textSize = 14.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.02f
            setTextColor(Palette.BRAND_ON)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Palette.BRAND_DEEP, Palette.BRAND)).apply {
                cornerRadius = dp(ctx, 12).toFloat()
            }
            setPadding(dp(ctx, 18), dp(ctx, 11), dp(ctx, 18), dp(ctx, 11))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(ctx, 14) }
            setOnClickListener { onClick() }
        }

    fun ghostBtn(ctx: Context, label: String, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            text = label; textSize = 13.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Palette.BRAND)
            background = round(ctx, 10, Palette.CARD, stroke = true, strokeColor = Palette.BRAND)
            setPadding(dp(ctx, 16), dp(ctx, 9), dp(ctx, 16), dp(ctx, 9))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = dp(ctx, 8) }
            setOnClickListener { onClick() }
        }

    fun chip(ctx: Context, label: String, color: Int, textColor: Int = Palette.BRAND_ON): TextView =
        TextView(ctx).apply {
            text = label; textSize = 11.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.04f
            setTextColor(textColor)
            background = round(ctx, 8, color)
            setPadding(dp(ctx, 8), dp(ctx, 3), dp(ctx, 8), dp(ctx, 3))
        }

    /**
     * 胶囊开关：48×28 圆角轨道 + 22dp 白色圆钮。tag 存 Boolean 当前值，
     * 读取方式与旧 toggleRow 兼容：`(row.tag as? Boolean) ?: default`。
     */
    fun toggle(ctx: Context, labelText: String, initial: Boolean): LinearLayout {
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(ctx, 12), 0, dp(ctx, 2)); tag = initial
        }
        val lab = text(ctx, labelText, 14f, Palette.PAPER).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val track = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 48), dp(ctx, 28))
        }
        val knob = View(ctx).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL; setColor(Palette.CARD)
            }
            layoutParams = FrameLayout.LayoutParams(dp(ctx, 22), dp(ctx, 22), Gravity.START or Gravity.CENTER_VERTICAL)
                .apply { leftMargin = dp(ctx, 3) }
        }
        fun paint(on: Boolean) {
            track.background = round(ctx, 14, if (on) Palette.BRAND else Palette.LINE)
            (knob.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = (if (on) Gravity.END else Gravity.START) or Gravity.CENTER_VERTICAL
                leftMargin = dp(ctx, 3); rightMargin = dp(ctx, 3)
            }
            knob.requestLayout()
        }
        paint(initial)
        track.setOnClickListener {
            val now = !((row.tag as? Boolean) ?: true); row.tag = now
            paint(now)
        }
        track.addView(knob)
        row.addView(lab); row.addView(track)
        return row
    }

    fun editBox(ctx: Context, value: String, hint: String? = null, multiLine: Int = 1): EditText =
        EditText(ctx).apply {
            setText(value)
            if (hint != null) this.hint = hint
            setHintTextColor(Palette.FAINT)
            setTextColor(Palette.PAPER)
            textSize = 14f
            setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8))
            background = round(ctx, 10, Palette.CARD, stroke = true)
            if (multiLine > 1) {
                setSingleLine(false)
                minLines = multiLine
            } else setSingleLine(true)
        }

    fun divider(ctx: Context): View = View(ctx).apply {
        setBackgroundColor(Palette.LINE)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 1)).apply {
            topMargin = dp(ctx, 6); bottomMargin = dp(ctx, 4)
        }
    }

    fun round(ctx: Context, radius: Int, color: Int, stroke: Boolean = false, strokeColor: Int = Palette.LINE) =
        GradientDrawable().apply {
            cornerRadius = dp(ctx, radius).toFloat(); setColor(color)
            if (stroke) setStroke(dp(ctx, 1), strokeColor)
        }

    /** 两按钮横排（确认/取消），主按钮在右。 */
    fun twoButtons(ctx: Context, okLabel: String, cancelLabel: String, onOk: () -> Unit, onCancel: () -> Unit): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END
            addView(ghostBtn(ctx, cancelLabel, onCancel))
            addView(ghostBtn(ctx, okLabel) { onOk() }.apply {
                background = round(ctx, 10, Palette.BRAND)
                setTextColor(Palette.BRAND_ON)
            })
        }
}
