package com.example.reminders

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    var editReminderId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("loginCredentials", MODE_PRIVATE)
        val loginMethod = sharedPref.getString("login_method", null)

        val locSharedPref = getSharedPreferences("virtualLocation", MODE_PRIVATE)
        val locEditor = locSharedPref.edit()

        /*locEditor.apply {
            val latDouble = 65.059167
            putLong("lat", latDouble.toRawBits())
            val lngDouble = 25.466111
            putLong("lng", lngDouble.toRawBits())
            apply()
        }*/

        val virtualLat = Double.fromBits(locSharedPref.getLong("lat", 0))
        val virtualLng = Double.fromBits(locSharedPref.getLong("lng", 0))
        println(virtualLat)
        print(virtualLng)

        val radiusInDegrees = 300 / 111000f

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

                var showAll by remember {
                    mutableStateOf(false)
                }

                var permissionGranted by remember {
                    mutableStateOf(isPermissionGranted())
                }

                val permissionLauncher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { permissionGranted_ ->
                        Toast.makeText(
                            this@MainActivity,
                            "permissionGranted_ $permissionGranted_",
                            Toast.LENGTH_SHORT
                        ).show()
                        permissionGranted = permissionGranted_
                    }

                /*Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            enabled = !permissionGranted,
                            onClick = {
                                if (!permissionGranted) {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                }
                            }) {
                            Text(text = if (permissionGranted) "Location tracking enabled" else "Enable location tracking")
                        }

                        if (permissionGranted) {
                            Toast.makeText(
                                this@MainActivity,
                                "Permission granted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }*/

                Column {
                    TopAppBar(
                        title = {
                            Text(text = "Reminders")
                        },
                        actions = {
                            if(showAll) {
                                IconButton(onClick = {
                                    showAll = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.VisibilityOff,
                                        contentDescription = "Hide future reminders",
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    showAll = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = "Show all",
                                    )
                                }
                            }
                            OverflowMenu {
                                DropdownMenuItem(onClick = {
                                    val intent =
                                        Intent(this@MainActivity, LocationActivity::class.java)
                                    startActivity(intent)
                                }) {
                                    Text("Select location")
                                }
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

                ScreenSetup(viewModel, this@MainActivity, showAll, virtualLat, virtualLng)
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

}

@Composable
fun ScreenSetup(viewModel: MainViewModel, activity: Activity, showAll: Boolean, virtualLat: Double, virtualLng: Double) {

    val allReminders by viewModel.allReminders.observeAsState(listOf())
    val searchResults by viewModel.searchResults.observeAsState(listOf())

    MainScreen(
        allReminders = allReminders,
        searchResults = searchResults,
        viewModel = viewModel,
        activity = activity,
        context = LocalContext.current,
        showAll = showAll,
        virtualLat = virtualLat,
        virtualLng = virtualLng
    )
}

@Composable
fun MainScreen(
    allReminders: List<Reminder>,
    searchResults: List<Reminder>,
    viewModel: MainViewModel,
    activity: Activity,
    context: Context,
    showAll: Boolean,
    virtualLat: Double,
    virtualLng: Double
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
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        Column {
                            Text(
                                "Icon:",
                                modifier = Modifier.padding(top = 10.dp, end = 16.dp)
                            )
                        }
                        iconOptions.forEach { text ->
                            Column(
                                modifier = Modifier
                                    .padding(
                                        vertical = 0.dp,
                                        horizontal = 4.dp,
                                    ),
                            ) {
                                if (text == "Default") {
                                    Icon(
                                        Icons.Filled.TaskAlt, "Default",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp))
                                            .clickable { onSelectionChange(text) }
                                            .background(
                                                if (text == selectedIcon) {
                                                    Color.LightGray
                                                } else {
                                                    MaterialTheme.colors.background
                                                }
                                            )
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if (text == "Star") {
                                    Icon(
                                        Icons.Filled.Star, "Star",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp))
                                            .clickable { onSelectionChange(text) }
                                            .background(
                                                if (text == selectedIcon) {
                                                    Color.LightGray
                                                } else {
                                                    MaterialTheme.colors.background
                                                }
                                            )
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if (text == "Flag") {
                                    Icon(
                                        Icons.Filled.Flag, "Flag",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp))
                                            .clickable { onSelectionChange(text) }
                                            .background(
                                                if (text == selectedIcon) {
                                                    Color.LightGray
                                                } else {
                                                    MaterialTheme.colors.background
                                                }
                                            )
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    val calendar = Calendar.getInstance()
                    var selectedDateText by remember { mutableStateOf("") }
                    val year = calendar[Calendar.YEAR]
                    val month = calendar[Calendar.MONTH]
                    val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
                    var selectedTimeText by remember { mutableStateOf("") }
                    val hour = calendar[Calendar.HOUR_OF_DAY]
                    val minute = calendar[Calendar.MINUTE]

                    val datePicker = DatePickerDialog(
                        context,
                        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                            val d = "%02d".format(selectedDayOfMonth)
                            val m = "%02d".format(selectedMonth + 1)
                            selectedDateText = "$d/$m/$selectedYear"
                        }, year, month, dayOfMonth
                    )
                    datePicker.datePicker.minDate = calendar.timeInMillis

                    Column() {
                        Text(
                            text = if (selectedDateText.isNotEmpty()) {
                                "Due date $selectedDateText"
                            } else {
                                "Due date not selected"
                            }
                        )

                        OutlinedButton(
                            onClick = {
                                datePicker.show()
                            }
                        ) {
                            Text(text = "Select date")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val timePicker = TimePickerDialog(
                        context,
                        { _, selectedHour: Int, selectedMinute: Int ->
                            val h = "%02d".format(selectedHour)
                            val s = "%02d".format(selectedMinute)
                            selectedTimeText = "$h:$s"
                        }, hour, minute, true
                    )

                    Column() {
                        Text(
                            text = if (selectedTimeText.isNotEmpty()) {
                                "Due time $selectedTimeText"
                            } else {
                                "Due time not selected"
                            }
                        )

                        OutlinedButton(
                            onClick = {
                                timePicker.show()
                            }
                        ) {
                            Text(text = "Select time")
                        }
                    }

                    var reminderTime = System.currentTimeMillis()
                    if (selectedDateText.isNotEmpty() && selectedTimeText.isNotEmpty()) {

                        var dateString = "$selectedDateText $selectedTimeText:00";
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                        val localDateTime = LocalDateTime.parse(dateString, formatter)

                        val zdt: ZonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
                        reminderTime = zdt.toInstant().toEpochMilli()
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    var reminderLatitude by remember { mutableStateOf(0.00) }
                    var reminderLongitude by remember { mutableStateOf(0.00) }
                    val openReminderLocationDialog = remember { mutableStateOf(false)  }

                    Column() {
                        Text(
                            text = if (reminderLatitude != 0.00) {
                                "Location not selected"
                            } else {
                                "Location selected"
                            }
                        )
                        OutlinedButton(
                            onClick = {
                                openReminderLocationDialog.value = true
                            }
                        ) {
                            Text(text = "Select location")
                        }
                    }

                    if (openReminderLocationDialog.value) {
                        Dialog(
                            onDismissRequest = {
                                openReminderLocationDialog.value = false
                            }
                        ) {
                            Surface(
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                var markerPos by remember { mutableStateOf(LatLng(virtualLat, virtualLng)) }
                                val cameraPositionState = rememberCameraPositionState {
                                    position = CameraPosition.fromLatLngZoom(markerPos, 10f)
                                }
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    //properties = MapProperties(isMyLocationEnabled = true),
                                    onMapClick = {
                                        markerPos = it
                                        println("latlong")
                                        println(markerPos)
                                    }
                                ) {
                                    Marker(
                                        state = MarkerState(position = markerPos),
                                        draggable = true,
                                        title = "Reminder location"
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row (modifier = Modifier
                                    .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = {openReminderLocationDialog.value = false },modifier = Modifier.padding(vertical = 0.dp, horizontal = 16.dp)) {
                                        Text(text = "Cancel")
                                    }
                                    Button(onClick = {
                                        openReminderLocationDialog.value = false
                                        reminderLatitude = markerPos.latitude;
                                        reminderLongitude = markerPos.longitude;
                                    },modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp)) {
                                        Text(text = "OK")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
                                        reminderLatitude,
                                        reminderLongitude,
                                        reminderTime,
                                        creationTime,
                                        0,
                                        false
                                    )
                                )
                                searching = false
                                reminderGeofence(activity, context, rid, reminderLatitude, reminderLongitude)
                                reminderNotification(context, reminderMessage, reminderLatitude, reminderLongitude, reminderTime, creationTime)
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
                //print("Current time ")
                //println(System.currentTimeMillis())
                print("Time ")
                println(reminder.reminder_time - System.currentTimeMillis())
                if(System.currentTimeMillis() > reminder.reminder_time || showAll) {

                    var reminderLat = reminder.location_x ?: virtualLat
                    if(reminder.location_x == 0.00) {
                        reminderLat = virtualLat
                    }
                    var reminderLng = reminder.location_y ?: virtualLng
                    if(reminder.location_y == 0.00) {
                        reminderLng = virtualLng
                    }
                    print("Lat ")
                    println(reminderLat)
                    print("Lng ")
                    println(reminderLng)
                    val distance = getDistanceBetween(reminderLat, reminderLng, virtualLat, virtualLng)
                    print("Distance ")
                    println(distance)
                    if(300 > distance || showAll) {
                        ReminderCard(
                            id = reminder.rid,
                            message = reminder.message,
                            icon = reminder.icon,
                            created = reminder.creation_time,
                            viewModel
                        )
                    }
                }
                if(list.lastIndex == i) {
                    Spacer(modifier = Modifier.height(80.dp))
                }
                i++

            }
        }
    }
}

fun getDistanceBetween(
    destinationLatitude: Double,
    destinationLongitude: Double,
    latitude: Double,
    longitude: Double
): Int {
    val locationA = Location("point A")
    locationA.latitude = destinationLatitude
    locationA.longitude = destinationLongitude
    val locationB = Location("point B")
    locationB.latitude = latitude
    locationB.longitude = longitude
    val distance = locationA.distanceTo(locationB)
    return distance.toInt()
}

fun reminderGeofence(activity: Activity, context: Context, rid: Int, reminderLatitude: Double, reminderLongitude: Double) {

    val requestId = "request$rid"

    val geofence = Geofence.Builder()
        .setRequestId(requestId)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setCircularRegion(reminderLatitude, reminderLongitude, 300F)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()

    val geofencingRequest = GeofencingRequest.Builder().apply {
        setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        addGeofence(geofence)
    }.build()

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity, GeofenceReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    val geofencingClient = LocationServices.getGeofencingClient(activity)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
        addOnFailureListener {
            println("addOnFailureListener")
        }
        addOnSuccessListener {
            println("addOnSuccessListener")
        }
    }

}

fun geofenceReminderNotification(context: Context, rid: String) {

    var reminderId = rid.filter { it.isDigit() }
    val reminderWorkerRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInputData(
            Data.Builder()
                .putString("reminderMessage", "You've arrived in the location")
                .build()
        )
        .build()
    WorkManager.getInstance(context).enqueue(reminderWorkerRequest)

}

fun reminderNotification(context: Context, message: String, reminderLatitude: Double, reminderLongitude: Double, reminderTime: Long, creationTime: Long) {

    val delay = reminderTime - creationTime
    val reminderWorkerRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInputData(
            Data.Builder()
                .putString("reminderMessage", message)
                .putDouble("reminderLatitude", reminderLatitude)
                .putDouble("reminderLongitude", reminderLongitude)
                .build()
        )
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()
    WorkManager.getInstance(context).enqueue(reminderWorkerRequest)

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
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
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
                                            .clip(shape = RoundedCornerShape(size = 8.dp))
                                            .clickable { onSelectionChange(text) }
                                            .background(
                                                if (text == selectedIcon) {
                                                    Color.LightGray
                                                } else {
                                                    MaterialTheme.colors.background
                                                }
                                            )
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if(text == "Star") {
                                    Icon(
                                        Icons.Filled.Star, "Star",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp))
                                            .clickable { onSelectionChange(text) }
                                            .background(
                                                if (text == selectedIcon) {
                                                    Color.LightGray
                                                } else {
                                                    MaterialTheme.colors.background
                                                }
                                            )
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                    )
                                } else if(text == "Flag") {
                                    Icon(
                                        Icons.Filled.Flag, "Flag",
                                        modifier = Modifier
                                            .clip(shape = RoundedCornerShape(size = 8.dp))
                                            .clickable { onSelectionChange(text) }
                                            .background(
                                                if (text == selectedIcon) {
                                                    Color.LightGray
                                                } else {
                                                    MaterialTheme.colors.background
                                                }
                                            )
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
            imageVector = Icons.Filled.MoreVert,
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
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
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
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .clickable { onSelectionChange(text) }
                            .background(
                                if (text == selectedOption) {
                                    Color.LightGray
                                } else {
                                    MaterialTheme.colors.background
                                }
                            )
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                    )
                } else if(text == "Star") {
                    Icon(
                        Icons.Filled.Star, "Star",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .clickable { onSelectionChange(text) }
                            .background(
                                if (text == selectedOption) {
                                    Color.LightGray
                                } else {
                                    MaterialTheme.colors.background
                                }
                            )
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                    )
                } else if(text == "Flag") {
                    Icon(
                        Icons.Filled.Flag, "Flag",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .clickable { onSelectionChange(text) }
                            .background(
                                if (text == selectedOption) {
                                    Color.LightGray
                                } else {
                                    MaterialTheme.colors.background
                                }
                            )
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
