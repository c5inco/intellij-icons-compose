package intellijicons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp
import com.beust.klaxon.Klaxon
import intellijicons.models.*
import intellijicons.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.withContext
import java.io.File

fun main() {
    Window {
        var isDarkTheme by remember { mutableStateOf(false) }
        var searchFilter by remember { mutableStateOf("")}
        var iconsFilter by remember { mutableStateOf("")}
        val filterFlow = MutableStateFlow("")
        val allGroups = remember { mutableStateListOf<DataIconGroup>() }

        LaunchedEffect(allGroups) {
            withContext(Dispatchers.IO) {
                val jsonData = File("src/main/resources/data.json").readText(Charsets.UTF_8)
                val result = Klaxon().parseArray<DataIconSet>(jsonData)

                result?.forEach {
                    val (set, _, sections, icons) = it
                    if (icons.isEmpty()) println("$set, icons empty")
                    //assert(icons.isNotEmpty())

                    val g = sections.map { section ->
                        icons.filter { icon ->
                            icon.section == section
                        }
                    }
                    if (g.isEmpty()) {
                        println("$set, group empty")
                    }
                    //assert(g.isNotEmpty())

                    g.forEachIndexed { index, list ->
                        if (list.isNotEmpty()) allGroups.add(DataIconGroup(set = set, section = sections[index], icons = list))
                    }
                }
                assert(allGroups.isNotEmpty())
            }
            filterFlow
                .debounce(timeoutMillis = 300L)
                .collect {
                    iconsFilter = it
                }
        }

        MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchBox(
                        isDarkActive = isDarkTheme,
                        filter = searchFilter,
                        onFilterChange = {
                            searchFilter = it
                            filterFlow.value = it
                         },
                        onThemeChange = { isDarkTheme = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Total groups: ${allGroups.size}")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (allGroups.size > 0) {
                        LazyColumn {
                            items(allGroups) { group ->
                                val sortedIcons = filterAndSortIcons(group, iconsFilter)

                                if (sortedIcons.isNotEmpty()) {
                                    IconGroup(group = group, sortedIcons = sortedIcons, isDarkTheme = isDarkTheme)
                                }
                            }
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    }
                }
            }
        }
    }
}

private fun filterAndSortIcons(group: DataIconGroup, searchFilter: String): List<DataIcon> {
    var filteredIcons = group.icons.toList()
    if (searchFilter.isNotBlank()) {
        filteredIcons = group.icons.filter { icon ->
            matchSearchFilter(icon, group.set, searchFilter)
        }
    }
    return filteredIcons.sortedBy { icon -> icon.name }
}

private fun matchSearchFilter(icon: DataIcon, set: String, searchFilter: String): Boolean {
    val s = set.toLowerCase()
    val sec = icon.section.toLowerCase()
    val sf = searchFilter.toLowerCase()
    var n = icon.name.toLowerCase()
    n = removeDash(n)

    return s.contains(sf) || sec.contains(sf) || n.contains(sf)
}

@Composable
private fun IconGroup(
    group: DataIconGroup,
    sortedIcons: List<DataIcon>,
    chunkSize: Int = 6,
    isDarkTheme: Boolean
) {
    Column {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = removeDash("${group.set} / ${group.section} — ${sortedIcons.size}"),
            style = MaterialTheme.typography.subtitle2
        )
        Spacer(modifier = Modifier.height(16.dp))
        for (i in 0..sortedIcons.size step chunkSize) {
            IconRow(i, chunkSize, sortedIcons, group, isDarkTheme)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Divider(thickness = 1.dp)
    }
}

@Composable
private fun IconRow(
    i: Int,
    chunkSize: Int,
    sortedIcons: List<DataIcon>,
    group: DataIconGroup,
    darkTheme: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        var chunkIndex = i + chunkSize
        if (chunkIndex > sortedIcons.size) {
            chunkIndex = i + (sortedIcons.size % chunkSize)
        }

        sortedIcons.slice(i until chunkIndex).forEach {
            // Need to check if icon only has a dark variant, which can happen
            var iconDark = if (darkTheme) it.dark else darkTheme
            if (it.variants == 1 && it.dark) iconDark = true

            IconTile(modifier = Modifier.weight(1f), set = group.set, icon = it, dark = iconDark)
        }
    }
}

@Composable
private fun IconTile(
    modifier: Modifier = Modifier,
    set: String,
    icon: DataIcon,
    dark: Boolean = false
) {
    val iconSize = 64.dp
    val sectionPath = if (icon.section.isNotBlank()) "${icon.section}/" else ""
    val darkSuffix = if (dark) "_dark" else ""

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon.kind == "png") {
            val dpiSuffix = if (icon.sizes.getOrNull(1) != null) "@2x" else ""
            var imageExists = false
            val imagePath = "icons/$set/${sectionPath}${icon.name}$dpiSuffix$darkSuffix.png"

            try {
                val img = imageFromResource(imagePath)
                imageExists = true
            } catch(e: Exception){
                println(e)
            }

            if (imageExists) {
                Image(
                    bitmap = imageResource(imagePath),
                    contentDescription = icon.name,
                    modifier = Modifier.size(iconSize)
                )
            }
        } else {
            Image(
                painter = svgResource("icons/$set/${sectionPath}${icon.name}$darkSuffix.svg"),
                contentDescription = icon.name,
                modifier = Modifier.size(iconSize)
            )
        }

        Text(
            text = removeDash(icon.name),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Composable
private fun SearchBox(isDarkActive: Boolean, filter: String, onFilterChange: (String) -> Unit, onThemeChange: (Boolean) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = filter,
                onValueChange = { onFilterChange(it) },
                singleLine = true,
                decorationBox = { innerTextField ->
                    // Because the decorationBox is used, the whole Row gets the same behaviour as the
                    // internal input field would have otherwise. For example, there is no need to add a
                    // Modifier.clickable to the Row anymore to bring the text field into focus when user
                    // taps on a larger text field area which includes paddings and the icon areas.
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface)
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                        )
                        Spacer(Modifier.width(12.dp))
                        Box {
                            innerTextField()
                            if (filter.isBlank()) {
                                Text(
                                    text = "Search",
                                    style = MaterialTheme.typography.body1,
                                    color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.body1.copy(color = LocalContentColor.current)
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