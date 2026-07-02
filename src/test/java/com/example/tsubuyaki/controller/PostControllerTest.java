package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("まだ投稿はありません"));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsへGETリクエストする")
    void 投稿一覧_更新ボタン_押すとpostsへGETリクエストする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("method=\"get\"")
                        .contains("action=\"/posts/\"")
                        .contains("type=\"submit\"")
                        .contains("更新"));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿あり_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = new Post(
                "alice",
                "これはテスト投稿です",
                Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .containsSubsequence("alice", "これはテスト投稿です", "2026-05-23 19:00"));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_詳細画面へのリンクを表示する")
    void 投稿一覧_投稿あり_詳細画面へのリンクを表示する() throws Exception {
        Post post = new Post(
                "alice",
                "詳細リンク付きの投稿です",
                Instant.parse("2026-05-23T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("id=\"post-42\"")
                        .contains("href=\"/posts/detail/42\"")
                        .contains("詳細"));
    }

    @Test
    @DisplayName("投稿フォーム_一覧から遷移するとき_posts_formへのリンクを表示する")
    void 投稿フォーム_一覧から遷移するとき_posts_formへのリンクを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("href=\"/posts/form\""));
    }

    @Test
    @DisplayName("投稿詳細_存在するIDの場合_posts_detailに投稿者内容投稿日を表示する")
    void 投稿詳細_存在するIDの場合_posts_detailに投稿者内容投稿日を表示する() throws Exception {
        Post post = new Post(
                "alice",
                "詳細画面に表示する投稿です",
                Instant.parse("2026-05-23T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/detail/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .containsSubsequence("alice", "詳細画面に表示する投稿です", "2026-05-23 19:00"));

        verify(postService).findById(42L);
    }

    @Test
    @DisplayName("投稿詳細_存在しないIDの場合_404エラー画面を表示する")
    void 投稿詳細_存在しないIDの場合_404エラー画面を表示する() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/detail/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("404 エラー")
                        .contains("投稿が見つかりません"));

        verify(postService).findById(999L);
    }

    @Test
    @DisplayName("投稿登録_成功した場合_postsにリダイレクトされる")
    void 投稿登録_成功した場合_postsにリダイレクトされる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "こんにちは"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "こんにちは");
    }

    @Test
    @DisplayName("投稿登録_失敗した場合_posts_formが再表示される")
    void 投稿登録_失敗した場合_posts_formが再表示される() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "こんにちは"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .contains("投稿者名を入力してください"));
    }
}
