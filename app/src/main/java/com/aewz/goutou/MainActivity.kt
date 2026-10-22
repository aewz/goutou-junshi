package com.aewz.goutou

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aewz.goutou.core.LastRound
import com.aewz.goutou.core.Prefs
import com.aewz.goutou.ui.Palette
import com.aewz.goutou.ui.Views

/**
 * 主页：顶部（Logo + 模式 + 设置）· 聊天分析主入口 · 三模式快切 ·
 * 最近分析状态 · 底部导航（首页 / 知识库 / 设置）。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var content: LinearLayout
    private val a11yComponent = "com.aewz.goutou/com.aewz.goutou.capture.ChatCaptureService"

    /** 当前深浅模式的色板。 */
    private val P get() = Palette.of(this)

    private fun dp(v: Int) = Views.dp(this, v)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)   // 底部导航固定，内容滚动不遮挡
        }
        content = Views.page(this)
        content.padForSystemBars()
        scroll.addView(content)
        root.addView(scroll)
        // 底部导航
        root.addView(Views.bottomNav(this, listOf(
            Triple("⌂", "首页") { },   // 已在首页
            Triple("▤", "知识库") { startActivity(Intent(this, KnowledgeActivity::class.java)) },
            Triple("⚙", "设置") { startActivity(Intent(this, SettingsActivity::class.java)) }
        ), selected = 0))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun modeName(): String = when (prefs.advisorMode) {
        Prefs.MODE_GOUTOU -> "狗头军师"
        Prefs.MODE_HYBRID -> "混合"
        else -> "经典"
    }

    private fun build() {
        content.removeAllViews()

        // ---- 顶部：Logo + 名称 + 模式 + 设置
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(10))
        }
        head.addView(TextView(this).apply {
            text = "军"; textSize = 20f; gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(P.BRAND_ON)
            background = Views.round(this@MainActivity, 12, P.BRAND)
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
        })
        head.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                .apply { leftMargin = dp(10) }
            addView(Views.rowText(this@MainActivity, "狗头军师", 20f, P.PAPER, bold = true))
            addView(Views.chip(this@MainActivity, modeName(), P.BRAND_DIM, P.BRAND).apply {
                (layoutParams as LinearLayout.LayoutParams).topMargin = dp(4)
            })
        })
        head.addView(Views.ghostBtn(this, "⚙ 设置") {
            startActivity(Intent(this, SettingsActivity::class.java))
        })
        content.addView(head)

        // ---- 主功能入口：聊天分析
        val ready = isA11yEnabled() && Settings.canDrawOverlays(this)
        val entry = Views.card(this, raised = true)
        entry.addView(Views.rowText(this, "聊天分析", 17f, P.PAPER, bold = true))
        entry.addView(Views.rowText(this,
            "在已支持的聊天 App（QQ / X / 飞书 / WhatsApp）里收到新消息时，悬浮窗自动给出判断与候选回复。发送始终由你手动点。",
            12.5f, P.SUB).apply { setPadding(0, dp(4), 0, 0) })
        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, 0)
        }
        statusRow.addView(View(this).apply {
            background = Views.round(this@MainActivity, 4, if (ready) P.OK else P.DANGER)
            layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply { rightMargin = dp(6) }
        })
        statusRow.addView(Views.rowText(this, if (ready) "已就绪" else "未就绪（无障碍/悬浮窗）", 13f, P.SUB))
        entry.addView(statusRow)
        entry.addView(Views.primaryBtn(this, if (ready) "查看最近分析" else "去开启权限") {
            if (ready) startActivity(Intent(this, AnalysisActivity::class.java))
            else startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        content.addView(entry)

        // ---- 三模式快捷切换（切换仅保存偏好，不触发任何 AI 请求）
        content.addView(Views.section(this, "分析模式"))
        val modeRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(
            Triple(Prefs.MODE_CLASSIC, "经典", "判断 + 排序"),
            Triple(Prefs.MODE_GOUTOU, "军师", "人格 + 简析"),
            Triple(Prefs.MODE_HYBRID, "混合", "双引擎对比")
        ).forEachIndexed { i, (m, name, desc) ->
            val on = prefs.advisorMode == m
            val c = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                background = Views.round(this@MainActivity, 12, if (on) P.BRAND_DIM else P.CARD, stroke = true,
                    strokeColor = if (on) P.BRAND else P.LINE)
                setPadding(dp(8), dp(10), dp(8), dp(10))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { rightMargin = if (i < 2) dp(8) else 0 }
                setOnClickListener {
                    prefs.advisorMode = m
                    Toast.makeText(this@MainActivity, "已切换为「$name」模式", Toast.LENGTH_SHORT).show()
                    build()
                }
            }
            c.addView(Views.rowText(this, name, 14f, if (on) P.BRAND else P.PAPER, bold = true))
            c.addView(Views.rowText(this, desc, 11f, P.SUB).apply { setPadding(0, dp(2), 0, 0) })
            modeRow.addView(c)
        }
        content.addView(modeRow)
        content.addView(Views.textBtn(this, "模式详情 ›", P.BRAND) {
            startActivity(Intent(this, ModeActivity::class.java))
        })

        // ---- 最近一次分析状态
        content.addView(Views.section(this, "最近分析"))
        if (LastRound.hasResult) {
            val c = Views.card(this)
            c.setOnClickListener { startActivity(Intent(this, AnalysisActivity::class.java)) }
            val lr = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            }
            lr.addView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                addView(Views.rowText(this@MainActivity, LastRound.title ?: "未知会话", 14.5f, P.PAPER, bold = true))
                addView(Views.rowText(this@MainActivity,
                    "${modeNameOf(LastRound.mode)} · ${LastRound.totalReplies} 条候选", 12f, P.SUB)
                    .apply { setPadding(0, dp(3), 0, 0) })
            })
            lr.addView(Views.rowText(this, "›", 22f, P.BRAND))
            c.addView(lr)
            content.addView(c)
        } else {
            content.addView(Views.card(this).apply {
                addView(Views.rowText(this@MainActivity,
                    "还没有分析记录。打开一个聊天 App，对方来消息后悬浮窗会自动分析；也可以点悬浮球手动「截屏识别一次」。",
                    12.5f, P.SUB))
            })
        }

        // ---- 权限区（未就绪时才完整展示）
        content.addView(Views.section(this, "权限与状态"))
        if (!ready) {
            content.addView(permRow("无障碍权限", "读当前聊天窗口文字", isA11yEnabled()) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            })
            content.addView(permRow("悬浮窗权限", "显示分析卡片", Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            })
            if (!isA11yEnabled() && isSideloaded()) {
                content.addView(permRow("受限设置（Android 13+）", "侧载安装需允许，否则开关会被自动关", false) {
                    runCatching { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }
                })
            }
            content.addView(permRow("电池优化白名单", "省电策略会冻结无障碍服务、开关被系统回收", false) {
                runCatching {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")))
                }
            })
            content.addView(permRow("自启动 + 省电无限制", "小米/HyperOS 必做", false) {
                runCatching { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }
            })
        }
        // 总开关
        val master = Views.primaryBtn(this, if (prefs.enabled) "助手已开启 · 点击关闭" else "助手已关闭 · 点击开启") {
            prefs.enabled = !prefs.enabled
            build()
        }
        content.addView(master)
        content.addView(Views.rowText(this, "读取的内容只发往你自己配置的接口 · 隐私政策", 11f, P.FAINT).apply {
            setPadding(dp(2), dp(8), 0, 0)
            setOnClickListener { openUrl(PRIVACY_URL) }
        })
    }

    private fun permRow(title: String, desc: String, ok: Boolean, onClick: () -> Unit): View {
        val c = Views.card(this)
        c.setOnClickListener { onClick() }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(View(this).apply {
            background = Views.round(this@MainActivity, 4, if (ok) P.OK else P.DANGER)
            layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply { rightMargin = dp(10) }
        })
        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            addView(Views.rowText(this@MainActivity, title, 14.5f, P.PAPER, bold = true))
            addView(Views.rowText(this@MainActivity, desc, 12f, P.SUB))
        })
        row.addView(Views.rowText(this, if (ok) "✓" else "›", 18f, if (ok) P.OK else P.BRAND, bold = true))
        c.addView(row)
        return c
    }

    private fun modeNameOf(m: String?): String = when (m) {
        Prefs.MODE_GOUTOU -> "狗头军师"
        Prefs.MODE_HYBRID -> "混合"
        else -> "经典"
    }

    private fun isA11yEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.contains(a11yComponent)
    }

    private fun isSideloaded(): Boolean {
        val installer = runCatching { packageManager.getInstallerPackageName(packageName) }.getOrNull()
        return installer.isNullOrBlank() || installer == "adb"
    }

    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { Toast.makeText(this, "打不开浏览器", Toast.LENGTH_SHORT).show() }
    }

    companion object {
        private const val PRIVACY_URL = "https://github.com/aewz/goutou-junshi/blob/main/PRIVACY.md"
    }
}
