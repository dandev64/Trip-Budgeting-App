package muli.labs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import io.realm.Realm;

public class RegisterActivity extends AppCompatActivity {

    EditText userNameInput;
    EditText passwordInput;
    EditText confirmPasswordInput;
    ImageView userImage;
    Button saveButton;
    Button cancelButton;
    TextView registerText;
    private String currentPhotoPath;
    Realm realm;

    static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();
        userNameInput = findViewById(R.id.userNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        userImage = findViewById(R.id.userImage);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        registerText = findViewById(R.id.registerText);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, ImageActivity.class);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN) {
            byte[] byteArray = data.getByteArrayExtra("rawJpeg");
            try {
                File imageFile = saveFile(byteArray);
                currentPhotoPath = imageFile.getAbsolutePath();
                Picasso.get().load(imageFile).into(userImage);
            } catch (IOException e) {
                Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File saveFile(byte[] jpeg) throws IOException {
        File imageDir = getExternalCacheDir();
        File imageFile = new File(imageDir, "user_image_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(jpeg);
        }
        return imageFile;
    }

    public void save() {
        String userName = userNameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (userName.isEmpty()) {
            Toast.makeText(this, "Name must not be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.executeTransactionAsync(realm -> {
            User user = realm.createObject(User.class, UUID.randomUUID().toString());
            user.setName(userName);
            user.setPassword(password);
            user.setImagePath(currentPhotoPath);
        }, () -> {
            Toast.makeText(getApplicationContext(), "New User saved", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            Toast.makeText(getApplicationContext(), "Error saving user", Toast.LENGTH_SHORT).show();
        });
    }

    public void cancel() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
