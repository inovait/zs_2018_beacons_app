package si.inova.zimskasola

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.zimskasola.R
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.nlopez.smartlocation.OnActivityUpdatedListener
import io.nlopez.smartlocation.OnGeofencingTransitionListener
import io.nlopez.smartlocation.OnLocationUpdatedListener
import io.nlopez.smartlocation.SmartLocation
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import java.util.*

const val TAG: String = "MyAppBeacons"
const val PREFS_KEY: String = "MyAppBeaconsData"

class ActivityMain: AppCompatActivity(), MrBeacon.Listener, OnLocationUpdatedListener, OnActivityUpdatedListener,
    OnGeofencingTransitionListener {

    val PREFS_FILENAME = "si.inova.zimskasola.prefs"
    var pogostostObiska: GraphData = GraphData()
    var lastTimeNotedHour: Int? = null

    val firestorage = MrFirestorage()
    val geofence = MrGeofence(this)
    val beaconScanner = MrBeacon(this, this, this)

    var lastBeacon: String = ""
    var currentBeacon: String = ""
    var currentBuilding: Building? = null
    var currentFloor: Floor? = null
    var currentRoom: Room? = null

    var provider: LocationGooglePlayServicesProvider = LocationGooglePlayServicesProvider()
    var smartLocation: SmartLocation = SmartLocation.Builder(this).logging(true).build()

    private val fragmentMyLocationLoading = FragmentLoading()
    private val fragmentMyLocation = FragmentMyLocation()
    private val fragmentAllLocations = FragmentPlaces()
    private val fragmentSettings = FragmentSettings()
    private val fragmentError = FragmentError()

    private var bootingUp = false

    private val context = this

    private var emptySaveFile: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // shared prefs za merjenje pogostosti obiska prostora
        /*val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        if (prefs.contains(PREFS_KEY)) {
            val gson = Gson()
            pogostostObiska = gson.fromJson(prefs.getString(PREFS_KEY, ""), GraphData::class.java)
            Log.d(TAG, "I found saved data:\n$pogostostObiska")
            emptySaveFile = false
        } else {
            emptySaveFile = true

        }*/
        createNotificationChannel()
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectListener)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = askPermission(
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_NETWORK_STATE
                )
                Log.d(TAG, "Permissions accepted ${result.accepted}")
                bootingUp = true
                initialize()
            } catch (e: PermissionException) {
                if (e.hasDenied()) {
                    Log.d(TAG, "Permissions denied")
                    AlertDialog.Builder(this@ActivityMain)
                        .setMessage("Please accept permissions in order to use Beacons app.")
                        .setPositiveButton("OK") { _, _ -> e.askAgain() }
                        .setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                if (e.hasForeverDenied()) {
                    Log.d(TAG, "Permissions forever denied")
                    e.goToSettings()
                }
            }
        }
    }

    fun initialize() {
        switchFragment(3) // shimmer loading effect
        CoroutineScope(Dispatchers.Default).launch {
            if (isNetworkAvailable() && setBluetooth(true) && gpsStatusCheck()) {
                runOnUiThread {
                    Toast.makeText(context, "Povezujem se s strežnikom...", Toast.LENGTH_SHORT).show()
                }
                getData()
            } else {
                switchFragment(4) // error
            }
        }
    }

    private fun getData() {
        Log.d(TAG, "getData")
        firestorage.downloadJsonObject()
        while (!firestorage.downloadSuccess) {
            if (firestorage.downloadFailed) {
                break
            }
        }
        if (firestorage.downloadSuccess) {
            Log.d(TAG, "success")
            startLocation()
        }
        if (firestorage.downloadFailed) {
            Log.d(TAG, "fail")
            runOnUiThread {
                AlertDialog.Builder(this@ActivityMain)
                    .setMessage("Prišlo je do napake pri komunikaciji s strežnikom. Poskusim ponovno?")
                    .setPositiveButton("Da") { dialog, _ ->
                        dialog.dismiss()
                        initialize()
                    }
                    .setNegativeButton("Ne") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun saveData(data: GraphData) {
        val prefs = this.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs.edit()
        val gson = Gson()
        val serialized = gson.toJson(data)
        editor.putString(PREFS_KEY, serialized)
        editor.apply()
        Log.d(TAG, "I saved data: \n$serialized")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Beacons id", "Beacons channel", importance)
                .apply { description = "Beacons description" }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setBluetooth(enable: Boolean): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = bluetoothAdapter.isEnabled
        if (enable && !isEnabled) {
            Log.d(TAG, "Bluetooth is enabled")
            return bluetoothAdapter.enable()
        } else if (!enable && isEnabled) {
            Log.d(TAG, "Bluetooth is disabled")
            return bluetoothAdapter.disable()
        }
        Log.d(TAG, "Bluetooth is enabled")
        return true
    }

    private fun gpsStatusCheck(): Boolean {
        val manager =  getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "GPS is disabled")
            false
        } else {
            Log.d(TAG, "GPS is enabled")
            true
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected) {
            Log.d(TAG, "Internet is enabled")
            true
        } else {
            Log.d(TAG, "Internet is disabled")
            false
        }
    }

    override fun onStop() {
        super.onStop()
        lastBeacon = currentBeacon
    }

    override fun onStart() {
        super.onStart()
        //beaconScanner.start()
        if (lastBeacon != currentBeacon) {
            refreshFragment(0)
        }
        /* check if we opened app from notification, doesnt work :(
        Log.d(TAG, "ONSTART()")
        val intent = this.intent
        if ((intent?.extras != null) && (intent?.extras.containsKey("EXTRAID")) && ((intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0)) {
            Log.d(TAG, "BACK FROM NOTIFICATION ")
            var jobID = intent.extras?.getInt("EXTRAID")
            if (jobID == 1234) {
                Log.d(TAG, "BACK FROM NOTIFICATION JOBID")
                switchFragment(0)
            }
        }*/
    }

    override fun onBeaconFound(data : String) {
        if ((currentBuilding != null) && (currentBeacon != data)) {
            Log.d(TAG, "CURRENT BEACON ID: $data")
            lastBeacon = currentBeacon
            currentBeacon = data

            currentFloor = currentBuilding?.floors?.find { floor ->
                floor.rooms.contains(floor.rooms.find { room ->
                    room.beacon_id == currentBeacon
                })
            }

            currentRoom = currentFloor?.rooms?.single { room ->
                room.beacon_id == currentBeacon
            }

            if (bootingUp && (currentBeacon != lastBeacon)) {
                Log.d(TAG, "Switching to my location fragment")
                switchFragment(0)
                bootingUp = false
            } else {
                val notificationIntent = Intent(this, ActivityMain::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                notificationIntent.putExtra("EXTRAID", 1234)

                val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
                val mBuilder = NotificationCompat.Builder(this, "Beacons id")
                    .setSmallIcon(R.drawable.notification_icon_background)
                    .setContentTitle(currentRoom?.name)
                    .setContentText("Zaznali smo nov prostor. Preveri njegove detajle.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                with(NotificationManagerCompat.from(this)) { notify(1234, mBuilder.build()) }

                // shrani obisk, če ga še nismo
                /*var rightNow = Calendar.getInstance()
                var currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY)
                if (lastTimeNotedHour == null || lastTimeNotedHour != currentHourIn24Format) {
                    // dobi obisk sobe, v kateri smo
                    if (emptySaveFile == true) {
                        // ustvari nov
                        var mapiraj = mutableMapOf<Int, Int>()
                        for (i in 0..23) {
                            mapiraj[i] = 0
                        }
                        for (floor in currentBuilding!!.floors) {
                            for (room in floor.rooms) {
                                pogostostObiska.data.add(Obisk(room.name, mapiraj))
                            }
                        }
                    } else {
                        var obisk = pogostostObiska.data.find {
                            it.imeSobe == currentRoom?.name
                        }
                        obisk?.obisk!![currentHourIn24Format] = +1
                    }
                    lastTimeNotedHour = currentHourIn24Format
                    saveData(pogostostObiska)
                }*/
            }
        }
    }

    override fun onBeaconLost(data : String) {
        //tvBeaconAttachment.setText(R.string.unknown_location)
    }

    override fun onLocationUpdated(location: Location) {
        showLocation(location)
    }

    override fun onActivityUpdated(detectedActivity: DetectedActivity) {
        showActivity(detectedActivity)
    }

    override fun onGeofenceTransition(geofence: TransitionGeofence) {
        showGeofence(geofence.geofenceModel.toGeofence(), geofence.transitionType)
    }

    private fun startLocation() {
        // testing start
        // ustvari ograjo okol moje tesne lokacije, drugače je treba ta naslov prebrat iz jsona (naslov stavbe)
        geofence.create("Tyrševa 30, Maribor")
        while (!geofence.geocodingSuccess) {
            if (geofence.geocodingFailed) {
                break
            }
        }
        if (geofence.geocodingSuccess) {
            // prižgi ograjo (myFences[0] ker je bila narejena samo ena, če jih je več je treba vse prižgat
            // in potem ob prehodu v tisto ograjo aplikacija zazna v kateri stavbi se nahajamo
            smartLocation.geofencing().add(geofence.myFences[0]).start(context)
            smartLocation.location(provider).start(context)
            smartLocation.activity().start(context)
        }
        if (geofence.geocodingFailed) {
            geofence.geocodingSuccess = false
            geofence.geocodingFailed = false
            startLocation()
        }
        // testing end

        // če mamo več stavb je mormo za vsako posebi nardit ograjo
        /* if (firestorage.buildings != null) {
            for (building in firestorage.buildings!!) {
                geofence.create(building.description)
                while (!geofence.geocodingSuccess) {
                    if (geofence.geocodingFailed) {
                        break
                    }
                }
                if (geofence.geocodingSuccess) {
                    if (geofence.myFences.size > 0) {
                        smartLocation.geofencing().add(geofence.myFences[geofence.myFences.size-1]).start(context)
                        smartLocation.location(provider).start(context)
                        smartLocation.activity().start(context)
                    } else {
                        Log.d(TAG, "Unexpected geofencing error. There were no fences in the array.")
                    }
                }
                if (geofence.geocodingFailed) {
                    geofence.geocodingSuccess = false
                    geofence.geocodingFailed = false
                    startLocation()
                    break
                }
            }
            for (fence in geofence.myFences) {
                smartLocation.geofencing().add(fence).start(context)
            }
        } else {
            Log.d("ActivityMain", "Hmmmmmm firestorage is empty but it shouldn't be")
        }
        */
    }

    private fun stopLocation() {
        SmartLocation.with(this).location().stop()
        Log.d("ActivityMain", "Location stopped!")

        SmartLocation.with(this).activity().stop()
        Log.d("ActivityMain", "Activity stopped!")

        SmartLocation.with(this).geofencing().stop()
        Log.d("ActivityMain", "Geofencing stopped!")
    }

    private fun showLocation(location: Location?) {
        if (location != null) {
            val text = String.format(
                "Latitude %.6f, Longitude %.6f",
                location.latitude,
                location.longitude
            )
            Log.d(TAG, "Current location: $text")
            // preveri če se nahajamo znotraj naših ograj
            for (fence in geofence.myFences) {
                val fenceLocation = Location(LocationManager.GPS_PROVIDER)
                fenceLocation.longitude = fence.longitude
                fenceLocation.latitude = fence.latitude
                val inside = geofence.currentlyInside(location, fenceLocation)
                if (inside != null) {
                    Log.d(TAG, "Inside of the geofence: ${fence.toGeofence()}!")

                    /*
                    // v bazi poišči stavbo, ki ima naslov, ki je znotraj te ograje

                    currentBuilding = firestorage.buildings?.find { building ->
                        building.description == fence.requestId
                    }
                    */

                    // testing start
                    // hardcoded prva stavba po vrsti (ker mamo itak samo eno)
                    currentBuilding = firestorage.buildings?.get(0)
                    // testing end

                    runOnUiThread {
                        tvMainHeader.text = currentBuilding?.title
                        tvMainSubHeader.text = currentBuilding?.description
                        Toast.makeText(this, "Iščem prostor, kjer se nahajaš...", Toast.LENGTH_SHORT).show()
                    }
                    beaconScanner.start()
                } else {
                    Log.d(TAG, "Outside of the geofence!")
                }
            }
        } else {
            Log.d(TAG, "Null location")
        }
    }

    private fun showActivity(detectedActivity: DetectedActivity?) {
        if (detectedActivity != null) {
            Log.d(TAG, "Activity ${getNameFromType(detectedActivity)} with ${detectedActivity.confidence} confidence")
        } else {
            Log.d(TAG, "Null activity")
        }
    }

    private fun showGeofence(geofence: Geofence?, transitionType: Int) {
        if (geofence != null) {
            Log.d(TAG, "Transition" + getTransitionNameFromType(transitionType) + " for Geofence with id = " + geofence.requestId)
        } else {
            Log.d(TAG, "Null geofence")
        }
    }

    private fun getTransitionNameFromType(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "enter"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "exit"
            else -> "dwell"
        }
    }

    private fun getNameFromType(activityType: DetectedActivity): String {
        return when (activityType.type) {
            DetectedActivity.IN_VEHICLE -> "in_vehicle"
            DetectedActivity.ON_BICYCLE -> "on_bicycle"
            DetectedActivity.ON_FOOT -> "on_foot"
            DetectedActivity.STILL -> "still"
            DetectedActivity.TILTING -> "tilting"
            else -> "unknown"
        }
    }

    private fun getFragments() : Array<Fragment> {
        fragmentMyLocation.activityMain = this
        fragmentAllLocations.activityMain = this
        fragmentSettings.activityMain = this
        return arrayOf(fragmentMyLocation, fragmentAllLocations, fragmentSettings, fragmentMyLocationLoading, fragmentError)
    }

    private fun switchFragment(pos : Int) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayoutFragments, getFragments()[pos])
            .commit()
    }

    private fun refreshFragment(pos : Int) {
        supportFragmentManager
            .beginTransaction()
            .detach(getFragments()[pos])
            .attach(getFragments()[pos])
            .commit()
    }

    private val navigationItemSelectListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_my_location -> {
                tvLogout.visibility = View.GONE
                switchFragment(0)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_all_locations -> {
                tvLogout.visibility = View.GONE
                switchFragment(1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_settings -> {
                tvLogout.visibility = View.VISIBLE
                switchFragment(2)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

}

