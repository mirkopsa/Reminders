package com.example.reminders

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    var editReminderId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("loginCredentials", MODE_PRIVATE)
        val loginMethod = sharedPref.getString("login_method", null)

        setContent {

            val owner = LocalViewModelStoreOwner.current

            owner?.let {
                val viewModel: MainViewModel = viewModel(
                    it,
                    "MainViewModel",
                    MainViewModelFactory(
                        LocalContext.current.applicationContext
                                as Application
                    )
                )

                Column {
                    TopAppBar(
                        title = {
                            Text(text = "Reminders")
                        },
                        actions = {
                            OverflowMenu {
                                DropdownMenuItem(onClick = {
                                    val intent =
                                        Intent(this@MainActivity, SettingsActivity::class.java)
                                    startActivity(intent)
                                }) {
                                    Text("Settings")
                                }
                                if (loginMethod == "account" || loginMethod == "pin") {
                                    DropdownMenuItem(onClick = {
                                        val intent =
                                            Intent(this@MainActivity, LoginActivity::class.java)
                                        startActivity(intent)
                                    }) {
                                        Text("Sign out")
                                    }
                                }
                            }
                        }
                    )
                }

                ScreenSetup(viewModel)
            }
        }
    }
}

@Composable
fun ScreenSetup(viewModel: MainViewModel) {

    val allReminders by viewModel.allReminders.observeAsState(listOf())
    val searchResults by viewModel.searchResults.observeAsState(listOf())

    MainScreen(
        allReminders = allReminders,
        searchResults = searchResults,
        viewModel = viewModel
    )
}

