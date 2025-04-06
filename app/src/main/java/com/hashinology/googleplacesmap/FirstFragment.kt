package com.hashinology.googleplacesmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.*
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.hashinology.googleplacesmap.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var placesClient: PlacesClient

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentPlaceLocations()
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        // Initialize Places SDK
        Places.initialize(requireContext(), getString(R.string.google_maps_API_key))
        placesClient = Places.createClient(requireContext())

        // Initialize AutocompleteFragment by adding the PlaceSelectionListener
        val autocompleteFragment = childFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.DISPLAY_NAME))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Autocomplete", "Selected: ${place.displayName} Id: ${place.id}")
            }

            override fun onError(status: Status) {
                Log.e("Autocomplete", "Error: $status")
            }
        })

        // Check permissions and get current place
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentPlaceLocations()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return binding.root
    }

    private fun getCurrentPlaceLocations() {
        // Explicit permission check
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("CurrentPlace", "Location permission not granted")
            return
        }

        val placeFields = listOf(Place.Field.DISPLAY_NAME, Place.Field.ID)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        try {
            placesClient.findCurrentPlace(request)
                .addOnSuccessListener { response ->
                    if (response.placeLikelihoods.isNullOrEmpty()) {
                        Log.e("CurrentPlace", "No nearby places found")
                        return@addOnSuccessListener
                    }

                    val firstPlaceId = response.placeLikelihoods[0].place.id
                    firstPlaceId?.let {
//                        getDetailsWithPlaceId(it)
                        getDetailsWithPlaceId(response.placeLikelihoods[0].place.id.toString())
                    } ?: Log.e("CurrentPlace", "Invalid place ID")
                    for(placeLikelihood: PlaceLikelihood in response.placeLikelihoods){
                        Log.i(
                            "TAG4",
                            "Place '${placeLikelihood.place.displayName}' has likelihood: ${placeLikelihood.likelihood} id : ${placeLikelihood.place.id}"
                        )
                    }
                }
                .addOnFailureListener { exception ->
                    when (exception) {
                        is ApiException -> {
                            Log.e(
                                "CurrentPlace",
                                "API error: ${exception.message} (Code: ${exception.statusCode})"
                            )
                        }

                        is SecurityException -> {
                            Log.e("CurrentPlace", "Security error: ${exception.message}")
                        }

                        else -> {
                            Log.e("CurrentPlace", "Unknown error: ${exception.message}")
                        }
                    }
                }
        } catch (e: SecurityException) {
            Log.e("CurrentPlace", "Security exception: ${e.message}")
        }
    }

    /*private fun getDetailsWithPlaceId(placeId: String) {
        if (placeId.isBlank()) {
            Log.e("PlaceDetails", "Invalid place ID")
            return
        }

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.ADDRESS,
            Place.Field.BUSINESS_STATUS,
            Place.Field.OPENING_HOURS
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                Log.d("PlaceDetails",
                    "Place: ${place.name ?: "N/A"}, " +
                            "Address: ${place.address ?: "N/A"}, " +
                            "Status: ${place.businessStatus ?: "N/A"}, " +
                            "Hours: ${place.openingHours ?: "N/A"}"
                )
            }
            .addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e("PlaceDetails", "Fetch failed: ${exception.message} (Code: ${exception.statusCode})")
                } else {
                    Log.e("PlaceDetails", "Fetch failed: ${exception.message}")
                }
            }
    }*/

    private fun getDetailsWithPlaceId(placeId: String) {

// Specify the fields to return.
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.BUSINESS_STATUS,
            Place.Field.OPENING_HOURS
        )

// Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.d(
                    "TAG",
                    "Place found: ${place.name}, Address: ${place.address}, Business Status: ${place.businessStatus}, Openning Hours: ${place.openingHours}"
                )
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e("TAG", "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode

                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}