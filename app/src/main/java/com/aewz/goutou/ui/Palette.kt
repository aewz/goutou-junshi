package com.aewz.goutou.ui

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.aewz.goutou.R

/**
 * M3 紫系色板（浅色/深色两套，取自 res/values(-night)/colors.xml）。
 * 用法：页面内 `private val P get() = Palette.of(this)`；工具函数用 `Palette.of(ctx)`。
 * 系统切深色模式后所有取色自动跟随。
 */
class Palette(
    @JvmField val BG: Int,
    @JvmField val CARD: Int,
    @JvmField val CARD_RAISED: Int,
    @JvmField val LINE: Int,
    @JvmField val PAPER: Int,
    @JvmField val SUB: Int,
    @JvmField val FAINT: Int,
    @JvmField val BRAND: Int,
    @JvmField val BRAND_DIM: Int,
    @JvmField val BRAND_ON: Int,
    @JvmField val GOLD: Int,
    @JvmField val GOLD_DIM: Int,
    @JvmField val DANGER: Int,
    @JvmField val OK: Int,
    @JvmField val INFO_BLUE: Int,
    @JvmField val SCRIM: Int
) {
    companion object {
        private var light: Palette? = null
        private var dark: Palette? = null

        fun of(ctx: Context): Palette {
            val night = (ctx.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            if (night) return dark ?: build(ctx).also { dark = it }
            return light ?: build(ctx).also { light = it }
        }

        private fun build(ctx: Context) = Palette(
            BG = color(ctx, R.color.g_bg),
            CARD = color(ctx, R.color.g_surface),
            CARD_RAISED = color(ctx, R.color.g_surface_variant),
            LINE = color(ctx, R.color.g_outline),
            PAPER = color(ctx, R.color.g_on_surface),
            SUB = color(ctx, R.color.g_on_surface_var),
            FAINT = color(ctx, R.color.g_on_surface_faint),
            BRAND = color(ctx, R.color.g_primary),
            BRAND_DIM = color(ctx, R.color.g_primary_container),
            BRAND_ON = color(ctx, R.color.g_on_primary),
            GOLD = color(ctx, R.color.g_tertiary),
            GOLD_DIM = color(ctx, R.color.g_tertiary_container),
            DANGER = color(ctx, R.color.g_error),
            OK = color(ctx, R.color.g_success),
            INFO_BLUE = color(ctx, R.color.g_secondary),
            SCRIM = color(ctx, R.color.g_scrim)
        )

        private fun color(ctx: Context, id: Int) = ContextCompat.getColor(ctx, id)
    }
}
