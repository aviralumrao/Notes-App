package com.example.Notes.App.service;

import com.example.Notes.App.model.Note;
import com.example.Notes.App.repository.NoteRepository;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public NoteService(NoteRepository noteRepository, S3Client s3Client) {
        this.noteRepository = noteRepository;
        this.s3Client = s3Client;
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note getNoteById(UUID id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found with id: " + id));
    }

    public Note createNote(String title, String content, MultipartFile image) {
        String imageKey = uploadImage(image);

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setImageKey(imageKey);

        return noteRepository.save(note);
    }

    public Note updateNote(UUID id, String title, String content, MultipartFile newImage) {
        return noteRepository.findById(id)
                .map(note -> {
                    note.setTitle(title);
                    note.setContent(content);
                    if (newImage != null && !newImage.isEmpty()) {
                        deleteImageFromS3(note.getImageKey());
                        note.setImageKey(uploadImage(newImage));
                    }
                    return noteRepository.save(note);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found with id: " + id));
    }

    public void deleteNote(UUID id) {
        var optionalNote = noteRepository.findById(id);
        if (optionalNote.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found with id: " + id);
        }

        Note note = optionalNote.get();
        deleteImageFromS3(note.getImageKey());
        noteRepository.delete(note);
    }

    private String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size too large");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/png")
                || contentType.equals("image/jpeg")
                || contentType.equals("image/svg+xml"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PNG, JPEG, and SVG images are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String imageKey = "notes/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return imageKey;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image to S3", e);
        }
    }

    private void deleteImageFromS3(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            System.err.println("Failed to delete");
        }
    }
}
