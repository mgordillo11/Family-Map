package com.example.family_map;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Authtoken;
import Models.Event;
import Models.Person;

public class DataCache {
    private static final DataCache instance = new DataCache();
    private static final Settings settings = new Settings();

    public static DataCache getInstance() {
        return instance;
    }
    public static Settings getSettings() {
        return settings;
    }

    private DataCache() {
        familyPersonTree = new HashMap<>();
        eventsByID = new HashMap<>();
        eventsOfPerson = new HashMap<>();
        paternalAncestors = new ArrayList<>();
        maternalAncestors = new ArrayList<>();
        maleEvents = new ArrayList<>();
        femaleEvents = new ArrayList<>();
        testingEvents = new ArrayList<>();
        testingPeople = new ArrayList<>();
    }

    public Event currentClickedEvent = null;
    public String currentClickedEventInfo = null;

    private Map<String, Person> familyPersonTree; //Get's a person based on their personID
    private Map<String, Event> eventsByID; //Get an event based on their event ID
    private Map<String, List<Event>> eventsOfPerson; //Get a list of events chronologically based on a person's ID

    private List<Person> paternalAncestors; //Get ancestors on dad's side
    private List<Person> maternalAncestors; //Get ancestor's on mom's side

    private List<Event> maleEvents; //All Male Events
    private List<Event> femaleEvents; //All Female Events

    //These two Lists are used for JUNIT testing only
    // because Android Studio doesn't recognize what a pair is
    public List<Event> testingEvents;
    public List<Person> testingPeople;

    private Authtoken currentAuthtoken;
    private Person currentPerson;

    public boolean userLoggedIn = false;

    public static class Settings {
        public boolean lifeStoryLines = true;
        public boolean familyTreeLines = true;
        public boolean spouseLines = true;
        public boolean fatherSide = true;
        public boolean motherSide = true;
        public boolean maleEvents = true;
        public boolean femaleEvents = true;
    }

    public void resetCacheData() {
        settings.familyTreeLines = true;
        settings.lifeStoryLines = true;
        settings.spouseLines = true;
        settings.fatherSide = true;
        settings.motherSide = true;
        settings.maleEvents = true;
        settings.femaleEvents = true;
        familyPersonTree = new HashMap<>();
        eventsByID = new HashMap<>();
        eventsOfPerson = new HashMap<>();
        paternalAncestors = new ArrayList<>();
        maternalAncestors = new ArrayList<>();
        maleEvents = new ArrayList<>();
        femaleEvents = new ArrayList<>();
    }

