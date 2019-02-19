package si.inova.zimskasola

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_my_location.view.*
import com.example.zimskasola.R
import com.squareup.picasso.Picasso

class RecyclerAdapterMyLocation(val items: ArrayList<DescriptionItems>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_my_location, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtTop.text = items[position].type
        holder.txtMid.text = items[position].title
        holder.txtBot.text = items[position].subtitle
        Picasso.with(context).load(items[position].type_icon).into(holder.img)
    }


    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val txtTop = itemView.recycler_tv_top
    val txtMid = itemView.recycler_tv_mid
    val txtBot = itemView.recycler_tv_bot
    val img = itemView.recycler_img
}

