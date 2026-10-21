package com.aewz.goutou.ui

import android.graphics.Color

/**
 * 狗头军师色板 v3 — 浅紫纸感：浅紫底、白卡轻投影、深紫渐变主操作、
 * 金色标记首选。与图标同族；所有页面与悬浮窗从这里取色。
 */
object Palette {
    // 底 / 卡 / 描边
    @JvmField val BG = Color.parseColor("#F7F6FB")
    @JvmField val CARD = Color.parseColor("#FFFFFF")
    @JvmField val CARD_RAISED = Color.parseColor("#FDFCFF")
    @JvmField val LINE = Color.parseColor("#EAE7F6")

    // 文本
    @JvmField val PAPER = Color.parseColor("#201A2E")
    @JvmField val SUB = Color.parseColor("#6B6483")
    @JvmField val FAINT = Color.parseColor("#A79EC0")

    // 品牌紫 / 浅紫底
    @JvmField val BRAND = Color.parseColor("#6C4CF1")
    @JvmField val BRAND_DEEP = Color.parseColor("#7C3AED")
    @JvmField val BRAND_DIM = Color.parseColor("#F0EBFD")
    @JvmField val BRAND_ON = Color.parseColor("#FFFFFF")

    // 金（首选/高亮）· 状态
    @JvmField val GOLD = Color.parseColor("#C8892B")
    @JvmField val GOLD_DIM = Color.parseColor("#FFF3DD")
    @JvmField val DANGER = Color.parseColor("#D64545")
    @JvmField val OK = Color.parseColor("#27915C")
    @JvmField val INFO_BLUE = Color.parseColor("#3F74C9")
}
