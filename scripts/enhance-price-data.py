#!/usr/bin/env python3
"""
주가 데이터 보강 스크립트

DCA 시뮬레이션을 위한 한국 주요 종목 과거 데이터 생성
- 실제 시장 패턴을 반영한 시뮬레이션 데이터
- 다양한 투자 주기 테스트를 위한 충분한 기간 커버
- 시장 사이클(상승, 하락, 횡보) 반영
"""

import mysql.connector
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import random
import math
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class PriceDataEnhancer:
    def __init__(self):
        self.db_config = {
            'host': '127.0.0.1',
            'port': 3306,
            'user': 'root',
            'password': 'rootpassword',
            'database': 'stockquest',
            'charset': 'utf8mb4'
        }

        # 한국 주요 종목 리스트 (DCA 시뮬레이션에 필요한 종목들)
        # 기존 005930 데이터를 기준으로 비슷한 패턴의 데이터 생성
        self.korean_stocks = {
            '005930': {'name': '삼성전자', 'base_price': 61600, 'volatility': 0.25, 'priority': 1},
            '000660': {'name': 'SK하이닉스', 'base_price': 91400, 'volatility': 0.35, 'priority': 2},
            '035720': {'name': '카카오', 'base_price': 124200, 'volatility': 0.40, 'priority': 3},
            '005380': {'name': '현대차', 'base_price': 180000, 'volatility': 0.30, 'priority': 4},
            '006400': {'name': '삼성SDI', 'base_price': 450000, 'volatility': 0.45, 'priority': 5},
            '035420': {'name': 'NAVER', 'base_price': 200000, 'volatility': 0.35, 'priority': 6},
            '051910': {'name': 'LG화학', 'base_price': 350000, 'volatility': 0.40, 'priority': 7},
            '068270': {'name': '셀트리온', 'base_price': 180000, 'volatility': 0.50, 'priority': 8},
            '028260': {'name': '삼성물산', 'base_price': 120000, 'volatility': 0.30, 'priority': 9},
            '066570': {'name': 'LG전자', 'base_price': 85000, 'volatility': 0.30, 'priority': 10}
        }

        # 글로벌 ETF 및 인덱스 (벤치마크용)
        self.global_etfs = {
            'SPY': {'name': 'S&P 500 ETF', 'base_price': 320, 'volatility': 0.20},
            'QQQ': {'name': 'NASDAQ 100 ETF', 'base_price': 280, 'volatility': 0.25},
            'VTI': {'name': '미국 전체 시장 ETF', 'base_price': 180, 'volatility': 0.18},
            'KODEX200': {'name': 'KODEX 코스피200', 'base_price': 35000, 'volatility': 0.22}
        }

    def connect_db(self):
        """데이터베이스 연결"""
        try:
            conn = mysql.connector.connect(**self.db_config)
            logger.info("데이터베이스 연결 성공")
            return conn
        except Exception as e:
            logger.error(f"데이터베이스 연결 실패: {e}")
            raise

    def generate_market_scenario(self, days, scenario_type='mixed'):
        """
        시장 시나리오별 수익률 패턴 생성

        Args:
            days: 생성할 일수
            scenario_type: 'bull'(상승), 'bear'(하락), 'sideways'(횡보), 'mixed'(혼합)
        """
        if scenario_type == 'bull':
            # 상승장: 전체적으로 상승 추세
            trend = np.linspace(0, 0.8, days)  # 80% 상승
            daily_returns = np.random.normal(0.001, 0.02, days) + trend / days

        elif scenario_type == 'bear':
            # 하락장: 전체적으로 하락 추세
            trend = np.linspace(0, -0.4, days)  # 40% 하락
            daily_returns = np.random.normal(-0.001, 0.025, days) + trend / days

        elif scenario_type == 'sideways':
            # 횡보장: 변동성은 있지만 전체적으로 평평
            daily_returns = np.random.normal(0, 0.015, days)

        else:  # mixed
            # 혼합: 다양한 시장 사이클 포함
            bull_period = days // 3
            bear_period = days // 4
            sideways_period = days - bull_period - bear_period

            bull_returns = np.random.normal(0.002, 0.02, bull_period)
            bear_returns = np.random.normal(-0.003, 0.03, bear_period)
            sideways_returns = np.random.normal(0, 0.015, sideways_period)

            daily_returns = np.concatenate([bull_returns, bear_returns, sideways_returns])
            np.random.shuffle(daily_returns)

        return daily_returns

    def generate_realistic_price_series(self, ticker, start_date, end_date, base_price, volatility):
        """
        현실적인 주가 시계열 데이터 생성

        Args:
            ticker: 종목 코드
            start_date: 시작일
            end_date: 종료일
            base_price: 기준 가격
            volatility: 변동성 (연율)
        """
        logger.info(f"{ticker} 데이터 생성 시작: {start_date} ~ {end_date}")

        # 날짜 범위 생성 (주말 제외)
        date_range = pd.bdate_range(start=start_date, end=end_date)
        days = len(date_range)

        # 시장 시나리오 결정 (기간에 따라)
        if days > 500:  # 2년 이상
            scenario = 'mixed'
        elif days > 250:  # 1년 이상
            scenario = random.choice(['bull', 'bear', 'mixed'])
        else:  # 1년 미만
            scenario = random.choice(['bull', 'bear', 'sideways'])

        # 일별 수익률 생성
        daily_returns = self.generate_market_scenario(days, scenario)

        # 변동성 조정
        daily_returns *= volatility

        # 특별 이벤트 시뮬레이션 (큰 상승/하락)
        event_probability = 0.05  # 5% 확률로 특별 이벤트
        for i in range(days):
            if random.random() < event_probability:
                event_magnitude = random.choice([-0.08, -0.05, 0.05, 0.08])  # ±5%, ±8%
                daily_returns[i] += event_magnitude

        # 가격 시계열 생성
        prices = [base_price]
        for return_rate in daily_returns:
            new_price = prices[-1] * (1 + return_rate)
            prices.append(max(new_price, base_price * 0.1))  # 최소 10% 가격 유지

        prices = prices[1:]  # 첫 번째 중복 제거

        # OHLC 데이터 생성
        data = []
        for i, (date, close_price) in enumerate(zip(date_range, prices)):
            # 일중 변동성 시뮬레이션
            intraday_volatility = 0.02  # 2% 일중 변동성
            high_factor = 1 + abs(np.random.normal(0, intraday_volatility))
            low_factor = 1 - abs(np.random.normal(0, intraday_volatility))

            # 이전 종가를 시가로 사용 (갭 상승/하락 시뮬레이션)
            if i == 0:
                open_price = close_price * random.uniform(0.99, 1.01)
            else:
                gap_factor = random.uniform(0.995, 1.005)  # ±0.5% 갭
                open_price = prices[i-1] * gap_factor

            high_price = max(open_price, close_price) * high_factor
            low_price = min(open_price, close_price) * low_factor

            # 거래량 시뮬레이션 (가격 변동성과 상관관계)
            price_change = abs(daily_returns[i])
            base_volume = random.randint(100000, 500000)
            volume_multiplier = 1 + price_change * 10  # 변동성이 클수록 거래량 증가
            volume = int(base_volume * volume_multiplier)

            data.append({
                'ticker': ticker,
                'date': date.date(),
                'open_price': round(open_price, 2),
                'high_price': round(high_price, 2),
                'low_price': round(low_price, 2),
                'close_price': round(close_price, 2),
                'volume': volume,
                'timeframe': 'DAILY'
            })

        logger.info(f"{ticker} 데이터 생성 완료: {len(data)}개 레코드")
        return data

    def check_existing_data_count(self, ticker):
        """기존 데이터 개수 확인"""
        conn = self.connect_db()
        cursor = conn.cursor()

        try:
            cursor.execute("SELECT COUNT(*) FROM price_candle WHERE ticker = %s", (ticker,))
            count = cursor.fetchone()[0]
            return count
        except Exception as e:
            logger.error(f"{ticker} 데이터 개수 확인 실패: {e}")
            return 0
        finally:
            cursor.close()
            conn.close()

    def clear_existing_data(self, ticker):
        """기존 데이터 삭제"""
        conn = self.connect_db()
        cursor = conn.cursor()

        try:
            cursor.execute("DELETE FROM price_candle WHERE ticker = %s", (ticker,))
            deleted_count = cursor.rowcount
            conn.commit()
            logger.info(f"{ticker} 기존 데이터 {deleted_count}개 삭제")
        except Exception as e:
            logger.error(f"{ticker} 데이터 삭제 실패: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()

    def insert_price_data(self, data):
        """주가 데이터 삽입"""
        if not data:
            logger.warning("삽입할 데이터가 없습니다")
            return

        conn = self.connect_db()
        cursor = conn.cursor()

        insert_query = """
        INSERT INTO price_candle (ticker, date, open_price, high_price, low_price, close_price, volume, timeframe)
        VALUES (%(ticker)s, %(date)s, %(open_price)s, %(high_price)s, %(low_price)s, %(close_price)s, %(volume)s, %(timeframe)s)
        """

        try:
            cursor.executemany(insert_query, data)
            conn.commit()
            logger.info(f"{data[0]['ticker']} 데이터 {len(data)}개 삽입 완료")
        except Exception as e:
            logger.error(f"데이터 삽입 실패: {e}")
            conn.rollback()
        finally:
            cursor.close()
            conn.close()

    def enhance_korean_stocks(self, start_date='2019-01-02', end_date='2024-12-31'):
        """한국 주요 종목 데이터 보강 - 우선순위에 따른 단계별 처리"""
        logger.info("한국 주요 종목 데이터 보강 시작")

        # 우선순위에 따라 정렬
        sorted_stocks = sorted(self.korean_stocks.items(), key=lambda x: x[1]['priority'])

        for ticker, info in sorted_stocks:
            logger.info(f"종목 처리 중 (우선순위 {info['priority']}): {ticker} - {info['name']}")

            try:
                # 기존 데이터가 충분한지 확인
                existing_count = self.check_existing_data_count(ticker)
                if existing_count >= 100:  # 충분한 데이터가 있으면 스킵
                    logger.info(f"{ticker} 기존 데이터 충분 ({existing_count}개) - 스킵")
                    continue

                # 기존 데이터 삭제 (부족한 경우만)
                if existing_count > 0:
                    self.clear_existing_data(ticker)

                # 새 데이터 생성 - 더 넓은 기간으로
                price_data = self.generate_realistic_price_series(
                    ticker=ticker,
                    start_date=start_date,
                    end_date=end_date,
                    base_price=info['base_price'],
                    volatility=info['volatility']
                )

                # 데이터 삽입
                self.insert_price_data(price_data)

                logger.info(f"{ticker} 완료 - {len(price_data)}개 레코드 생성")

            except Exception as e:
                logger.error(f"{ticker} 처리 중 오류 발생: {e}")
                continue

    def enhance_global_etfs(self, start_date='2018-01-01', end_date='2024-12-31'):
        """글로벌 ETF 데이터 보강"""
        logger.info("글로벌 ETF 데이터 보강 시작")

        for ticker, info in self.global_etfs.items():
            logger.info(f"ETF 처리 중: {ticker} - {info['name']}")

            # 기존 데이터 삭제
            self.clear_existing_data(ticker)

            # 새 데이터 생성
            price_data = self.generate_realistic_price_series(
                ticker=ticker,
                start_date=start_date,
                end_date=end_date,
                base_price=info['base_price'],
                volatility=info['volatility']
            )

            # 데이터 삽입
            self.insert_price_data(price_data)

            logger.info(f"{ticker} 완료")

    def verify_data_quality(self):
        """데이터 품질 검증"""
        logger.info("데이터 품질 검증 시작")

        conn = self.connect_db()
        cursor = conn.cursor()

        try:
            # 각 종목별 데이터 통계
            cursor.execute("""
                SELECT
                    ticker,
                    COUNT(*) as record_count,
                    MIN(date) as start_date,
                    MAX(date) as end_date,
                    AVG(close_price) as avg_price,
                    STDDEV(close_price) as price_std,
                    MIN(close_price) as min_price,
                    MAX(close_price) as max_price
                FROM price_candle
                GROUP BY ticker
                ORDER BY ticker
            """)

            results = cursor.fetchall()

            logger.info("=== 데이터 품질 검증 결과 ===")
            for row in results:
                ticker, count, start, end, avg_price, std, min_price, max_price = row
                logger.info(f"{ticker}: {count}개 레코드, {start}~{end}")
                logger.info(f"  평균가: {avg_price:,.0f}, 표준편차: {std:,.0f}")
                logger.info(f"  최저가: {min_price:,.0f}, 최고가: {max_price:,.0f}")
                logger.info("")

            # 데이터 무결성 검증
            cursor.execute("""
                SELECT ticker, COUNT(*) as invalid_count
                FROM price_candle
                WHERE low_price > high_price
                   OR close_price > high_price
                   OR close_price < low_price
                   OR open_price > high_price
                   OR open_price < low_price
                GROUP BY ticker
            """)

            invalid_data = cursor.fetchall()
            if invalid_data:
                logger.error("데이터 무결성 오류 발견:")
                for ticker, count in invalid_data:
                    logger.error(f"  {ticker}: {count}개 잘못된 레코드")
            else:
                logger.info("✅ 모든 데이터 무결성 검증 통과")

        except Exception as e:
            logger.error(f"데이터 품질 검증 실패: {e}")
        finally:
            cursor.close()
            conn.close()

    def run_korean_stocks_only(self):
        """한국 종목만 우선 보강 실행"""
        logger.info("=== 한국 주요 종목 데이터 보강 시작 ===")

        try:
            # 한국 주요 종목 보강 (우선순위 기반)
            self.enhance_korean_stocks()

            # 한국 종목 데이터 품질 검증
            self.verify_korean_data_quality()

            logger.info("=== 한국 주요 종목 데이터 보강 완료 ===")

        except Exception as e:
            logger.error(f"한국 종목 데이터 보강 중 오류 발생: {e}")
            raise

    def verify_korean_data_quality(self):
        """한국 종목 데이터 품질 검증"""
        logger.info("한국 종목 데이터 품질 검증 시작")

        conn = self.connect_db()
        cursor = conn.cursor()

        try:
            # 한국 종목별 데이터 통계
            korean_tickers = "', '".join(self.korean_stocks.keys())
            cursor.execute(f"""
                SELECT
                    ticker,
                    COUNT(*) as record_count,
                    MIN(date) as start_date,
                    MAX(date) as end_date,
                    AVG(close_price) as avg_price,
                    MIN(close_price) as min_price,
                    MAX(close_price) as max_price
                FROM price_candle
                WHERE ticker IN ('{korean_tickers}')
                GROUP BY ticker
                ORDER BY ticker
            """)

            results = cursor.fetchall()

            logger.info("=== 한국 종목 데이터 검증 결과 ===")
            for row in results:
                ticker, count, start, end, avg_price, min_price, max_price = row
                stock_name = self.korean_stocks.get(ticker, {}).get('name', 'Unknown')
                status = "✅ 충분" if count >= 100 else "❌ 부족"
                logger.info(f"{ticker} ({stock_name}): {status} - {count}개, {start}~{end}")
                logger.info(f"  가격범위: {min_price:,.0f} ~ {max_price:,.0f} (평균: {avg_price:,.0f})")

            # 데이터 무결성 검증
            cursor.execute(f"""
                SELECT ticker, COUNT(*) as invalid_count
                FROM price_candle
                WHERE ticker IN ('{korean_tickers}')
                  AND (low_price > high_price
                    OR close_price > high_price
                    OR close_price < low_price
                    OR open_price > high_price
                    OR open_price < low_price)
                GROUP BY ticker
            """)

            invalid_data = cursor.fetchall()
            if invalid_data:
                logger.error("데이터 무결성 오류 발견:")
                for ticker, count in invalid_data:
                    logger.error(f"  {ticker}: {count}개 잘못된 레코드")
            else:
                logger.info("✅ 모든 한국 종목 데이터 무결성 검증 통과")

        except Exception as e:
            logger.error(f"한국 종목 데이터 품질 검증 실패: {e}")
        finally:
            cursor.close()
            conn.close()

    def run_full_enhancement(self):
        """전체 데이터 보강 실행"""
        logger.info("=== 주가 데이터 전체 보강 시작 ===")

        try:
            # 한국 주요 종목 보강
            self.enhance_korean_stocks()

            # 글로벌 ETF 보강
            self.enhance_global_etfs()

            # 데이터 품질 검증
            self.verify_data_quality()

            logger.info("=== 주가 데이터 보강 완료 ===")

        except Exception as e:
            logger.error(f"데이터 보강 중 오류 발생: {e}")
            raise

def main():
    """메인 실행 함수"""
    enhancer = PriceDataEnhancer()

    # 한국 주요 종목만 우선 처리
    enhancer.run_korean_stocks_only()

    # 전체 보강이 필요한 경우 아래 주석 해제
    # enhancer.run_full_enhancement()

if __name__ == "__main__":
    main()