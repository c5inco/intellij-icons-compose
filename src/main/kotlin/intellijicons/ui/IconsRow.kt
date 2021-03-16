package intellijicons.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import intellijicons.models.DataIcon
import intellijicons.models.DataIconGroup

@Composable
fun IconsRow(
    iconsChunk: List<DataIcon>,
    chunkSize: Int,
    isDarkTheme: Boolean,
    group: DataIconGroup,
    onIconSelect: (DataIcon?) -> Unit,
    activeIcon: DataIcon?
) {
    Row(
        modifier = Modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        iconsChunk.forEach {
            // Need to check if icon only has a dark variant, which can happen
            var iconDark = if (isDarkTheme) it.dark else isDarkTheme
            if (it.variants == 1 && it.dark) iconDark = true

            IconTile(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onIconSelect(if (it == activeIcon) null else it)
                    },
                set = group.set,
                icon = it,
                dark = iconDark,
                active = it == activeIcon
            )
        }

        // Needed since rows can't have set columns
        if (iconsChunk.size < chunkSize) {
            for (i in 0 until chunkSize - iconsChunk.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}