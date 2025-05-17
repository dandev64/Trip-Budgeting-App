package muli.labs;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;

public class UserAdapter extends RealmRecyclerViewAdapter<User, UserAdapter.ViewHolder> {

    private AdminActivity activity;

    public UserAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<User> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, password;
        ImageView userPic;
        ImageButton deleteButton, editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            password = itemView.findViewById(R.id.password);
            userPic = itemView.findViewById(R.id.userImage);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = activity.getLayoutInflater().inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = getItem(position);
        if (user != null) {
            holder.name.setText(user.getName());
            holder.password.setText(user.getPassword());

            String imagePath = user.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    Picasso.get().load(imgFile).into(holder.userPic);
                } else {
                    holder.userPic.setImageResource(R.drawable.default_profile);
                }
            } else {
                holder.userPic.setImageResource(R.drawable.default_profile);
            }

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.confirmAndDeleteUser(user);
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, EditActivity.class);
                    intent.putExtra("userId", user.getUuid());
                    activity.startActivity(intent);
                }
            });
        }
    }
}
