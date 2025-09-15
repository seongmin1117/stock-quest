package com.stockquest.adapter.in.web.session;

import com.stockquest.adapter.in.web.session.dto.*;
import com.stockquest.application.order.port.in.PlaceOrderUseCase;
import com.stockquest.application.order.port.in.PlaceOrderUseCase.PlaceOrderCommand;
import com.stockquest.application.session.port.in.CloseChallengeUseCase;
import com.stockquest.application.session.port.in.GetSessionDetailUseCase;
import com.stockquest.application.session.port.in.GetPortfolioUseCase;
import com.stockquest.application.session.port.in.GetOrdersUseCase;
import com.stockquest.application.session.port.in.CloseChallengeUseCase.CloseChallengeCommand;
import com.stockquest.application.session.dto.GetSessionDetailQuery;
import com.stockquest.application.session.dto.GetPortfolioQuery;
import com.stockquest.application.session.dto.GetOrdersQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 챌린지 세션(거래) 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "챌린지 세션", description = "챌린지 세션 및 거래 관련 API")
public class SessionController {
    
    private final GetSessionDetailUseCase getSessionDetailUseCase;
    private final GetPortfolioUseCase getPortfolioUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final PlaceOrderUseCase placeOrderUseCase;
    private final CloseChallengeUseCase closeChallengeUseCase;
    
    @GetMapping("/{sessionId}")
    @Operation(summary = "세션 상세 조회", description = "챌린지 세션의 상세 정보를 조회합니다")
    public ResponseEntity<SessionDetailResponse> getSessionDetail(
            @PathVariable @NotNull @Positive Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        var query = new GetSessionDetailQuery(sessionId, userId);
        var result = getSessionDetailUseCase.getSessionDetail(query);
        
        return ResponseEntity.ok(SessionDetailResponse.from(result));
    }

    @GetMapping("/{sessionId}/portfolio")
    @Operation(summary = "포트폴리오 조회", description = "챌린지 세션의 포트폴리오를 조회합니다")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @PathVariable @NotNull @Positive Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        var query = new GetPortfolioQuery(sessionId, userId);
        var result = getPortfolioUseCase.getPortfolio(query);

        return ResponseEntity.ok(PortfolioResponse.from(result));
    }

    @GetMapping("/{sessionId}/orders")
    @Operation(summary = "주문 내역 조회", description = "챌린지 세션의 주문 내역을 조회합니다")
    public ResponseEntity<OrdersResponse> getOrders(
            @PathVariable @NotNull @Positive Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        var query = new GetOrdersQuery(sessionId, userId);
        var result = getOrdersUseCase.getOrders(query);

        return ResponseEntity.ok(OrdersResponse.from(result));
    }

    @PostMapping("/{sessionId}/orders")
    @Operation(summary = "주문 실행", description = "챌린지 세션에서 주문을 실행합니다")
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @PathVariable @NotNull @Positive Long sessionId,
            @Valid @RequestBody PlaceOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        var command = new PlaceOrderCommand(
                sessionId,
                request.instrumentKey(),
                request.side(),
                request.quantity(),
                request.orderType(),
                request.limitPrice()
        );
        var result = placeOrderUseCase.placeOrder(command);
        
        return ResponseEntity.ok(PlaceOrderResponse.from(result));
    }
    
    @PostMapping("/{sessionId}/close")
    @Operation(summary = "챌린지 종료", description = "챌린지 세션을 종료하고 결과를 확인합니다")
    public ResponseEntity<CloseChallengeResponse> closeChallenge(
            @PathVariable @NotNull @Positive Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        var command = new CloseChallengeCommand(sessionId);
        var result = closeChallengeUseCase.close(command);
        
        return ResponseEntity.ok(CloseChallengeResponse.from(result));
    }
    
    private Long getUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}