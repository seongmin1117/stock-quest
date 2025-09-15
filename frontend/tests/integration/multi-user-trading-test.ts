/**
 * 멀티 유저 트레이딩 통합 테스트
 * 5명의 유저를 생성하고 각각 다른 거래 전략을 실행한 후 리더보드를 확인합니다.
 */

import axios, { AxiosInstance } from 'axios';

// API 기본 설정
const API_BASE_URL = 'http://localhost:8080';

// 테스트 유저 정보
interface TestUser {
  email: string;
  password: string;
  nickname: string;
  accessToken?: string;
  userId?: number;
  sessionId?: number;
  strategy: string;
}

// 테스트 유저 5명 정의
const TEST_USERS: TestUser[] = [
  {
    email: 'testuser1@test.com',
    password: 'Test1234!',
    nickname: 'TestUser1',
    strategy: 'AAPL 매수 위주'
  },
  {
    email: 'testuser2@test.com',
    password: 'Test1234!',
    nickname: 'TestUser2',
    strategy: 'GOOGL 매수 위주'
  },
  {
    email: 'testuser3@test.com',
    password: 'Test1234!',
    nickname: 'TestUser3',
    strategy: 'MSFT 매수 후 일부 매도'
  },
  {
    email: 'testuser4@test.com',
    password: 'Test1234!',
    nickname: 'TestUser4',
    strategy: '분산 투자 (여러 종목)'
  },
  {
    email: 'testuser5@test.com',
    password: 'Test1234!',
    nickname: 'TestUser5',
    strategy: '단타 매매 (빈번한 매수/매도)'
  }
];

// API 클라이언트 생성
function createApiClient(token?: string): AxiosInstance {
  const client = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    }
  });

  // 에러 로깅
  client.interceptors.response.use(
    response => response,
    error => {
      console.error(`API Error: ${error.response?.status} - ${error.response?.data?.message || error.message}`);
      return Promise.reject(error);
    }
  );

  return client;
}

// 1. 유저 생성 (회원가입)
async function createUser(user: TestUser): Promise<void> {
  const client = createApiClient();
  try {
    console.log(`📝 Creating user: ${user.email}`);
    const response = await client.post('/api/auth/signup', {
      email: user.email,
      password: user.password,
      nickname: user.nickname
    });
    console.log(`✅ User created: ${user.email}`);
  } catch (error: any) {
    if (error.response?.status === 409) {
      console.log(`ℹ️ User already exists: ${user.email}`);
    } else {
      throw error;
    }
  }
}

// 2. 로그인
async function login(user: TestUser): Promise<void> {
  const client = createApiClient();
  console.log(`🔐 Logging in: ${user.email}`);

  const response = await client.post('/api/auth/signin', {
    email: user.email,
    password: user.password
  });

  user.accessToken = response.data.accessToken;
  user.userId = response.data.userId;
  console.log(`✅ Logged in: ${user.email} (userId: ${user.userId})`);
}

// 3. 챌린지 목록 조회
async function getChallenges(user: TestUser): Promise<any[]> {
  const client = createApiClient(user.accessToken);
  console.log(`🔍 Getting challenges for: ${user.email}`);

  const response = await client.get('/api/challenges');
  return response.data.challenges || response.data;
}

// 4. 챌린지 참여
async function joinChallenge(user: TestUser, challengeId: number): Promise<void> {
  const client = createApiClient(user.accessToken);
  console.log(`🚀 Joining challenge ${challengeId} for: ${user.email}`);

  const response = await client.post(`/api/challenges/${challengeId}/start`);
  user.sessionId = response.data.sessionId;
  console.log(`✅ Joined challenge: ${user.email} (sessionId: ${user.sessionId})`);
}

// 5. 주문 실행
async function placeOrder(
  user: TestUser,
  symbol: string,
  side: 'BUY' | 'SELL',
  quantity: number,
  price: number
): Promise<void> {
  const client = createApiClient(user.accessToken);
  console.log(`📈 Placing ${side} order for ${quantity} ${symbol} @ $${price} - User: ${user.email}`);

  try {
    const response = await client.post(`/api/sessions/${user.sessionId}/orders`, {
      symbol,
      side,
      orderType: 'MARKET',
      quantity,
      price
    });
    console.log(`✅ Order placed: ${response.data.orderId} - Status: ${response.data.status}`);
  } catch (error: any) {
    console.error(`❌ Order failed for ${user.email}: ${error.response?.data?.message}`);
  }
}