    public List<Event> getEventsBySettings() {
        List<Event> updatedEvents = new ArrayList<>();
        if (settings.femaleEvents && settings.maleEvents && settings.motherSide && settings.fatherSide) {
            updatedEvents.addAll(getInstance().femaleEvents);
            updatedEvents.addAll(getInstance().maleEvents);
            return updatedEvents;
        }

        if (settings.maleEvents && settings.motherSide && settings.fatherSide) {
            return getInstance().getMaleEvents();
        }

        if (settings.femaleEvents && settings.motherSide && settings.fatherSide) {
            return getInstance().getFemaleEvents();
        }

        if (settings.femaleEvents && settings.maleEvents && settings.fatherSide) {
            updatedEvents.addAll(getEventsByParentSide("f", "Dad"));
            updatedEvents.addAll(getEventsByParentSide("m", "Dad"));

            //Gets all events for spouse if the current user has one
            if (currentPerson.getSpouseID() != null) {
                updatedEvents.addAll(getInstance().getEventsOfPerson().get(currentPerson.getSpouseID()));
            }

            return updatedEvents;
        }

        if (settings.femaleEvents && settings.maleEvents && settings.motherSide) {
            updatedEvents.addAll(getEventsByParentSide("f", "Mom"));
            updatedEvents.addAll(getEventsByParentSide("m", "Mom"));

            if (currentPerson.getSpouseID() != null) {
                updatedEvents.addAll(getInstance().getEventsOfPerson().get(currentPerson.getSpouseID()));
            }

            return updatedEvents;
        }

        if (settings.femaleEvents && settings.fatherSide) {
            updatedEvents.addAll(getEventsByParentSide("f", "Dad"));

            return updatedEvents;
        }

        if (settings.femaleEvents && settings.motherSide) {
            updatedEvents.addAll(getEventsByParentSide("f", "Mom"));

            return updatedEvents;
        }

        if (settings.maleEvents && settings.fatherSide) {
            updatedEvents.addAll(getEventsByParentSide("m", "Dad"));

            if (currentPerson.getSpouseID() != null) {
                updatedEvents.addAll(getInstance().getEventsOfPerson().get(currentPerson.getSpouseID()));
            }

            return updatedEvents;
        }

        if (settings.maleEvents && settings.motherSide) {
            updatedEvents.addAll(getEventsByParentSide("m", "Mom"));

            if (currentPerson.getSpouseID() != null) {
                updatedEvents.addAll(getInstance().getEventsOfPerson().get(currentPerson.getSpouseID()));
            }

            return updatedEvents;
        }

        if (!settings.maleEvents && !settings.femaleEvents) {
            return null;
        }

        //This part return only current user's and spouse (if they have one) events when all the possible
        //combinations don't match the current settings
        List<Event> currentUserEvents = new ArrayList<>();
        currentUserEvents.addAll(getInstance().getEventsOfPerson().get(currentPerson.getPersonID()));

        if (currentPerson.getSpouseID() != null) {
            currentUserEvents.addAll(getInstance().getEventsOfPerson().get(currentPerson.getSpouseID()));
        }
        return currentUserEvents;
    }

    public List<Event> getEventsByParentSide(String gender, String parentSide) {
        List<Event> eventsByParentAndGender = new ArrayList<>();

        if (parentSide.equals("Dad")) {
            for (Person currentPerson : getInstance().paternalAncestors) {
                if (currentPerson.getGender().equals(gender)) {
                    List<Event> currentPersonEvents = getInstance().eventsOfPerson.get(currentPerson.getPersonID());
                    eventsByParentAndGender.addAll(currentPersonEvents);
                }
            }
        } else {
            for (Person currentPerson : getInstance().maternalAncestors) {
                if (currentPerson.getGender().equals(gender)) {
                    List<Event> currentPersonEvents = getInstance().eventsOfPerson.get(currentPerson.getPersonID());
                    eventsByParentAndGender.addAll(currentPersonEvents);
                }
            }
        }

        eventsByParentAndGender.addAll(getEventsOfPerson().get(currentPerson.getPersonID()));
        return eventsByParentAndGender;
    }

    public Event getEarliestEventOfPerson(String personID) {
        List<Event> currentEarliestEvents = getInstance().getEventsOfPerson().get(personID);

        int earliestYear = currentEarliestEvents.get(0).getYear();
        Event earliestEvent = currentEarliestEvents.get(0);

        for (int i = 0; i < currentEarliestEvents.size(); i++) {
            if (earliestYear > currentEarliestEvents.get(i).getYear()) {
                earliestYear = currentEarliestEvents.get(i).getYear();
                earliestEvent = currentEarliestEvents.get(i);
            }
        }

        return earliestEvent;
    }

