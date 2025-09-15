'use client';

import React from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Avatar,
  Menu,
  MenuItem,
  IconButton,
  useMediaQuery,
  useTheme,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
} from '@mui/material';
import {
  Home,
  TrendingUp,
  Leaderboard,
  People,
  Person,
  ExitToApp,
  Menu as MenuIcon,
  Dashboard,
  ShowChart,
} from '@mui/icons-material';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth, useAuthActions } from '@/shared/lib/auth/auth-store';

interface NavbarProps {
  position?: 'fixed' | 'static' | 'sticky' | 'absolute' | 'relative';
}

/**
 * 네비게이션 바 컴포넌트
 * 반응형 디자인과 사용자 인증 상태에 따른 메뉴 제공
 */
export default function Navbar({ position = 'static' }: NavbarProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const { logout } = useAuthActions();
  
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const [mobileOpen, setMobileOpen] = React.useState(false);

  const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    handleUserMenuClose();
    router.push('/');
  };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const navigationItems = [
    { label: '홈', icon: <Home />, href: '/' },
    { label: '챌린지', icon: <TrendingUp />, href: '/challenges' },
    { label: 'DCA 시뮬레이션', icon: <ShowChart />, href: '/dca-simulation' },
    { label: '리더보드', icon: <Leaderboard />, href: '/leaderboard' },
    { label: '커뮤니티', icon: <People />, href: '/community' },
  ];

  const authItems = isAuthenticated
    ? [
        { label: '대시보드', icon: <Dashboard />, href: '/dashboard' },
        { label: '프로필', icon: <Person />, href: '/profile' },
      ]
    : [
        { label: '로그인', icon: <Person />, href: '/auth/login' },
        { label: '회원가입', icon: <Person />, href: '/auth/signup' },
      ];

  // 모바일 드로어 컨텐츠
  const drawer = (
    <Box sx={{ width: 250 }} role="presentation">
      <Box sx={{ p: 2, backgroundColor: '#1A1F2E' }}>
        <Typography variant="h6" sx={{ color: '#2196F3', fontWeight: 'bold' }}>
          StockQuest
        </Typography>
        {isAuthenticated && user && (
          <Typography variant="body2" sx={{ color: '#78828A', mt: 1 }}>
            {user.nickname}님 환영합니다
          </Typography>
        )}
      </Box>
      
      <Divider sx={{ borderColor: '#2A3441' }} />
      
      <List>
        {navigationItems.map((item) => (
          <ListItem 
            key={item.href} 
            component={Link} 
            href={item.href}
            onClick={() => setMobileOpen(false)}
            sx={{
              '&:hover': {
                backgroundColor: 'rgba(33, 150, 243, 0.08)',
              }
            }}
          >
            <ListItemIcon sx={{ color: '#2196F3' }}>
              {item.icon}
            </ListItemIcon>
            <ListItemText 
              primary={item.label} 
              sx={{ '& .MuiListItemText-primary': { color: '#FFFFFF' } }}
            />
          </ListItem>
        ))}
      </List>

      <Divider sx={{ borderColor: '#2A3441' }} />

      <List>
        {authItems.map((item) => (
          <ListItem 
            key={item.href} 
            component={Link} 
            href={item.href}
            onClick={() => setMobileOpen(false)}
            sx={{
              '&:hover': {
                backgroundColor: 'rgba(33, 150, 243, 0.08)',
              }
            }}
          >
            <ListItemIcon sx={{ color: '#2196F3' }}>
              {item.icon}
            </ListItemIcon>
            <ListItemText 
              primary={item.label} 
              sx={{ '& .MuiListItemText-primary': { color: '#FFFFFF' } }}
            />
          </ListItem>
        ))}

        {isAuthenticated && user && (
          <ListItem 
            onClick={() => {
              handleLogout();
              setMobileOpen(false);
            }}
            sx={{
              cursor: 'pointer',
              '&:hover': {
                backgroundColor: 'rgba(244, 67, 54, 0.08)',
              }
            }}
          >
            <ListItemIcon sx={{ color: '#F44336' }}>
              <ExitToApp />
            </ListItemIcon>
            <ListItemText 
              primary="로그아웃" 
              sx={{ '& .MuiListItemText-primary': { color: '#F44336' } }}
            />
          </ListItem>
        )}
      </List>
    </Box>
  );

  return (
    <>
      <AppBar 
        position={position}
        sx={{ 
          backgroundColor: '#0A0E18',
          borderBottom: '1px solid #2A3441',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        }}
      >
        <Toolbar sx={{ px: { xs: 2, md: 4 } }}>
          {/* 로고 */}
          <Typography
            component={Link}
            href="/"
            variant="h6"
            sx={{
              fontWeight: 'bold',
              color: '#2196F3',
              textDecoration: 'none',
              mr: 4,
              '&:hover': {
                color: '#1976D2',
              }
            }}
          >
            StockQuest
          </Typography>

          {/* 데스크톱 네비게이션 */}
          {!isMobile && (
            <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
              {navigationItems.map((item) => (
                <Button
                  key={item.href}
                  component={Link}
                  href={item.href}
                  startIcon={item.icon}
                  sx={{
                    color: '#B0BEC5',
                    '&:hover': {
                      color: '#2196F3',
                      backgroundColor: 'rgba(33, 150, 243, 0.08)',
                    },
                    px: 2,
                  }}
                >
                  {item.label}
                </Button>
              ))}
            </Box>
          )}

          <Box sx={{ flexGrow: isMobile ? 1 : 0 }} />

          {/* 사용자 메뉴 (데스크톱) */}
          {!isMobile && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              {isAuthenticated ? (
                <>
                  <Button
                    component={Link}
                    href="/dashboard"
                    startIcon={<Dashboard />}
                    sx={{
                      color: '#B0BEC5',
                      '&:hover': {
                        color: '#2196F3',
                        backgroundColor: 'rgba(33, 150, 243, 0.08)',
                      },
                    }}
                  >
                    대시보드
                  </Button>
                  
                  <IconButton
                    onClick={handleUserMenuOpen}
                    sx={{ p: 0.5 }}
                  >
                    <Avatar
                      sx={{
                        width: 32,
                        height: 32,
                        backgroundColor: '#2196F3',
                        fontSize: '0.875rem',
                        fontWeight: 'bold',
                      }}
                    >
                      {user?.nickname?.charAt(0)?.toUpperCase() || 'U'}
                    </Avatar>
                  </IconButton>

                  <Menu
                    anchorEl={anchorEl}
                    open={Boolean(anchorEl)}
                    onClose={handleUserMenuClose}
                    PaperProps={{
                      sx: {
                        backgroundColor: '#1A1F2E',
                        border: '1px solid #2A3441',
                        mt: 1,
                      }
                    }}
                  >
                    <MenuItem
                      component={Link}
                      href="/profile"
                      onClick={handleUserMenuClose}
                      sx={{ color: '#FFFFFF' }}
                    >
                      <Person sx={{ mr: 1, fontSize: 20 }} />
                      프로필
                    </MenuItem>
                    <MenuItem
                      onClick={handleLogout}
                      sx={{ color: '#F44336' }}
                    >
                      <ExitToApp sx={{ mr: 1, fontSize: 20 }} />
                      로그아웃
                    </MenuItem>
                  </Menu>
                </>
              ) : (
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    component={Link}
                    href="/auth/login"
                    sx={{
                      color: '#B0BEC5',
                      '&:hover': {
                        color: '#FFFFFF',
                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                      },
                    }}
                  >
                    로그인
                  </Button>
                  <Button
                    component={Link}
                    href="/auth/signup"
                    variant="contained"
                    sx={{
                      backgroundColor: '#2196F3',
                      '&:hover': {
                        backgroundColor: '#1976D2',
                      },
                    }}
                  >
                    회원가입
                  </Button>
                </Box>
              )}
            </Box>
          )}

          {/* 모바일 메뉴 버튼 */}
          {isMobile && (
            <IconButton
              color="inherit"
              aria-label="open drawer"
              onClick={handleDrawerToggle}
              sx={{ color: '#2196F3' }}
            >
              <MenuIcon />
            </IconButton>
          )}
        </Toolbar>
      </AppBar>

      {/* 모바일 드로어 */}
      <Drawer
        variant="temporary"
        anchor="right"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }}
        PaperProps={{
          sx: {
            backgroundColor: '#0A0E18',
            color: '#FFFFFF',
          }
        }}
      >
        {drawer}
      </Drawer>
    </>
  );
}