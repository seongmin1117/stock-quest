package com.stockquest.adapter.out.persistence;

import com.stockquest.application.port.out.ChallengeCategoryPort;
import com.stockquest.domain.challenge.ChallengeCategory;
import com.stockquest.domain.challenge.port.ChallengeCategoryRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ChallengeCategoryPort 인터페이스의 어댑터 구현체
 * 기존 ChallengeCategoryRepository를 ChallengeCategoryPort로 연결
 */
@Repository
public class ChallengeCategoryPortAdapter implements ChallengeCategoryPort {

    private final ChallengeCategoryRepository challengeCategoryRepository;

    public ChallengeCategoryPortAdapter(ChallengeCategoryRepository challengeCategoryRepository) {
        this.challengeCategoryRepository = challengeCategoryRepository;
    }

    @Override
    public ChallengeCategory save(ChallengeCategory category) {
        return challengeCategoryRepository.save(category);
    }

    @Override
    public Optional<ChallengeCategory> findById(Long id) {
        return challengeCategoryRepository.findById(id);
    }

    @Override
    public List<ChallengeCategory> findAllActive() {
        return challengeCategoryRepository.findAllActiveOrderBySortOrder();
    }

    @Override
    public List<ChallengeCategory> findAll() {
        // 첫 번째 페이지의 모든 카테고리를 가져옴 (페이지 크기를 크게 설정)
        return challengeCategoryRepository.findAll(
            org.springframework.data.domain.PageRequest.of(0, 1000)
        ).getContent();
    }

    @Override
    public Optional<ChallengeCategory> findByName(String name) {
        return challengeCategoryRepository.findByName(name);
    }

    @Override
    public void deleteById(Long id) {
        challengeCategoryRepository.deleteById(id);
    }

    @Override
    public List<ChallengeCategory> findByParentId(Long parentId) {
        // TODO: 상위 카테고리 기능이 구현되면 해당 메서드 추가 필요
        // 현재는 모든 활성 카테고리 반환
        return findAllActive();
    }
}