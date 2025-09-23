# CLAUDE.md - Stock Quest Frontend Guide for Claude Code

Frontend-specific guidance for the Stock Quest trading simulation platform.

## 🎯 Frontend Overview

**Stock Quest Frontend** is a modern React application delivering real-time trading simulation with sophisticated UI/UX design.

- **Architecture**: Feature-Sliced Design for scalable component organization
- **Technology Stack**: Next.js 14 + React 18 + TypeScript 5.5 + Material-UI
- **Real-time Features**: TanStack Query + WebSocket integration
- **Status**: Core features complete, mobile-responsive design

## 🚀 Quick Commands

### Essential Development Commands
```bash
# Development workflow
npm run dev              # Start dev server (localhost:3000)
npm run build            # Production build
npm run type-check       # TypeScript validation

# API Integration
npm run generate-api     # Generate TypeScript client from OpenAPI spec

# Quality checks
npm test                 # Run test suite
npm run lint             # ESLint validation
```

## 🏗️ Feature-Sliced Design Architecture

### Core Principles
- **Feature-Based**: Organize by business features, not technical layers
- **Scalable Structure**: Clear separation between features, entities, and shared code
- **Import Rules**: Strict import conventions prevent circular dependencies

### Directory Structure
```
src/
├── app/                           # 🚀 Next.js 14 App Router
│   ├── (auth)/                   # Authentication routes
│   ├── challenges/               # Challenge-related pages
│   ├── dashboard/                # User dashboard
│   └── trading/                  # Trading interface
├── features/                      # 💼 Business Features
│   ├── challenge-management/     # Challenge CRUD operations
│   ├── portfolio-management/     # Portfolio tracking
│   ├── order-execution/          # Trading order system
│   └── real-time-data/          # WebSocket data handling
├── entities/                      # 🏢 Business Entities
│   ├── challenge/                # Challenge domain models
│   ├── portfolio/                # Portfolio entities
│   ├── user/                     # User domain
│   └── company/                  # Company data models
├── widgets/                       # 🧩 Complex UI Compositions
│   ├── portfolio/                # Portfolio dashboard widgets
│   ├── market-data/              # Market data panels
│   └── ml-signals/               # ML trading signals display
└── shared/                        # 🔧 Shared Resources
    ├── api/                      # API client & generated types
    ├── ui/                       # Reusable UI components
    ├── hooks/                    # Custom React hooks
    └── utils/                    # Utility functions
```

## ⚡ Technology Stack

### Frontend Technologies
- **Framework**: Next.js 14 with App Router + React 18
- **Language**: TypeScript 5.5 with strict type checking
- **UI Library**: Material-UI (MUI) + Heroicons for icons
- **State Management**: TanStack Query for server state + React hooks
- **Real-time**: WebSocket integration with auto-reconnection
- **API Client**: Auto-generated from OpenAPI spec using Orval
- **Styling**: CSS-in-JS with MUI's emotion + responsive design

### Performance Features
- **Code Splitting**: Automatic route-based splitting
- **Optimization**: Next.js built-in optimizations (Image, Bundle Analyzer)
- **Caching**: TanStack Query intelligent caching with stale-while-revalidate
- **Real-time Updates**: Efficient WebSocket connection management
- **Mobile-First**: Responsive design with touch optimization

## 🎨 Component Architecture

### UI Component Strategy
```
shared/ui/
├── form/                 # Form controls (Input, Select, Button)
├── layout/              # Layout components (Container, Grid, Stack)
├── feedback/            # User feedback (Alert, Loading, Toast)
├── navigation/          # Navigation components (Menu, Breadcrumb)
└── data-display/        # Data visualization (Table, Chart, Card)
```

### Widget Composition Pattern
```
widgets/portfolio/
├── PortfolioPanel.tsx           # Main portfolio display
├── PositionTable.tsx            # Holdings table
├── PerformanceChart.tsx         # P&L visualization
└── index.ts                     # Clean exports
```

