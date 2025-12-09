import { createTheme } from '@mui/material/styles';

// 자연에서 영감을 받은 색상 팔레트
const colors = {
  // Primary - 차분한 숲의 녹색
  primary: {
    main: '#2D5A47',
    light: '#4A7C68',
    dark: '#1E3D30',
    contrastText: '#FFFFFF',
  },
  // Secondary - 따뜻한 테라코타
  secondary: {
    main: '#C67B5C',
    light: '#D9A089',
    dark: '#A85D3F',
    contrastText: '#FFFFFF',
  },
  // 배경색 - 부드러운 오프 화이트
  background: {
    default: '#FAFAF8',
    paper: '#FFFFFF',
  },
  // 텍스트 - 진한 녹색
  text: {
    primary: '#2C3E35',
    secondary: '#5F7268',
    disabled: '#A0AFA6',
  },
  // 상태 색상
  // - 민트 그린
  success: {
    main: '#4CAF7C',
    light: '#7BC9A0',
    dark: '#2E8B57',
  },
  // - 골든 옐로우
  warning: {
    main: '#E5A84B',
    light: '#F0C478',
    dark: '#C4872E',
  },
  // - 코랄 레드
  error: {
    main: '#D66853',
    light: '#E59485',
    dark: '#B84A36',
  },
  // - 스틸 블루
  info: {
    main: '#5B8BA0',
    light: '#89B3C4',
    dark: '#3D6B7F',
  },
  // 추가 색상
  divider: '#E0E5E2',
};

const theme = createTheme({
  palette: {
    ...colors,
  },
  typography: {
    fontFamily: [
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
    ].join(','),
    h1: {
      fontWeight: 600,
      color: colors.text.primary,
    },
    h2: {
      fontWeight: 600,
      color: colors.text.primary,
    },
    h3: {
      fontWeight: 600,
      color: colors.text.primary,
    },
    h4: {
      fontWeight: 500,
      color: colors.text.primary,
    },
    h5: {
      fontWeight: 500,
      color: colors.text.primary,
    },
    h6: {
      fontWeight: 500,
      color: colors.text.primary,
    },
    body1: {
      color: colors.text.primary,
    },
    body2: {
      color: colors.text.secondary,
    },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 500,
          borderRadius: 8,
          padding: '8px 20px',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.15)',
          },
        },
        colorInherit: {
          '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.2)',
          },
        },
        contained: {
          boxShadow: 'none',
        },
        containedPrimary: {
          '&:hover': {
            backgroundColor: colors.primary.light,
          },
        },
        containedSecondary: {
          '&:hover': {
            backgroundColor: colors.secondary.light,
          },
        },
        outlined: {
          '&:hover': {
            backgroundColor: 'rgba(45, 90, 71, 0.08)',
          },
        },
        outlinedPrimary: {
          '&:hover': {
            backgroundColor: 'rgba(45, 90, 71, 0.08)',
            borderColor: colors.primary.light,
          },
        },
        outlinedSecondary: {
          '&:hover': {
            backgroundColor: 'rgba(198, 123, 92, 0.08)',
            borderColor: colors.secondary.light,
          },
        },
        text: {
          '&:hover': {
            backgroundColor: 'rgba(45, 90, 71, 0.08)',
          },
        },
      },
    },
    MuiLink: {
      styleOverrides: {
        root: {
          color: colors.primary.main,
          textDecoration: 'none',
          transition: 'color 0.2s ease-in-out',
          '&:hover': {
            color: colors.secondary.light,
            textDecoration: 'underline',
          },
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            backgroundColor: 'rgba(45, 90, 71, 0.12)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 12px rgba(0, 0, 0, 0.08)',
          borderRadius: 12,
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.08)',
        },
      },
    },
  },
});

export default theme;
