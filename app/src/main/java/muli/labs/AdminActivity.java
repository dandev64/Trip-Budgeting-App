package muli.labs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;

public class AdminActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button addButton, adminClearButton;

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        adminClearButton = findViewById(R.id.adminClearButton);

        // Initialize Realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RealmResults<User> users = realm.where(User.class).findAll();
        UserAdapter adapter = new UserAdapter(this, users, true);
        recyclerView.setAdapter(adapter);

        // Setup button listeners
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, RegisterActivity.class));
            }
        });

        adminClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllUsers();
            }
        });
    }

    public void confirmAndDeleteUser(final User user) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    realm.executeTransactionAsync(realm -> {
                        User u = realm.where(User.class).equalTo("uuid", user.getUuid()).findFirst();
                        if (u != null) {
                            File imgFile = new File(u.getImagePath());
                            if (imgFile.exists()) {
                                imgFile.delete();
                            }
                            u.deleteFromRealm();
                        }
                    }, () -> {
                        Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    }, error -> {
                        Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void clearAllUsers() {
        realm.executeTransactionAsync(realm -> realm.delete(User.class),
                () -> Toast.makeText(this, "All users cleared", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Error clearing users", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
