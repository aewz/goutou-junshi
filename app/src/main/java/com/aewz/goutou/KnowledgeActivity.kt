package com.aewz.goutou

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aewz.goutou.core.kb.Contact
import com.aewz.goutou.core.kb.KbStore
import com.aewz.goutou.core.kb.Note
import com.aewz.goutou.ui.Palette
import com.aewz.goutou.ui.Views

/**
 * 知识库（三合一）：笔记 / 联系人 / 军师资料。
 * 军师资料 = 随包打包的参考资料（assets/goutou），分类浏览 + 搜索 + 详情预览。
 * 数据全部本机（filesDir/kb 与 assets），不上传。
 */
class KnowledgeActivity : AppCompatActivity() {

    private lateinit var store: KbStore
    private lateinit var container: LinearLayout

    /** 0 = 笔记, 1 = 联系人, 2 = 军师资料. */
    private var tab = 0
    private var query = ""
    private var category = ""          // "" 全部 / "knowledge" / "practical"
    private var files: List<String> = emptyList()
    private var detailPath: String? = null

    /** 当前深浅模式的色板。 */
    private val P get() = Palette.of(this)

    private fun dp(v: Int) = Views.dp(this, v)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = KbStore.get(this)
        files = listAssetFiles("").sorted()
        val scroll = ScrollView(this)
        container = Views.page(this)
        container.padForSystemBars()
        scroll.addView(container)
        setContentView(scroll)
        render()
    }

    // --------------------------------------------------------------- screens

    private fun render() {
        container.removeAllViews()
        container.addView(Views.title(this, "知识库"))
        container.addView(Views.rowText(this, "只存在本机，不上传。分析时按会话标题匹配联系人、按关键词命中笔记。",
            12f, P.SUB).apply { setPadding(0, dp(4), 0, dp(4)) })
        container.addView(tabs())
        when (tab) {
            0 -> renderNotes()
            1 -> renderContacts()
            else -> renderAdvisorLib()
        }
    }

    private fun tabs(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) }
        }
        listOf("笔记", "联系人", "军师资料").forEachIndexed { i, name ->
            val t = Views.text(this, name, 13f,
                if (i == tab) P.BRAND else P.SUB, bold = i == tab).apply {
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(8), dp(16), dp(8))
                background = Views.round(this@KnowledgeActivity, 9,
                    if (i == tab) P.BRAND_DIM else P.CARD, stroke = i == tab)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT).apply { rightMargin = dp(8) }
                setOnClickListener { tab = i; query = ""; detailPath = null; render() }
            }
            row.addView(t)
        }
        return row
    }

    // ----------------------------------------------------------------- notes

    private fun renderNotes() {
        val notes = store.notes().sortedByDescending { it.updatedAt }
        container.addView(twoButtons("新建笔记", { editNoteDialog(null) },
            "从文本导入", { importNotesDialog() }))
        if (notes.isEmpty()) {
            container.addView(Views.emptyState(this, "还没有笔记",
                "写点该记住的事实：习惯、忌口、项目代号、约定过的时间。分析时命中即自动带上。"))
            return
        }
        notes.forEach { container.addView(noteRow(it)) }
        container.addView(Views.hintText(this,
            "点条目编辑，长按删除。命中规则：任一标签或标题出现在会话标题或最近 6 条消息里。")
            .apply { setPadding(dp(2), dp(12), 0, 0) })
    }

    private fun noteRow(n: Note): View {
        val c = Views.card(this).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(10)
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        }
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val head = n.title.ifBlank { "（无标题）" } + if (n.alwaysOn) "  · 常驻" else ""
        left.addView(Views.rowText(this, head, 15f, P.PAPER, bold = true))
        left.addView(Views.rowText(this,
            if (n.tags.isEmpty()) "无标签" else "标签：" + n.tags.joinToString("、"),
            12f, P.SUB).apply { setPadding(0, dp(3), 0, 0) })
        left.addView(Views.rowText(this, n.content.replace("\n", " ").take(46), 12f, P.SUB)
            .apply { setPadding(0, dp(3), 0, 0) })
        row.addView(left)
        row.addView(smallToggle(n.enabled) {
            store.saveNote(n.copy(enabled = !n.enabled)); render()
        })
        c.addView(row)
        c.setOnClickListener { editNoteDialog(n) }
        c.setOnLongClickListener {
            confirm("删除笔记", "删除「${n.title}」？不可恢复。") {
                store.deleteNote(n.id); render()
            }
            true
        }
        return c
    }

    private fun editNoteDialog(existing: Note?) {
        val box = dialogBox()
        val titleEdit = Views.editBox(this, existing?.title ?: "", hint = "标题，例如：口味忌口")
        val contentEdit = Views.editBox(this, existing?.content ?: "", hint = "正文，写清楚事实本身", multiLine = 4).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            gravity = Gravity.TOP
        }
        val tagsEdit = Views.editBox(this, existing?.tags?.joinToString("，") ?: "", hint = "逗号分隔，例如：吃饭，周末")
        val alwaysRow = Views.toggle(this, "常驻（每次分析都带上）", existing?.alwaysOn ?: false)
        val enabledRow = Views.toggle(this, "启用", existing?.enabled ?: true)
        box.addView(Views.label(this, "标题")); box.addView(titleEdit)
        box.addView(Views.label(this, "正文")); box.addView(contentEdit)
        box.addView(Views.label(this, "标签")); box.addView(tagsEdit)
        box.addView(alwaysRow); box.addView(enabledRow)

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "新建笔记" else "编辑笔记")
            .setView(wrapScroll(box))
            .setPositiveButton("保存") { _, _ ->
                val title = titleEdit.text.toString().trim()
                val content = contentEdit.text.toString().trim()
                if (title.isBlank() && content.isBlank()) {
                    toast("标题和正文不能都空着"); return@setPositiveButton
                }
                store.saveNote(Note(
                    id = existing?.id ?: KbStore.newId(),
                    title = title, content = content,
                    tags = splitTags(tagsEdit.text.toString()),
                    alwaysOn = (alwaysRow.tag as? Boolean) ?: false,
                    enabled = (enabledRow.tag as? Boolean) ?: true
                ))
                render()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun importNotesDialog() {
        val box = dialogBox()
        box.addView(Views.hintText(this, "按空行分段，每段第一行当标题，其余当正文。"))
        val input = Views.editBox(this, "", hint = "口味忌口\n不吃香菜，海鲜过敏\n\n项目代号\n内部叫小蓝", multiLine = 8).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            gravity = Gravity.TOP
        }
        box.addView(input)
        AlertDialog.Builder(this)
            .setTitle("从文本导入")
            .setView(wrapScroll(box))
            .setPositiveButton("导入") { _, _ ->
                val chunks = input.text.toString().split(Regex("\\r?\\n[ \\t]*\\r?\\n"))
                var n = 0
                chunks.forEach { chunk ->
                    val lines = chunk.trim().split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    if (lines.isNotEmpty()) {
                        store.saveNote(Note(
                            id = KbStore.newId(),
                            title = lines.first(),
                            content = lines.drop(1).joinToString("\n")
                        ))
                        n++
                    }
                }
                toast(if (n == 0) "没解析出内容" else "已导入 $n 条")
                render()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun splitTags(raw: String): List<String> =
        raw.split(",", "，", "、").map { it.trim() }.filter { it.isNotEmpty() }

    // -------------------------------------------------------------- contacts

    private fun renderContacts() {
        val contacts = store.contacts().sortedByDescending { it.updatedAt }
        container.addView(twoButtons("新建联系人", { editContactDialog(null) }, null, null))
        if (contacts.isEmpty()) {
            container.addView(Views.emptyState(this, "还没有联系人",
                "在聊天里长按悬浮球，选「把当前会话存为联系人」，或在这里手动新建。"))
            return
        }
        contacts.forEach { container.addView(contactRow(it)) }
        container.addView(Views.hintText(this,
            "点条目编辑，长按删除。会话标题等于名字或任一别名即算命中（忽略大小写与群人数后缀）。")
            .apply { setPadding(dp(2), dp(12), 0, 0) })
    }

    private fun contactRow(c0: Contact): View {
        val c = Views.card(this).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(10)
        }
        c.addView(Views.rowText(this, c0.name.ifBlank { "（无名）" }, 15f, P.PAPER, bold = true))
        if (c0.aliases.isNotEmpty())
            c.addView(Views.rowText(this, "别名：" + c0.aliases.joinToString("、"), 12f, P.SUB)
                .apply { setPadding(0, dp(3), 0, 0) })
        if (c0.apps.isNotEmpty())
            c.addView(Views.rowText(this, "来源：" + c0.apps.joinToString("、") { appLabel(it) }, 12f, P.SUB)
                .apply { setPadding(0, dp(3), 0, 0) })
        if (c0.relationship.isNotBlank())
            c.addView(Views.rowText(this, "关系：" + c0.relationship.replace("\n", " ").take(40), 12f, P.SUB)
                .apply { setPadding(0, dp(3), 0, 0) })
        if (c0.notes.isNotBlank())
            c.addView(Views.rowText(this, "备注：" + c0.notes.replace("\n", " ").take(40), 12f, P.SUB)
                .apply { setPadding(0, dp(3), 0, 0) })

        val logN = store.logSize(c0.id)
        val clear = TextView(this).apply {
            text = "清空此人历史（$logN 条）"
            textSize = 12.5f; setTextColor(P.DANGER); setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(10), 0, dp(2))
            setOnClickListener {
                if (logN == 0) { toast("本来就没有历史"); return@setOnClickListener }
                confirm("清空历史", "删掉「${c0.name}」的 $logN 条聊天历史？联系人档案保留。") {
                    store.clearLog(c0.id); render()
                }
            }
        }
        c.addView(clear)
        c.setOnClickListener { editContactDialog(c0) }
        c.setOnLongClickListener {
            confirm("删除联系人", "删除「${c0.name}」及其全部历史？不可恢复。") {
                store.deleteContact(c0.id); render()
            }
            true
        }
        return c
    }

    private fun editContactDialog(existing: Contact?) {
        val box = dialogBox()
        val nameEdit = Views.editBox(this, existing?.name ?: "", hint = "名字，一般就是会话标题")
        val aliasEdit = Views.editBox(this, existing?.aliases?.joinToString("\n") ?: "",
            hint = "每行一个，例如另一个 App 里的昵称", multiLine = 3).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            gravity = Gravity.TOP
        }
        val relEdit = Views.editBox(this, existing?.relationship ?: "", hint = "例如：同事，带我做项目的组长")
        val notesEdit = Views.editBox(this, existing?.notes ?: "", hint = "关于这个人要记住的事", multiLine = 3).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            gravity = Gravity.TOP
        }
        box.addView(Views.label(this, "名字")); box.addView(nameEdit)
        box.addView(Views.label(this, "别名（每行一个）")); box.addView(aliasEdit)
        box.addView(Views.label(this, "关系")); box.addView(relEdit)
        box.addView(Views.label(this, "备注")); box.addView(notesEdit)

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "新建联系人" else "编辑联系人")
            .setView(wrapScroll(box))
            .setPositiveButton("保存") { _, _ ->
                val name = nameEdit.text.toString().trim()
                if (name.isBlank()) { toast("名字不能空"); return@setPositiveButton }
                store.saveContact(Contact(
                    id = existing?.id ?: KbStore.newId(),
                    name = name,
                    aliases = aliasEdit.text.toString().split("\n")
                        .map { it.trim() }.filter { it.isNotEmpty() },
                    apps = existing?.apps ?: emptyList(),
                    relationship = relEdit.text.toString().trim(),
                    notes = notesEdit.text.toString().trim(),
                    autoSummary = existing?.autoSummary ?: ""
                ))
                render()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun appLabel(pkg: String): String = when (pkg) {
        "com.tencent.mobileqq" -> "QQ"
        "com.ss.android.lark" -> "飞书"
        "com.twitter.android" -> "X"
        else -> pkg
    }

    // ------------------------------------------------------- 军师资料（assets）

    private fun renderAdvisorLib() {
        // 详情视图
        detailPath?.let { renderAdvisorDetail(it); return }

        // 搜索框
        val searchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, 0)
        }
        val search = Views.editBox(this, query, hint = "搜索文件名").apply {
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        searchRow.addView(search)
        searchRow.addView(Views.ghostBtn(this, "搜索") {
            query = search.text.toString().trim(); render()
        })
        container.addView(searchRow)

        // 分类筛选
        val catRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }
        listOf("" to "全部", "knowledge" to "知识", "practical" to "实操").forEachIndexed { i, (v, name) ->
            val on = category == v
            catRow.addView(Views.text(this, name, 12f, if (on) P.BRAND else P.SUB, bold = on).apply {
                gravity = Gravity.CENTER
                setPadding(dp(14), dp(6), dp(14), dp(6))
                background = Views.round(this@KnowledgeActivity, 9, if (on) P.BRAND_DIM else P.CARD, stroke = on)
                (layoutParams as LinearLayout.LayoutParams).rightMargin = dp(8)
                setOnClickListener { category = v; render() }
            })
        }
        container.addView(catRow)

        val shown = files.filter { f ->
            (category.isEmpty() || f.startsWith("$category/")) &&
                (query.isBlank() || f.contains(query, ignoreCase = true))
        }
        container.addView(Views.rowText(this, "共 ${files.size} 份 · 命中 ${shown.size} 份",
            11f, P.FAINT).apply { setPadding(0, dp(10), 0, 0) })
        if (files.isEmpty()) {
            container.addView(Views.errorState(this, "军师资料读取失败（assets 缺失）") { render() })
            return
        }
        if (shown.isEmpty()) {
            container.addView(Views.emptyState(this, "没有匹配的资料", "换个关键词或分类试试。"))
            return
        }
        shown.forEach { container.addView(advisorRow(it)) }
    }

    private fun advisorRow(path: String): View {
        val c = Views.card(this).apply {
            (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(8)
            setOnClickListener { detailPath = path; render() }
        }
        c.addView(Views.rowText(this, displayName(path), 15f, P.PAPER, bold = true))
        c.addView(Views.rowText(this, path, 11f, P.FAINT).apply { setPadding(0, dp(3), 0, 0) })
        previewOf(path)?.let { pv ->
            c.addView(Views.rowText(this, pv.replace("\n", " ").take(60), 12f, P.SUB)
                .apply { setPadding(0, dp(4), 0, 0) })
        }
        return c
    }

    private fun renderAdvisorDetail(path: String) {
        val content = try {
            assets.open("goutou/$path").bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            "读取失败：${e.message}"
        }
        container.addView(Views.rowText(this, "← 返回列表", 14f, P.BRAND, bold = true).apply {
            setPadding(0, 0, 0, dp(6))
            setOnClickListener { detailPath = null; render() }
        })
        container.addView(Views.rowText(this, displayName(path), 20f, P.PAPER, bold = true))
        container.addView(Views.rowText(this, path, 11f, P.FAINT).apply {
            setPadding(0, dp(4), 0, dp(10))
        })
        container.addView(Views.rowText(this, stripFrontmatter(content), 14f, P.PAPER).apply {
            setLineSpacing(dp(3).toFloat(), 1f)
            setTextIsSelectable(true)
        })
    }

    /** Depth-first walk of assets/goutou; regular files only. */
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

    private fun stripFrontmatter(raw: String): String {
        val t = raw.trimStart('﻿')
        if (!t.startsWith("---")) return raw
        val end = t.indexOf("\n---", 3)
        return if (end < 0) raw else t.substring(end + 4).trimStart('\n', '\r')
    }

    private fun previewOf(path: String): String? {
        val content = try {
            assets.open("goutou/$path").bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) { return null }
        return stripFrontmatter(content).trim().lines().firstOrNull { it.isNotBlank() }
    }

    private fun displayName(path: String): String =
        path.substringAfterLast('/').removeSuffix(".md")

    // ----------------------------------------------------------------- atoms

    private fun confirm(title: String, msg: String, onYes: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title).setMessage(msg)
            .setPositiveButton("确定") { _, _ -> onYes() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

    private fun dialogBox() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(8), dp(20), dp(4))
    }

    private fun wrapScroll(v: View) = ScrollView(this).apply { addView(v) }

    private fun twoButtons(
        a: String, onA: () -> Unit,
        b: String?, onB: (() -> Unit)?
    ): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) }
        }
        row.addView(wideBtn(a, true, onA))
        if (b != null && onB != null) row.addView(wideBtn(b, false, onB))
        return row
    }

    private fun wideBtn(labelText: String, primary: Boolean, onClick: () -> Unit) = TextView(this).apply {
        text = labelText; textSize = 14f; gravity = Gravity.CENTER
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(if (primary) P.BRAND_ON else P.BRAND)
        background = Views.round(this@KnowledgeActivity, 11,
            if (primary) P.BRAND else P.CARD,
            stroke = !primary, strokeColor = P.BRAND)
        setPadding(dp(12), dp(11), dp(12), dp(11))
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            .apply { rightMargin = dp(8) }
        setOnClickListener { onClick() }
    }

    private fun smallToggle(on: Boolean, onClick: () -> Unit) = TextView(this).apply {
        text = if (on) "开" else "关"; textSize = 13f; gravity = Gravity.CENTER
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(if (on) P.BRAND_ON else P.SUB)
        background = Views.round(this@KnowledgeActivity, 10, if (on) P.BRAND else P.LINE)
        setPadding(dp(16), dp(6), dp(16), dp(6))
        setOnClickListener { onClick() }
    }
}
