package com.example.finaltermproject;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Objects;

public class User implements Parcelable {
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;
    
    private final int id;
    private String username;

    public User(int id, @NonNull String username) {
        if (id < 0) {
            throw new IllegalArgumentException("User ID cannot be negative");
        }
        validateUsername(username);
        
        this.id = id;
        this.username = username;
    }

    private static void validateUsername(@NonNull String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        
        String trimmed = username.trim();
        if (trimmed.length() < MIN_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                "Username must be at least " + MIN_USERNAME_LENGTH + " characters long"
            );
        }
        if (trimmed.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                "Username cannot be longer than " + MAX_USERNAME_LENGTH + " characters"
            );
        }
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException(
                "Username can only contain letters, numbers, and underscores"
            );
        }
    }

    public int getId() { 
        return id; 
    }

    @NonNull
    public String getUsername() { 
        return username; 
    }

    public void setUsername(@NonNull String username) {
        validateUsername(username);
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @NonNull
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "'}";
    }

    // Parcelable implementation
    protected User(Parcel in) {
        id = in.readInt();
        username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(username);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
