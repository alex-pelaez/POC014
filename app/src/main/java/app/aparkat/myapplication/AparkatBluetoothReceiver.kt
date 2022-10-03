package app.aparkat.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.widget.Toast
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val TAG = "AparkatBluetoothBroadcastReceiver"

// FusedLocationProviderClient - Main class for receiving location updates.
private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

// LocationRequest - Requirements for the location updates, i.e.,
// how often you should receive updates, the priority, etc.
private lateinit var locationRequest: LocationRequest

// LocationCallback - Called when FusedLocationProviderClient
// has a new Location
private lateinit var locationCallback: LocationCallback

// This will store current location info
private var currentLocation: Location? = null

class AparkatBluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
 /*       StringBuilder().apply {
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                Log.d(TAG, log)
                Toast.makeText(context, log, Toast.LENGTH_LONG).show()

            }
        }
        Fuel.get("https://hello-uun4mb7fqq-no.a.run.app")
            .response { request, response, result ->
                val (bytes, error) = result
                if (bytes != null) {
                    val aux = "[response bytes] ${String(bytes)}".toString()
                    Log.d(TAG, aux)
                }
            }*/

        // Get current position
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest().apply {
            // Sets the desired interval for
            // active location updates.
            // This interval is inexact.
            interval = TimeUnit.SECONDS.toMillis(60)

            // Sets the fastest rate for active location updates.
            // This interval is exact, and your application will never
            // receive updates more frequently than this value
            fastestInterval = TimeUnit.SECONDS.toMillis(30)

            // Sets the maximum time when batched location
            // updates are delivered. Updates may be
            // delivered sooner than this interval
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let {
                    currentLocation  = locationResult.lastLocation
                    //DB
                    // Access a Cloud Firestore instance from your Activity
                    val db = Firebase.firestore

                    // Get Current Position
                    val currentPosition = hashMapOf(
                        "location" to GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude),
                        "type" to "Medium",
                        "user" to "user02",
                        "timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    )

                    // Add a new document with a generated ID
                    db.collection("ParkingSlot")
                        .add(currentPosition)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                } ?: {
                    Log.d(TAG, "Location information isn't available.")
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

        //DB
        // Access a Cloud Firestore instance from your Activity
        val db = Firebase.firestore

        // Get Current Position
        val currentPosition = hashMapOf(
            "location" to GeoPoint(41.0, 5.0),
            "type" to "Medium",
            "user" to "user02",
            "timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )

        // Add a new document with a generated ID
        db.collection("ParkingSlot")
            .add(currentPosition)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }





/*
        // Get all records
        db.collection("ParkingSlot")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }*/
    }

    fun Query.getNearestLocation(latitude: Double, longitude: Double, distance: Double): Query {
        // ~1 mile of lat and lon in degrees
        val lat = 0.0144927536231884
        val lon = 0.0181818181818182

        val lowerLat = latitude - (lat * distance)
        val lowerLon = longitude - (lon * distance)

        val greaterLat = latitude + (lat * distance)
        val greaterLon = longitude + (lon * distance)

        val lesserGeopoint = GeoPoint(lowerLat, lowerLon)
        val greaterGeopoint = GeoPoint(greaterLat, greaterLon)

        val docRef = FirebaseFirestore.getInstance().collection("ParkingSlot")
        return docRef
            .whereGreaterThan("location", lesserGeopoint)
            .whereLessThan("location", greaterGeopoint)
    }
}