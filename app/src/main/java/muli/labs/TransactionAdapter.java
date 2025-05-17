package muli.labs;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class TransactionAdapter extends RealmRecyclerViewAdapter<Transaction, TransactionAdapter.ViewHolder> {

    private TripDetailsActivity activity;
    private Realm realm;

    public TransactionAdapter(TripDetailsActivity activity, @Nullable OrderedRealmCollection<Transaction> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
        this.realm = Realm.getDefaultInstance();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView transactionName;
        TextView transactionAmount;
        LinearLayout contributorsContainer;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionName = itemView.findViewById(R.id.transactionName);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            contributorsContainer = itemView.findViewById(R.id.contributorsContainer);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Transaction transaction = getItem(position);
        if (transaction != null) {
            holder.transactionName.setText(transaction.getName());
            holder.transactionAmount.setText("Amount: " + transaction.getTotalBill());

            holder.contributorsContainer.removeAllViews();

            // Add column headers
            View headerView = LayoutInflater.from(activity).inflate(R.layout.contributor_item, holder.contributorsContainer, false);
            TextView headerName = headerView.findViewById(R.id.contributorName);
            TextView headerContributed = headerView.findViewById(R.id.contributedAmount);
            TextView headerOwed = headerView.findViewById(R.id.owedAmount);

            headerName.setText("Name");
            headerContributed.setText("Paid");
            headerOwed.setText("Owed");

            holder.contributorsContainer.addView(headerView);

            for (Contributor contributor : transaction.getContributors()) {
                View contributorView = LayoutInflater.from(activity).inflate(R.layout.contributor_item, holder.contributorsContainer, false);
                TextView contributorName = contributorView.findViewById(R.id.contributorName);
                TextView contributedAmount = contributorView.findViewById(R.id.contributedAmount);
                TextView owedAmount = contributorView.findViewById(R.id.owedAmount);

                Friend friend = realm.where(Friend.class).equalTo("id", contributor.getFriendId()).findFirst();
                if (friend != null) {
                    contributorName.setText(friend.getName());
                    contributedAmount.setText(String.valueOf(contributor.getContributedAmount()));
                    owedAmount.setText(String.valueOf(contributor.getAmountOwed()));
                }
                holder.contributorsContainer.addView(contributorView);
            }

            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(activity, EditTransactionActivity.class);
                intent.putExtra("transactionId", transaction.getId());
                activity.startActivity(intent);
            });
            holder.deleteButton.setOnClickListener(v -> {
                activity.confirmAndDeleteTransaction(transaction.getId());
            });
        }
    }
}
