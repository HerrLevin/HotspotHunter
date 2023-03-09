package com.lvnka.hotspothunter

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
                this.wifiScanner.startScanning()
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

        val tutorials = listOf<String>("Algorithms", "Data Structures",
            "Languages", "Interview Corner",
            "GATE", "ISRO CS",
            "UGC NET CS", "CS Subjects",
            "Web Technologies")

        var adapter: ArrayAdapter<String> = ArrayAdapter<String>(activity!!.applicationContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, tutorials)

        binding.wifiList.adapter = adapter

        binding.buttonScan.setOnClickListener {
            if (!this.wifiScanner.isScanning()) {
                binding.buttonScan.text = resources.getString(R.string.scan)
                this.wifiScanner.startScanning()
            } else {
                binding.buttonScan.text = resources.getString(R.string.scan_stop)
                this.wifiScanner.stopScanning()
            }
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this._binding = null
    }
}