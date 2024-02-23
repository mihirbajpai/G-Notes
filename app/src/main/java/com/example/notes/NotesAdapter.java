package com.example.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notesList;
    private Context context;

    public NotesAdapter(List<Note> notesList, Context context) {
        this.notesList = notesList;
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note noteData = notesList.get(position);
        String note = noteData.getNoteContent();
        holder.textViewNoteContent.setText(note);
        holder.itemView.setOnLongClickListener(view -> {
            showDeleteConfirmationDialog(noteData);
            return true;
        });

        holder.itemView.setOnClickListener(view -> showEditNoteDialog(noteData.getNoteId(), noteData.getNoteContent()));
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNoteContent;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNoteContent = itemView.findViewById(R.id.textViewNoteContent);
        }
    }

    private void showDeleteConfirmationDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this note?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteNoteFromFirebase(note);
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteNoteFromFirebase(Note note) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notes").child(note.getNoteId());
            noteRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditNoteDialog(String noteId, String currentContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Note");

        final EditText input = new EditText(context);
        input.setText(currentContent);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedContent = input.getText().toString().trim();
            updateNoteInFirebase(noteId, updatedContent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateNoteInFirebase(String noteId, String updatedContent) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notes").child(noteId);
            noteRef.setValue(updatedContent)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update note", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }
}