package com.stockquest.adapter.in.web.dca;

import com.stockquest.application.dca.DCASimulationService;
import com.stockquest.application.dca.dto.DCASimulationCommand;
import com.stockquest.application.dca.dto.DCASimulationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * DCA (Dollar Cost Averaging) 시뮬레이션 웹 어댑터
 *
 * <p>정액 투자법 시뮬레이션 API를 제공합니다.</p>
 *
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>과거 주가 데이터 기반 DCA 시뮬레이션 실행</li>
 *   <li>투자 수익률 및 포트폴리오 분석</li>
 *   <li>벤치마크(S&P 500, NASDAQ) 대비 성과 비교</li>
 *   <li>월별 투자 기록 및 누적 성과 추적</li>
 * </ul>
 *
 * <h3>지원 투자 주기:</h3>
 * <ul>
 *   <li>DAILY: 매일 투자</li>
 *   <li>WEEKLY: 매주 투자</li>
 *   <li>MONTHLY: 매월 투자</li>
 * </ul>
 *
 * <h3>응답 형식:</h3>
 * <ul>
 *   <li>성공: 200 OK + {@link DCASimulationResponse}</li>
 *   <li>검증 오류: 400 Bad Request + {@link DCASimulationErrorResponse}</li>
 *   <li>서버 오류: 500 Internal Server Error + {@link DCASimulationErrorResponse}</li>
 * </ul>
 *
 * @author StockQuest Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/v1/dca")
@RequiredArgsConstructor
public class DCAController {

    private final DCASimulationService dcaSimulationService;

