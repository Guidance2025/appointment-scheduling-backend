package org.rocs.asa.controller.post;

import org.rocs.asa.domain.post.Post;
import org.rocs.asa.service.post.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guidance/posts")
@CrossOrigin("*")
@PreAuthorize("hasRole('STAFF')")
public class PostController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@RequestBody Post post, Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        Post createdPost = postService.createPost(post, employeeNumber);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @PutMapping("/update/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @RequestBody Post post, Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        Post updatedPost = postService.updatePost(postId, post, employeeNumber);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        postService.deletePost(postId, employeeNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<Post>> getPosts(Authentication authentication) {
        Long employeeNumber = getEmployeeNumberFromAuth(authentication);
        List<Post> posts = postService.getPostsByStaff(employeeNumber);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<Post>> getPostsBySection(@PathVariable Long sectionId) {
        List<Post> posts = postService.getPostsBySection(sectionId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Post>> getPostsByCategory(@PathVariable Long categoryId) {
        List<Post> posts = postService.getPostsByCategory(categoryId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    private Long getEmployeeNumberFromAuth(Authentication authentication) {
        throw new UnsupportedOperationException("Implement based on your authentication setup");
    }
}
