package com.example.finaltermproject;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class NewUserDialogFragment extends DialogFragment {

    public interface OnUserCreatedListener {
        void onUserCreated(User newUser);
    }

    private OnUserCreatedListener listener;
    private DatabaseHelper db;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnUserCreatedListener) {
            listener = (OnUserCreatedListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnUserCreatedListener");
        }
        db = DatabaseHelper.getInstance(context.getApplicationContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_story, null);
        EditText editUserName = view.findViewById(R.id.editUserName);
        Button btnStart = view.findViewById(R.id.btnStartNewStory);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();

        btnStart.setOnClickListener(v -> {
            String name = editUserName.getText().toString().trim();
            if (name.isEmpty()) {
                editUserName.setError("Name is required");
                return;
            }

            try {
                long userId = db.addUser(name);
                User newUser = new User((int) userId, name);
                listener.onUserCreated(newUser);
                dialog.dismiss();
            } catch (IllegalStateException e) {
                editUserName.setError("Name already exists");
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error creating user", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        return dialog;
    }
}

