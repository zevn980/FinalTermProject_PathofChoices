package com.example.finaltermproject;

public class Choice {
    private int id;
    private int dialogId;
    private String choiceText;
    private int nextDialogId;

    public Choice(int id, int dialogId, String choiceText, int nextDialogId) {
        this.id = id;
        this.dialogId = dialogId;
        this.choiceText = choiceText;
        this.nextDialogId = nextDialogId;
    }

    public int getId() { return id; }
    public int getDialogId() { return dialogId; }
    public String getChoiceText() { return choiceText; }
    public int getNextDialogId() { return nextDialogId; }
}
