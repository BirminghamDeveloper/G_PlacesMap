package com.hashinology.googleplacesmap

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.hashinology.googleplacesmap.databinding.FragmentFirstBinding


class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        // initialise the SDK
        Places.initialize(requireContext(), getString(R.string.google_maps_API_key))

        // Create a new PlacesClient Instance
        val placeClient = Places.createClient(requireContext())

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            /*activity?.supportFragmentManager?.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment*/
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.DISPLAY_NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i("TAG", "Place: ${place.displayName}, ${place.id} ")
            }

            override fun onError(status: Status) {
                Log.i("TAG", "An error occurred: $status")
            }
        })

        return binding!!.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}