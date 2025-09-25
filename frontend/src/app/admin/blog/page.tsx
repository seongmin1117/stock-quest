'use client';

import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Visibility,
  Publish,
  UnpublishedOutlined,
  Analytics,
  Category,
  LocalOffer,
} from '@mui/icons-material';
import { useRouter } from 'next/navigation';
import { blogAdminApi, BlogAnalytics, blogAdminUtils } from '@/shared/api/blog-admin-client';
import { Article, Category as BlogCategory, Tag, ArticleStatus } from '@/shared/api/blog-client';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`blog-tabpanel-${index}`}
      aria-labelledby={`blog-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export default function BlogAdminPage() {
  const router = useRouter();
  const [tabValue, setTabValue] = useState(0);
  const [articles, setArticles] = useState<Article[]>([]);
  const [categories, setCategories] = useState<BlogCategory[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [analytics, setAnalytics] = useState<BlogAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Dialog states
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [articlesData, categoriesData, tagsData, analyticsData] = await Promise.all([
        blogAdminApi.getAllArticles(),
        blogAdminApi.getAllCategoriesAdmin(),
        blogAdminApi.getAllTagsAdmin(),
        blogAdminApi.getBlogAnalytics(),
      ]);

      setArticles(articlesData);
      setCategories(categoriesData);
      setTags(tagsData);
      setAnalytics(analyticsData);
    } catch (err) {
      setError('데이터를 불러오는 중 오류가 발생했습니다.');
      console.error('Error loading blog admin data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleDeleteClick = (article: Article) => {
    setSelectedArticle(article);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!selectedArticle) return;

    try {
      await blogAdminApi.deleteArticle(selectedArticle.id);
      setArticles(articles.filter(a => a.id !== selectedArticle.id));
      setDeleteDialogOpen(false);
      setSelectedArticle(null);
      loadData(); // Refresh analytics
    } catch (err) {
      setError('게시글 삭제 중 오류가 발생했습니다.');
    }
  };

  const handlePublishToggle = async (article: Article) => {
    try {
      const updatedArticle = article.status === ArticleStatus.PUBLISHED
        ? await blogAdminApi.unpublishArticle(article.id)
        : await blogAdminApi.publishArticle(article.id);

      setArticles(articles.map(a => a.id === article.id ? updatedArticle : a));
      loadData(); // Refresh analytics
    } catch (err) {
      setError('게시글 상태 변경 중 오류가 발생했습니다.');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Typography>블로그 데이터를 불러오는 중...</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          블로그 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => router.push('/admin/blog/articles/new')}
        >
          새 게시글 작성
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="blog admin tabs">
          <Tab label="대시보드" />
          <Tab label="게시글 관리" />
          <Tab label="카테고리 관리" />
          <Tab label="태그 관리" />
        </Tabs>
      </Box>

      {/* Dashboard Tab */}
      <TabPanel value={tabValue} index={0}>
        {analytics && (
          <Grid container spacing={3}>
            {/* Analytics Cards */}
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    전체 게시글
                  </Typography>
                  <Typography variant="h4">
                    {analytics.totalArticles}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    게시된 글
                  </Typography>
                  <Typography variant="h4" color="success.main">
                    {analytics.publishedArticles}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    초안
                  </Typography>
                  <Typography variant="h4" color="warning.main">
                    {analytics.draftArticles}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={3}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    총 조회수
                  </Typography>
                  <Typography variant="h4" color="primary.main">
                    {analytics.totalViews.toLocaleString()}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            {/* Popular Articles */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    인기 게시글
                  </Typography>
                  {analytics.popularArticles.map((article, index) => (
                    <Box key={article.id} sx={{ display: 'flex', justifyContent: 'space-between', py: 1 }}>
                      <Typography variant="body2" noWrap sx={{ flex: 1, mr: 2 }}>
                        {index + 1}. {article.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {article.viewCount} 조회
                      </Typography>
                    </Box>
                  ))}
                </CardContent>
              </Card>
            </Grid>

            {/* Category Distribution */}
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    카테고리별 게시글 수
                  </Typography>
                  {analytics.categoryDistribution.map((category) => (
                    <Box key={category.categoryName} sx={{ display: 'flex', justifyContent: 'space-between', py: 1 }}>
                      <Typography variant="body2">
                        {category.categoryName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {category.articleCount} 개
                      </Typography>
                    </Box>
                  ))}
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        )}
      </TabPanel>

      {/* Articles Tab */}
      <TabPanel value={tabValue} index={1}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">
            게시글 목록 ({articles.length}개)
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => router.push('/admin/blog/articles/new')}
          >
            새 게시글
          </Button>
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>제목</TableCell>
                <TableCell>카테고리</TableCell>
                <TableCell>상태</TableCell>
                <TableCell>조회수</TableCell>
                <TableCell>작성일</TableCell>
                <TableCell>작업</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {articles.map((article) => (
                <TableRow key={article.id}>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                      {article.title}
                    </Typography>
                    {article.featured && (
                      <Chip label="추천" size="small" color="secondary" sx={{ mt: 0.5 }} />
                    )}
                  </TableCell>
                  <TableCell>{article.categoryName}</TableCell>
                  <TableCell>
                    <Chip
                      label={blogAdminUtils.getStatusDisplay(article.status).label}
                      color={blogAdminUtils.getStatusDisplay(article.status).color as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{article.viewCount.toLocaleString()}</TableCell>
                  <TableCell>{formatDate(article.createdAt)}</TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      onClick={() => router.push(`/admin/blog/articles/${article.id}/edit`)}
                    >
                      <Edit />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => handlePublishToggle(article)}
                      color={article.status === ArticleStatus.PUBLISHED ? 'warning' : 'success'}
                    >
                      {article.status === ArticleStatus.PUBLISHED ? <UnpublishedOutlined /> : <Publish />}
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => handleDeleteClick(article)}
                      color="error"
                    >
                      <Delete />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </TabPanel>

      {/* Categories Tab */}
      <TabPanel value={tabValue} index={2}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">
            카테고리 목록 ({categories.length}개)
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => router.push('/admin/blog/categories/new')}
          >
            새 카테고리
          </Button>
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>이름</TableCell>
                <TableCell>설명</TableCell>
                <TableCell>게시글 수</TableCell>
                <TableCell>홈페이지 노출</TableCell>
                <TableCell>작업</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {categories.map((category) => (
                <TableRow key={category.id}>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                      {category.name}
                    </Typography>
                  </TableCell>
                  <TableCell>{category.description}</TableCell>
                  <TableCell>{category.articleCount}</TableCell>
                  <TableCell>
                    <Chip
                      label={category.featuredOnHome ? '노출' : '비노출'}
                      color={category.featuredOnHome ? 'success' : 'default'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      onClick={() => router.push(`/admin/blog/categories/${category.id}/edit`)}
                    >
                      <Edit />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </TabPanel>

      {/* Tags Tab */}
      <TabPanel value={tabValue} index={3}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">
            태그 목록 ({tags.length}개)
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => router.push('/admin/blog/tags/new')}
          >
            새 태그
          </Button>
        </Box>

        <Grid container spacing={2}>
          {tags.map((tag) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={tag.id}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Box>
                      <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                        {tag.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {tag.usageCount}번 사용됨
                      </Typography>
                    </Box>
                    <IconButton
                      size="small"
                      onClick={() => router.push(`/admin/blog/tags/${tag.id}/edit`)}
                    >
                      <Edit />
                    </IconButton>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </TabPanel>

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
            &ldquo;{selectedArticle?.title}&rdquo; 게시글을 정말 삭제하시겠습니까?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>
            취소
          </Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained">
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}