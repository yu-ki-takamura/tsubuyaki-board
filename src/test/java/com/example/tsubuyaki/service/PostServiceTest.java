package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryの新着50件をJST表示用に変換して返す")
    void 投稿一覧_latest_Repositoryの新着50件をJst表示用に変換して返す() {
        postService = new PostService(postRepository);
        List<Post> posts = List.of(new Post("alice", "hello", OffsetDateTime.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        var actual = postService.latest();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).author()).isEqualTo("alice");
        assertThat(actual.get(0).body()).isEqualTo("hello");
        assertThat(actual.get(0).createdAt()).hasToString("2026-05-23T19:00");
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_create_UTCのタイムゾーン付き日時で保存する")
    void 投稿登録_create_Utcのタイムゾーン付き日時で保存する() {
        Clock fixedClock = Clock.fixed(OffsetDateTime.parse("2026-05-23T00:00:00Z").toInstant(), ZoneOffset.UTC);
        postService = new PostService(postRepository, fixedClock);

        postService.create("alice", "hello");

        verify(postRepository).save(argThat(post -> post.getCreatedAt()
                .equals(OffsetDateTime.parse("2026-05-23T00:00:00Z"))));
    }
}
