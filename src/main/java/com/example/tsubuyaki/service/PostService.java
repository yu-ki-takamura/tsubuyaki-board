package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    private final PostRepository repository;
    private final Clock clock;

    @Autowired
    public PostService(PostRepository repository) {
        this(repository, Clock.systemUTC());
    }

    PostService(PostRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<PostView> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public Post create(String author, String body) {
        return repository.save(new Post(author, body, OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC)));
    }

    private PostView toView(Post post) {
        LocalDateTime jstCreatedAt = post.getCreatedAt().atZoneSameInstant(JST).toLocalDateTime();
        return new PostView(post.getAuthor(), post.getBody(), jstCreatedAt);
    }
}
