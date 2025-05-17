package muli.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.realm.Realm;

public class WelcomeActivity extends AppCompatActivity {

    Realm realm;
    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        realm = Realm.getDefaultInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Load the default fragment (TripsFragment)
        loadFragment(new TripsFragment());
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.nav_trips) {
            selectedFragment = new TripsFragment();
        } else if (itemId == R.id.nav_groups) {
            selectedFragment = new FriendGroupsFragment();
        } else if (itemId == R.id.nav_last_trip) {
            selectedFragment = new TripAnalysisFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }
        return loadFragment(selectedFragment);
    };

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void confirmAndDeleteGroup(final FriendGroup group) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Friend Group")
                .setMessage("Are you sure you want to delete this friend group?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    realm.executeTransactionAsync(realm -> {
                        FriendGroup fg = realm.where(FriendGroup.class).equalTo("id", group.getId()).findFirst();
                        if (fg != null) {
                            fg.deleteFromRealm();
                        }
                    }, () -> {
                        Toast.makeText(this, "Friend group deleted", Toast.LENGTH_SHORT).show();
                    }, error -> {
                        Toast.makeText(this, "Error deleting friend group", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
