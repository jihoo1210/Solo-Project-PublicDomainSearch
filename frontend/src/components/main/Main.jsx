import React from 'react'
import { Box, Container, Typography, Button, Grid } from '@mui/material'
import { useNavigate } from 'react-router-dom'

// 레이아웃 3: 풀스크린 히어로 + 그리드 섹션
const Layout3 = ({ children }) => {

  const navigate = useNavigate();

  return (
    <Box>
      {/* 히어로 섹션 */}
      <Box
        sx={{
          minHeight: '100vh',
          background: 'linear-gradient(135deg, #2D5A47 0%, #1E3D30 100%)',
          display: 'flex',
          alignItems: 'center',
          color: 'white',
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ maxWidth: 600 }}>
            <Typography variant="h2" sx={{color: 'white', fontWeight: 700, mb: 3 }}>
            언어 장벽을 넘어,
            </Typography>
            <Typography variant="h6" sx={{color: 'white', mb: 4, opacity: 0.9 }}>
            시대를 초월한 고전을 경험하세요.
            </Typography>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button type='button' variant="contained" color="secondary" size="large" onClick={() => navigate('/search')}>조회하기</Button>
              <Button type='button' variant="outlined" sx={{ color: 'white', borderColor: 'white' }} size="large" onClick={() => navigate('/list')}>전체보기</Button>
            </Box>
          </Box>
        </Container>
      </Box>

      {/* 기능 섹션 */}
      <Box sx={{ py: 10, bgcolor: 'background.paper' }}>
        <Container maxWidth="lg">
          <Typography variant="h4" align="center" sx={{ mb: 6, fontWeight: 600 }}>
            주요 기능
          </Typography>
          <Grid container spacing={4}>
            {['빠른 속도', '안전한 보안', '쉬운 사용'].map((feature) => (
              <Grid size={{ xs: 12, md: 4 }} key={feature}>
                <Box sx={{ p: 4, bgcolor: 'grey.50', borderRadius: 3, textAlign: 'center' }}>
                  <Box sx={{ width: 60, height: 60, bgcolor: 'primary.main', borderRadius: '50%', mx: 'auto', mb: 2 }} />
                  <Typography variant="h6" sx={{ mb: 1 }}>{feature}</Typography>
                  <Typography color="text.secondary">
                    서비스 설명이 들어갑니다.
                  </Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>

      {/* 본문 */}
      {children && (
        <Box sx={{ py: 6 }}>
          <Container maxWidth="lg">{children}</Container>
        </Box>
      )}

      {/* 푸터 */}
      <Box sx={{ bgcolor: 'grey.900', color: 'white', py: 6 }}>
        <Container maxWidth="lg">
          <Grid container spacing={4}>
            <Grid size={{ xs: 12, md: 4 }}>
              <Typography variant="h6" sx={{ mb: 2 }}>Brand</Typography>
              <Typography variant="body2" sx={{ opacity: 0.7 }}>
                더 나은 서비스를 제공합니다.
              </Typography>
            </Grid>
            <Grid size={{ xs: 6, md: 2 }}>
              <Typography variant="subtitle2" sx={{ mb: 2 }}>서비스</Typography>
              {['기능', '가격', 'FAQ'].map((item) => (
                <Typography key={item} variant="body2" sx={{ opacity: 0.7, mb: 1, cursor: 'pointer' }}>{item}</Typography>
              ))}
            </Grid>
            <Grid size={{ xs: 6, md: 2 }}>
              <Typography variant="subtitle2" sx={{ mb: 2 }}>회사</Typography>
              {['소개', '채용', '블로그'].map((item) => (
                <Typography key={item} variant="body2" sx={{ opacity: 0.7, mb: 1, cursor: 'pointer' }}>{item}</Typography>
              ))}
            </Grid>
            <Grid size={{ xs: 12, md: 4 }}>
              <Typography variant="subtitle2" sx={{ mb: 2 }}>문의</Typography>
              <Typography variant="body2" sx={{ opacity: 0.7 }}>contact@brand.com</Typography>
            </Grid>
          </Grid>
        </Container>
      </Box>
    </Box>
  )
}

export default Layout3
