package com.stockquest.adapter.out.persistence;

import com.stockquest.application.challenge.dto.ChallengePage;
import com.stockquest.application.challenge.dto.ChallengeSearchCriteria;
import com.stockquest.application.port.out.ChallengePort;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.port.ChallengeRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ChallengePort 인터페이스의 어댑터 구현체
 * 기존 ChallengeRepository를 ChallengePort로 연결
 */
@Repository
public class ChallengePortAdapter implements ChallengePort {

    private final ChallengeRepository challengeRepository;

    public ChallengePortAdapter(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @Override
    public Challenge save(Challenge challenge) {
        return challengeRepository.save(challenge);
    }

    @Override
    public Optional<Challenge> findById(Long id) {
        return challengeRepository.findById(id);
    }

    @Override
    public ChallengePage findByCriteria(ChallengeSearchCriteria criteria) {
        // TODO: 실제 구현에서는 criteria를 사용한 복잡한 검색 로직 구현
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;
        
        List<Challenge> content = challengeRepository.findAll(page, size);
        long totalElements = content.size(); // TODO: 실제 count 쿼리로 대체
        
        return ChallengePage.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .build();
    }

    @Override
    public List<Challenge> findByCategoryId(Long categoryId) {
        // TODO: ChallengeRepository에 findByCategoryId 메서드 추가 필요
        return challengeRepository.findAll(0, 100); // 임시 구현
    }

    @Override
    public List<Challenge> findByTemplateId(Long templateId) {
        // TODO: ChallengeRepository에 findByTemplateId 메서드 추가 필요
        return challengeRepository.findAll(0, 100); // 임시 구현
    }

    @Override
    public List<Challenge> findPopularChallenges(int limit) {
        // 활성 챌린지 중에서 제한된 수만 반환 (인기도 기준이 없으므로 활성 챌린지 중 상위 N개)
        return challengeRepository.findActiveChallenges()
                .stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<Challenge> findFeaturedChallenges() {
        // 활성 챌린지들을 피처드 챌린지로 반환
        return challengeRepository.findActiveChallenges();
    }

    @Override
    public void deleteById(Long id) {
        // TODO: ChallengeRepository에 deleteById 메서드 추가 필요
        // 현재는 구현하지 않음 (소프트 삭제 로직으로 대체)
    }

    @Override
    public List<Challenge> findAll() {
        return challengeRepository.findAll(0, 1000); // 기본적으로 최대 1000개
    }

    @Override
    public List<Challenge> findByStatus(String status) {
        // TODO: String을 ChallengeStatus로 변환하는 로직 필요
        return challengeRepository.findAll(0, 100); // 임시 구현
    }
}