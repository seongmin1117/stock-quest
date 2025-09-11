import { setupWorker } from 'msw/browser';
import handlers from './handlers';

/**
 * 브라우저용 MSW 워커 설정
 * 개발 환경에서 API 모킹에 사용
 */
export const worker = setupWorker(...handlers);

// 개발 환경에서만 MSW 활성화
if (process.env.NODE_ENV === 'development' && process.env.NEXT_PUBLIC_MOCK_API === 'true') {
  worker.start({
    onUnhandledRequest: 'warn',
  });
}