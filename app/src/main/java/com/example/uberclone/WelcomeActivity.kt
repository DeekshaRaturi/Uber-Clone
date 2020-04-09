package com.example.uberclone

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference


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

    companion object{
        private var MY_PERMISSION_REQUEST_CODE = 7000
        private var PLAY_SERVICE_RES_REQUEST = 7001
        private var UPDATE_INTERVAL = 5000
        private var FATEST_INTERVAL = 3000
        private var DISPLACEMENT = 10

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        init()
    }

    private fun init() {

        var mapFragment: SupportMapFragment= supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
              if (location!=null){
                  if (location_switch!!.isChecked){
                      var lat = location.latitude
                      var lng = location.longitude

                      // update to firebase
                      geoFire?.setLocation(FirebaseAuth.getInstance().currentUser?.uid,
                          GeoLocation(lat,lng), object : GeoFire.CompletionListener {
                              override fun onComplete(key: String?, error: DatabaseError?) {
                                  if (mcurrent != null){
                                      mcurrent?.remove()
                                      mcurrent = map?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_background)).position(LatLng(lat,lng)).title("you"))
                                      map?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(lat,lng)))
                                  }
                              } })
                  }else{

                  }
              }
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startLocationUpdates() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
               return
        }
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

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