## 🔌 API Integration

### Auto-Generated Client
```bash
# Generate TypeScript client from backend OpenAPI spec
npm run generate-api

# Generated files (in .gitignore)
src/shared/api/generated/
├── model/               # TypeScript interfaces
├── challenge-client/    # Challenge API hooks
├── portfolio-client/    # Portfolio API hooks
└── company-client/      # Company data hooks
```

### TanStack Query Integration
```typescript
// Example: Using generated API client
import { useGetChallenges } from '@/shared/api/challenge-client';

function ChallengeList() {
  const { data: challenges, isLoading, error } = useGetChallenges({
    query: {
      refetchInterval: 30000, // Auto-refresh every 30 seconds
      staleTime: 60000,       // Consider fresh for 1 minute
    }
  });

  if (isLoading) return <Loading />;
  if (error) return <ErrorDisplay error={error} />;

  return <ChallengeGrid challenges={challenges} />;
}
```

### WebSocket Real-time Integration
```typescript
// Real-time portfolio updates
import { useWebSocket } from '@/shared/hooks/useWebSocket';

function PortfolioPanel({ sessionId }: { sessionId: number }) {
  const { data: portfolioUpdate } = useWebSocket(
    `/topic/portfolio/${sessionId}`,
    { enabled: !!sessionId }
  );

  // Component automatically updates when WebSocket data arrives
  return <PortfolioDisplay data={portfolioUpdate} />;
}
```

## 🌟 Feature Implementation Patterns

### Challenge Management Feature
```
features/challenge-management/
├── api/                 # Challenge-specific API calls
├── components/          # Feature-specific components
│   ├── ChallengeCard.tsx
│   ├── ChallengeForm.tsx
│   └── ChallengeFilters.tsx
├── hooks/              # Feature-specific hooks
│   └── useChallengeState.ts
└── types/              # Feature-specific types
    └── challenge.types.ts
```

### Real-time Data Feature
```
features/real-time-data/
├── hooks/
│   ├── useMarketData.ts        # Real-time market prices
│   ├── usePortfolioUpdates.ts  # Portfolio changes
│   └── useOrderExecution.ts    # Order status updates
├── components/
│   ├── PriceDisplay.tsx        # Live price component
│   └── ConnectionStatus.tsx    # WebSocket status indicator
└── utils/
    └── websocket.utils.ts      # WebSocket helper functions
```

## 📱 Mobile-First Design

### Responsive Breakpoints
```typescript
// MUI breakpoints configuration
const theme = createTheme({
  breakpoints: {
    values: {
      xs: 0,      // Mobile portrait
      sm: 600,    // Mobile landscape
      md: 900,    // Tablet
      lg: 1200,   // Desktop
      xl: 1536,   // Large desktop
    },
  },
});
```

### Touch-Optimized Trading Interface
- **Order Entry**: Large touch targets for buy/sell buttons
- **Portfolio Management**: Swipe gestures for quick actions
- **Chart Interaction**: Pinch-to-zoom and pan navigation
- **Navigation**: Bottom tab bar for mobile accessibility

## 🔧 Development Workflow

### Component Development Process
1. **Design Review**: Ensure component follows MUI design system
2. **TypeScript Types**: Define strict interfaces for props and state
3. **Implementation**: Build with accessibility and performance in mind
4. **Testing**: Unit tests for logic, visual tests for UI
5. **Integration**: Verify API integration and real-time updates

### Code Quality Standards
```typescript
// Example: Well-structured component
interface PortfolioPanelProps {
  sessionId: number;
  refreshInterval?: number;
}

export function PortfolioPanel({
  sessionId,
  refreshInterval = 5000
}: PortfolioPanelProps) {
  // Custom hook for data fetching
  const { data, isLoading, error } = usePortfolioData(sessionId, {
    refetchInterval: refreshInterval,
  });

  // Early returns for loading states
  if (isLoading) return <PortfolioSkeleton />;
  if (error) return <ErrorBoundary error={error} />;

  // Main component render
  return (
    <Paper sx={{ p: 3 }}>
      <PortfolioSummary data={data.summary} />
      <PositionTable positions={data.positions} />
    </Paper>
  );
}
```

