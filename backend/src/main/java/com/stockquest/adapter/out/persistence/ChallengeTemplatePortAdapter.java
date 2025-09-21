package com.stockquest.adapter.out.persistence;

import com.stockquest.application.port.out.ChallengeTemplatePort;
import com.stockquest.domain.challenge.ChallengeTemplate;
import com.stockquest.domain.challenge.ChallengeDifficulty;
import com.stockquest.domain.challenge.port.ChallengeTemplateRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ChallengeTemplatePort 인터페이스의 어댑터 구현체
 * 기존 ChallengeTemplateRepository를 ChallengeTemplatePort로 연결
 */
@Repository
public class ChallengeTemplatePortAdapter implements ChallengeTemplatePort {

    private final ChallengeTemplateRepository challengeTemplateRepository;

    public ChallengeTemplatePortAdapter(ChallengeTemplateRepository challengeTemplateRepository) {
        this.challengeTemplateRepository = challengeTemplateRepository;
    }

    @Override
    public ChallengeTemplate save(ChallengeTemplate template) {
        return challengeTemplateRepository.save(template);
    }

    @Override
    public Optional<ChallengeTemplate> findById(Long id) {
        return challengeTemplateRepository.findById(id);
    }

    @Override
    public List<ChallengeTemplate> findAllActive() {
        return challengeTemplateRepository.findAllActive();
    }

    @Override
    public List<ChallengeTemplate> findAll() {
        return challengeTemplateRepository.findAll(com.stockquest.domain.common.PageRequest.of(0, 1000)).getContent();
    }

    @Override
    public List<ChallengeTemplate> findByCategoryId(Long categoryId) {
        return challengeTemplateRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<ChallengeTemplate> findPopularTemplates(int limit) {
        // TODO: 실제 구현에서는 사용 횟수나 평점 기반으로 인기 템플릿 조회
        return challengeTemplateRepository.findAll(com.stockquest.domain.common.PageRequest.of(0, limit)).getContent();
    }

    @Override
    public List<ChallengeTemplate> findByDifficulty(String difficulty) {
        try {
            ChallengeDifficulty challengeDifficulty = ChallengeDifficulty.valueOf(difficulty.toUpperCase());
            return challengeTemplateRepository.findByDifficulty(challengeDifficulty);
        } catch (IllegalArgumentException e) {
            return List.of(); // 잘못된 난이도인 경우 빈 리스트 반환
        }
    }

    @Override
    public void deleteById(Long id) {
        challengeTemplateRepository.deleteById(id);
    }

    @Override
    public List<ChallengeTemplate> findByNameContaining(String name) {
        return challengeTemplateRepository.findByNameContaining(name);
    }
}