package me.imtoggle.tooltipshot

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.KeyBind
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.core.OneKeyBind
import cc.polyfrost.oneconfig.config.data.*
import cc.polyfrost.oneconfig.libs.universal.UKeyboard

object ModConfig : Config(Mod(TooltipShot.NAME, ModType.UTIL_QOL), "${TooltipShot.MODID}.json") {

    @Slider(
        name = "Scale",
        min = 1f,
        max = 5f
    )
    var screenshotScale = 2
        get() = field.coerceIn(1..5)

    @KeyBind(
        name = "ScreenShot"
    )
    var shotKeyBind = OneKeyBind(UKeyboard.KEY_K)

    init {
        initialize()
    }
}