### Pre-Commit Checklist
```bash
# 1. TypeScript validation
npm run type-check

# 2. Build verification
npm run build

# 3. Linting
npm run lint

# 4. API client generation (if OpenAPI spec changed)
npm run generate-api
```

## 🌏 Korean Language Support

### Internationalization Setup
```typescript
// Material-UI Korean locale
import { koKR } from '@mui/material/locale';
import { koKR as coreKoKR } from '@mui/material/locale';

const theme = createTheme(
  {
    // Your theme customization
  },
  koKR, // Korean locale for MUI components
);
```

### Text Display Best Practices
- **Font Support**: Ensure Korean fonts are loaded (Noto Sans Korean)
- **Text Overflow**: Handle Korean text wrapping properly
- **Input Validation**: Support Korean input methods (IME)
- **Currency Formatting**: Display Korean Won (₩) correctly

## 📊 Real-time Features

### WebSocket Integration Strategy
```typescript
// WebSocket connection management
export function useWebSocketConnection() {
  const [connectionStatus, setConnectionStatus] = useState('connecting');

  useEffect(() => {
    const client = new StompJs.Client({
      brokerURL: 'ws://localhost:8080/ws',
      onConnect: () => setConnectionStatus('connected'),
      onDisconnect: () => setConnectionStatus('disconnected'),
      reconnectDelay: 5000, // Auto-reconnect after 5 seconds
    });

    client.activate();
    return () => client.deactivate();
  }, []);

  return { connectionStatus };
}
```

### Real-time Data Handling
- **Market Data**: Live price updates with smooth animations
- **Portfolio Changes**: Instant position and P&L updates
- **Order Execution**: Real-time order status notifications
- **Challenge Updates**: Live leaderboard and participant counts

## 🧪 Testing Strategy

### Testing Approach
```typescript
// Component testing with React Testing Library
import { render, screen } from '@testing-library/react';
import { PortfolioPanel } from './PortfolioPanel';

test('displays portfolio data correctly', async () => {
  render(<PortfolioPanel sessionId={123} />);

  // Wait for data to load
  await screen.findByText('Portfolio Summary');

  // Assert portfolio metrics are displayed
  expect(screen.getByText('Total Value')).toBeInTheDocument();
  expect(screen.getByText('Profit/Loss')).toBeInTheDocument();
});
```

### Testing Coverage
- **Unit Tests**: Custom hooks and utility functions
- **Component Tests**: UI component behavior and rendering
- **Integration Tests**: API integration and data flow
- **E2E Tests**: Complete user workflows (Playwright)

## 🚀 Performance Optimization

### Bundle Optimization
```typescript
// Dynamic imports for code splitting
const TradingInterface = dynamic(
  () => import('@/features/trading/TradingInterface'),
  {
    loading: () => <TradingSkeleton />,
    ssr: false // Disable SSR for client-only components
  }
);
```

### Image Optimization
```typescript
// Next.js Image component for optimal loading
import Image from 'next/image';

<Image
  src="/images/company-logos/samsung.png"
  alt="Samsung Electronics"
  width={64}
  height={64}
  priority={false} // Lazy load non-critical images
/>
```

## 🐛 Troubleshooting

### Common Issues & Solutions

**API Client Generation Issues**:
```bash
# Clear generated files and regenerate
rm -rf src/shared/api/generated/
npm run generate-api
```

**TypeScript Type Errors**:
```bash
# Check for type conflicts
npm run type-check

# Clear Next.js cache
rm -rf .next/
npm run dev
```

**WebSocket Connection Problems**:
```typescript
// Check WebSocket connection in browser DevTools
// Network tab should show WebSocket upgrade request
// Console should show STOMP connection logs
```

