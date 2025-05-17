package muli.labs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private EditText userNameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private ImageView userImage;
    private TextView rememberMeTextView;
    private Button saveButton;
    private Button cancelButton;
    private String currentPhotoPath;
    private String userId;
    private Realm realm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        realm = Realm.getDefaultInstance();
        userNameInput = view.findViewById(R.id.userNameInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        userImage = view.findViewById(R.id.userImage);
        rememberMeTextView = view.findViewById(R.id.rememberMeTextView);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        userImage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ImageActivity.class);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        });

        saveButton.setOnClickListener(v -> save());
        cancelButton.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        SharedPreferences credentials = getActivity().getSharedPreferences("credentials", getActivity().MODE_PRIVATE);
        userId = credentials.getString("uuid", null);
        User user = realm.where(User.class).equalTo("uuid", userId).findFirst();
        boolean rememberMe = credentials.getBoolean("rememberMe", false);
        loadUserData(user);

        String rememberMeText = "Welcome " + user.getName() + "! " + (rememberMe ? "You will be remembered." : "You will not be remembered.");
        rememberMeTextView.setText(rememberMeText);

        return view;
    }

    private void loadUserData(User user) {
        if (user != null) {
            userNameInput.setText(user.getName());
            passwordInput.setText(user.getPassword());
            confirmPasswordInput.setText(user.getPassword());
            currentPhotoPath = user.getImagePath();

            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                File imageFile = new File(currentPhotoPath);
                Picasso.get().load(imageFile).into(userImage);
            } else {
                userImage.setImageResource(R.drawable.default_profile); // Set default profile picture
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN && data != null) {
            byte[] byteArray = data.getByteArrayExtra("rawJpeg");
            try {
                File imageFile = saveFile(byteArray);
                currentPhotoPath = imageFile.getAbsolutePath();
                Picasso.get().load(imageFile).into(userImage);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error saving image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File saveFile(byte[] jpeg) throws IOException {
        File imageDir = getContext().getExternalCacheDir();
        File imageFile = new File(imageDir, "user_image_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(jpeg);
        }
        return imageFile;
    }

    private void save() {
        String userName = userNameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (userName.isEmpty()) {
            Toast.makeText(getContext(), "Name must not be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Confirm password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.executeTransactionAsync(realm -> {
            User user = realm.where(User.class).equalTo("uuid", userId).findFirst();
            if (user != null) {
                user.setName(userName);
                user.setPassword(password);
                user.setImagePath(currentPhotoPath);
            }
        }, () -> {
            Toast.makeText(getContext(), "User updated", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }, error -> {
            Toast.makeText(getContext(), "Error updating user", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
