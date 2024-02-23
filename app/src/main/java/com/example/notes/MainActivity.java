package com.example.notes;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginSuccessListener {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private List<Note> notesList;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, "Please wait while loading the data.", Toast.LENGTH_SHORT).show();
            displayNotesFromFirebase();
        } else {
            if (savedInstanceState == null) {
                LoginFragment loginFragment = new LoginFragment();
                loginFragment.setOnLoginSuccessListener(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, loginFragment)
                        .commit();
            }
        }

    }

    @Override
    public void onLoginSuccess() {
        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();
        Toast.makeText(this, "Please wait while loading the data.", Toast.LENGTH_SHORT).show();
        displayNotesFromFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_add_note) {
            showAddNoteDialog();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Note");

        final EditText input = new EditText(this);
        input.setHint("Enter your note");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String noteContent = input.getText().toString().trim();
            saveNoteToFirebase(noteContent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveNoteToFirebase(String noteContent) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notes");


        String noteId = generateNoteId();
        databaseRef.child(noteId).setValue(noteContent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to save note", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateNoteId() {
        return String.valueOf(System.currentTimeMillis());
    }


    private void displayNotesFromFirebase() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesList = new ArrayList<>();
        adapter = new NotesAdapter(notesList, MainActivity.this);
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notes");
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    notesList.clear();
                    for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                        String noteId = noteSnapshot.getKey();
                        String noteContent = noteSnapshot.getValue(String.class);
                        Note note = new Note(noteId, noteContent);
                        notesList.add(note);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching notes", databaseError.toException());
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

}