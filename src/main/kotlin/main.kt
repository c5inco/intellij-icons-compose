package intellijicons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
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

@ExperimentalFoundationApi
fun main() {
    val defaultWindowSize = IntSize(width = 800, height = 600)
    var chunkSize = mutableStateOf(6)

    Window(
        title = "IntelliJ Icons",
        size = defaultWindowSize,
        events = WindowEvents(
            onResize = { (width, _) ->
                when {
                    width >= 1000 -> chunkSize.value = 8
                    width >= 800 -> chunkSize.value = 6
                    width >= 600 -> chunkSize.value = 4
                    else -> chunkSize.value = 3
                }
            }
        )
    ) {
        var isDarkTheme by remember { mutableStateOf(false) }
        var searchFilter by remember { mutableStateOf("")}
        val filterFlow = remember { MutableStateFlow("") }
        val allGroupsMap = remember { mutableStateMapOf<DataIconGroup, List<DataIcon>>() }

        LaunchedEffect(allGroupsMap) {
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
                        if (list.isNotEmpty()) {
                            allGroupsMap.put(DataIconGroup(set = set, section = sections[index]), list)
                        }
                    }
                }
                assert(allGroupsMap.isNotEmpty())
            }
        }

        LaunchedEffect(isDarkTheme) {
            filterFlow
                .debounce(timeoutMillis = 300L)
                .collect {
                    searchFilter = it
                }
        }

        DesktopMaterialTheme(colors = if (isDarkTheme) darkThemeColors() else lightThemeColors()) {
            Surface(color = MaterialTheme.colors.background) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchBox(
                        isDarkActive = isDarkTheme,
                        onFilterChange = {
                            filterFlow.value = it
                        },
                        onThemeChange = { isDarkTheme = it }
                    )
                    if (allGroupsMap.size > 0) {
                        LazyColumn {
                            val sortedGroupsMap = allGroupsMap.toSortedMap(compareBy<DataIconGroup> { it.set }.thenBy { it.section })
                            sortedGroupsMap.forEach { (group, icons) ->
                                val filteredList = filterAndSortIcons(icons, group.set, searchFilter)

                                if (filteredList.isNotEmpty()) {
                                    stickyHeader {
                                        IconGroupHeader(group, icons)
                                    }

                                    items(chunk(filteredList, chunkSize.value)) { iconsChunk ->
                                        IconsRow(iconsChunk, chunkSize.value, isDarkTheme, group)
                                    }

                                    item(group) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.fillMaxHeight(0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconGroupHeader(
    group: DataIconGroup,
    icons: List<DataIcon>
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background.copy(alpha = 0.9f))
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Text(
            text = removeDash("${group.set} / ${group.section}"),
            style = MaterialTheme.typography.subtitle2
        )

        /*
        Text(
            text = "${icons.size}",
            style = MaterialTheme.typography.subtitle2
        )
        */
    }
}

@Composable
private fun IconsRow(
    iconsChunk: List<DataIcon>,
    chunkSize: Int,
    isDarkTheme: Boolean,
    group: DataIconGroup
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        iconsChunk.forEach {
            // Need to check if icon only has a dark variant, which can happen
            var iconDark = if (isDarkTheme) it.dark else isDarkTheme
            if (it.variants == 1 && it.dark) iconDark = true

            IconTile(
                modifier = Modifier.weight(1f),
                set = group.set,
                icon = it,
                dark = iconDark
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

private fun filterAndSortIcons(icons: List<DataIcon>, set:String, searchFilter: String): List<DataIcon> {
    var filteredIcons = icons.toList()
    if (searchFilter.isNotBlank()) {
        filteredIcons = icons.filter { icon ->
            matchSearchFilter(icon, set, searchFilter)
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
private fun IconTile(
    modifier: Modifier = Modifier,
    set: String,
    icon: DataIcon,
    dark: Boolean = false
) {
    val defaultIconSize = 32.dp
    val sectionPath = if (icon.section.isNotBlank()) "${icon.section}/" else ""
    val darkSuffix = if (dark) "_dark" else ""
    val hovered = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
                } catch(e: Exception){
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
                        modifier = Modifier.size(width = width.dp, height = height.dp).background(Color.LightGray)
                    )
                }
            } else {
                Image(
                    painter = svgResource("icons/$set/${sectionPath}${icon.name}$darkSuffix.svg"),
                    contentDescription = icon.name,
                    modifier = Modifier.size(defaultIconSize)
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

@Composable
private fun SearchBox(isDarkActive: Boolean, onFilterChange: (String) -> Unit, onThemeChange: (Boolean) -> Unit) {
    var filter by remember { mutableStateOf("")}

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