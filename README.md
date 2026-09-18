# Notes App API

A Spring Boot RESTful API for managing notes with AWS S3 image upload support and request validation.

---

## Base URL
```
http://localhost:8080/api/notes
```

---

## Endpoints Overview

| Method | Endpoint | Content-Type | Description |
|---|---|---|---|
| `GET` | `/api/notes` | N/A | Retrieve all notes |
| `POST` | `/api/notes` | `multipart/form-data` | Create a new note (with optional image) |
| `GET` | `/api/notes/{id}` | N/A | Get a note by ID |
| `PUT` | `/api/notes/{id}` | `application/json` | Update an existing note |
| `DELETE` | `/api/notes/{id}` | N/A | Delete a note and its associated S3 image |

---

## Endpoints Details & Testing

### 1. Get All Notes
- **Method:** `GET`
- **Path:** `/api/notes`
- **Request Body:** None
- **Response:** `200 OK`
  ```json
  [
    {
      "id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "title": "Meeting Notes",
      "content": "Discuss project timeline and deliverables.",
      "imageKey": "notes/123e4567-e89b-12d3-a456-426614174000.png"
    }
  ]
  ```
- **cURL Example:**
  ```bash
  curl -X GET http://localhost:8080/api/notes
  ```



## Validation & Constraints Summary

- **Title (`title`):** Required, cannot be blank (`@NotBlank`).
- **Content (`content`):** Required, cannot be blank (`@NotBlank`).
- **Image Upload (`image`):**
  - Allowed MIME Types: `image/png`, `image/jpeg`, `image/svg+xml`
  - Maximum File Size: `1 MB`
