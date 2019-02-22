package si.inova.zimskasola

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.zimskasola.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData
import java.util.*
import kotlin.collections.ArrayList

private const val ANIM_LENGTH : Long = 1000

class FragmentSettings : Fragment() {

    lateinit var activityMain : ActivityMain

    lateinit var chart: LineChart
    lateinit var cl: ConstraintLayout
    lateinit var tvNadstropje: TextView
    lateinit var tvSoba: TextView

    val rnd: Random = Random()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        cl = root.findViewById(R.id.clSettings) as ConstraintLayout
        chart = root.findViewById(R.id.chart) as LineChart
        tvNadstropje = root.findViewById(R.id.tvSettingsNadstropje) as TextView
        tvSoba = root.findViewById(R.id.tvSettingsImeSobe) as TextView

        tvNadstropje.text = activityMain.currentFloor?.name
        tvSoba.text = activityMain.currentRoom?.name

        // GRAPH DATA
        // napolni dataObjects s podatki
        // test start random hardcoded data
        var dataObjects = GraphData()
        var mapiraj = mutableMapOf<Int, Int>()
        for (i in 0..23) {
            mapiraj[i] = rnd.nextInt(10)
        }
        Log.d(TAG, "NANA")
        for (j in 0..5) {
            dataObjects.data.add(Obisk("soba $j", mapiraj))
        }
        Log.d(TAG, "NANGAA")
        val entries = ArrayList<Entry>()
        for (i in 0..(dataObjects.data[0].obisk.size-1)) {
            Log.d(TAG, "$i")
            entries.add(Entry(i.toFloat(), dataObjects.data[0].obisk[i]!!.toFloat()))
        }

        // test end napolni entries z dataObjects

        /* ACTUAL DATa
        val entries = ArrayList<Entry>()
        for (data in activityMain.pogostostObiska.data) {
            if (data.imeSobe == activityMain.currentRoom?.name) {
                for (i in 0..data.obisk.size) {
                    if (data.obisk[i]!! > 0)
                        entries.add(Entry(i.toFloat(), data.obisk[i]!!.toFloat()))
                        Log.d(TAG, "entry added")
                }
            }
        }
        Log.d(TAG, $entries)
        */

        // GRAPH LOOKS
        // data settings
        val dataSet = LineDataSet(entries, "Pogostost nahajanja v sobi ob določeni uri")
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.label = "Število obiskov prostora ob določeni uri"
        dataSet.setDrawFilled(true)
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 0.7f
        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)
        chart.data = lineData

        // graph settings
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 24f

        val left = chart.axisLeft
        left.setDrawLabels(false)
        left.setDrawGridLines(false)
        left.axisMinimum = 0f
        left.setDrawLabels(false)
        left.setDrawAxisLine(false)

        val right = chart.axisRight
        right.setDrawLabels(false)
        right.setDrawGridLines(false)
        right.setDrawAxisLine(false)

        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.description = Description().apply { text = "" }
        chart.setNoDataText("Podatki niso na voljo.")

        // interaction settings
        chart.setTouchEnabled(false)

        // refresh
        chart.invalidate()

        cl.alpha = 0f
        cl.apply { animate().alpha(1f).setDuration(ANIM_LENGTH).setListener(null) }

        return root
    }

    override fun onResume() {
        super.onResume()
    }
}

class GraphData (
    var data: ArrayList<Obisk> = ArrayList()
)

class Obisk (
    var imeSobe: String,
    var obisk: MutableMap<Int, Int>
)