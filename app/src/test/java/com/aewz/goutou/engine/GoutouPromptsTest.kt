package com.aewz.goutou.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The condensed persona is the only thing injected into every GOUTOU / HYBRID
 * draft — its contract must hold: identity, the five steps, the JSON-array
 * output format, and the safety boundaries from SKILL.md.
 */
class GoutouPromptsTest {

    @Test fun draftSystemNamesThePersona() {
        assertTrue(GoutouPrompts.draftSystem(3).contains("狗头军师"))
    }

    @Test fun draftSystemCarriesTheFiveSteps() {
        val s = GoutouPrompts.draftSystem(3)
        assertTrue(s.contains("情绪落地"))
        assertTrue(s.contains("事实拆分"))
        assertTrue(s.contains("利益判断"))
        assertTrue(s.contains("明确建议"))
        assertTrue(s.contains("行动收束"))
    }

    @Test fun draftSystemDemandsABareJsonArray() {
        val s = GoutouPrompts.draftSystem(3)
        assertTrue(s.contains("JSON 数组"))
        assertTrue(s.contains("不要解释"))
    }

    @Test fun draftSystemAsksForExactlyNReplies() {
        assertEquals(1, Regex("含且仅含 (\\d+) 条").find(GoutouPrompts.draftSystem(1))!!.groupValues[1].toInt())
        assertEquals(3, Regex("含且仅含 (\\d+) 条").find(GoutouPrompts.draftSystem(3))!!.groupValues[1].toInt())
    }

    @Test fun draftSystemKeepsTheSafetyBoundaries() {
        val s = GoutouPrompts.draftSystem(3)
        assertTrue(s.contains("不编造"))
        assertTrue(s.contains("不读心"))
        assertTrue(s.contains("不强行推进"))
        assertTrue(s.contains("不贬低"))
    }

    @Test fun singleCandidateDropsTheStrategyList() {
        assertTrue(GoutouPrompts.draftSystem(1).contains("最稳妥、最自然"))
        assertTrue(!GoutouPrompts.draftSystem(1).contains("策略要有区别"))
    }

    @Test fun analysisPromptIsShortAndPlainText() {
        assertTrue(GoutouPrompts.ANALYSIS.contains("不超过 60 字"))
        assertTrue(GoutouPrompts.ANALYSIS.contains("直接输出简析正文"))
        assertTrue(GoutouPrompts.ANALYSIS.contains("情绪落地"))
    }
}
