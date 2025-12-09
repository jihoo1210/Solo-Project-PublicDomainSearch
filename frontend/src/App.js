import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, Box } from '@mui/material';
import theme from './themes/Theme';
import { UserProvider } from './contexts/UserContext';
import Navigation from './components/layouts/Navigation';
import Main from './components/main/Main';
import Login from './components/auth/Login';
import Signup from './components/auth/Signup';
import ResetPassword from './components/auth/ResetPassword';
import BookSearch from './components/book/BookSearch';

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <UserProvider>
        <BrowserRouter>
          <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            <Navigation />
            <Box component="main" sx={{ flex: 1 }}>
              <Routes>
                <Route path="/" element={<Main />} />
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path='/reset-password' element={<ResetPassword />} />

                <Route path='/search' element={<BookSearch />} />
              </Routes>
            </Box>
            {/* Footer 추가 예정 */}
          </Box>
        </BrowserRouter>
      </UserProvider>
    </ThemeProvider>
  );
}

export default App;
