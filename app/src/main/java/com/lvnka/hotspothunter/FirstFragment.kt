package com.lvnka.hotspothunter

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.snackbar.Snackbar
import com.lvnka.hotspothunter.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@Suppress("DEPRECATION")
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var wifiScanner: WifiScanner


    override fun onStart() {
        super.onStart()
        this.wifiScanner = WifiScanner(activity!!)
        requireActivity().registerReceiver(this.wifiScanner, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(this.wifiScanner)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == wifiScanner.requestLocationPermission || requestCode == wifiScanner.requestWifiPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                this.wifiScanner.prepareScanning()
            }
        }
    }



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                updateButton()
                binding.textView2.text = this@FirstFragment.wifiScanner.location
                updateList()
                mainHandler.postDelayed(this, 500)
            }
        })


        binding.buttonScan.setOnClickListener {
            if (!this.wifiScanner.isScanning()) {
                this.wifiScanner.prepareScanning()
                Snackbar.make(view, "Starting Wifi Scan!", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            } else {
                this.wifiScanner.stopScanning()
            }
            updateButton()
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun updateButton() {
        if (this.wifiScanner.isScanning()) {
            binding.buttonScan.text = resources.getString(R.string.scan_stop)
        } else {
            binding.buttonScan.text = resources.getString(R.string.scan)
        }
    }

    private fun updateList() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(activity!!.applicationContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, this.wifiScanner.resultText)
        binding.wifiList.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this._binding = null
    }
}