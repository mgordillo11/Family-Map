package com.example.family_map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;

import Models.Event;
import Models.Person;

public class SearchActivity extends AppCompatActivity {
    private static final int SEARCH_EVENT_ITEM_VIEW_TYPE = 0;
    private static final int SEARCH_PERSON_ITEM_VIEW_TYPE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView searchRecycler = findViewById(R.id.RecyclerView);
        searchRecycler.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        SearchView searchView = findViewById(R.id.searchViewActivity);

        List<Event> filterEventResults = new ArrayList<>();
        List<Person> filterPeopleResults = new ArrayList<>();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                Pair<List<Event>, List<Person>> filterResults = DataCache.getInstance().searchFilter(queryString);
                filterEventResults.addAll(filterResults.first);
                filterPeopleResults.addAll(filterResults.second);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryString) {
                Pair<List<Event>, List<Person>> filterResults = DataCache.getInstance().searchFilter(queryString);
                filterEventResults.addAll(filterResults.first);
                filterPeopleResults.addAll(filterResults.second);
                return false;
            }
        });
    }

    private class searchAdapter extends RecyclerView.Adapter<searchViewHolder> {
        private final List<Event> searchedEvents;
        private final List<Person> searchedPeople;

        searchAdapter(List<Event> events, List<Person> searchedPeople) {
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

            if(viewType == SEARCH_EVENT_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.family_members, parent, false);
            }
            return new searchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull searchViewHolder holder, int position) {
            if(position < searchedEvents.size()) {
                //holder.bind(searchedEvents.get(position));
            } else {
                //holder.bind(searchedPeople.get(position - searchedEvents.size()));
            }
        }

        @Override
        public int getItemCount() {
            return searchedEvents.size() + searchedPeople.size();
        }
    }

    private class searchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final int viewType;

        searchViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {

        }
    }
}