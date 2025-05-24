package com.example.finaltermproject;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Objects;

public final class Choice implements Parcelable {
    private static final int MAX_CHOICE_TEXT_LENGTH = 200;
    
    private final int id;
    private final int dialogId;
    private final String choiceText;
    private final int nextDialogId;

    public Choice(int id, int dialogId, @NonNull String choiceText, int nextDialogId) {
        if (id < 0) {
            throw new IllegalArgumentException("Choice ID cannot be negative");
        }
        if (dialogId < 0) {
            throw new IllegalArgumentException("Dialog ID cannot be negative");
        }
        if (nextDialogId < 0) {
            throw new IllegalArgumentException("Next dialog ID cannot be negative");
        }
        validateChoiceText(choiceText);

        this.id = id;
        this.dialogId = dialogId;
        this.choiceText = choiceText;
        this.nextDialogId = nextDialogId;
    }

    private static void validateChoiceText(@NonNull String choiceText) {
        if (choiceText == null) {
            throw new IllegalArgumentException("Choice text cannot be null");
        }
        
        String trimmed = choiceText.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Choice text cannot be empty");
        }
        if (trimmed.length() > MAX_CHOICE_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                "Choice text cannot be longer than " + MAX_CHOICE_TEXT_LENGTH + " characters"
            );
        }
    }

    public int getId() {
        return id;
    }

    public int getDialogId() {
        return dialogId;
    }

    @NonNull
    public String getChoiceText() {
        return choiceText;
    }

    public int getNextDialogId() {
        return nextDialogId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Choice choice = (Choice) o;
        return id == choice.id &&
               dialogId == choice.dialogId &&
               nextDialogId == choice.nextDialogId &&
               Objects.equals(choiceText, choice.choiceText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dialogId, choiceText, nextDialogId);
    }

    @NonNull
    @Override
    public String toString() {
        return "Choice{" +
               "id=" + id +
               ", dialogId=" + dialogId +
               ", choiceText='" + choiceText + '\'' +
               ", nextDialogId=" + nextDialogId +
               '}';
    }

    // Parcelable implementation
    protected Choice(Parcel in) {
        id = in.readInt();
        dialogId = in.readInt();
        choiceText = in.readString();
        nextDialogId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(dialogId);
        dest.writeString(choiceText);
        dest.writeInt(nextDialogId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Choice> CREATOR = new Creator<Choice>() {
        @Override
        public Choice createFromParcel(Parcel in) {
            return new Choice(in);
        }

        @Override
        public Choice[] newArray(int size) {
            return new Choice[size];
        }
    };
}
