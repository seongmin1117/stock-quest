'use client';

import React from 'react';
import { Box, Drawer, AppBar, Toolbar, Typography, List, ListItem, ListItemButton, ListItemIcon, ListItemText, CssBaseline } from '@mui/material';
import { Dashboard, TrendingUp, Assignment, People, Settings, Analytics, SmartToy, Recommend, PieChart, Security } from '@mui/icons-material';
import { useRouter, usePathname } from 'next/navigation';
import AdminAuthGuard from '@/features/admin-panel/components/AdminAuthGuard';

const drawerWidth = 240;

const menuItems = [
  { text: '대시보드', icon: <Dashboard />, path: '/admin/dashboard' },
  { text: '챌린지 관리', icon: <TrendingUp />, path: '/admin/challenges' },
  { text: '템플릿 관리', icon: <Assignment />, path: '/admin/templates' },
  { text: '사용자 관리', icon: <People />, path: '/admin/users' },
  { text: '분석', icon: <Analytics />, path: '/admin/analytics' },
  { text: 'ML 트레이딩', icon: <SmartToy />, path: '/admin/ml-trading' },
  { text: '추천 시스템', icon: <Recommend />, path: '/admin/recommendations' },
  { text: '포트폴리오 최적화', icon: <PieChart />, path: '/admin/portfolio-optimization' },
  { text: '리스크 관리', icon: <Security />, path: '/admin/risk-management' },
  { text: '설정', icon: <Settings />, path: '/admin/settings' },
];

interface AdminLayoutProps {
  children: React.ReactNode;
}

export default function AdminLayout({ children }: AdminLayoutProps) {
  const router = useRouter();
  const pathname = usePathname();

  return (
    <AdminAuthGuard>
      <Box sx={{ display: 'flex' }}>
        <CssBaseline />

        {/* App Bar */}
        <AppBar
          position="fixed"
          sx={{
            width: `calc(100% - ${drawerWidth}px)`,
            ml: `${drawerWidth}px`,
            bgcolor: '#1976d2',
          }}
        >
          <Toolbar>
            <Typography variant="h6" noWrap component="div">
              StockQuest 관리자 패널
            </Typography>
          </Toolbar>
        </AppBar>

        {/* Sidebar */}
        <Drawer
          sx={{
            width: drawerWidth,
            flexShrink: 0,
            '& .MuiDrawer-paper': {
              width: drawerWidth,
              boxSizing: 'border-box',
              bgcolor: '#f5f5f5',
            },
          }}
          variant="permanent"
          anchor="left"
        >
          <Box sx={{ p: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
              StockQuest
            </Typography>
            <Typography variant="caption" color="text.secondary">
              관리자 도구
            </Typography>
          </Box>

          <List>
            {menuItems.map((item) => (
              <ListItem key={item.text} disablePadding>
                <ListItemButton
                  onClick={() => router.push(item.path)}
                  selected={pathname === item.path}
                  sx={{
                    '&.Mui-selected': {
                      bgcolor: '#e3f2fd',
                      '&:hover': {
                        bgcolor: '#bbdefb',
                      },
                    },
                  }}
                >
                  <ListItemIcon sx={{ color: pathname === item.path ? '#1976d2' : 'inherit' }}>
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText
                    primary={item.text}
                    sx={{
                      '& .MuiTypography-root': {
                        color: pathname === item.path ? '#1976d2' : 'inherit',
                        fontWeight: pathname === item.path ? 'bold' : 'normal'
                      }
                    }}
                  />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Drawer>

        {/* Main Content */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            bgcolor: '#f9f9f9',
            p: 3,
            minHeight: '100vh',
          }}
        >
          <Toolbar /> {/* Spacer for fixed AppBar */}
          {children}
        </Box>
      </Box>
    </AdminAuthGuard>
  );
}