    /**
     * DCA 시뮬레이션 실행
     *
     * <p>지정된 종목과 기간에 대해 정액 투자법(Dollar Cost Averaging) 시뮬레이션을 수행합니다.</p>
     *
     * <h4>요청 예시:</h4>
     * <pre>
     * POST /api/v1/dca/simulate
     * Content-Type: application/json
     *
     * {
     *   "symbol": "005930",
     *   "monthlyInvestmentAmount": 100000,
     *   "startDate": "2020-01-02T00:00:00",
     *   "endDate": "2020-06-01T00:00:00",
     *   "frequency": "MONTHLY"
     * }
     * </pre>
     *
     * <h4>성공 응답 예시 (200 OK):</h4>
     * <pre>
     * {
     *   "symbol": "005930",
     *   "totalInvestmentAmount": 500000,
     *   "finalPortfolioValue": 572680.0000,
     *   "totalReturnPercentage": 14.54,
     *   "annualizedReturn": 38.86,
     *   "investmentRecords": [
     *     {
     *       "investmentDate": "2020-01-02T00:00:00",
     *       "investmentAmount": 100000,
     *       "stockPrice": 60200.00,
     *       "sharesPurchased": 1.66,
     *       "portfolioValue": 99932.0000
     *     }
     *   ],
     *   "sp500ReturnAmount": 500000,
     *   "nasdaqReturnAmount": 500000,
     *   "outperformanceVsSP500": 14.54,
     *   "outperformanceVsNASDAQ": 14.54,
     *   "maxPortfolioValue": 494400.0000
     * }
     * </pre>
     *
     * <h4>오류 응답 예시 (400 Bad Request):</h4>
     * <pre>
     * {
     *   "errorCode": "VALIDATION_ERROR",
     *   "message": "종목 코드는 필수입니다. 예: '005930' (삼성전자)",
     *   "details": null,
     *   "timestamp": "2025-09-19 18:20:16",
     *   "path": "/api/v1/dca/simulate"
     * }
     * </pre>
     *
     * <h4>지원 종목:</h4>
     * <ul>
     *   <li>005930: 삼성전자 (2020-01-02 ~ 2020-06-01 데이터 보유)</li>
     *   <li>AAPL, GOOGL, MSFT: 미국 주식 (제한적 데이터)</li>
     * </ul>
     *
     * <h4>날짜 형식:</h4>
     * <ul>
     *   <li>ISO 8601 형식 지원: "2020-01-01T00:00:00"</li>
     *   <li>날짜만 지원: "2020-01-01" (자동으로 00:00:00 시간 추가)</li>
     *   <li>타임존 포함: "2020-01-01T00:00:00.000Z"</li>
     * </ul>
     *
     * @param request DCA 시뮬레이션 요청 파라미터
     * @return 성공시 {@link DCASimulationResponse}, 오류시 {@link DCASimulationErrorResponse}
     *
     * @apiNote 시뮬레이션은 과거 데이터 기반으로 수행되며, 실제 투자 결과와 다를 수 있습니다.
     * @throws IllegalArgumentException 잘못된 요청 파라미터 (400 Bad Request)
     * @throws RuntimeException 서버 내부 오류 (500 Internal Server Error)
     */
    @PostMapping("/simulate")
    public ResponseEntity<?> simulate(@RequestBody DCASimulationRequest request) {
        System.err.println("=== DCA Controller simulate method called ===");
        System.err.println("Request: " + request);
        try {
            System.err.println("Converting to command...");
            // 웹 요청을 애플리케이션 커맨드로 변환
            DCASimulationCommand command = convertToCommand(request);
            System.err.println("Command created successfully: " + command);

            // 애플리케이션 서비스 호출
            DCASimulationResponse response = dcaSimulationService.simulate(command);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.err.println("DCA Validation Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(createValidationErrorResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("DCA General Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(createInternalErrorResponse("시뮬레이션 처리 중 오류가 발생했습니다", e.getMessage()));
        }
    }

    /**
     * DCA 컨트롤러 상태 확인 엔드포인트
     *
     * <p>DCA 시뮬레이션 서비스가 정상적으로 작동하는지 확인하기 위한 헬스체크 엔드포인트입니다.</p>
     *
     * <h4>요청 예시:</h4>
     * <pre>
     * GET /api/v1/dca/test
     * </pre>
     *
     * <h4>응답 예시 (200 OK):</h4>
     * <pre>
     * "DCA Controller is working!"
     * </pre>
     *
     * @return DCA 컨트롤러 상태 메시지
     * @apiNote 개발 및 디버깅 용도로 사용됩니다.
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        System.err.println("=== DCA Test endpoint called ===");
        return ResponseEntity.ok("DCA Controller is working!");
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
     * 다양한 ISO 8601 형식을 지원 (타임존 포함/미포함)
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            throw new IllegalArgumentException("날짜가 비어있습니다");
        }

        try {
            // 1. ISO 8601 with timezone (Z suffix) - 예: 2020-01-01T00:00:00.000Z
            if (dateTimeString.endsWith("Z")) {
                return Instant.parse(dateTimeString).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }

            // 2. ISO 8601 with offset - 예: 2020-01-01T00:00:00+09:00
            if (dateTimeString.contains("+") || dateTimeString.lastIndexOf("-") > 10) {
                return OffsetDateTime.parse(dateTimeString).toLocalDateTime();
            }

            // 3. ISO Local DateTime - 예: 2020-01-01T00:00:00
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        } catch (DateTimeParseException e) {
            // 4. 날짜만 있는 경우 - 예: 2020-01-01
            try {
                return LocalDateTime.parse(dateTimeString + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException(
                    String.format("지원하지 않는 날짜 형식입니다: %s. 지원 형식: yyyy-MM-ddTHH:mm:ss 또는 yyyy-MM-ddTHH:mm:ss.SSSZ",
                                  dateTimeString));
            }
        }
    }

    /**
     * 검증 오류 응답 생성
     */
    private DCASimulationErrorResponse createValidationErrorResponse(String message) {
        return new DCASimulationErrorResponse(
            "VALIDATION_ERROR",
            message,
            "/api/v1/dca/simulate"
        );
    }

    /**
     * 내부 서버 오류 응답 생성
     */
    private DCASimulationErrorResponse createInternalErrorResponse(String message, String details) {
        return new DCASimulationErrorResponse(
            "INTERNAL_ERROR",
            message,
            details,
            "/api/v1/dca/simulate"
        );
    }

    /**
     * 예외 처리 핸들러
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<DCASimulationErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(createValidationErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DCASimulationErrorResponse> handleGenericException(Exception e) {
        return ResponseEntity.internalServerError()
            .body(createInternalErrorResponse("시뮬레이션 처리 중 오류가 발생했습니다", e.getMessage()));
    }
}