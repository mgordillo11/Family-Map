package com.example.family_map;

import android.content.Intent;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
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
    private List<Polyline> currentPolyLines = new ArrayList<>();
    private Map<String, Event> mapEvents;

    public MapsFragment() {
        this.mapEvents = new HashMap<>();
    }

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
            if (eventID != null) {
                setHasOptionsMenu(false);
            }

            if (DataCache.getInstance().settingsUpdate() != null) {
                mapEvents.clear();

                if (eventID != null) {
                    for (Event maleEvents : DataCache.getInstance().getMaleEvents()) {
                        mapEvents.put(maleEvents.getEventID(), maleEvents);
                    }

                    for (Event femaleEvents : DataCache.getInstance().getFemaleEvents()) {
                        mapEvents.put(femaleEvents.getEventID(), femaleEvents);
                    }
                    // mapEvents.addAll(DataCache.getInstance().getMaleEvents());
                    //mapEvents.addAll(DataCache.getInstance().getFemaleEvents());
                } else {
                    //mapEvents.addAll(DataCache.getInstance().settingsUpdate());
                    List<Event> temp = DataCache.getInstance().settingsUpdate();
                    for (Event settingEvents : DataCache.getInstance().settingsUpdate()) {
                        mapEvents.put(settingEvents.getEventID(), settingEvents);
                    }
                }

                for (Map.Entry<String, Event> event : mapEvents.entrySet()) {
                    float color;

                    Event eventEntry = event.getValue();
                    switch (eventEntry.getEventType().toLowerCase()) {
                        case "birth":
                            color = BitmapDescriptorFactory.HUE_MAGENTA;
                            break;
                        case "marriage":
                            color = BitmapDescriptorFactory.HUE_BLUE;
                            break;
                        case "death":
                            color = BitmapDescriptorFactory.HUE_GREEN;
                            break;
                        default:
                            color = BitmapDescriptorFactory.HUE_CYAN;
                            break;
                    }

                    Person currentPerson = DataCache.getInstance().getFamilyPeople().get(eventEntry.getPersonID());

                    Vector<String> markerInfo = new Vector<>();
                    markerInfo.add(0, eventEntry.getPersonID());
                    markerInfo.add(1, currentPerson.getGender());
                    markerInfo.add(2, eventEntry.getEventID());

                    String markerTitle = currentPerson.getFirstName() + " " + currentPerson.getLastName() + "\n"
                            + eventEntry.getEventType().toUpperCase() + ": " + eventEntry.getCity() + ", " +
                            eventEntry.getCountry() + " (" + eventEntry.getYear() + ")";

                    googleMap.addMarker(new MarkerOptions().position(new LatLng(eventEntry.getLatitude(),
                            eventEntry.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(color)).title(markerTitle)).setTag(markerInfo);

                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            for (Polyline currentLine : currentPolyLines) {
                                currentLine.remove();
                            }
                            currentPolyLines.clear();

                            Vector<String> markerData = (Vector<String>) marker.getTag();

                            currentPersonID = markerData.get(0);
                            currentGender = markerData.get(1);

                            if (currentGender.equals("m")) {
                                detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                            } else {
                                detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                            }

                            //Event clickedMarkerEvent = DataCache.getInstance().getEvents().get(markerData.get(2));
                            Event clickedMarkerEvent = mapEvents.get(markerData.get(2));
                            Person currentPerson = DataCache.getInstance().getFamilyPeople().get(currentPersonID);

                            if (currentPerson.getSpouseID() != null && DataCache.getSettings().spouseLines) {
                                Person currentSpouse = DataCache.getInstance().getFamilyPeople().get(currentPerson.getSpouseID());
                                Event testEvent = null;

                                for(Map.Entry<String, Event> tempEvent : mapEvents.entrySet()) {
                                    if(tempEvent.getValue().getPersonID().equals(currentSpouse.getPersonID())) {
                                        testEvent = tempEvent.getValue();
                                        break;
                                    }
                                }

                                if (testEvent != null) {
                                    List<Event> spouseEvents = DataCache.getInstance().getPersonEvents().get(currentSpouse.getPersonID());
                                    int earliestEvent = spouseEvents.get(0).getYear();
                                    Event spouseEarliestEvent = spouseEvents.get(0);

                                    for (int i = 0; i < spouseEvents.size(); i++) {
                                        if (earliestEvent > spouseEvents.get(i).getYear()) {
                                            earliestEvent = spouseEvents.get(i).getYear();
                                            spouseEarliestEvent = spouseEvents.get(i);
                                        }
                                    }

                                    LatLng spouseLatLng = new LatLng(spouseEarliestEvent.getLatitude(), spouseEarliestEvent.getLongitude());
                                    LatLng personLatLng = new LatLng(clickedMarkerEvent.getLatitude(), clickedMarkerEvent.getLongitude());


                                    currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).add(spouseLatLng, personLatLng).color(Color.RED)));
                                }
                            }

                            if (DataCache.getSettings().lifeStoryLines) {
                                List<Event> currentPersonEvents = DataCache.getInstance().getPersonEvents().get(currentPerson.getPersonID());
                                List<LatLng> eventLocations = new ArrayList<>();

                                int earliestEvent = currentPersonEvents.get(0).getYear();

                                for (int i = 0; i < currentPersonEvents.size(); i++) {
                                    LatLng currentLatLng = new LatLng(currentPersonEvents.get(i).getLatitude(),
                                            currentPersonEvents.get(i).getLongitude());

                                    if (earliestEvent > currentPersonEvents.get(i).getYear()) {
                                        eventLocations.add(currentLatLng);
                                    } else {
                                        eventLocations.add(currentLatLng);
                                    }
                                }

                                currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).addAll(eventLocations).color(Color.BLUE)));
                            }

                            if (DataCache.getSettings().familyTreeLines) {
                                assert clickedMarkerEvent != null;
                                addFamilyTreeLines(googleMap, clickedMarkerEvent.getEventID(), 20, currentPolyLines);
                            }

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
            System.out.println("Test");
        }
    };

    public void addFamilyTreeLines(GoogleMap googleMap, String currentEventID, int currentPolylineWidth, List<Polyline> currentPolyLines) {
        Event currentEvent = DataCache.getInstance().getEvents().get(currentEventID);
        Person currentPerson = DataCache.getInstance().getFamilyPeople().get(currentEvent.getPersonID());

        Event earliestMotherEvent = null;
        Event earliestFatherEvent = null;

        if (currentPerson.getMotherID() != null && currentPerson.getFatherID() != null) {
            earliestMotherEvent = DataCache.getInstance().getEarliestEvent(currentPerson.getMotherID());
            earliestFatherEvent = DataCache.getInstance().getEarliestEvent(currentPerson.getFatherID());

            List<LatLng> currentMotherSide = new ArrayList<>();
            currentMotherSide.add(new LatLng(currentEvent.getLatitude(), currentEvent.getLongitude()));
            currentMotherSide.add(new LatLng(earliestMotherEvent.getLatitude(), earliestMotherEvent.getLongitude()));

            List<LatLng> currentFatherSide = new ArrayList<>();
            currentFatherSide.add(new LatLng(currentEvent.getLatitude(), currentEvent.getLongitude()));
            currentFatherSide.add(new LatLng(earliestFatherEvent.getLatitude(), earliestFatherEvent.getLongitude()));

            currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).addAll(currentMotherSide).color(Color.CYAN).width(currentPolylineWidth)));
            currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).addAll(currentFatherSide).color(Color.CYAN).width(currentPolylineWidth)));
        }

        if (currentPerson.getMotherID() != null) {
            addFamilyTreeLines(googleMap, earliestMotherEvent.getEventID(), currentPolylineWidth - 3, currentPolyLines);
        }

        if (currentPerson.getFatherID() != null) {
            addFamilyTreeLines(googleMap, earliestFatherEvent.getEventID(), currentPolylineWidth - 3, currentPolyLines);
        }

    }

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