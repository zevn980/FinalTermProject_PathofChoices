package com.example.finaltermproject;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Objects;

public final class DialogEntry implements Parcelable {
    private static final int MAX_DIALOG_TEXT_LENGTH = 1000;
    
    private final int id;
    private final String text;

    public DialogEntry(int id, @NonNull String text) {
        if (id < 0) {
            throw new IllegalArgumentException("Dialog ID cannot be negative");
        }
        validateText(text);
        
        this.id = id;
        this.text = text;
    }

    private static void validateText(@NonNull String text) {
        if (text == null) {
            throw new IllegalArgumentException("Dialog text cannot be null");
        }
        
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Dialog text cannot be empty");
        }
        if (trimmed.length() > MAX_DIALOG_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                "Dialog text cannot be longer than " + MAX_DIALOG_TEXT_LENGTH + " characters"
            );
        }
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DialogEntry that = (DialogEntry) o;
        return id == that.id && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }

    @NonNull
    @Override
    public String toString() {
        return "DialogEntry{" +
               "id=" + id +
               ", text='" + text + '\'' +
               '}';
    }

    // Parcelable implementation
    protected DialogEntry(Parcel in) {
        id = in.readInt();
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DialogEntry> CREATOR = new Creator<DialogEntry>() {
        @Override
        public DialogEntry createFromParcel(Parcel in) {
            return new DialogEntry(in);
        }

        @Override
        public DialogEntry[] newArray(int size) {
            return new DialogEntry[size];
        }
    };
}
