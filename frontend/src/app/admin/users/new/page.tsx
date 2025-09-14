'use client';

import React, { useState } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Avatar,
  Alert,
  Switch,
  FormControlLabel,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  IconButton,
  InputAdornment
} from '@mui/material';
import {
  ArrowBack,
  Save,
  Cancel,
  PhotoCamera,
  Email,
  Person,
  AdminPanelSettings,
  Security,
  Warning,
  Info,
  Visibility,
  VisibilityOff,
  PersonAdd
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';

interface NewUserFormData {
  username: string;
  email: string;
  fullName: string;
  password: string;
  confirmPassword: string;
  role: 'ADMIN' | 'USER';
  status: 'ACTIVE' | 'INACTIVE';
  sendWelcomeEmail: boolean;
  requirePasswordChange: boolean;
  isEmailVerified: boolean;
  allowTrading: boolean;
  allowDataExport: boolean;
  maxChallenges: number;
  notes: string;
}

const initialFormData: NewUserFormData = {
  username: '',
  email: '',
  fullName: '',
  password: '',
  confirmPassword: '',
  role: 'USER',
  status: 'ACTIVE',
  sendWelcomeEmail: true,
  requirePasswordChange: true,
  isEmailVerified: false,
  allowTrading: true,
  allowDataExport: false,
  maxChallenges: 10,
  notes: ''
};

export default function NewUserPage() {
  const router = useRouter();
  const [formData, setFormData] = useState<NewUserFormData>(initialFormData);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [avatarFile, setAvatarFile] = useState<File | null>(null);

  const handleInputChange = (field: keyof NewUserFormData, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleAvatarChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setAvatarFile(file);
    }
  };

  const generateRandomPassword = () => {
    const length = 12;
    const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*';
    let password = '';
    for (let i = 0; i < length; i++) {
      password += charset.charAt(Math.floor(Math.random() * charset.length));
    }
    setFormData(prev => ({
      ...prev,
      password: password,
      confirmPassword: password
    }));
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError(null);

      // 유효성 검사
      if (!formData.fullName.trim()) {
        setError('사용자 이름을 입력해주세요.');
        return;
      }
      if (!formData.username.trim()) {
        setError('사용자명을 입력해주세요.');
        return;
      }
      if (!formData.email.trim()) {
        setError('이메일을 입력해주세요.');
        return;
      }
      if (!formData.password) {
        setError('비밀번호를 입력해주세요.');
        return;
      }
      if (formData.password !== formData.confirmPassword) {
        setError('비밀번호가 일치하지 않습니다.');
        return;
      }

      // 이메일 형식 검증
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        setError('올바른 이메일 형식을 입력해주세요.');
        return;
      }

      // 사용자명 형식 검증 (영문, 숫자, 언더스코어만 허용)
      const usernameRegex = /^[a-zA-Z0-9_]+$/;
      if (!usernameRegex.test(formData.username)) {
        setError('사용자명은 영문, 숫자, 언더스코어(_)만 사용할 수 있습니다.');
        return;
      }

      // 비밀번호 강도 검증
      if (formData.password.length < 8) {
        setError('비밀번호는 최소 8자 이상이어야 합니다.');
        return;
      }

      // TODO: 실제 API 호출
      await new Promise(resolve => setTimeout(resolve, 1500));

      setSuccess('새 사용자가 성공적으로 생성되었습니다.');

      // 2초 후 사용자 목록으로 이동
      setTimeout(() => {
        router.push('/admin/users');
      }, 2000);

    } catch (err) {
      setError('사용자 생성 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    router.push('/admin/users');
  };

  const handleLoadExample = (type: 'regular' | 'admin' | 'test') => {
    const examples = {
      regular: {
        username: 'new_user',
        email: 'newuser@example.com',
        fullName: '새로운사용자',
        role: 'USER' as const,
        status: 'ACTIVE' as const,
        allowTrading: true,
        allowDataExport: false,
        maxChallenges: 10,
        notes: '일반 사용자 계정입니다.'
      },
      admin: {
        username: 'admin_user',
        email: 'admin@stockquest.com',
        fullName: '관리자',
        role: 'ADMIN' as const,
        status: 'ACTIVE' as const,
        allowTrading: false,
        allowDataExport: true,
        maxChallenges: 0,
        notes: '시스템 관리자 계정입니다.'
      },
      test: {
        username: 'test_user',
        email: 'test@example.com',
        fullName: '테스트사용자',
        role: 'USER' as const,
        status: 'INACTIVE' as const,
        allowTrading: true,
        allowDataExport: true,
        maxChallenges: 5,
        notes: '테스트용 계정입니다.'
      }
    };

    const example = examples[type];
    setFormData(prev => ({
      ...prev,
      ...example,
      password: '',
      confirmPassword: '',
      sendWelcomeEmail: prev.sendWelcomeEmail,
      requirePasswordChange: prev.requirePasswordChange,
      isEmailVerified: prev.isEmailVerified
    }));
  };

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Button
          variant="text"
          startIcon={<ArrowBack />}
          onClick={() => router.push('/admin/users')}
          sx={{ mb: 2 }}
        >
          사용자 목록으로
        </Button>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          새 사용자 추가
        </Typography>
        <Typography variant="body1" color="text.secondary">
          시스템에 새로운 사용자를 추가하세요
        </Typography>
      </Box>

      {/* 알림 메시지 */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}

      {/* 예시 사용자 로드 */}
      <Card sx={{ mb: 3, bgcolor: 'info.light', color: 'info.contrastText' }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            빠른 시작
          </Typography>
          <Typography variant="body2" sx={{ mb: 2 }}>
            예시 사용자 정보를 로드하여 빠르게 시작하세요
          </Typography>
          <Box display="flex" gap={1} flexWrap="wrap">
            <Button
              variant="contained"
              size="small"
              onClick={() => handleLoadExample('regular')}
              sx={{ bgcolor: 'white', color: 'info.main', '&:hover': { bgcolor: 'grey.100' } }}
            >
              일반 사용자 예시
            </Button>
            <Button
              variant="contained"
              size="small"
              onClick={() => handleLoadExample('admin')}
              sx={{ bgcolor: 'white', color: 'info.main', '&:hover': { bgcolor: 'grey.100' } }}
            >
              관리자 예시
            </Button>
            <Button
              variant="contained"
              size="small"
              onClick={() => handleLoadExample('test')}
              sx={{ bgcolor: 'white', color: 'info.main', '&:hover': { bgcolor: 'grey.100' } }}
            >
              테스트 사용자 예시
            </Button>
          </Box>
        </CardContent>
      </Card>

      <form onSubmit={(e) => { e.preventDefault(); handleSave(); }}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            {/* 기본 정보 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  기본 정보
                </Typography>

                <Grid container spacing={3}>
                  <Grid item xs={12}>
                    <Box display="flex" alignItems="center" gap={3}>
                      <Avatar
                        sx={{
                          width: 80,
                          height: 80,
                          bgcolor: formData.role === 'ADMIN' ? 'error.main' : 'primary.main'
                        }}
                      >
                        <PersonAdd sx={{ fontSize: 40 }} />
                      </Avatar>
                      <Box>
                        <input
                          accept="image/*"
                          style={{ display: 'none' }}
                          id="avatar-upload"
                          type="file"
                          onChange={handleAvatarChange}
                        />
                        <label htmlFor="avatar-upload">
                          <Button
                            variant="outlined"
                            component="span"
                            startIcon={<PhotoCamera />}
                            size="small"
                          >
                            프로필 사진 선택
                          </Button>
                        </label>
                        {avatarFile && (
                          <Typography variant="caption" color="success.main" sx={{ display: 'block', mt: 1 }}>
                            선택된 파일: {avatarFile.name}
                          </Typography>
                        )}
                      </Box>
                    </Box>
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="사용자 이름"
                      value={formData.fullName}
                      onChange={(e) => handleInputChange('fullName', e.target.value)}
                      required
                      InputProps={{
                        startAdornment: <Person sx={{ mr: 1, color: 'text.secondary' }} />
                      }}
                      placeholder="김투자"
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="사용자명"
                      value={formData.username}
                      onChange={(e) => handleInputChange('username', e.target.value)}
                      required
                      helperText="영문, 숫자, 언더스코어(_)만 사용 가능"
                      placeholder="john_investor"
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="이메일"
                      type="email"
                      value={formData.email}
                      onChange={(e) => handleInputChange('email', e.target.value)}
                      required
                      InputProps={{
                        startAdornment: <Email sx={{ mr: 1, color: 'text.secondary' }} />
                      }}
                      placeholder="user@example.com"
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* 비밀번호 설정 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6">
                    비밀번호 설정
                  </Typography>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={generateRandomPassword}
                  >
                    자동 생성
                  </Button>
                </Box>

                <Grid container spacing={3}>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="비밀번호"
                      type={showPassword ? 'text' : 'password'}
                      value={formData.password}
                      onChange={(e) => handleInputChange('password', e.target.value)}
                      required
                      InputProps={{
                        endAdornment: (
                          <InputAdornment position="end">
                            <IconButton
                              onClick={() => setShowPassword(!showPassword)}
                              edge="end"
                            >
                              {showPassword ? <VisibilityOff /> : <Visibility />}
                            </IconButton>
                          </InputAdornment>
                        )
                      }}
                      helperText="최소 8자 이상"
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="비밀번호 확인"
                      type={showConfirmPassword ? 'text' : 'password'}
                      value={formData.confirmPassword}
                      onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
                      required
                      InputProps={{
                        endAdornment: (
                          <InputAdornment position="end">
                            <IconButton
                              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                              edge="end"
                            >
                              {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                            </IconButton>
                          </InputAdornment>
                        )
                      }}
                      error={formData.confirmPassword !== '' && formData.password !== formData.confirmPassword}
                      helperText={
                        formData.confirmPassword !== '' && formData.password !== formData.confirmPassword
                          ? '비밀번호가 일치하지 않습니다'
                          : ''
                      }
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* 계정 설정 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  계정 설정
                </Typography>

                <Grid container spacing={3}>
                  <Grid item xs={12} sm={6}>
                    <FormControl fullWidth>
                      <InputLabel>역할</InputLabel>
                      <Select
                        value={formData.role}
                        onChange={(e) => handleInputChange('role', e.target.value)}
                        label="역할"
                        startAdornment={
                          formData.role === 'ADMIN' ?
                            <AdminPanelSettings sx={{ mr: 1, color: 'error.main' }} /> :
                            <Person sx={{ mr: 1, color: 'primary.main' }} />
                        }
                      >
                        <MenuItem value="USER">
                          <Box display="flex" alignItems="center" gap={1}>
                            <Person color="primary" />
                            사용자
                          </Box>
                        </MenuItem>
                        <MenuItem value="ADMIN">
                          <Box display="flex" alignItems="center" gap={1}>
                            <AdminPanelSettings color="error" />
                            관리자
                          </Box>
                        </MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <FormControl fullWidth>
                      <InputLabel>초기 상태</InputLabel>
                      <Select
                        value={formData.status}
                        onChange={(e) => handleInputChange('status', e.target.value)}
                        label="초기 상태"
                      >
                        <MenuItem value="ACTIVE">
                          <Box display="flex" alignItems="center" gap={1}>
                            <Security color="success" />
                            활성
                          </Box>
                        </MenuItem>
                        <MenuItem value="INACTIVE">
                          <Box display="flex" alignItems="center" gap={1}>
                            <Person color="disabled" />
                            비활성
                          </Box>
                        </MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="최대 챌린지 참여 수"
                      type="number"
                      value={formData.maxChallenges}
                      onChange={(e) => handleInputChange('maxChallenges', parseInt(e.target.value) || 0)}
                      helperText="0은 무제한을 의미합니다"
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>

            {/* 권한 설정 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  권한 설정
                </Typography>

                <List>
                  <ListItem>
                    <ListItemIcon>
                      <Email color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="이메일 인증됨"
                      secondary="계정 생성시 이메일이 인증된 상태로 설정"
                    />
                    <FormControlLabel
                      control={
                        <Switch
                          checked={formData.isEmailVerified}
                          onChange={(e) => handleInputChange('isEmailVerified', e.target.checked)}
                        />
                      }
                      label=""
                    />
                  </ListItem>

                  <ListItem>
                    <ListItemIcon>
                      <Security color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="거래 허용"
                      secondary="챌린지에서 주식 거래를 할 수 있습니다"
                    />
                    <FormControlLabel
                      control={
                        <Switch
                          checked={formData.allowTrading}
                          onChange={(e) => handleInputChange('allowTrading', e.target.checked)}
                        />
                      }
                      label=""
                    />
                  </ListItem>

                  <ListItem>
                    <ListItemIcon>
                      <Info color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="데이터 내보내기 허용"
                      secondary="거래 내역 및 포트폴리오 데이터를 내보낼 수 있습니다"
                    />
                    <FormControlLabel
                      control={
                        <Switch
                          checked={formData.allowDataExport}
                          onChange={(e) => handleInputChange('allowDataExport', e.target.checked)}
                        />
                      }
                      label=""
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>

            {/* 관리자 노트 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  관리자 노트
                </Typography>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  placeholder="이 사용자에 대한 관리자 노트를 작성하세요..."
                  value={formData.notes}
                  onChange={(e) => handleInputChange('notes', e.target.value)}
                  helperText="이 노트는 관리자만 볼 수 있습니다"
                />
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            {/* 생성 옵션 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  생성 옵션
                </Typography>

                <List>
                  <ListItem>
                    <ListItemIcon>
                      <Email color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="환영 이메일 발송"
                      secondary="생성 후 환영 이메일을 발송합니다"
                    />
                    <FormControlLabel
                      control={
                        <Switch
                          checked={formData.sendWelcomeEmail}
                          onChange={(e) => handleInputChange('sendWelcomeEmail', e.target.checked)}
                        />
                      }
                      label=""
                    />
                  </ListItem>

                  <ListItem>
                    <ListItemIcon>
                      <Security color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="첫 로그인시 비밀번호 변경 요구"
                      secondary="보안을 위해 첫 로그인시 비밀번호 변경"
                    />
                    <FormControlLabel
                      control={
                        <Switch
                          checked={formData.requirePasswordChange}
                          onChange={(e) => handleInputChange('requirePasswordChange', e.target.checked)}
                        />
                      }
                      label=""
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>

            {/* 액션 버튼 */}
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  생성
                </Typography>

                <Box display="flex" flexDirection="column" gap={2}>
                  <Button
                    variant="contained"
                    startIcon={<Save />}
                    onClick={handleSave}
                    disabled={saving}
                    fullWidth
                    size="large"
                  >
                    {saving ? '생성 중...' : '사용자 생성'}
                  </Button>

                  <Button
                    variant="outlined"
                    startIcon={<Cancel />}
                    onClick={handleCancel}
                    disabled={saving}
                    fullWidth
                  >
                    취소
                  </Button>
                </Box>

                <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                  생성된 사용자는 즉시 시스템에 추가됩니다.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
}