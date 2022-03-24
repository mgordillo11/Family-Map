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
    private final List<Polyline> currentPolyLines = new ArrayList<>();
    private final Map<String, Event> mapEvents;
    private final List<Float> colors = new ArrayList<>();
    private Map<String, Float> uniqueEventMarker = new HashMap<>();
    private final List<Float> usedMarkerColors = new ArrayList<>();

    public MapsFragment() {
        this.mapEvents = new HashMap<>();


        /*
        uniqueEventMarker.put("birth", BitmapDescriptorFactory.HUE_MAGENTA);
        uniqueEventMarker.put("marriage", BitmapDescriptorFactory.HUE_MAGENTA);
        uniqueEventMarker.put("event", BitmapDescriptorFactory.HUE_MAGENTA);*/

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
                } else {
                    for (Event settingEvents : DataCache.getInstance().settingsUpdate()) {
                        mapEvents.put(settingEvents.getEventID(), settingEvents);
                    }
                }

                for (Map.Entry<String, Event> event : mapEvents.entrySet()) {
                    float color;

                    Event eventEntry = event.getValue();
                    color = getUniqueMarkerColor(eventEntry.getEventType().toLowerCase());
                    /*switch (eventEntry.getEventType().toLowerCase()) {
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
                            color = getUniqueMarkerColor(eventEntry.getEventType().toLowerCase());
                            //color = BitmapDescriptorFactory.HUE_YELLOW;
                            break;
                    }*/

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

                            Event clickedMarkerEvent = mapEvents.get(markerData.get(2));
                            DataCache.getInstance().currentClickedEvent = clickedMarkerEvent;

                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                    clickedMarkerEvent.getLatitude(), clickedMarkerEvent.getLongitude()
                            ), 2));

                            Person currentPerson = DataCache.getInstance().getFamilyPeople().get(currentPersonID);

                            if (currentPerson.getSpouseID() != null && DataCache.getSettings().spouseLines) {
                                Person currentSpouse = DataCache.getInstance().getFamilyPeople().get(currentPerson.getSpouseID());
                                Event testEvent = null;

                                for (Map.Entry<String, Event> tempEvent : mapEvents.entrySet()) {
                                    if (tempEvent.getValue().getPersonID().equals(currentSpouse.getPersonID())) {
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
                                addFamilyTreeLines(googleMap, clickedMarkerEvent.getEventID(), 20, currentPolyLines);
                            }

                            DataCache.getInstance().currentClickedMarkerInfo = marker.getTitle();
                            detailedView.setText(marker.getTitle());
                            return true;
                        }
                    });
                }

                if (eventID != null) {
                    Event markerPressedEvent = DataCache.getInstance().getEvents().get(eventID);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                            markerPressedEvent.getLatitude(), markerPressedEvent.getLongitude()
                    ), 2));

//                    LatLng markerLocation = new LatLng(markerPressedEvent.getLatitude(), markerPressedEvent.getLongitude());
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 5));

                    for (Polyline currentLine : currentPolyLines) {
                        currentLine.remove();
                    }
                    currentPolyLines.clear();

                    Person personRelatedToEvent = DataCache.getInstance().getFamilyPeople().get(markerPressedEvent.getPersonID());

                    assert personRelatedToEvent != null;
                    currentPersonID = personRelatedToEvent.getPersonID();
                    currentGender = personRelatedToEvent.getGender();

                    if (currentGender.equals("m")) {
                        detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                    } else {
                        detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                    }

                    //Event clickedMarkerEvent = mapEvents.get(markerPressedEvent.getEventID());
                    Person currentPerson = DataCache.getInstance().getFamilyPeople().get(currentPersonID);

                    Person currentSpouse = DataCache.getInstance().getFamilyPeople().get(currentPerson.getSpouseID());
                    Event testEvent = null;

                    for (Map.Entry<String, Event> tempEvent : DataCache.getInstance().getEvents().entrySet()) {
                        if (tempEvent.getValue().getPersonID().equals(currentSpouse.getPersonID())) {
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
                        LatLng personLatLng = new LatLng(markerPressedEvent.getLatitude(), markerPressedEvent.getLongitude());

                        currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).add(spouseLatLng, personLatLng).color(Color.RED)));
                    }

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

                    //assert markerPressedEvent != null;
                    addFamilyTreeLines(googleMap, markerPressedEvent.getEventID(), 20, currentPolyLines);

                    detailedView.setText(zoomedEvent);
                }

                if (DataCache.getInstance().currentClickedEvent != null && eventID == null) {
                    if (DataCache.getInstance().settingsUpdate().contains(DataCache.getInstance().currentClickedEvent)) {
                        LatLng clickedLatLng = new LatLng(DataCache.getInstance().currentClickedEvent.getLatitude(),
                                DataCache.getInstance().currentClickedEvent.getLongitude());

                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clickedLatLng, 2));

                        Person clickedPerson = DataCache.getInstance().getFamilyPeople()
                                .get(DataCache.getInstance().currentClickedEvent.getPersonID());

                        if (clickedPerson.getGender().equals("m")) {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_man_24, 0, 0);
                        } else {
                            detailedView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_woman_24, 0, 0);
                        }

                        detailedView.setText(DataCache.getInstance().currentClickedMarkerInfo);
                    }
                }
            }
        }
    };

    public void addFamilyTreeLines(GoogleMap googleMap, String currentEventID, int currentPolylineWidth, List<Polyline> currentPolyLines) {
        //Event currentEvent = DataCache.getInstance().getEvents().get(currentEventID);

        Event currentEvent = null;
        List<Event> filteredEvents = DataCache.getInstance().settingsUpdate();

        for (Event event : filteredEvents) {
            if (event.getEventID().equals(currentEventID)) {
                currentEvent = event;
            }
        }

        if(currentEvent == null) {
            return;
        }

        Person currentPerson = DataCache.getInstance().getFamilyPeople().get(currentEvent.getPersonID());

        Event earliestMotherEvent = null;
        Event earliestFatherEvent = null;

        if (currentPerson.getMotherID() != null && currentPerson.getFatherID() != null) {
            earliestMotherEvent = DataCache.getInstance().getEarliestEvent(currentPerson.getMotherID());
            earliestFatherEvent = DataCache.getInstance().getEarliestEvent(currentPerson.getFatherID());

            if(DataCache.getSettings().femaleEvents) {
                List<LatLng> currentMotherSide = new ArrayList<>();
                currentMotherSide.add(new LatLng(currentEvent.getLatitude(), currentEvent.getLongitude()));
                currentMotherSide.add(new LatLng(earliestMotherEvent.getLatitude(), earliestMotherEvent.getLongitude()));
                currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).addAll(currentMotherSide).color(Color.CYAN).width(currentPolylineWidth)));
            }

            if(DataCache.getSettings().maleEvents) {
                List<LatLng> currentFatherSide = new ArrayList<>();
                currentFatherSide.add(new LatLng(currentEvent.getLatitude(), currentEvent.getLongitude()));
                currentFatherSide.add(new LatLng(earliestFatherEvent.getLatitude(), earliestFatherEvent.getLongitude()));
                currentPolyLines.add(googleMap.addPolyline(new PolylineOptions().clickable(false).addAll(currentFatherSide).color(Color.CYAN).width(currentPolylineWidth)));
            }
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