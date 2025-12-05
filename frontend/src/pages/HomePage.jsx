import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

const Page = styled.div`
  width: 100%;
  min-height: 100vh;
  background: ${({ theme }) => theme.colors.background};
  position: relative;
  overflow: hidden;
`;

const BackgroundSlide = styled.div`
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
`;

const SlideContent = styled.div`
  font-size: 1.2rem;
  color: ${({ theme }) => theme.colors.border};
  line-height: 2;
  white-space: pre-wrap;
  word-wrap: break-word;
  padding: ${({ theme }) => theme.spacing.lg};
  font-family: ${({ theme }) => theme.fonts.serif};

  @keyframes slideUp {
    0% {
      transform: translateY(0);
    }
    100% {
      transform: translateY(-100%);
    }
  }

  animation: slideUp 20s linear infinite;

  &:nth-child(2) {
    animation-delay: 10s;
  }
`;

const HeroSection = styled.section`
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: ${({ theme }) => theme.spacing.xl};
  position: relative;
  z-index: 10;

  @media (max-width: ${({ theme }) => theme.breakpoints.md}) {
    min-height: auto;
    padding: ${({ theme }) => theme.spacing.lg};
  }

  @media (max-width: ${({ theme }) => theme.breakpoints.sm}) {
    padding: ${({ theme }) => theme.spacing.md};
  }
`;

const Content = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-width: 700px;
  text-align: center;

  @keyframes slideUpText {
    from {
      opacity: 0;
      transform: translateY(40px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  animation: slideUpText 0.8s ease-out;
`;

const Title = styled.h1`
  font-size: clamp(2.8rem, 8vw, 5rem);
  margin: 0 0 ${({ theme }) => theme.spacing.lg} 0;
  line-height: 1.1;
  letter-spacing: -0.02em;
  font-weight: 700;

  @keyframes slideUpText {
    from {
      opacity: 0;
      transform: translateY(60px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  animation: slideUpText 0.8s ease-out 0.1s backwards;
`;

const Subtitle = styled.p`
  font-size: clamp(0.95rem, 2.5vw, 1.15rem);
  color: ${({ theme }) => theme.colors.textSecondary};
  max-width: 600px;
  margin: 0 0 ${({ theme }) => theme.spacing.xxl} 0;
  line-height: 1.7;
  font-weight: 400;

  @keyframes slideUpText {
    from {
      opacity: 0;
      transform: translateY(60px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  animation: slideUpText 0.8s ease-out 0.2s backwards;
`;

const SearchContainer = styled.div`
  width: 100%;
  max-width: 550px;
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing.md};

  @keyframes slideUpText {
    from {
      opacity: 0;
      transform: translateY(60px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  animation: slideUpText 0.8s ease-out 0.3s backwards;

  @media (max-width: ${({ theme }) => theme.breakpoints.sm}) {
    max-width: 100%;
  }
`;

const SearchForm = styled.form`
  display: flex;
  gap: ${({ theme }) => theme.spacing.sm};
  width: 100%;

  @media (max-width: ${({ theme }) => theme.breakpoints.sm}) {
    flex-direction: column;
  }
`;

const SearchInput = styled.input`
  flex: 1;
  padding: ${({ theme }) => theme.spacing.md};
  border: 1px solid ${({ theme }) => theme.colors.border};
  font-size: 1rem;
  font-family: inherit;
  transition: all ${({ theme }) => theme.transitions.normal};
  background: ${({ theme }) => theme.colors.white};

  &:focus {
    outline: none;
    border-color: ${({ theme }) => theme.colors.primary};
    box-shadow: 0 0 0 3px rgba(26, 26, 26, 0.05);
  }

  &::placeholder {
    color: ${({ theme }) => theme.colors.muted};
  }

  @media (max-width: ${({ theme }) => theme.breakpoints.sm}) {
    width: 100%;
  }
`;

const SearchButton = styled.button`
  padding: ${({ theme }) => theme.spacing.md} ${({ theme }) => theme.spacing.lg};
  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  border: none;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all ${({ theme }) => theme.transitions.normal};
  white-space: nowrap;

  &:hover {
    background: ${({ theme }) => theme.colors.accentAlt};
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(26, 26, 26, 0.15);
  }

  &:active {
    transform: translateY(0);
  }

  @media (max-width: ${({ theme }) => theme.breakpoints.sm}) {
    width: 100%;
  }
`;

const Footer = styled.div`
  font-size: 0.9rem;
  color: ${({ theme }) => theme.colors.muted};
  text-align: center;
  margin-top: ${({ theme }) => theme.spacing.xl};

  @keyframes slideUpText {
    from {
      opacity: 0;
      transform: translateY(60px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  animation: slideUpText 0.8s ease-out 0.4s backwards;
`;

// 슬라이드될 텍스트 생성
const generateSlideText = () => {
  const texts = [
    '세계의 고전 문학을 발견하세요',
    '저작권 만료된 도서들의 무한한 세계',
    '제목, 저자, 키워드로 검색하세요',
    '공개 도메인 도서의 보물창고',
    '고전의 가치를 현대에 담다',
    '무료로 읽는 세계 문학',
    '도서 탐색을 시작하세요',
    '문학의 거인들과 만나다',
    '시간을 초월한 이야기들',
    '지혜의 흔적을 따라가다',
  ];

  return texts.map((text, i) => `${text}\n`).join('');
};

export default function HomePage() {
  const [query, setQuery] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/search?q=${encodeURIComponent(query)}`);
    }
  };

  const slideText = generateSlideText();

  return (
    <Page>
      <BackgroundSlide>
        <SlideContent>{slideText}</SlideContent>
        <SlideContent>{slideText}</SlideContent>
      </BackgroundSlide>

      <HeroSection>
        <Content>
          <Title>PublicDomain</Title>
          <Subtitle>
            세계의 공개 도메인 도서를 검색하고 무료로 읽어보세요.
          </Subtitle>

          <SearchContainer>
            <SearchForm onSubmit={handleSearch}>
              <SearchInput
                type="text"
                placeholder="제목, 저자, 키워드로 검색..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                autoFocus
              />
              <SearchButton type="submit">검색</SearchButton>
            </SearchForm>
          </SearchContainer>
        </Content>

        <Footer>
          수천 권의 도서를 무료로 탐색하세요
        </Footer>
      </HeroSection>
    </Page>
  );
}
