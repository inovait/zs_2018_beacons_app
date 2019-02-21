package si.inova.zimskasola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zimskasola.R
import kotlinx.android.synthetic.main.recycler_places_item.view.*

class RecyclerAdapterPlaces(val items: ArrayList<Soba>, val context: Context) : RecyclerView.Adapter<ViewHolderPlaces>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPlaces {
        return ViewHolderPlaces(LayoutInflater.from(parent.context).inflate(R.layout.recycler_places_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderPlaces, position: Int) {
        if (items[position].prva) {
            holder.header.visibility = View.VISIBLE
            holder.default.visibility = View.GONE

            holder.txtHeaderFloor.text = items[position].nadstropje
            holder.txtHeaderRoom.text = items[position].soba
        } else {
            holder.header.visibility = View.GONE
            holder.default.visibility = View.VISIBLE
            holder.txtDefaultRoom.text = items[position].soba
        }
    }


    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolderPlaces (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val header = itemView.clPlacesHeader
    val default = itemView.clPlacesDefault

    val txtHeaderFloor = itemView.tvPlacesFloor
    val txtHeaderRoom = itemView.tvPlacesRoomHeader

    val txtDefaultRoom = itemView.tvPlacesRoomDefault
}