**Performance Issues**:
```bash
# Analyze bundle size
npm run analyze

# Check for unnecessary re-renders
# Use React DevTools Profiler
```

### Debug Commands
```bash
# Development with detailed logging
DEBUG=* npm run dev

# Build analysis
npm run build && npm run analyze

# Type checking in watch mode
npm run type-check -- --watch
```

### Environment Verification
```bash
# Verify API connectivity
curl -s http://localhost:8080/api/challenges | jq .

# Check WebSocket endpoint
wscat -c ws://localhost:8080/ws

# Test API client generation
npm run generate-api && echo "✅ API client generated successfully"
```

## 🎨 UI/UX Improvement Plan

### Current State Analysis

**Technical Foundation (Excellent)**:
- ✅ Next.js 14 + Material-UI + TypeScript + Feature-Sliced Design
- ✅ Mobile-first responsive design already implemented
- ✅ Professional trading platform theme with dark mode support
- ✅ Touch-optimized components with swipe gestures
- ✅ Real-time WebSocket integration for live data
- ✅ Auto-generated API client with TanStack Query

**Current Strengths**:
- Solid architectural foundation with Feature-Sliced Design
- Comprehensive mobile responsiveness
- Professional trading interface with dark theme
- Efficient state management and API integration
- Korean language support infrastructure

### 3-Tier Improvement Roadmap

#### **Tier 1: Essential UX Enhancements** (13 hours)
*Focus: Immediate user experience improvements with high ROI*

**1. Loading & Feedback States** (4 hours)
```typescript
// Enhanced loading states with skeleton components
export function PortfolioSkeleton() {
  return (
    <Box sx={{ p: 3 }}>
      <Skeleton variant="rectangular" width="100%" height={60} sx={{ mb: 2 }} />
      <Skeleton variant="text" width="60%" height={40} sx={{ mb: 1 }} />
      <Skeleton variant="circular" width={40} height={40} />
    </Box>
  );
}

// Smart loading indicators
export function SmartLoadingButton({ isLoading, children, ...props }) {
  return (
    <Button {...props} disabled={isLoading}>
      {isLoading ? <CircularProgress size={20} /> : children}
    </Button>
  );
}
```

**2. Data Visualization Enhancement** (5 hours)
```typescript
// Enhanced chart components with better mobile interaction
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip } from 'recharts';

export function PerformanceChart({ data, theme }) {
  return (
    <ResponsiveContainer width="100%" height={300}>
      <LineChart data={data}>
        <XAxis
          dataKey="date"
          tick={{ fontSize: 12 }}
          axisLine={{ stroke: theme.palette.divider }}
        />
        <YAxis
          tick={{ fontSize: 12 }}
          axisLine={{ stroke: theme.palette.divider }}
        />
        <Tooltip
          contentStyle={{
            backgroundColor: theme.palette.background.paper,
            border: `1px solid ${theme.palette.divider}`,
            borderRadius: 8,
          }}
        />
        <Line
          type="monotone"
          dataKey="value"
          stroke={theme.palette.primary.main}
          strokeWidth={2}
          dot={false}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
```

**3. Micro-interactions & Feedback** (4 hours)
```typescript
// Animated state transitions
import { motion, AnimatePresence } from 'framer-motion';

export function OrderStatusCard({ order }) {
  const getStatusColor = (status) => {
    switch (status) {
      case 'FILLED': return 'success.main';
      case 'PENDING': return 'warning.main';
      case 'CANCELLED': return 'error.main';
      default: return 'text.secondary';
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      transition={{ duration: 0.3 }}
    >
      <Card sx={{ mb: 1 }}>
        <CardContent>
          <Chip
            label={order.status}
            color={getStatusColor(order.status)}
            size="small"
            sx={{
              animation: order.status === 'PENDING' ? 'pulse 2s infinite' : 'none'
            }}
          />
          <Typography variant="body2">{order.symbol} - {order.quantity} shares</Typography>
        </CardContent>
      </Card>
    </motion.div>
  );
}
```

