package intellijicons.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp

fun removeDash(str: String): String {
    return str
        .replace("-", " ")
        .replace("/", " / ")
}

@Composable
fun PlaceholderIcon() {
    Image(
        painter = svgResource("icons/AllIcons/actions/annotate.svg"),
        contentDescription = "Annotate icon",
        modifier = Modifier.size(128.dp)
    )
}