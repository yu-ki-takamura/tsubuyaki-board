package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
        PostView post = new PostView(
                "alice",
                "これはテスト投稿です",
                LocalDateTime.parse("2026-05-23T19:00:00"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                        .containsSubsequence("alice", "これはテスト投稿です", "2026-05-23 19:00"));
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
