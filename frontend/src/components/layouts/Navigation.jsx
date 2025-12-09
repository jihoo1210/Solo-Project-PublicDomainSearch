import { Box, Button, Container, Typography, IconButton, Drawer, List, ListItem, ListItemText } from '@mui/material'
import MenuIcon from '@mui/icons-material/Menu'
import CloseIcon from '@mui/icons-material/Close'
import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useUser } from '../../contexts/UserContext'

const Navigation = () => {
    const navigate = useNavigate();
    const [isVisible, setIsVisible] = useState(true);
    const [lastScrollY, setLastScrollY] = useState(0);
    const [mobileOpen, setMobileOpen] = useState(false);
    const { isLoggedIn, logout } = useUser();

    const navItems = [
        { label: '홈', path: '/' },
        { label: '전체', path: '/list' },
        { label: '조회', path: '/search' },
        { label: '문의', path: '/contact' },
    ]

    useEffect(() => {
        const handleScroll = () => {
            const currentScrollY = window.scrollY;

            // 스크롤 다운 → 숨김, 스크롤 업 → 보임
            if (currentScrollY > lastScrollY && currentScrollY > 80) {
                setIsVisible(false);
            } else {
                setIsVisible(true);
            }

            setLastScrollY(currentScrollY);
        };

        window.addEventListener('scroll', handleScroll, { passive: true });
        return () => window.removeEventListener('scroll', handleScroll);
    }, [lastScrollY]);

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    // 모바일 드로어 콘텐츠
    const drawer = (
        <Box sx={{ width: 280, height: '100%', bgcolor: 'primary.main' }}>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', p: 2 }}>
                <IconButton onClick={handleDrawerToggle} sx={{ color: 'white' }}>
                    <CloseIcon />
                </IconButton>
            </Box>
            <List>
                {navItems.map((item) => (
                    <ListItem
                        key={item.label}
                        onClick={() => { navigate(item.path); handleDrawerToggle(); }}
                        sx={{
                            cursor: 'pointer',
                            '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                            '& .MuiListItemText-primary': {
                                color: 'white',
                                transition: 'color 0.2s ease-in-out',
                            },
                            '&:hover .MuiListItemText-primary': {
                                color: 'secondary.light',
                            }
                        }}
                    >
                        <ListItemText
                            primary={item.label}
                            sx={{ textAlign: 'center' }}
                        />
                    </ListItem>
                ))}
                <ListItem sx={{ flexDirection: 'column', gap: 2, mt: 2 }}>
                    <Button
                        fullWidth
                        variant="outlined"
                        sx={{ color: 'white', borderColor: 'white' }}
                        onClick={() => { isLoggedIn ? logout() : navigate('/signup'); handleDrawerToggle(); }}
                    >
                        {isLoggedIn ? '로그아웃' : '회원가입'}
                    </Button>
                    <Button
                        fullWidth
                        variant="contained"
                        color='secondary'
                        onClick={() => { isLoggedIn ? navigate('/mypage') : navigate('/login'); handleDrawerToggle(); }}
                    >
                        {isLoggedIn ? '마이페이지' : '로그인'}
                    </Button>
                </ListItem>
            </List>
        </Box>
    );

    return (
        <>
            {/* 네비게이션 바 */}
            <Box
                sx={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    zIndex: 1000,
                    bgcolor: 'rgba(0, 0, 0, 0.2)',
                    transform: isVisible ? 'translateY(0)' : 'translateY(-100%)',
                    transition: 'transform 0.3s ease-in-out',
                }}
            >
                <Container maxWidth="xl">
                    <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        py: { xs: 1.5, md: 2 }
                    }}>
                        {/* 로고 */}
                        <Typography
                            variant="h6"
                            sx={{
                                color: 'white',
                                fontWeight: 700,
                                cursor: 'pointer',
                                fontSize: { xs: '1.1rem', md: '1.25rem', width: '180px'}
                            }}
                            onClick={() => navigate('/')}
                        >
                            Brand
                        </Typography>

                        {/* 데스크톱 네비게이션 */}
                        <Box sx={{ display: { xs: 'none', md: 'flex' }, gap: 4 }}>
                            {navItems.map((item) => (
                                <Typography
                                    key={item.label}
                                    onClick={() => navigate(item.path)}
                                    sx={{
                                        color: 'white',
                                        cursor: 'pointer',
                                        fontSize: '0.95rem',
                                        transition: 'color 0.2s ease-in-out',
                                        '&:hover': {
                                            color: 'secondary.light',
                                        },
                                    }}
                                >
                                    {item.label}
                                </Typography>
                            ))}
                        </Box>

                        {/* 데스크톱 버튼 */}
                        <Box sx={{ display: { xs: 'none', md: 'flex' }, gap: 2 }}>
                            <Button
                                variant="outlined"
                                size="small"
                                sx={{
                                    color: 'white',
                                    borderColor: 'rgba(255,255,255,0.5)',
                                }}
                                onClick={() => navigate('/signup')}
                            >
                                회원가입
                            </Button>
                            <Button
                                variant="contained"
                                color="secondary"
                                size="small"
                                onClick={() => navigate('/login')}
                            >
                                로그인
                            </Button>
                        </Box>

                        {/* 모바일 메뉴 버튼 */}
                        <IconButton
                            sx={{ display: { xs: 'flex', md: 'none' }, color: 'white' }}
                            onClick={handleDrawerToggle}
                        >
                            <MenuIcon />
                        </IconButton>
                    </Box>
                </Container>
            </Box>

            {/* 모바일 드로어 */}
            <Drawer
                variant="temporary"
                anchor="right"
                open={mobileOpen}
                onClose={handleDrawerToggle}
                ModalProps={{ keepMounted: true }}
                sx={{
                    display: { xs: 'block', md: 'none' },
                    '& .MuiDrawer-paper': {
                        boxSizing: 'border-box',
                        width: 280,
                        bgcolor: 'primary.main'
                    },
                }}
            >
                {drawer}
            </Drawer>
        </>
    )
}

export default Navigation
