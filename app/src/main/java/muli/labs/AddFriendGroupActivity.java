package muli.labs;

import android.content.SharedPreferences;
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

public class AddFriendGroupActivity extends AppCompatActivity {

    private EditText friendGroupName;
    private EditText addFriendName;
    private Button saveGroupButton;
    private ImageButton addFriendButton;
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendAdapter;
    private List<Friend> friendsList;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_group);

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
        SharedPreferences credentials = getSharedPreferences("credentials", MODE_PRIVATE);
        String userId = credentials.getString("uuid", null);
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
            String groupId = UUID.randomUUID().toString();
            FriendGroup friendGroup = realm.createObject(FriendGroup.class, groupId);
            friendGroup.setName(groupName);
            friendGroup.setUserId(userId);

            // Set groupId for each friend
            for (Friend friend : friendsList) {
                friend.setGroupId(groupId);
            }

            friendGroup.getMembers().addAll(friendsList);
        }, () -> {
            Toast.makeText(this, "Friend Group saved", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            Toast.makeText(this, "Error saving Friend Group", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