#### **Tier 2: Advanced Mobile UX** (26 hours)
*Focus: Mobile-specific optimizations and accessibility*

**1. Advanced Mobile Gestures** (8 hours)
```typescript
// Enhanced swipe gestures for portfolio management
import { useSwipeable } from 'react-swipeable';

export function SwipeablePositionCard({ position, onEdit, onSell }) {
  const handlers = useSwipeable({
    onSwipedLeft: () => onSell(position.id),
    onSwipedRight: () => onEdit(position.id),
    swipeDuration: 500,
    preventScrollOnSwipe: true,
    trackMouse: true,
  });

  return (
    <motion.div
      {...handlers}
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
    >
      <Card sx={{ position: 'relative', overflow: 'hidden' }}>
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            background: 'linear-gradient(to right, #4caf50, transparent)',
            opacity: 0,
            transition: 'opacity 0.3s',
            '&.swipe-hint': { opacity: 0.1 }
          }}
        />
        <CardContent>
          <Typography variant="h6">{position.symbol}</Typography>
          <Typography variant="body2" color="textSecondary">
            Swipe left to sell, right to edit
          </Typography>
        </CardContent>
      </Card>
    </motion.div>
  );
}
```

**2. Accessibility Enhancements** (8 hours)
```typescript
// WCAG 2.1 AA compliant components
export function AccessibleTradingButton({
  action,
  symbol,
  price,
  onClick,
  disabled
}) {
  const ariaLabel = `${action} ${symbol} at ${price} dollars per share`;

  return (
    <Button
      variant="contained"
      color={action === 'BUY' ? 'success' : 'error'}
      onClick={onClick}
      disabled={disabled}
      aria-label={ariaLabel}
      sx={{
        minHeight: 48, // Minimum touch target size
        fontSize: '1rem',
        fontWeight: 'bold',
        '&:focus': {
          outline: '2px solid',
          outlineColor: 'primary.main',
          outlineOffset: '2px',
        }
      }}
    >
      <Box component="span" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        {action === 'BUY' ? <TrendingUpIcon /> : <TrendingDownIcon />}
        {action} {symbol}
      </Box>
    </Button>
  );
}
```

**3. Performance Optimization** (10 hours)
```typescript
// Virtual scrolling for large datasets
import { FixedSizeList as List } from 'react-window';

export function VirtualizedPositionList({ positions }) {
  const Row = ({ index, style }) => (
    <div style={style}>
      <PositionCard position={positions[index]} />
    </div>
  );

  return (
    <List
      height={400}
      itemCount={positions.length}
      itemSize={80}
      width="100%"
    >
      {Row}
    </List>
  );
}

// Optimized chart rendering with canvas
export function HighPerformanceChart({ data }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');

    // Custom canvas rendering for better performance
    renderChart(ctx, data);
  }, [data]);

  return (
    <canvas
      ref={canvasRef}
      width={800}
      height={400}
      style={{ width: '100%', height: 'auto' }}
    />
  );
}
```

#### **Tier 3: Advanced Features** (34 hours)
*Focus: Progressive Web App features and advanced interactions*

