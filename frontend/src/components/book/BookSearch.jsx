import React, { useState, useRef, useEffect } from 'react'
import { Box, Container, Typography, TextField, Paper, Chip, List, ListItem, ListItemText, Avatar, Button, Grid, Card, CardContent, CardMedia, CircularProgress } from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import { useNavigate } from 'react-router-dom'
import axiosApi from '../../api/AxiosApi'

const RECENT_SEARCHES_KEY = 'recentSearches'
const MAX_RECENT_SEARCHES = 3

const BookSearch = () => {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [isFocused, setIsFocused] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState(null)
  const [searchResults, setSearchResults] = useState([])
  const [hasSearched, setHasSearched] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [nextUrl, setNextUrl] = useState(null)
  const resultsRef = useRef(null)
  const [recentSearches, setRecentSearches] = useState([])

  const popularSearches = ['Jane Austen', 'Charles Dickens', 'Mark Twain']

  const categories = ['Fiction', 'Poetry', 'Drama', 'Philosophy', 'History']

  // sessionStorage에서 최근 검색어 불러오기
  useEffect(() => {
    const saved = sessionStorage.getItem(RECENT_SEARCHES_KEY)
    if (saved) {
      setRecentSearches(JSON.parse(saved))
    }
  }, [])

  // 최근 검색어 저장 함수
  const saveRecentSearch = (query) => {
    const trimmed = query.trim()
    if (!trimmed) return

    setRecentSearches(prev => {
      const filtered = prev.filter(item => item !== trimmed)
      const updated = [trimmed, ...filtered].slice(0, MAX_RECENT_SEARCHES)
      sessionStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated))
      return updated
    })
  }

  const [isLoadingMore, setIsLoadingMore] = useState(false)
  const [autocompleteData, setAutocompleteData] = useState([])
  const [filteredSuggestions, setFilteredSuggestions] = useState([])

  useEffect(() => {
    const fetchAutocomplete = async () => {
      try {
        const data = await axiosApi.get('/users/books/autocomplete')
        setAutocompleteData(data.items || [])
      } catch (error) {
        console.error('자동완성 데이터 로드 실패:', error)
      }
    }
    fetchAutocomplete()
  }, [])

  useEffect(() => {
    if (keyword.trim() && autocompleteData.length > 0) {
      const query = keyword.toLowerCase()
      const filtered = autocompleteData.filter(item =>
        item.title.toLowerCase().includes(query) ||
        item.author.toLowerCase().includes(query)
      ).slice(0, 8)
      setFilteredSuggestions(filtered)
    } else {
      setFilteredSuggestions([])
    }
  }, [keyword, autocompleteData])

  const executeSearch = async (query) => {
    const trimmed = query.trim()
    if (!trimmed) return

    setIsLoading(true)
    saveRecentSearch(trimmed)
    try {
      let url = `/users/books?query=${encodeURIComponent(trimmed)}`
      if (selectedCategory) {
        url += `&topic=${encodeURIComponent(selectedCategory.toLowerCase())}`
      }
      const data = await axiosApi.get(url)
      setSearchResults(data.bookDetails || [])
      setNextUrl(data.nextUrl || null)
      setHasSearched(true)
      setTimeout(() => {
        resultsRef.current?.scrollIntoView({ behavior: 'smooth' })
      }, 100)
    } catch (error) {
      console.error('검색 실패:', error)
      setSearchResults([])
    } finally {
      setIsLoading(false)
    }
  }

  const handleSearch = () => {
    executeSearch(keyword)
  }

  const handleLoadMore = async () => {
    if (!nextUrl || isLoadingMore) return

    setIsLoadingMore(true)
    try {
      const data = await axiosApi.get(`/users/books/next?nextUrl=${encodeURIComponent(nextUrl)}`)
      setSearchResults(prev => [...prev, ...(data.bookDetails || [])])
      setNextUrl(data.nextUrl || null)
    } catch (error) {
      console.error('더보기 실패:', error)
    } finally {
      setIsLoadingMore(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleSearch()
    }
  }

  return (
    <Box>
      {/* 히어로 섹션 */}
      <Box
        sx={{
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #2D5A47 0%, #1E3D30 100%)',
          display: 'flex',
          alignItems: 'center',
          pt: 8,
        }}
      >
        <Container maxWidth="sm">
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Typography variant="h3" sx={{ color: 'white', fontWeight: 700, mb: 2 }}>
              어떤 고전을 찾고 계신가요?
            </Typography>
            <Typography sx={{ color: 'white', opacity: 0.9 }}>
              퍼블릭 도메인 도서를 검색하세요
            </Typography>
          </Box>

          {/* 검색 입력 */}
          <Box sx={{ position: 'relative', maxWidth: 480, mx: 'auto' }}>
            <TextField
              fullWidth
              placeholder="영문 도서명, 저자명으로 검색..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onFocus={() => setIsFocused(true)}
              onBlur={() => setTimeout(() => setIsFocused(false), 200)}
              onKeyDown={handleKeyDown}
              autoComplete="off"
              size="small"
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 2,
                  bgcolor: 'white',
                  boxShadow: 2,
                  '& fieldset': { border: 'none' },
                },
                '& .MuiInputBase-input': {
                  caretColor: '#2D5A47',
                  // caretWidth: 'thin',
                },
              }}
              slotProps={{input: {
                sx: { py: 0.5, px: 2 },
              }
              }}
            />

            {/* 자동완성 드롭다운 */}
            {isFocused && !keyword && (
              <Paper sx={{ position: 'absolute', top: '100%', left: 0, right: 0, mt: 1, zIndex: 10, boxShadow: 4, borderRadius: 2, opacity: '.9' }}>
                <Box sx={{ p: 3 }}>
                  {recentSearches.length > 0 && (
                    <>
                      <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1.5 }}>최근 검색어</Typography>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 3 }}>
                        {recentSearches.map((text, i) => (
                          <Chip
                            key={i}
                            label={text}
                            size="small"
                            variant="outlined"
                            onClick={() => {
                              setKeyword(text)
                              setIsFocused(false)
                              executeSearch(text)
                            }}
                            sx={{
                              borderColor: 'rgba(45, 90, 71, 0.5)',
                              color: 'text.secondary',
                              '&:hover': {
                                bgcolor: 'rgba(45, 90, 71, 0.1)',
                                borderColor: 'primary.main',
                                color: 'primary.main'
                              }
                            }}
                          />
                        ))}
                      </Box>
                    </>
                  )}
                  <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1.5 }}>인기 검색어</Typography>
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    {popularSearches.map((text, i) => (
                      <Chip
                        key={i}
                        label={text}
                        size="small"
                        variant="outlined"
                        onClick={() => {
                          setKeyword(text)
                          setIsFocused(false)
                          executeSearch(text)
                        }}
                        sx={{
                          borderColor: 'rgba(230, 126, 34, 0.5)',
                          color: 'text.secondary',
                          '&:hover': {
                            bgcolor: 'rgba(230, 126, 34, 0.1)',
                            borderColor: 'secondary.main',
                            color: 'secondary.main'
                          }
                        }}
                      />
                    ))}
                  </Box>
                </Box>
              </Paper>
            )}

            {/* 자동완성 결과 */}
            {keyword && filteredSuggestions.length > 0 && isFocused && (
              <Paper sx={{ position: 'absolute', top: '100%', left: 0, right: 0, mt: 1, zIndex: 10, boxShadow: 4, borderRadius: 2 }}>
                <List sx={{ p: 0 }}>
                  {filteredSuggestions.map((item) => (
                    <ListItem
                      key={item.id}
                      onClick={() => {
                        setKeyword(item.title)
                        setIsFocused(false)
                        executeSearch(item.title)
                      }}
                      sx={{
                        cursor: 'pointer',
                        '&:hover': { bgcolor: 'grey.50' },
                        py: 2,
                      }}
                    >
                      <Avatar sx={{ mr: 2, bgcolor: 'primary.main' }}>{item.title.charAt(0)}</Avatar>
                      <ListItemText
                        primary={item.title}
                        secondary={item.author}
                      />
                    </ListItem>
                  ))}
                </List>
              </Paper>
            )}
          </Box>

          {/* 카테고리 */}
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1, flexWrap: 'wrap', mt: 3, maxWidth: 480, mx: 'auto' }}>
            {categories.map((cat) => (
              <Chip
                key={cat}
                label={cat}
                size="small"
                variant={selectedCategory === cat ? 'filled' : 'outlined'}
                onClick={() => setSelectedCategory(selectedCategory === cat ? null : cat)}
                sx={{
                  color: selectedCategory === cat ? '#2D5A47' : 'white',
                  bgcolor: selectedCategory === cat ? 'white' : 'transparent',
                  borderColor: 'rgba(255,255,255,0.5)',
                  '&:hover': {
                    bgcolor: selectedCategory === cat ? 'white' : 'rgba(255,255,255,0.15)',
                    borderColor: 'white',
                    cursor: 'pointer'
                  },
                }}
              />
            ))}
          </Box>

          {/* 검색 버튼 */}
          <Box sx={{ maxWidth: 480, mx: 'auto', mt: 4 }}>
            <Button
              fullWidth
              variant="contained"
              color="secondary"
              startIcon={isLoading ? <CircularProgress size={20} color="inherit" /> : <SearchIcon />}
              onClick={handleSearch}
              disabled={isLoading}
              sx={{ py: 1.2 }}
            >
              {isLoading ? '검색 진행중...' : '검색하기'}
            </Button>
          </Box>
        </Container>
      </Box>

      {/* 검색 결과 섹션 */}
      <Box
        ref={resultsRef}
        sx={{
          minHeight: hasSearched ? '100vh' : 0,
          bgcolor: 'white',
          py: hasSearched ? 6 : 0,
        }}
      >
        {hasSearched && (
          <Container maxWidth="lg">
            <Typography variant="h5" sx={{ fontWeight: 600, mb: 4 }}>
              검색 결과 ({searchResults.length}건)
            </Typography>

            {isLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                <CircularProgress color="primary" />
              </Box>
            ) : (
            <Grid container spacing={3}>
              {searchResults.map((book) => (
                <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={book.id}>
                  <Card
                    onClick={() => navigate(`/books/${book.id}`)}
                    sx={{
                      height: '100%',
                      cursor: 'pointer',
                      transition: 'transform 0.2s, box-shadow 0.2s',
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: 4,
                      },
                    }}
                  >
                    {book.imageUrl ? (
                      <CardMedia
                        component="img"
                        sx={{ height: 200, objectFit: 'cover' }}
                        image={book.imageUrl}
                        alt={book.title}
                      />
                    ) : (
                      <Box
                        sx={{
                          height: 200,
                          bgcolor: 'primary.light',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                        }}
                      >
                        <Typography variant="h2" sx={{ color: 'white', opacity: 0.5 }}>
                          {book.title.charAt(0)}
                        </Typography>
                      </Box>
                    )}
                    <CardContent>
                      <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 0.5 }} noWrap>
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

            {!isLoading && searchResults.length === 0 && (
              <Box sx={{ textAlign: 'center', py: 8 }}>
                <Typography variant="h6" color="text.secondary">
                  검색 결과가 없습니다.
                </Typography>
              </Box>
            )}

            {/* 더보기 버튼 */}
            {!isLoading && nextUrl && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <Button
                  variant="outlined"
                  color="primary"
                  onClick={handleLoadMore}
                  disabled={isLoadingMore}
                  sx={{ px: 4, py: 1.5 }}
                >
                  {isLoadingMore ? <CircularProgress size={24} /> : '더보기'}
                </Button>
              </Box>
            )}
          </Container>
        )}
      </Box>
    </Box>
  )
}

export default BookSearch
