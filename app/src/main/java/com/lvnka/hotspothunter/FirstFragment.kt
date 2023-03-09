package com.lvnka.hotspothunter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lvnka.hotspothunter.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    var resultList = ArrayList<ScanResult>()
    lateinit var wifiManager: WifiManager
    private val REQUEST_LOCATION_PERMISSION = 1
    private val requestWifiChangePermission = 2

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            checkWifiPermission()
            resultList = wifiManager.scanResults as ArrayList<ScanResult>
            Log.d("TESTING", "onReceive called!")
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        if (activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) != null) {
            this.wifiManager = activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return
        }
        Log.d("TESTING", "could not create wifiManager")
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    private fun checkWifiPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("TESTING", "FINE LOCATION has not been granted")
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.CHANGE_WIFI_STATE
        ) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TESTING", "CHANGE WIFI STATE has not been granted")
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CHANGE_WIFI_STATE), requestWifiChangePermission)
            return false
        }
        Log.d("TESTING", "Permissions have been granted!")
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION || requestCode == requestWifiChangePermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startScanning()
            }
        }
    }

    private fun startScanning() {
        if (checkWifiPermission()) {
            Log.d("TESTING", "starting scanning?")
            wifiManager.startScan()

            Handler().postDelayed({
                stopScanning()
            }, 10000)
        }
    }

    private fun stopScanning() {
        val axisList = ArrayList<String>()
        for (result in resultList) {
            axisList.add(result.BSSID.toString())
            //axisList.add(result.BSSID.toString(), result.level.toString())
        }
        Log.d("TESTING", axisList.toString())
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            startScanning()
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}