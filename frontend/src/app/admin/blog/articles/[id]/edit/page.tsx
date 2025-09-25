'use client';

import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  TextField,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Chip,
  Alert,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Autocomplete,
  Paper,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  ExpandMore,
  Save,
  Preview,
  Cancel,
  Delete,
  Publish,
  UnpublishedOutlined,
} from '@mui/icons-material';
import { useParams, useRouter } from 'next/navigation';
import {
  blogAdminApi,
  UpdateArticleRequest,
  blogAdminUtils,
  handleBlogAdminError
} from '@/shared/api/blog-admin-client';
import {
  Article,
  Category,
  Tag,
  ArticleStatus,
  ArticleDifficulty,
  blogApi
} from '@/shared/api/blog-client';

export default function EditArticlePage() {
  const params = useParams();
  const router = useRouter();
  const articleId = parseInt(params.id as string);

  // Original data
  const [originalArticle, setOriginalArticle] = useState<Article | null>(null);

  // Form state
  const [formData, setFormData] = useState<UpdateArticleRequest>({});

  // Supporting data
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [selectedTags, setSelectedTags] = useState<Tag[]>([]);

  // UI state
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [showSeoFields, setShowSeoFields] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    if (articleId) {
      loadArticleData();
      loadSupportingData();
    }
  }, [articleId]);

  const loadArticleData = async () => {
    try {
      setLoading(true);
      const article = await blogAdminApi.getArticleById(articleId);
      setOriginalArticle(article);

      // Initialize form with existing data
      setFormData({
        title: article.title,
        content: article.content,
        summary: article.summary,
        categoryId: article.categoryId,
        tagIds: article.tags.map(tag => tag.id),
        status: article.status,
        featured: article.featured,
        difficulty: article.difficulty,
        seoTitle: article.seoTitle,
        metaDescription: article.metaDescription,
        seoKeywords: article.seoKeywords,
        canonicalUrl: article.canonicalUrl,
        ogTitle: article.ogTitle,
        ogDescription: article.ogDescription,
        ogImageUrl: article.ogImageUrl,
        twitterTitle: article.twitterTitle,
        twitterDescription: article.twitterDescription,
        twitterImageUrl: article.twitterImageUrl,
        indexable: article.indexable,
        followable: article.followable,
        schemaType: article.schemaType,
      });

      // Set selected tags
      setSelectedTags(article.tags);

      // Show SEO fields if any SEO data exists
      const hasSeoData = !!(article.seoTitle || article.metaDescription || article.seoKeywords ||
                        article.ogTitle || article.ogDescription || article.twitterTitle);
      setShowSeoFields(hasSeoData);

    } catch (err) {
      setError('게시글을 불러오는 중 오류가 발생했습니다.');
      console.error('Error loading article:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadSupportingData = async () => {
    try {
      const [categoriesData, tagsData] = await Promise.all([
        blogApi.getAllCategories(),
        blogApi.getPopularTags(100),
      ]);
      setCategories(categoriesData);
      setTags(tagsData);
    } catch (err) {
      console.error('Error loading supporting data:', err);
    }
  };

  const handleInputChange = (field: keyof UpdateArticleRequest) => (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement> | any
  ) => {
    const value = event.target.type === 'checkbox' ? event.target.checked : event.target.value;
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleTagsChange = (event: any, newValue: Tag[]) => {
    setSelectedTags(newValue);
    setFormData(prev => ({
      ...prev,
      tagIds: newValue.map(tag => tag.id)
    }));
  };

  const handleSubmit = async (publishImmediately = false) => {
    try {
      setSaving(true);
      setError(null);
      setValidationErrors([]);

      // Set status based on action
      const submitData = {
        ...formData,
        status: publishImmediately ? ArticleStatus.PUBLISHED : formData.status
      };

      // Validate form data
      const errors = blogAdminUtils.validateArticle(submitData);
      if (errors.length > 0) {
        setValidationErrors(errors);
        return;
      }

      // Update article
      const updatedArticle = await blogAdminApi.updateArticle(articleId, submitData);
      setOriginalArticle(updatedArticle);

      // Show success message and redirect
      router.push('/admin/blog');

    } catch (err) {
      const blogError = handleBlogAdminError(err);
      setError(blogError.message);
      if (blogError.fieldErrors) {
        const fieldErrorMessages = Object.values(blogError.fieldErrors).flat();
        setValidationErrors(fieldErrorMessages);
      }
    } finally {
      setSaving(false);
    }
  };

  const handlePublishToggle = async () => {
    if (!originalArticle) return;

    try {
      setSaving(true);
      const updatedArticle = originalArticle.status === ArticleStatus.PUBLISHED
        ? await blogAdminApi.unpublishArticle(articleId)
        : await blogAdminApi.publishArticle(articleId);

      setOriginalArticle(updatedArticle);
      setFormData(prev => ({ ...prev, status: updatedArticle.status }));
    } catch (err) {
      setError('게시 상태 변경 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    try {
      await blogAdminApi.deleteArticle(articleId);
      router.push('/admin/blog');
    } catch (err) {
      setError('게시글 삭제 중 오류가 발생했습니다.');
    }
    setDeleteDialogOpen(false);
  };

  const handleCancel = () => {
    router.push('/admin/blog');
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Typography>게시글을 불러오는 중...</Typography>
      </Box>
    );
  }

  if (!originalArticle) {
    return (
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Typography variant="h6" color="error">
          게시글을 찾을 수 없습니다.
        </Typography>
        <Button onClick={() => router.push('/admin/blog')} sx={{ mt: 2 }}>
          게시글 목록으로 돌아가기
        </Button>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" component="h1">
            게시글 편집
          </Typography>
          <Typography variant="body2" color="text.secondary">
            ID: {articleId} | 작성일: {new Date(originalArticle.createdAt).toLocaleDateString('ko-KR')}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            variant="outlined"
            startIcon={<Delete />}
            color="error"
            onClick={() => setDeleteDialogOpen(true)}
            disabled={saving}
          >
            삭제
          </Button>
          <Button
            variant="outlined"
            startIcon={originalArticle.status === ArticleStatus.PUBLISHED ? <UnpublishedOutlined /> : <Publish />}
            onClick={handlePublishToggle}
            disabled={saving}
            color={originalArticle.status === ArticleStatus.PUBLISHED ? 'warning' : 'success'}
          >
            {originalArticle.status === ArticleStatus.PUBLISHED ? '게시 취소' : '게시하기'}
          </Button>
          <Button
            variant="outlined"
            startIcon={<Cancel />}
            onClick={handleCancel}
            disabled={saving}
          >
            취소
          </Button>
          <Button
            variant="contained"
            startIcon={<Save />}
            onClick={() => handleSubmit(false)}
            disabled={saving}
          >
            {saving ? '저장 중...' : '저장'}
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

      <Grid container spacing={3}>
        {/* Main Content */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                기본 정보
              </Typography>

              <TextField
                fullWidth
                label="제목 *"
                value={formData.title || ''}
                onChange={handleInputChange('title')}
                margin="normal"
                placeholder="게시글 제목을 입력하세요"
              />

              <TextField
                fullWidth
                label="요약"
                value={formData.summary || ''}
                onChange={handleInputChange('summary')}
                margin="normal"
                multiline
                rows={3}
                placeholder="게시글 요약을 입력하세요 (검색 결과에 표시됩니다)"
                helperText="SEO에 중요한 요약 설명입니다"
              />

              <TextField
                fullWidth
                label="내용 *"
                value={formData.content || ''}
                onChange={handleInputChange('content')}
                margin="normal"
                multiline
                rows={15}
                placeholder="게시글 내용을 입력하세요. Markdown 형식을 지원합니다."
              />
            </CardContent>
          </Card>

          {/* SEO Settings */}
          <Card sx={{ mt: 3 }}>
            <Accordion expanded={showSeoFields} onChange={() => setShowSeoFields(!showSeoFields)}>
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Typography variant="h6">
                  SEO 설정 (Google AdSense 최적화)
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="SEO 제목"
                      value={formData.seoTitle || ''}
                      onChange={handleInputChange('seoTitle')}
                      placeholder="검색 엔진에 표시될 제목"
                      helperText="비워두면 기본 제목을 사용합니다"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="메타 설명"
                      value={formData.metaDescription || ''}
                      onChange={handleInputChange('metaDescription')}
                      multiline
                      rows={2}
                      placeholder="검색 결과에 표시될 설명"
                      helperText="150-160자 권장"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="SEO 키워드"
                      value={formData.seoKeywords || ''}
                      onChange={handleInputChange('seoKeywords')}
                      placeholder="키워드1, 키워드2, 키워드3"
                      helperText="쉼표로 구분하여 입력"
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="정규 URL"
                      value={formData.canonicalUrl || ''}
                      onChange={handleInputChange('canonicalUrl')}
                      placeholder="https://example.com/article"
                      helperText="중복 콘텐츠 방지"
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="스키마 타입"
                      value={formData.schemaType || ''}
                      onChange={handleInputChange('schemaType')}
                      placeholder="Article, BlogPosting, NewsArticle"
                      helperText="구조화된 데이터 타입"
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Typography variant="subtitle2" gutterBottom>
                      Open Graph 설정
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="OG 제목"
                      value={formData.ogTitle || ''}
                      onChange={handleInputChange('ogTitle')}
                      placeholder="소셜 미디어 공유 제목"
                    />
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <TextField
                      fullWidth
                      label="OG 이미지 URL"
                      value={formData.ogImageUrl || ''}
                      onChange={handleInputChange('ogImageUrl')}
                      placeholder="https://example.com/image.jpg"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="OG 설명"
                      value={formData.ogDescription || ''}
                      onChange={handleInputChange('ogDescription')}
                      multiline
                      rows={2}
                      placeholder="소셜 미디어 공유 설명"
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Typography variant="subtitle2" gutterBottom>
                      Twitter 카드 설정
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      label="Twitter 제목"
                      value={formData.twitterTitle || ''}
                      onChange={handleInputChange('twitterTitle')}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      label="Twitter 이미지"
                      value={formData.twitterImageUrl || ''}
                      onChange={handleInputChange('twitterImageUrl')}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <TextField
                      fullWidth
                      label="Twitter 설명"
                      value={formData.twitterDescription || ''}
                      onChange={handleInputChange('twitterDescription')}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', gap: 2 }}>
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={formData.indexable ?? true}
                            onChange={handleInputChange('indexable')}
                          />
                        }
                        label="검색엔진 색인 허용"
                      />
                      <FormControlLabel
                        control={
                          <Checkbox
                            checked={formData.followable ?? true}
                            onChange={handleInputChange('followable')}
                          />
                        }
                        label="링크 추적 허용"
                      />
                    </Box>
                  </Grid>
                </Grid>
              </AccordionDetails>
            </Accordion>
          </Card>
        </Grid>

        {/* Sidebar */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                게시 설정
              </Typography>

              <FormControl fullWidth margin="normal">
                <InputLabel>상태</InputLabel>
                <Select
                  value={formData.status || ArticleStatus.DRAFT}
                  onChange={handleInputChange('status')}
                  label="상태"
                >
                  <MenuItem value={ArticleStatus.DRAFT}>초안</MenuItem>
                  <MenuItem value={ArticleStatus.PUBLISHED}>게시됨</MenuItem>
                  <MenuItem value={ArticleStatus.ARCHIVED}>보관됨</MenuItem>
                </Select>
              </FormControl>

              <FormControl fullWidth margin="normal">
                <InputLabel>카테고리 *</InputLabel>
                <Select
                  value={formData.categoryId || 0}
                  onChange={handleInputChange('categoryId')}
                  label="카테고리 *"
                >
                  <MenuItem value={0}>카테고리 선택</MenuItem>
                  {categories.map((category) => (
                    <MenuItem key={category.id} value={category.id}>
                      {category.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              <FormControl fullWidth margin="normal">
                <InputLabel>난이도</InputLabel>
                <Select
                  value={formData.difficulty || ArticleDifficulty.BEGINNER}
                  onChange={handleInputChange('difficulty')}
                  label="난이도"
                >
                  <MenuItem value={ArticleDifficulty.BEGINNER}>초급</MenuItem>
                  <MenuItem value={ArticleDifficulty.INTERMEDIATE}>중급</MenuItem>
                  <MenuItem value={ArticleDifficulty.ADVANCED}>고급</MenuItem>
                </Select>
              </FormControl>

              <Autocomplete
                multiple
                id="tags-autocomplete"
                options={tags}
                getOptionLabel={(option) => option.name}
                value={selectedTags}
                onChange={handleTagsChange}
                renderTags={(tagValue, getTagProps) =>
                  tagValue.map((option, index) => (
                    <Chip
                      variant="outlined"
                      label={option.name}
                      {...getTagProps({ index })}
                      key={`tag-${index}`}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="태그"
                    placeholder="태그를 검색하고 선택하세요"
                    margin="normal"
                  />
                )}
              />

              <FormControlLabel
                control={
                  <Checkbox
                    checked={formData.featured ?? false}
                    onChange={handleInputChange('featured')}
                  />
                }
                label="추천 게시글"
                sx={{ mt: 2 }}
              />
            </CardContent>
          </Card>

          <Card sx={{ mt: 2 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                게시글 정보
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>조회수:</strong> {originalArticle.viewCount.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>좋아요:</strong> {originalArticle.likeCount.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>댓글:</strong> {originalArticle.commentCount.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>작성자:</strong> {originalArticle.authorNickname}
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                <strong>마지막 수정:</strong> {new Date(originalArticle.updatedAt).toLocaleDateString('ko-KR')}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>게시글 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            &ldquo;{originalArticle.title}&rdquo; 게시글을 정말 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>
            취소
          </Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}