package com.lvnka.hotspothunter

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager

class WifiScanner(activity: Activity) : BroadcastReceiver() {

    private var resultList = ArrayList<ScanResult>()
    var resultText = mutableListOf<String>()
    private lateinit var wifiManager: WifiManager
    private var locationManager: LocationManager
    var location: String = ""
    private var activity: Activity
    val requestLocationPermission = 1
    val requestWifiPermission = 1
    val mainHandler = Handler(Looper.getMainLooper())
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
    private var sync: Boolean = false
    private var interval: Int = 5000
    private var isScanning: Boolean = false

    init {
        this.activity = activity
        if (this.activity.applicationContext.getSystemService(Context.WIFI_SERVICE) != null) {
            this.wifiManager = this.activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            Log.d("TESTING", " wifiManager created")
        }
        this.locationManager = this.activity.applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        this.syncPrefs()
    }

    fun isScanning(): Boolean {
        return this.isScanning
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        checkWifiPermission()
        resultText.clear()
        this.resultList = this.wifiManager.scanResults as ArrayList<ScanResult>
        for (result in resultList) {
            this.resultText.add("${result.SSID} -- ${result.BSSID} -- Level: ${result.level}")
        }
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
        Log.d("WifiScanner", "Permissions have been granted!")
        return true
    }

    private fun startScanning() {
        this.getLocation()
        if (checkWifiPermission()) {
            this.isScanning = true
            Log.d("WifiScanner", "Starting scan")
            wifiManager.startScan()

            Handler().postDelayed({
                if (this.sync) {
                    stopWithoutBreaking()
                } else {
                    stopScanning()
                }
            }, if (this.interval > 5000) 9000 else 3000)
        }
    }

    private fun syncPrefs() {
        this.sync = prefs.getBoolean("sync", false)
        this.interval = prefs.getInt("interval", 5000)
    }

    fun prepareScanning() {
        if (this.sync) {
            mainHandler.post(object : Runnable {
                override fun run() {
                    Log.d("WifiScanner", "Starting interval scan")
                    startScanning()
                    mainHandler.postDelayed(this, this@WifiScanner.interval.toLong())
                }
            })
        } else {
            startScanning()
        }
    }

    private fun getLocation() {
        if (checkWifiPermission()) {
            Log.d("LOCATION", "fetch location")
            this.locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                this.locationListener
            )
        }
    }

    private var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            this@WifiScanner.location = "${location.longitude}:${location.latitude}"
            Log.d("LOCATION", this@WifiScanner.location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun stopWithoutBreaking() {
        Log.d("WifiScanner", "Stopping scan run")
        val uploader = Uploader(this.activity)
        uploader.upload(this.resultList)

        for (result in this.resultList) {
            Log.d("WIFI", result.BSSID.toString())
        }
    }

    fun stopScanning() {
        this.mainHandler.removeCallbacksAndMessages(null)
        this.isScanning = false
        this.locationManager.removeUpdates(locationListener)
        this.stopWithoutBreaking()
    }
}