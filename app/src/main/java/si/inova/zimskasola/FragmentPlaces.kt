package si.inova.zimskasola

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zimskasola.R

class FragmentPlaces: Fragment() {

    lateinit var activityMain: ActivityMain
    lateinit var tvProstori: TextView
    lateinit var recycler: RecyclerView

    var vseSobe: ArrayList<Soba> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var root = inflater.inflate(R.layout.fragment_places, container, false)
        if (activityMain.currentBuilding == null) {
            Log.d(TAG, "activityMain.currentBuilding was null, initializing...")
            activityMain.initialize()
        } else {
            tvProstori = root.findViewById(R.id.textViewProstori) as TextView
            recycler = root.findViewById(R.id.recyclerProstori) as RecyclerView

            recycler.layoutManager = LinearLayoutManager(this.context)

            var stej = 0
            for (floor in activityMain.currentBuilding?.floors!!) {
                for (room in floor.rooms) {
                    if (stej == 0)
                        vseSobe.add(Soba(true, floor.name, room.name))
                    else
                        vseSobe.add(Soba(false, floor.name, room.name))
                    stej++
                }
                stej = 0
            }

            recycler.adapter = RecyclerAdapterPlaces(vseSobe, this.context!!)
            recycler.itemAnimator = DefaultItemAnimator()
        }
        return root
    }
}

class Soba (
    var prva: Boolean,
    var nadstropje: String,
    var soba: String
)