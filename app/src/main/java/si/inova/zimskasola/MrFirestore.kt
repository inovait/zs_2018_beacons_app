// ta razred je v tem trenutku neuporaben, saj vse podatke pridobimo iz json datoteke

package si.inova.zimskasola

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

private const val COLLECTION_PATH = "locations"
private const val SUB_COLLECTION_PATH = "places"
private const val SUB_SUB_COLLECTION_PATH = "description_items"

data class Lokacija (
    var documentId: String = "",
    val address: String = "",
    val name: String = "",
    var places: ArrayList<Prostor> = ArrayList()
)

data class Prostor (
    var documentId: String = "",
    val floor: String = "",
    val image: String = "",
    val name: String = "",
    var descriptionItems: ArrayList<Stvari> = ArrayList()
)

data class Stvari (
    val subtitle: String = "",
    val title: String = "",
    val type: String = "",
    var type_icon: String = ""
)

class MrFirestore {
    private val db = FirebaseFirestore.getInstance()

    var locations = ArrayList<Lokacija>()

    var loadedPlaces = false
    var loadedPlacesData = false

    // saves all locations from firestore to locations arraylist (without subdocuments)
    fun requestLocations() {
        Log.d("FirestoreManager", "requesting locations")
        db.collection(COLLECTION_PATH)
            .addSnapshotListener { data, _ ->
                if (data == null) {
                    return@addSnapshotListener
                }
                locations.clear()
                for (document in data.documents) {
                    val location = document.toObject(Lokacija::class.java)!!
                    location.documentId = document.id
                    locations.add(location)
                }
                loadedPlaces = true
                Log.d("FirestoreManager", locations.toString())
            }
    }

    // saves all subdocuments from specific location into location in locations arraylist
    fun requestLocationData(locationId: String) {
        val locationIndex = locations.indexOf(locations.single {
                location -> location.documentId == locationId
        })
        db.collection("$COLLECTION_PATH/$locationId/$SUB_COLLECTION_PATH")
            .addSnapshotListener { data, _ ->
                if (data == null) {
                    return@addSnapshotListener
                }
                locations[locationIndex].places.clear()
                for (document in data.documents) {
                    val place = document.toObject(Prostor::class.java)!!
                    place.documentId = document.id
                    locations[locationIndex].places.add(place)

                    requestDescriptionItems(locationIndex, locationId, place.documentId)
                }
        }
    }

    private fun requestDescriptionItems(locationIndex: Int, locationId: String, placeId: String) {
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
                locations[locationIndex].places[placeIndex].descriptionItems.clear()
                data.map {
                    locations[locationIndex].places[placeIndex].descriptionItems.add(
                        it.toObject(Stvari::class.java)
                    )
                }
                loadedPlacesData = true
                Log.d("FirestoreManager", locations.toString())
        }
    }
}