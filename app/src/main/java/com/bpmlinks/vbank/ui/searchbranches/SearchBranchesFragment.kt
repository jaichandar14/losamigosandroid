package com.bpmlinks.vbank.ui.searchbranches

import com.bpmlinks.vbank.R
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentSearchBranchesBinding
import com.google.android.gms.location.*
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_search_branches.*
import java.lang.StringBuilder
import java.util.*
import javax.inject.Inject
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.model.*
import com.bpmlinks.vbank.twilio.VideoActivity
import com.bpmlinks.vbank.ui.searchbranches.adapter.SearchBranchesAdapter
import com.vbank.vidyovideoview.connector.UserDataParams
import com.vbank.vidyovideoview.helper.BundleKeys
import kotlinx.android.synthetic.main.fragment_search_branches.progress_bar
import kotlinx.android.synthetic.main.fragment_search_branches.recycler_view
import kotlinx.android.synthetic.main.fragment_search_branches.tool_bar
import java.lang.Exception

class SearchBranchesFragment :
    BaseFragment<FragmentSearchBranchesBinding, SearchBranchesViewModel>(), View.OnClickListener,
    androidx.appcompat.widget.SearchView.OnQueryTextListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
        private const val GPS_REQUEST = 201
    }

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun getViewModel(): SearchBranchesViewModel? =
        ViewModelProvider(this, factory).get(SearchBranchesViewModel::class.java)

    private var adapter: SearchBranchesAdapter? = null
    private var branchInfoItem: List<BranchDtosItem>? = null

    override fun getBindingVariable(): Int = BR.searchBankerVM

    override fun getContentView(): Int = R.layout.fragment_search_branches

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        init()
    }

    private fun init() {
        tool_bar.setNavigationOnClickListener { findNavController().popBackStack() }
        initRecyclerAdapter()
        tool_bar.setOnClickListener(this)
        txtUserLocation.setOnClickListener(this)
        searchView.setOnQueryTextListener(this)
        activity?.let { activity ->
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        }
        if (getViewModel()?.zipcode?.value?.zipCode.isNullOrBlank()){
            requestPermission()
        }else{
            getBranches(getViewModel()?.zipcode?.value?.zipCode,
                getViewModel()?.zipcode?.value?.currentLocation)
        }
    }

    private fun initRecyclerAdapter() {
        adapter = SearchBranchesAdapter(this)
        recycler_view.adapter = adapter
    }

    private fun getBranches(places: String?, currentLocation: Boolean?) {
        if (checkInternetAvailable()) {
            getViewModel()?.getBranchesByZipCode()
                ?.observe(viewLifecycleOwner, Observer { apiResponse ->
                    when (apiResponse) {
                        is ApisResponse.Success -> {
                            branchInfoItem = apiResponse.response.data.branchDtos
                            updateBranches(branchInfoItem)
                        }
                        is ApisResponse.Error -> {
                            showToast(apiResponse.exception.localizedMessage!!)
                        }
                        ApisResponse.LOADING -> {
                            showProgress()
                        }
                        ApisResponse.COMPLETED -> {
                            hideProgress()
                        }
                    }
                })
            val zipCode = ZipCode(places,currentLocation)
            getViewModel()?.setZipCode(zipCode)
          currentLocation?.let {
              if (it) {
                  searchView.setQuery("", false)
                  searchView.clearFocus()
              }
          }
        }
    }


    private fun updateBranches(branchInfoItem: List<BranchDtosItem>?) {
        if (branchInfoItem.isNullOrEmpty()) {
            isBranchesAvailable(false)
        } else {
            adapter?.setBranchesList(branchInfoItem)
            isBranchesAvailable(true)
        }
    }

    private fun isBranchesAvailable(available: Boolean) {
        if (available) {
            recycler_view.visibility = View.VISIBLE
            txtNoBranchesAvailable.visibility = View.GONE
        } else {
            recycler_view.visibility = View.GONE
            txtNoBranchesAvailable.visibility = View.VISIBLE
        }
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.txtUserLocation -> {
                requestPermission()
            }
            R.id.btn_call_back -> {
                val branchItem = v.getTag(R.id.btn_call_back) as BranchDtosItem
                callBackDialog(branchItem)
            }
            R.id.btn_connecting_now -> {
                val branchItem = v.getTag(R.id.btn_connecting_now) as BranchDtosItem
                showBankerOptions(branchItem)
            }
        }
    }

    private fun requestPermission() {
        if (checkInternetAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!checkLocationPermission()) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_CODE
                    )
                } else {
                    checkLocationEnabled()
                }
            } else {
                checkLocationEnabled()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        activity?.let {
            val result1 =
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)
            return result1 == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                checkLocationEnabled()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_REQUEST) {
            if (isEnabledLocation()) {
                showProgress()
                obtainUserLocation()
            }
        }
    }

    private fun isEnabledLocation(): Boolean {
        val locationManager: LocationManager? =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }
        return false
    }

    private fun checkLocationEnabled() {
        if (isEnabledLocation()) {
            showProgress()
            obtainUserLocation()
        } else {
            showPermissionAlert()
        }
    }

    private fun obtainUserLocation() {
        requestNewLocationData()
    }

    private fun requestNewLocationData() {
        startLocationUpdates()
    }

    private var locationCallback: LocationCallback? = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            val locations = p0?.locations
            if (locations != null) {
                updateLocation(locations[0].longitude, locations[0].latitude)
            }
        }
    }

    private fun updateLocation(longitude: Double, latitude: Double) {
        setLocationName(longitude, latitude)
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.numUpdates = 1
        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun showPermissionAlert() {
        val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.alert_location_permission_title))
            builder.setMessage(getString(R.string.alert_location_permission_msg))

            builder.setPositiveButton(getString(R.string.all_alert_dialog_yes)) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, GPS_REQUEST)
            }
            builder.setNeutralButton(getString(R.string.all_alert_dialog_cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(true)
            alertDialog.show()
    }

    private fun setLocationName(lan: Double, lat: Double) {
        try {
            val geoCoder = Geocoder(activity, Locale.getDefault())
            val addresses: List<Address> = geoCoder.getFromLocation(lat, lan, 1)
            val locality: String? = addresses[0].locality
            val subLocality: String? = addresses[0].subLocality
            val cityName: String? = addresses[0].subAdminArea
            val admin: String? = addresses[0].adminArea
            val zipCode: String? = addresses[0].postalCode

            val stringBuilder = StringBuilder()
            if (subLocality == null) {
                if (locality == null) {
                    stringBuilder.append(cityName).append(",").append("\t").append(admin)
                } else {
                    stringBuilder.append(locality).append(",").append("\t").append(cityName)
                }
            } else {
                stringBuilder.append(subLocality).append(",").append("\t").append(cityName)
            }

            getBranches(zipCode, true)

            getViewModel()?.userCurrentLocation?.value = stringBuilder.toString()
        } catch (e: Exception) {
            isBranchesAvailable(false)
            e.printStackTrace()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        getBranches(query, false)
        searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun showBankerOptions(branchItem: BranchDtosItem) {
        activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)
            val alertLayout = layoutInflater.inflate(R.layout.dialog_banker_options, null)
            val btnYes = alertLayout.findViewById<AppCompatButton>(R.id.btnYes)
            val btnNo = alertLayout.findViewById<AppCompatButton>(R.id.btnNo)
            val imgClose = alertLayout.findViewById<AppCompatImageView>(R.id.img_close)
            builder.setView(alertLayout)

            val alertDialog: AlertDialog? = builder.create()
            alertDialog?.setCancelable(true)

            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            alertDialog?.show()
            alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            alertDialog?.window?.setLayout(
                (width * 0.75).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

            btnYes.setOnClickListener {
                showBanker(branchItem)
                alertDialog?.dismiss()
            }
            btnNo.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectMeeting(branchItem)
                } else {
                    val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    val customerKeyNb = sharedPref.getInt(AppConstants.CUSTOMER_KEY_NB, 0)
                    val userDataParams = UserDataParams(
                        customerKeyNb = customerKeyNb,
                        branchKeyNb = branchItem.branchKeyNb
                    )
//                  val bundle = bundleOf(BundleKeys.MeetingParams to userDataParams)
                    val intent = Intent(activity, VideoActivity::class.java)
                    intent.putExtra(BundleKeys.UserDataParams, userDataParams)
                    startActivity(intent)
                }

                alertDialog?.dismiss()
            }
            imgClose.setOnClickListener {
                alertDialog?.dismiss()
            }
        }
    }

    private fun callBackDialog(branchItem: BranchDtosItem) {
        val callbackRequest = CallbackRequest()
        callbackRequest.branchKeyNb = branchItem.branchKeyNb
        val action = SearchBranchesFragmentDirections.actionSearchBankerFragmentToCallbackDialogFragment(callbackRequest, branchItem)
        findNavController().navigate(action)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun connectMeeting(branch: BranchDtosItem) {
        val bankerItem = BankerDtosItem()
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val downSpeed = nc?.linkDownstreamBandwidthKbps
        if (checkInternetAvailable()) {
            downSpeed?.let {
                bankerItem.currentSpeed = downSpeed / (1024 * 128)
                if (bankerItem.currentSpeed >= 3) {
                    val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    val customerKeyNb = sharedPref.getInt(AppConstants.CUSTOMER_KEY_NB, 0)
                    val customerName = sharedPref.getString(AppConstants.CUSTOMER_NAME, "")

                    val userDataParams = UserDataParams(
                        customerKeyNb = customerKeyNb, branchKeyNb = branch.branchKeyNb,
                        displayName = customerName
                    )
                    val intent = Intent(activity, VideoActivity::class.java)
                    intent.putExtra(BundleKeys.UserDataParams, userDataParams)
                    startActivity(intent)
                } else {
                    val action =
                        SearchBranchesFragmentDirections.actionSearchBankerFragmentToNetworkSpeedFragment(
                            bankerItem
                        )
                    findNavController().navigate(action)
                }
            }
        }

    }

    private fun showBanker(branchItem: BranchDtosItem) {
        val action =
            SearchBranchesFragmentDirections.actionSearchBankerFragmentToChooseBankerFragment(
                branchItem
            )
        findNavController().navigate(action)
    }

    override fun internetConnected() {
        if (branchInfoItem.isNullOrEmpty()) {
            requestPermission()
        } else {
            updateBranches(branchInfoItem)
        }
    }

}