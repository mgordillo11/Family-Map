package com.example.family_map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import Models.Event;
import Models.Person;

public class PersonActivity extends AppCompatActivity {
    private TextView actualFirstName;
    private TextView actualLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        actualFirstName = findViewById(R.id.actualPersonFirstName);
        actualLastName = findViewById(R.id.actualPersonLastName);
        TextView actualGender = findViewById(R.id.actualPersonGender);

        ExpandableListView lifeEvents = findViewById(R.id.expandableLifeEvents);

        String personID = getIntent().getStringExtra("personID");
        Person personMarker = DataCache.getInstance().getPersonMap().get(personID);

        List<Event> eventsSettings = DataCache.getInstance().getEventsBySettings();
        List<Event> currentPersonEvents = DataCache.getInstance().getEventsOfPerson()
                .get(personMarker.getPersonID());

        List<Event> actualEvents = new ArrayList<>();

        for (Event currentEvent : eventsSettings) {
            if (currentPersonEvents.contains(currentEvent)) {
                actualEvents.add(currentEvent);
            }
        }

        //Sorts activities by the year they happened
        Collections.sort(actualEvents);
        List<Pair<String, Person>> familyTree = new LinkedList<>();

        if (personMarker.getFatherID() != null) {
            familyTree.add(new Pair<>("Father", DataCache.getInstance().getPersonMap()
                    .get(personMarker.getFatherID())));
        } else {
            familyTree.add(new Pair<>("Father", null));
        }

        if (personMarker.getMotherID() != null) {
            familyTree.add(new Pair<>("Mother", DataCache.getInstance().getPersonMap()
                    .get(personMarker.getMotherID())));
        } else {
            familyTree.add(new Pair<>("Mother", null));
        }

        if (personMarker.getSpouseID() != null) {
            familyTree.add(new Pair<>("Spouse", DataCache.getInstance().getPersonMap()
                    .get(personMarker.getSpouseID())));
        }

        Person markerPersonChild = DataCache.getInstance().getChildFromParent(personMarker.getPersonID());
        familyTree.add(new Pair<>("Child", markerPersonChild));

        actualFirstName.setText(personMarker.getFirstName());
        actualLastName.setText(personMarker.getLastName());

        if (personMarker.getGender().equals("m")) {
            actualGender.setText(R.string.male);
        } else {
            actualGender.setText(R.string.female);
        }

        lifeEvents.setAdapter(new ExpandableListAdapter(actualEvents, familyTree));
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private static final int ACTUAL_EVENTS_GROUP_POSITION = 0;
        private static final int FAMILY_TREE_GROUP_POSITION = 1;

        private final List<Pair<String, Person>> familyTree;
        private final List<Event> actualEvents;

        public ExpandableListAdapter(List<Event> actualEvents, List<Pair<String, Person>> familyTree) {
            this.actualEvents = actualEvents;
            this.familyTree = familyTree;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case ACTUAL_EVENTS_GROUP_POSITION:
                    return actualEvents.size();
                case FAMILY_TREE_GROUP_POSITION:
                    return familyTree.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case ACTUAL_EVENTS_GROUP_POSITION:
                    return getString(R.string.lifeEventsTitle);
                case FAMILY_TREE_GROUP_POSITION:
                    return getString(R.string.familyTitle);
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case ACTUAL_EVENTS_GROUP_POSITION:
                    return actualEvents.get(childPosition);
                case FAMILY_TREE_GROUP_POSITION:
                    return familyTree.get(childPosition).second;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case ACTUAL_EVENTS_GROUP_POSITION:
                    titleView.setText(R.string.lifeEventsTitle);
                    break;
                case FAMILY_TREE_GROUP_POSITION:
                    titleView.setText(R.string.familyTitle);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch (groupPosition) {
                case ACTUAL_EVENTS_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeLifeEvents(itemView, childPosition);
                    break;
                case FAMILY_TREE_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.family_members, parent, false);
                    initializeFamilyTree(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return itemView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private void initializeLifeEvents(View lifeEventItemView, final int childPosition) {
            TextView eventItemView = lifeEventItemView.findViewById(R.id.eventItem);

            Event itemEvent = actualEvents.get(childPosition);
            String eventItemText = itemEvent.getEventType().toUpperCase() + ": " + itemEvent.getCity() + "," + itemEvent.getCountry()
                    + "(" + itemEvent.getYear() + ")\n" + actualFirstName.getText().toString() + " " + actualLastName.getText().toString();

            String newEventActivityInfo = actualFirstName.getText().toString() + " " + actualLastName.getText().toString() +
                    "\n" + itemEvent.getEventType().toUpperCase() + ": " + itemEvent.getCity() + "," + itemEvent.getCountry()
                    + "(" + itemEvent.getYear() + ")";

            eventItemView.setText(eventItemText);

            lifeEventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent eventIntent = new Intent(PersonActivity.this, EventActivity.class);
                    eventIntent.putExtra("eventID", itemEvent.getEventID());
                    eventIntent.putExtra("eventInfo", newEventActivityInfo);
                    startActivity(eventIntent);
                }
            });
        }

        private void initializeFamilyTree(View familyItemView, final int childPosition) {
            TextView familyMemberItemView = familyItemView.findViewById(R.id.familyMember);
            Person currentFamilyMember = familyTree.get(childPosition).second;
            String familyMemberInfo;

            if (currentFamilyMember == null) {
                familyMemberInfo = "NO " + familyTree.get(childPosition).first.toUpperCase();
            } else {
                familyMemberInfo = currentFamilyMember.getFirstName() + " "
                        + currentFamilyMember.getLastName() + "\n" + familyTree.get(childPosition).first;

                if (currentFamilyMember.getGender().equals("m")) {
                    familyMemberItemView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_man_24, 0, 0, 0);
                } else {
                    familyMemberItemView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_woman_24, 0, 0, 0);
                }
            }

            familyMemberItemView.setText(familyMemberInfo);
            familyItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentFamilyMember != null) {
                        Intent personIntent = new Intent(PersonActivity.this, PersonActivity.class);
                        personIntent.putExtra("personID", familyTree.get(childPosition).second.getPersonID());
                        startActivity(personIntent);
                    }
                }
            });
        }
    }
}