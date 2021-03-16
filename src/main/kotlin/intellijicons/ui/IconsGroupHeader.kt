package intellijicons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import intellijicons.IconsGroupHeaderTheme
import intellijicons.models.DataIcon
import intellijicons.models.DataIconGroup
import intellijicons.utils.removeDash

@Composable
fun IconsGroupHeader(
    group: DataIconGroup,
    icons: List<List<DataIcon>>,
    isDarkTheme: Boolean
) {
    val totalIcons = icons.fold(0) { acc, chunkedIcons -> acc + chunkedIcons.size }

    IconsGroupHeaderTheme(isDarkTheme) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .padding(vertical = 12.dp, horizontal = 20.dp)
        ) {
            Text(
                text = removeDash("${group.set} / ${group.section}"),
                style = MaterialTheme.typography.subtitle2
            )

            /*
        Text(
            text = "$totalIcons",
            style = MaterialTheme.typography.subtitle2
        )
        */
        }
    }
}