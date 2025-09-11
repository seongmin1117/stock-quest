package com.stockquest.adapter.in.web.admin;

import com.stockquest.application.challenge.ChallengeManagementService;
import com.stockquest.application.challenge.dto.*;
import com.stockquest.domain.challenge.Challenge;
import com.stockquest.domain.challenge.ChallengeStatus;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 관리자용 챌린지 CRUD API 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/challenges")
@CrossOrigin(origins = "*")
public class AdminChallengeController {

    private final ChallengeManagementService challengeManagementService;

    public AdminChallengeController(ChallengeManagementService challengeManagementService) {
        this.challengeManagementService = challengeManagementService;
    }
    /**
     * 새로운 챌린지 생성
     */
    @PostMapping
    public ResponseEntity<Challenge> createChallenge(@Valid @RequestBody CreateChallengeCommand command) {
        try {
            Challenge challenge = challengeManagementService.createChallenge(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 템플릿 기반 챌린지 생성
     */
    @PostMapping("/from-template")
    public ResponseEntity<Challenge> createChallengeFromTemplate(@Valid @RequestBody CreateFromTemplateCommand command) {
        try {
            Challenge challenge = challengeManagementService.createChallengeFromTemplate(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 챌린지 수정
     */
    @PutMapping("/{challengeId}")
    public ResponseEntity<Challenge> updateChallenge(@PathVariable Long challengeId, 
                                                   @Valid @RequestBody UpdateChallengeCommand command) {
        try {
            command.setChallengeId(challengeId);
            Challenge challenge = challengeManagementService.updateChallenge(command);
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 챌린지 상태 변경
     */
    @PatchMapping("/{challengeId}/status")
    public ResponseEntity<Challenge> changeStatus(@PathVariable Long challengeId,
                                                @RequestParam ChallengeStatus status,
                                                @RequestParam Long modifiedBy) {
        try {
            Challenge challenge = challengeManagementService.changeStatus(challengeId, status, modifiedBy);
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 챌린지 활성화
     */
    @PostMapping("/{challengeId}/activate")
    public ResponseEntity<Challenge> activateChallenge(@PathVariable Long challengeId,
                                                     @RequestParam Long modifiedBy) {
        try {
            Challenge challenge = challengeManagementService.activateChallenge(challengeId, modifiedBy);
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 챌린지 완료 처리
     */
    @PostMapping("/{challengeId}/complete")
    public ResponseEntity<Challenge> completeChallenge(@PathVariable Long challengeId,
                                                     @RequestParam Long modifiedBy) {
        try {
            Challenge challenge = challengeManagementService.completeChallenge(challengeId, modifiedBy);
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 챌린지 아카이브 (소프트 삭제)
     */
    @PostMapping("/{challengeId}/archive")
    public ResponseEntity<Challenge> archiveChallenge(@PathVariable Long challengeId,
                                                    @RequestParam Long modifiedBy) {
        try {
            Challenge challenge = challengeManagementService.archiveChallenge(challengeId, modifiedBy);
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 피처드 챌린지 설정/해제
     */
    @PatchMapping("/{challengeId}/featured")
    public ResponseEntity<Challenge> setFeaturedChallenge(@PathVariable Long challengeId,
                                                        @RequestParam boolean featured,
                                                        @RequestParam Long modifiedBy) {
        try {
            Challenge challenge = challengeManagementService.setFeaturedChallenge(challengeId, featured, modifiedBy);
            return ResponseEntity.ok(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 챌린지 목록 조회 (필터링 및 페이징)
     */
    @GetMapping
    public ResponseEntity<ChallengePage> getChallenges(@ModelAttribute ChallengeSearchCriteria criteria) {
        try {
            ChallengePage challengePage = challengeManagementService.getChallenges(criteria);
            return ResponseEntity.ok(challengePage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 챌린지 상세 조회
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<Challenge> getChallengeById(@PathVariable Long challengeId) {
        Optional<Challenge> challenge = challengeManagementService.getChallengeById(challengeId);
        return challenge.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 카테고리별 챌린지 조회
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Challenge>> getChallengesByCategory(@PathVariable Long categoryId) {
        try {
            List<Challenge> challenges = challengeManagementService.getChallengesByCategory(categoryId);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 템플릿별 챌린지 조회
     */
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<Challenge>> getChallengesByTemplate(@PathVariable Long templateId) {
        try {
            List<Challenge> challenges = challengeManagementService.getChallengesByTemplate(templateId);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 인기 챌린지 조회
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Challenge>> getPopularChallenges(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Challenge> challenges = challengeManagementService.getPopularChallenges(limit);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 피처드 챌린지 조회
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Challenge>> getFeaturedChallenges() {
        try {
            List<Challenge> challenges = challengeManagementService.getFeaturedChallenges();
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 챌린지 복제
     */
    @PostMapping("/{challengeId}/clone")
    public ResponseEntity<Challenge> cloneChallenge(@PathVariable Long challengeId,
                                                  @RequestParam String newTitle,
                                                  @RequestParam Long createdBy) {
        try {
            Challenge challenge = challengeManagementService.cloneChallenge(challengeId, newTitle, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(challenge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}