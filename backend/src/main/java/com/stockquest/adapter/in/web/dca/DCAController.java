package com.stockquest.adapter.in.web.dca;

import com.stockquest.application.dca.DCASimulationService;
import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * DCA 시뮬레이션 웹 어댑터
 * HTTP 요청을 받아 애플리케이션 서비스를 호출하고 응답을 반환
 */
@RestController
@RequestMapping("/api/v1/dca")
@RequiredArgsConstructor
public class DCAController {

    private final DCASimulationService dcaSimulationService;

    /**
     * DCA 시뮬레이션 실행
     *
     * @param request DCA 시뮬레이션 요청
     * @return 시뮬레이션 결과
     */
    @PostMapping("/simulate")
    public ResponseEntity<DCASimulationResponse> simulate(@Valid @RequestBody DCASimulationRequest request) {
        try {
            // 웹 요청을 애플리케이션 커맨드로 변환
            DCASimulationCommand command = convertToCommand(request);

            // 애플리케이션 서비스 호출
            DCASimulationResponse response = dcaSimulationService.simulate(command);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("시뮬레이션 처리 중 오류가 발생했습니다"));
        }
    }

    /**
     * 웹 요청을 애플리케이션 커맨드로 변환
     */
    private DCASimulationCommand convertToCommand(DCASimulationRequest request) {
        LocalDateTime startDate = parseDateTime(request.getStartDate());
        LocalDateTime endDate = parseDateTime(request.getEndDate());

        return new DCASimulationCommand(
            request.getSymbol(),
            request.getMonthlyInvestmentAmount(),
            startDate,
            endDate,
            request.getFrequency()
        );
    }

    /**
     * 날짜 문자열을 LocalDateTime으로 변환
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            throw new IllegalArgumentException("올바르지 않은 날짜 형식입니다: " + dateTimeString);
        }
    }

    /**
     * 에러 응답 생성 (임시 구현)
     */
    private DCASimulationResponse createErrorResponse(String message) {
        // 실제로는 별도의 ErrorResponse DTO를 만들어야 하지만,
        // 현재 TDD 단계에서는 간단히 null 필드로 처리
        return new DCASimulationResponse(
            null, null, null, null, null, null, null, null, null, null, null
        );
    }

    /**
     * 예외 처리 핸들러
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.internalServerError()
            .body(Map.of("message", "시뮬레이션 처리 중 오류가 발생했습니다"));
    }
}