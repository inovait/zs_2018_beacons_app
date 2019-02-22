package si.inova.zimskasola

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.zimskasola.R

private const val ANIM_LENGTH : Long = 1000

class FragmentError : Fragment() {

    lateinit var tvMain: TextView
    lateinit var tvSub: TextView
    lateinit var img: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var root = inflater.inflate(R.layout.fragment_error, container, false)

        img = root.findViewById(R.id.imageView2) as ImageView
        tvMain = root.findViewById(R.id.tvErrorHeader) as TextView
        tvSub = root.findViewById(R.id.tvErrorSub) as TextView

        img.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
        tvMain.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }
        tvSub.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }

        return root
    }
}
