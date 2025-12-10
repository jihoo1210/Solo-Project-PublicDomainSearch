import React, { useState } from 'react'
import { Box, TextField, Button, Typography, Link, Grid, Divider } from '@mui/material'
import GoogleIcon from '@mui/icons-material/Google'
import { validateAndExecute } from '../../util/Handle'
import { useUser } from '../../contexts/UserContext'
import { useNavigate } from 'react-router-dom'

const Login = () => {
  const [formData, setFormData] = useState({ email: '', password: '' })
  const { login, checkInfo } = useUser();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    
    validateAndExecute(formData, (data) => {
        login(data)
        navigate("/")
    })
  }

  const handleGoogleLogin = () => {
    window.location.href = `${process.env.REACT_APP_API_URL}/auth/login/google`
    checkInfo()
  }

  const handleNaverLogin = () => {
    window.location.href = `${process.env.REACT_APP_API_URL}/auth/login/naver`
    checkInfo()
  }

  return (
    <Grid container sx={{ minHeight: '100vh' }}>
      {/* 좌측: 이미지/브랜딩 영역 */}
      <Grid
        size={{ xs: 12, md: 6 }}
        sx={{
          background: 'linear-gradient(135deg, #2D5A47 0%, #4A7C68 100%)',
          display: { xs: 'none', md: 'flex' },
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          color: 'white',
          p: 6,
        }}
      >
        <Typography variant="h3" sx={{ fontWeight: 700, mb: 2 }}>환영합니다</Typography>
        <Typography variant="h6" sx={{ opacity: 0.9, textAlign: 'center', maxWidth: 400 }}>
          새로운 경험을 시작하세요. 간편하게 가입하고 서비스를 이용해보세요.
        </Typography>
      </Grid>

      {/* 우측: 폼 영역 */}
      <Grid
        size={{ xs: 12, md: 6 }}
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 4,
        }}
      >
        <Box sx={{ width: '100%', maxWidth: 400 }}>
          <Typography variant="h4" sx={{ mb: 1, fontWeight: 600 }}>
            로그인
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 4 }}>
            계정에 로그인하세요
          </Typography>

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="이메일"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              margin="normal"
              variant="outlined"
            />
            <TextField
              fullWidth
              label="비밀번호"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              margin="normal"
              variant="outlined"
            />

            <Box sx={{ textAlign: 'right', mt: 1 }}>
              <Link component="button" variant="body2" onClick={(e) => {e.preventDefault(); navigate('/reset-password')}}>비밀번호 찾기</Link>
            </Box>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              sx={{ mt: 3, py: 1.5 }}
            >
              로그인
            </Button>
          </Box>

          <Divider sx={{ my: 3 }}>또는</Divider>

          {/* 소셜 로그인 버튼 */}
          <Button
            fullWidth
            variant="outlined"
            size="large"
            startIcon={<GoogleIcon />}
            onClick={handleGoogleLogin}
            sx={{
              py: 1.5,
              mb: 2,
              borderColor: '#dadce0',
              color: '#3c4043',
              '&:hover': {
                borderColor: '#dadce0',
                backgroundColor: '#f8f9fa',
              },
            }}
          >
            Google로 로그인
          </Button>

          <Button
            fullWidth
            variant="contained"
            size="large"
            onClick={handleNaverLogin}
            sx={{
              py: 1.5,
              backgroundColor: '#03C75A',
              color: 'white',
              '&:hover': {
                backgroundColor: '#02b351',
              },
            }}
          >
            네이버로 로그인
          </Button>

          <Typography variant="body2" sx={{ mt: 4, textAlign: 'center' }}>
            계정이 없으신가요?{' '}
            <Link onClick={(e) => {e.preventDefault(); navigate('/signup')}} sx={{ fontWeight: 600 }}>
              회원가입
            </Link>
          </Typography>
        </Box>
      </Grid>
    </Grid>
  )
}

export default Login
