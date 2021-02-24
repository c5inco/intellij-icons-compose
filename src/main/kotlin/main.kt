import AnnotationIcon
import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.*
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun main() {
    Window {
        // var (text, setText) = remember { mutableStateOf("Hello, World!") }
        // var count = mutableStateOf(0)
        var isDarkTheme by remember { mutableStateOf(false) }

        DisposableEffect(Unit) {
            // stuff
            onDispose {
                // stuff
            }
        }

        MaterialTheme(colors = if (isDarkTheme) darkColors() else lightColors()) {
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchBox(isDarkActive = isDarkTheme, onThemeChange = { isDarkTheme = it })
                    Spacer(modifier = Modifier.height(64.dp))
                    Text(text = "Hello there $isDarkTheme")
                    Spacer(modifier = Modifier.height(64.dp))
                    AnnotationIcon()
                }
            }
        }
    }
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
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (darkTheme) Color(0xff3c3f41) else Color(0xffaaaaaa),
                shape = CircleShape)
            .background(color = if (darkTheme) Color(0xff3c3f41) else Color.White)
            .size(36.dp),
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
fun AnnotationIcon() {
    Image(
        painter = svgResource("actions/annotate.svg"),
        contentDescription = "Annotate icon",
        modifier = Modifier.size(128.dp)
    )
}