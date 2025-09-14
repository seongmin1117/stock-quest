'use client';

import React, { useState, useEffect } from 'react';
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
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
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
  Info
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';

interface UserFormData {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'USER';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  isEmailVerified: boolean;
  allowTrading: boolean;
  allowDataExport: boolean;
  maxChallenges: number;
  notes: string;
}

const mockUser: UserFormData = {
  id: 1,
  username: 'john_investor',
  email: 'john@example.com',
  fullName: '김투자',
  role: 'USER',
  status: 'ACTIVE',
  isEmailVerified: true,
  allowTrading: true,
  allowDataExport: false,
  maxChallenges: 10,
  notes: ''
};

export default function EditUserPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const [formData, setFormData] = useState<UserFormData>(mockUser);
  const [originalData, setOriginalData] = useState<UserFormData>(mockUser);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    title: string;
    message: string;
    action?: () => void;
  }>({ open: false, title: '', message: '' });

  useEffect(() => {
    const loadUser = async () => {
      try {
        // TODO: 실제 API 호출
        await new Promise(resolve => setTimeout(resolve, 500));
        setFormData(mockUser);
        setOriginalData(mockUser);
      } catch (error) {
        setError('사용자 정보를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, [params.id]);

  const handleInputChange = (field: keyof UserFormData, value: any) => {
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

  const handleSave = async () => {
    try {
      setSaving(true);
      setError(null);

      // 유효성 검사
      if (!formData.fullName.trim()) {
        setError('사용자 이름을 입력해주세요.');
        return;
      }
      if (!formData.email.trim()) {
        setError('이메일을 입력해주세요.');
        return;
      }
      if (!formData.username.trim()) {
        setError('사용자명을 입력해주세요.');
        return;
      }

      // 이메일 형식 검증
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        setError('올바른 이메일 형식을 입력해주세요.');
        return;
      }

      // TODO: 실제 API 호출
      await new Promise(resolve => setTimeout(resolve, 1000));

      setSuccess('사용자 정보가 성공적으로 수정되었습니다.');
      setOriginalData(formData);

      // 2초 후 상세 페이지로 이동
      setTimeout(() => {
        router.push(`/admin/users/${params.id}`);
      }, 2000);

    } catch (err) {
      setError('사용자 정보 수정 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    // 변경사항이 있으면 확인 다이얼로그 표시
    if (JSON.stringify(formData) !== JSON.stringify(originalData)) {
      setConfirmDialog({
        open: true,
        title: '변경사항 취소',
        message: '변경된 내용이 저장되지 않습니다. 정말 취소하시겠습니까?',
        action: () => {
          router.push(`/admin/users/${params.id}`);
        }
      });
    } else {
      router.push(`/admin/users/${params.id}`);
    }
  };

  const handleResetPassword = () => {
    setConfirmDialog({
      open: true,
      title: '비밀번호 재설정',
      message: '사용자에게 비밀번호 재설정 이메일을 발송하시겠습니까?',
      action: () => {
        // TODO: 비밀번호 재설정 API 호출
        console.log('Password reset for user:', formData.id);
        setSuccess('비밀번호 재설정 이메일이 발송되었습니다.');
        setConfirmDialog({ open: false, title: '', message: '' });
      }
    });
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <Typography>사용자 정보를 불러오는 중...</Typography>
      </Box>
    );
  }

  return (
    <Box>
      {/* 헤더 */}
      <Box mb={4}>
        <Button
          variant="text"
          startIcon={<ArrowBack />}
          onClick={() => router.push(`/admin/users/${params.id}`)}
          sx={{ mb: 2 }}
        >
          사용자 상세로
        </Button>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
          사용자 정보 수정
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {formData.fullName}님의 계정 정보를 수정하세요
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
                        {formData.fullName[0]}
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
                            프로필 사진 변경
                          </Button>
                        </label>
                        {avatarFile && (
                          <Typography variant="caption" color="success.main" sx={{ display: 'block', mt: 1 }}>
                            새 프로필 사진: {avatarFile.name}
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
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="사용자명"
                      value={formData.username}
                      onChange={(e) => handleInputChange('username', e.target.value)}
                      required
                      disabled // 일반적으로 username은 변경 불가
                      helperText="사용자명은 변경할 수 없습니다"
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
                      <InputLabel>상태</InputLabel>
                      <Select
                        value={formData.status}
                        onChange={(e) => handleInputChange('status', e.target.value)}
                        label="상태"
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
                        <MenuItem value="SUSPENDED">
                          <Box display="flex" alignItems="center" gap={1}>
                            <Warning color="error" />
                            정지
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
                      secondary="이메일이 인증된 계정입니다"
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
                  rows={4}
                  placeholder="이 사용자에 대한 관리자 노트를 작성하세요..."
                  value={formData.notes}
                  onChange={(e) => handleInputChange('notes', e.target.value)}
                  helperText="이 노트는 관리자만 볼 수 있습니다"
                />
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={4}>
            {/* 액션 버튼 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  저장
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
                    {saving ? '저장 중...' : '변경사항 저장'}
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
                  변경된 내용은 즉시 적용됩니다.
                </Typography>
              </CardContent>
            </Card>

            {/* 계정 관리 */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  계정 관리
                </Typography>

                <Box display="flex" flexDirection="column" gap={2}>
                  <Button
                    variant="outlined"
                    color="warning"
                    onClick={handleResetPassword}
                    fullWidth
                  >
                    비밀번호 재설정 이메일 발송
                  </Button>
                </Box>

                <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                  사용자에게 비밀번호 재설정 링크가 포함된 이메일을 발송합니다.
                </Typography>
              </CardContent>
            </Card>

            {/* 위험 작업 */}
            <Card sx={{ border: '1px solid', borderColor: 'error.main' }}>
              <CardContent>
                <Typography variant="h6" gutterBottom color="error.main">
                  위험 작업
                </Typography>

                <Box display="flex" flexDirection="column" gap={2}>
                  <Button
                    variant="outlined"
                    color="error"
                    onClick={() => router.push(`/admin/users/${params.id}`)}
                    fullWidth
                  >
                    계정 정지
                  </Button>

                  <Button
                    variant="contained"
                    color="error"
                    onClick={() => router.push(`/admin/users/${params.id}`)}
                    fullWidth
                  >
                    계정 삭제
                  </Button>
                </Box>

                <Typography variant="caption" color="error.main" sx={{ mt: 2, display: 'block' }}>
                  이러한 작업은 되돌릴 수 없습니다. 신중히 결정하세요.
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </form>

      {/* 확인 다이얼로그 */}
      <Dialog
        open={confirmDialog.open}
        onClose={() => setConfirmDialog({ open: false, title: '', message: '' })}
      >
        <DialogTitle>{confirmDialog.title}</DialogTitle>
        <DialogContent>
          <Typography>{confirmDialog.message}</Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setConfirmDialog({ open: false, title: '', message: '' })}
          >
            취소
          </Button>
          <Button
            onClick={confirmDialog.action}
            color="primary"
            variant="contained"
          >
            확인
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}