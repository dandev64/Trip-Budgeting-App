package muli.labs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import io.realm.Realm;
import io.realm.RealmResults;
import android.Manifest;


public class LoginActivity extends AppCompatActivity {


    EditText userNameInput;
    EditText passwordInput;
    CheckBox rememberMeInput;
    Button signInButton;
    Button adminButton;
    Button clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkPermissions();
    }
    public void init() {
        SharedPreferences credentials = getSharedPreferences("credentials", MODE_PRIVATE);

        userNameInput = findViewById(R.id.userNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeInput = findViewById(R.id.rememberMeInput);

        if (credentials.getBoolean("rememberMe", false)) {
            String uuid = credentials.getString("uuid", "");
            Realm realm = Realm.getDefaultInstance();
            User user = realm.where(User.class)
                    .equalTo("uuid", uuid)
                    .findFirst();
            if (user != null && user.isValid()) {
                userNameInput.setText(user.getName());
                passwordInput.setText(user.getPassword());
                rememberMeInput.setChecked(true);
            }
        }



        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {signIn();}
        });

        adminButton = findViewById(R.id.adminButton);
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {admin();}
        });

        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {clear();}
        });
    }
    public void checkPermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                )
                .withListener(new BaseMultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            // all permissions accepted proceed
                            init();
                        } else {
                            // notify about permissions
                            toastRequirePermissions();
                        }
                    }
                })
                .check();
    }

    public void toastRequirePermissions() {
        Toast.makeText(this, "You must provide permissions for app to run", Toast.LENGTH_LONG).show();
        finish();
    }

    public void signIn() {
        Realm realm = Realm.getDefaultInstance();
        User user = realm.where(User.class)
                .equalTo("name", userNameInput.getText().toString())
                .findFirst();

        if (user == null) {
            Toast.makeText(getApplicationContext(), "No User found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!user.getPassword().equals(passwordInput.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences credentials = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = credentials.edit();
        editor.putBoolean("rememberMe", this.rememberMeInput.isChecked());
        editor.putString("userName", user.getName());
        editor.putString("password", user.getPassword());
        editor.putString("uuid", user.getUuid());
        editor.apply();
        welcome();
        finish();
    }
    public void welcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
    public void admin() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    public void clear() {
        SharedPreferences credentials = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = credentials.edit();
        editor.clear();
        editor.apply();
        this.userNameInput.setHint("User Name");
        this.userNameInput.setText("");
        this.passwordInput.setHint("Password");
        this.passwordInput.setText("");
        this.rememberMeInput.setChecked(false);
    }
}