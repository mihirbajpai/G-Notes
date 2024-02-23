package com.example.notes;

public class Note {
    private String noteId;
    private String noteContent;

    public Note(String noteId, String noteContent) {
        this.noteId = noteId;
        this.noteContent = noteContent;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getNoteContent() {
        return noteContent;
    }

}
