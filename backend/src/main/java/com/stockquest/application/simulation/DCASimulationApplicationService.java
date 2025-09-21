package com.stockquest.application.simulation;

import com.stockquest.domain.simulation.DCASimulationService;
import com.stockquest.domain.simulation.DCASimulationParameters;
import com.stockquest.domain.simulation.DCASimulationResult;
import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import com.stockquest.domain.simulation.port.PriceDataRepository;
import org.springframework.stereotype.Service;

/**
 * DCA 시뮬레이션 애플리케이션 서비스
 * 헥사고날 아키텍처: 도메인 서비스를 Spring에서 관리하기 위한 어댑터
 */
@Service
public class DCASimulationApplicationService {

    private final DCASimulationService dcaSimulationService;

    public DCASimulationApplicationService(PriceDataRepository priceDataRepository,
                                         BenchmarkDataRepository benchmarkDataRepository) {
        // 도메인 서비스를 애플리케이션 레이어에서 생성하여 Spring 의존성 분리
        this.dcaSimulationService = new DCASimulationService(priceDataRepository, benchmarkDataRepository);
    }

    /**
     * DCA 시뮬레이션 실행
     *
     * @param parameters 시뮬레이션 파라미터
     * @return 시뮬레이션 결과
     */
    public DCASimulationResult simulate(DCASimulationParameters parameters) {
        return dcaSimulationService.simulate(parameters);
    }
}