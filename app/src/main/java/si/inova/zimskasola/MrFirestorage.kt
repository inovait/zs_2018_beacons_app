package si.inova.zimskasola

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.io.File
import kotlin.collections.ArrayList

private const val FILE_NAME = "25022c4a-3035-11e9-bb6a-a5c92278bce1.json"

data class Building (
    var title: String = "",
    var description: String = "",
    var floors: ArrayList<Floor> = ArrayList()
)

data class Floor (
    var floor_id: Int = 0,
    var name: String = "",
    var rooms: ArrayList<Room> = ArrayList()
)

data class Room (
    var room_id: Int = 0,
    var beacon_id:  String = "",
    var name: String = "",
    var image: String = "",
    var stuff: ArrayList<Stuff> = ArrayList()
)

data class Stuff (
    var stuff_id: Int = 0,
    var name: String = "",
    var category: String = "",
    var description: String = "",
    var icon: String = ""
)

class MrFirestorage {
    val storage = FirebaseStorage.getInstance()
    var building: Building? = null
    val gson = Gson()
    var downloadSuccess = false

    fun downloadJson() {
        val storageRef = storage.reference
        val pathReference = storageRef.child(FILE_NAME)
        val localFile = File.createTempFile("file", "json")
        pathReference.getFile(localFile).addOnSuccessListener {
            Log.d("MrFirestorage", "Success downloading JSON, ${it.bytesTransferred} bytes")
            building = gson.fromJson(localFile.readText(), Building::class.java)
            downloadSuccess = true
        }.addOnFailureListener {
            Log.d("MrFirestorage", "Error while downloading JSON from Firestorage")
            downloadSuccess = false
        }
    }
}
