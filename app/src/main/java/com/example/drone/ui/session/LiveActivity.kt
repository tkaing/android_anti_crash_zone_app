package com.example.drone.ui.session

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.drone.R
import com.example.drone.data.AppConfig.Companion.API_URL
import com.example.drone.data.api.factory.SessionDetailsFactory
import com.example.drone.data.api.factory.SessionFactory
import com.example.drone.data.api.factory.VolleyFactory
import com.example.drone.data.api.model.SessionDetails
import com.example.drone.data.mqtt.ClientMQTT
import com.example.drone.data.storage.SessionStorage
import com.example.drone.data.storage.UserStorage
import com.example.drone.ui.authentication.ProfileActivity
import com.example.drone.ui.list.DetailsAdapter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_live.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject


class LiveActivity : AppCompatActivity(), OnMapReadyCallback, PermissionListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var countCollision = 0

    companion object {
        const val REQUEST_CHECK_SETTINGS = 43
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live)

        setupMapView()
        setupRecyclerView()
        setupTitleTextView()

        updateTitleTextView()
        updateTimeAndSpeedAndCollisionButton(null)

        setupClientMQTT()
        updateLiveTextView()
        setupLaunchStopButton()
        updateLaunchStopButton()
    }

    private fun updateCollisionCount(distance: Int)
    {
        if (distance <= 15) {
            countCollision += 1
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(200)
            }
        }
    }

    private fun updateLiveTextView()
    {
        if (SessionStorage.isActive()) {
            text_view_live.visibility = View.VISIBLE
            text_view_live.setOnClickListener {
                val intent = Intent(this, LiveActivity::class.java)
                startActivity(intent)
            }
        } else {
            text_view_live.visibility = View.INVISIBLE
        }
    }

    private fun updateTitleTextView()
    {
        val sessionId = SessionStorage.currentSession.id
        text_view_title.text = if (SessionStorage.isActive()) "En Cours - #$sessionId" else "Nouveau Parcours"
    }

    private fun updateTimeAndSpeedAndCollisionButton(details: SessionDetails?)
    {
        if (details != null) {
            val speed = details.speed
            val distance = details.distance
            updateCollisionCount(distance)
            button_time.text = SessionDetailsFactory.getFormattedDateNow()
            button_speed.text = "$speed m/s"
            if (countCollision > 0)
                button_collision.text = "$countCollision collisions"
        } else {
            button_time.text = "--:--"
            button_speed.text = "vitesse"
            button_collision.text = "0 collision"
        }
    }

    private fun setupTitleTextView() {
        text_view_title.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
    private fun setupMapView() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = FusedLocationProviderClient(this)
    }
    private fun setupRecyclerView()
    {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = DetailsAdapter(SessionStorage.detailsArray)
    }
    private fun setupClientMQTT()
    {
        val clientMQTT = ClientMQTT(applicationContext)
        val clientMQTTTag = "CLIENT MQTT"

        clientMQTT.mqttAndroidClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String?) {
                Log.w(clientMQTTTag, "CONNECTED!")
            }

            override fun connectionLost(throwable: Throwable?) {
                Log.w(clientMQTTTag, "DISCONNECTED!")
            }

            override fun messageArrived(topic: String?, mqttMessage: MqttMessage) {
                Log.w(clientMQTTTag, "MESSAGE ARRIVED!")
                if (SessionStorage.isActive()) {
                    val payload = JSONObject(String(mqttMessage.payload))
                    val details = SessionDetailsFactory.mapFromPayload(payload)
                    storeDetails(details)
                    SessionStorage.detailsArray.add(details)
                    updateTimeAndSpeedAndCollisionButton(details)
                    recycler_view.adapter = DetailsAdapter(SessionStorage.detailsArray.asReversed())

                }
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken?) {
                Log.w(clientMQTTTag, "MESSAGE COMPLETED!")
            }
        })
    }

    private fun updateLaunchStopButton()
    {
        val color = if (!SessionStorage.isActive()) R.color.colorAccent else R.color.colorRed
        val buttonContent = if (!SessionStorage.isActive()) "Démarrer" else "Arrêter"
        button_launch_stop.text = buttonContent
        button_launch_stop.setBackgroundColor(ContextCompat.getColor(applicationContext, color))
    }

    private fun setupLaunchStopButton()
    {
        button_launch_stop.setOnClickListener {
            val color = if (SessionStorage.isActive()) R.color.colorAccent else R.color.colorRed
            val buttonContent = if (SessionStorage.isActive()) "Démarrer" else "Arrêter"
            if (!SessionStorage.isActive()) {
                button_launch_stop.isEnabled = false
                val url = "${API_URL}/session/cmd/store"
                val map = HashMap<String,String>()
                map["userId"] = UserStorage.loggedUser.id.toString()
                val parameters = JSONObject(map as Map<String, String>)
                val volleyRequest = Volley.newRequestQueue(this)
                val jsonObjectRequest = object :
                    JsonObjectRequest(Method.POST, url, parameters, Response.Listener { jsonResponse ->
                        button_launch_stop.isEnabled = true
                        button_launch_stop.text = buttonContent
                        button_launch_stop.setBackgroundColor(ContextCompat.getColor(applicationContext, color))
                        val session = SessionFactory.mapFromJSON(jsonResponse)
                        SessionStorage.currentSession = session
                        updateLiveTextView()
                        updateTitleTextView()
                    }, Response.ErrorListener { error ->
                        button_launch_stop.isEnabled = true
                        val message = VolleyFactory.getMessage(error)
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }) {
                    override fun getBodyContentType(): String {
                        return "application/json"
                    }
                }
                volleyRequest.add(jsonObjectRequest)
            } else {
                button_launch_stop.text = buttonContent
                button_launch_stop.setBackgroundColor(ContextCompat.getColor(applicationContext, color))
                SessionStorage.currentSession = SessionStorage.defaultSession
                updateLiveTextView()
                updateTitleTextView()
                updateTimeAndSpeedAndCollisionButton(null)
                SessionStorage.detailsArray.clear()
                recycler_view.adapter = DetailsAdapter(SessionStorage.detailsArray)
            }
        }
    }

    private fun storeDetails(details: SessionDetails)
    {
        if (SessionStorage.isActive())
        {
            val url = "${API_URL}/details/cmd/store"
            val map = HashMap<String,String>()
            map["speed"] = details.speed.toString()
            map["distance"] = details.distance.toString()
            map["sessionId"] = SessionStorage.currentSession.id.toString()
            val parameters = JSONObject(map as Map<String, String>)
            val volleyRequest = Volley.newRequestQueue(this)
            val jsonObjectRequest = object :
                JsonObjectRequest(Method.POST, url, parameters, Response.Listener { jsonResponse ->
                }, Response.ErrorListener { error ->
                    val message = VolleyFactory.getMessage(error)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }) {
                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            volleyRequest.add(jsonObjectRequest)
        }
    }

    private fun fetchDetails()
    {
        if (SessionStorage.isActive())
        {
            val session = SessionStorage.currentSession
            val url = "${API_URL}/details/query/list/session/1"
            val volleyRequest = Volley.newRequestQueue(this)
            val jsonArrayRequest = object :
                JsonArrayRequest(Method.GET, url, null, Response.Listener { jsonResponse ->
                    val detailsArray = SessionDetailsFactory.mapFromJSONArray(jsonResponse)
                    recycler_view.adapter = DetailsAdapter(detailsArray)
                }, Response.ErrorListener { error ->
                    val message = VolleyFactory.getMessage(error)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }) {
                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            volleyRequest.add(jsonArrayRequest)
        }
    }

    private fun oldAutoRefreshRecyclerView()
    {
        val handler = Handler()
        val timedTask: Runnable = object : Runnable {
            override fun run() {
                fetchDetails()
                handler.postDelayed(this, 30000)
            }
        }
        handler.post(timedTask)
    }

    // GOOGLE MAP POSITION

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!
        // Add a marker in Sydney and move the camera
        /*val sydney = LatLng(48.9, 2.3)
        val zoom = CameraUpdateFactory.zoomTo(15f)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Paris"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.animateCamera(zoom)*/
        if (isPermissionGiven()){
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            getCurrentLocation()
        } else {
            givePermission()
        }
    }
    private fun isPermissionGiven(): Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private fun givePermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        getCurrentLocation()
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        token!!.continuePermissionRequest()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        Toast.makeText(this, "Permission required for showing location", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun getCurrentLocation() {

        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (10 * 1000).toLong()
        locationRequest.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        val result = LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest)
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                if (response!!.locationSettingsStates.isLocationPresent){
                    getLastLocation()
                }
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = exception as ResolvableApiException
                        resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) {
                    } catch (e: ClassCastException) {
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { }
                }
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PERMMM","ytfytf")
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    val sydney = LatLng(location!!.latitude, location.longitude)
                    val zoom = CameraUpdateFactory.zoomTo(15f)
                    //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Paris"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                    mMap.animateCamera(zoom)
                    Toast.makeText(this, "${location!!.latitude} = ${location.longitude}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "No current location found", Toast.LENGTH_LONG).show()
                }
            }
    }
}
