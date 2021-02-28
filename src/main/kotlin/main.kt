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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp
import com.beust.klaxon.Klaxon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class DataIconSet (
    val set: String,
    val areas: List<String>,
    val sections: List<String>,
    val icons: List<DataIcon>
)

data class DataIconGroup (
    val set: String,
    val section: String,
    val icons: List<DataIcon>
)

data class DataIcon (
    val name: String,
    val area: String,
    val section: String,
    val variants: Int,
    val dark: Boolean,
    val hiDPI: Boolean,
    val sizes: List<List<Int>>,
    val kind: String,
    val java: String
)

fun main() {
    Window {
        var isDarkTheme by remember { mutableStateOf(false) }
        val allGroups = remember { mutableStateListOf<DataIconGroup>() }

        LaunchedEffect(allGroups) {
            withContext(Dispatchers.IO) {
                val jsonData = File("src/main/resources/data.json").readText(Charsets.UTF_8)
                val result = Klaxon().parseArray<DataIconSet>(jsonData)
                //assert(result?.get(2)?.set == "AngularJSIcons")

                //val allGroups = mutableListOf<DataIconGroup>()

                result?.forEach {
                    val (set, area, sections, icons) = it
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

                //result?.let { iconsData.addAll(it) }
            }
        }

        MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchBox(isDarkActive = isDarkTheme, onThemeChange = { isDarkTheme = it })
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Total groups: ${allGroups.size}")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (allGroups.size > 0) {
                        LazyColumn {
                            items(allGroups) { group ->
                                val chunkSize = 6
                                val sortedIcons = group.icons.sortedBy { icon -> icon.name }

                                Column {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Text(
                                        text = "${group.set} / ${group.section} — ${sortedIcons.size}",
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
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    }
                }
            }
        }
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

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                IconTile(set = group.set, icon = it, dark = iconDark)
            }
        }
    }
}

@Composable
private fun IconTile(set: String, icon: DataIcon, dark: Boolean = false) {
    val iconSize = 64.dp
    val sectionPath = if(icon.section.isNotBlank()) "${icon.section}/" else ""
    val darkStr = if (dark) "_dark" else ""

    if (icon.kind == "png") {
        val dpiSuffix = if (icon.sizes.getOrNull(1) != null) "@2x" else ""

        Image(
            bitmap = imageResource("icons/$set/${sectionPath}${icon.name}$darkStr$dpiSuffix.png"),
            contentDescription = icon.name,
            modifier = Modifier.size(iconSize)
        )
    } else {
        Image(
            painter = svgResource("icons/$set/${sectionPath}${icon.name}$darkStr.svg"),
            contentDescription = icon.name,
            modifier = Modifier.size(iconSize)
        )
    }

    Text(
        text = icon.name,
        style = MaterialTheme.typography.caption,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun SearchBox(isDarkActive: Boolean, onThemeChange: (Boolean) -> Unit) {
    Column {
        var value by rememberSaveable { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = { value = it },
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
                            if (value.isEmpty()) {
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
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color = if (darkTheme) Color(0xff3c3f41) else Color.White)
            .border(
                width = 1.dp,
                color = if (darkTheme) Color(0xff3c3f41) else Color(0xffaaaaaa),
                shape = CircleShape
            )
            .clickable { onClick() },
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

@Composable
fun PlaceholderIcon() {
    Image(
        painter = svgResource("icons/AllIcons/actions/annotate.svg"),
        contentDescription = "Annotate icon",
        modifier = Modifier.size(128.dp)
    )
}