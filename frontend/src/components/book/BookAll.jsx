import React, { useState, useEffect } from 'react'
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardMedia,
  CircularProgress,
  Button
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import axiosApi from '../../api/AxiosApi'

const BookAll = () => {
  const navigate = useNavigate()
  const [books, setBooks] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchAllBooks = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const data = await axiosApi.get('/users/books/all')
        setBooks(data.bookDetails || [])
      } catch (err) {
        console.error('Failed to load books:', err)
        setError('도서 목록을 불러오는 데 실패했습니다.')
      } finally {
        setIsLoading(false)
      }
    }

    fetchAllBooks()
  }, [])

  return (
    <Box>
      {/* 헤더 섹션 */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #2D5A47 0%, #1E3D30 100%)',
          pt: { xs: 12, sm: 14 },
          pb: { xs: 6, sm: 8 },
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="h3" sx={{ color: 'white', fontWeight: 700, mb: 2 }}>
              전체 도서 목록
            </Typography>
            <Typography sx={{ color: 'white', opacity: 0.9 }}>
              퍼블릭 도메인으로 제공되는 모든 고전 문학을 만나보세요
            </Typography>
          </Box>
        </Container>
      </Box>

      {/* 도서 목록 섹션 */}
      <Box sx={{ bgcolor: '#FAFAF8', py: 6, minHeight: '60vh' }}>
        <Container maxWidth="lg">
          {/* 결과 카운트 */}
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              {isLoading ? '로딩 중...' : `총 ${books.length}권의 도서`}
            </Typography>
          </Box>

          {/* 로딩 상태 */}
          {isLoading && (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
              <CircularProgress color="primary" size={48} />
            </Box>
          )}

          {/* 에러 상태 */}
          {error && !isLoading && (
            <Box sx={{ textAlign: 'center', py: 8 }}>
              <Typography variant="h6" color="error" sx={{ mb: 2 }}>
                {error}
              </Typography>
              <Button
                variant="contained"
                color="primary"
                onClick={() => window.location.reload()}
              >
                다시 시도
              </Button>
            </Box>
          )}

          {/* 도서 그리드 */}
          {!isLoading && !error && (
            <Grid container spacing={3}>
              {books.map((book) => (
                <Grid item xs={12} sm={6} md={4} lg={3} key={book.id}>
                  <Card
                    onClick={() => navigate(`/books/${book.id}`)}
                    sx={{
                      height: '100%',
                      cursor: 'pointer',
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        transform: 'translateY(-8px)',
                        boxShadow: '0 12px 24px rgba(45, 90, 71, 0.15)',
                      },
                    }}
                  >
                    {book.imageUrl ? (
                      <CardMedia
                        component="img"
                        sx={{ height: 220, objectFit: 'cover' }}
                        image={book.imageUrl}
                        alt={book.title}
                      />
                    ) : (
                      <Box
                        sx={{
                          height: 220,
                          background: 'linear-gradient(135deg, #4A7C68 0%, #2D5A47 100%)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                        }}
                      >
                        <Typography variant="h1" sx={{ color: 'white', opacity: 0.3, fontWeight: 700 }}>
                          {book.title.charAt(0)}
                        </Typography>
                      </Box>
                    )}
                    <CardContent sx={{ p: 2.5 }}>
                      <Typography
                        variant="subtitle1"
                        sx={{
                          fontWeight: 600,
                          mb: 0.5,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                          lineHeight: 1.4,
                          minHeight: '2.8em',
                        }}
                      >
                        {book.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" noWrap>
                        {book.author || '작자 미상'}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}

          {/* 결과 없음 */}
          {!isLoading && !error && books.length === 0 && (
            <Box sx={{ textAlign: 'center', py: 8 }}>
              <Typography variant="h6" color="text.secondary">
                등록된 도서가 없습니다.
              </Typography>
            </Box>
          )}
        </Container>
      </Box>
    </Box>
  )
}

export default BookAll
