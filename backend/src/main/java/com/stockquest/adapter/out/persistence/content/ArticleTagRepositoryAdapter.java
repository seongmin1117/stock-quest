package com.stockquest.adapter.out.persistence.content;

import com.stockquest.domain.content.articletag.ArticleTag;
import com.stockquest.domain.content.articletag.port.ArticleTagRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArticleTagRepositoryAdapter implements ArticleTagRepository {

    @Override
    public List<ArticleTag> findByArticleId(Long articleId) {
        return List.of();
    }

    @Override
    public List<ArticleTag> findByTagId(Long tagId) {
        return List.of();
    }

    @Override
    public void saveAll(List<ArticleTag> articleTags) {
        // Stub implementation
    }

    @Override
    public void deleteByArticleId(Long articleId) {
        // Stub implementation
    }

    @Override
    public void deleteByTagId(Long tagId) {
        // Stub implementation
    }
}