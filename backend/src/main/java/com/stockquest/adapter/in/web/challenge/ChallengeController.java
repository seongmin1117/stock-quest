package com.stockquest.adapter.in.web.challenge;

import com.stockquest.adapter.in.web.challenge.dto.ChallengeDetailResponse;
import com.stockquest.adapter.in.web.challenge.dto.ChallengeListResponse;
import com.stockquest.adapter.in.web.challenge.dto.StartChallengeResponse;
import com.stockquest.adapter.in.web.challenge.dto.ChallengeInstrumentsResponse;
import com.stockquest.application.challenge.port.in.GetChallengeDetailUseCase;
import com.stockquest.application.challenge.port.in.GetChallengeListUseCase;
import com.stockquest.application.challenge.port.in.StartChallengeUseCase;
import com.stockquest.application.challenge.port.in.GetChallengeInstrumentsUseCase;
import com.stockquest.application.challenge.dto.GetChallengeDetailQuery;
import com.stockquest.application.challenge.dto.GetChallengeListQuery;
import com.stockquest.application.challenge.dto.GetChallengeInstrumentsQuery;
import com.stockquest.application.challenge.port.in.StartChallengeUseCase.StartChallengeCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 챌린지 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "챌린지", description = "투자 챌린지 관련 API")
public class ChallengeController {
    
    private final GetChallengeListUseCase getChallengeListUseCase;
    private final GetChallengeDetailUseCase getChallengeDetailUseCase;
    private final StartChallengeUseCase startChallengeUseCase;
    private final GetChallengeInstrumentsUseCase getChallengeInstrumentsUseCase;
    
    @GetMapping
    @Operation(summary = "챌린지 목록 조회", description = "활성 상태의 챌린지 목록을 조회합니다")
    public ResponseEntity<ChallengeListResponse> getChallengeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var query = new GetChallengeListQuery(page, size);
        var result = getChallengeListUseCase.getChallengeList(query);

        return ResponseEntity.ok(ChallengeListResponse.from(result));
    }

    @GetMapping("/active")
    @Operation(summary = "활성 챌린지 목록 조회", description = "현재 활성 상태의 챌린지 목록을 조회합니다")
    public ResponseEntity<ChallengeListResponse> getActiveChallenges() {
        var query = new GetChallengeListQuery(0, 20); // 활성 챌린지는 페이징 없이 최대 20개
        var result = getChallengeListUseCase.getChallengeList(query);

        return ResponseEntity.ok(ChallengeListResponse.from(result));
    }
    
    @GetMapping("/{challengeId}")
    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다")
    public ResponseEntity<ChallengeDetailResponse> getChallengeDetail(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        var query = new GetChallengeDetailQuery(challengeId, userId);
        var result = getChallengeDetailUseCase.getChallengeDetail(query);
        
        return ResponseEntity.ok(ChallengeDetailResponse.from(result));
    }
    
    @PostMapping("/{challengeId}/start")
    @Operation(summary = "챌린지 시작", description = "새로운 챌린지 세션을 시작합니다")
    public ResponseEntity<StartChallengeResponse> startChallenge(
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "false") boolean forceRestart,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        var command = new StartChallengeCommand(userId, challengeId, forceRestart);
        var result = startChallengeUseCase.start(command);

        return ResponseEntity.ok(StartChallengeResponse.from(result));
    }

    @GetMapping("/{challengeId}/instruments")
    @Operation(summary = "챌린지 상품 목록 조회", description = "특정 챌린지에 포함된 금융상품 목록을 조회합니다")
    public ResponseEntity<ChallengeInstrumentsResponse> getChallengeInstruments(
            @PathVariable Long challengeId) {

        var query = new GetChallengeInstrumentsQuery(challengeId);
        var result = getChallengeInstrumentsUseCase.getChallengeInstruments(query);

        return ResponseEntity.ok(ChallengeInstrumentsResponse.from(result));
    }

    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}