package intellijicons.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp
import intellijicons.models.DataIcon
import intellijicons.utils.generateIconsSrcPath

@Composable
fun IconFooter(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    activeIcon: DataIcon?,
    onClose: () -> Unit
) {
    val offsetState by animateDpAsState(if (activeIcon != null) 0.dp else 72.dp)
    val hovered = remember { mutableStateOf(false) }

    FooterTheme(isDarkTheme) {
        Box(modifier
            .offset(y = offsetState)
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
            .wrapContentSize()
        ) {
            Row(
                modifier = Modifier
                    .shadow(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        elevation = 16.dp
                    )
                    .height(72.dp)
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
                    val dark = it.dark
                    val onlyDark = it.variants == 1 && dark

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (it.kind == "svg") {
                            if (!onlyDark)          ThumbnailSvg(it)
                            if (dark)               ThumbnailSvg(it, true)
                        } else {
                            ThumbnailPng(icon = it)
                            if (it.hiDPI)           ThumbnailPng(icon = it, retina = true)
                            if (dark)               ThumbnailPng(icon = it, dark = true)
                            if (dark && it.hiDPI)   ThumbnailPng(icon = it, dark = true, retina = true)
                        }
                    }
                }
            }
            if (activeIcon != null) {
                CloseIcon(Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-12).dp, x = 12.dp)
                    .clickable { onClose() }
                )
            }
        }
    }
}

@Composable
private fun CloseIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(shape = CircleShape, elevation = 8.dp)
            .size(24.dp)
            .background(Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close icon for footer",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ThumbnailPng(
    icon: DataIcon,
    dark: Boolean = false,
    retina: Boolean = false
) {
    val darkSuffix = if (dark) "_dark" else ""

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(if (dark) darkGray300 else Color(0xffececec)),
        contentAlignment = Alignment.Center
    ) {
        val dpiSuffix = if (retina) "@2x" else ""
        var imageExists = false
        val imagePath = "${generateIconsSrcPath(icon)}${icon.name}$dpiSuffix$darkSuffix.png"

        try {
            imageFromResource(imagePath)
            imageExists = true
        } catch (e: Exception) {
            println(e)
        }

        if (imageExists) {
            val sizes = icon.sizes
            var displayWidth = sizes[0][0]
            var displayHeight = sizes[0][1]
            var actualWidth = displayWidth
            var actualHeight = displayHeight

            if (sizes.elementAtOrNull(1) != null && retina) {
                actualWidth = sizes[1][0]
                actualHeight = sizes[1][1]
            }

            displayWidth = Math.min(displayWidth, 42)
            displayHeight = Math.min(displayHeight, 42)

            Image(
                bitmap = imageResource(imagePath),
                contentDescription = "${icon.name} - thumbnail",
                modifier = Modifier.size(
                    width = displayWidth.dp,
                    height = displayHeight.dp
                )
            )
        }
    }
}

@Composable
private fun ThumbnailSvg(
    icon: DataIcon,
    dark: Boolean = false
) {
    val darkSuffix = if (dark) "_dark" else ""

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(if (dark) darkGray300 else Color(0xffececec)),
        contentAlignment = Alignment.Center
    ) {
        // TODO: May need to figure out a better try/catch her for invalid SVG paths
        Image(
            painter = svgResource("${generateIconsSrcPath(icon)}${icon.name}${darkSuffix}.svg"),
            contentDescription = "${icon.name} - thumbnail",
            modifier = Modifier.size(16.dp)
        )
    }
}