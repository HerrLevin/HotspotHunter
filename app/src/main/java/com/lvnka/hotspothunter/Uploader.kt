package com.lvnka.hotspothunter

import android.app.Activity
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class Uploader(activity: Activity) {
    private val activity = activity
    private lateinit var url: String
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

    private fun prepareUrl() {
        url = prefs.getString("hostname", "localhost").toString()
        url = "https://$url/api/v1/scan"
    }

    fun upload(wifiResults: ArrayList<ScanResult>, latitude: Double, longitude: Double) {
        if (!prefs.getBoolean("upload", false) && wifiResults.isNotEmpty()) {
            return
        }
        Log.d("UPLOADER", "Trying to upload")

        val queue = Volley.newRequestQueue(activity.applicationContext)
        val wifiArray = JSONArray()
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        for (wifi in wifiResults) {
            val wifiElement = JSONObject()
            wifiElement.put("bssid", wifi.BSSID)
            wifiElement.put("ssid", wifi.SSID)
            wifiElement.put("signal", wifi.level)
            wifiElement.put("quality", WifiManager.calculateSignalLevel(wifi.level, 100).toString() + "/100")
            wifiElement.put("frequency", wifi.frequency)
            wifiElement.put("encrypted", wifi.capabilities.length > 5)
            wifiElement.put("channel", wifi.channelWidth)
            if (latitude != 0.0 && longitude != 0.0) {
                wifiElement.put("latitude", latitude)
                wifiElement.put("longitude", longitude)
            }
            wifiElement.put("created_at", dateTime.format(Date()))

            wifiArray.put(wifiElement)
        }

        val requestBody = wifiArray.toString()
        this.prepareUrl()

        val stringRequest = object : StringRequest(
            Method.POST,
            this.url,
            Response.Listener { response ->
                Log.i("LOG_VOLLEY", response)
            },
            Response.ErrorListener { error ->
                Log.e("LOG_VOLLEY", error.toString())

                Snackbar.make(
                    activity.window.decorView.rootView,
                    "Could not connect to ${this.url}",
                    Snackbar.LENGTH_SHORT
                ).setAction("Action", null).show()
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = super.getHeaders().toMutableMap()
                headers["Authentication"] = prefs.getString("token", "This should not be visible. Is your app set up correctly?")
                return headers
            }

            override fun getBody(): ByteArray? {
                return requestBody.toByteArray(Charset.forName("utf-8"))
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                var responseString = ""
                if (response != null) {
                    responseString = response.statusCode.toString()
                }
                return Response.success(
                    responseString,
                    HttpHeaderParser.parseCacheHeaders(response)
                )
            }
        }

        queue.add(stringRequest)
    }
}