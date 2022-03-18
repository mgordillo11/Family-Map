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
    //APPROACH #2 - Singleton Pattern
    private static DataCache instance = new DataCache();
    private static Settings settings = new Settings();

    public static DataCache getInstance() {
        return instance;
    }

    public static Settings getSettings() {
        return settings;
    }

    private DataCache() {
        familyPeople = new HashMap<>();
        familyEvents = new HashMap<>();
        personEvents = new HashMap<>();
        userFamily = new HashMap<>();
        paternalAncestors = new ArrayList<>();
        maternalAncestors = new ArrayList<>();
        maleEvents = new ArrayList<>();
        femaleEvents = new ArrayList<>();
    }

    private Map<String, Person> familyPeople; //Get's a person based on their personID
    private Map<String, Event> familyEvents; //Get an event based on their event ID

    private Map<String, List<Event>> personEvents; //Get a list of events chronologically based on a person's ID
    private Map<String, List<Person>> userFamily; //Get a list of family based on a person's ID

    private List<Person> paternalAncestors; //Get ancestors on dad's side
    private List<Person> maternalAncestors; //Get ancestor's on mom's side

    private List<Event> maleEvents;
    private List<Event> femaleEvents;

    private Authtoken currentAuthtoken;
    private Person currentPerson;

    public boolean userLoggedIn = false;

    public List<Event> settingsUpdate() {
        if (settings.femaleEvents && settings.maleEvents && settings.motherSide && settings.fatherSide) {
            List<Event> updatedEvents = new ArrayList<>();
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

        if (settings.femaleEvents && settings.fatherSide) {
            return getEventOfParentSideByGender("f", "Dad");
        }

        if (settings.maleEvents && settings.fatherSide) {
            return getEventOfParentSideByGender("m", "Dad");
        }

        if (settings.maleEvents && settings.motherSide) {
            return getEventOfParentSideByGender("m", "Mom");
        }

        if (settings.femaleEvents && settings.motherSide) {
            return getEventOfParentSideByGender("f", "Mom");
        }

        /*
        if (settings.femaleEvents && settings.motherSide && settings.maleEvents) {
            ArrayList<Event> tempArrayList = new ArrayList<>();
            tempArrayList.addAll(getEventOfParentSideByGender("f", "Mom"));
            tempArrayList.addAll(getEventOfParentSideByGender("m", "Mom"));
            return tempArrayList;
        }

        if (settings.femaleEvents && settings.fatherSide && settings.maleEvents) {
            ArrayList<Event> tempArrayList = new ArrayList<>();
            tempArrayList.addAll(getEventOfParentSideByGender("f", "Dad"));
            tempArrayList.addAll(getEventOfParentSideByGender("m", "Dad"));
            return tempArrayList;
        }*/

        if (!settings.maleEvents && !settings.femaleEvents) {
            return null;
        }
        if (!settings.motherSide && !settings.fatherSide) {
            return null;
        }

        return null;

    }

    public List<Event> getEventOfParentSideByGender(String gender, String parentSide) {
        List<Event> genderArray = new ArrayList<>();

        genderArray.addAll(getInstance().getParentEventSide(gender, parentSide));

        return genderArray;
    }

    public List<Event> getParentEventSide(String gender, String parentSide) {
        List<Event> genderArray = new ArrayList<>();

        if (parentSide.equals("Dad")) {
            for (Person currentPerson : getInstance().getPaternalAncestors()) {
                if (currentPerson.getGender().equals(gender)) {
                    List<Event> currentPersonEvents = getInstance().personEvents.get(currentPerson.getPersonID());
                    genderArray.addAll(currentPersonEvents);
                }
            }
        } else {
            for (Person currentPerson : getInstance().getMaternalAncestors()) {
                if (currentPerson.getGender().equals(gender)) {
                    List<Event> currentPersonEvents = getInstance().personEvents.get(currentPerson.getPersonID());
                    genderArray.addAll(currentPersonEvents);
                }
            }
        }

        return genderArray;
    }

    public Event getEarliestEvent(String personID) {
        List<Event> currentEarliestEvents = getInstance().getPersonEvents().get(personID);

        int earliestYear = currentEarliestEvents.get(0).getYear();
        Event earliestEvent = currentEarliestEvents.get(0);

        for(int i = 0; i < currentEarliestEvents.size(); i++) {
            if(earliestYear > currentEarliestEvents.get(i).getYear()) {
                earliestYear = currentEarliestEvents.get(i).getYear();
                earliestEvent = currentEarliestEvents.get(i);
            }
        }

        return earliestEvent;
    }

    public static class Settings {
        public boolean lifeStoryLines = true;
        public boolean familyTreeLines = true;
        public boolean spouseLines = true;
        public boolean fatherSide = true;
        public boolean motherSide = true;
        public boolean maleEvents = true;
        public boolean femaleEvents = true;
    }

    public Pair<List<Event>, List<Person>> searchFilter(String searchString) {
        List<Event> dataEvents = new ArrayList<>();
        List<Person> dataPeople = new ArrayList<>();

        for (Map.Entry<String, Event> currentEvent : DataCache.getInstance().getEvents().entrySet()) {
            dataEvents.add(currentEvent.getValue());
        }

        for (Map.Entry<String, Person> currentPerson : DataCache.getInstance().getFamilyPeople().entrySet()) {
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

                Person relatedPersonEvent = DataCache.getInstance().familyPeople.get(currentEvent.getPersonID());

                if (currentEvent.getEventType().toLowerCase().contains(searchString) ||
                        currentYear.contains(filteredString) || currentEvent.getCountry().toLowerCase().contains(filteredString) ||
                        currentEvent.getCity().toLowerCase().contains(filteredString) ||
                        currentEvent.getAssociatedUsername().toLowerCase().contains(filteredString) ||
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

        return new Pair<>(filteredEvents, filteredPeople);
    }

    public void getFamilySide(Person currentPerson, String familySide) {
        if (currentPerson.getMotherID() != null & currentPerson.getFatherID() != null) {
            getFamilySide(DataCache.getInstance().familyPeople.get(currentPerson.getMotherID()), familySide);
            getFamilySide(DataCache.getInstance().familyPeople.get(currentPerson.getFatherID()), familySide);
        }

        if (familySide.equals("Mom")) {
            getInstance().getMaternalAncestors().add(currentPerson);
        } else {
            getInstance().getPaternalAncestors().add(currentPerson);
        }
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

    public Map<String, Person> getFamilyPeople() {
        return familyPeople;
    }

    public Person getChildFromParent(String parentID) {
        for (Map.Entry<String, Person> entry : getFamilyPeople().entrySet()) {
            if (entry.getValue().getFatherID() != null && entry.getValue().getMotherID() != null) {
                if (entry.getValue().getFatherID().equals(parentID) || entry.getValue().getMotherID().equals(parentID)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public Map<String, List<Event>> getPersonEvents() {
        return personEvents;
    }

    public Map<String, List<Person>> getUserFamily() {
        return userFamily;
    }

    public Map<String, Event> getEvents() {
        return familyEvents;
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
