'use client';

import React, { useState } from 'react';
import {
  Box,
  Typography,
  Tab,
  Tabs,
  Card,
  CardContent,
  TextField,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Divider,
  IconButton,
  Collapse,
  LinearProgress,
  Alert,
} from '@mui/material';
import {
  Forum,
  Send,
  ExpandMore,
  ExpandLess,
  Comment,
  Person,
} from '@mui/icons-material';

interface Post {
  id: number;
  challengeId: number;
  authorId: number;
  content: string;
  createdAt: string;
  updatedAt: string;
}

interface Comment {
  id: number;
  postId: number;
  authorId: number;
  content: string;
  createdAt: string;
  updatedAt: string;
}

interface CommunityPanelProps {
  challengeId: number;
}

/**
 * 커뮤니티 패널 컴포넌트
 * 챌린지별 게시판과 댓글 기능 제공
 */
export function CommunityPanel({ challengeId }: CommunityPanelProps) {
  const [tabValue, setTabValue] = useState(0);
  const [posts, setPosts] = useState<Post[]>([]);
  const [expandedPost, setExpandedPost] = useState<number | null>(null);
  const [comments, setComments] = useState<Record<number, Comment[]>>({});
  const [loading, setLoading] = useState(false);
  const [newPostContent, setNewPostContent] = useState('');
  const [newCommentContent, setNewCommentContent] = useState<Record<number, string>>({});
  const [error, setError] = useState<string | null>(null);

  React.useEffect(() => {
    if (tabValue === 0) {
      loadPosts();
    }
  }, [challengeId, tabValue]);

  const loadPosts = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/challenges/${challengeId}/posts`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth-token')}`,
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        setPosts(data);
      } else {
        setError('게시글을 불러올 수 없습니다');
      }
    } catch (err) {
      console.error('게시글 로드 오류:', err);
      setError('게시글 로드 중 오류가 발생했습니다');
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async (postId: number) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/challenges/${challengeId}/posts/${postId}/comments`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth-token')}`,
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        setComments(prev => ({ ...prev, [postId]: data }));
      }
    } catch (err) {
      console.error('댓글 로드 오류:', err);
    }
  };

  const createPost = async () => {
    if (!newPostContent.trim()) return;

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/challenges/${challengeId}/posts`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('auth-token')}`,
          },
          body: JSON.stringify({ content: newPostContent }),
        }
      );

      if (response.ok) {
        setNewPostContent('');
        loadPosts(); // 게시글 목록 새로고침
      } else {
        setError('게시글 작성에 실패했습니다');
      }
    } catch (err) {
      console.error('게시글 작성 오류:', err);
      setError('게시글 작성 중 오류가 발생했습니다');
    }
  };

  const createComment = async (postId: number) => {
    const content = newCommentContent[postId];
    if (!content?.trim()) return;

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/challenges/${challengeId}/posts/${postId}/comments`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('auth-token')}`,
          },
          body: JSON.stringify({ content }),
        }
      );

      if (response.ok) {
        setNewCommentContent(prev => ({ ...prev, [postId]: '' }));
        loadComments(postId); // 댓글 목록 새로고침
      } else {
        setError('댓글 작성에 실패했습니다');
      }
    } catch (err) {
      console.error('댓글 작성 오류:', err);
      setError('댓글 작성 중 오류가 발생했습니다');
    }
  };

  const toggleComments = (postId: number) => {
    if (expandedPost === postId) {
      setExpandedPost(null);
    } else {
      setExpandedPost(postId);
      if (!comments[postId]) {
        loadComments(postId);
      }
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR');
  };

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  if (loading) {
    return (
      <Box>
        <Typography variant="h6" gutterBottom>
          커뮤니티 💬
        </Typography>
        <LinearProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        커뮤니티 💬
      </Typography>

      <Tabs value={tabValue} onChange={handleTabChange} sx={{ mb: 2 }}>
        <Tab label="게시판" icon={<Forum />} />
      </Tabs>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {tabValue === 0 && (
        <Box>
          {/* 게시글 작성 */}
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="subtitle2" gutterBottom>
                게시글 작성
              </Typography>
              <TextField
                multiline
                rows={3}
                fullWidth
                placeholder="투자 전략이나 경험을 공유해보세요..."
                value={newPostContent}
                onChange={(e) => setNewPostContent(e.target.value)}
                variant="outlined"
                sx={{ mb: 2 }}
                inputProps={{ maxLength: 2000 }}
              />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="caption" color="text.secondary">
                  {newPostContent.length}/2000
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<Send />}
                  onClick={createPost}
                  disabled={!newPostContent.trim()}
                  size="small"
                >
                  게시
                </Button>
              </Box>
            </CardContent>
          </Card>

          {/* 게시글 목록 */}
          {posts.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 3 }}>
              아직 게시글이 없습니다. 첫 번째 게시글을 작성해보세요!
            </Typography>
          ) : (
            <List>
              {posts.map((post, index) => (
                <Box key={post.id}>
                  <ListItem alignItems="flex-start">
                    <ListItemAvatar>
                      <Avatar>
                        <Person />
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="body2" fontWeight="medium">
                            사용자 {post.authorId}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {formatDate(post.createdAt)}
                          </Typography>
                        </Box>
                      }
                      secondary={
                        <Box sx={{ mt: 1 }}>
                          <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                            {post.content}
                          </Typography>
                          <Box sx={{ mt: 1, display: 'flex', alignItems: 'center' }}>
                            <IconButton
                              size="small"
                              onClick={() => toggleComments(post.id)}
                              sx={{ mr: 1 }}
                            >
                              <Comment fontSize="small" />
                            </IconButton>
                            <Typography variant="caption" color="text.secondary" sx={{ mr: 2 }}>
                              댓글 {comments[post.id]?.length || 0}개
                            </Typography>
                            <IconButton size="small" onClick={() => toggleComments(post.id)}>
                              {expandedPost === post.id ? <ExpandLess /> : <ExpandMore />}
                            </IconButton>
                          </Box>
                        </Box>
                      }
                    />
                  </ListItem>

                  {/* 댓글 섹션 */}
                  <Collapse in={expandedPost === post.id}>
                    <Box sx={{ ml: 7, mr: 2, mb: 2 }}>
                      {/* 댓글 작성 */}
                      <Box sx={{ mb: 2, display: 'flex', gap: 1 }}>
                        <TextField
                          size="small"
                          placeholder="댓글 달기..."
                          value={newCommentContent[post.id] || ''}
                          onChange={(e) => setNewCommentContent(prev => ({ ...prev, [post.id]: e.target.value }))}
                          variant="outlined"
                          fullWidth
                          inputProps={{ maxLength: 1000 }}
                        />
                        <Button
                          size="small"
                          variant="contained"
                          onClick={() => createComment(post.id)}
                          disabled={!newCommentContent[post.id]?.trim()}
                        >
                          댓글 달기
                        </Button>
                      </Box>

                      {/* 댓글 목록 */}
                      {comments[post.id]?.map((comment) => (
                        <Box key={comment.id} sx={{ mb: 1, p: 1, bgcolor: 'background.default', borderRadius: 1 }}>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                            <Typography variant="caption" fontWeight="medium">
                              사용자 {comment.authorId}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {formatDate(comment.createdAt)}
                            </Typography>
                          </Box>
                          <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                            {comment.content}
                          </Typography>
                        </Box>
                      ))}
                    </Box>
                  </Collapse>

                  {index < posts.length - 1 && <Divider />}
                </Box>
              ))}
            </List>
          )}
        </Box>
      )}
    </Box>
  );
}