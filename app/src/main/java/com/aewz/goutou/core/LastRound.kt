package com.aewz.goutou.core

/**
 * 进程内「最近一轮分析」快照：采集服务在每轮结束时写入，主页与
 * 聊天分析结果页读取。同进程共享（无障碍服务与 Activity 同属本应用进程），
 * 不落盘——App 被杀即清空，无隐私残留。
 */
object LastRound {

    @Volatile var snapshot: ChatSnapshot? = null
    @Volatile var title: String? = null       // 会话标题（来源）
    @Volatile var app: String? = null         // 来源包名
    @Volatile var mode: String? = null        // 当时模式（Prefs.MODE_*）
    @Volatile var judgment: Analysis? = null
    @Volatile var advisorNote: String? = null
    @Volatile var replies: List<RankedReply> = emptyList()
    @Volatile var replyError: String? = null
    @Volatile var analysisError: String? = null
    @Volatile var waiting: Boolean = false
    @Volatile var at: Long = 0                // 最近完成时刻

    val hasResult: Boolean get() = at > 0
    val totalReplies: Int get() = replies.size

    fun beginRound(snap: ChatSnapshot, convTitle: String?, pkg: String?, advisorMode: String) {
        snapshot = snap
        title = convTitle
        app = pkg
        mode = advisorMode
        judgment = null
        advisorNote = null
        replies = emptyList()
        replyError = null
        analysisError = null
        waiting = true
    }

    fun finish(error: String? = null) {
        waiting = false
        at = System.currentTimeMillis()
        analysisError = error
    }
}
