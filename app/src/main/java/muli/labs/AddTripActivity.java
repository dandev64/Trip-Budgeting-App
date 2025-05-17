package muli.labs;

import android.content.SharedPreferences;
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
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class AddTripActivity extends AppCompatActivity {

    private EditText tripNameInput;
    private Spinner friendGroupSpinner;
    private Button saveButton;
    private Realm realm;
    private List<String> friendGroupNames;
    private List<String> friendGroupIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        tripNameInput = findViewById(R.id.tripNameInput);
        friendGroupSpinner = findViewById(R.id.friendGroupSpinner);
        saveButton = findViewById(R.id.saveButton);

        realm = Realm.getDefaultInstance();

        friendGroupNames = new ArrayList<>();
        friendGroupIds = new ArrayList<>();

        loadFriendGroups();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrip();
            }
        });
    }

    private void loadFriendGroups() {
        SharedPreferences credentials = getSharedPreferences("credentials", MODE_PRIVATE);
        String userId = credentials.getString("uuid", null);
        RealmResults<FriendGroup> friendGroups = realm.where(FriendGroup.class).equalTo("userId",userId).findAll();
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
        SharedPreferences credentials = getSharedPreferences("credentials", MODE_PRIVATE);
        String userId = credentials.getString("uuid", null);
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
            Trip trip = realm.createObject(Trip.class, UUID.randomUUID().toString());
            trip.setName(tripName);
            trip.setUserId(userId);
            trip.setFriendGroupId(friendGroupId);
        }, () -> {
            Toast.makeText(this, "Trip saved", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            Toast.makeText(this, "Error saving trip", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
