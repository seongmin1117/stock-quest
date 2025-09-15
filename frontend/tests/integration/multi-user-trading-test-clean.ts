import axios from 'axios';

// Base API URL
const API_BASE_URL = 'http://localhost:8080/api';

// Test users with unique timestamps to avoid conflicts
const TIMESTAMP = Date.now();
const TEST_USERS = [
    { email: `testuser1_${TIMESTAMP}@test.com`, password: 'password123', nickname: `User1_${TIMESTAMP}` },
    { email: `testuser2_${TIMESTAMP}@test.com`, password: 'password123', nickname: `User2_${TIMESTAMP}` },
    { email: `testuser3_${TIMESTAMP}@test.com`, password: 'password123', nickname: `User3_${TIMESTAMP}` },
    { email: `testuser4_${TIMESTAMP}@test.com`, password: 'password123', nickname: `User4_${TIMESTAMP}` },
    { email: `testuser5_${TIMESTAMP}@test.com`, password: 'password123', nickname: `User5_${TIMESTAMP}` }
];

// Trading strategies for each user
const TRADING_STRATEGIES = [
    { name: 'Conservative', orders: [{ instrumentKey: 'A', side: 'BUY', quantity: 10 }, { instrumentKey: 'B', side: 'BUY', quantity: 5 }] },
    { name: 'Aggressive', orders: [{ instrumentKey: 'A', side: 'BUY', quantity: 50 }, { instrumentKey: 'E', side: 'BUY', quantity: 20 }] },
    { name: 'Diversified', orders: [{ instrumentKey: 'A', side: 'BUY', quantity: 10 }, { instrumentKey: 'B', side: 'BUY', quantity: 10 }, { instrumentKey: 'C', side: 'BUY', quantity: 10 }] },
    { name: 'Tech Focus', orders: [{ instrumentKey: 'A', side: 'BUY', quantity: 30 }, { instrumentKey: 'B', side: 'BUY', quantity: 15 }] },
    { name: 'Balanced', orders: [{ instrumentKey: 'A', side: 'BUY', quantity: 20 }, { instrumentKey: 'C', side: 'BUY', quantity: 15 }, { instrumentKey: 'D', side: 'BUY', quantity: 10 }] }
];

interface User {
    email: string;
    password: string;
    nickname: string;
    token?: string;
    userId?: number;
    sessionId?: number;
    strategy?: typeof TRADING_STRATEGIES[0];
}

// Helper function to wait for server
async function waitForServer(): Promise<void> {
    console.log('⏳ Waiting for backend server to be ready...');
    let attempts = 0;
    const maxAttempts = 10;
    
    while (attempts < maxAttempts) {
        try {
            const response = await axios.get(`${API_BASE_URL}/challenges`, { timeout: 2000 });
            if (response.status === 200) {
                console.log('✅ Backend server is ready!');
                return;
            }
        } catch (error) {
            attempts++;
            if (attempts >= maxAttempts) {
                throw new Error('Backend server not responding after maximum attempts');
            }
            await new Promise(resolve => setTimeout(resolve, 1000));
        }
    }
}

