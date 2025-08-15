package com.example.negociomx_pos.Utils

import android.net.VpnService
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import java.util.concurrent.atomic.AtomicBoolean

class MyVpnService : VpnService() {
    private val tag="Radmin VPN"
    private var isRunning: AtomicBoolean = AtomicBoolean(false)
    private lateinit var vpnInterface: ParcelFileDescriptor
    private var serverIP:String=""
    private var serverPortNumber:Int=0
    var handler: Handler = Handler(Looper.getMainLooper())


}