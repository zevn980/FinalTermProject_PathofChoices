package com.example.finaltermproject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.Objects;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class User implements Parcelable {
    private static final String TAG = "User";
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
    
    private final int id;
    private String username;
    private String passwordHash;
    private String salt;
    private long lastLoginTime;
    private int loginAttempts;
    private boolean isLocked;

    public User(int id, @NonNull String username) {
        this(id, username, null);
    }

    public User(int id, @NonNull String username, String password) {
        if (id < 0) {
            throw new IllegalArgumentException("User ID cannot be negative");
        }
        validateUsername(username);
        
        this.id = id;
        this.username = username;
        this.loginAttempts = 0;
        this.isLocked = false;
        this.lastLoginTime = System.currentTimeMillis();
        
        if (password != null) {
            setPassword(password);
        }
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
        if (!trimmed.matches(USERNAME_PATTERN)) {
            throw new IllegalArgumentException(
                "Username can only contain letters, numbers, and underscores"
            );
        }
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] saltBytes = new byte[16];
            random.nextBytes(saltBytes);
            this.salt = Base64.getEncoder().encodeToString(saltBytes);
            
            // Hash the password with the salt
            this.passwordHash = hashPassword(password, this.salt);
        } catch (Exception e) {
            Log.e(TAG, "Error setting password", e);
            throw new RuntimeException("Failed to set password", e);
        }
    }

    private String hashPassword(String password, String salt) throws Exception {
        String saltedPassword = password + salt;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(saltedPassword.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean verifyPassword(String password) {
        if (password == null || passwordHash == null || salt == null) {
            return false;
        }
        
        try {
            String hashedInput = hashPassword(password, salt);
            return MessageDigest.isEqual(
                hashedInput.getBytes("UTF-8"),
                passwordHash.getBytes("UTF-8")
            );
        } catch (Exception e) {
            Log.e(TAG, "Error verifying password", e);
            return false;
        }
    }

    public boolean isAccountLocked() {
        return isLocked;
    }

    public void recordLoginAttempt(boolean successful) {
        if (successful) {
            loginAttempts = 0;
            isLocked = false;
            lastLoginTime = System.currentTimeMillis();
        } else {
            loginAttempts++;
            if (loginAttempts >= 5) {
                isLocked = true;
            }
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

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    // Parcelable implementation
    protected User(Parcel in) {
        id = in.readInt();
        username = in.readString();
        passwordHash = in.readString();
        salt = in.readString();
        lastLoginTime = in.readLong();
        loginAttempts = in.readInt();
        isLocked = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(username);
        dest.writeString(passwordHash);
        dest.writeString(salt);
        dest.writeLong(lastLoginTime);
        dest.writeInt(loginAttempts);
        dest.writeInt(isLocked ? 1 : 0);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && 
               Objects.equals(username, user.username);
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
}
