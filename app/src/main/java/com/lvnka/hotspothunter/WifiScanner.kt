package com.lvnka.hotspothunter

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat

class WifiScanner(activity: Activity) : BroadcastReceiver() {

    private var resultList = ArrayList<ScanResult>()
    private lateinit var wifiManager: WifiManager
    private var activity: Activity
    val requestLocationPermission = 1
    val requestWifiPermission = 1
    private var isScanning: Boolean = false

    init {
        this.activity = activity
        if (this.activity.applicationContext.getSystemService(Context.WIFI_SERVICE) != null) {
            this.wifiManager = this.activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            Log.d("TESTING", " wifiManager created")
        }
    }

    fun isScanning(): Boolean {
        return this.isScanning
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        checkWifiPermission()
        this.resultList = this.wifiManager.scanResults as ArrayList<ScanResult>
        Log.d("TESTING", "onReceive called!")
    }

    private fun checkWifiPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this.activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("TESTING", "FINE LOCATION has not been granted")
            ActivityCompat.requestPermissions(this.activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), this.requestLocationPermission)
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                this.activity.applicationContext,
                Manifest.permission.CHANGE_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TESTING", "CHANGE WIFI STATE has not been granted")
            ActivityCompat.requestPermissions(this.activity, arrayOf(Manifest.permission.CHANGE_WIFI_STATE), this.requestWifiPermission)
            return false
        }
        Log.d("TESTING", "Permissions have been granted!")
        return true
    }

    fun startScanning() {
        if (checkWifiPermission()) {
            this.isScanning = true
            Log.d("TESTING", "starting scanning?")
            wifiManager.startScan()

            Handler().postDelayed({
                stopScanning()
            }, 10000)
        }
    }

    fun stopScanning() {
        this.isScanning = false
        val axisList = ArrayList<String>()
        for (result in this.resultList) {
            axisList.add(result.BSSID.toString())
            //axisList.add(result.BSSID.toString(), result.level.toString())
        }
        Log.d("TESTING", axisList.toString())
    }
}