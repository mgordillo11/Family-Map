package com.example.family_map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.Authtoken;
import Models.Event;
import Models.Person;

public class DataCache {
    //APPROACH #2 - Singleton Pattern
    private static DataCache instance = new DataCache();
    private static Settings settingsInstance = new Settings();

    public static DataCache getInstance() {
        return instance;
    }
    public static Settings getSettingsInstance() { return settingsInstance; }

    private DataCache() {
        familyPeople = new HashMap<>();
        familyEvents = new HashMap<>();
        personEvents = new HashMap<>();
        userFamily = new HashMap<>();
        paternalAncestors = new HashSet<>();
        maternalAncestors = new HashSet<>();
    }

    private Map<String, Person> familyPeople; //Get's a person based on their personID
    private Map<String, Event> familyEvents; //Get an event based on their event ID

    private Map<String, List<Event>> personEvents; //Get a list of events chronologically based on a person's ID
    private Map<String, List<Person>> userFamily; //Get a list of family based on a person's ID

    private Set<String> paternalAncestors; //Get ancestors on dad's side
    private Set<String> maternalAncestors; //Get ancestor's on mom's side

    private Authtoken currentAuthtoken;
    private Person currentPerson;

    public boolean userLoggedIn = false;

    public Authtoken getCurrentAuthtoken() {
        return currentAuthtoken;
    }

    public void setCurrentAuthtoken(Authtoken currentAuthtoken) {
        this.currentAuthtoken = currentAuthtoken;
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

    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }

    public Map<String, Person> getFamilyPeople() {
        return familyPeople;
    }

    public void setFamilyPeople(Map<String, Person> familyPeople) {
        this.familyPeople = familyPeople;
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

    public static class Settings {
        public boolean lifeStoryLines = true;
        public boolean familyTreeLines = true;
        public boolean spouseLines = true;
        public boolean fatherSide = true;
        public boolean motherSide = true;
        public boolean maleEvents = true;
        public boolean femaleEvents = true;
    }
}
