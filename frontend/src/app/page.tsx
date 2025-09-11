import { Container, Typography, Box, Button, Card, CardContent, Grid, Chip, Stack } from '@mui/material';
import Link from 'next/link';
import { TrendingUp, TrendingDown, Analytics, Security, Speed, Groups, ArrowForward, ShowChart, Assessment, Timeline } from '@mui/icons-material';

/**
 * StockQuest Professional Trading Platform Landing Page
 * Clean, data-focused design inspired by modern trading platforms
 */
export default function HomePage() {
  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#0A0E18' }}>
      <Container maxWidth="xl" sx={{ pt: { xs: 3, md: 6 }, pb: 6 }}>
        {/* Hero Section - Professional Trading Style */}
        <Box textAlign="center" sx={{ mb: 10 }}>
          <Chip 
            icon={<Analytics />}
            label="AI Trading Simulator"
            variant="outlined"
            sx={{ 
              mb: 4,
              px: 3,
              py: 1,
              fontSize: '0.875rem',
              fontWeight: 500,
              color: '#2196F3',
              border: '1px solid #2196F3',
              backgroundColor: 'rgba(33, 150, 243, 0.08)',
            }}
          />
          
          <Typography 
            variant="h1" 
            component="h1" 
            sx={{ 
              mb: 3,
              fontSize: { xs: '2.5rem', sm: '3rem', md: '3.5rem' },
              fontWeight: 700,
              color: '#FFFFFF',
              letterSpacing: '-0.02em',
            }}
          >
            StockQuest
          </Typography>
          
          <Typography 
            variant="h4" 
            sx={{ 
              mb: 4,
              fontSize: { xs: '1.125rem', sm: '1.25rem', md: '1.5rem' },
              fontWeight: 500,
              color: '#B0BEC5',
              lineHeight: 1.5,
              maxWidth: 600,
              mx: 'auto',
            }}
          >
            과거 데이터로 미래 투자 실력을 키우는{' '}
            <Box component="span" sx={{ color: '#2196F3', fontWeight: 600 }}>
              AI 트레이딩 플랫폼
            </Box>
          </Typography>
          
          <Typography 
            variant="body1" 
            sx={{ 
              mb: 6, 
              maxWidth: 550, 
              mx: 'auto',
              fontSize: '1rem',
              lineHeight: 1.6,
              color: '#78828A',
            }}
          >
            실제 과거 시장 데이터를 AI가 빠르게 재생하여 투자 경험을 쌓고,
            전 세계 트레이더들과 경쟁하며 투자 실력을 향상시켜보세요.
          </Typography>
          
          <Stack 
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            justifyContent="center"
          >
            <Button
              component={Link}
              href="/challenges"
              variant="contained"
              size="large"
              endIcon={<ArrowForward />}
              sx={{ 
                px: 4, 
                py: 1.5,
                fontSize: '0.875rem',
                fontWeight: 600,
                backgroundColor: '#2196F3',
                '&:hover': {
                  backgroundColor: '#1976D2',
                  transform: 'translateY(-1px)',
                }
              }}
            >
              챌린지 시작하기
            </Button>
            
            <Button
              component={Link}
              href="/auth/signup"
              variant="outlined"
              size="large"
              sx={{ 
                px: 4, 
                py: 1.5,
                fontSize: '0.875rem',
                fontWeight: 500,
                border: '1px solid #2A3441',
                color: '#B0BEC5',
                '&:hover': {
                  border: '1px solid #2196F3',
                  backgroundColor: 'rgba(33, 150, 243, 0.08)',
                  transform: 'translateY(-1px)',
                }
              }}
            >
              무료 체험하기
            </Button>
          </Stack>
        </Box>

        {/* Features Section - Professional Grid */}
        <Box sx={{ mb: 10 }}>
          <Box textAlign="center" sx={{ mb: 8 }}>
            <Typography 
              variant="h2" 
              sx={{ 
                mb: 3,
                fontSize: { xs: '1.5rem', md: '2rem' },
                fontWeight: 600,
                color: '#FFFFFF',
              }}
            >
              전문가들이 선택한 플랫폼
            </Typography>
            <Typography 
              variant="body1" 
              sx={{ 
                fontSize: '1rem', 
                maxWidth: 500, 
                mx: 'auto',
                color: '#78828A',
                lineHeight: 1.6,
              }}
            >
              실제 투자 전문가들이 사용하는 도구와 데이터로 실전 경험을 쌓아보세요
            </Typography>
          </Box>
          
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Card 
                sx={{
                  height: '100%',
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  transition: 'all 0.2s ease-out',
                  '&:hover': {
                    border: '1px solid #2196F3',
                    boxShadow: '0 4px 16px rgba(33, 150, 243, 0.1)',
                  }
                }}
              >
                <CardContent sx={{ p: 4 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        backgroundColor: 'rgba(33, 150, 243, 0.15)',
                        borderRadius: 1.5,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mr: 2,
                      }}
                    >
                      <Speed sx={{ fontSize: 24, color: '#2196F3' }} />
                    </Box>
                    <Typography variant="h6" sx={{ fontWeight: 600, color: '#FFFFFF' }}>
                      AI 가속 시뮬레이션
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ color: '#78828A', lineHeight: 1.6 }}>
                    과거 20년 시장 데이터를 AI가 분석하여 1일을 5초로 압축 재생.
                    수년간의 투자 경험을 단 몇 시간만에 체험하세요.
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={4}>
              <Card 
                sx={{
                  height: '100%',
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  transition: 'all 0.2s ease-out',
                  '&:hover': {
                    border: '1px solid #4CAF50',
                    boxShadow: '0 4px 16px rgba(76, 175, 80, 0.1)',
                  }
                }}
              >
                <CardContent sx={{ p: 4 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        backgroundColor: 'rgba(76, 175, 80, 0.15)',
                        borderRadius: 1.5,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mr: 2,
                      }}
                    >
                      <Security sx={{ fontSize: 24, color: '#4CAF50' }} />
                    </Box>
                    <Typography variant="h6" sx={{ fontWeight: 600, color: '#FFFFFF' }}>
                      제로 리스크 환경
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ color: '#78828A', lineHeight: 1.6 }}>
                    실제 돈 없이 100만원의 가상 자금으로 안전하게 연습.
                    실패해도 부담 없는 완벽한 학습 환경을 제공합니다.
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={4}>
              <Card 
                sx={{
                  height: '100%',
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  transition: 'all 0.2s ease-out',
                  '&:hover': {
                    border: '1px solid #FF9800',
                    boxShadow: '0 4px 16px rgba(255, 152, 0, 0.1)',
                  }
                }}
              >
                <CardContent sx={{ p: 4 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        backgroundColor: 'rgba(255, 152, 0, 0.15)',
                        borderRadius: 1.5,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mr: 2,
                      }}
                    >
                      <Groups sx={{ fontSize: 24, color: '#FF9800' }} />
                    </Box>
                    <Typography variant="h6" sx={{ fontWeight: 600, color: '#FFFFFF' }}>
                      글로벌 커뮤니티
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ color: '#78828A', lineHeight: 1.6 }}>
                    전 세계 트레이더들과 실시간 수익률 경쟁.
                    커뮤니티에서 투자 노하우를 공유하고 배우세요.
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Box>

        {/* Stats Section - Trading Dashboard Style */}
        <Box sx={{ mb: 10 }}>
          <Grid container spacing={2}>
            <Grid item xs={6} md={3}>
              <Box 
                sx={{
                  p: 3,
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  textAlign: 'center',
                }}
              >
                <Typography 
                  variant="h4" 
                  sx={{ 
                    fontWeight: 700,
                    color: '#2196F3',
                    fontFamily: '"Roboto Mono", monospace',
                    fontSize: '1.5rem',
                  }}
                >
                  10K+
                </Typography>
                <Typography variant="body2" sx={{ color: '#78828A', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  ACTIVE USERS
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={6} md={3}>
              <Box 
                sx={{
                  p: 3,
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  textAlign: 'center',
                }}
              >
                <Typography 
                  variant="h4" 
                  sx={{ 
                    fontWeight: 700,
                    color: '#4CAF50',
                    fontFamily: '"Roboto Mono", monospace',
                    fontSize: '1.5rem',
                  }}
                >
                  500M+
                </Typography>
                <Typography variant="body2" sx={{ color: '#78828A', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  TRADES EXECUTED
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={6} md={3}>
              <Box 
                sx={{
                  p: 3,
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  textAlign: 'center',
                }}
              >
                <Typography 
                  variant="h4" 
                  sx={{ 
                    fontWeight: 700,
                    color: '#FF9800',
                    fontFamily: '"Roboto Mono", monospace',
                    fontSize: '1.5rem',
                  }}
                >
                  20Y
                </Typography>
                <Typography variant="body2" sx={{ color: '#78828A', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  MARKET DATA
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={6} md={3}>
              <Box 
                sx={{
                  p: 3,
                  backgroundColor: '#1A1F2E',
                  border: '1px solid #2A3441',
                  borderRadius: 2,
                  textAlign: 'center',
                }}
              >
                <Typography 
                  variant="h4" 
                  sx={{ 
                    fontWeight: 700,
                    color: '#2196F3',
                    fontFamily: '"Roboto Mono", monospace',
                    fontSize: '1.5rem',
                  }}
                >
                  95%
                </Typography>
                <Typography variant="body2" sx={{ color: '#78828A', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  SATISFACTION
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Box>

        {/* CTA Section - Professional Call to Action */}
        <Box 
          sx={{
            textAlign: 'center',
            py: 6,
            px: 4,
            backgroundColor: '#1A1F2E',
            border: '1px solid #2A3441',
            borderRadius: 2,
          }}
        >
          <Typography 
            variant="h3" 
            sx={{ 
              mb: 3,
              fontSize: { xs: '1.5rem', md: '2rem' },
              fontWeight: 600,
              color: '#FFFFFF',
            }}
          >
            투자 전문가의 여정을 시작하세요
          </Typography>
          
          <Typography 
            variant="body1" 
            sx={{ 
              mb: 4, 
              color: '#78828A',
              lineHeight: 1.6,
              maxWidth: 500,
              mx: 'auto'
            }}
          >
            전 세계 10,000명의 트레이더들이 선택한 플랫폼에서 당신의 투자 실력을 검증해보세요
          </Typography>
          
          <Stack 
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            justifyContent="center"
            sx={{ mb: 4 }}
          >
            <Button
              component={Link}
              href="/auth/signup"
              variant="contained"
              size="large"
              endIcon={<ArrowForward />}
              sx={{
                px: 6,
                py: 1.5,
                fontSize: '0.875rem',
                fontWeight: 600,
                backgroundColor: '#2196F3',
                '&:hover': {
                  backgroundColor: '#1976D2',
                  transform: 'translateY(-1px)',
                }
              }}
            >
              무료로 시작하기
            </Button>
            
            <Button
              component={Link}
              href="/challenges"
              variant="outlined"
              size="large"
              sx={{
                px: 6,
                py: 1.5,
                fontSize: '0.875rem',
                fontWeight: 500,
                border: '1px solid #2A3441',
                color: '#B0BEC5',
                '&:hover': {
                  border: '1px solid #2196F3',
                  backgroundColor: 'rgba(33, 150, 243, 0.08)',
                }
              }}
            >
              챌린지 둘러보기
            </Button>
          </Stack>
          
          <Stack 
            direction="row" 
            spacing={3}
            justifyContent="center"
            divider={<Box sx={{ width: 1, height: 16, backgroundColor: '#2A3441' }} />}
          >
            <Typography variant="caption" sx={{ color: '#78828A', display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box component="span" sx={{ color: '#4CAF50' }}>⭐</Box>
              4.9/5 사용자 평점
            </Typography>
            <Typography variant="caption" sx={{ color: '#78828A', display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box component="span" sx={{ color: '#2196F3' }}>🔒</Box>
              100% 무료 체험
            </Typography>
            <Typography variant="caption" sx={{ color: '#78828A', display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box component="span" sx={{ color: '#FF9800' }}>⚡</Box>
              30초 만에 시작
            </Typography>
          </Stack>
        </Box>
      </Container>
    </Box>
  );
}