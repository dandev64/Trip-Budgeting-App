package muli.labs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class ShowStreamlinedTransactionsActivity extends AppCompatActivity {

    private Realm realm;
    private String tripId;
    private TableLayout balancesListLayout;
    private TableLayout transactionsListLayout;
    private LinearLayout receiptButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_streamlined_transactions);

        realm = Realm.getDefaultInstance();
        balancesListLayout = findViewById(R.id.balancesListLayout);
        transactionsListLayout = findViewById(R.id.transactionsListLayout);
        receiptButtonsLayout = findViewById(R.id.receiptButtonsLayout);

        tripId = getIntent().getStringExtra("tripId");

        displayStreamlinedTransactions();
    }

    private void displayStreamlinedTransactions() {
        Trip trip = realm.where(Trip.class).equalTo("id", tripId).findFirst();
        if (trip == null) {
            return;
        }

        String friendGroupId = trip.getFriendGroupId();
        FriendGroup friendGroup = realm.where(FriendGroup.class).equalTo("id", friendGroupId).findFirst();
        if (friendGroup == null) {
            return;
        }

        RealmList<Friend> friendsInGroup = friendGroup.getMembers();
        Map<String, Double> balanceMap = new HashMap<>();

        // Calculate balances
        RealmResults<Transaction> transactions = realm.where(Transaction.class).equalTo("tripId", tripId).findAll();
        for (Transaction txn : transactions) {
            for (Contributor contributor : txn.getContributors()) {
                String friendId = contributor.getFriendId();
                double amountOwed = contributor.getAmountOwed();
                double contributedAmount = contributor.getContributedAmount();

                double balance = contributedAmount - amountOwed;

                balanceMap.put(friendId, balanceMap.getOrDefault(friendId, 0.0) + balance);
            }
        }

        // Display balances
        for (Map.Entry<String, Double> entry : balanceMap.entrySet()) {
            String friendId = entry.getKey();
            double balance = entry.getValue();

            Friend friend = realm.where(Friend.class).equalTo("id", friendId).findFirst();
            if (friend != null && friendsInGroup.contains(friend)) {
                String friendName = "   " + friend.getName();
                TableRow balanceRow = new TableRow(this);

                TextView nameTextView = new TextView(this);
                nameTextView.setText(friendName);
                nameTextView.setPadding(4, 4, 4, 4);

                TextView balanceTextView = new TextView(this);
                balanceTextView.setText(String.format("%.2f", balance));
                balanceTextView.setPadding(4, 4, 4, 4);

                balanceRow.addView(nameTextView);
                balanceRow.addView(balanceTextView);

                balancesListLayout.addView(balanceRow);
            }
        }

        // Display transactions
        for (Map.Entry<String, Double> entry : balanceMap.entrySet()) {
            if (entry.getValue() < 0) {
                for (Map.Entry<String, Double> innerEntry : balanceMap.entrySet()) {
                    if (innerEntry.getValue() > 0) {
                        double amount = Math.min(-entry.getValue(), innerEntry.getValue());
                        if (amount > 0) {
                            Friend fromFriend = realm.where(Friend.class).equalTo("id", entry.getKey()).findFirst();
                            Friend toFriend = realm.where(Friend.class).equalTo("id", innerEntry.getKey()).findFirst();
                            if (fromFriend != null && toFriend != null && friendsInGroup.contains(fromFriend) && friendsInGroup.contains(toFriend)) {
                                TableRow transactionRow = new TableRow(this);

                                TextView fromTextView = new TextView(this);
                                fromTextView.setText("   " + fromFriend.getName());
                                fromTextView.setPadding(4, 4, 4, 4);

                                TextView toTextView = new TextView(this);
                                toTextView.setText("   " + toFriend.getName());
                                toTextView.setPadding(4, 4, 4, 4);

                                TextView amountTextView = new TextView(this);
                                amountTextView.setText("   " + String.format("%.2f", amount));
                                amountTextView.setPadding(4, 4, 4, 4);

                                transactionRow.addView(fromTextView);
                                transactionRow.addView(toTextView);
                                transactionRow.addView(amountTextView);

                                transactionsListLayout.addView(transactionRow);

                                balanceMap.put(entry.getKey(), entry.getValue() + amount);
                                balanceMap.put(innerEntry.getKey(), innerEntry.getValue() - amount);
                            }
                        }
                    }
                }
            }
        }

        // Generate receipt buttons
        for (Friend friend : friendsInGroup) {
            Button receiptButton = new Button(this);
            receiptButton.setText(String.format("Generate Receipt for %s", friend.getName()));
            receiptButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ShowReceiptActivity.class);
                intent.putExtra("friendId", friend.getId());
                intent.putExtra("tripId", tripId);
                startActivity(intent);
            });
            receiptButtonsLayout.addView(receiptButton);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
