package si.inova.zimskasola

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

private const val COLLECTION_PATH = "locations"
private const val SUB_COLLECTION_PATH = "places"
private const val SUB_SUB_COLLECTION_PATH = "description_items"

data class Location (
    var documentId: String = "",
    val address: String = "",
    val name: String = "",
    var places: ArrayList<Places> = ArrayList()
)

data class Places (
    var documentId: String = "",
    val floor: String = "",
    val image: String = "",
    val name: String = "",
    var descriptionItems: ArrayList<DescriptionItems> = ArrayList()
)

data class DescriptionItems (
    val subtitle: String = "",
    val title: String = "",
    val type: String = "",
    val type_icon: String = ""
)

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    var locations = ArrayList<Location>()

    // get id, name and address for all locations
    fun getLocations() {
        db.collection(COLLECTION_PATH)
            .addSnapshotListener { data, _ ->
                if (data == null) {
                    return@addSnapshotListener
                }
                for (document in data.documents) {
                    val location = document.toObject(Location::class.java)!!
                    location.documentId = document.id
                    locations.add(location)
                }

                Log.d("FirestoreManager", locations.toString())
            }
    }

    // get places and description_items for a specific location
    fun getLocationData(locationId: String) {
        val locationIndex = locations.indexOf(locations.single {
                location -> location.documentId == locationId
        })

        db.collection("$COLLECTION_PATH/$locationId/$SUB_COLLECTION_PATH")
            .addSnapshotListener { data, _ ->
                if (data == null) {
                    return@addSnapshotListener
                }
                for (document in data.documents) {
                    val place = document.toObject(Places::class.java)!!
                    place.documentId = document.id
                    locations[locationIndex].places.add(place)

                    getDescriptionItems(locationIndex, locationId, place.documentId)
                }
        }
    }

    private fun getDescriptionItems(locationIndex: Int, locationId: String, placeId: String) {
        val placeIndex = locations[locationIndex].places.indexOf(
            locations[locationIndex].places.single {
                place -> place.documentId == placeId
            }
        )

        db.collection("$COLLECTION_PATH/$locationId/$SUB_COLLECTION_PATH/$placeId/$SUB_SUB_COLLECTION_PATH")
            .addSnapshotListener {
            data, _ ->
                if (data == null) {
                    return@addSnapshotListener
                }
                data.map {
                    locations[locationIndex].places[placeIndex].descriptionItems.add(
                        it.toObject(DescriptionItems::class.java)
                    )
                }

                Log.d("FirestoreManager", locations.toString())
        }
    }
}