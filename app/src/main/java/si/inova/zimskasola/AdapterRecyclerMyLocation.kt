package si.inova.zimskasola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_my_location_item.view.*
import com.example.zimskasola.R
import com.squareup.picasso.Picasso

class RecyclerAdapterMyLocation(val items: ArrayList<Stuff>, val context: Context) : RecyclerView.Adapter<ViewHolderMyLocation>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMyLocation {
        return ViewHolderMyLocation(LayoutInflater.from(parent.context).inflate(R.layout.recycler_my_location_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderMyLocation, position: Int) {
        /* replace items with val items: ArrayList<Stvari> if we load from firestore
        holder.txtTop.text = items[position].type
        holder.txtMid.text = items[position].title
        holder.txtBot.text = items[position].subtitle
        Picasso.with(context).load(items[position].type_icon).into(holder.img)*/
        holder.txtTop.text = items[position].category
        holder.txtMid.text = items[position].name
        holder.txtBot.text = items[position].description
        Picasso.with(context).load(items[position].icon).into(holder.img)
    }


    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolderMyLocation (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val txtTop = itemView.recycler_tv_top
    val txtMid = itemView.recycler_tv_mid
    val txtBot = itemView.recycler_tv_bot
    val img = itemView.recycler_img
}

