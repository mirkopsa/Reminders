package com.example.reminders

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reminders.ui.theme.RemindersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("loginCredentials", MODE_PRIVATE)
        val loginMethod = sharedPref.getString("login_method",null)

        setContent {
            Column {
                TopAppBar(
                    title = {
                        Text(text = "Reminders")
                    },
                    actions = {
                        OverflowMenu {
                            DropdownMenuItem(onClick = {
                                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                                startActivity(intent);
                            }) {
                                Text("Settings")
                            }
                            if(loginMethod == "account" || loginMethod == "pin") {
                                DropdownMenuItem(onClick = {
                                    val intent =
                                        Intent(this@MainActivity, LoginActivity::class.java)
                                    startActivity(intent);
                                }) {
                                    Text("Sign out")
                                }
                            }
                        }
                    }
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    LazyColumn {
                        item {
                            Card(
                                modifier = Modifier
                                    .padding(top = 4.dp, bottom = 8.dp)
                                    .fillMaxWidth(),
                                elevation = 2.dp,
                                //backgroundColor = Color.White,
                                shape = RoundedCornerShape(corner = CornerSize(12.dp))
                            ) {
                                Text(
                                    modifier = Modifier.padding(
                                        vertical = 16.dp,
                                        horizontal = 24.dp
                                    ), text = "Reminder 1"
                                )
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .padding(top = 4.dp, bottom = 8.dp)
                                    .fillMaxWidth(),
                                elevation = 2.dp,
                                //backgroundColor = Color.White,
                                shape = RoundedCornerShape(corner = CornerSize(12.dp))
                            ) {
                                Text(
                                    modifier = Modifier.padding(
                                        vertical = 16.dp,
                                        horizontal = 24.dp
                                    ), text = "Reminder 2"
                                )
                            }
                        }
                    }
                }
            }
            floatingActionButton()
        }
    }
}

@Composable
fun floatingActionButton() {
    Box(Modifier.fillMaxSize()) {
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add reminder",
                tint = Color.White,
            )
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