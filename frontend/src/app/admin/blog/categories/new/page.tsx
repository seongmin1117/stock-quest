'use client';

import React, { useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  FormControlLabel,
  Checkbox,
  Alert,
} from '@mui/material';
import { Save, Cancel } from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import {
  blogAdminApi,
  CreateCategoryRequest,
  blogAdminUtils,
  handleBlogAdminError
} from '@/shared/api/blog-admin-client';

export default function CreateCategoryPage() {
  const router = useRouter();

  // Form state
  const [formData, setFormData] = useState<CreateCategoryRequest>({
    name: '',
    description: '',
    slug: '',
    showOnHomepage: false,
  });

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  const handleInputChange = (field: keyof CreateCategoryRequest) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const target = event.target as HTMLInputElement;
    const value = target.type === 'checkbox' ? target.checked : target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));

    // Auto-generate slug from name
    if (field === 'name' && typeof value === 'string') {
      const generatedSlug = blogAdminUtils.generateSlug(value);
      setFormData(prev => ({
        ...prev,
        slug: generatedSlug
      }));
    }
  };

  const handleSubmit = async () => {
    try {
      setLoading(true);
      setError(null);
      setValidationErrors([]);

      // Validate form data
      const errors = blogAdminUtils.validateCategory(formData);
      if (errors.length > 0) {
        setValidationErrors(errors);
        return;
      }

      // Create category
      await blogAdminApi.createCategory(formData);

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
          새 카테고리 생성
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
            카테고리 정보
          </Typography>

          <TextField
            fullWidth
            label="카테고리 이름 *"
            value={formData.name}
            onChange={handleInputChange('name')}
            margin="normal"
            placeholder="예: 주식 투자 기초"
          />

          <TextField
            fullWidth
            label="슬러그"
            value={formData.slug}
            onChange={handleInputChange('slug')}
            margin="normal"
            placeholder="URL에 사용될 슬러그 (자동 생성됨)"
            helperText="URL에 표시될 고유 식별자입니다"
          />

          <TextField
            fullWidth
            label="설명"
            value={formData.description}
            onChange={handleInputChange('description')}
            margin="normal"
            multiline
            rows={3}
            placeholder="카테고리에 대한 설명을 입력하세요"
            helperText="SEO와 사용자 이해를 위한 설명입니다"
          />

          <FormControlLabel
            control={
              <Checkbox
                checked={formData.showOnHomepage}
                onChange={handleInputChange('showOnHomepage')}
              />
            }
            label="홈페이지에 노출"
            sx={{ mt: 2 }}
          />

          <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
            * 표시된 필드는 필수 입력 항목입니다.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}