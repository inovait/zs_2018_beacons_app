package si.inova.zimskasola

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import io.nlopez.smartlocation.geofencing.model.GeofenceModel
import io.nlopez.smartlocation.SmartLocation

private const val GEOFENCE_RADIUS_IN_METERS = 1000F

class MrGeofence(var context: Context) {

    var myFences: ArrayList<GeofenceModel> = ArrayList()

    var geocodingSuccess = false
    var geocodingFailed = false

    fun create(address: String) {
        SmartLocation.with(context).geocoding()
            .direct(address) { name, results ->
                Log.d(TAG, "Smart Location result: $results")
                if (results.size > 0) {
                    Log.d(TAG, "Found location of $name:\n${results[0].location}")
                    val location = results[0].location
                    val fence = GeofenceModel.Builder(address)
                        .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .setTransition(Geofence.GEOFENCE_TRANSITION_DWELL)
                        .setLatitude(location.latitude)
                        .setLongitude(location.longitude)
                        .setRadius(GEOFENCE_RADIUS_IN_METERS)
                        .build()
                    Log.d(TAG, "Built geofence model: ${fence.toGeofence()}")
                    myFences.add(fence)
                    geocodingSuccess = true
                    geocodingFailed = false
                } else {
                    Log.d(TAG, "Address $address could not be found")
                    geocodingSuccess = false
                    geocodingFailed = true
                }
            }
    }

    fun currentlyInside(currentLocation: Location, geoFenceLocation: Location): Location? {
        if (currentLocation.distanceTo(geoFenceLocation) < GEOFENCE_RADIUS_IN_METERS)
            return geoFenceLocation
        return null
    }
}