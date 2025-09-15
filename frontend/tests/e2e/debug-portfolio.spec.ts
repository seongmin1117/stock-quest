import { test, expect } from '@playwright/test';

test('Debug portfolio page content', async ({ page }) => {
  console.log('🔍 Starting debug test...');

  await page.goto('/admin/portfolio-optimization', { waitUntil: 'networkidle' });

  console.log('📄 Page URL:', page.url());
  console.log('📖 Page title:', await page.title());

  // Get all text content from the page
  const pageContent = await page.textContent('body');
  console.log('📝 Page content preview:', pageContent?.substring(0, 500) + '...');

  // Get all headings
  const headings = await page.locator('h1, h2, h3, h4, h5, h6').allTextContents();
  console.log('📋 All headings found:', headings);

  // Check if the specific heading exists with different selectors
  const h4Elements = await page.locator('h4').allTextContents();
  console.log('📋 All h4 elements:', h4Elements);

  // Check for loading states
  const loadingElements = await page.locator('[role="progressbar"], .loading').count();
  console.log('⏳ Loading elements found:', loadingElements);

  // Check for error states
  const errorElements = await page.locator('[role="alert"], .error').count();
  console.log('❌ Error elements found:', errorElements);

  // Take screenshot
  await page.screenshot({ path: 'debug-portfolio-page.png', fullPage: true });
  console.log('📸 Screenshot saved as debug-portfolio-page.png');

  // Check if page has loaded completely
  const allImages = await page.locator('img').count();
  const allButtons = await page.locator('button').count();
  const allCards = await page.locator('.MuiCard-root, [role="button"]').count();

  console.log(`📊 Page elements: ${allImages} images, ${allButtons} buttons, ${allCards} cards`);

  // Check if React Query is working
  const reactQueryElements = await page.evaluate(() => {
    const html = document.body.innerHTML;
    return {
      hasReactQueryDevtools: html.includes('react-query'),
      hasApiCalls: html.includes('api/v1'),
      hasPortfolioData: html.includes('portfolio-optimization')
    };
  });

  console.log('🔍 React Query check:', reactQueryElements);
});