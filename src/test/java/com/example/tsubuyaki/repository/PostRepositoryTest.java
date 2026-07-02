package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("投稿一覧_51件以上あるとき_新着50件だけを返す")
    void 投稿一覧_51件以上あるとき_新着50件だけを返す() {
        OffsetDateTime base = OffsetDateTime.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> latest = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latest).hasSize(50);
        assertThat(latest.get(0).getBody()).isEqualTo("body50");
        assertThat(latest.get(49).getBody()).isEqualTo("body1");
        assertThat(latest).extracting(Post::getCreatedAt).isSortedAccordingTo((left, right) -> right.compareTo(left));
    }

    @Test
    @DisplayName("投稿一覧_created_atをタイムゾーン付きTIMESTAMPとしてUTCで取得する")
    void 投稿一覧_createdAtをタイムゾーン付きTimestampとしてUtcで取得する() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-01T08:00:42.381632Z");
        postRepository.saveAndFlush(new Post("alice", "UTCで保存する投稿です", createdAt));

        List<Post> latest = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latest.get(0).getCreatedAt()).isEqualTo(createdAt);
        assertThat(createdAt.toString()).isEqualTo("2026-07-01T08:00:42.381632Z");
        assertThat(createdAt.toLocalDateTime()).hasToString("2026-07-01T08:00:42.381632");
        assertThat(createdAt.getOffset().getId()).isEqualTo("Z");
    }

    @Test
    @DisplayName("投稿一覧_created_atカラム_TIMESTAMPTIMEZONE型で定義される")
    void 投稿一覧_createdAtカラム_TimestampTimezone型で定義される() {
        String typeName = jdbcTemplate.queryForObject(
                """
                SELECT DATA_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'POSTS'
                  AND COLUMN_NAME = 'CREATED_AT'
                """,
                String.class);

        assertThat(typeName).isEqualTo("TIMESTAMP WITH TIME ZONE");
    }
}