**1. PWA Implementation** (12 hours)
```typescript
// Service worker for offline functionality
// next.config.js
const withPWA = require('next-pwa')({
  dest: 'public',
  register: true,
  skipWaiting: true,
  runtimeCaching: [
    {
      urlPattern: /^https:\/\/api\.stockquest\.com\/.*$/,
      handler: 'NetworkFirst',
      options: {
        cacheName: 'api-cache',
        expiration: {
          maxEntries: 100,
          maxAgeSeconds: 5 * 60, // 5 minutes
        },
      },
    },
  ],
});

module.exports = withPWA({
  // Your Next.js config
});

// PWA install prompt
export function PWAInstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [showInstall, setShowInstall] = useState(false);

  useEffect(() => {
    const handler = (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
      setShowInstall(true);
    };

    window.addEventListener('beforeinstallprompt', handler);
    return () => window.removeEventListener('beforeinstallprompt', handler);
  }, []);

  const handleInstall = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      console.log('PWA install outcome:', outcome);
      setDeferredPrompt(null);
      setShowInstall(false);
    }
  };

  if (!showInstall) return null;

  return (
    <Snackbar open={showInstall} autoHideDuration={6000}>
      <Alert
        severity="info"
        action={
          <Button color="inherit" size="small" onClick={handleInstall}>
            Install App
          </Button>
        }
      >
        Install Stock Quest for better performance and offline access
      </Alert>
    </Snackbar>
  );
}
```

**2. Advanced Data Visualization** (12 hours)
```typescript
// Interactive candlestick charts with D3.js
import * as d3 from 'd3';

export function CandlestickChart({ data, width = 800, height = 400 }) {
  const svgRef = useRef();

  useEffect(() => {
    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove(); // Clear previous render

    const margin = { top: 20, right: 30, bottom: 40, left: 40 };
    const innerWidth = width - margin.left - margin.right;
    const innerHeight = height - margin.top - margin.bottom;

    const xScale = d3.scaleTime()
      .domain(d3.extent(data, d => new Date(d.date)))
      .range([0, innerWidth]);

    const yScale = d3.scaleLinear()
      .domain(d3.extent(data, d => Math.max(d.high, d.low)))
      .range([innerHeight, 0]);

    const g = svg.append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    // Draw candlesticks
    g.selectAll('.candlestick')
      .data(data)
      .enter().append('g')
      .attr('class', 'candlestick')
      .attr('transform', d => `translate(${xScale(new Date(d.date))},0)`)
      .each(function(d) {
        const candle = d3.select(this);

        // High-low line
        candle.append('line')
          .attr('y1', yScale(d.high))
          .attr('y2', yScale(d.low))
          .attr('stroke', '#666')
          .attr('stroke-width', 1);

        // Open-close rectangle
        candle.append('rect')
          .attr('y', yScale(Math.max(d.open, d.close)))
          .attr('height', Math.abs(yScale(d.open) - yScale(d.close)))
          .attr('width', 6)
          .attr('x', -3)
          .attr('fill', d.close > d.open ? '#4caf50' : '#f44336');
      });

    // Add axes
    g.append('g')
      .attr('transform', `translate(0,${innerHeight})`)
      .call(d3.axisBottom(xScale));

    g.append('g')
      .call(d3.axisLeft(yScale));

  }, [data, width, height]);

  return <svg ref={svgRef} width={width} height={height} />;
}
```

**3. Advanced Animation System** (10 hours)
```typescript
// Context-aware animation system
import { createContext, useContext } from 'react';
import { useSpring, config } from '@react-spring/web';

const AnimationContext = createContext({
  reduceMotion: false,
  animationSpeed: 1,
});

export function AnimationProvider({ children }) {
  const [reduceMotion, setReduceMotion] = useState(
    window.matchMedia('(prefers-reduced-motion: reduce)').matches
  );

  return (
    <AnimationContext.Provider value={{ reduceMotion, animationSpeed: 1 }}>
      {children}
    </AnimationContext.Provider>
  );
}

export function useSmartAnimation(animationConfig) {
  const { reduceMotion, animationSpeed } = useContext(AnimationContext);

  return useSpring({
    ...animationConfig,
    config: reduceMotion
      ? { duration: 0 }
      : { ...config.gentle, duration: config.gentle.duration / animationSpeed },
  });
}

// Portfolio value animation
export function AnimatedPortfolioValue({ value, previousValue }) {
  const { reduceMotion } = useContext(AnimationContext);

  const animatedValue = useSpring({
    from: { value: previousValue || value },
    to: { value },
    config: config.slow,
    immediate: reduceMotion,
  });

  const colorSpring = useSpring({
    color: value > previousValue ? '#4caf50' : '#f44336',
    config: config.gentle,
    immediate: reduceMotion,
  });

  return (
    <animated.div style={colorSpring}>
      <animated.span>
        {animatedValue.value.to(val => `$${val.toLocaleString()}`)}
      </animated.span>
    </animated.div>
  );
}
```

