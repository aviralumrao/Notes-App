package com.example.Notes.App.controller;

import com.example.Notes.App.dto.NoteRequest;
import com.example.Notes.App.model.Note;
import com.example.Notes.App.service.NoteService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<Note> getNotes() {
        return noteService.getAllNotes();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Note> createNote(@Valid @ModelAttribute NoteRequest request) {
        Note savedNote = noteService.createNote(request.getTitle(), request.getContent(), request.getImage());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
    }

    @GetMapping("/{id}")
    public Note getNoteById(@PathVariable UUID id) {
        return noteService.getNoteById(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Note updateNote(@PathVariable UUID id, @Valid @ModelAttribute NoteRequest request) {
        return noteService.updateNote(id, request.getTitle(), request.getContent(), request.getImage());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
