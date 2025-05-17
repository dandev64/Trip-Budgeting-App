package muli.labs;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

public class EditFriendGroupActivity extends AppCompatActivity {

    EditText friendGroupName;
    EditText addFriendName;
    Button saveGroupButton;
    ImageButton addFriendButton;
    RecyclerView friendsRecyclerView;
    FriendsAdapter friendAdapter;
    List<Friend> friendsList;
    Realm realm;
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friend_group);

        friendGroupName = findViewById(R.id.groupNameInput);
        addFriendName = findViewById(R.id.friendNameInput);
        saveGroupButton = findViewById(R.id.saveButton);
        addFriendButton = findViewById(R.id.addFriendButton);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);

        friendsList = new ArrayList<>();
        friendAdapter = new FriendsAdapter(friendsList);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendAdapter);

        realm = Realm.getDefaultInstance();

        groupId = getIntent().getStringExtra("groupId");
        loadFriendGroup(groupId);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });

        saveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFriendGroup();
            }
        });
    }

    private void loadFriendGroup(String groupId) {
        // Load the friend group without a transaction
        FriendGroup group = realm.where(FriendGroup.class).equalTo("id", groupId).findFirst();
        if (group != null) {
            friendGroupName.setText(group.getName());
            friendsList.clear();
            friendsList.addAll(realm.copyFromRealm(group.getMembers()));
            friendAdapter.notifyDataSetChanged();
        }
    }

    private void addFriend() {
        String friendName = addFriendName.getText().toString();
        if (!friendName.isEmpty()) {
            for (Friend friend : friendsList) {
                if (friend.getName().equalsIgnoreCase(friendName)) {
                    Toast.makeText(this, "Friend already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Friend friend = new Friend();
            friend.setId(UUID.randomUUID().toString());
            friend.setName(friendName);
            friendsList.add(friend);
            friendAdapter.notifyDataSetChanged();
            addFriendName.setText("");
        } else {
            Toast.makeText(this, "Enter friend name", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFriendGroup() {
        String groupName = friendGroupName.getText().toString();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (friendsList.isEmpty()) {
            Toast.makeText(this, "Add at least one friend", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.executeTransactionAsync(realm -> {
            FriendGroup friendGroup = realm.where(FriendGroup.class).equalTo("id", groupId).findFirst();
            if (friendGroup != null) {
                friendGroup.setName(groupName);
                friendGroup.getMembers().clear();
                for (Friend friend : friendsList) {
                    Friend managedFriend = realm.copyToRealmOrUpdate(friend);
                    managedFriend.setGroupId(groupId);
                    friendGroup.getMembers().add(managedFriend);
                }
            }
        }, () -> {
            Toast.makeText(this, "Friend Group updated", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            Toast.makeText(this, "Error updating Friend Group", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
