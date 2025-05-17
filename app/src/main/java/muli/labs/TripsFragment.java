package muli.labs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class TripsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private Realm realm;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        Button addTripButton = view.findViewById(R.id.addButton);

        realm = Realm.getDefaultInstance();

        // Retrieve the userId from SharedPreferences
        SharedPreferences credentials = requireActivity().getSharedPreferences("credentials", Context.MODE_PRIVATE);
        userId = credentials.getString("uuid", null);

        addTripButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddTripActivity.class);
            startActivity(intent);
        });

        // Filter trips by userId
        RealmResults<Trip> trips = realm.where(Trip.class)
                .equalTo("userId", userId)
                .findAll();
        adapter = new TripAdapter(this, trips, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void confirmAndDeleteTrip(String tripId) {
        new androidx.appcompat.app.AlertDialog.Builder(this.getContext())
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete this trip?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    realm.executeTransactionAsync(realm -> {
                        Trip trip = realm.where(Trip.class).equalTo("id", tripId).findFirst();
                        if (trip != null) {
                            trip.deleteFromRealm();
                        }
                    }, () -> {
                        Toast.makeText(getContext(), "Trip deleted", Toast.LENGTH_SHORT).show();
                    }, error -> {
                        Toast.makeText(getContext(), "Error deleting trip", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null) {
            realm.close();
        }
    }
}
