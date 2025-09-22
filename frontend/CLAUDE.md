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

---
*Frontend guide for Claude Code - Updated 2025-09-22*