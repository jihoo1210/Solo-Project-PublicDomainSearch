import React, { useState, useEffect, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import {
  Box,
  Container,
  Typography,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  IconButton,
  CircularProgress,
  Paper,
  Button,
  Collapse,
  Tabs,
  Tab,
  useMediaQuery,
  useTheme,
  Slide,
  Chip,
  LinearProgress,
  Fade,
  Snackbar,
  Alert
} from '@mui/material'
import MenuBookIcon from '@mui/icons-material/MenuBook'
import CloseIcon from '@mui/icons-material/Close'
import NavigateBeforeIcon from '@mui/icons-material/NavigateBefore'
import NavigateNextIcon from '@mui/icons-material/NavigateNext'
import ExpandLess from '@mui/icons-material/ExpandLess'
import ExpandMore from '@mui/icons-material/ExpandMore'
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp'
import axiosApi from '../../api/AxiosApi'
import { useUser } from '../../contexts/UserContext'

const READING_PROGRESS_KEY = 'readingProgress'

// localStorage 헬퍼 함수
const getLocalProgress = (bookId) => {
  try {
    const saved = localStorage.getItem(READING_PROGRESS_KEY)
    if (saved) {
      const progress = JSON.parse(saved)
      return progress[bookId] || null
    }
  } catch (error) {
    console.error('읽기 진행 상태 로드 실패:', error)
  }
  return null
}

const saveLocalProgress = (bookId, page, title) => {
  try {
    const saved = localStorage.getItem(READING_PROGRESS_KEY)
    const progress = saved ? JSON.parse(saved) : {}
    progress[bookId] = {
      page,
      title,
      lastReadAt: new Date().toISOString()
    }
    localStorage.setItem(READING_PROGRESS_KEY, JSON.stringify(progress))
  } catch (error) {
    console.error('읽기 진행 상태 저장 실패:', error)
  }
}

// 서버 API 헬퍼 함수
const getServerProgress = async (bookId) => {
  try {
    const response = await axiosApi.get(`/users/reading-progress/${bookId}`)
    if (response) {
      return {
        page: response.currentPage,
        title: response.bookTitle,
        lastReadAt: response.lastReadAt
      }
    }
  } catch (error) {
    // 401 에러는 비로그인 상태이므로 무시
    if (error.response?.status !== 401) {
      console.error('서버 진행 상태 로드 실패:', error)
    }
  }
  return null
}

const saveServerProgress = async (bookId, page, title) => {
  try {
    await axiosApi.post('/users/reading-progress', {
      bookId: Number(bookId),
      currentPage: page,
      bookTitle: title
    })
  } catch (error) {
    if (error.response?.status !== 401) {
      console.error('서버 진행 상태 저장 실패:', error)
    }
  }
}

const BookDetail = () => {
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const isSmall = useMediaQuery(theme.breakpoints.down('sm'))
  const { isLoggedIn } = useUser()

  const { bookId } = useParams()
  const [bookData, setBookData] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [navOpen, setNavOpen] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [tabValue, setTabValue] = useState(0)
  const [expandedChapter, setExpandedChapter] = useState(null)
  const [showContinueReading, setShowContinueReading] = useState(false)
  const [savedProgress, setSavedProgress] = useState(null)

  // 진행 상태 저장 (로그인 여부에 따라 서버/로컬 구분)
  const saveProgress = useCallback(async (page, title) => {
    if (isLoggedIn) {
      await saveServerProgress(bookId, page, title)
    }
    // 로컬에도 항상 저장 (오프라인 지원)
    saveLocalProgress(bookId, page, title)
  }, [bookId, isLoggedIn])

  const fetchBookDetail = useCallback(async (page = 0, isInitial = false) => {
    setIsLoading(true)
    try {
      const data = await axiosApi.get(`/users/books/${bookId}?page=${page}`)
      setBookData(data)
      setCurrentPage(data.currentPage)

      // 페이지 변경 시 진행 상태 저장 (초기 로드가 아닌 경우)
      if (!isInitial && page > 0) {
        saveProgress(data.currentPage, data.title)
      }

      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch (error) {
      console.error('책 상세 정보 로드 실패:', error)
    } finally {
      setIsLoading(false)
    }
  }, [bookId, saveProgress])

  useEffect(() => {
    const loadProgress = async () => {
      if (!bookId) return

      let progress = null

      // 로그인 사용자는 서버에서 먼저 확인
      if (isLoggedIn) {
        progress = await getServerProgress(bookId)
      }

      // 서버에 없으면 로컬에서 확인
      if (!progress) {
        progress = getLocalProgress(bookId)
      }

      if (progress && progress.page > 0) {
        setSavedProgress(progress)
        setShowContinueReading(true)
      }

      fetchBookDetail(0, true)
    }

    loadProgress()
  }, [bookId, isLoggedIn, fetchBookDetail])

  const handleContinueReading = () => {
    if (savedProgress) {
      fetchBookDetail(savedProgress.page)
    }
    setShowContinueReading(false)
  }

  const handleStartFromBeginning = () => {
    setShowContinueReading(false)
    saveProgress(0, bookData?.title || '')
  }

  const handlePageChange = (page) => {
    fetchBookDetail(page)
    setNavOpen(false)
    // 페이지 변경 시 진행 상태 저장
    if (bookData?.title) {
      saveProgress(page, bookData.title)
    }
  }

  const handlePrevPage = () => {
    if (bookData?.hasPrevious) {
      handlePageChange(currentPage - 1)
    }
  }

  const handleNextPage = () => {
    if (bookData?.hasNext) {
      handlePageChange(currentPage + 1)
    }
  }

  const handleChapterClick = (chapterIndex) => {
    setExpandedChapter(expandedChapter === chapterIndex ? null : chapterIndex)
  }

  const getChapterPages = (chapterIndex) => {
    if (!bookData?.chapters || chapterIndex >= bookData.chapters.length) return []

    const currentChapter = bookData.chapters[chapterIndex]
    const nextChapter = bookData.chapters[chapterIndex + 1]

    const startPage = currentChapter.startPage
    const endPage = nextChapter ? nextChapter.startPage - 1 : bookData.totalPages - 1

    const pages = []
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i)
    }
    return pages
  }

  const groupSentencesByParagraph = (sentences) => {
    if (!sentences) return []

    const paragraphs = []
    let currentParagraph = []
    let currentParagraphNumber = -1

    sentences.forEach((sentence) => {
      if (sentence.paragraphNumber !== currentParagraphNumber) {
        if (currentParagraph.length > 0) {
          paragraphs.push({
            paragraphNumber: currentParagraphNumber,
            sentences: currentParagraph
          })
        }
        currentParagraph = [sentence]
        currentParagraphNumber = sentence.paragraphNumber
      } else {
        currentParagraph.push(sentence)
      }
    })

    if (currentParagraph.length > 0) {
      paragraphs.push({
        paragraphNumber: currentParagraphNumber,
        sentences: currentParagraph
      })
    }

    return paragraphs
  }

  const paragraphs = groupSentencesByParagraph(bookData?.sentences)
  const progress = bookData ? ((currentPage + 1) / bookData.totalPages) * 100 : 0

  // 네비게이션 패널 컨텐츠
  const NavigationContent = () => (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ p: 2, pb: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            탐색
          </Typography>
          <IconButton onClick={() => setNavOpen(false)} size="small">
            {isMobile ? <KeyboardArrowUpIcon /> : <CloseIcon />}
          </IconButton>
        </Box>

        <Tabs
          value={tabValue}
          onChange={(_, newValue) => setTabValue(newValue)}
          sx={{ borderBottom: 1, borderColor: 'divider' }}
          variant={isSmall ? 'fullWidth' : 'standard'}
        >
          <Tab label="챕터" />
          <Tab label="페이지" />
        </Tabs>
      </Box>

      <Box sx={{ flex: 1, overflow: 'auto', p: 2, pt: 1 }}>
        {tabValue === 0 && (
          <List sx={{ p: 0 }}>
            {bookData?.chapters && bookData.chapters.length > 0 ? (
              bookData.chapters.map((chapter, index) => (
                <Box key={index}>
                  <ListItemButton
                    onClick={() => handleChapterClick(index)}
                    sx={{
                      borderRadius: 1,
                      mb: 0.5,
                      bgcolor: expandedChapter === index ? 'action.selected' : 'transparent',
                    }}
                  >
                    <ListItemText
                      primary={chapter.title}
                      primaryTypographyProps={{
                        variant: 'body2',
                        fontWeight: expandedChapter === index ? 600 : 400,
                        noWrap: true,
                      }}
                    />
                    {expandedChapter === index ? <ExpandLess /> : <ExpandMore />}
                  </ListItemButton>
                  <Collapse in={expandedChapter === index} timeout="auto" unmountOnExit>
                    <Box sx={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      gap: 0.5,
                      pl: 2,
                      pr: 1,
                      pb: 1
                    }}>
                      {getChapterPages(index).map((pageNum) => (
                        <Chip
                          key={pageNum}
                          label={pageNum + 1}
                          size="small"
                          onClick={() => handlePageChange(pageNum)}
                          disabled={isLoading}
                          color={currentPage === pageNum ? 'primary' : 'default'}
                          variant={currentPage === pageNum ? 'filled' : 'outlined'}
                          sx={{ minWidth: 40 }}
                        />
                      ))}
                    </Box>
                  </Collapse>
                </Box>
              ))
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
                챕터 정보가 없습니다.
              </Typography>
            )}
          </List>
        )}

        {tabValue === 1 && (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {bookData?.totalPages > 0 ? (
              Array.from({ length: bookData.totalPages }, (_, index) => (
                <Chip
                  key={index}
                  label={index + 1}
                  size="small"
                  onClick={() => handlePageChange(index)}
                  disabled={isLoading}
                  color={currentPage === index ? 'primary' : 'default'}
                  variant={currentPage === index ? 'filled' : 'outlined'}
                  sx={{ minWidth: 40 }}
                />
              ))
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ p: 2, width: '100%', textAlign: 'center' }}>
                페이지 정보가 없습니다.
              </Typography>
            )}
          </Box>
        )}
      </Box>
    </Box>
  )

  if (isLoading && !bookData) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    )
  }

  if (!bookData) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <Typography variant="h6" color="text.secondary">
          책 정보를 불러올 수 없습니다.
        </Typography>
      </Box>
    )
  }

  return (
    <Box sx={{ minHeight: '100vh', pt: { xs: 7, sm: 8 }, pb: { xs: 10, md: 4 } }}>
      {/* 진행률 바 - 상단 고정 */}
      <Box sx={{
        position: 'fixed',
        top: { xs: 56, sm: 64 },
        left: 0,
        right: 0,
        zIndex: 1100
      }}>
        <LinearProgress
          variant="determinate"
          value={progress}
          sx={{
            height: 3,
            bgcolor: 'grey.200',
            '& .MuiLinearProgress-bar': {
              bgcolor: 'primary.main'
            }
          }}
        />
      </Box>

      <Container maxWidth="md" sx={{ py: { xs: 2, sm: 4 } }}>
        {/* 헤더 영역 */}
        <Box sx={{ mb: { xs: 2, sm: 4 } }}>
          <Typography
            variant={isSmall ? 'h5' : 'h4'}
            sx={{
              fontWeight: 700,
              mb: 1,
              color: 'primary.main',
              wordBreak: 'keep-all'
            }}
          >
            {bookData.title}
          </Typography>

          <Box sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            flexWrap: 'wrap'
          }}>
            <Typography variant="body2" color="text.secondary">
              {currentPage + 1} / {bookData.totalPages} 페이지
            </Typography>
            <Chip
              label={`${Math.round(progress)}% 완료`}
              size="small"
              color="primary"
              variant="outlined"
            />
          </Box>
        </Box>

        {/* 페이지 네비게이션 - 데스크톱 (상단) */}
        {!isMobile && (
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Button
              variant="outlined"
              startIcon={<NavigateBeforeIcon />}
              onClick={handlePrevPage}
              disabled={!bookData.hasPrevious || isLoading}
            >
              이전
            </Button>

            <Button
              variant="contained"
              onClick={() => setNavOpen(!navOpen)}
            >
              {navOpen ? '닫기' : '목차'}
            </Button>

            <Button
              variant="outlined"
              endIcon={<NavigateNextIcon />}
              onClick={handleNextPage}
              disabled={!bookData.hasNext || isLoading}
            >
              다음
            </Button>
          </Box>
        )}

        {/* 네비게이션 패널 - 데스크톱 (상단 배치) */}
        {!isMobile && (
          <Collapse in={navOpen}>
            <Paper
              elevation={3}
              sx={{
                mb: 3,
                maxHeight: 400,
                overflowY: 'auto',
                borderRadius: 2
              }}
            >
              <NavigationContent />
            </Paper>
          </Collapse>
        )}

        {/* 본문 */}
        <Paper
          elevation={0}
          sx={{
            p: { xs: 2, sm: 4 },
            bgcolor: '#f5e6c8',
            borderRadius: 2,
            minHeight: { xs: 300, sm: 400 },
            position: 'relative'
          }}
        >
          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 300 }}>
              <CircularProgress />
            </Box>
          ) : (
            <Fade in={!isLoading}>
              <Box>
                {paragraphs.map((paragraph, index) => (
                  <Box
                    key={paragraph.paragraphNumber}
                    id={`paragraph-${index}`}
                    sx={{ mb: { xs: 2, sm: 3 } }}
                  >
                    <Typography
                      variant="body1"
                      sx={{
                        lineHeight: { xs: 1.7, sm: 1.8 },
                        textAlign: 'justify',
                        textIndent: '2em',
                        fontSize: { xs: '0.95rem', sm: '1rem' },
                        color: '#2d2d2d'
                      }}
                    >
                      {paragraph.sentences.map((sentence) => sentence.content).join(' ')}
                    </Typography>
                  </Box>
                ))}
              </Box>
            </Fade>
          )}
        </Paper>
      </Container>

      {/* 하단 네비게이션 바 - 모바일 */}
      {isMobile && (
        <>
          {/* 하단 고정 버튼 바 */}
          <Paper
            elevation={8}
            sx={{
              position: 'fixed',
              bottom: 0,
              left: 0,
              right: 0,
              zIndex: 1200,
              borderRadius: '16px 16px 0 0',
              overflow: 'hidden'
            }}
          >
            <Box sx={{
              display: 'flex',
              justifyContent: 'space-around',
              alignItems: 'center',
              py: 1,
              px: 2,
              bgcolor: 'background.paper'
            }}>
              <IconButton
                onClick={handlePrevPage}
                disabled={!bookData.hasPrevious || isLoading}
                color="primary"
                sx={{
                  border: 1,
                  borderColor: 'divider',
                  '&:disabled': { borderColor: 'action.disabled' }
                }}
              >
                <NavigateBeforeIcon />
              </IconButton>

              <Button
                variant="contained"
                onClick={() => setNavOpen(!navOpen)}
                startIcon={navOpen ? <KeyboardArrowUpIcon /> : <MenuBookIcon />}
                sx={{
                  borderRadius: 3,
                  px: 3
                }}
              >
                {navOpen ? '닫기' : '목차'}
              </Button>

              <IconButton
                onClick={handleNextPage}
                disabled={!bookData.hasNext || isLoading}
                color="primary"
                sx={{
                  border: 1,
                  borderColor: 'divider',
                  '&:disabled': { borderColor: 'action.disabled' }
                }}
              >
                <NavigateNextIcon />
              </IconButton>
            </Box>
          </Paper>

          {/* 슬라이드 업 네비게이션 패널 - 모바일 */}
          <Slide direction="up" in={navOpen} mountOnEnter unmountOnExit>
            <Paper
              elevation={16}
              sx={{
                position: 'fixed',
                bottom: 64,
                left: 0,
                right: 0,
                maxHeight: '60vh',
                zIndex: 1199,
                borderRadius: '16px 16px 0 0',
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column'
              }}
            >
              <NavigationContent />
            </Paper>
          </Slide>

          {/* 백드롭 */}
          {navOpen && (
            <Box
              onClick={() => setNavOpen(false)}
              sx={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 64,
                bgcolor: 'rgba(0,0,0,0.3)',
                zIndex: 1198
              }}
            />
          )}
        </>
      )}

      {/* 계속 읽기 알림 */}
      <Snackbar
        open={showContinueReading}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        sx={{
          bottom: { xs: 80, md: 24 },
          right: { xs: 16, md: 24 }
        }}
      >
        <Alert
          severity="info"
          variant="filled"
          sx={{
            bgcolor: 'primary.main',
            color: 'white',
            borderRadius: 2,
            boxShadow: 4,
            '& .MuiAlert-icon': { color: 'white' },
            '& .MuiAlert-action': { pt: 0 }
          }}
          action={
            <Box sx={{ display: 'flex', gap: 1, ml: 1 }}>
              <Button
                size="small"
                variant="contained"
                onClick={handleContinueReading}
                sx={{
                  bgcolor: 'white',
                  color: 'primary.main',
                  '&:hover': { bgcolor: 'grey.100' },
                  fontWeight: 600,
                  minWidth: 'auto',
                  px: 1.5
                }}
              >
                이어읽기
              </Button>
              <IconButton
                size="small"
                onClick={handleStartFromBeginning}
                sx={{ color: 'white' }}
              >
                <CloseIcon fontSize="small" />
              </IconButton>
            </Box>
          }
        >
          <Typography variant="body2" sx={{ fontWeight: 500 }}>
            {savedProgress?.page + 1}페이지부터 계속 읽으시겠습니까?
          </Typography>
        </Alert>
      </Snackbar>
    </Box>
  )
}

export default BookDetail
