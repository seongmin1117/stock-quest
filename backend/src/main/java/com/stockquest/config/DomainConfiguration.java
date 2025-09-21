package com.stockquest.config;

import com.stockquest.domain.simulation.DCASimulationService;
import com.stockquest.domain.simulation.port.BenchmarkDataRepository;
import com.stockquest.domain.simulation.port.PriceDataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 도메인 서비스를 위한 Spring 설정
 * 헥사고날 아키텍처: 도메인과 프레임워크를 분리하면서 필요한 Bean들을 관리
 */
@Configuration
public class DomainConfiguration {

    /**
     * DCA 시뮬레이션 도메인 서비스 Bean 등록
     * 도메인 서비스 자체는 Spring에 의존하지 않으나, 애플리케이션에서 사용하기 위해 Bean으로 등록
     */
    @Bean
    public DCASimulationService dcaSimulationService(PriceDataRepository priceDataRepository,
                                                   BenchmarkDataRepository benchmarkDataRepository) {
        return new DCASimulationService(priceDataRepository, benchmarkDataRepository);
    }
}