### Implementation Roadmap

#### **Phase 1: Quick Wins** (Week 1-2)
1. **Loading States** → Implement skeleton loading for all data components
2. **Micro-interactions** → Add button feedback and hover effects
3. **Data Visualization** → Enhance chart readability and mobile interaction

**Business Impact**:
- 25% reduction in perceived loading time
- Improved user engagement through better feedback
- Enhanced mobile trading experience

#### **Phase 2: Mobile Excellence** (Week 3-5)
1. **Advanced Gestures** → Implement swipe actions for common tasks
2. **Accessibility** → Achieve WCAG 2.1 AA compliance
3. **Performance** → Optimize for 60fps on mobile devices

**Business Impact**:
- 40% improvement in mobile conversion rates
- Better accessibility compliance
- Reduced bounce rate on mobile devices

#### **Phase 3: Advanced Features** (Week 6-8)
1. **PWA Features** → Enable offline functionality and app installation
2. **Advanced Charts** → Implement professional trading chart components
3. **Animation System** → Create cohesive motion design language

**Business Impact**:
- 60% increase in user retention through PWA features
- Professional trading platform experience
- Enhanced brand perception and user satisfaction

### Technical Dependencies

#### **New Dependencies Required**:
```json
{
  "framer-motion": "^10.16.5",
  "react-spring": "^9.7.1",
  "react-window": "^1.8.8",
  "react-swipeable": "^7.0.1",
  "d3": "^7.8.5",
  "next-pwa": "^5.6.0",
  "recharts": "^2.8.0"
}
```

#### **Configuration Updates**:
```typescript
// next.config.js additions for PWA
const withPWA = require('next-pwa')({
  dest: 'public',
  register: true,
  skipWaiting: true,
});

// Theme enhancements for animation support
const theme = createTheme({
  transitions: {
    duration: {
      shortest: 150,
      shorter: 200,
      short: 250,
      standard: 300,
      complex: 375,
      enteringScreen: 225,
      leavingScreen: 195,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 4px 8px rgba(0,0,0,0.12)',
          },
        },
      },
    },
  },
});
```

### Success Metrics

#### **Tier 1 Success Metrics**:
- Loading time perception: <2s perceived load time
- User engagement: 30% increase in page interaction time
- Mobile usability: 90%+ mobile usability score

#### **Tier 2 Success Metrics**:
- Mobile conversion: 40% improvement in mobile trading completion
- Accessibility: WCAG 2.1 AA compliance (100%)
- Performance: Lighthouse score >90 on mobile

#### **Tier 3 Success Metrics**:
- User retention: 60% increase through PWA features
- Professional perception: 95%+ user satisfaction with trading interface
- Technical excellence: Industry-leading frontend performance metrics

### Quality Gates

#### **Design Review Checklist**:
- [ ] Mobile-first responsive design verified
- [ ] Dark mode compatibility confirmed
- [ ] Korean text rendering validated
- [ ] Touch target sizes (44px minimum) verified
- [ ] Color contrast ratios meet WCAG AA standards

#### **Performance Validation**:
- [ ] Bundle size impact <50KB per tier
- [ ] Runtime performance maintains 60fps
- [ ] Memory usage stays within mobile limits
- [ ] Network requests optimized for mobile networks

#### **Accessibility Testing**:
- [ ] Screen reader compatibility verified
- [ ] Keyboard navigation functional
- [ ] Focus management implemented
- [ ] ARIA labels and descriptions added
- [ ] Color-blind user testing completed

---
*Frontend guide for Claude Code - Updated 2025-09-22*