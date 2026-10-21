package com.aewz.goutou

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.aewz.goutou.ui.Palette
import com.aewz.goutou.ui.Views

/**
 * 军师知识库浏览页：随包打包的参考资料（assets/goutou），本地只读。
 * 列表 + 文件名搜索 + 原文详情（YAML frontmatter 已剥离）。
 */
class GoutouKnowledgeActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private var query = ""
    private var files: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        files = listAssetFiles("").sorted()
        val scroll = ScrollView(this)
        container = Views.page(this)
        container.padForSystemBars()
        scroll.addView(container)
        setContentView(scroll)
        render()
    }

    /** Depth-first walk of assets/goutou; regular files only, paths relative to it. */
    private fun listAssetFiles(dir: String): List<String> {
        val out = ArrayList<String>()
        val root = if (dir.isEmpty()) "goutou" else "goutou/$dir"
        val entries = try { assets.list(root) ?: emptyArray() } catch (e: Exception) { emptyArray() }
        for (e in entries) {
            val path = if (dir.isEmpty()) e else "$dir/$e"
            val sub = try { assets.list("goutou/$path") ?: emptyArray() } catch (ex: Exception) { emptyArray() }
            if (sub.isEmpty()) out.add(path) else out.addAll(listAssetFiles(path))
        }
        return out
    }

    // --------------------------------------------------------------- screens

    private fun render() {
        container.removeAllViews()
        container.addView(Views.title(this, "军师知识库"))
        container.addView(Views.rowText(this,
            "狗头军师的参考资料随包打包，仅本地浏览；每次分析只注入浓缩人格，不引用这里的全文。",
            12f, Palette.SUB).apply { setPadding(0, Views.dp(this@GoutouKnowledgeActivity, 4), 0, 0) })

        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, Views.dp(this@GoutouKnowledgeActivity, 10), 0, 0)
        }
        val search = Views.editBox(this, query, hint = "搜索文件名").apply {
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        searchRow.addView(search)
        searchRow.addView(Views.ghostBtn(this, "搜索") {
            query = search.text.toString().trim()
            render()
        })
        container.addView(searchRow)

        val shown = files.filter { query.isBlank() || it.contains(query, ignoreCase = true) }
        container.addView(Views.rowText(this, "共 ${files.size} 份 · 命中 ${shown.size} 份",
            11f, Palette.FAINT).apply { setPadding(0, Views.dp(this@GoutouKnowledgeActivity, 10), 0, 0) })
        shown.forEach { container.addView(fileRow(it)) }
    }

    private fun fileRow(path: String): View {
        val c = Views.card(this).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = Views.dp(this@GoutouKnowledgeActivity, 8)
            setOnClickListener { openDetail(path) }
        }
        c.addView(Views.rowText(this, displayName(path), 15f, Palette.PAPER, bold = true))
        c.addView(Views.rowText(this, path, 11f, Palette.FAINT).apply {
            setPadding(0, Views.dp(this@GoutouKnowledgeActivity, 3), 0, 0)
        })
        return c
    }

    private fun openDetail(path: String) {
        val content = try {
            assets.open("goutou/$path").bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            "读取失败：${e.message}"
        }
        container.removeAllViews()
        container.addView(Views.rowText(this, "← 返回列表", 14f, Palette.BRAND, bold = true).apply {
            setPadding(0, 0, 0, Views.dp(this@GoutouKnowledgeActivity, 6))
            setOnClickListener { render() }
        })
        container.addView(Views.rowText(this, displayName(path), 20f, Palette.PAPER, bold = true))
        container.addView(Views.rowText(this, path, 11f, Palette.FAINT).apply {
            setPadding(0, Views.dp(this@GoutouKnowledgeActivity, 4), 0, Views.dp(this@GoutouKnowledgeActivity, 10))
        })
        container.addView(Views.rowText(this, stripFrontmatter(content), 14f, Palette.PAPER).apply {
            setLineSpacing(Views.dp(this@GoutouKnowledgeActivity, 3).toFloat(), 1f)
            setTextIsSelectable(true)
        })
    }

    /** SKILL.md and friends open with a YAML block ("---" ... "---"): skip it. */
    private fun stripFrontmatter(raw: String): String {
        val t = raw.trimStart('﻿')
        if (!t.startsWith("---")) return raw
        val end = t.indexOf("\n---", 3)
        return if (end < 0) raw else t.substring(end + 4).trimStart('\n', '\r')
    }

    private fun displayName(path: String): String =
        path.substringAfterLast('/').removeSuffix(".md")
}
