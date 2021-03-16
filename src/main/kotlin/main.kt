package intellijicons

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.beust.klaxon.Klaxon
import intellijicons.ui.IconsGroupHeader
import intellijicons.ui.IconsRow
import intellijicons.ui.SearchBox
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
    val defaultWindowSize = IntSize(width = 800, height = 700)
    var chunkSize = mutableStateOf(6)

    Window(
        title = "IntelliJ Icons",
        size = defaultWindowSize,
        events = WindowEvents(
            onResize = { (width, _) ->
                when {
                    width >= 1200 -> chunkSize.value = 10
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
                        val sortedGroupsMap = allGroupsMap.toSortedMap(compareBy<DataIconGroup> { it.set }.thenBy { it.section })
                        val chunkedGroupsMap = mutableMapOf<DataIconGroup, List<List<DataIcon>>>()

                        sortedGroupsMap.forEach { (group, icons) ->
                            val filteredList = filterAndSortIcons(icons, group.set, searchFilter)

                            if (filteredList.isNotEmpty()) {
                                val chunked = chunk(filteredList, chunkSize.value)
                                chunkedGroupsMap.set(group, chunked.toList())
                            }
                        }

                        if (chunkedGroupsMap.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 32.dp)
                            ) {
                                chunkedGroupsMap.forEach { (group, chunkedIcons) ->
                                    stickyHeader {
                                        IconsGroupHeader(group, chunkedIcons)
                                    }

                                    items(chunkedIcons) { iconsChunk ->
                                        IconsRow(iconsChunk, chunkSize.value, isDarkTheme, group)
                                    }

                                    item(group) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(thickness = 1.dp)
                                    }
                                }
                            }
                        } else {
                            FeedbackState {
                                Text(
                                    text = "No results found.",
                                    style = MaterialTheme.typography.subtitle2
                                )
                            }
                        }
                    } else {
                        FeedbackState {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
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