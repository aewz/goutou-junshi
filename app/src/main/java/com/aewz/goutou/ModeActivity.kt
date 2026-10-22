package com.aewz.goutou

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aewz.goutou.core.Prefs
import com.aewz.goutou.ui.Palette
import com.aewz.goutou.ui.Views

/**
 * 三模式选择页：经典 / 狗头军师 / 混合。
 * 选择仅保存偏好、绝不触发 AI 请求，也不清除任何已有内容。
 */
class ModeActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var container: LinearLayout

    /** 当前深浅模式的色板。 */
    private val P get() = Palette.of(this)

    private fun dp(v: Int) = Views.dp(this, v)

    private data class ModeInfo(
        val value: String, val name: String, val desc: String,
        val depends: String, val accent: Int
    )

    private fun modes(): List<ModeInfo> = listOf(
        ModeInfo(Prefs.MODE_CLASSIC, "经典", "判断引擎先分析对方意图与风险，再起草并排序候选回复。信息最全，需配置判断接口。",
            "判断 + 回复接口", 0xFF3F74C9.toInt()),
        ModeInfo(Prefs.MODE_GOUTOU, "狗头军师", "军师人格先给一句情绪落地式简析，再出三条可直接发送的话术，第一条即首选。只需回复接口。",
            "仅回复接口", 0xFF6C4CF1.toInt()),
        ModeInfo(Prefs.MODE_HYBRID, "混合", "判断引擎与军师同台：两组候选并排对比，由你挑最合适的一条。判断密钥未配时自动降级。",
            "判断或回复接口", 0xFFC8892B.toInt())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        val scroll = ScrollView(this)
        container = Views.page(this)
        container.padForSystemBars()
        scroll.addView(container)
        setContentView(scroll)
        render()
    }

    private fun render() {
        container.removeAllViews()
        container.addView(Views.rowText(this, "← 返回", 14f, P.BRAND, bold = true).apply {
            setPadding(0, 0, 0, dp(6))
            setOnClickListener { finish() }
        })
        container.addView(Views.title(this, "分析模式"))
        container.addView(Views.rowText(this,
            "选择只保存偏好，不会触发任何分析请求，也不影响已有内容。切换后下一轮分析立即生效。",
            12f, P.SUB).apply { setPadding(0, dp(2), 0, dp(6)) })

        modes().forEach { m ->
            val on = prefs.advisorMode == m.value
            val c = Views.card(this, raised = on).apply {
                (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(10)
                setOnClickListener {
                    prefs.advisorMode = m.value
                    Toast.makeText(this@ModeActivity, "已选择「${m.name}」", Toast.LENGTH_SHORT).show()
                    render()
                }
            }
            // 头行：名称 + 选中标
            val head = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            }
            head.addView(Views.chip(this, m.name, if (on) m.accent else P.CARD_RAISED,
                if (on) P.BRAND_ON else P.SUB))
            head.addView(LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            head.addView(Views.rowText(this, if (on) "● 已选" else "○ 未选", 12f,
                if (on) m.accent else P.FAINT, bold = on))
            c.addView(head)
            c.addView(Views.rowText(this, m.desc, 13f, P.SUB).apply { setPadding(0, dp(8), 0, 0) })
            c.addView(Views.rowText(this, "依赖：${m.depends}", 11.5f, P.FAINT).apply {
                setPadding(0, dp(6), 0, 0)
            })
            container.addView(c)
        }

        container.addView(Views.primaryBtn(this, "进入分析") {
            startActivity(Intent(this, AnalysisActivity::class.java))
        })
    }
}
