package muli.labs;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Contributor extends RealmObject {
    @PrimaryKey
    private String id;
    @Required
    private String friendId;
    private double contributedAmount;
    private double amountOwed;
    private Friend friend;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public double getContributedAmount() {
        return contributedAmount;
    }

    public void setContributedAmount(double contributedAmount) {
        this.contributedAmount = contributedAmount;
    }

    public double getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(double amountOwed) {
        this.amountOwed = amountOwed;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }
}
