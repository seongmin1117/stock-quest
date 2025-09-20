package com.stockquest.adapter.out.persistence.content;

import com.stockquest.adapter.out.persistence.entity.CategoryJpaEntity;
import com.stockquest.adapter.out.persistence.repository.CategoryJpaRepository;
import com.stockquest.domain.content.category.Category;
import com.stockquest.domain.content.category.CategoryMetadata;
import com.stockquest.domain.content.category.port.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomainModel);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug)
                .map(this::toDomainModel);
    }

    @Override
    public List<Category> findAllActive() {
        return jpaRepository.findAllActive()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findFeaturedCategories() {
        return jpaRepository.findFeaturedCategories()
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentId(Long parentId) {
        return jpaRepository.findByParentIdAndIsActiveTrue(parentId)
                .stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = toJpaEntity(category);
        CategoryJpaEntity saved = jpaRepository.save(entity);
        return toDomainModel(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Category toDomainModel(CategoryJpaEntity entity) {
        CategoryMetadata metadata = new CategoryMetadata(
                entity.getIsActive(),
                entity.getShowOnHomepage(),
                entity.getIconCode(),
                entity.getColorCode()
        );

        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .parentId(entity.getParentId())
                .sortOrder(entity.getSortOrder())
                .metadata(metadata)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CategoryJpaEntity toJpaEntity(Category category) {
        CategoryJpaEntity.CategoryJpaEntityBuilder builder = CategoryJpaEntity.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt());

        if (category.getMetadata() != null) {
            CategoryMetadata metadata = category.getMetadata();
            builder.isActive(metadata.isActive())
                    .showOnHomepage(metadata.isShowOnHomepage())
                    .iconCode(metadata.getIconCode())
                    .colorCode(metadata.getColorCode());
        }

        return builder.build();
    }
}