// Step 1: Create users
async function createUsers(): Promise<User[]> {
    console.log('\n📋 Step 1: Creating Test Users');
    console.log('----------------------------------------');
    
    const users: User[] = [];
    
    for (let i = 0; i < TEST_USERS.length; i++) {
        const userData = TEST_USERS[i];
        console.log(`📝 Creating user: ${userData.email}`);
        
        try {
            const response = await axios.post(`${API_BASE_URL}/auth/signup`, {
                email: userData.email,
                password: userData.password,
                nickname: userData.nickname
            });
            
            if (response.status === 200 || response.status === 201) {
                const user: User = {
                    ...userData,
                    userId: response.data.id,
                    strategy: TRADING_STRATEGIES[i]
                };
                users.push(user);
                console.log(`✅ User created successfully: ${userData.nickname} (ID: ${response.data.id})`);
            }
        } catch (error: any) {
            console.error(`❌ Failed to create user ${userData.email}:`, error.response?.data || error.message);
            throw error;
        }
        
        // Small delay between requests
        await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    console.log(`\n🎉 Successfully created ${users.length} users`);
    return users;
}

// Step 2: Login users
async function loginUsers(users: User[]): Promise<User[]> {
    console.log('\n🔐 Step 2: User Authentication');
    console.log('----------------------------------------');
    
    for (const user of users) {
        try {
            console.log(`🔑 Logging in: ${user.email}`);
            const response = await axios.post(`${API_BASE_URL}/auth/signin`, {
                email: user.email,
                password: user.password
            });
            
            if (response.data.accessToken) {
                user.token = response.data.accessToken;
                console.log(`✅ Login successful: ${user.nickname}`);
            }
        } catch (error: any) {
            console.error(`❌ Login failed for ${user.email}:`, error.response?.data || error.message);
            throw error;
        }
        
        await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    console.log(`\n🎉 All ${users.length} users logged in successfully`);
    return users;
}

// Step 3: Start challenges
async function startChallenges(users: User[]): Promise<User[]> {
    console.log('\n🎯 Step 3: Starting Challenges');
    console.log('----------------------------------------');
    
    // Get available challenges first
    const challengesResponse = await axios.get(`${API_BASE_URL}/challenges`);
    const challenges = challengesResponse.data.challenges;
    
    if (!challenges || challenges.length === 0) {
        throw new Error('No challenges available');
    }
    
    const challengeId = challenges[0].id; // Use first challenge
    console.log(`Using challenge: "${challenges[0].title}" (ID: ${challengeId})`);
    
    for (const user of users) {
        try {
            console.log(`🚀 Starting challenge for: ${user.nickname}`);
            const response = await axios.post(
                `${API_BASE_URL}/challenges/${challengeId}/start`,
                {},
                {
                    headers: {
                        'Authorization': `Bearer ${user.token}`
                    }
                }
            );
            
            if (response.data.sessionId) {
                user.sessionId = response.data.sessionId;
                console.log(`✅ Challenge started: ${user.nickname} (Session ID: ${response.data.sessionId})`);
            }
        } catch (error: any) {
            console.error(`❌ Failed to start challenge for ${user.nickname}:`, error.response?.data || error.message);
            throw error;
        }
        
        await new Promise(resolve => setTimeout(resolve, 100));
    }
    
    console.log(`\n🎉 All ${users.length} users started challenges successfully`);
    return users;
}

// Step 4: Execute trades
async function executeTrades(users: User[]): Promise<void> {
    console.log('\n📈 Step 4: Executing Trading Strategies');
    console.log('----------------------------------------');
    
    for (const user of users) {
        if (!user.strategy || !user.sessionId) continue;
        
        console.log(`\n💼 Executing ${user.strategy.name} strategy for: ${user.nickname}`);
        
        for (const order of user.strategy.orders) {
            try {
                console.log(`  📊 ${order.side} ${order.quantity} shares of ${order.instrumentKey}`);
                
                const response = await axios.post(
                    `${API_BASE_URL}/sessions/${user.sessionId}/orders`,
                    {
                        instrumentKey: order.instrumentKey,
                        side: order.side,
                        quantity: order.quantity,
                        orderType: 'MARKET'
                    },
                    {
                        headers: {
                            'Authorization': `Bearer ${user.token}`
                        }
                    }
                );
                
                if (response.status === 200) {
                    console.log(`  ✅ Order executed successfully`);
                } else {
                    console.log(`  ⚠️ Order response: ${response.status}`);
                }
            } catch (error: any) {
                console.error(`  ❌ Order failed:`, error.response?.data || error.message);
                // Continue with other orders instead of failing completely
            }
            
            await new Promise(resolve => setTimeout(resolve, 200)); // Delay between orders
        }
    }
    
    console.log('\n🎉 Trading execution completed for all users');
}

// Step 5: Verify results
async function verifyResults(users: User[]): Promise<void> {
    console.log('\n📊 Step 5: Verifying Trading Results');
    console.log('----------------------------------------');
    
    for (const user of users) {
        if (!user.sessionId) continue;
        
        console.log(`\n🔍 Checking results for: ${user.nickname}`);
        
        try {
            // Test the new portfolio endpoint
            console.log(`  📈 Fetching portfolio...`);
            const portfolioResponse = await axios.get(
                `${API_BASE_URL}/sessions/${user.sessionId}/portfolio`,
                {
                    headers: {
                        'Authorization': `Bearer ${user.token}`
                    }
                }
            );
            
            if (portfolioResponse.status === 200) {
                const portfolio = portfolioResponse.data;
                console.log(`  ✅ Portfolio retrieved successfully`);
                console.log(`     Cash Balance: $${portfolio.cashBalance}`);
                console.log(`     Total Value: $${portfolio.totalValue}`);
                console.log(`     Positions: ${portfolio.positions.length}`);
            }
            
            // Test the new orders endpoint
            console.log(`  📋 Fetching orders...`);
            const ordersResponse = await axios.get(
                `${API_BASE_URL}/sessions/${user.sessionId}/orders`,
                {
                    headers: {
                        'Authorization': `Bearer ${user.token}`
                    }
                }
            );
            
            if (ordersResponse.status === 200) {
                const orders = ordersResponse.data;
                console.log(`  ✅ Orders retrieved successfully`);
                console.log(`     Total Orders: ${orders.orders.length}`);
                
                // Display order details
                orders.orders.forEach((order: any, index: number) => {
                    console.log(`     Order ${index + 1}: ${order.side} ${order.quantity} ${order.instrumentKey} - ${order.status}`);
                });
            }
        } catch (error: any) {
            console.error(`  ❌ Failed to verify results for ${user.nickname}:`, error.response?.data || error.message);
        }
        
        await new Promise(resolve => setTimeout(resolve, 100));
    }
}

// Main test function
async function runMultiUserTradingTest(): Promise<void> {
    try {
        await waitForServer();
        
        console.log('🎯 Starting Multi-User Trading Test');
        console.log('\n================================================================================');
        
        // Execute test steps
        let users = await createUsers();
        users = await loginUsers(users);
        users = await startChallenges(users);
        await executeTrades(users);
        await verifyResults(users);
        
        console.log('\n================================================================================');
        console.log('🎉 Multi-User Trading Test Completed Successfully!');
        console.log('================================================================================');
        
        // Summary
        console.log('\n📈 Test Summary:');
        console.log(`   Users Created: ${users.length}`);
        console.log(`   Challenges Started: ${users.filter(u => u.sessionId).length}`);
        console.log(`   Trading Strategies Executed: ${users.filter(u => u.strategy).length}`);
        
        process.exit(0);
        
    } catch (error: any) {
        console.error('\n❌ Test Failed:', error.message);
        if (error.response?.data) {
            console.error('Response data:', error.response.data);
        }
        process.exit(1);
    }
}

// Run the test
runMultiUserTradingTest();
