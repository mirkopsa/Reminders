package com.example.reminders

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("loginCredentials", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val loginMethod = sharedPref.getString("login_method",null)
        var selectedLoginMethod = 0

        if(loginMethod == "account") {
            selectedLoginMethod = 0
        } else if(loginMethod == "pin") {
            selectedLoginMethod = 1
        } else {
            selectedLoginMethod = 2
        }

        var loginMethodSetting = mutableStateOf(selectedLoginMethod)
        val loginMethodSettings by loginMethodSetting

        setContent {
            Column {
                TopAppBar(
                    title = { Text(text = "Settings") },
                    navigationIcon = {
                        IconButton(onClick = {
                            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                            startActivity(intent)
                        }) {
                            Icon(Icons.Rounded.ArrowBack, "")
                        }
                    }
                )
                Column (modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {
                    Column (modifier = Modifier.padding(bottom = 12.dp)) {
                        Text("Authentication method", color = Color(74, 74, 74), fontWeight = FontWeight.Bold)
                    }
                    val radioOptions = listOf("account", "pin", "none")
                    val radioLabels = arrayOf("Username and password", "PIN code", "None")

                    var selectedItem by remember {
                        mutableStateOf(radioOptions[selectedLoginMethod])
                    }

                    editor.apply {
                        putString("login_method",selectedItem)
                        apply()
                    }

                    Column(modifier = Modifier.selectableGroup()) {

                        var i = 0
                        radioOptions.forEach { label ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .selectable(
                                        selected = (selectedItem == label),
                                        onClick = {
                                            selectedItem = label
                                            editor.apply {
                                                putString("login_method", selectedItem)
                                                apply()
                                                recreate()
                                            }
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    modifier = Modifier.padding(end = 12.dp),
                                    selected = (selectedItem == label),
                                    onClick = null // null recommended for accessibility with screen readers
                                )
                                Text(text = radioLabels[i])
                                i++
                            }
                        }
                    }
                    Column (modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)) {
                        if (selectedLoginMethod == 0) {
                            Text("Username and password", color = Color(74, 74, 74), fontWeight = FontWeight.Bold)

                            val openUsernamePasswordDialog = remember { mutableStateOf(false)  }

                            TextButton(onClick = {openUsernamePasswordDialog.value = true },modifier = Modifier.padding(vertical = 4.dp, horizontal = 0.dp)) {
                                Text(text = "Change")
                            }

                            if (openUsernamePasswordDialog.value) {
                                Dialog(
                                    onDismissRequest = {
                                        openUsernamePasswordDialog.value = false
                                    }
                                ) {
                                    Surface(
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
                                            Text("Username and password", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(20.dp))
                                            var newUsername by remember { mutableStateOf("") }
                                            OutlinedTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                value = newUsername, onValueChange = { newUsername = it }, label = { Text("New username") }
                                            )
                                            var newPassword by remember { mutableStateOf("") }
                                            OutlinedTextField(
                                                modifier = Modifier
                                                    .padding(top = 16.dp)
                                                    .fillMaxWidth(),
                                                visualTransformation = PasswordVisualTransformation(),
                                                value = newPassword, onValueChange = { newPassword = it }, label = { Text("New password") }
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Row (modifier = Modifier
                                                .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End) {
                                                TextButton(onClick = {openUsernamePasswordDialog.value = false },modifier = Modifier.padding(vertical = 0.dp, horizontal = 16.dp)) {
                                                    Text(text = "Cancel")
                                                }
                                                Button(onClick = {
                                                    openUsernamePasswordDialog.value = false
                                                    editor.apply {
                                                        putString("username", newUsername)
                                                        putString("password", newPassword)
                                                        apply()
                                                        recreate()
                                                    }
                                                },modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp)) {
                                                    Text(text = "Change")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (selectedLoginMethod == 1) {
                            Text("PIN code", color = Color(74, 74, 74), fontWeight = FontWeight.Bold)

                            val openPinDialog = remember { mutableStateOf(false)  }

                            TextButton(onClick = {openPinDialog.value = true },modifier = Modifier.padding(vertical = 4.dp, horizontal = 0.dp)) {
                                Text(text = "Change")
                            }

                            if (openPinDialog.value) {
                                Dialog(
                                    onDismissRequest = {
                                        openPinDialog.value = false
                                    }
                                ) {
                                    Surface(
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
                                            Text("PIN code", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(20.dp))
                                            var newPin by remember { mutableStateOf("") }
                                            OutlinedTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                visualTransformation = PasswordVisualTransformation(),
                                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                                value = newPin, onValueChange = { newPin = it }, label = { Text("Enter new PIN") }
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Row (modifier = Modifier
                                                .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End) {
                                                TextButton(onClick = {openPinDialog.value = false },modifier = Modifier.padding(vertical = 0.dp, horizontal = 16.dp)) {
                                                    Text(text = "Cancel")
                                                }
                                                Button(onClick = {
                                                                    openPinDialog.value = false
                                                                    editor.apply {
                                                                        putString("pin", newPin)
                                                                        apply()
                                                                        recreate()
                                                                    }
                                                                 },modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp)) {
                                                    Text(text = "Change")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}