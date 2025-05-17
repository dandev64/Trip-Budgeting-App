package muli.labs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class TripAdapter extends RealmRecyclerViewAdapter<Trip, TripAdapter.ViewHolder> {

    private TripsFragment fragment;
    private Realm realm;

    public TripAdapter(TripsFragment fragment, @Nullable OrderedRealmCollection<Trip> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.fragment = fragment;
        this.realm = Realm.getDefaultInstance();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tripName;
        TextView friendGroupName;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripName = itemView.findViewById(R.id.tripName);
            friendGroupName = itemView.findViewById(R.id.friendGroupName);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            // Set onClickListener for the whole item view
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Trip trip = getItem(position);
                    if (trip != null) {
                        Intent intent = new Intent(fragment.getContext(), TripDetailsActivity.class);
                        intent.putExtra("tripId", trip.getId());
                        fragment.getContext().startActivity(intent);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.trip_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Trip trip = getItem(position);
        if (trip != null) {
            holder.tripName.setText(trip.getName());

            // Fetch the friend group name
            String friendGroupId = trip.getFriendGroupId();
            FriendGroup friendGroup = realm.where(FriendGroup.class).equalTo("id", friendGroupId).findFirst();
            if (friendGroup != null) {
                holder.friendGroupName.setText("Group: " + friendGroup.getName());
            } else {
                holder.friendGroupName.setText("Unknown Group");
            }

            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(fragment.getContext(), EditTripActivity.class);
                intent.putExtra("tripId", trip.getId());
                fragment.getContext().startActivity(intent);
            });

            holder.deleteButton.setOnClickListener(v -> {
                fragment.confirmAndDeleteTrip(trip.getId());
            });
        }
    }
}
