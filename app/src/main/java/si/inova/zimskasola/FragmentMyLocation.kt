package si.inova.zimskasola

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zimskasola.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

private const val ANIM_LENGTH : Long = 1000

class FragmentMyLocation : Fragment() {

    lateinit var activityMain: ActivityMain

    lateinit var clLoading: ConstraintLayout
    lateinit var progress: ProgressBar
    lateinit var recycler: RecyclerView
    lateinit var tvNahajasSe: TextView
    lateinit var tvName: TextView
    lateinit var tvFloor: TextView
    lateinit var img: ImageView
    lateinit var divider: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_my_location, container, false)
        if (activityMain.currentBuilding == null) {
            Log.d(TAG, "Fragment my location: activityMain.currentBuilding was null, initializing...")
            activityMain.initialize()
        } else {
            Log.d(TAG, "building my location fragment")
            clLoading = root.findViewById(R.id.clLoading) as ConstraintLayout
            progress = root.findViewById(R.id.progressBar) as ProgressBar
            img = root.findViewById(R.id.imageView) as ImageView
            tvNahajasSe = root.findViewById(R.id.textViewNahajasSe) as TextView
            tvName = root.findViewById(R.id.tvImeLokacije) as TextView
            tvFloor = root.findViewById(R.id.tvNadstropje) as TextView
            recycler = root.findViewById(R.id.recyclerView) as RecyclerView
            divider = root.findViewById(R.id.divider7) as View

            Picasso.with(context).load(activityMain.currentRoom?.image).into(img, object: Callback {
                override fun onSuccess() {
                    progress.apply { visibility = View.GONE }
                    clLoading.apply { visibility = View.GONE }
                    tvName.text = activityMain.currentRoom?.name
                    tvFloor.text = activityMain.currentFloor?.name
                    img.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
                    tvNahajasSe.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
                    tvName.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
                    divider.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
                    tvFloor.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
                    recycler.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
                }
                override fun onError() {
                }
            })
            recycler.layoutManager = LinearLayoutManager(this.context)
            recycler.adapter = RecyclerAdapterMyLocation(activityMain.currentRoom?.stuff!!, this.context!!)
            recycler.itemAnimator = DefaultItemAnimator()
        }
        return root
    }


}
