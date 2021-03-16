package intellijicons.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.dp

@Composable
fun SearchBox(isDarkActive: Boolean, onFilterChange: (String) -> Unit, onThemeChange: (Boolean) -> Unit) {
    var filter by remember { mutableStateOf("") }

    Surface {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = filter,
                    onValueChange = {
                        filter = it
                        onFilterChange(it)
                    },
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colors.surface)
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon decorator",
                                tint = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                innerTextField()
                                if (filter.isBlank()) {
                                    Text(
                                        text = "Search",
                                        style = MaterialTheme.typography.body1,
                                        color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                                    )
                                }
                            }
                            if (filter.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear icon",
                                    tint = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                                    modifier = Modifier
                                        .clickable {
                                            filter = ""
                                            onFilterChange("")
                                        }
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.body1.copy(color = LocalContentColor.current),
                    cursorBrush = SolidColor(if (isDarkActive) Color.White else Color.Black)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThemeToggleButton(active = !isDarkActive, onClick = { onThemeChange(false) })
                    Spacer(Modifier.width(12.dp))
                    ThemeToggleButton(active = isDarkActive, darkTheme = true, onClick = { onThemeChange(true) })
                }
                Spacer(Modifier.width(20.dp))
            }
            Divider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ThemeToggleButton(active: Boolean = false, darkTheme: Boolean = false, onClick: () -> Unit) {
    val hovered = remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(if (hovered.value && !active) 1.15f else 1f, tween(250))

    Box(
        modifier = Modifier
            .scale(buttonScale)
            .size(36.dp)
            .clip(CircleShape)
            .background(color = if (darkTheme) Color(0xff3c3f41) else Color.White)
            .border(
                width = 1.dp,
                color = if (darkTheme) Color(0xff3c3f41) else Color(0xffaaaaaa),
                shape = CircleShape
            )
            .clickable { onClick() }
            .pointerMoveFilter(
                onEnter = {
                    hovered.value = true
                    false
                },
                onExit = {
                    hovered.value = false
                    false
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (active) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (darkTheme) Color.White else Color.Black
            )
        }
    }
}