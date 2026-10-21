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
import com.aewz.goutou.core.Prefs
import com.aewz.goutou.ui.Palette
import com.aewz.goutou.ui.Views

/**
 * 主页 · 「军帐夜谈」风格：深色军帐底、发丝线描边卡、紫色主操作。
 * 内容：就绪状态卡、权限清单、设置入口、总开关。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var container: LinearLayout
    private val a11yComponent =
        "com.aewz.goutou/com.aewz.goutou.capture.ChatCaptureService"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val scroll = ScrollView(this)
        container = Views.page(this)
        container.padForSystemBars()   // edge-to-edge: keep the title off the status bar
        scroll.addView(container)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun dp(v: Int) = Views.dp(this, v)

    private fun modeName(): String = when (prefs.advisorMode) {
        Prefs.MODE_GOUTOU -> "狗头军师"
        Prefs.MODE_HYBRID -> "混合"
        else -> "经典"
    }

    private fun build() {
        container.removeAllViews()

        // 渐变头卡：品牌区（紫渐变 + 白字）
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_hero)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(4) }
        }
        hero.addView(TextView(this).apply {
            text = "狗头军师"; textSize = 26f; setTextColor(android.graphics.Color.WHITE)
            setTypeface(typeface, Typeface.BOLD); letterSpacing = 0.02f
        })
        hero.addView(Views.chip(this, "当前模式 · " + modeName(),
            0x33FFFFFF, android.graphics.Color.WHITE).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(8)
        })
        hero.addView(Views.rowText(this,
            "在聊天 App 旁读对方消息（已支持 QQ、X、飞书、WhatsApp），给出判断和候选回复。发送始终由你手动点。",
            12.5f, android.graphics.Color.WHITE).apply {
            alpha = 0.92f
            setPadding(0, dp(10), 0, 0)
        })
        container.addView(hero)

        val a11y = isA11yEnabled()
        val overlay = Settings.canDrawOverlays(this)
        val key = prefs.hasKey()   // judge route key: the one analysis cannot run without
        val ready = a11y && overlay && key

        // 就绪状态
        container.addView(statusCard(ready, a11y, overlay, key))
        container.addView(privacyHint())
        container.addView(platformRiskHint())

        // 权限清单
        container.addView(Views.section(this, "权限设置"))
        container.addView(permCard("无障碍权限", "读取当前聊天窗口的消息文字", a11y) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        container.addView(permCard("悬浮窗权限", "在聊天窗口上方显示分析卡片", overlay) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        container.addView(permCard("自启动 + 省电无限制", "小米/HyperOS 必做，否则服务被冻结、读不到消息", null) {
            runCatching {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
            }
        })

        // 其他
        container.addView(Views.section(this, "其他"))
        container.addView(actionRow("设置", "密钥 · 模型 · 关系 · 透明度 · 会话白名单") {
            startActivity(Intent(this, SettingsActivity::class.java))
        })

        // 总开关
        val toggle = TextView(this).apply {
            text = if (prefs.enabled) "助手已开启 · 点击关闭" else "助手已关闭 · 点击开启"
            textSize = 15f; gravity = Gravity.CENTER; setTypeface(typeface, Typeface.BOLD)
            setTextColor(if (prefs.enabled) Palette.BRAND_ON else Palette.BRAND)
            background = Views.round(this@MainActivity, 12, if (prefs.enabled) Palette.BRAND else Palette.BRAND_DIM,
                stroke = !prefs.enabled)
            setPadding(dp(16), dp(15), dp(16), dp(15))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(18) }
        }
        toggle.setOnClickListener {
            prefs.enabled = !prefs.enabled
            build()
        }
        container.addView(toggle)
    }

    // ---------------------------------------------------------------- cards

    private fun statusCard(ready: Boolean, a11y: Boolean, overlay: Boolean, key: Boolean): View {
        val c = Views.card(this, raised = true).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(6)
        }
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(View(this).apply {
            background = Views.round(this@MainActivity, 5, if (ready) Palette.OK else Palette.DANGER)
            layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply { rightMargin = dp(10) }
        })
        head.addView(Views.rowText(this, if (ready) "已就绪，可以用了" else "尚未就绪", 16f,
            if (ready) Palette.OK else Palette.PAPER, bold = true))
        c.addView(head)
        c.addView(checkLine("无障碍", a11y))
        c.addView(checkLine("悬浮窗", overlay))
        c.addView(checkLine("密钥", key, okWord = "已设", noWord = "未设"))
        // History recording is opt-in (off by default). Mention it here, never block on it.
        if (!prefs.contextEnabled) {
            c.addView(Views.hintText(this, "关联上下文未开启，可在设置里开启").apply {
                setPadding(0, dp(8), 0, 0)
            })
        }
        return c
    }

    /** One tappable line under the readiness card, opening the privacy policy page. */
    private fun privacyHint(): View = Views.text(this, "读取的聊天内容只发往你自己配置的接口 · 隐私政策", 11f, Palette.FAINT).apply {
        setPadding(dp(2), dp(8), 0, 0)
        setOnClickListener { openUrl(PRIVACY_URL) }
    }

    /** One line under the privacy hint: using it inside third-party chat apps is at the user's own risk. */
    private fun platformRiskHint(): View =
        Views.text(this, "在第三方聊天 App 里使用可能不符合其用户协议，账号有被限制的风险，请自行判断", 11f, Palette.FAINT).apply {
            setPadding(dp(2), dp(4), 0, 0)
        }

    /** Opens an external link; swallows the failure with a toast rather than crashing. */
    private fun openUrl(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure {
            Toast.makeText(this, "打不开浏览器", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLine(label: String, ok: Boolean, okWord: String = "已开", noWord: String = "未开"): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(5), 0, 0)
        }
        row.addView(Views.text(this, if (ok) "✓" else "✗", 14f, if (ok) Palette.OK else Palette.DANGER, bold = true).apply {
            width = dp(22)
        })
        row.addView(Views.text(this, label + (if (ok) okWord else noWord), 13f, Palette.SUB))
        return row
    }

    private fun permCard(title: String, desc: String, granted: Boolean?, onClick: () -> Unit): View {
        val c = Views.card(this).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(8)
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        left.addView(Views.rowText(this, title, 15f, Palette.PAPER, bold = true))
        left.addView(Views.rowText(this, desc, 12f, Palette.SUB).apply { setPadding(0, dp(3), 0, 0) })
        if (granted == true) left.addView(Views.rowText(this, "✓ 已开启", 12f, Palette.OK, bold = true).apply { setPadding(0, dp(4), 0, 0) })
        row.addView(left)
        val b = Views.ghostBtn(this, if (granted == true) "已开启" else "去开启") {
            if (granted != true) onClick()
        }
        if (granted == true) b.setTextColor(Palette.SUB)
        row.addView(b)
        c.addView(row)
        return c
    }

    private fun actionRow(title: String, desc: String, onClick: () -> Unit): View {
        val c = Views.card(this).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(8)
            setOnClickListener { onClick() }
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        left.addView(Views.rowText(this, title, 15f, Palette.PAPER, bold = true))
        left.addView(Views.rowText(this, desc, 12f, Palette.SUB).apply { setPadding(0, dp(3), 0, 0) })
        row.addView(left)
        row.addView(Views.text(this, "›", 22f, Palette.BRAND))
        c.addView(row)
        return c
    }

    private fun isA11yEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.contains(a11yComponent)
    }

    companion object {
        private const val PRIVACY_URL = "https://github.com/aewz/goutou-junshi/blob/main/PRIVACY.md"
    }
}
