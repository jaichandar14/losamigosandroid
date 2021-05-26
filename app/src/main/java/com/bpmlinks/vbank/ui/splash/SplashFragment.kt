package com.bpmlinks.vbank.ui.splash


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bpmlinks.vbank.R
import kotlinx.android.synthetic.main.fragment_splash.*


class SplashFragment : Fragment() {

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        var view: View = inflater.inflate(R.layout.fragment_splash, container, false)





//    if (checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED && checkSelfPermission(activity!!,
//                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//        //when permission granted call the method
//        getCurrentLocation()
//
//    }else{
//        //when permission is not granted
//        //request for permission
//
//        requestPermissions( arrayOf(Manifest.permission.ACCESS_FINE_LOCATION
//                ,Manifest.permission.ACCESS_COARSE_LOCATION),100)
//    }

        return view
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode==100 &&(grantResults[0]+grantResults[1]==PackageManager.PERMISSION_GRANTED) )
//
//        {
//            getCurrentLocation()
//        }
//    else{
//            Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
//    }}
//
//
//    @SuppressLint("MissingPermission")
//    private fun getCurrentLocation() {
//
//        lateinit var locationManager: LocationManager
//
//        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//
//
//            client.lastLocation.addOnCompleteListener(object :OnCompleteListener<Location>{
//                override fun onComplete(task:  Task<Location>) {
//                   var  location: Location? = null
//                           location=task.result
//
//                            if (location != null) {
//                                //if location is not null it will print lin log
//                                Log.d(TAG, "longitude:" + location.longitude + "latitude:" + location.latitude)
//                                // Toast.makeText(activity, "${location.longitude}", Toast.LENGTH_SHORT).show()
//                            } else {
//                                //if the location is null it will intitalize the locaton request
//                                var locationRequest: LocationRequest = LocationRequest.create();
//                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                                locationRequest.setInterval(1000);
//                                locationRequest.setFastestInterval(500);
//                                locationRequest.setNumUpdates(100)
//                                var locationCallback: LocationCallback? = object : LocationCallback() {
//                                    override fun onLocationResult(p0: LocationResult?) {
//
//                                        val locations = p0?.lastLocation
//
//                                        Log.d(TAG, "longitude1:" + locations!!.longitude + "latitude1:" + locations.latitude)
//
//
//                                    }
//
//                                }
//                                client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
//                            }
//                }
//            })
//
//        }
//        else{
//            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
//
//          }
//
//
//    }
//



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.status_bar_colore)

        init()


    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun init() {

        var loginAlready = requireActivity().applicationContext.getSharedPreferences("MyUser", Context.MODE_PRIVATE)
        var mailId = loginAlready.getString("MailId","")
        Log.d("TAG", "isLogged: ${mailId}")

        if (!mailId.isNullOrEmpty()){
            val action = SplashFragmentDirections.actionSplashFragmentToLoginFragment()
            findNavController().navigate(action)

        }else{

            img_next.setOnClickListener {

                val action = SplashFragmentDirections.actionSplashFragmentToLoginFragment()
                findNavController().navigate(action)

            }
        }
    }
}



