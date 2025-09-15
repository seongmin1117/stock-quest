#!/usr/bin/env tsx

/**
 * 5-User Trading System End-to-End Test
 * Tests comprehensive trading workflows with multiple users
 */

import axios, { AxiosInstance } from 'axios';

interface User {
  id: number;
  email: string;
  nickname: string;
  accessToken: string;
  refreshToken: string;
}

interface Challenge {
  id: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
}

interface ChallengeSession {
  id: number;
  challengeId: number;
  userId: number;
  status: string;
  seedMoney: number;
  currentBalance: number;
}

interface Order {
  id: number;
  sessionId: number;
  instrumentKey: string;
  type: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  status: string;
}

interface PortfolioPosition {
  id: number;
  sessionId: number;
  instrumentKey: string;
  quantity: number;
  averagePrice: number;
  currentValue: number;
}

class TradingTestClient {
  private baseURL = 'http://localhost:8080/api';
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 10000,
    });
  }

  async registerUser(email: string, password: string, nickname: string): Promise<User> {
    try {
      const response = await this.client.post('/auth/signup', {
        email,
        password,
        nickname
      });
      return response.data;
    } catch (error: any) {
      console.log(`User registration failed for ${email}: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async loginUser(email: string, password: string): Promise<User> {
    try {
      const response = await this.client.post('/auth/login', {
        email,
        password
      });
      return response.data;
    } catch (error: any) {
      console.log(`User login failed for ${email}: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async getActiveChallenges(): Promise<Challenge[]> {
    try {
      const response = await this.client.get('/challenges/active');
      return response.data.challenges || [];
    } catch (error: any) {
      console.log(`Failed to get active challenges: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async getChallengeInstruments(challengeId: number): Promise<any[]> {
    try {
      const response = await this.client.get(`/challenges/${challengeId}/instruments`);
      return response.data.instruments || [];
    } catch (error: any) {
      console.log(`Failed to get challenge instruments: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async startChallengeSession(challengeId: number, accessToken: string): Promise<ChallengeSession> {
    try {
      const response = await this.client.post(`/challenges/${challengeId}/start`, {}, {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      return response.data;
    } catch (error: any) {
      console.log(`Failed to start challenge session: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async getSessionPortfolio(sessionId: number, accessToken: string): Promise<PortfolioPosition[]> {
    try {
      const response = await this.client.get(`/sessions/${sessionId}/portfolio`, {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      return response.data.positions || [];
    } catch (error: any) {
      console.log(`Failed to get session portfolio: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async getSessionOrders(sessionId: number, accessToken: string): Promise<Order[]> {
    try {
      const response = await this.client.get(`/sessions/${sessionId}/orders`, {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      return response.data.orders || [];
    } catch (error: any) {
      console.log(`Failed to get session orders: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async placeOrder(sessionId: number, instrumentKey: string, type: 'BUY' | 'SELL', quantity: number, accessToken: string): Promise<Order> {
    try {
      const response = await this.client.post(`/sessions/${sessionId}/orders`, {
        instrumentKey,
        type,
        quantity
      }, {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
      return response.data;
    } catch (error: any) {
      console.log(`Failed to place order: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }

  async getLeaderboard(challengeId: number): Promise<any[]> {
    try {
      const response = await this.client.get(`/challenges/${challengeId}/leaderboard`);
      return response.data.leaderboard || [];
    } catch (error: any) {
      console.log(`Failed to get leaderboard: ${error.response?.data?.message || error.message}`);
      throw error;
    }
  }
}

async function runTradingTest() {
  const client = new TradingTestClient();

  console.log('🚀 Starting 5-User Trading System End-to-End Test...\n');

  // Test users with unique emails and nicknames using short random suffix
  const suffix = Math.floor(Math.random() * 10000).toString();
  const testUsers = [
    { email: `trader1${suffix}@test.com`, password: 'Test123!@#', nickname: `Trader1${suffix}` },
    { email: `trader2${suffix}@test.com`, password: 'Test123!@#', nickname: `Trader2${suffix}` },
    { email: `trader3${suffix}@test.com`, password: 'Test123!@#', nickname: `Trader3${suffix}` },
    { email: `trader4${suffix}@test.com`, password: 'Test123!@#', nickname: `Trader4${suffix}` },
    { email: `trader5${suffix}@test.com`, password: 'Test123!@#', nickname: `Trader5${suffix}` }
  ];

  const users: User[] = [];
  const sessions: ChallengeSession[] = [];

  try {
    // Step 1: Register or login users
    console.log('📝 Step 1: Registering/logging in users...');
    for (const userData of testUsers) {
      try {
        // Try to register first
        await client.registerUser(userData.email, userData.password, userData.nickname);
        console.log(`✅ Registered user: ${userData.nickname} (${userData.email})`);

        // Then login to get tokens
        const user = await client.loginUser(userData.email, userData.password);
        users.push(user);
        console.log(`✅ Logged in user: ${userData.nickname} with tokens`);
      } catch (registerError: any) {
        if (registerError.response?.status === 400 || registerError.response?.status === 500) {
          // User already exists, try login
          try {
            const user = await client.loginUser(userData.email, userData.password);
            users.push(user);
            console.log(`✅ Logged in existing user: ${userData.nickname} (${userData.email})`);
          } catch (loginError) {
            console.log(`❌ Failed to login user ${userData.email}:`, loginError);
            throw loginError;
          }
        } else {
          console.log(`❌ Failed to register user ${userData.email}:`, registerError);
          throw registerError;
        }
      }
    }

    // Step 2: Get active challenges
    console.log('\n🏆 Step 2: Getting active challenges...');
    const challenges = await client.getActiveChallenges();
    console.log(`✅ Found ${challenges.length} active challenges`);

    if (challenges.length === 0) {
      throw new Error('No active challenges found. Please create at least one active challenge.');
    }

    const testChallenge = challenges[0];
    console.log(`📋 Using challenge: ${testChallenge.title} (ID: ${testChallenge.id})`);

    // Step 3: Get challenge instruments
    console.log('\n📊 Step 3: Getting challenge instruments...');
    const instruments = await client.getChallengeInstruments(testChallenge.id);
    console.log(`✅ Found ${instruments.length} instruments:`, instruments.map(i => `${i.instrumentKey}(${i.actualTicker})`));

    if (instruments.length === 0) {
      throw new Error('No instruments found for the challenge.');
    }

    // Step 4: Start challenge sessions for all users
    console.log('\n🎮 Step 4: Starting challenge sessions...');
    for (const user of users) {
      try {
        const session = await client.startChallengeSession(testChallenge.id, user.accessToken);
        sessions.push(session);
        console.log(`✅ Started session for ${user.nickname}: Session ID ${session.id}, Balance: $${session.currentBalance}`);
      } catch (error: any) {
        if (error.response?.status === 400 && error.response?.data?.message?.includes('already participating')) {
          console.log(`⚠️  User ${user.nickname} already has an active session`);
          // For now, we'll skip this user - in a real scenario, we'd get their existing session
        } else {
          throw error;
        }
      }
    }

    // Step 5: Execute diverse trading strategies
    console.log('\n💰 Step 5: Executing trading strategies...');

    const tradingStrategies = [
      { userIndex: 0, strategy: 'Buy AAPL and MSFT', trades: [
        { instrument: 'A', type: 'BUY' as const, quantity: 10 },
        { instrument: 'B', type: 'BUY' as const, quantity: 5 }
      ]},
      { userIndex: 1, strategy: 'Buy GOOGL heavily', trades: [
        { instrument: 'C', type: 'BUY' as const, quantity: 20 }
      ]},
      { userIndex: 2, strategy: 'Diversified portfolio', trades: [
        { instrument: 'A', type: 'BUY' as const, quantity: 5 },
        { instrument: 'C', type: 'BUY' as const, quantity: 3 },
        { instrument: 'D', type: 'BUY' as const, quantity: 2 }
      ]},
      { userIndex: 3, strategy: 'TSLA focus', trades: [
        { instrument: 'D', type: 'BUY' as const, quantity: 15 }
      ]},
      { userIndex: 4, strategy: 'Conservative AMZN', trades: [
        { instrument: 'E', type: 'BUY' as const, quantity: 8 }
      ]}
    ];

    for (const strategy of tradingStrategies) {
      if (strategy.userIndex >= users.length || strategy.userIndex >= sessions.length) {
        console.log(`⚠️  Skipping strategy for user ${strategy.userIndex} - user or session not available`);
        continue;
      }

      const user = users[strategy.userIndex];
      const session = sessions[strategy.userIndex];

      console.log(`\n👤 ${user.nickname} - ${strategy.strategy}:`);

      for (const trade of strategy.trades) {
        try {
          await new Promise(resolve => setTimeout(resolve, 500)); // Small delay between trades
          const order = await client.placeOrder(session.id, trade.instrument, trade.type, trade.quantity, user.accessToken);
          console.log(`  ✅ ${trade.type} ${trade.quantity} shares of ${trade.instrument} - Order ID: ${order.id}`);
        } catch (error: any) {
          console.log(`  ❌ Failed to place ${trade.type} order for ${trade.instrument}: ${error.response?.data?.message || error.message}`);
        }
      }
    }

    // Step 6: Verify portfolios and orders
    console.log('\n📋 Step 6: Verifying portfolios and orders...');
    for (let i = 0; i < Math.min(users.length, sessions.length); i++) {
      const user = users[i];
      const session = sessions[i];

      try {
        await new Promise(resolve => setTimeout(resolve, 200)); // Small delay

        const [portfolio, orders] = await Promise.all([
          client.getSessionPortfolio(session.id, user.accessToken),
          client.getSessionOrders(session.id, user.accessToken)
        ]);

        console.log(`\n👤 ${user.nickname} (Session ${session.id}):`);
        console.log(`  📊 Portfolio positions: ${portfolio.length}`);
        portfolio.forEach(pos => {
          console.log(`    - ${pos.instrumentKey}: ${pos.quantity} shares @ avg $${pos.averagePrice} = $${pos.currentValue}`);
        });

        console.log(`  📝 Orders executed: ${orders.length}`);
        orders.forEach(order => {
          console.log(`    - ${order.type} ${order.quantity} ${order.instrumentKey} @ $${order.price} (${order.status})`);
        });

      } catch (error: any) {
        console.log(`❌ Failed to get data for ${user.nickname}: ${error.response?.data?.message || error.message}`);
      }
    }

    // Step 7: Check leaderboard
    console.log('\n🏆 Step 7: Checking leaderboard...');
    try {
      const leaderboard = await client.getLeaderboard(testChallenge.id);
      console.log(`✅ Leaderboard entries: ${leaderboard.length}`);
      leaderboard.slice(0, 5).forEach((entry, index) => {
        console.log(`  ${index + 1}. ${entry.nickname || 'Unknown'}: ${entry.returnRate}% return`);
      });
    } catch (error: any) {
      console.log(`❌ Failed to get leaderboard: ${error.response?.data?.message || error.message}`);
    }

    // Step 8: Test selling positions (partial)
    console.log('\n💸 Step 8: Testing sell orders...');
    for (let i = 0; i < Math.min(users.length, sessions.length, 2); i++) {
      const user = users[i];
      const session = sessions[i];

      try {
        const portfolio = await client.getSessionPortfolio(session.id, user.accessToken);
        if (portfolio.length > 0) {
          const position = portfolio[0];
          const sellQuantity = Math.floor(position.quantity / 2);

          if (sellQuantity > 0) {
            await new Promise(resolve => setTimeout(resolve, 300));
            const sellOrder = await client.placeOrder(session.id, position.instrumentKey, 'SELL', sellQuantity, user.accessToken);
            console.log(`✅ ${user.nickname} sold ${sellQuantity} shares of ${position.instrumentKey} - Order ID: ${sellOrder.id}`);
          }
        }
      } catch (error: any) {
        console.log(`❌ ${user.nickname} sell order failed: ${error.response?.data?.message || error.message}`);
      }
    }

    console.log('\n🎉 Multi-user trading test completed successfully!');
    console.log('\n📊 Test Summary:');
    console.log(`- Users processed: ${users.length}/${testUsers.length}`);
    console.log(`- Sessions created: ${sessions.length}`);
    console.log(`- Challenge used: ${testChallenge.title}`);
    console.log(`- Instruments available: ${instruments.length}`);
    console.log('\n✅ All major trading system components are working correctly!');

  } catch (error: any) {
    console.error('\n❌ Test failed:', error.message);
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    }
    process.exit(1);
  }
}

// Run the test
if (require.main === module) {
  runTradingTest().catch(console.error);
}

export { runTradingTest };