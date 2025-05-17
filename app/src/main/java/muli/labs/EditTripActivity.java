package muli.labs;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class EditTripActivity extends AppCompatActivity {

    private EditText tripNameInput;
    private Spinner friendGroupSpinner;
    private Button saveButton;
    private Realm realm;
    private String tripId;
    private List<String> friendGroupNames;
    private List<String> friendGroupIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip);

        tripNameInput = findViewById(R.id.tripNameInput);
        friendGroupSpinner = findViewById(R.id.friendGroupSpinner);
        saveButton = findViewById(R.id.saveButton);

        realm = Realm.getDefaultInstance();
        tripId = getIntent().getStringExtra("tripId");

        friendGroupNames = new ArrayList<>();
        friendGroupIds = new ArrayList<>();

        loadFriendGroups();

        Trip trip = realm.where(Trip.class).equalTo("id", tripId).findFirst();
        if (trip != null) {
            tripNameInput.setText(trip.getName());
            int selectedPosition = friendGroupIds.indexOf(trip.getFriendGroupId());
            friendGroupSpinner.setSelection(selectedPosition);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrip();
            }
        });
    }

    private void loadFriendGroups() {
        RealmResults<FriendGroup> friendGroups = realm.where(FriendGroup.class).findAll();
        for (FriendGroup group : friendGroups) {
            friendGroupNames.add(group.getName());
            friendGroupIds.add(group.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, friendGroupNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendGroupSpinner.setAdapter(adapter);
    }

    private void saveTrip() {
        String tripName = tripNameInput.getText().toString();
        int selectedPosition = friendGroupSpinner.getSelectedItemPosition();
        if (tripName.isEmpty()) {
            Toast.makeText(this, "Enter trip name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPosition == -1) {
            Toast.makeText(this, "Select a friend group", Toast.LENGTH_SHORT).show();
            return;
        }

        String friendGroupId = friendGroupIds.get(selectedPosition);

        realm.executeTransactionAsync(realm -> {
            Trip trip = realm.where(Trip.class).equalTo("id", tripId).findFirst();
            if (trip != null) {
                trip.setName(tripName);
                trip.setFriendGroupId(friendGroupId);
            }
        }, () -> {
            Toast.makeText(this, "Trip updated", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            Toast.makeText(this, "Error updating trip", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
