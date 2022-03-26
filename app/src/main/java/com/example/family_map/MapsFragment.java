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
    private String eventActivityID; //This is what determines if it's the event activity
    private String eventActivityInfo;
    private final List<Polyline> currentPolyLines = new ArrayList<>();
    private final Map<String, Event> mapEvents;
    private final List<Float> colors = new ArrayList<>();
    private final Map<String, Float> uniqueEventMarker = new HashMap<>();
    private final List<Float> usedMarkerColors = new ArrayList<>();

    public MapsFragment() {
        this.mapEvents = new HashMap<>();

        colors.add(BitmapDescriptorFactory.HUE_BLUE);
        colors.add(BitmapDescriptorFactory.HUE_ORANGE);
        colors.add(BitmapDescriptorFactory.HUE_ROSE);
        colors.add(BitmapDescriptorFactory.HUE_GREEN);
        colors.add(BitmapDescriptorFactory.HUE_AZURE);
        colors.add(BitmapDescriptorFactory.HUE_CYAN);
        colors.add(BitmapDescriptorFactory.HUE_MAGENTA);
        colors.add(BitmapDescriptorFactory.HUE_RED);
        colors.add(BitmapDescriptorFactory.HUE_VIOLET);
        colors.add(BitmapDescriptorFactory.HUE_YELLOW);
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
            if (eventActivityID != null) {
                setHasOptionsMenu(false);
            }

            if (DataCache.getInstance().getEventsBySettings() != null) {
                //Clears all saved event in case settings were changed
                mapEvents.clear();

                if (eventActivityID != null) {
                    for (Event maleEvents : DataCache.getInstance().getMaleEvents()) {
                        mapEvents.put(maleEvents.getEventID(), maleEvents);
                    }
                    for (Event femaleEvents : DataCache.getInstance().getFemaleEvents()) {
                        mapEvents.put(femaleEvents.getEventID(), femaleEvents);
                    }
                } else {
                    for (Event settingEvents : DataCache.getInstance().getEventsBySettings()) {
                        mapEvents.put(settingEvents.getEventID(), settingEvents);
                    }
                }

                for (Map.Entry<String, Event> event : mapEvents.entrySet()) {
                    float markerColor;

                    Event currentMarkerEvent = event.getValue();
                    markerColor = getUniqueMarkerColor(currentMarkerEvent.getEventType().toLowerCase());

                    Person currentPersonOfEvent = DataCache.getInstance().getPersonMap()
                            .get(currentMarkerEvent.getPersonID());

                    Vector<String> markerInfo = new Vector<>();
                    markerInfo.add(0, currentMarkerEvent.getPersonID());
                    markerInfo.add(1, currentPersonOfEvent.getGender());
                    markerInfo.add(2, currentMarkerEvent.getEventID());

                    String markerTitle = currentPersonOfEvent.getFirstName() + " "
                            + currentPersonOfEvent.getLastName() + "\n"
                            + currentMarkerEvent.getEventType().toUpperCase() + ": "
                            + currentMarkerEvent.getCity() + ", " +
                            currentMarkerEvent.getCountry() + " ("
                            + currentMarkerEvent.getYear() + ")";

                    googleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerEvent.getLatitude(),
                            currentMarkerEvent.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                            .title(markerTitle)).setTag(markerInfo);

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

                            Event clickedMarkerEvent = mapEvents.get(markerData.get(2));
                            DataCache.getInstance().currentClickedEvent = clickedMarkerEvent;

                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    clickedMarkerEvent.getLatitude(), clickedMarkerEvent.getLongitude()
                            ), 2));

                            Person currentPerson = DataCache.getInstance().getPersonMap().get(currentPersonID);

                            if (currentPerson.getSpouseID() != null && DataCache.getSettings().spouseLines) {
                                Person currentSpouse = DataCache.getInstance().getPersonMap().get(currentPerson.getSpouseID());
                                Event testEvent = null;

                                for (Map.Entry<String, Event> tempEvent : mapEvents.entrySet()) {
                                    if (tempEvent.getValue().getPersonID().equals(currentSpouse.getPersonID())) {
                                        testEvent = tempEvent.getValue();
                                        break;
                                    }
                                }

                                if (testEvent != null) {
                                    List<Event> spouseEvents = DataCache.getInstance().getEventsOfPerson().get(currentSpouse.getPersonID());
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
                                List<Event> currentPersonEvents = DataCache.getInstance().getEventsOfPerson().get(currentPerson.getPersonID());
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
                                addFamilyTreeLines(googleMap, clickedMarkerEvent.getEventID(), 20, currentPolyLines);
                            }

                            DataCache.getInstance().currentClickedEventInfo = marker.getTitle();
                            detailedView.setText(marker.getTitle());
                            return true;
                        }
                    });
                }

                if (eventActivityID != null) {
                    Event markerPressedEvent = DataCache.getInstance().getEvents().get(eventActivityID);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                            markerPressedEvent.getLatitude(), markerPressedEvent.getLongitude()
                    ), 2));

                    for (Polyline currentLine : currentPolyLines) {
                        currentLine.remove();
                    }
                    currentPolyLines.clear();

                    Person personRelatedToEvent = DataCache.getInstance().getPersonMap().get(markerPressedEvent.getPersonID());
                    currentPersonID = personRelatedToEvent.getPersonID();
                    currentGender = personRelatedToEvent.getGender();

                    if (currentGender.equals("m")) {
                        detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                    } else {
                        detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                    }

                    Person currentPerson = DataCache.getInstance().getPersonMap().get(currentPersonID);

                    Person currentSpouse = DataCache.getInstance().getPersonMap().get(currentPerson.getSpouseID());
                    Event testEvent = null;

                    for (Map.Entry<String, Event> tempEvent : DataCache.getInstance().getEvents().entrySet()) {
                        if (tempEvent.getValue().getPersonID().equals(currentSpouse.getPersonID())) {
                            testEvent = tempEvent.getValue();
                            break;
                        }
                    }

                    if (testEvent != null) {
                        List<Event> spouseEvents = DataCache.getInstance().getEventsOfPerson().get(currentSpouse.getPersonID());
                        int earliestEvent = spouseEvents.get(0).getYear();
                        Event spouseEarliestEvent = spouseEvents.get(0);

                        for (int i = 0; i < spouseEvents.size(); i++) {
                            if (earliestEvent > spouseEvents.get(i).getYear()) {
                                earliestEvent = spouseEvents.get(i).getYear();
                                spouseEarliestEvent = spouseEvents.get(i);
                            }
                        }

                        LatLng spouseLatLng = new LatLng(spouseEarliestEvent.getLatitude(), spouseEarliestEvent.getLongitude());
                        LatLng personLatLng = new LatLng(markerPressedEvent.getLatitude(), markerPressedEvent.getLongitude());

                        currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).add(spouseLatLng, personLatLng).color(Color.RED)));
                    }

                    List<Event> currentPersonEvents = DataCache.getInstance().getEventsOfPerson()
                            .get(currentPerson.getPersonID());
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

                    currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false)
                            .addAll(eventLocations).color(Color.BLUE)));

                    addFamilyTreeLines(googleMap, markerPressedEvent.getEventID(),
                            20, currentPolyLines);

                    detailedView.setText(eventActivityInfo);
                }

                if (DataCache.getInstance().currentClickedEvent != null && eventActivityID == null) {
                    if (DataCache.getInstance().getEventsBySettings().contains(DataCache.getInstance().currentClickedEvent)) {
                        LatLng clickedLatLng = new LatLng(DataCache.getInstance().currentClickedEvent.getLatitude(),
                                DataCache.getInstance().currentClickedEvent.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clickedLatLng, 2));

                        for (Polyline currentLine : currentPolyLines) {
                            currentLine.remove();
                        }
                        currentPolyLines.clear();

                        Person savedStatePerson = DataCache.getInstance().getPersonMap().get(
                                DataCache.getInstance().currentClickedEvent.getPersonID());

                        currentPersonID = savedStatePerson.getPersonID();
                        currentGender = savedStatePerson.getGender();

                        if (currentGender.equals("m")) {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                        } else {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                        }

                        Event clickedMarkerEvent = DataCache.getInstance().currentClickedEvent;
                        //DataCache.getInstance().currentClickedEvent = clickedMarkerEvent;

                        if (savedStatePerson.getSpouseID() != null && DataCache.getSettings().spouseLines) {
                            Person currentSpouse = DataCache.getInstance().getPersonMap().get(savedStatePerson.getSpouseID());
                            Event testEvent = null;

                            for (Map.Entry<String, Event> tempEvent : mapEvents.entrySet()) {
                                if (tempEvent.getValue().getPersonID().equals(currentSpouse.getPersonID())) {
                                    testEvent = tempEvent.getValue();
                                    break;
                                }
                            }

                            if (testEvent != null) {
                                List<Event> spouseEvents = DataCache.getInstance().getEventsOfPerson().get(currentSpouse.getPersonID());
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
                            List<Event> currentPersonEvents = DataCache.getInstance().getEventsOfPerson().get(savedStatePerson.getPersonID());
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
                            addFamilyTreeLines(googleMap, DataCache.getInstance().currentClickedEvent.getEventID(),
                                    20, currentPolyLines);
                        }

                        Person clickedPerson = DataCache.getInstance().getPersonMap()
                                .get(DataCache.getInstance().currentClickedEvent.getPersonID());

                        if (clickedPerson.getGender().equals("m")) {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                        } else {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                        }

                        detailedView.setText(DataCache.getInstance().currentClickedEventInfo);
                    }
                }
            }
        }
    };

    private Float getUniqueMarkerColor(String eventType) {
        Float uniqueColor = null;
        if (uniqueEventMarker.containsKey(eventType)) {
            return uniqueEventMarker.get(eventType);
        }

        for (Float currentColor : colors) {
            if (!usedMarkerColors.contains(currentColor)) {
                usedMarkerColors.add(currentColor);
                uniqueEventMarker.put(eventType, currentColor);
                uniqueColor = currentColor;
                break;
            }
        }
        return uniqueColor;
    }

    private void addFamilyTreeLines(GoogleMap googleMap, String currentEventID,
                                    int currentPolylineWidth, List<Polyline> currentPolyLines) {
        Event currentEvent = null;
        List<Event> filteredEvents = DataCache.getInstance().getEventsBySettings();

        for (Event event : filteredEvents) {
            if (event.getEventID().equals(currentEventID)) {
                currentEvent = event;
                break;
            }
        }

        if (currentEvent != null) {
            Person currentPerson = DataCache.getInstance().getPersonMap().get(currentEvent.getPersonID());

            Event earliestMotherEvent = null;
            Event earliestFatherEvent = null;

            if (currentPerson.getMotherID() != null && currentPerson.getFatherID() != null) {
                earliestMotherEvent = DataCache.getInstance().getEarliestEventOfPerson(currentPerson.getMotherID());
                earliestFatherEvent = DataCache.getInstance().getEarliestEventOfPerson(currentPerson.getFatherID());

                if (filteredEvents.contains(earliestMotherEvent)) {
                    List<LatLng> currentMotherSideLocations = new ArrayList<>();
                    currentMotherSideLocations.add(new LatLng(currentEvent.getLatitude(),
                            currentEvent.getLongitude()));

                    currentMotherSideLocations.add(new LatLng(earliestMotherEvent.getLatitude(),
                            earliestMotherEvent.getLongitude()));

                    currentPolyLines.add(googleMap.addPolyline(new
                            PolylineOptions().clickable(false).addAll(currentMotherSideLocations)
                            .color(Color.CYAN).width(currentPolylineWidth)));
                }

                if (filteredEvents.contains(earliestFatherEvent)) {
                    List<LatLng> currentFatherSideLocations = new ArrayList<>();
                    currentFatherSideLocations.add(new LatLng(currentEvent.getLatitude(),
                            currentEvent.getLongitude()));

                    currentFatherSideLocations.add(new LatLng(earliestFatherEvent.getLatitude(),
                            earliestFatherEvent.getLongitude()));
                    currentPolyLines.add(googleMap.addPolyline(new
                            PolylineOptions().clickable(false).addAll(currentFatherSideLocations)
                            .color(Color.CYAN).width(currentPolylineWidth)));
                }
            }

            if (currentPerson.getMotherID() != null) {
                if (filteredEvents.contains(earliestMotherEvent)) {
                    addFamilyTreeLines(googleMap, earliestMotherEvent.getEventID(),
                            currentPolylineWidth - 3, currentPolyLines);
                }
            }

            if (currentPerson.getFatherID() != null) {
                if (filteredEvents.contains(earliestFatherEvent)) {
                    addFamilyTreeLines(googleMap, earliestFatherEvent.getEventID(),
                            currentPolylineWidth - 3, currentPolyLines);
                }
            }
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
            eventActivityID = getArguments().getString("eventID");
            eventActivityInfo = getArguments().getString("eventInfo");
        }

        detailedView.setOnClickListener(detailedView -> {
            if (currentPersonID != null) {
                Intent intent = new Intent(detailedView.getContext(), PersonActivity.class);
                intent.putExtra("personID", currentPersonID);
                startActivity(intent);
            }
        });
        return view;
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