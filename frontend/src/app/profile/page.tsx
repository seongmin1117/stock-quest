'use client';

import React from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Avatar,
  TextField,
  Button,
  Alert,
  Divider,
  FormControlLabel,
  Switch,
  Grid,
  Chip,
} from '@mui/material';
import {
  Person,
  Edit,
  Save,
  Cancel,
  Email,
  Badge,
  Settings,
  Notifications,
  Security,
  Language,
} from '@mui/icons-material';
import { useAuth, useAuthActions } from '@/shared/lib/auth/auth-store';

interface UserProfile {
  email: string;
  nickname: string;
  joinedAt: string;
  settings: {
    emailNotifications: boolean;
    marketAlerts: boolean;
    communityUpdates: boolean;
    language: 'ko' | 'en';
    theme: 'dark' | 'light';
  };
}

/**
 * 사용자 프로필 페이지
 * 개인 정보 수정 및 설정 관리
 */
export default function ProfilePage() {
  const { user } = useAuth();
  const { logout } = useAuthActions();
  const [profile, setProfile] = React.useState<UserProfile | null>(null);
  const [editMode, setEditMode] = React.useState(false);
  const [editedNickname, setEditedNickname] = React.useState('');
  const [loading, setLoading] = React.useState(false);
  const [success, setSuccess] = React.useState<string | null>(null);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (user) {
      loadProfile();
    }
  }, [user]);

  const loadProfile = async () => {
    try {
      // 실제로는 백엔드에서 프로필 데이터를 가져와야 함
      const mockProfile: UserProfile = {
        email: user?.email || '',
        nickname: user?.nickname || '',
        joinedAt: '2024-01-01T00:00:00Z',
        settings: {
          emailNotifications: true,
          marketAlerts: true,
          communityUpdates: false,
          language: 'ko',
          theme: 'dark',
        }
      };
      
      setProfile(mockProfile);
      setEditedNickname(mockProfile.nickname);
    } catch (err: any) {
      setError('프로필을 불러오는데 실패했습니다');
    }
  };

  const handleEditStart = () => {
    setEditMode(true);
    setError(null);
    setSuccess(null);
  };

  const handleEditCancel = () => {
    setEditMode(false);
    setEditedNickname(profile?.nickname || '');
    setError(null);
  };

  const handleSaveProfile = async () => {
    if (!editedNickname.trim()) {
      setError('닉네임을 입력해주세요');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // 실제로는 백엔드 API 호출
      // await apiClient.put('/api/auth/profile', { nickname: editedNickname.trim() });

      setProfile(prev => prev ? { ...prev, nickname: editedNickname.trim() } : null);
      setEditMode(false);
      setSuccess('프로필이 성공적으로 업데이트되었습니다');
    } catch (err: any) {
      setError('프로필 업데이트에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleSettingChange = (key: keyof UserProfile['settings'], value: any) => {
    setProfile(prev => prev ? {
      ...prev,
      settings: {
        ...prev.settings,
        [key]: value
      }
    } : null);
    
    // 실제로는 백엔드에 설정 저장
    setSuccess('설정이 저장되었습니다');
    setTimeout(() => setSuccess(null), 3000);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (!user) {
    return (
      <Container maxWidth="md">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <Alert severity="warning">
            프로필을 보려면 로그인이 필요합니다.
          </Alert>
        </Box>
      </Container>
    );
  }

  if (!profile) {
    return (
      <Container maxWidth="md">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
          <Typography>프로필을 불러오는 중...</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: '#0A0E18', pt: 4 }}>
      <Container maxWidth="md">
        <Box sx={{ py: 4 }}>
          {/* 헤더 */}
          <Box sx={{ mb: 4 }}>
            <Typography 
              variant="h3" 
              component="h1" 
              gutterBottom
              sx={{ 
                color: '#FFFFFF',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: 2,
              }}
            >
              <Person sx={{ fontSize: 40, color: '#2196F3' }} />
              프로필
            </Typography>
            
            <Typography 
              variant="body1" 
              sx={{ 
                color: '#78828A',
                lineHeight: 1.6,
              }}
            >
              개인 정보를 관리하고 알림 설정을 변경할 수 있습니다
            </Typography>
          </Box>

          {success && (
            <Alert severity="success" sx={{ mb: 3 }}>
              {success}
            </Alert>
          )}

          {error && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {error}
            </Alert>
          )}

          <Grid container spacing={3}>
            {/* 프로필 정보 */}
            <Grid item xs={12} md={8}>
              <Card sx={{ 
                backgroundColor: '#1A1F2E',
                border: '1px solid #2A3441',
                mb: 3,
              }}>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, mb: 4 }}>
                    <Avatar
                      sx={{
                        width: 80,
                        height: 80,
                        backgroundColor: '#2196F3',
                        fontSize: '2rem',
                        fontWeight: 'bold',
                      }}
                    >
                      {profile.nickname.charAt(0).toUpperCase()}
                    </Avatar>
                    
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="h5" sx={{ color: '#FFFFFF', fontWeight: 'bold', mb: 1 }}>
                        {profile.nickname}
                      </Typography>
                      <Typography variant="body2" sx={{ color: '#78828A', mb: 1 }}>
                        {profile.email}
                      </Typography>
                      <Typography variant="caption" sx={{ color: '#78828A' }}>
                        가입일: {formatDate(profile.joinedAt)}
                      </Typography>
                    </Box>

                    {!editMode && (
                      <Button
                        variant="outlined"
                        startIcon={<Edit />}
                        onClick={handleEditStart}
                        sx={{
                          color: '#2196F3',
                          borderColor: '#2196F3',
                          '&:hover': {
                            backgroundColor: 'rgba(33, 150, 243, 0.08)',
                          }
                        }}
                      >
                        수정
                      </Button>
                    )}
                  </Box>

                  <Divider sx={{ borderColor: '#2A3441', mb: 3 }} />

                  {/* 기본 정보 수정 */}
                  <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold', mb: 2 }}>
                    기본 정보
                  </Typography>

                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Email sx={{ color: '#78828A' }} />
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography variant="body2" sx={{ color: '#78828A', mb: 0.5 }}>
                          이메일
                        </Typography>
                        <Typography variant="body1" sx={{ color: '#FFFFFF' }}>
                          {profile.email}
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#78828A' }}>
                          이메일은 변경할 수 없습니다
                        </Typography>
                      </Box>
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Badge sx={{ color: '#78828A' }} />
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography variant="body2" sx={{ color: '#78828A', mb: 0.5 }}>
                          닉네임
                        </Typography>
                        {editMode ? (
                          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                            <TextField
                              value={editedNickname}
                              onChange={(e) => setEditedNickname(e.target.value)}
                              size="small"
                              sx={{
                                flexGrow: 1,
                                '& .MuiOutlinedInput-root': {
                                  color: '#FFFFFF',
                                  backgroundColor: '#0A0E18',
                                  '& fieldset': {
                                    borderColor: '#2A3441',
                                  },
                                  '&:hover fieldset': {
                                    borderColor: '#2196F3',
                                  },
                                  '&.Mui-focused fieldset': {
                                    borderColor: '#2196F3',
                                  },
                                },
                              }}
                            />
                            <Button
                              variant="contained"
                              size="small"
                              startIcon={<Save />}
                              onClick={handleSaveProfile}
                              disabled={loading}
                              sx={{
                                backgroundColor: '#2196F3',
                                '&:hover': {
                                  backgroundColor: '#1976D2',
                                }
                              }}
                            >
                              저장
                            </Button>
                            <Button
                              variant="outlined"
                              size="small"
                              startIcon={<Cancel />}
                              onClick={handleEditCancel}
                              sx={{
                                color: '#78828A',
                                borderColor: '#2A3441',
                                '&:hover': {
                                  borderColor: '#78828A',
                                }
                              }}
                            >
                              취소
                            </Button>
                          </Box>
                        ) : (
                          <Typography variant="body1" sx={{ color: '#FFFFFF' }}>
                            {profile.nickname}
                          </Typography>
                        )}
                      </Box>
                    </Box>
                  </Box>
                </CardContent>
              </Card>

              {/* 알림 설정 */}
              <Card sx={{ 
                backgroundColor: '#1A1F2E',
                border: '1px solid #2A3441',
              }}>
                <CardContent>
                  <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold', mb: 2 }}>
                    <Notifications sx={{ mr: 1, verticalAlign: 'middle' }} />
                    알림 설정
                  </Typography>

                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <FormControlLabel
                      control={
                        <Switch
                          checked={profile.settings.emailNotifications}
                          onChange={(e) => handleSettingChange('emailNotifications', e.target.checked)}
                          sx={{
                            '& .MuiSwitch-switchBase.Mui-checked': {
                              color: '#2196F3',
                            },
                            '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                              backgroundColor: '#2196F3',
                            },
                          }}
                        />
                      }
                      label={
                        <Box>
                          <Typography variant="body2" sx={{ color: '#FFFFFF' }}>
                            이메일 알림
                          </Typography>
                          <Typography variant="caption" sx={{ color: '#78828A' }}>
                            중요한 업데이트를 이메일로 받습니다
                          </Typography>
                        </Box>
                      }
                    />

                    <FormControlLabel
                      control={
                        <Switch
                          checked={profile.settings.marketAlerts}
                          onChange={(e) => handleSettingChange('marketAlerts', e.target.checked)}
                          sx={{
                            '& .MuiSwitch-switchBase.Mui-checked': {
                              color: '#2196F3',
                            },
                            '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                              backgroundColor: '#2196F3',
                            },
                          }}
                        />
                      }
                      label={
                        <Box>
                          <Typography variant="body2" sx={{ color: '#FFFFFF' }}>
                            시장 알림
                          </Typography>
                          <Typography variant="caption" sx={{ color: '#78828A' }}>
                            주요 시장 이벤트 알림을 받습니다
                          </Typography>
                        </Box>
                      }
                    />

                    <FormControlLabel
                      control={
                        <Switch
                          checked={profile.settings.communityUpdates}
                          onChange={(e) => handleSettingChange('communityUpdates', e.target.checked)}
                          sx={{
                            '& .MuiSwitch-switchBase.Mui-checked': {
                              color: '#2196F3',
                            },
                            '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                              backgroundColor: '#2196F3',
                            },
                          }}
                        />
                      }
                      label={
                        <Box>
                          <Typography variant="body2" sx={{ color: '#FFFFFF' }}>
                            커뮤니티 업데이트
                          </Typography>
                          <Typography variant="caption" sx={{ color: '#78828A' }}>
                            새 댓글 및 게시글 알림을 받습니다
                          </Typography>
                        </Box>
                      }
                    />
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            {/* 사이드바 */}
            <Grid item xs={12} md={4}>
              {/* 계정 상태 */}
              <Card sx={{ 
                backgroundColor: '#1A1F2E',
                border: '1px solid #2A3441',
                mb: 3,
              }}>
                <CardContent>
                  <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold', mb: 2 }}>
                    계정 상태
                  </Typography>

                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" sx={{ color: '#78828A' }}>
                        계정 유형
                      </Typography>
                      <Chip label="프리미엄" color="primary" size="small" />
                    </Box>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" sx={{ color: '#78828A' }}>
                        인증 상태
                      </Typography>
                      <Chip label="인증 완료" color="success" size="small" />
                    </Box>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" sx={{ color: '#78828A' }}>
                        가입일
                      </Typography>
                      <Typography variant="body2" sx={{ color: '#FFFFFF' }}>
                        {formatDate(profile.joinedAt)}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>

              {/* 설정 */}
              <Card sx={{ 
                backgroundColor: '#1A1F2E',
                border: '1px solid #2A3441',
                mb: 3,
              }}>
                <CardContent>
                  <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold', mb: 2 }}>
                    <Settings sx={{ mr: 1, verticalAlign: 'middle' }} />
                    환경 설정
                  </Typography>

                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Language sx={{ color: '#78828A', fontSize: 20 }} />
                        <Typography variant="body2" sx={{ color: '#78828A' }}>
                          언어
                        </Typography>
                      </Box>
                      <Typography variant="body2" sx={{ color: '#FFFFFF' }}>
                        한국어
                      </Typography>
                    </Box>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Security sx={{ color: '#78828A', fontSize: 20 }} />
                        <Typography variant="body2" sx={{ color: '#78828A' }}>
                          테마
                        </Typography>
                      </Box>
                      <Typography variant="body2" sx={{ color: '#FFFFFF' }}>
                        다크 모드
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>

              {/* 위험 영역 */}
              <Card sx={{ 
                backgroundColor: '#1A1F2E',
                border: '1px solid #F44336',
              }}>
                <CardContent>
                  <Typography variant="h6" sx={{ color: '#F44336', fontWeight: 'bold', mb: 2 }}>
                    위험 영역
                  </Typography>

                  <Typography variant="body2" sx={{ color: '#78828A', mb: 3 }}>
                    이 작업들은 되돌릴 수 없습니다. 신중히 결정해주세요.
                  </Typography>

                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Button
                      variant="outlined"
                      fullWidth
                      sx={{
                        color: '#F44336',
                        borderColor: '#F44336',
                        '&:hover': {
                          backgroundColor: 'rgba(244, 67, 54, 0.08)',
                        }
                      }}
                    >
                      비밀번호 변경
                    </Button>

                    <Button
                      variant="outlined"
                      fullWidth
                      onClick={logout}
                      sx={{
                        color: '#FF9800',
                        borderColor: '#FF9800',
                        '&:hover': {
                          backgroundColor: 'rgba(255, 152, 0, 0.08)',
                        }
                      }}
                    >
                      로그아웃
                    </Button>

                    <Button
                      variant="outlined"
                      fullWidth
                      sx={{
                        color: '#F44336',
                        borderColor: '#F44336',
                        '&:hover': {
                          backgroundColor: 'rgba(244, 67, 54, 0.08)',
                        }
                      }}
                    >
                      계정 삭제
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </Box>
      </Container>
    </Box>
  );
}