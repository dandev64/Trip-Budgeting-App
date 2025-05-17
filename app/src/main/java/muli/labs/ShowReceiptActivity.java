package muli.labs;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.realm.Realm;
import io.realm.RealmResults;

public class ShowReceiptActivity extends AppCompatActivity {

    private Realm realm;
    private String friendId;
    private String tripId;
    private TableLayout receiptLayout;
    private TextView receiptHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_receipt);

        realm = Realm.getDefaultInstance();
        receiptLayout = findViewById(R.id.receiptLayout);
        receiptHeader = findViewById(R.id.receiptHeader);

        friendId = getIntent().getStringExtra("friendId");
        tripId = getIntent().getStringExtra("tripId");

        displayReceipt();
    }

    private void displayReceipt() {
        Friend friend = realm.where(Friend.class).equalTo("id", friendId).findFirst();
        if (friend != null) {
            receiptHeader.setText(String.format("%s's Receipt", friend.getName()));

            TableRow headerRow = new TableRow(this);

            TextView transactionHeader = new TextView(this);
            transactionHeader.setText("Transaction");
            transactionHeader.setPadding(4, 4, 4, 4);
            transactionHeader.setGravity(android.view.Gravity.CENTER);
            transactionHeader.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView contributedHeader = new TextView(this);
            contributedHeader.setText("Contributed");
            contributedHeader.setPadding(4, 4, 4, 4);
            contributedHeader.setGravity(android.view.Gravity.CENTER);
            contributedHeader.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView owedHeader = new TextView(this);
            owedHeader.setText("Owed");
            owedHeader.setPadding(4, 4, 4, 4);
            owedHeader.setGravity(android.view.Gravity.CENTER);
            owedHeader.setTypeface(null, android.graphics.Typeface.BOLD);

            headerRow.addView(transactionHeader);
            headerRow.addView(contributedHeader);
            headerRow.addView(owedHeader);

            receiptLayout.addView(headerRow);

            double totalContributed = 0;
            double totalOwed = 0;

            RealmResults<Transaction> transactions = realm.where(Transaction.class).equalTo("tripId", tripId).findAll();
            for (Transaction transaction : transactions) {
                for (Contributor contributor : transaction.getContributors()) {
                    if (contributor.getFriendId().equals(friendId)) {
                        TableRow transactionRow = new TableRow(this);

                        TextView transactionName = new TextView(this);
                        transactionName.setText(transaction.getName());
                        transactionName.setPadding(4, 4, 4, 4);
                        transactionName.setGravity(android.view.Gravity.CENTER);

                        TextView contributedAmount = new TextView(this);
                        contributedAmount.setText(String.format("%.2f", contributor.getContributedAmount()));
                        contributedAmount.setPadding(4, 4, 4, 4);
                        contributedAmount.setGravity(android.view.Gravity.CENTER);

                        TextView owedAmount = new TextView(this);
                        owedAmount.setText(String.format("%.2f", contributor.getAmountOwed()));
                        owedAmount.setPadding(4, 4, 4, 4);
                        owedAmount.setGravity(android.view.Gravity.CENTER);

                        transactionRow.addView(transactionName);
                        transactionRow.addView(contributedAmount);
                        transactionRow.addView(owedAmount);

                        receiptLayout.addView(transactionRow);

                        totalContributed += contributor.getContributedAmount();
                        totalOwed += contributor.getAmountOwed();
                    }
                }
            }

            TableRow totalRow = new TableRow(this);

            TextView totalLabel = new TextView(this);
            totalLabel.setText("Total");
            totalLabel.setPadding(4, 4, 4, 4);
            totalLabel.setGravity(android.view.Gravity.CENTER);
            totalLabel.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView totalContributedView = new TextView(this);
            totalContributedView.setText(String.format("%.2f", totalContributed));
            totalContributedView.setPadding(4, 4, 4, 4);
            totalContributedView.setGravity(android.view.Gravity.CENTER);

            TextView totalOwedView = new TextView(this);
            totalOwedView.setText(String.format("%.2f", totalOwed));
            totalOwedView.setPadding(4, 4, 4, 4);
            totalOwedView.setGravity(android.view.Gravity.CENTER);

            totalRow.addView(totalLabel);
            totalRow.addView(totalContributedView);
            totalRow.addView(totalOwedView);

            receiptLayout.addView(totalRow);

            double finalBalance = totalContributed - totalOwed;
            TextView finalBalanceTextView = new TextView(this);
            finalBalanceTextView.setText(String.format("Final Balance: %.2f", finalBalance));
            finalBalanceTextView.setPadding(4, 4, 4, 4);
            finalBalanceTextView.setGravity(android.view.Gravity.CENTER);
            finalBalanceTextView.setTypeface(null, android.graphics.Typeface.BOLD);

            receiptLayout.addView(finalBalanceTextView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
