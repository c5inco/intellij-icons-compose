package intellijicons.utils

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import intellijicons.models.DataIcon

fun removeDash(str: String): String {
    return str
        .replace("-", " ")
        .replace("/", " / ")
}

fun generateIconsSrcPath(icon: DataIcon): String {
    val sectionPath = if (icon.section.isNotBlank()) "${icon.section}/" else ""

    return "icons/${icon.set}/${sectionPath}"
}

fun <T> chunk(list: List<T>, size: Int): List<List<T>> {
    val chunkedList = mutableListOf<List<T>>()

    for (i in 0..list.size step size) {
        var chunkIndex = i + size
        if (chunkIndex > list.size) {
            chunkIndex = i + (list.size % size)
        }

        chunkedList.add(list.slice(i until chunkIndex))
    }

    return chunkedList.toList()
}

@Composable
fun FeedbackState(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
        Spacer(modifier = Modifier.fillMaxHeight(0.5f))
    }
}

@Composable
fun PlaceholderIcon() {
    Image(
        painter = svgResource("icons/AllIcons/actions/annotate.svg"),
        contentDescription = "Annotate icon",
        modifier = Modifier.size(128.dp)
    )
}

@Composable
fun imageScale(active: Boolean, target: Int): State<Dp> {
    return animateDpAsState(if (active) (target * 1.4).dp else target.dp)
}

fun Modifier.upperRoundRectStroke(
    brush: Brush,
    stroke: Stroke,
    cornerRadius: CornerRadius
): Modifier {
    return this.then(Modifier.drawWithContent {
        drawContent()
        clipRect {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(stroke.width / 2, stroke.width / 2), // inset round rect to account for the stroke width
                size = Size(
                    size.width - stroke.width, // adjust size to account for stroke width on either side
                    size.height - stroke.width / 2 + cornerRadius.y // make the rect taller to extend beyond the bounds of the clip
                ),
                style = stroke,
                cornerRadius = cornerRadius
            )
        }
    }
    )
}