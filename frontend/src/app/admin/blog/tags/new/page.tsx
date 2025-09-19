'use client';

import React, { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Alert,
} from '@mui/material';
import { Save, Cancel } from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import {
  blogAdminApi,
  CreateTagRequest,
  blogAdminUtils,
  handleBlogAdminError
} from '@/shared/api/blog-admin-client';

export default function CreateTagPage() {
  const router = useRouter();

  // Form state
  const [formData, setFormData] = useState<CreateTagRequest>({
    name: '',
  });

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  const handleInputChange = (field: keyof CreateTagRequest) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const value = event.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSubmit = async () => {
    try {
      setLoading(true);
      setError(null);
      setValidationErrors([]);

      // Validate form data
      const errors = blogAdminUtils.validateTag(formData);
      if (errors.length > 0) {
        setValidationErrors(errors);
        return;
      }

      // Create tag
      await blogAdminApi.createTag(formData);

      // Redirect back to blog management
      router.push('/admin/blog');

    } catch (err) {
      const blogError = handleBlogAdminError(err);
      setError(blogError.message);
      if (blogError.fieldErrors) {
        const fieldErrorMessages = Object.values(blogError.fieldErrors).flat();
        setValidationErrors(fieldErrorMessages);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    router.push('/admin/blog');
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          새 태그 생성
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            variant="outlined"
            startIcon={<Cancel />}
            onClick={handleCancel}
            disabled={loading}
          >
            취소
          </Button>
          <Button
            variant="contained"
            startIcon={<Save />}
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? '저장 중...' : '저장'}
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {validationErrors.length > 0 && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          <Typography variant="body2" sx={{ fontWeight: 'bold', mb: 1 }}>
            다음 사항을 확인해 주세요:
          </Typography>
          <ul style={{ margin: 0, paddingLeft: 20 }}>
            {validationErrors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        </Alert>
      )}

      <Card sx={{ maxWidth: 600 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            태그 정보
          </Typography>

          <TextField
            fullWidth
            label="태그 이름 *"
            value={formData.name}
            onChange={handleInputChange('name')}
            margin="normal"
            placeholder="예: 주식투자"
            helperText="투자 주제나 카테고리를 나타내는 키워드를 입력하세요"
          />

          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            * 표시된 필드는 필수 입력 항목입니다.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}