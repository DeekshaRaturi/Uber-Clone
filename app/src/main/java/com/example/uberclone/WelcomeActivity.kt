package com.example.uberclone

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class WelcomeActivity : FragmentActivity() , OnMapReadyCallback,LocationListener
{
    private var map: GoogleMap? = null
    private var geoFire: GeoFire? = null
    private var drivers: DatabaseReference? = null
    private var mcurrent: Marker? = null
    private var location_switch: MaterialAnimatedSwitch? = null
    private var mapFragment: SupportMapFragment? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var locationCallback: LocationCallback
    private var location: Location? = null
    private var polylineList: List<LatLng>? = null
    private var pickupLocationMarker: Marker? = null
    private var v: Float? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private var handler: Handler? = null
    private var index: Int? = null
    private var next: Int? = null
    private var btnGo: Button? = null
    private var searchEditText: EditText? = null
    private var destination: String? = null
    private var polylineOptions: PolylineOptions? = null
    private var blackPolylineOptions: PolylineOptions? = null
    private var polyline: Polyline? = null
    private var greyPolyline: Polyline? = null


    companion object{
        private var MY_PERMISSION_REQUEST_CODE = 7000
        private var PLAY_SERVICE_RES_REQUEST = 7001
        private var UPDATE_INTERVAL = 5000L
        private var FATEST_INTERVAL = 3000L
        private var DISPLACEMENT = 10f
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        init()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun init() {

        var mapFragment: SupportMapFragment= supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        var ref = FirebaseDatabase.getInstance().getReference("Drivers")
        geoFire = GeoFire(ref)
        location_switch = findViewById(R.id.location_switch)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {

                }
            }
        }
        location_switch?.setOnCheckedChangeListener(object : MaterialAnimatedSwitch.OnCheckedChangeListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onCheckedChanged(isOnline: Boolean) {
                if (isOnline){
                    startLocationUpdates()
                    displayLocation()
                    Toast.makeText(this@WelcomeActivity,"You are online",Toast.LENGTH_SHORT).show()
                }else{
                    stopLocationUpdates()
                    Toast.makeText(this@WelcomeActivity,"You are offline",Toast.LENGTH_SHORT).show()
                }
            }
        })
        setUpLocation()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            return
        }
        else{
            if (checkPlayService()){
                buildGoogleApiClient()
                    createLocationRequest()
                if (location_switch!!.isChecked){
                    displayLocation()
                }

            }
        }

    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = UPDATE_INTERVAL
        locationRequest?.fastestInterval = FATEST_INTERVAL
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.setSmallestDisplacement(DISPLACEMENT)




    }

    private fun buildGoogleApiClient() {

    }

    private fun checkPlayService(): Boolean {
        var resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    PLAY_SERVICE_RES_REQUEST
                ).show()
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show()
                finish()
            }
            return false
        }
        return true
    }



    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun displayLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
            return
        }

         fusedLocationProviderClient?.lastLocation!!
            .addOnSuccessListener { location : Location? ->
                Log.e("loca",""+location+" "+location?.latitude)
              if (location!=null){
                  if (location_switch!!.isChecked){
                      var lat = location.latitude
                      var lng = location.longitude

                      // update to firebase
                      geoFire?.setLocation(FirebaseAuth.getInstance().currentUser?.uid,
                          GeoLocation(lat,lng), object : GeoFire.CompletionListener {
                              override fun onComplete(key: String?, error: DatabaseError?) {
//                                  if (mcurrent != null){
                                      mcurrent?.remove()
                                      mcurrent = map?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.transportation)).position(LatLng(lat,lng)).title("you"))
                                     Log.e("curr",""+mcurrent)
                                      map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(lat,lng)))
                                  map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location?.latitude,location.longitude), 15.0f))
//                                  rotateMarker(mcurrent!!,-360,map!!)
                                  // }
                              } })
                  }
                  else{
                      Log.e("curr","fff")

                       Toast.makeText(this,"Can't get your location",Toast.LENGTH_SHORT).show()

                  }
              }
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
     when(requestCode){
         MY_PERMISSION_REQUEST_CODE->{
             if (grantResults?.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                 if (checkPlayService()){
                     buildGoogleApiClient()
                     createLocationRequest()
                     if (location_switch!!.isChecked){
                         displayLocation()
                     }

                 }
             }
         }
     }
       }

    private fun rotateMarker(marker: Marker, i: Int, map: GoogleMap)  {

        var handler = Handler()
        var start = SystemClock.uptimeMillis()
        var startRotation = mcurrent?.rotation
        var duration = 1500

        var interpolator = LinearInterpolator()
        handler?.post(object : Runnable {
            override fun run() {
                var elapsed = SystemClock.uptimeMillis() - start
                var t = interpolator?.getInterpolation((elapsed/duration).toFloat())
                var rot = t*i+(1-t)*startRotation!!
                mcurrent?.rotation = 150f
                if (t<1.0){
                    handler?.postDelayed(this,16)
                }
            }
        })


    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startLocationUpdates() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
               return
        }
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest,
            locationCallback, Looper.getMainLooper())

    }

    override fun onMapReady(p0: GoogleMap?) {
     map = p0

    }

    override fun onLocationChanged(location: Location?) {

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

}