    public Pair<List<Event>, List<Person>> searchFilter(String searchString) {
        //Generates the events allows by what the user has chosen to pop up
        List<Event> dataEvents = new ArrayList<>(DataCache.getInstance().getEventsBySettings());
        List<Person> dataPeople = new ArrayList<>();

        for (Map.Entry<String, Person> currentPerson : DataCache.getInstance().getFamilyPersonTree().entrySet()) {
            dataPeople.add(currentPerson.getValue());
        }

        List<Event> filteredEvents = new ArrayList<>();
        List<Person> filteredPeople = new ArrayList<>();

        if (searchString.isEmpty()) {
            filteredEvents.addAll(dataEvents);
            filteredPeople.addAll(dataPeople);
        } else {
            String filteredString = searchString.toLowerCase().trim();

            for (Event currentEvent : dataEvents) {
                String currentYear = Integer.toString(currentEvent.getYear()).toLowerCase();
                String currentLongitude = Double.toString(currentEvent.getLongitude()).toLowerCase();
                String currentLatitude = Double.toString(currentEvent.getLatitude()).toLowerCase();

                Person relatedPersonEvent = DataCache.getInstance().familyPersonTree.get(currentEvent.getPersonID());

                if (currentEvent.getEventType().toLowerCase().contains(searchString) ||
                        currentYear.contains(filteredString) || currentEvent.getCountry().toLowerCase().contains(filteredString) ||
                        currentEvent.getCity().toLowerCase().contains(filteredString) ||
                        currentEvent.getEventID().toLowerCase().contains(filteredString)
                        || currentEvent.getPersonID().toLowerCase().contains(filteredString) ||
                        currentLongitude.contains(filteredString) || currentLatitude.contains(filteredString) ||
                        relatedPersonEvent.getFirstName().toLowerCase().contains(filteredString) ||
                        relatedPersonEvent.getLastName().toLowerCase().contains(filteredString)) {

                    filteredEvents.add(currentEvent);

                }
            }

            for (Person currentPerson : dataPeople) {
                if (currentPerson.getFirstName().toLowerCase().contains(filteredString) ||
                        currentPerson.getLastName().toLowerCase().contains(filteredString)) {
                    filteredPeople.add(currentPerson);
                }
            }
        }

        testingEvents = filteredEvents;
        testingPeople = filteredPeople;

        return new Pair<>(filteredEvents, filteredPeople);
    }

    public void fillMaternalAncestors(Person currentMotherSide) {
        if (currentMotherSide.getMotherID() != null & currentMotherSide.getFatherID() != null) {
            fillMaternalAncestors(getInstance().getFamilyPersonTree().get(currentMotherSide.getMotherID()));
            fillMaternalAncestors(getInstance().getFamilyPersonTree().get(currentMotherSide.getFatherID()));
        }
        getInstance().getMaternalAncestors().add(currentMotherSide);
    }

    public void fillPaternalAncestors(Person currentFatherSide) {
        if (currentFatherSide.getMotherID() != null & currentFatherSide.getFatherID() != null) {
            fillPaternalAncestors(getInstance().getFamilyPersonTree().get(currentFatherSide.getMotherID()));
            fillPaternalAncestors(getInstance().getFamilyPersonTree().get(currentFatherSide.getFatherID()));
        }
        getInstance().getPaternalAncestors().add(currentFatherSide);
    }

    public List<Event> getMaleEvents() {
        return maleEvents;
    }

    public List<Event> getFemaleEvents() {
        return femaleEvents;
    }

    public List<Person> getPaternalAncestors() {
        return paternalAncestors;
    }

    public List<Person> getMaternalAncestors() {
        return maternalAncestors;
    }

    public Map<String, Person> getFamilyPersonTree() {
        return familyPersonTree;
    }

    public Person getChildFromParent(String parentID) {
        for (Map.Entry<String, Person> entry : getFamilyPersonTree().entrySet()) {
            if (entry.getValue().getFatherID() != null && entry.getValue().getMotherID() != null) {
                if (entry.getValue().getFatherID().equals(parentID) || entry.getValue().getMotherID().equals(parentID)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public Map<String, List<Event>> getEventsOfPerson() {
        return eventsOfPerson;
    }

    public Map<String, Event> getEvents() {
        return eventsByID;
    }

    public Person getCurrentPerson() {
        return currentPerson;
    }

    public Authtoken getCurrentAuthtoken() {
        return currentAuthtoken;
    }

    public void setCurrentAuthtoken(Authtoken currentAuthtoken) {
        this.currentAuthtoken = currentAuthtoken;
    }

    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }
}
