package intellijicons.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import intellijicons.models.DataIcon
import intellijicons.utils.imageScale
import intellijicons.utils.removeDash

@Composable
fun IconTile(
    modifier: Modifier = Modifier,
    set: String,
    icon: DataIcon,
    dark: Boolean = false
) {
    val sectionPath = if (icon.section.isNotBlank()) "${icon.section}/" else ""
    val darkSuffix = if (dark) "_dark" else ""
    val hovered = remember { mutableStateOf(false) }
    val defaultIconSize = 32

    Column(
        modifier = modifier
            .clickable { }
            .pointerMoveFilter(
                onEnter = {
                    hovered.value = true
                    false
                },
                onExit = {
                    hovered.value = false
                    false
                }
            )
            .clip(RoundedCornerShape(8.dp))
            .background(color = if (hovered.value) MaterialTheme.colors.onBackground.copy(alpha = 0.1f) else Color.Transparent)
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            if (icon.kind != "svg") {
                val dpiSuffix = if (icon.sizes.getOrNull(1) != null) "@2x" else ""
                var imageExists = false
                val imagePath = "icons/$set/${sectionPath}${icon.name}$dpiSuffix$darkSuffix.png"

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

                    if (sizes.elementAtOrNull(1) != null) {
                        width = sizes[1][0]
                        height = sizes[1][1]
                    }

                    width = Math.min(width, 64)
                    height = Math.min(height, 64)

                    Image(
                        bitmap = imageResource(imagePath),
                        contentDescription = icon.name,
                        modifier = Modifier.size(
                            width = imageScale(hovered.value, width).value,
                            height = imageScale(hovered.value, height).value
                        )
                    )
                }
            } else {
                // TODO: May need to figure out a better try/catch her for invalid SVG paths
                Image(
                    painter = svgResource("icons/$set/${sectionPath}${icon.name}$darkSuffix.svg"),
                    contentDescription = icon.name,
                    modifier = Modifier.size(imageScale(hovered.value, defaultIconSize).value)
                )
            }
        }

        Text(
            text = removeDash(icon.name),
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}