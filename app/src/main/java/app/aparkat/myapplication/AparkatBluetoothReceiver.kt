package app.aparkat.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.util.Log
import com.github.kittinunf.fuel.Fuel



private const val TAG = "AparkatBluetoothBroadcastReceiver"

class AparkatBluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        StringBuilder().apply {
            append("Action: ${intent.action}\n")
            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                Log.d(TAG, log)
                Toast.makeText(context, log, Toast.LENGTH_LONG).show()

            }
        }
        Fuel.get("https://hello-uun4mb7fqq-no.a.run.app")
            .response { request, response, result ->
                val (bytes, error) = result
                if (bytes != null) {
                    val aux = "[response bytes] ${String(bytes)}".toString()
                    Log.d(TAG, aux)
                }
            }
    }
}