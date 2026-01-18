package org.rocs.asa.controller.post;

import jakarta.validation.Valid;
import org.rocs.asa.domain.post.Post;
import org.rocs.asa.dto.CreatePostRequest;
import org.rocs.asa.service.post.PostService;
import org.rocs.asa.service.student.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/api")
public class PostController {

    private final PostService postService;
    private final StudentService studentService;

    public PostController(PostService postService, StudentService studentService) {
        this.postService = postService;
        this.studentService = studentService;
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        Post post = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);  // Completed the return statement
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Map<String, Object>>> getPosts(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(postService.getAllPosts(limit));
    }

    @GetMapping("/posts/latest")
    public ResponseEntity<List<Map<String, Object>>> getLatestPosts(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(postService.getAllPosts(limit));
    }

    @GetMapping("/posts/quote-of-the-day")
    public ResponseEntity<Map<String, Object>> getQuoteOfTheDay() {
        return ResponseEntity.ok(postService.getQuoteOfTheDay());
    }

    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getFeed(@RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> feed = postService.getFeed(limit);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = postService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/student")
    public ResponseEntity<List<Map<String, Object>>> getPostsForStudent(@RequestParam(defaultValue = "20") int limit) {
        Long studentId = studentService.findByAuthenticatedStudent().getId();
        List<Map<String, Object>> posts = postService.getPostsForStudent(studentId, limit);
        return ResponseEntity.ok(posts);
    }
}