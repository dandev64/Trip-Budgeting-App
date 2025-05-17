package muli.labs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class FriendGroupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendGroupAdapter adapter;
    private Realm realm;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFriendGroups);
        Button addGroupButton = view.findViewById(R.id.addGroupButton);

        realm = Realm.getDefaultInstance();

        // Retrieve the userId from SharedPreferences
        SharedPreferences credentials = getActivity().getSharedPreferences("credentials", Context.MODE_PRIVATE);
        userId = credentials.getString("uuid", null);

        addGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddFriendGroupActivity.class);
            startActivity(intent);
        });

        // Filter friend groups by userId
        RealmResults<FriendGroup> friendGroups = realm.where(FriendGroup.class)
                .equalTo("userId", userId)
                .findAll();
        adapter = new FriendGroupAdapter(this, friendGroups, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void confirmAndDeleteGroup(String groupId) {
        new androidx.appcompat.app.AlertDialog.Builder(this.getContext())
                .setTitle("Delete Friend Group")
                .setMessage("Are you sure you want to delete this friend group?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    realm.executeTransactionAsync(realm -> {
                        FriendGroup fg = realm.where(FriendGroup.class).equalTo("id", groupId).findFirst();
                        if (fg != null) {
                            fg.deleteFromRealm();
                        }
                    }, () -> {
                        Toast.makeText(getContext(), "Friend group deleted", Toast.LENGTH_SHORT).show();
                    }, error -> {
                        Toast.makeText(getContext(), "Error deleting friend group", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null) {
            realm.close();
        }
    }
}
