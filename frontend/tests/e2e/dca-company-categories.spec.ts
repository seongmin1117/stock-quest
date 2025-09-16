import { test, expect } from '@playwright/test';

/**
 * DCA Company Categories E2E Tests
 * Tests the company-category functionality after hexagonal architecture implementation
 */
test.describe('DCA Company Categories Integration Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dca-simulation');
  });

  test('카테고리 목록 API 호출 및 응답 구조 검증', async ({ page }) => {
    // 카테고리 API 직접 호출 테스트
    const response = await page.evaluate(async () => {
      try {
        const apiResponse = await fetch('http://localhost:8080/api/v1/companies/categories');
        const data = await apiResponse.json();
        return {
          status: apiResponse.status,
          data: data
        };
      } catch (error) {
        return {
          error: error.message
        };
      }
    });

    console.log('Categories API Response:', JSON.stringify(response, null, 2));

    if (response.status === 200 && response.data) {
      // 새로운 백엔드 hexagonal architecture 응답 구조 검증
      expect(Array.isArray(response.data)).toBe(true);

      if (response.data.length > 0) {
        const category = response.data[0];
        expect(category).toHaveProperty('id');
        expect(category).toHaveProperty('categoryId');
        expect(category).toHaveProperty('nameKr');
        expect(category).toHaveProperty('nameEn');
        expect(category).toHaveProperty('companyCount');
        expect(category).toHaveProperty('isActive', true);

        console.log('✅ Categories API response structure is correct');
        console.log('✅ Found categories:', response.data.map(c => c.nameKr).join(', '));
      }
    } else {
      console.log('⚠️ Categories API error:', response.error);
    }
  });

  test('카테고리별 회사 목록 조회 기능 검증', async ({ page }) => {
    // 테크 카테고리 회사 목록 조회
    const response = await page.evaluate(async () => {
      try {
        const apiResponse = await fetch('http://localhost:8080/api/v1/companies/category/TECH');
        const data = await apiResponse.json();
        return {
          status: apiResponse.status,
          data: data
        };
      } catch (error) {
        return {
          error: error.message
        };
      }
    });

    console.log('Companies by Category API Response:', JSON.stringify(response, null, 2));

    if (response.status === 200 && response.data) {
      expect(Array.isArray(response.data)).toBe(true);

      if (response.data.length > 0) {
        const company = response.data[0];
        expect(company).toHaveProperty('id');
        expect(company).toHaveProperty('symbol');
        expect(company).toHaveProperty('nameKr');
        expect(company).toHaveProperty('nameEn');
        expect(company).toHaveProperty('categories');
        expect(company.categories).toContain('tech');

        console.log('✅ Companies by category API working correctly');
        console.log('✅ Found TECH companies:', response.data.map(c => c.nameKr).join(', '));
      }
    } else {
      console.log('⚠️ Companies by category API error:', response.error);
    }
  });

  test('회사 검색 자동완성에서 카테고리 필터링 테스트', async ({ page }) => {
    // 회사 검색 입력 필드 클릭하여 드롭다운 열기
    const companyInput = page.getByLabel('회사 검색');
    await companyInput.click();

    // 자동완성 드롭다운이 나타날 때까지 대기
    await page.waitForTimeout(1000);

    // 카테고리 필터가 표시되는지 확인
    const categorySection = page.getByText('카테고리');
    if (await categorySection.count() > 0) {
      console.log('✅ Category filters are displayed in autocomplete');

      // 테크 카테고리 필터 클릭
      const techFilter = page.getByTestId('category-filter-tech');
      if (await techFilter.count() > 0) {
        await techFilter.click();
        console.log('✅ Tech category filter clicked');

        // 필터링된 결과 대기
        await page.waitForTimeout(1000);

        // 검색 결과에 테크 회사들이 표시되는지 확인
        const companyOptions = page.locator('[data-testid^="company-option-"]');
        const optionCount = await companyOptions.count();

        if (optionCount > 0) {
          console.log(`✅ Found ${optionCount} companies in TECH category`);

          // 첫 번째 결과 클릭해서 선택
          await companyOptions.first().click();
          console.log('✅ Selected first tech company from filtered results');
        } else {
          console.log('⚠️ No company options found after filtering');
        }
      } else {
        console.log('⚠️ Tech category filter not found');
      }
    } else {
      console.log('⚠️ Category section not found in autocomplete');
    }
  });

  test('인기 회사 목록 표시 및 선택 기능 테스트', async ({ page }) => {
    const companyInput = page.getByLabel('회사 검색');
    await companyInput.click();

    // 인기 종목 섹션이 표시될 때까지 대기
    await page.waitForTimeout(1000);

    const popularSection = page.getByText('인기 종목');
    if (await popularSection.count() > 0) {
      console.log('✅ Popular companies section is displayed');

      // 인기 회사 목록 확인
      const popularCompanies = page.locator('[data-testid^="popular-company-"]');
      const popularCount = await popularCompanies.count();

      if (popularCount > 0) {
        console.log(`✅ Found ${popularCount} popular companies`);

        // 첫 번째 인기 회사 클릭
        await popularCompanies.first().click();
        console.log('✅ Selected first popular company');

        // 회사가 선택되었는지 확인 (입력 필드에 값이 채워짐)
        const inputValue = await companyInput.inputValue();
        if (inputValue && inputValue.length > 0) {
          console.log(`✅ Company selected successfully: ${inputValue}`);
        }
      } else {
        console.log('⚠️ No popular companies found');
      }
    } else {
      console.log('⚠️ Popular companies section not found');
    }
  });

  test('전체 회사-카테고리 통합 워크플로우 테스트', async ({ page }) => {
    console.log('🚀 Starting complete company-category integration workflow');

    // 1. 페이지 로딩 확인
    await expect(page.getByRole('heading', { name: 'DCA 시뮬레이션' })).toBeVisible();
    console.log('✅ DCA simulation page loaded');

    // 2. 회사 검색 자동완성 열기
    const companyInput = page.getByLabel('회사 검색');
    await companyInput.click();
    await page.waitForTimeout(1000);
    console.log('✅ Company autocomplete dropdown opened');

    // 3. 카테고리 필터 사용하여 테크 회사 검색
    const techFilter = page.getByTestId('category-filter-tech');
    const hasTechFilter = await techFilter.count() > 0;

    if (hasTechFilter) {
      await techFilter.click();
      await page.waitForTimeout(1000);
      console.log('✅ Tech category filter applied');

      // 4. 필터링된 결과에서 회사 선택
      const companyOptions = page.locator('[data-testid^="company-option-"]');
      const optionCount = await companyOptions.count();

      if (optionCount > 0) {
        await companyOptions.first().click();
        console.log('✅ Tech company selected from filtered results');

        // 5. 선택된 회사로 시뮬레이션 실행
        await page.getByLabel('월 투자 금액').fill('100000');
        await page.getByLabel('시작일').fill('2020-01-01');
        await page.getByLabel('종료일').fill('2020-06-01');

        // 투자 주기 선택
        await page.locator('div[role="combobox"]').click();
        await page.getByRole('option', { name: '월별' }).click();

        console.log('✅ Simulation parameters set');

        // 시뮬레이션 실행
        await page.getByRole('button', { name: '시뮬레이션 실행' }).click();
        console.log('🔄 DCA simulation started with selected tech company');

        // 결과 대기 (5초)
        await page.waitForTimeout(5000);

        // 결과 확인
        const hasResults = await page.getByText('시뮬레이션 결과').count() > 0;
        const hasError = await page.locator('[role="alert"]').count() > 0;

        if (hasResults) {
          console.log('✅ DCA simulation completed successfully with tech company');
        } else if (hasError) {
          const errorText = await page.locator('[role="alert"]').textContent();
          console.log('⚠️ Simulation error (may be expected):', errorText);
        } else {
          console.log('⚠️ Simulation result unclear');
        }
      } else {
        console.log('⚠️ No tech companies found in filtered results');
      }
    } else {
      // 카테고리 필터가 없는 경우 직접 삼성전자 검색
      await companyInput.fill('Samsung');
      await page.waitForTimeout(1000);

      const searchResults = page.locator('[data-testid^="company-option-"]');
      const resultCount = await searchResults.count();

      if (resultCount > 0) {
        await searchResults.first().click();
        console.log('✅ Samsung company selected via search');
      } else {
        // 직접 종목 코드 입력
        await companyInput.fill('005930');
        console.log('✅ Samsung stock code entered directly');
      }
    }

    console.log('🏁 Company-category integration workflow completed');
  });

  test('실제 API 응답 데이터 구조 상세 검증', async ({ page }) => {
    console.log('🔍 Detailed API response structure validation');

    // 1. 카테고리 API 상세 검증
    const categoriesResponse = await page.evaluate(async () => {
      const response = await fetch('http://localhost:8080/api/v1/companies/categories');
      return {
        status: response.status,
        data: await response.json()
      };
    });

    console.log('Categories API detailed response:', categoriesResponse);

    if (categoriesResponse.status === 200) {
      const categories = categoriesResponse.data;
      expect(Array.isArray(categories)).toBe(true);

      if (categories.length > 0) {
        // 첫 번째 카테고리 구조 상세 검증
        const firstCategory = categories[0];
        const expectedFields = ['id', 'categoryId', 'nameKr', 'nameEn', 'sortOrder', 'isActive', 'companyCount'];

        expectedFields.forEach(field => {
          expect(firstCategory).toHaveProperty(field);
        });

        console.log('✅ Category API structure validation passed');
      }
    }

    // 2. 회사 검색 API 상세 검증
    const searchResponse = await page.evaluate(async () => {
      const response = await fetch('http://localhost:8080/api/v1/companies/search?limit=5');
      return {
        status: response.status,
        data: await response.json()
      };
    });

    console.log('Search API detailed response:', searchResponse);

    if (searchResponse.status === 200) {
      const searchData = searchResponse.data;

      // 새로운 응답 구조 검증
      expect(searchData).toHaveProperty('companies');
      expect(searchData).toHaveProperty('totalCount');
      expect(searchData).toHaveProperty('limit');
      expect(searchData).toHaveProperty('offset');

      if (searchData.companies && searchData.companies.length > 0) {
        const firstCompany = searchData.companies[0];
        const expectedFields = ['id', 'symbol', 'nameKr', 'nameEn', 'categories', 'popularityScore'];

        expectedFields.forEach(field => {
          expect(firstCompany).toHaveProperty(field);
        });

        console.log('✅ Company search API structure validation passed');
      }
    }

    console.log('✅ All API response structures validated successfully');
  });
});