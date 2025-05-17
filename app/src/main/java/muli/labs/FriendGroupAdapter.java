package muli.labs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class FriendGroupAdapter extends RealmRecyclerViewAdapter<FriendGroup, FriendGroupAdapter.ViewHolder> {

    private FriendGroupsFragment fragment;
    private Realm realm;

    public FriendGroupAdapter(FriendGroupsFragment fragment, @Nullable OrderedRealmCollection<FriendGroup> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.fragment = fragment;
        this.realm = Realm.getDefaultInstance();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView friendNames;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            friendNames = itemView.findViewById(R.id.groupMembers);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.friend_group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final FriendGroup group = getItem(position);
        if (group != null) {
            holder.groupName.setText(group.getName());
            holder.friendNames.setText(getFriendNames(group));

            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(fragment.getContext(), EditFriendGroupActivity.class);
                intent.putExtra("groupId", group.getId());
                fragment.getContext().startActivity(intent);
            });

            holder.deleteButton.setOnClickListener(v -> {
                fragment.confirmAndDeleteGroup(group.getId());
            });
        }
    }

    private String getFriendNames(FriendGroup group) {
        StringBuilder names = new StringBuilder();
        for (Friend friend : group.getMembers()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(friend.getName());
        }

        // Truncate the names if they exceed a certain length
        if (names.length() > 50) {
            names.setLength(50);
            names.append("...");
        }

        return names.toString();
    }
}
