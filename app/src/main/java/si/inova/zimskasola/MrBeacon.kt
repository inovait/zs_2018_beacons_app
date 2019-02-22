package si.inova.zimskasola

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*

class MrBeacon(private val context: Context, private val listener: Listener, private val activityMain: ActivityMain) : MessageListener() {

    fun start() {
        getClient(context).subscribe(this, buildOptions())
    }

    fun stop() {
        getClient(context).unsubscribe(this)
    }

    private fun buildOptions(): SubscribeOptions {
        return SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .build()
    }

    private fun getClient(context: Context): MessagesClient {
        val options = MessagesOptions.Builder().setPermissions(NearbyPermissions.BLE).build()
        return Nearby.getMessagesClient(context, options)
    }

    override fun onFound(message: Message) {
        super.onFound(message)
        String(message.content).let {
            Log.d(TAG, "Beacon found: $it")
            listener.onBeaconFound(it)
        }
    }

    override fun onLost(message: Message) {
        super.onLost(message)
        String(message.content).let {
            Log.d(TAG, "Beacon lost: $it")
            listener.onBeaconLost(it)
        }
    }

    interface Listener {
        fun onBeaconFound(data: String)
        fun onBeaconLost(data: String)
    }

}