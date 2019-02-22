package si.inova.zimskasola

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.io.File
import kotlin.collections.ArrayList
import com.google.gson.reflect.TypeToken;

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

    val gson = Gson()

    var downloadSuccess = false
    var downloadFailed = false

    var buildings: ArrayList<Building>? = null

    fun downloadJsonObject() {
        val storageRef = storage.reference
        val pathReference = storageRef.child(FILE_NAME)
        val localFile = File.createTempFile("file", "json")
        pathReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "Success downloading JSON, ${it.bytesTransferred} bytes")
            buildings = ArrayList()
            buildings!!.add(gson.fromJson(localFile.readText(), Building::class.java))
            downloadSuccess = true
            downloadFailed = false
        }.addOnFailureListener {
            Log.d(TAG, "Error while downloading JSON object from Firestorage")
            downloadSuccess = false
            downloadFailed = true
            // should add try again or check for connection
        }
    }

    // ce bi na firestorage imeli vec buildingov oz. json tipa array namesto object bi naj baje to delovalo
    // https://stackoverflow.com/questions/33381384/how-to-use-typetoken-generics-with-gson-in-kotlin
    fun downloadJsonArray() {
        val storageRef = storage.reference
        val pathReference = storageRef.child(FILE_NAME)
        val localFile = File.createTempFile("file", "json")
        pathReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "Success downloading JSON, ${it.bytesTransferred} bytes")
            buildings = gson.fromJson<ArrayList<Building>>(localFile.readText())
            downloadSuccess = true
        }.addOnFailureListener {
            Log.d(TAG, "Error while downloading JSON array from Firestorage")
            downloadSuccess = false
        }
    }

    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
}
