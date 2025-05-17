package muli.labs;

import java.util.UUID;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FriendGroup extends RealmObject {
    @PrimaryKey
    private String id = UUID.randomUUID().toString();
    private String userId; // Reference to the User
    private String name;
    private RealmList<Friend> members;

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

    public RealmList<Friend> getMembers() {
        return members;
    }

    public void setMembers(RealmList<Friend> members) {
        this.members = members;
    }
}
