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
 * 统一设计系统组件库 — 所有页面共用（M3 紫系，明暗双模式）。
 * 间距与圆角取自 dims.xml；颜色经 [Palette.of] 按系统深浅色自动切换。
 */
object Views {

    fun dp(ctx: Context, v: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), ctx.resources.displayMetrics).toInt()

    // ------------------------------------------------------------ 页面骨架

    fun page(ctx: Context, padTop: Int = 20): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(ctx, 18), dp(ctx, padTop), dp(ctx, 18), dp(ctx, 28))
            setBackgroundColor(Palette.of(ctx).BG)
        }

    /** 页面大标题（24sp 粗体）。 */
    fun title(ctx: Context, t: String): TextView =
        TextView(ctx).apply {
            text = t; textSize = 24f; setTextColor(Palette.of(ctx).PAPER)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(ctx, 6))
        }

    /** 分区头：紫色竖条 + 小字。 */
    fun section(ctx: Context, t: String): LinearLayout =
        LinearLayout(ctx).apply {
            val P = Palette.of(ctx)
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(ctx, 2), dp(ctx, 16), 0, dp(ctx, 6))
            addView(View(ctx).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(ctx, 2).toFloat(); setColor(P.BRAND)
                }
                layoutParams = LinearLayout.LayoutParams(dp(ctx, 3), dp(ctx, 12))
            })
            addView(TextView(ctx).apply {
                text = t; textSize = 11.5f; setTextColor(P.SUB)
                setTypeface(typeface, Typeface.BOLD)
                letterSpacing = 0.12f
                setPadding(dp(ctx, 8), 0, 0, 0)
            })
        }

    // ------------------------------------------------------------ 卡片

    /** 白卡（深色模式为深卡）：描边 + 柔和投影。 */
    fun card(ctx: Context, raised: Boolean = false): LinearLayout =
        LinearLayout(ctx).apply {
            val P = Palette.of(ctx)
            orientation = LinearLayout.VERTICAL
            background = round(ctx, 16, if (raised) P.CARD_RAISED else P.CARD, stroke = true)
            elevation = dp(ctx, if (raised) 2 else 1).toFloat()
            setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12))
        }

    // ------------------------------------------------------------ 文本

    fun text(ctx: Context, t: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(ctx).apply {
            text = t; setTextColor(color); textSize = size
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    fun rowText(ctx: Context, t: String, size: Float = 14f, color: Int = Palette.of(ctx).PAPER, bold: Boolean = false) =
        text(ctx, t, size, color, bold)

    fun hintText(ctx: Context, t: String) = text(ctx, t, 12f, Palette.of(ctx).SUB)

    /** 输入区大标签。 */
    fun label(ctx: Context, t: String) = text(ctx, t, 13f, Palette.of(ctx).BRAND, bold = true).apply {
        setPadding(0, dp(ctx, 12), 0, dp(ctx, 4))
        letterSpacing = 0.04f
    }

    // ------------------------------------------------------------ 按钮（M3 四档）

    /** 实心主按钮。 */
    fun primaryBtn(ctx: Context, label: String, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            val P = Palette.of(ctx)
            text = label; textSize = 14.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(P.BRAND_ON)
            background = round(ctx, 12, P.BRAND)
            setPadding(dp(ctx, 18), dp(ctx, 11), dp(ctx, 18), dp(ctx, 11))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(ctx, 14) }
            setOnClickListener { onClick() }
        }

    /** 浅底主色按钮（次级强调）。 */
    fun tonalBtn(ctx: Context, label: String, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            val P = Palette.of(ctx)
            text = label; textSize = 13.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(P.BRAND)
            background = round(ctx, 10, P.BRAND_DIM)
            setPadding(dp(ctx, 16), dp(ctx, 9), dp(ctx, 16), dp(ctx, 9))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = dp(ctx, 8) }
            setOnClickListener { onClick() }
        }

    /** 描边按钮。 */
    fun ghostBtn(ctx: Context, label: String, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            val P = Palette.of(ctx)
            text = label; textSize = 13.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(P.BRAND)
            background = round(ctx, 10, P.CARD, stroke = true, strokeColor = P.BRAND)
            setPadding(dp(ctx, 16), dp(ctx, 9), dp(ctx, 16), dp(ctx, 9))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = dp(ctx, 8) }
            setOnClickListener { onClick() }
        }

    /** 文字按钮（危险操作等）。 */
    fun textBtn(ctx: Context, label: String, color: Int, onClick: () -> Unit): TextView =
        TextView(ctx).apply {
            text = label; textSize = 13.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(color)
            setPadding(dp(ctx, 16), dp(ctx, 9), dp(ctx, 16), dp(ctx, 9))
            setOnClickListener { onClick() }
        }

    // ------------------------------------------------------------ 小组件

    fun chip(ctx: Context, label: String, color: Int, textColor: Int = Palette.of(ctx).BRAND_ON): TextView =
        TextView(ctx).apply {
            text = label; textSize = 11.5f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.04f
            setTextColor(textColor)
            background = round(ctx, 8, color)
            setPadding(dp(ctx, 8), dp(ctx, 3), dp(ctx, 8), dp(ctx, 3))
        }

    /** 胶囊开关；tag 存 Boolean。 */
    fun toggle(ctx: Context, labelText: String, initial: Boolean): LinearLayout {
        val P = Palette.of(ctx)
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(ctx, 12), 0, dp(ctx, 2)); tag = initial
        }
        val lab = text(ctx, labelText, 14f, P.PAPER).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val track = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 48), dp(ctx, 28))
        }
        val knob = View(ctx).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL; setColor(P.CARD)
            }
            layoutParams = FrameLayout.LayoutParams(dp(ctx, 22), dp(ctx, 22), Gravity.START or Gravity.CENTER_VERTICAL)
                .apply { leftMargin = dp(ctx, 3) }
        }
        fun paint(on: Boolean) {
            track.background = round(ctx, 14, if (on) P.BRAND else P.LINE)
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
            val P = Palette.of(ctx)
            setText(value)
            if (hint != null) this.hint = hint
            setHintTextColor(P.FAINT)
            setTextColor(P.PAPER)
            textSize = 14f
            setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8))
            background = round(ctx, 10, P.CARD, stroke = true)
            if (multiLine > 1) { setSingleLine(false); minLines = multiLine } else setSingleLine(true)
        }

    fun divider(ctx: Context): View = View(ctx).apply {
        setBackgroundColor(Palette.of(ctx).LINE)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 1)).apply {
            topMargin = dp(ctx, 6); bottomMargin = dp(ctx, 4)
        }
    }

    fun round(ctx: Context, radius: Int, color: Int, stroke: Boolean = false, strokeColor: Int = Palette.of(ctx).LINE) =
        GradientDrawable().apply {
            cornerRadius = dp(ctx, radius).toFloat(); setColor(color)
            if (stroke) setStroke(dp(ctx, 1), strokeColor)
        }

    // ------------------------------------------------------------ 状态态组件

    /** 加载态：转圈文案。 */
    fun loadingState(ctx: Context, label: String): TextView =
        text(ctx, "◌ $label", 14f, Palette.of(ctx).SUB, bold = true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(ctx, 32), 0, dp(ctx, 32))
        }

    /** 空态：图标 + 标题 + 一句引导。 */
    fun emptyState(ctx: Context, title: String, guide: String): LinearLayout {
        val P = Palette.of(ctx)
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(ctx, 24), dp(ctx, 40), dp(ctx, 24), dp(ctx, 40))
            addView(text(ctx, "○", 40f, P.FAINT))
            addView(text(ctx, title, 16f, P.PAPER, bold = true).apply {
                setPadding(0, dp(ctx, 10), 0, dp(ctx, 4)); gravity = Gravity.CENTER
            })
            addView(text(ctx, guide, 12.5f, P.SUB).apply { gravity = Gravity.CENTER })
        }
    }

    /** 错误态：原因 + 重试按钮。 */
    fun errorState(ctx: Context, reason: String, retry: (() -> Unit)? = null): LinearLayout {
        val P = Palette.of(ctx)
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(ctx, 24), dp(ctx, 40), dp(ctx, 24), dp(ctx, 40))
            addView(text(ctx, "⚠", 32f, P.DANGER))
            addView(text(ctx, reason, 14f, P.PAPER, bold = true).apply {
                setPadding(0, dp(ctx, 10), 0, 0); gravity = Gravity.CENTER
            })
            if (retry != null) addView(tonalBtn(ctx, "重试") { retry() }.apply {
                (layoutParams as LinearLayout.LayoutParams).topMargin = dp(ctx, 16)
            })
        }
    }

    /** 底部导航条：等宽三项，选中紫。 */
    fun bottomNav(ctx: Context, items: List<Triple<String, String, () -> Unit>>, selected: Int): LinearLayout {
        val P = Palette.of(ctx)
        val bar = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            background = round(ctx, 0, P.CARD, stroke = true, strokeColor = P.LINE)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 60))
        }
        items.forEachIndexed { i, (icon, name, onClick) ->
            val item = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener { onClick() }
            }
            item.addView(text(ctx, icon, 17f, if (i == selected) P.BRAND else P.FAINT, bold = i == selected))
            item.addView(text(ctx, name, 11f, if (i == selected) P.BRAND else P.SUB, bold = i == selected))
            bar.addView(item)
        }
        return bar
    }
}
