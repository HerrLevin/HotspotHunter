package com.lvnka.hotspothunter

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.nio.charset.Charset

class Uploader(activity: Activity) {
    private val queue = Volley.newRequestQueue(activity.applicationContext)
    private lateinit var url: String
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)

    private fun prepareUrl() {
        url = prefs.getString("hostname", "localhost").toString()
        url = "https://$url/api/v1/scan"
    }

    fun test() {
        //val test = queue.add(stringRequest)
        var jsonBody = JSONObject()
        jsonBody.put("firstKey", "firstValue")
        jsonBody.put("secondKey", "secondValue")
        val requestBody = jsonBody.toString()
        this.prepareUrl()

        val stringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener { response ->
                Log.i("LOG_VOLLEY", response)
            },
            Response.ErrorListener { error ->
                Log.e("LOG_VOLLEY", error.toString())
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