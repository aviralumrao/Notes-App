package com.example.Notes.App.repository;

import com.example.Notes.App.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
