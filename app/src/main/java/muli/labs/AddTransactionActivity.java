package muli.labs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText transactionNameInput;
    private EditText totalBillInput;
    private LinearLayout friendsListLayout;
    private Button saveButton;
    private Realm realm;
    private List<String> friendNames;
    private List<String> friendIds;
    private List<View> friendViews;
    private String tripId;
    private String friendGroupId; // Add this variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        transactionNameInput = findViewById(R.id.transactionNameInput);
        totalBillInput = findViewById(R.id.totalBillInput);
        friendsListLayout = findViewById(R.id.friendsListLayout);
        saveButton = findViewById(R.id.addTransactionButton);

        realm = Realm.getDefaultInstance();

        friendNames = new ArrayList<>();
        friendIds = new ArrayList<>();
        friendViews = new ArrayList<>();

        tripId = getIntent().getStringExtra("tripId");  // Retrieve tripId
        loadFriendGroupId(); // Load the friend group ID
        loadContributors();

        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void loadFriendGroupId() {
        Trip trip = realm.where(Trip.class).equalTo("id", tripId).findFirst();
        if (trip != null) {
            friendGroupId = trip.getFriendGroupId();
        } else {
            Toast.makeText(this, "Error loading trip details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadContributors() {
        RealmResults<Friend> friends = realm.where(Friend.class).equalTo("groupId", friendGroupId).findAll(); // Filter friends by groupId
        for (Friend friend : friends) {
            friendNames.add(friend.getName());
            friendIds.add(friend.getId());

            View friendView = getLayoutInflater().inflate(R.layout.item_friend_contribution, null);

            CheckBox friendCheckBox = friendView.findViewById(R.id.friendCheckBox);
            EditText contributionInput = friendView.findViewById(R.id.contributionInput);
            EditText owedInput = friendView.findViewById(R.id.owedInput);

            friendCheckBox.setText(friend.getName());
            friendViews.add(friendView);

            friendsListLayout.addView(friendView);

            friendCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                contributionInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                owedInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }
    }

    private void saveTransaction() {
        String transactionName = transactionNameInput.getText().toString();
        String totalBillString = totalBillInput.getText().toString();

        if (transactionName.isEmpty() || totalBillString.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalBill;
        try {
            totalBill = Double.parseDouble(totalBillString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid total bill amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalContributed = 0;
        double totalOwed = 0;

        for (int i = 0; i < friendViews.size(); i++) {
            View friendView = friendViews.get(i);
            CheckBox friendCheckBox = friendView.findViewById(R.id.friendCheckBox);
            EditText contributionInput = friendView.findViewById(R.id.contributionInput);
            EditText owedInput = friendView.findViewById(R.id.owedInput);

            if (friendCheckBox.isChecked()) {
                String contributionString = contributionInput.getText().toString();
                String owedString = owedInput.getText().toString();

                if (contributionString.isEmpty() || owedString.isEmpty()) {
                    Toast.makeText(this, "Contribution or owed amount is empty for friend: " + friendNames.get(i), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double contributionAmount = Double.parseDouble(contributionString);
                    double owedAmount = Double.parseDouble(owedString);
                    totalContributed += contributionAmount;
                    totalOwed += owedAmount;
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid contribution or owed amount for friend: " + friendNames.get(i), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        if (totalContributed != totalBill || totalOwed != totalBill) {
            Toast.makeText(this, "Total contributed and total owed must equal the total bill.", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.executeTransactionAsync(realm -> {
            Transaction transaction = realm.createObject(Transaction.class, UUID.randomUUID().toString());
            transaction.setName(transactionName);
            transaction.setTotalBill(totalBill);
            transaction.setTripId(tripId);  // Set tripId

            for (int i = 0; i < friendViews.size(); i++) {
                View friendView = friendViews.get(i);
                CheckBox friendCheckBox = friendView.findViewById(R.id.friendCheckBox);
                EditText contributionInput = friendView.findViewById(R.id.contributionInput);
                EditText owedInput = friendView.findViewById(R.id.owedInput);

                if (friendCheckBox.isChecked()) {
                    double contributionAmount = Double.parseDouble(contributionInput.getText().toString());
                    double owedAmount = Double.parseDouble(owedInput.getText().toString());

                    Contributor contributor = realm.createObject(Contributor.class, UUID.randomUUID().toString());
                    contributor.setFriendId(friendIds.get(i));
                    contributor.setContributedAmount(contributionAmount);
                    contributor.setAmountOwed(owedAmount);
                    transaction.getContributors().add(contributor);
                }
            }
        }, () -> {
            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