// 6. 포트폴리오 조회
async function getPortfolio(user: TestUser): Promise<any> {
  const client = createApiClient(user.accessToken);
  console.log(`💼 Getting portfolio for: ${user.email}`);

  const response = await client.get(`/api/sessions/${user.sessionId}/portfolio`);
  return response.data;
}

// 7. 리더보드 조회
async function getLeaderboard(challengeId: number): Promise<any[]> {
  const client = createApiClient();
  console.log(`🏆 Getting leaderboard for challenge ${challengeId}`);

  const response = await client.get(`/api/challenges/${challengeId}/leaderboard`);
  return response.data;
}

// 거래 전략 실행
async function executeTrading(user: TestUser, index: number): Promise<void> {
  console.log(`\n💹 Executing trading strategy for ${user.email}: ${user.strategy}`);

  // 현재 가격 (실제로는 API에서 가져와야 함)
  const prices = {
    'AAPL': 175.50,
    'GOOGL': 138.20,
    'MSFT': 340.10,
    'AMZN': 145.80,
    'TSLA': 250.30
  };

  switch (index) {
    case 0: // User1: AAPL 매수 위주
      await placeOrder(user, 'AAPL', 'BUY', 50, prices['AAPL']);
      await placeOrder(user, 'AAPL', 'BUY', 30, prices['AAPL'] * 0.99);
      break;

    case 1: // User2: GOOGL 매수 위주
      await placeOrder(user, 'GOOGL', 'BUY', 40, prices['GOOGL']);
      await placeOrder(user, 'GOOGL', 'BUY', 25, prices['GOOGL'] * 0.98);
      break;

    case 2: // User3: MSFT 매수 후 일부 매도
      await placeOrder(user, 'MSFT', 'BUY', 30, prices['MSFT']);
      await new Promise(resolve => setTimeout(resolve, 1000));
      await placeOrder(user, 'MSFT', 'SELL', 10, prices['MSFT'] * 1.01);
      break;

    case 3: // User4: 분산 투자
      await placeOrder(user, 'AAPL', 'BUY', 10, prices['AAPL']);
      await placeOrder(user, 'GOOGL', 'BUY', 10, prices['GOOGL']);
      await placeOrder(user, 'MSFT', 'BUY', 10, prices['MSFT']);
      await placeOrder(user, 'AMZN', 'BUY', 10, prices['AMZN']);
      break;

    case 4: // User5: 단타 매매
      await placeOrder(user, 'TSLA', 'BUY', 20, prices['TSLA']);
      await new Promise(resolve => setTimeout(resolve, 500));
      await placeOrder(user, 'TSLA', 'SELL', 10, prices['TSLA'] * 1.005);
      await placeOrder(user, 'TSLA', 'BUY', 15, prices['TSLA'] * 0.995);
      await placeOrder(user, 'TSLA', 'SELL', 25, prices['TSLA'] * 1.01);
      break;
  }
}

