package app.aparkat.myapplication

import android.app.*
import android.bluetooth.BluetoothDevice
import android.companion.*
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import java.util.*
import java.util.regex.Pattern
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ListView
import android.util.Log

private const val SELECT_DEVICE_REQUEST_CODE_LE = 1
private const val SELECT_DEVICE_REQUEST_CODE_CLASSIC = 2

class MainActivity : AppCompatActivity() {
    lateinit var editText: EditText
    lateinit var button: Button
    lateinit var btnScan: Button
    lateinit var btnScanLE: Button
    lateinit var btnList: Button
    lateinit var listView: ListView
    lateinit var textView: TextView
    var list: ArrayList<String> = ArrayList()
    lateinit var arrayAdapter: ArrayAdapter<String>
    var TAG = "SCAN_APP"

    private val deviceManager: CompanionDeviceManager by lazy {
        getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Prepare UI
        title = "Kotlin"
        listView = findViewById(R.id.listView)
        editText = findViewById(R.id.editText)
        button = findViewById(R.id.btnAdd)
        btnList = findViewById(R.id.btnList)
        btnScan = findViewById(R.id.btnScan)
        btnScanLE = findViewById(R.id.btnScanLE)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        button.setOnClickListener {
            list.add(editText.text.toString())
            editText.setText("")
            arrayAdapter.notifyDataSetChanged()
            listView.adapter = arrayAdapter
        }

        btnScanLE.setOnClickListener {
            Log.i(TAG, "Scan LE")

            // To skip filters based on names and supported feature flags (UUIDs),
            // omit calls to setNamePattern() and addServiceUuid()
            // respectively, as shown in the following  Bluetooth example.
            val deviceFilter: BluetoothLeDeviceFilter = BluetoothLeDeviceFilter.Builder()
                //.setNamePattern(Pattern.compile("Jabra"))
                //.addServiceUuid(ParcelUuid(UUID(0x123abcL, -1L)), null)
                .build()

            // The argument provided in setSingleDevice() determines whether a single
            // device name or a list of them appears.
            val pairingRequest: AssociationRequest = AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build()

            // When the app tries to pair with a Bluetooth device, show the
            // corresponding dialog box to the user.
            deviceManager.associate(pairingRequest,
                object : CompanionDeviceManager.Callback() {

                    override fun onDeviceFound(chooserLauncher: IntentSender) {
                        startIntentSenderForResult(chooserLauncher,
                            SELECT_DEVICE_REQUEST_CODE_LE, null, 0, 0, 0)
                    }

                    override fun onFailure(error: CharSequence?) {
                        val objectAux  = 0
                    }
                }, null)
        }

        btnScan.setOnClickListener {
            Log.i(TAG, "Scan Classic")

            // To skip filters based on names and supported feature flags (UUIDs),
            // omit calls to setNamePattern() and addServiceUuid()
            // respectively, as shown in the following  Bluetooth example.
            val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
                //.setNamePattern(Pattern.compile("Jabra"))
                //.addServiceUuid(ParcelUuid(UUID(0x123abcL, -1L)), null)
                .build()

            // The argument provided in setSingleDevice() determines whether a single
            // device name or a list of them appears.
            val pairingRequest: AssociationRequest = AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build()

            // When the app tries to pair with a Bluetooth device, show the
            // corresponding dialog box to the user.
            deviceManager.associate(pairingRequest,
                object : CompanionDeviceManager.Callback() {

                    override fun onDeviceFound(chooserLauncher: IntentSender) {
                        startIntentSenderForResult(chooserLauncher,
                            SELECT_DEVICE_REQUEST_CODE_CLASSIC, null, 0, 0, 0)
                    }

                    override fun onFailure(error: CharSequence?) {
                        val objectAux  = 0
                    }
                }, null)
        }

        btnList.setOnClickListener {
            deviceManager.getAssociations().forEach { list.add(it) }
            arrayAdapter.notifyDataSetChanged()
            listView.adapter = arrayAdapter

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SELECT_DEVICE_REQUEST_CODE_CLASSIC -> when(resultCode) {
                Activity.RESULT_OK -> {
                    //list.add("New")
                    // The user chose to pair the app with a Bluetooth device.
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let { device ->
                        device.createBond()
                        // Maintain continuous interaction with a paired device.
                    }
                }
            }
            SELECT_DEVICE_REQUEST_CODE_LE -> when(resultCode) {
                Activity.RESULT_OK -> {
                    //list.add("New")
                    // The user chose to pair the app with a Bluetooth device.
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let { device ->
                        device.createBond()
                        // Maintain continuous interaction with a paired device.
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}