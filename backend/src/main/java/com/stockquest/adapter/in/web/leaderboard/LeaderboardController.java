package com.stockquest.adapter.in.web.leaderboard;

import com.stockquest.adapter.in.web.leaderboard.dto.LeaderboardResponse;
import com.stockquest.application.leaderboard.port.in.CalculateLeaderboardUseCase;
import com.stockquest.application.leaderboard.port.in.GetLeaderboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 리더보드 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/challenges/{challengeId}/leaderboard")
@RequiredArgsConstructor
@Tag(name = "리더보드", description = "챌린지 리더보드 관련 API")
public class LeaderboardController {
    
    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final CalculateLeaderboardUseCase calculateLeaderboardUseCase;
    
    @GetMapping
    @Operation(summary = "리더보드 조회", description = "특정 챌린지의 리더보드를 조회합니다")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard(
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "10") int limit) {
        
        var query = new GetLeaderboardUseCase.GetLeaderboardQuery(challengeId, limit);
        var entries = getLeaderboardUseCase.getLeaderboard(query);
        
        return ResponseEntity.ok(LeaderboardResponse.from(entries));
    }
    
    @PostMapping("/calculate")
    @Operation(summary = "리더보드 계산", description = "챌린지 종료 후 리더보드를 계산합니다")
    public ResponseEntity<List<LeaderboardResponse>> calculateLeaderboard(
            @PathVariable Long challengeId) {
        
        var command = new CalculateLeaderboardUseCase.CalculateLeaderboardCommand(challengeId);
        var entries = calculateLeaderboardUseCase.calculateLeaderboard(command);
        
        return ResponseEntity.ok(LeaderboardResponse.from(entries));
    }
}