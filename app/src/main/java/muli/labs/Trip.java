package muli.labs;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Trip extends RealmObject {
    @PrimaryKey
    private String id = UUID.randomUUID().toString();
    private String userId; // Reference to the User
    private String name;
    private String friendGroupId; // Reference to the associated FriendGroup
    private RealmList<Transaction> transactions;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendGroupId() {
        return friendGroupId;
    }

    public void setFriendGroupId(String friendGroupId) {
        this.friendGroupId = friendGroupId;
    }

    public RealmList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(RealmList<Transaction> transactions) {
        this.transactions = transactions;
    }
}
