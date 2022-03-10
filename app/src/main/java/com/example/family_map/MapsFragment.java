package com.example.family_map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;
import java.util.Vector;

import Models.Event;
import Models.Person;

public class MapsFragment extends Fragment {
    private TextView detailedView;
    private String currentPersonID;
    private String currentGender;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            for(Map.Entry<String, Event> eventEntry : DataCache.getInstance().getEvents().entrySet()) {
                float color;
                //float zIndex;
                switch (eventEntry.getValue().getEventType()) {
                    case "Birth":
                        color = BitmapDescriptorFactory.HUE_MAGENTA;
                        break;
                    case "Marriage":
                        color = BitmapDescriptorFactory.HUE_BLUE;
                        break;
                    case "Death":
                        color = BitmapDescriptorFactory.HUE_GREEN;
                        break;
                    default:
                        color = BitmapDescriptorFactory.HUE_CYAN;
                        break;
                }

                Event currentEvent = eventEntry.getValue();
                Person currentPerson = DataCache.getInstance().getFamilyPeople().get(currentEvent.getPersonID());

                Vector<String> markerInfo = new Vector<>();
                markerInfo.add(0, currentEvent.getPersonID());
                markerInfo.add(1, currentPerson.getGender());

                String markerTitle = currentPerson.getFirstName() + " " + currentPerson.getLastName() + "\n"
                        + currentEvent.getEventType().toUpperCase() + ": " + currentEvent.getCity() + ", " +
                        currentEvent.getCountry() + " (" + currentEvent.getYear() + ")";

                googleMap.addMarker(new MarkerOptions().position(new LatLng(eventEntry.getValue().getLatitude(),
                        eventEntry.getValue().getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(color)).title(markerTitle)).setTag(markerInfo);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Vector<String> markerData = (Vector<String>) marker.getTag();

                        currentPersonID = markerData.get(0);
                        currentGender = markerData.get(1);

                        if(currentGender.equals("m")) {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                        } else {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                        }

                        detailedView.setText(marker.getTitle());
                        return true;
                    }
                });
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        detailedView =  view.findViewById(R.id.markerInfo);

        detailedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PersonActivity.class);
                intent.putExtra("personID", currentPersonID);
                startActivity(intent);
            }
        });
        return view;
        //return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}