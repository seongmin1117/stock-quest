'use client';

import React from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Avatar,
  Button,
  TextField,
  IconButton,
  Divider,
  Chip,
  Alert,
} from '@mui/material';
import { 
  People, 
  Add, 
  ChatBubbleOutline,
  Send,
  ExpandMore,
  ExpandLess,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@/shared/lib/auth/auth-store';
import apiClient from '@/shared/api/api-client';

interface Post {
  id: number;
  content: string;
  authorNickname: string;
  createdAt: string;
  commentCount?: number;
}

interface Comment {
  id: number;
  content: string;
  authorNickname: string;
  createdAt: string;
}

interface CommunityFeedProps {
  challengeId: number;
  maxPosts?: number;
  showCreatePost?: boolean;
}

/**
 * 커뮤니티 피드 위젯
 * 특정 챌린지의 게시글과 댓글을 표시하는 재사용 가능한 컴포넌트
 */
export function CommunityFeed({ challengeId, maxPosts = 5, showCreatePost = true }: CommunityFeedProps) {
  const { user } = useAuth();
  const [expandedPosts, setExpandedPosts] = React.useState<Set<number>>(new Set());
  const [newPost, setNewPost] = React.useState('');
  const [newComments, setNewComments] = React.useState<Record<number, string>>({});
  const [comments, setComments] = React.useState<Record<number, Comment[]>>({});

  // 게시글 목록 조회
  const { 
    data: posts = [], 
    isLoading,
    error,
    refetch: refetchPosts 
  } = useQuery<Post[]>({
    queryKey: ['posts', challengeId],
    queryFn: async () => {
      const data = await apiClient.get<Post[]>(`/api/challenges/${challengeId}/posts`);
      return data ?? [];
    },
    refetchInterval: 30000,
  });

  const loadComments = async (postId: number) => {
    try {
      const data = await apiClient.get<Comment[]>(
        `/api/challenges/${challengeId}/posts/${postId}/comments`
      );
      setComments(prev => ({
        ...prev,
        [postId]: data ?? []
      }));
    } catch (err) {
      console.error('댓글 로딩 실패:', err);
    }
  };

  const handleCreatePost = async () => {
    if (!newPost.trim() || !user) return;

    try {
      await apiClient.post(`/api/challenges/${challengeId}/posts`, {
        content: newPost.trim()
      });
      
      setNewPost('');
      refetchPosts();
    } catch (err) {
      console.error('게시글 작성 실패:', err);
    }
  };

  const handleCreateComment = async (postId: number) => {
    const content = newComments[postId]?.trim();
    if (!content || !user) return;

    try {
      await apiClient.post(
        `/api/challenges/${challengeId}/posts/${postId}/comments`,
        { content }
      );
      
      setNewComments(prev => ({ ...prev, [postId]: '' }));
      loadComments(postId);
    } catch (err) {
      console.error('댓글 작성 실패:', err);
    }
  };

  const togglePostExpansion = async (postId: number) => {
    const newExpanded = new Set(expandedPosts);
    
    if (expandedPosts.has(postId)) {
      newExpanded.delete(postId);
    } else {
      newExpanded.add(postId);
      if (!comments[postId]) {
        await loadComments(postId);
      }
    }
    
    setExpandedPosts(newExpanded);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (isLoading) {
    return (
      <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <People sx={{ color: '#2196F3' }} />
            <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
              커뮤니티
            </Typography>
          </Box>
          <Box display="flex" justifyContent="center" py={4}>
            <Typography sx={{ color: '#78828A' }}>로딩 중...</Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (error && !user) {
    return (
      <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <People sx={{ color: '#2196F3' }} />
            <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
              커뮤니티
            </Typography>
          </Box>
          <Alert severity="info">
            커뮤니티 기능을 사용하려면 로그인이 필요합니다.
          </Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ backgroundColor: '#1A1F2E', border: '1px solid #2A3441' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <People sx={{ color: '#2196F3' }} />
          <Typography variant="h6" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
            커뮤니티
          </Typography>
        </Box>

        {/* 게시글 작성 */}
        {user && showCreatePost && (
          <Box sx={{ mb: 3 }}>
            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
              <Avatar
                sx={{
                  width: 32,
                  height: 32,
                  backgroundColor: '#2196F3',
                  fontSize: '0.875rem',
                  fontWeight: 'bold',
                }}
              >
                {user.nickname?.charAt(0)?.toUpperCase() || 'U'}
              </Avatar>
              
              <Box sx={{ flexGrow: 1 }}>
                <TextField
                  fullWidth
                  size="small"
                  multiline
                  rows={2}
                  placeholder="투자 경험을 공유해보세요..."
                  value={newPost}
                  onChange={(e) => setNewPost(e.target.value)}
                  sx={{
                    '& .MuiOutlinedInput-root': {
                      color: '#FFFFFF',
                      backgroundColor: '#0A0E18',
                      '& fieldset': {
                        borderColor: '#2A3441',
                      },
                      '&:hover fieldset': {
                        borderColor: '#2196F3',
                      },
                    },
                  }}
                />
              </Box>
            </Box>
            
            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                variant="contained"
                size="small"
                startIcon={<Add />}
                onClick={handleCreatePost}
                disabled={!newPost.trim()}
                sx={{
                  backgroundColor: '#2196F3',
                  '&:hover': {
                    backgroundColor: '#1976D2',
                  },
                }}
              >
                게시
              </Button>
            </Box>
            
            <Divider sx={{ borderColor: '#2A3441', mt: 2 }} />
          </Box>
        )}

        {/* 게시글 목록 */}
        {posts.slice(0, maxPosts).map((post, index) => (
          <Box key={post.id}>
            {index > 0 && <Divider sx={{ borderColor: '#2A3441', my: 2 }} />}
            
            {/* 게시글 헤더 */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
              <Avatar
                sx={{
                  width: 24,
                  height: 24,
                  backgroundColor: '#2A3441',
                  fontSize: '0.75rem',
                  fontWeight: 'bold',
                }}
              >
                {post.authorNickname.charAt(0).toUpperCase()}
              </Avatar>
              
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="subtitle2" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
                  {post.authorNickname}
                </Typography>
                <Typography variant="caption" sx={{ color: '#78828A' }}>
                  {formatDate(post.createdAt)}
                </Typography>
              </Box>
            </Box>

            {/* 게시글 내용 */}
            <Typography variant="body2" sx={{ color: '#FFFFFF', mb: 1, lineHeight: 1.5 }}>
              {post.content}
            </Typography>

            {/* 게시글 액션 */}
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
              <Button
                size="small"
                startIcon={<ChatBubbleOutline />}
                onClick={() => togglePostExpansion(post.id)}
                sx={{
                  color: '#78828A',
                  '&:hover': {
                    color: '#2196F3',
                    backgroundColor: 'rgba(33, 150, 243, 0.08)',
                  },
                }}
              >
                댓글 {post.commentCount || 0}
              </Button>

              <IconButton
                size="small"
                onClick={() => togglePostExpansion(post.id)}
                sx={{ color: '#78828A' }}
              >
                {expandedPosts.has(post.id) ? <ExpandLess /> : <ExpandMore />}
              </IconButton>
            </Box>

            {/* 댓글 섹션 */}
            {expandedPosts.has(post.id) && (
              <Box sx={{ ml: 4, mt: 2, pl: 2, borderLeft: '2px solid #2A3441' }}>
                {/* 댓글 입력 */}
                {user && (
                  <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                    <Avatar
                      sx={{
                        width: 20,
                        height: 20,
                        backgroundColor: '#2196F3',
                        fontSize: '0.625rem',
                        fontWeight: 'bold',
                      }}
                    >
                      {user.nickname?.charAt(0)?.toUpperCase() || 'U'}
                    </Avatar>
                    
                    <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
                      <TextField
                        fullWidth
                        size="small"
                        placeholder="댓글을 입력하세요..."
                        value={newComments[post.id] || ''}
                        onChange={(e) => setNewComments(prev => ({
                          ...prev,
                          [post.id]: e.target.value
                        }))}
                        onKeyPress={(e) => {
                          if (e.key === 'Enter' && !e.shiftKey) {
                            e.preventDefault();
                            handleCreateComment(post.id);
                          }
                        }}
                        sx={{
                          '& .MuiOutlinedInput-root': {
                            color: '#FFFFFF',
                            backgroundColor: '#0A0E18',
                            '& fieldset': {
                              borderColor: '#2A3441',
                            },
                            '&:hover fieldset': {
                              borderColor: '#2196F3',
                            },
                          },
                        }}
                      />
                      
                      <IconButton
                        size="small"
                        onClick={() => handleCreateComment(post.id)}
                        disabled={!newComments[post.id]?.trim()}
                        sx={{
                          color: '#2196F3',
                          '&:disabled': {
                            color: '#2A3441',
                          }
                        }}
                      >
                        <Send fontSize="small" />
                      </IconButton>
                    </Box>
                  </Box>
                )}

                {/* 댓글 목록 */}
                {comments[post.id]?.slice(0, 3).map((comment) => (
                  <Box key={comment.id} sx={{ display: 'flex', gap: 1, mb: 1 }}>
                    <Avatar
                      sx={{
                        width: 18,
                        height: 18,
                        backgroundColor: '#2A3441',
                        fontSize: '0.625rem',
                        fontWeight: 'bold',
                      }}
                    >
                      {comment.authorNickname.charAt(0).toUpperCase()}
                    </Avatar>
                    
                    <Box sx={{ flexGrow: 1 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.25 }}>
                        <Typography variant="caption" sx={{ color: '#FFFFFF', fontWeight: 'bold' }}>
                          {comment.authorNickname}
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#78828A' }}>
                          {formatDate(comment.createdAt)}
                        </Typography>
                      </Box>
                      <Typography variant="body2" sx={{ color: '#B0BEC5', fontSize: '0.75rem' }}>
                        {comment.content}
                      </Typography>
                    </Box>
                  </Box>
                ))}
                
                {comments[post.id]?.length === 0 && (
                  <Typography variant="body2" sx={{ color: '#78828A', textAlign: 'center', py: 1 }}>
                    첫 번째 댓글을 작성해보세요!
                  </Typography>
                )}
              </Box>
            )}
          </Box>
        ))}

        {posts.length === 0 ? (
          <Box textAlign="center" sx={{ py: 4 }}>
            <Typography variant="body2" sx={{ color: '#78828A' }}>
              아직 게시글이 없습니다
            </Typography>
            <Typography variant="caption" sx={{ color: '#78828A' }}>
              {user ? '첫 번째 게시글을 작성해보세요!' : '로그인하고 첫 번째 게시글을 작성해보세요!'}
            </Typography>
          </Box>
        ) : posts.length > maxPosts && (
          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Chip 
              label={`+${posts.length - maxPosts}개 더 보기`}
              variant="outlined"
              size="small"
              sx={{
                color: '#78828A',
                borderColor: '#2A3441',
                '&:hover': {
                  borderColor: '#2196F3',
                  color: '#2196F3',
                }
              }}
            />
          </Box>
        )}
      </CardContent>
    </Card>
  );
}