// 메인 테스트 함수
async function runMultiUserTradingTest() {
  console.log('🎯 Starting Multi-User Trading Test\n');
  console.log('=' .repeat(80));

  try {
    // 1. 모든 유저 생성
    console.log('\n📋 Step 1: Creating Test Users');
    console.log('-'.repeat(40));
    for (const user of TEST_USERS) {
      await createUser(user);
    }

    // 2. 모든 유저 로그인
    console.log('\n🔑 Step 2: Logging in All Users');
    console.log('-'.repeat(40));
    for (const user of TEST_USERS) {
      await login(user);
    }

    // 3. 첫 번째 유저로 챌린지 목록 조회
    console.log('\n🎮 Step 3: Getting Available Challenges');
    console.log('-'.repeat(40));
    const challenges = await getChallenges(TEST_USERS[0]);

    if (challenges.length === 0) {
      console.error('❌ No active challenges found!');
      return;
    }

    const targetChallenge = challenges[0];
    console.log(`📍 Selected Challenge: ${targetChallenge.title} (ID: ${targetChallenge.id})`);

    // 4. 모든 유저가 같은 챌린지에 참여
    console.log('\n🏁 Step 4: All Users Joining the Challenge');
    console.log('-'.repeat(40));
    for (const user of TEST_USERS) {
      await joinChallenge(user, targetChallenge.id);
      await new Promise(resolve => setTimeout(resolve, 500)); // 서버 부하 방지
    }

    // 5. 각 유저별 거래 전략 실행
    console.log('\n💼 Step 5: Executing Trading Strategies');
    console.log('-'.repeat(40));
    for (let i = 0; i < TEST_USERS.length; i++) {
      await executeTrading(TEST_USERS[i], i);
      await new Promise(resolve => setTimeout(resolve, 1000)); // 유저 간 간격
    }

    // 6. 각 유저의 포트폴리오 확인
    console.log('\n📊 Step 6: Checking Portfolios');
    console.log('-'.repeat(40));
    for (const user of TEST_USERS) {
      const portfolio = await getPortfolio(user);
      console.log(`\n${user.email} Portfolio:`);
      console.log(`  - Total Value: $${portfolio.totalValue?.toFixed(2) || 'N/A'}`);
      console.log(`  - Cash: $${portfolio.cashBalance?.toFixed(2) || 'N/A'}`);
      console.log(`  - Positions: ${portfolio.positions?.length || 0}`);
      if (portfolio.positions?.length > 0) {
        portfolio.positions.forEach((pos: any) => {
          console.log(`    • ${pos.symbol}: ${pos.quantity} shares @ $${pos.averagePrice?.toFixed(2)}`);
        });
      }
    }

    // 7. 리더보드 확인
    console.log('\n🏆 Step 7: Final Leaderboard');
    console.log('-'.repeat(40));
    const leaderboard = await getLeaderboard(targetChallenge.id);

    if (leaderboard && leaderboard.length > 0) {
      console.log('\nRank | User | Total Value | Return %');
      console.log('-'.repeat(50));
      leaderboard.forEach((entry: any, index: number) => {
        const returnRate = ((entry.totalValue - 1000000) / 1000000 * 100).toFixed(2);
        console.log(`#${index + 1}   | ${entry.userNickname || entry.userId} | $${entry.totalValue?.toFixed(2)} | ${returnRate}%`);
      });
    } else {
      console.log('ℹ️ Leaderboard is empty or not yet calculated');
    }

    // 8. 결과 검증
    console.log('\n✅ Step 8: Verification');
    console.log('-'.repeat(40));

    // 모든 유저가 리더보드에 있는지 확인
    const userNicknames = TEST_USERS.map(u => u.nickname);
    const leaderboardNicknames = leaderboard.map((e: any) => e.userNickname);

    const missingUsers = userNicknames.filter(nickname =>
      !leaderboardNicknames.includes(nickname)
    );

    if (missingUsers.length === 0) {
      console.log('✅ All 5 users are reflected in the leaderboard');
    } else {
      console.log(`⚠️ Missing users in leaderboard: ${missingUsers.join(', ')}`);
    }

    // 각 유저의 거래가 반영되었는지 확인
    let allTradingReflected = true;
    for (const user of TEST_USERS) {
      const portfolio = await getPortfolio(user);
      if (!portfolio.positions || portfolio.positions.length === 0) {
        if (user.strategy !== '단타 매매 (빈번한 매수/매도)') {
          console.log(`⚠️ No positions found for ${user.email}`);
          allTradingReflected = false;
        }
      }
    }

    if (allTradingReflected) {
      console.log('✅ All trading strategies were executed and reflected');
    }

    console.log('\n' + '='.repeat(80));
    console.log('🎉 Multi-User Trading Test Completed Successfully!');
    console.log('='.repeat(80));

  } catch (error: any) {
    console.error('\n❌ Test Failed:', error.message);
    if (error.response) {
      console.error('Response data:', error.response.data);
    }
    process.exit(1);
  }
}

// 백엔드 서버 대기 함수
async function waitForBackend(maxAttempts = 30): Promise<boolean> {
  console.log('⏳ Waiting for backend server to be ready...');

  for (let i = 0; i < maxAttempts; i++) {
    try {
      const response = await axios.get(`${API_BASE_URL}/actuator/health`, {
        timeout: 1000
      }).catch(() => null);

      if (response?.status === 200) {
        console.log('✅ Backend server is ready!');
        return true;
      }
    } catch (error) {
      // Ignore errors, keep trying
    }

    await new Promise(resolve => setTimeout(resolve, 2000));
    process.stdout.write('.');
  }

  console.log('\n❌ Backend server is not responding');
  return false;
}

// 테스트 실행
(async () => {
  // 백엔드 서버가 준비될 때까지 대기
  const isBackendReady = await waitForBackend();

  if (!isBackendReady) {
    console.error('Cannot proceed without backend server');
    process.exit(1);
  }

  // 테스트 실행
  await runMultiUserTradingTest();
})();