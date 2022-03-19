package com.example.family_map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import Models.Event;
import Models.Person;

public class SearchActivity extends AppCompatActivity {
    private static final int SEARCH_EVENT_ITEM_VIEW_TYPE = 0;
    private static final int SEARCH_PERSON_ITEM_VIEW_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(DataCache.getSettings().maleEvents || DataCache.getSettings().femaleEvents) {
            RecyclerView searchRecycler = findViewById(R.id.RecyclerView);
            searchRecycler.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

            SearchView searchView = findViewById(R.id.searchViewActivity);

            List<Event> filterEventResults = new ArrayList<>();
            List<Person> filterPeopleResults = new ArrayList<>();

            if (searchView.getQuery().toString().equals("")) {
                Pair<List<Event>, List<Person>> filterResults = DataCache.getInstance().searchFilter("");
                filterEventResults.addAll(filterResults.first);
                filterPeopleResults.addAll(filterResults.second);

                SearchAdapter searchAdapter = new SearchAdapter(filterEventResults, filterPeopleResults);
                searchRecycler.setAdapter(searchAdapter);
            }

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String queryString) {
                    Pair<List<Event>, List<Person>> filterResults = DataCache.getInstance().searchFilter(queryString);
                    filterEventResults.addAll(filterResults.first);
                    filterPeopleResults.addAll(filterResults.second);

                    SearchAdapter searchAdapter = new SearchAdapter(filterEventResults, filterPeopleResults);
                    searchRecycler.setAdapter(searchAdapter);

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String queryString) {
                    Pair<List<Event>, List<Person>> filterResults = DataCache.getInstance().searchFilter(queryString);
                    filterEventResults.clear();
                    filterPeopleResults.clear();

                    filterEventResults.addAll(filterResults.first);
                    filterPeopleResults.addAll(filterResults.second);

                    SearchAdapter searchAdapter = new SearchAdapter(filterEventResults, filterPeopleResults);
                    searchRecycler.setAdapter(searchAdapter);

                    return false;
                }
            });
        }
    }

    private class SearchAdapter extends RecyclerView.Adapter<searchViewHolder> {
        private final List<Event> searchedEvents;
        private final List<Person> searchedPeople;

        SearchAdapter(List<Event> events, List<Person> searchedPeople) {
            this.searchedEvents = events;
            this.searchedPeople = searchedPeople;
        }

        @Override
        public int getItemViewType(int position) {
            return position < searchedEvents.size() ? SEARCH_EVENT_ITEM_VIEW_TYPE : SEARCH_PERSON_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public searchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if (viewType == SEARCH_EVENT_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.family_members, parent, false);
            }
            return new searchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull searchViewHolder holder, int position) {
            if (position < searchedEvents.size()) {
                holder.bind(searchedEvents.get(position));
            } else {
                holder.bind(searchedPeople.get(position - searchedEvents.size()));
            }
        }

        @Override
        public int getItemCount() {
            return searchedEvents.size() + searchedPeople.size();
        }
    }

    private class searchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView name;
        private final int viewType;
        private Event currentEvent;
        private Person currentPerson;
        private String newEventActivityInfo;
        private String currentPersonID;

        searchViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if (viewType == SEARCH_EVENT_ITEM_VIEW_TYPE) {
                name = itemView.findViewById(R.id.eventItem);
            } else {
                name = itemView.findViewById(R.id.familyMember);
            }

        }

        private void bind(Event event) {
            this.currentEvent = event;
            Person currentPerson = DataCache.getInstance().getFamilyPeople().get(event.getPersonID());

            String eventItemText = currentEvent.getEventType().toUpperCase() + ": " + currentEvent.getCity() + "," + currentEvent.getCountry()
                    + "(" + currentEvent.getYear() + ")\n" + currentPerson.getFirstName() + " " + currentPerson.getLastName();

            newEventActivityInfo = currentPerson.getFirstName() + " " + currentPerson.getLastName() +
                    "\n" + event.getEventType().toUpperCase() + ": " + event.getCity() + "," + event.getCountry()
                    + "(" + event.getYear() + ")";

            name.setText(eventItemText);
        }

        private void bind(Person person) {
            this.currentPerson = person;
            currentPersonID = person.getPersonID();

            String familyMemberInfo = person.getFirstName() + " " + person.getLastName() + "\n";

            name.setText(familyMemberInfo);
            if (person.getGender().equals("m")) {
                name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_man_24, 0, 0, 0);
            } else {
                name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_woman_24, 0, 0, 0);
            }
        }

        @Override
        public void onClick(View view) {
            if (viewType == SEARCH_EVENT_ITEM_VIEW_TYPE) {
                Intent eventIntent = new Intent(SearchActivity.this, EventActivity.class);
                eventIntent.putExtra("eventID", currentEvent.getEventID());
                eventIntent.putExtra("eventInfo", newEventActivityInfo);
                startActivity(eventIntent);
            } else {
                Intent personIntent = new Intent(SearchActivity.this, PersonActivity.class);
                personIntent.putExtra("personID", currentPersonID);
                startActivity(personIntent);
            }
        }
    }
}