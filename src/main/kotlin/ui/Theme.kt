package intellijicons.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val lightBackground = Color(0xfff9f9f9)
val darkGray300 = Color(0xff3c3f41)
val darkGray500 = Color(0xff2d2f31)
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
        background = darkGray300,
        onBackground = Color(0xffa7a7a7),
        surface = Color(0xff4C5052)
    )
}

@Composable
fun IconsGroupHeaderTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColors().copy(
            surface = Color(0xff45494a)
        )
    } else {
        lightColors().copy(
            surface = Color(0xfff2f2f2)
        )
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}

@Composable
fun FooterTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColors().copy(
            surface = darkGray500,
            onSurface = Color.White
        )
    } else {
        lightColors().copy(
        )
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}