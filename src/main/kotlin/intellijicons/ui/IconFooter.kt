package intellijicons.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp
import intellijicons.FooterTheme
import intellijicons.darkGray300
import intellijicons.models.DataIcon
import intellijicons.utils.imageScale

@Composable
fun IconFooter(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    activeIcon: DataIcon?
) {
    val offsetState by animateDpAsState(if (activeIcon != null) 0.dp else 72.dp)

    FooterTheme(isDarkTheme) {
        Row(
            modifier = modifier
                .height(72.dp)
                .offset(y = offsetState)
                .shadow(elevation = 16.dp)
                .fillMaxWidth(0.8f)
                .background(
                    MaterialTheme.colors.surface,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            activeIcon?.let {
                Column {
                    Text(it.name)
                    Spacer(Modifier.height(4.dp))
                    Text(it.java, style = MaterialTheme.typography.caption)
                }
                Row {
                    Thumbnail(it)
                    if (it.variants > 1) {
                        Spacer(Modifier.width(4.dp))
                        Thumbnail(it, true)
                    }
                }
            }
        }
    }
}

@Composable
private fun Thumbnail(
    icon: DataIcon,
    dark: Boolean = false
) {
    val sectionPath = if (icon.section.isNotBlank()) "${icon.section}/" else ""
    val darkSuffix = if (dark) "_dark" else ""

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(if (dark) darkGray300 else Color(0xffececec)),
        contentAlignment = Alignment.Center
    ) {
        if (icon.kind != "svg") {
            val dpiSuffix = if (icon.sizes.getOrNull(1) != null) "@2x" else ""
            var imageExists = false
            val imagePath = "icons/${icon.set}/${sectionPath}${icon.name}$dpiSuffix$darkSuffix.png"

            try {
                imageFromResource(imagePath)
                imageExists = true
            } catch (e: Exception) {
                println(e)
            }

            if (imageExists) {
                val sizes = icon.sizes
                var width = sizes[0][0]
                var height = sizes[0][1]

                /*
                if (sizes.elementAtOrNull(1) != null) {
                    width = sizes[1][0]
                    height = sizes[1][1]
                }
                */

                width = Math.min(width, 42)
                height = Math.min(height, 42)

                Image(
                    bitmap = imageResource(imagePath),
                    contentDescription = icon.name,
                    modifier = Modifier.size(
                        width = width.dp,
                        height = height.dp
                    )
                )
            }
        } else {
            // TODO: May need to figure out a better try/catch her for invalid SVG paths
            Image(
                painter = svgResource("icons/${icon.set}/${sectionPath}${icon.name}$darkSuffix.svg"),
                contentDescription = icon.name,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}