package com.printing3D.spoolsmanager.demo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.printing3d.spoolsmanager.IInputConsumptionService
import kotlinx.android.synthetic.main.activity_demo.*


class DemoActivity : AppCompatActivity() {

    ////////////////
    // Attributes //
    ////////////////

    private var serviceConnection: RemoteServiceConnection? = null
    private var service: IInputConsumptionService? = null

    ///////////////
    // LifeCycle //
    ///////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_demo)

        btn_send_input.setOnClickListener { sendInput() }
    }

    override fun onStart() {
        super.onStart()

        connectService()
    }

    override fun onStop() {
        disconnectService()

        super.onStop()
    }

    /////////////////////
    // Private methods //
    /////////////////////

    /**
     * Send input to Filament Spool Manager
     * over AIDL service interface
     */
    private fun sendInput() {
        try {
            val lengthInput = edt_value.text.toString().toFloat()

            if (lengthInput <= 0) {
                Toast.makeText(this@DemoActivity,
                        "Length input should be positive and != 0", Toast.LENGTH_LONG).show()
                return
            }

            /* Send length input to Filament Spool Manager.
             * You can also send weight input, so you should specify length to 0f.
             * If provided length > 0, it will have the highest priority, even if
             * a weight > 0 is provided. */
            service?.selectSpoolAndInput(lengthInput, 0f)

        } catch (e: NumberFormatException) {
            Toast.makeText(this@DemoActivity, "Invalid number format", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Connect to the remote service.
     */
    private fun connectService() {
        serviceConnection = RemoteServiceConnection()

        // Create service intent
        val intent = Intent()
        intent.setClassName(AIDL_PACKAGE, AIDL_CLASSNAME)
        intent.action = REMOTE_ACTION

        // Bind to service
        val ret = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        btn_send_input.isEnabled = ret

        Log.i(TAG, "bindService returns $ret")
    }

    /**
     * Disconnect service. It is mandatory to prevent any leaks
     */
    private fun disconnectService() {
        serviceConnection?: return

        // Unbind from service
        unbindService(serviceConnection)
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * RemoteServiceConnection object, to get callbacks
     * on service connection changed
     */
    private inner class RemoteServiceConnection: ServiceConnection {
        // Service is connected
        override fun onServiceConnected(name: ComponentName, boundService: IBinder) {
            service = IInputConsumptionService.Stub.asInterface(boundService)
            Toast.makeText(this@DemoActivity, "Service connected", Toast.LENGTH_LONG).show()
        }

        // Service is disconnected
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            Toast.makeText(this@DemoActivity, "Service disconnected", Toast.LENGTH_LONG).show()
        }
    }

    ///////////////
    // Companion //
    ///////////////

    companion object {

        private const val TAG = "DemoActivity"

        /**
         * The remote action ID. Do not change this value.
         */
        private const val REMOTE_ACTION = "com.printing3d.spoolsmanager.InputConsumptionService.BIND"

        /**
         * Remote service package. Do not change this value.
         */
        private const val AIDL_PACKAGE = "com.printing3d.spoolsmanager"

        /**
         * Remote service location. Do not change this value.
         */
        private const val AIDL_CLASSNAME = "fr.appbase.app.base.ipc.InputConsumptionService"

    }

}
