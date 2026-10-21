package com.aewz.goutou.engine

import com.aewz.goutou.core.Analysis
import com.aewz.goutou.core.ChatSnapshot
import com.aewz.goutou.core.Prefs
import com.aewz.goutou.core.RankedReply
import com.aewz.goutou.core.kb.ChatContext

/**
 * Thin facade over the three split clients so callers keep one entry point.
 * Construct with [Prefs] — every route reads its own address / key / model from
 * there, so switching providers in settings takes effect on the next call.
 */
class EngineClient(prefs: Prefs) {

    private val judgeClient = JudgeClient(prefs)
    private val replyClient = ReplyClient(prefs)

    /** The 7 judgment questions. Errors come back inside [Analysis.error]. */
    fun judge(snapshot: ChatSnapshot, relationship: String, ctx: ChatContext? = null): Analysis =
        judgeClient.judge(snapshot, relationship, ctx)

    /**
     * Draft [count] candidates on the reply route, then rank them on the judge
     * route. The ranking question requires exactly 3 candidates, so anything
     * fewer comes back unranked (prob 0.0 — the overlay then hides rank bars).
     * [avoid] = replies already shown for this chat (the "换一组" request).
     */
    fun draftAndRank(
        snapshot: ChatSnapshot,
        relationship: String,
        ctx: ChatContext? = null,
        count: Int = 3,
        avoid: List<String> = emptyList()
    ): List<RankedReply> {
        val candidates = replyClient.draft(snapshot, relationship, ctx, count, avoid)
        if (candidates.size < 3) return candidates.map { RankedReply(it, 0.0) }
        return judgeClient.rank(snapshot, relationship, candidates, ctx)
    }

    /**
     * Draft with an explicit persona and NO ranking round (the judge route is a
     * judge service the GOUTOU half never touches). Returns unranked candidates.
     */
    fun draft(
        snapshot: ChatSnapshot,
        relationship: String,
        ctx: ChatContext? = null,
        count: Int = 3,
        avoid: List<String> = emptyList(),
        persona: Persona
    ): List<String> =
        replyClient.draft(snapshot, relationship, ctx, count, avoid, persona)

    /** The 军师简析 (GOUTOU mode): plain text, reply route only, no judge calls. */
    fun advisorAnalysis(snapshot: ChatSnapshot, relationship: String, ctx: ChatContext? = null): String =
        replyClient.advisorAnalysis(snapshot, relationship, ctx)

    /** Judge + replies, sequential. Used by the settings connectivity test. */
    fun analyze(snapshot: ChatSnapshot, relationship: String, ctx: ChatContext? = null): Analysis {
        val a = judge(snapshot, relationship, ctx)
        if (a.error != null) return a
        val ranked = try { draftAndRank(snapshot, relationship, ctx) } catch (e: Exception) { emptyList() }
        return a.copy(rankedReplies = ranked)
    }
}
