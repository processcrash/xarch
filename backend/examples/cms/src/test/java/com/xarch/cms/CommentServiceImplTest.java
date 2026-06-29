package com.xarch.cms;

import com.xarch.cms.dto.CommentDTO;
import com.xarch.cms.entity.Comment;
import com.xarch.cms.exception.CmsException;
import com.xarch.cms.mapper.CommentMapper;
import com.xarch.cms.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CommentServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Comment Service Tests")
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should insert comment with PENDING status and default parentId=0")
        void shouldInsertComment() {
            CommentDTO dto = new CommentDTO(1L, null, "Hello world");

            commentService.create(dto, 42L);

            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            verify(commentMapper).insert(captor.capture());
            Comment saved = captor.getValue();
            assertThat(saved.getArticleId()).isEqualTo(1L);
            assertThat(saved.getUserId()).isEqualTo(42L);
            assertThat(saved.getContent()).isEqualTo("Hello world");
            assertThat(saved.getStatus()).isEqualTo(CommentServiceImpl.STATUS_PENDING);
            assertThat(saved.getParentId()).isEqualTo(0L);
        }

        @Test
        @DisplayName("should keep supplied parentId")
        void shouldKeepSuppliedParentId() {
            CommentDTO dto = new CommentDTO(1L, 7L, "Reply");

            commentService.create(dto, 42L);

            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            verify(commentMapper).insert(captor.capture());
            assertThat(captor.getValue().getParentId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("should pass through non-empty content to mapper")
        void shouldPassNonEmptyContent() {
            CommentDTO dto = new CommentDTO(1L, null, "Non-empty");

            commentService.create(dto, 42L);

            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            verify(commentMapper).insert(captor.capture());
            assertThat(captor.getValue().getContent()).isEqualTo("Non-empty");
        }
    }

    @Nested
    @DisplayName("listByArticle / countByArticle")
    class Reads {

        @Test
        @DisplayName("should return comments for article")
        void shouldListByArticle() {
            when(commentMapper.selectByArticleId(1L)).thenReturn(List.of(sampleComment(1L, 1L)));

            List<Comment> result = commentService.listByArticle(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getArticleId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should count comments for article")
        void shouldCountByArticle() {
            when(commentMapper.countByArticleId(1L)).thenReturn(7L);

            long count = commentService.countByArticle(1L);

            assertThat(count).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("hide / delete")
    class Lifecycle {

        @Test
        @DisplayName("should hide comment")
        void shouldHideComment() {
            Comment comment = sampleComment(1L, 1L);
            when(commentMapper.selectOneById(1L)).thenReturn(comment);

            commentService.hide(1L);

            assertThat(comment.getStatus()).isEqualTo(CommentServiceImpl.STATUS_HIDDEN);
            verify(commentMapper).update(comment);
        }

        @Test
        @DisplayName("should throw when hiding missing comment")
        void shouldThrowOnHide_whenMissing() {
            when(commentMapper.selectOneById(1L)).thenReturn(null);

            assertThatThrownBy(() -> commentService.hide(1L))
                    .isInstanceOf(CmsException.class);
        }

        @Test
        @DisplayName("should delete comment by id")
        void shouldDeleteComment() {
            commentService.delete(1L);
            verify(commentMapper).deleteById(1L);
        }
    }

    private Comment sampleComment(long id, long articleId) {
        return Comment.builder()
                .id(id)
                .articleId(articleId)
                .userId(42L)
                .content("hi")
                .parentId(0L)
                .status(CommentServiceImpl.STATUS_VISIBLE)
                .build();
    }
}