@Composable
fun MainScreen(
    allReminders: List<Reminder>,
    searchResults: List<Reminder>,
    viewModel: MainViewModel
) {

    var reminderMessage by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }

    val onReminderTextChange = { text : String ->
        reminderMessage = text
    }

    val openReminderDialog = remember { mutableStateOf(false)  }

    Box(
        Modifier
            .fillMaxSize()
            .zIndex(2f)) {
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            onClick = {
                openReminderDialog.value = true
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add reminder",
                tint = Color.White,
            )
        }
    }

    if (openReminderDialog.value) {
        Dialog(
            onDismissRequest = {
                openReminderDialog.value = false
            }
        ) {
            Surface(
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 24.dp)) {
                    Row {
                        Text("New reminder", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(modifier = Modifier.offset(x = 8.dp, y = -12.dp),
                            onClick = {
                                openReminderDialog.value = false
                            }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(106, 106, 106)
                            )
                        }
                    }
                    CustomTextField(
                        title = "Description",
                        textState = reminderMessage,
                        onTextChange = onReminderTextChange,
                        keyboardType = KeyboardType.Text
                    )
                    val iconOptions = listOf(
                        "Default",
                        "Star",
                        "Flag",
                    )
                    var selectedIcon by remember {
                        mutableStateOf("Default")
                    }
                    val onSelectionChange = { text: String ->
                        selectedIcon = text
                    }
                    Row(
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    ) {
                        Column {
                            Text("Icon:",
                                modifier = Modifier.padding(top = 10.dp, end = 16.dp))
                        }
                        iconOptions.forEach { text ->
                            Column(
                                modifier = Modifier
                                    .padding(
                                        vertical = 0.dp,
                                        horizontal = 4.dp,
                                    ),
                            ) {
                                if(text == "Default") {
                                    Icon(
                                        Icons.Filled.TaskAlt, "Default",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                                            .clickable { onSelectionChange(text) }
                                            .background(if (text == selectedIcon) { Color.LightGray } else { MaterialTheme.colors.background })
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if(text == "Star") {
                                    Icon(
                                        Icons.Filled.Star, "Star",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                                            .clickable { onSelectionChange(text) }
                                            .background(if (text == selectedIcon) { Color.LightGray } else { MaterialTheme.colors.background })
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if(text == "Flag") {
                                    Icon(
                                        Icons.Filled.Flag, "Flag",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                                            .clickable { onSelectionChange(text) }
                                            .background(if (text == selectedIcon) { Color.LightGray } else { MaterialTheme.colors.background })
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row (modifier = Modifier
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {openReminderDialog.value = false },modifier = Modifier.padding(vertical = 0.dp, horizontal = 16.dp)) {
                            Text(text = "Cancel")
                        }
                        Button(onClick = {
                            if (reminderMessage.isNotEmpty()) {
                                val rid = (1..10000000).random()
                                val creationTime = System.currentTimeMillis()
                                viewModel.insertReminder(
                                    Reminder(
                                        rid,
                                        reminderMessage,
                                        selectedIcon,
                                        "",
                                        "",
                                        0,
                                        creationTime,
                                        0,
                                        false
                                    )
                                )
                                searching = false
                            }
                            openReminderDialog.value = false
                            reminderMessage = ""
                        },modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp)) {
                            Text(text = "Add reminder")
                        }
                    }
                }
            }
        }
    }
    Column(modifier = Modifier.padding(vertical = 0.dp, horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(56.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        ) {
            val list = if (searching) searchResults else allReminders
            list.sortedByDescending { it.creation_time } // Doesn't work
            var i = 0

            items(list) { reminder ->
                if(i == 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Doesn't solve scroll area issues
                }
                ReminderCard(id = reminder.rid, message = reminder.message, icon = reminder.icon, created = reminder.creation_time, viewModel)
                if(list.lastIndex == i) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
                i++
            }
        }
    }
}

@Composable
fun ReminderCard(id: Int, message: String, icon: String, created: Long, viewModel: MainViewModel) {

    val openEditReminderDialog = remember { mutableStateOf(false)  }

    var reminderMessage by remember { mutableStateOf(message) }
    var searching by remember { mutableStateOf(false) }

    val onReminderTextChange = { text : String ->
        reminderMessage = text
    }

    val createdTime = Date(created)
    val createdTimeFormatted = SimpleDateFormat("dd.MM.yyyy HH.mm").format(createdTime)

    Card(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 8.dp)
            .fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(corner = CornerSize(12.dp))
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    openEditReminderDialog.value = true
                },
        ) {
            Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)){
                if(icon == "Star") {
                    Icon(
                        Icons.Filled.Star, "Star", tint = Color.Gray,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp),
                    )
                } else if(icon == "Flag") {
                    Icon(
                        Icons.Filled.Flag, "Flag", tint = Color.Gray,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp),
                    )
                } else {
                    Icon(
                        Icons.Filled.TaskAlt, "Reminder", tint = Color.Gray,
                        modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp),
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(vertical = 2.dp, horizontal = 8.dp),
                    text = message //text = "${createdTimeFormatted} ${message}"
                )
            }
        }
    }

    if (openEditReminderDialog.value) {
        Dialog(
            onDismissRequest = {
                openEditReminderDialog.value = false
            }
        ) {
            Surface(
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 24.dp)) {
                    Row {
                        Text("Reminder", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        IconButton(modifier = Modifier.offset(x = 8.dp, y = -12.dp),
                            onClick = {
                                openEditReminderDialog.value = false
                            }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(106, 106, 106)
                            )
                        }
                    }
                    CustomTextField(
                        title = "Description",
                        textState = reminderMessage,
                        onTextChange = onReminderTextChange,
                        keyboardType = KeyboardType.Text
                    )
                    val iconOptions = listOf(
                        "Default",
                        "Star",
                        "Flag",
                    )
                    var selectedIcon by remember {
                        mutableStateOf(icon)
                    }
                    val onSelectionChange = { text: String ->
                        selectedIcon = text
                    }
                    Row(
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    ) {
                        Column {
                            Text("Icon:",
                                modifier = Modifier.padding(top = 10.dp, end = 16.dp))
                        }
                        iconOptions.forEach { text ->
                            Column(
                                modifier = Modifier
                                    .padding(
                                        vertical = 0.dp,
                                        horizontal = 4.dp,
                                    ),
                            ) {
                                if(text == "Default") {
                                    Icon(
                                        Icons.Filled.TaskAlt, "Default",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                                            .clickable { onSelectionChange(text) }
                                            .background(if (text == selectedIcon) { Color.LightGray } else { MaterialTheme.colors.background })
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if(text == "Star") {
                                    Icon(
                                        Icons.Filled.Star, "Star",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                                            .clickable { onSelectionChange(text) }
                                            .background(if (text == selectedIcon) { Color.LightGray } else { MaterialTheme.colors.background })
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if(text == "Flag") {
                                    Icon(
                                        Icons.Filled.Flag, "Flag",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                                            .clickable { onSelectionChange(text) }
                                            .background(if (text == selectedIcon) { Color.LightGray } else { MaterialTheme.colors.background })
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Column {
                        Row (modifier = Modifier
                            .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End) {
                            IconButton(modifier = Modifier.offset(x = -8.dp),
                                onClick = {
                                searching = false
                                viewModel.deleteReminder(reminderMessage)
                                openEditReminderDialog.value = false
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(106, 106, 106)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { openEditReminderDialog.value = false },modifier = Modifier.padding(vertical = 0.dp, horizontal = 16.dp)) {
                                Text(text = "Cancel")
                            }
                            Button(onClick = {
                                if (reminderMessage.isNotEmpty()) {
                                    searching = false
                                    viewModel.editReminder(id, reminderMessage, selectedIcon)
                                    openEditReminderDialog.value = false
                                }
                            },modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp)) {
                                Text(text = "Edit")
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun OverflowMenu(content: @Composable () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showMenu = !showMenu
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "More",
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        content()
    }
}

@Composable
fun CustomTextField(
    title: String,
    textState: String,
    onTextChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = textState,
        onValueChange = { onTextChange(it) },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        singleLine = true,
        label = { Text(title)},
        modifier = Modifier
            .padding(top = 0.dp)
            .fillMaxWidth()
    )
}

@Composable
fun SelectIcon() {
    val options = listOf(
        "Default",
        "Star",
        "Flag",
    )
    var selectedOption by remember {
        mutableStateOf("Default")
    }
    val onSelectionChange = { text: String ->
        selectedOption = text
    }

    Row(
        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
    ) {
        Column {
            Text("Icon:",
            modifier = Modifier.padding(top = 10.dp, end = 16.dp))
        }
        options.forEach { text ->
            Column(
                modifier = Modifier
                    .padding(
                        vertical = 0.dp,
                        horizontal = 4.dp,
                    ),
            ) {
                if(text == "Default") {
                    Icon(
                        Icons.Filled.TaskAlt, "Default",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                            .clickable { onSelectionChange(text) }
                            .background(if (text == selectedOption) { Color.LightGray } else { MaterialTheme.colors.background })
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                    )
                } else if(text == "Star") {
                    Icon(
                        Icons.Filled.Star, "Star",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                            .clickable { onSelectionChange(text) }
                            .background(if (text == selectedOption) { Color.LightGray } else { MaterialTheme.colors.background })
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                    )
                } else if(text == "Flag") {
                    Icon(
                        Icons.Filled.Flag, "Flag",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = 8.dp),)
                            .clickable { onSelectionChange(text) }
                            .background(if (text == selectedOption) { Color.LightGray } else { MaterialTheme.colors.background })
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                    )
                }
            }
        }
    }
}

class MainViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(application) as T
    }
}
