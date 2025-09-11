import { test, expect } from '@playwright/test';

/**
 * StockQuest 챌린지 전체 플로우 E2E 테스트
 * 시나리오: 회원가입 → 로그인 → 챌린지 시작 → 주문 → 종료 → 티커 공개
 */
test.describe('StockQuest 챌린지 플로우', () => {
  
  test('전체 챌린지 플로우 - 회원가입부터 결과 확인까지', async ({ page }) => {
    // 1. 메인 페이지 접속
    await page.goto('/');
    
    await expect(page).toHaveTitle(/StockQuest/);
    await expect(page.getByText('StockQuest')).toBeVisible();
    await expect(page.getByText('과거 시장 데이터로 배우는 모의 투자 챌린지')).toBeVisible();

    // 2. 회원가입 페이지로 이동
    await page.getByRole('link', { name: '회원가입' }).click();
    await expect(page).toHaveURL('/auth/signup');

    // 회원가입 폼 작성 및 제출
    const timestamp = Date.now();
    await page.fill('[name="email"]', `test${timestamp}@example.com`);
    await page.fill('[name="password"]', 'password123');
    await page.fill('[name="nickname"]', `테스터${timestamp}`);
    
    await page.getByRole('button', { name: '회원가입' }).click();

    // 회원가입 성공 후 챌린지 페이지로 리다이렉트 확인
    await expect(page).toHaveURL('/challenges');

    // 3. 챌린지 목록 확인
    await expect(page.getByText('투자 챌린지')).toBeVisible();
    await expect(page.getByText('2020년 코로나 급락장 챌린지')).toBeVisible();

    // 4. 첫 번째 챌린지 시작
    await page.getByRole('button', { name: '챌린지 시작' }).first().click();
    
    // 챌린지 세션 페이지로 이동 확인
    await expect(page.getByText('투자 챌린지 진행중')).toBeVisible();
    await expect(page.getByText('주문 접수')).toBeVisible();
    await expect(page.getByText('포트폴리오')).toBeVisible();
    await expect(page.getByText('리더보드')).toBeVisible();

    // 5. 시장 데이터 확인 (회사명이 숨겨져 있는지 확인)
    await expect(page.getByText('회사 A')).toBeVisible();
    await expect(page.getByText('회사 B')).toBeVisible();
    await expect(page.getByText('회사 C')).toBeVisible();
    
    // 실제 회사명이 보이지 않는지 확인
    await expect(page.getByText('Apple')).not.toBeVisible();
    await expect(page.getByText('Microsoft')).not.toBeVisible();

    // 6. 첫 번째 주문 (매수)
    await page.getByRole('button', { name: '매수' }).click();
    
    // 상품 선택
    await page.click('[aria-labelledby="상품 선택"]');
    await page.getByRole('option', { name: '회사 A (STOCK)' }).click();
    
    // 수량 입력
    await page.fill('[name="quantity"]', '10');
    
    // 시장가 주문 선택 (기본값)
    await page.getByRole('button', { name: '시장가' }).click();
    
    // 주문 접수
    await page.getByRole('button', { name: '주문 접수' }).click();
    
    // 주문 성공 메시지 확인
    await expect(page.getByText(/매수 주문이 체결되었습니다/)).toBeVisible();

    // 7. 포트폴리오에 포지션 추가 확인
    await expect(page.getByText('회사 A')).toBeVisible();
    await expect(page.getByText('10')).toBeVisible(); // 수량 확인

    // 8. 두 번째 주문 (다른 종목 매수)
    await page.getByRole('button', { name: '매수' }).click();
    
    await page.click('[aria-labelledby="상품 선택"]');
    await page.getByRole('option', { name: '회사 B (STOCK)' }).click();
    
    await page.fill('[name="quantity"]', '5');
    await page.getByRole('button', { name: '주문 접수' }).click();
    
    await expect(page.getByText(/매수 주문이 체결되었습니다/)).toBeVisible();

    // 9. 일부 매도 주문
    await page.getByRole('button', { name: '매도' }).click();
    
    await page.click('[aria-labelledby="상품 선택"]');
    await page.getByRole('option', { name: '회사 A (STOCK)' }).click();
    
    await page.fill('[name="quantity"]', '5');  // 절반 매도
    await page.getByRole('button', { name: '주문 접수' }).click();
    
    await expect(page.getByText(/매도 주문이 체결되었습니다/)).toBeVisible();

    // 10. 잠깐 대기 (시뮬레이션 시간 경과)
    await page.waitForTimeout(3000);

    // 11. 챌린지 종료
    await page.getByRole('button', { name: '챌린지 종료하기' }).click();
    
    // 결과 페이지로 이동 확인
    await expect(page.getByText(/챌린지 결과/)).toBeVisible();

    // 12. 티커 공개 확인
    await expect(page.getByText('Apple Inc.')).toBeVisible();
    await expect(page.getByText('Microsoft Corporation')).toBeVisible();
    await expect(page.getByText('AAPL')).toBeVisible();
    await expect(page.getByText('MSFT')).toBeVisible();

    // 13. 최종 손익 확인
    await expect(page.getByText(/최종 수익률/)).toBeVisible();
    await expect(page.getByText(/총 손익/)).toBeVisible();
    await expect(page.getByText(/순위/)).toBeVisible();
  });

  test('로그인 플로우 테스트', async ({ page }) => {
    // 로그인 페이지 접속
    await page.goto('/auth/login');
    
    await expect(page.getByText('로그인')).toBeVisible();
    
    // 테스트 계정으로 로그인
    await page.fill('[name="email"]', 'test@example.com');
    await page.fill('[name="password"]', 'password123');
    
    await page.getByRole('button', { name: '로그인' }).click();
    
    // 챌린지 페이지로 리다이렉트 확인
    await expect(page).toHaveURL('/challenges');
    await expect(page.getByText('투자 챌린지')).toBeVisible();
  });

  test('잘못된 로그인 정보로 로그인 실패 테스트', async ({ page }) => {
    await page.goto('/auth/login');
    
    // 잘못된 정보 입력
    await page.fill('[name="email"]', 'wrong@example.com');
    await page.fill('[name="password"]', 'wrongpassword');
    
    await page.getByRole('button', { name: '로그인' }).click();
    
    // 에러 메시지 확인
    await expect(page.getByText(/이메일 또는 비밀번호가 일치하지 않습니다/)).toBeVisible();
  });

  test('포트폴리오 실시간 업데이트 테스트', async ({ page }) => {
    // 로그인
    await page.goto('/auth/login');
    await page.fill('[name="email"]', 'test@example.com');
    await page.fill('[name="password"]', 'password123');
    await page.getByRole('button', { name: '로그인' }).click();
    
    // 챌린지 시작
    await page.getByRole('button', { name: '챌린지 시작' }).first().click();
    
    // 주문 실행
    await page.getByRole('button', { name: '매수' }).click();
    await page.click('[aria-labelledby="상품 선택"]');
    await page.getByRole('option', { name: '회사 A (STOCK)' }).click();
    await page.fill('[name="quantity"]', '10');
    await page.getByRole('button', { name: '주문 접수' }).click();
    
    // 포트폴리오 업데이트 확인
    await expect(page.getByText('회사 A')).toBeVisible();
    
    // 실시간 업데이트 대기 (몇 초 후 가격 변동 확인)
    await page.waitForTimeout(6000);
    
    // 포트폴리오 값이 업데이트되었는지 확인 (정확한 값은 체크하지 않고 존재만 확인)
    await expect(page.getByText(/₩/)).toBeVisible();
  });

  test('리더보드 표시 테스트', async ({ page }) => {
    // 로그인
    await page.goto('/auth/login');
    await page.fill('[name="email"]', 'test@example.com');
    await page.fill('[name="password"]', 'password123');
    await page.getByRole('button', { name: '로그인' }).click();
    
    // 챌린지 시작
    await page.getByRole('button', { name: '챌린지 시작' }).first().click();
    
    // 리더보드 확인
    await expect(page.getByText('리더보드')).toBeVisible();
    await expect(page.getByText(/순위/)).toBeVisible();
    
    // 모킹된 리더보드 데이터 확인
    await expect(page.getByText('투자고수')).toBeVisible();
    await expect(page.getByText('주식왕')).toBeVisible();
  });
});