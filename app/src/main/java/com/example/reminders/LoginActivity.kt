package com.example.reminders

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.reminders.ui.theme.RemindersTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("loginCredentials", MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.apply {
            //putString("login_method","")
            //putString("username","mirko")
            //putString("password","test123")
            //putString("pin","1234")
            apply()
        }

        val loginMethod = sharedPref.getString("login_method",null)
        val username = sharedPref.getString("username",null)
        val password = sharedPref.getString("password",null)
        val pin = sharedPref.getString("pin",null)

        var loginError = mutableStateOf("")
        val loginErrorMessage by loginError

        setContent {
            Column {
                TopAppBar(
                    title = { Text(text = "Sign in") }
                )
                if(loginMethod == "pin") {
                    Column(modifier = Modifier.padding(vertical = 32.dp, horizontal = 40.dp)) {
                        if(loginErrorMessage != "") {
                            Text(loginErrorMessage, color = MaterialTheme.colors.error)
                        } else {
                            Text("Please enter PIN", color = Color(74, 74, 74), fontWeight = FontWeight.Bold)
                        }
                        var enteredPin by remember { mutableStateOf("") }
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            value = enteredPin, onValueChange = { enteredPin = it }, label = { Text("PIN") }
                        )
                        Button(modifier = Modifier
                            .padding(vertical = 24.dp, horizontal = 0.dp)
                            .fillMaxWidth(),
                            onClick = {
                                if(enteredPin == pin) {
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent);
                                } else {
                                    loginError.value = "Incorrect PIN"
                                }
                            }) {
                            Text("Sign in")
                        }
                    }
                } else if(loginMethod == "account") {
                    Column(modifier = Modifier.padding(vertical = 32.dp, horizontal = 40.dp)) {
                        if(loginErrorMessage != "") {
                            Text(loginErrorMessage, color = MaterialTheme.colors.error)
                        } else {
                            Text("Please sign in", color = Color(74, 74, 74), fontWeight = FontWeight.Bold)
                        }
                        var enteredUsername by remember { mutableStateOf("") }
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .fillMaxWidth(),
                            value = enteredUsername, onValueChange = { enteredUsername = it }, label = { Text("Username") }
                        )
                        var enteredPassword by remember { mutableStateOf("") }
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            value = enteredPassword, onValueChange = { enteredPassword = it }, label = { Text("Password") }
                        )
                        Button(modifier = Modifier
                            .padding(vertical = 24.dp, horizontal = 0.dp)
                            .fillMaxWidth(),
                            onClick = {
                                if(enteredUsername == username && enteredPassword == password) {
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent);
                                } else {
                                    loginError.value = "Incorrect username or password"
                                }
                            }) {
                            Text("Sign in")
                        }
                    }
                } else {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent);
                }
            }
        }
    }
}