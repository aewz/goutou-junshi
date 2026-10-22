package com.aewz.goutou

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aewz.goutou.capture.ChatCaptureService
import com.aewz.goutou.core.Analysis
import com.aewz.goutou.core.LastRound
import com.aewz.goutou.core.Prefs
import com.aewz.goutou.core.RankedReply
import com.aewz.goutou.ui.Palette
import com.aewz.goutou.ui.Views

/**
 * 聊天分析结果页：最近一轮分析的完整视图。
 * 数据来自 [LastRound]（服务每轮写入）；填入/重新生成/取消经服务桥。
 * 默认每组件显示前 2 条候选，可展开全部；长文本随内容撑开、不截断。
 */
class AnalysisActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private var expanded = false
    private var factsExpanded = false

    /** 当前深浅模式的色板。 */
    private val P get() = Palette.of(this)

    private fun dp(v: Int) = Views.dp(this, v)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        container = Views.page(this)
        container.padForSystemBars()
        scroll.addView(container)
        setContentView(scroll)
        render()
    }

    override fun onResume() {
        super.onResume()
        render()   // 回到页面时刷新（可能在等结果）
    }

    private fun render() {
        container.removeAllViews()

        // 顶部：返回 + 标题 + 关闭
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(10))
        }
        top.addView(Views.rowText(this, "←", 20f, P.BRAND, bold = true).apply {
            setOnClickListener { finish() }
        })
        top.addView(Views.rowText(this, "聊天分析结果", 20f, P.PAPER, bold = true).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                .apply { leftMargin = dp(12) }
        })
        top.addView(Views.rowText(this, "✕", 18f, P.SUB).apply {
            setOnClickListener { finish() }
        })
        container.addView(top)

        if (LastRound.waiting) {
            container.addView(Views.loadingState(this, "正在分析中…稍候片刻，结果会自动出现"))
            container.addView(Views.textBtn(this, "取消分析", P.DANGER) {
                ChatCaptureService.instance?.cancelFromPage()
                render()   // cancelFromPage 会复位 waiting，立即刷新
            })
            // 1s 轮询：分析落地后自动刷新，不依赖任何外部通知
            container.postDelayed({ if (LastRound.waiting) render() }, 1000)
            return
        }
        if (!LastRound.hasResult) {
            container.addView(Views.emptyState(this, "还没有分析结果",
                "打开一个聊天 App（QQ / X / 飞书 / WhatsApp），对方来消息后悬浮窗会自动分析。也可以点悬浮球手动「截屏识别一次」。"))
            return
        }

        // 来源与模式
        val meta = Views.card(this, raised = true)
        meta.addView(Views.rowText(this, LastRound.title ?: "未知会话", 16f, P.PAPER, bold = true))
        val metaRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, 0)
        }
        metaRow.addView(Views.chip(this, modeName(LastRound.mode), modeAccent(LastRound.mode), P.BRAND_ON))
        metaRow.addView(Views.rowText(this, "  ·  " + appLabel(LastRound.app), 12f, P.SUB))
        meta.addView(metaRow)
        container.addView(meta)

        // 分析区
        val mode = LastRound.mode
        if (mode == Prefs.MODE_GOUTOU) {
            if (!LastRound.advisorNote.isNullOrBlank()) {
                val c = Views.card(this)
                c.addView(Views.rowText(this, "军师简析", 13f, P.BRAND, bold = true))
                c.addView(Views.rowText(this, LastRound.advisorNote!!, 14f, P.PAPER).apply {
                    setPadding(0, dp(6), 0, 0); setLineSpacing(dp(2).toFloat(), 1f)
                })
                container.addView(c)
            }
        } else {
            LastRound.judgment?.let { j ->
                container.addView(judgmentCard(j))
            }
        }
        LastRound.analysisError?.let {
            container.addView(Views.card(this).apply {
                addView(Views.rowText(this@AnalysisActivity, it, 13f, P.DANGER, bold = true))
            })
        }

        // 对话事实（默认 4 条，可展开）
        val snap = LastRound.snapshot
        if (snap != null && snap.messages.isNotEmpty()) {
            val c = Views.card(this)
            c.addView(Views.rowText(this, "对话事实（最近 ${snap.messages.size} 条）", 13f, P.BRAND, bold = true))
            val show = if (factsExpanded) snap.messages.size else minOf(4, snap.messages.size)
            snap.messages.takeLast(show).forEach { m ->
                c.addView(Views.rowText(this,
                    (if (m.side == "me") "我：" else "对方：") + m.text,
                    12.5f, P.SUB).apply { setPadding(0, dp(5), 0, 0) })
            }
            if (snap.messages.size > 4) {
                c.addView(Views.textBtn(this, if (factsExpanded) "收起" else "展开全部", P.BRAND) {
                    factsExpanded = !factsExpanded
                    render()
                })
            }
            container.addView(c)
        }

        // 回复区（经典 / 军师两组；每组默认 2 条，可展开）
        container.addView(Views.section(this, "候选回复"))
        if (LastRound.replies.isEmpty()) {
            container.addView(Views.card(this).apply {
                addView(Views.rowText(this@AnalysisActivity,
                    LastRound.replyError ?: "（未生成候选回复）", 13f, P.DANGER))
            })
        } else {
            val classic = LastRound.replies.filter { it.source != "goutou" }
            val goutou = LastRound.replies.filter { it.source == "goutou" }
            val dual = classic.isNotEmpty() && goutou.isNotEmpty()
            if (classic.isNotEmpty()) {
                container.addView(groupChip("经典" + (if (classic.any { it.prob > 0.0 }) " · 引擎排序" else ""), P.INFO_BLUE))
                classic.take(if (expanded) classic.size else minOf(2, classic.size)).forEachIndexed { i, r ->
                    container.addView(replyCard(i, r, i == 0))
                }
            }
            if (goutou.isNotEmpty()) {
                container.addView(groupChip("狗头军师", P.BRAND))
                goutou.take(if (expanded) goutou.size else minOf(2, goutou.size)).forEachIndexed { i, r ->
                    container.addView(replyCard(i, r, i == 0))
                }
            }
            val total = LastRound.totalReplies
            if (total > (if (dual) 4 else 2)) {
                container.addView(Views.tonalBtn(this, if (expanded) "收起回复" else "展开全部 $total 条") {
                    expanded = !expanded
                    render()
                })
            }
            if (LastRound.replyError != null) {
                container.addView(Views.rowText(this, LastRound.replyError!!, 12f, P.DANGER).apply {
                    setPadding(0, dp(8), 0, 0)
                })
            }
        }

        // 操作区
        val ops = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        }
        ops.addView(Views.tonalBtn(this, "↻ 重新生成") {
            val ok = ChatCaptureService.instance?.regenerateFromPage() == true
            Toast.makeText(this,
                if (ok) "已请求重新生成，结果稍后出现在悬浮窗与本页"
                else "当前不在刚才的聊天会话里，无法重新生成",
                Toast.LENGTH_SHORT).show()
        })
        ops.addView(Views.ghostBtn(this, "关闭") { finish() })
        container.addView(ops)
    }

    private fun judgmentCard(j: Analysis): View {
        val c = Views.card(this)
        c.addView(Views.rowText(this, "判断引擎分析", 13f, P.INFO_BLUE, bold = true))
        j.trueIntent?.let {
            val labels = mapOf(
                "confirm_you_care" to "确认你在不在乎", "vent_anger" to "在发泄情绪",
                "request_action" to "要你办事", "seek_explanation" to "要个解释",
                "casual_chat" to "随便聊聊", "close_topic" to "事情过去了")
            c.addView(Views.rowText(this, "对方真实意图：${labels[it.choice] ?: it.choice}",
                14f, P.PAPER, bold = true).apply { setPadding(0, dp(6), 0, 0) })
            c.addView(Views.rowText(this, "把握 ${(it.confidence * 100).toInt()}%", 12f, P.SUB))
        }
        val bits = ArrayList<String>()
        val needs = mapOf("apology" to "道歉", "action" to "具体行动", "explanation" to "解释",
            "care" to "你的在乎", "nothing" to "（不用做什么）")
        val actions = mapOf("check_history" to "翻聊天记录", "apologize" to "先道歉",
            "give_commitment" to "给承诺", "explain" to "解释清楚", "acknowledge" to "接住情绪",
            "say_less" to "少说两句", "make_plan" to "定个安排")
        j.sheNeeds?.let { bits.add("对方要：${needs[it.choice] ?: it.choice}") }
        j.bestAction?.let { bits.add("建议动作：${actions[it.choice] ?: it.choice}") }
        j.shouldReplyNow?.let { bits.add(if (it >= 0.5) "可以给实质回应" else "先别给实质") }
        j.dangerLevel?.let { bits.add("风险 ${it.score.toInt()}/${it.maxLevel}") }
        if (bits.isNotEmpty()) {
            c.addView(Views.rowText(this, bits.joinToString("  ·  "), 13f, P.SUB).apply {
                setPadding(0, dp(6), 0, 0)
            })
        }
        return c
    }

    private fun groupChip(label: String, color: Int): View =
        Views.chip(this, label, color, P.BRAND_ON).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(8)
        }

    /** 候选卡：长文本随内容撑开；首条带「推荐」标；复制/填入按钮。 */
    private fun replyCard(idx: Int, r: RankedReply, recommended: Boolean): View {
        val c = Views.card(this)
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        }
        head.addView(Views.rowText(this, "候选 ${idx + 1}", 12f, P.FAINT, bold = true))
        if (recommended) {
            head.addView(Views.chip(this, "推荐", P.GOLD_DIM, P.GOLD).apply {
                (layoutParams as LinearLayout.LayoutParams).leftMargin = dp(6)
            })
        }
        if (r.prob > 0.0 && r.source != "goutou") {
            head.addView(Views.rowText(this, "  ·  把握 ${(r.prob * 100).toInt()}%", 11.5f, P.GOLD).apply {
                (layoutParams as LinearLayout.LayoutParams).leftMargin = dp(4)
            })
        }
        c.addView(head)
        c.addView(Views.rowText(this, r.text, 15f, P.PAPER).apply {
            setPadding(0, dp(6), 0, dp(4))
            setLineSpacing(dp(2).toFloat(), 1f)
            setTextIsSelectable(true)
        })
        val btns = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END
        }
        btns.addView(Views.ghostBtn(this, "复制") { copy(r.text) })
        btns.addView(Views.tonalBtn(this, "填入聊天") {
            val svc = ChatCaptureService.instance
            if (svc == null) {
                Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
            } else svc.fillFromPage(r.text)
        })
        c.addView(btns)
        return c
    }

    private fun copy(text: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("goutou_reply", text))
        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
    }

    private fun modeName(m: String?): String = when (m) {
        Prefs.MODE_GOUTOU -> "狗头军师"
        Prefs.MODE_HYBRID -> "混合"
        else -> "经典"
    }

    private fun modeAccent(m: String?): Int = when (m) {
        Prefs.MODE_GOUTOU -> P.BRAND
        Prefs.MODE_HYBRID -> P.GOLD
        else -> P.INFO_BLUE
    }

    private fun appLabel(pkg: String?): String = when (pkg) {
        "com.tencent.mobileqq" -> "QQ"
        "com.ss.android.lark" -> "飞书"
        "com.twitter.android" -> "X"
        "com.whatsapp" -> "WhatsApp"
        null -> "未知来源"
        else -> pkg!!
    }
}
