package intellijicons

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

val lightBackground = Color(0xfff9f9f9)
val darkBackground = Color(0xff3c3f41)
val bluePrimary = Color(0xff318ff2)

fun lightThemeColors(): Colors {
    return lightColors().copy(
        primary = bluePrimary,
        background = lightBackground,
        onBackground = Color(0xff757575)
    )
}

fun darkThemeColors(): Colors {
    return darkColors().copy(
        primary = bluePrimary,
        background = darkBackground,
        onBackground = Color(0xffa7a7a7),
        surface = Color(0xff4C5052)
    )
}