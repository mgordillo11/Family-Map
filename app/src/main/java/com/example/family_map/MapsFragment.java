package com.example.family_map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import Models.Event;
import Models.Person;

public class MapsFragment extends Fragment {
    private TextView detailedView;
    private String currentPersonID;
    private String currentGender;
    private String eventID;
    private String zoomedEvent;

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
            for (Map.Entry<String, Event> eventEntry : DataCache.getInstance().getEvents().entrySet()) {
                float color;
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
                markerInfo.add(2, currentEvent.getEventID());

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

                        if (currentGender.equals("m")) {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                        } else {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                        }

                        /*Event clickedMarkerEvent = DataCache.getInstance().getEvents().get(markerInfo.get(2));

                        Person currentPerson = DataCache.getInstance().getFamilyPeople().get(currentPersonID);
                        Person currentSpouse = null;

                        for (Map.Entry<String, Person> spouseSearch : DataCache.getInstance().getFamilyPeople().entrySet()) {
                            assert currentPerson != null;
                            if (spouseSearch.getValue().getPersonID().equals(currentPerson.getSpouseID())) {
                                currentSpouse = spouseSearch.getValue();
                            }
                        }

                        if (currentSpouse != null) {
                            Event spouseEarliestEvent = null;
                            List<Event> spouseEvents = DataCache.getInstance().getPersonEvents().get(currentSpouse.getPersonID());
                            for (int i = 0; i < spouseEvents.size(); i++) {
                                if (spouseEvents.get(i).getEventType().equals("Birth")) {
                                    spouseEarliestEvent = spouseEvents.get(i);
                                    break;
                                }
                            }

                            LatLng spouseLatLng = new LatLng(spouseEarliestEvent.getLatitude(), spouseEarliestEvent.getLongitude());
                            LatLng personLatLng = new LatLng(clickedMarkerEvent.getLatitude(), clickedMarkerEvent.getLongitude());

                            googleMap.addPolyline(new PolylineOptions().clickable(false).add(personLatLng, spouseLatLng));
                        }*/

                        detailedView.setText(marker.getTitle());
                        return true;
                    }
                });
            }

            if (eventID != null) {
                Event markerPressedEvent = DataCache.getInstance().getEvents().get(eventID);

                assert markerPressedEvent != null;
                LatLng markerLocation = new LatLng(markerPressedEvent.getLatitude(), markerPressedEvent.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 10));

                detailedView.setText(zoomedEvent);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchButton:
                Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.settingsButton:
                Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        detailedView = view.findViewById(R.id.markerInfo);

        if (getArguments() != null) {
            eventID = getArguments().getString("eventID");
            zoomedEvent = getArguments().getString("eventInfo");
        }

        detailedView.setOnClickListener(detailedView -> {
            if (currentPersonID != null) {
                Intent intent = new Intent(detailedView.getContext(), PersonActivity.class);
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