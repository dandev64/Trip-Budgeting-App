package muli.labs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class TripDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    Realm realm;
    private TextView totalAmount;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        realm = Realm.getDefaultInstance();
        recyclerView = findViewById(R.id.recyclerView);
        Button addTransactionButton = findViewById(R.id.addTransactionButton);
        Button buttonShowStreamlinedTransactions = findViewById(R.id.buttonShowStreamlinedTransactions);
        totalAmount = findViewById(R.id.totalAmount);

        addTransactionButton.setOnClickListener(view -> {
            Intent intent = new Intent(TripDetailsActivity.this, AddTransactionActivity.class);
            intent.putExtra("tripId", tripId);
            startActivity(intent);
        });

        buttonShowStreamlinedTransactions.setOnClickListener(view -> {
            Intent intent = new Intent(TripDetailsActivity.this, ShowStreamlinedTransactionsActivity.class);
            intent.putExtra("tripId", tripId);
            startActivity(intent);
        });

        tripId = getIntent().getStringExtra("tripId");
        RealmResults<Transaction> transactions = realm.where(Transaction.class).equalTo("tripId", tripId).findAll();
        adapter = new TransactionAdapter(this, transactions, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        calculateTotalAmount(transactions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RealmResults<Transaction> transactions = realm.where(Transaction.class).equalTo("tripId", tripId).findAll();
        adapter.updateData(transactions);
        calculateTotalAmount(transactions);
    }

    private void calculateTotalAmount(RealmResults<Transaction> transactions) {
        double total = 0;
        for (Transaction transaction : transactions) {
            total += transaction.getTotalBill();
        }
        totalAmount.setText("Total: " + total);
    }

    public void confirmAndDeleteTransaction(String transactionId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    realm.executeTransactionAsync(realm -> {
                        Transaction transaction = realm.where(Transaction.class).equalTo("id", transactionId).findFirst();
                        if (transaction != null) {
                            transaction.deleteFromRealm();
                        }
                    }, () -> {
                        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        RealmResults<Transaction> transactions = realm.where(Transaction.class).equalTo("tripId", tripId).findAll();
                        adapter.updateData(transactions);
                        calculateTotalAmount(transactions);
                    }, error -> {
                        Toast.makeText(this, "Error deleting transaction", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
