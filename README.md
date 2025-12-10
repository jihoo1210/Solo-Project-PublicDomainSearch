# Public Domain Book Search

퍼블릭 도메인 도서를 검색하고 읽을 수 있는 웹 애플리케이션입니다.
[Gutendex API](https://gutendex.com/)를 활용하여 Project Gutenberg의 무료 전자책을 제공합니다.

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security (JWT 인증)
- Spring Data JPA
- AWS S3 (도서 데이터 저장)
- MySQL

### Frontend
- React 18
- Material UI (MUI)
- React Router
- Axios

## 프로젝트 구조

```
├── backend/
│   └── src/main/java/com/example/backend/
│       ├── controller/          # REST API 컨트롤러
│       ├── service/             # 비즈니스 로직
│       ├── repository/          # JPA 레포지토리
│       ├── entity/              # JPA 엔티티
│       ├── dto/                 # 데이터 전송 객체
│       ├── security/            # JWT, Security 설정
│       └── config/              # 애플리케이션 설정
│
└── frontend/
    └── src/
        ├── components/          # React 컴포넌트
        │   ├── auth/            # 인증 관련 (Login, Signup)
        │   ├── book/            # 도서 관련 (BookSearch, BookDetail)
        │   ├── layouts/         # 레이아웃 (Navigation)
        │   └── main/            # 메인 페이지
        ├── contexts/            # React Context (UserContext)
        ├── api/                 # Axios API 설정
        └── themes/              # MUI 테마 설정
```

## 주요 기능

### 1. 사용자 인증
- 회원가입 / 로그인 (이메일, 비밀번호)
- OAuth 로그인 (Google, Kakao)
- JWT 토큰 기반 인증 (HttpOnly Cookie)

### 2. 도서 검색
- **키워드 검색**: 제목, 저자명으로 검색
- **카테고리 필터**: Fiction, Poetry, Drama, Philosophy, History
- **자동완성**: 저장된 도서 데이터 기반 실시간 자동완성
- **페이지네이션**: 더보기 버튼으로 추가 결과 로드

### 3. 도서 상세 보기
- **페이지 단위 로딩**: 한 번에 50문장씩 로드 (성능 최적화)
- **페이지 네비게이션**: 이전/다음 페이지 버튼
- **페이지 선택**: 우측 Drawer에서 원하는 페이지로 바로 이동
- **문단 구분**: 가독성 높은 문단별 표시

## API 엔드포인트

### 인증 API (`/api/auth`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/signup` | 회원가입 |
| POST | `/login/local` | 로컬 로그인 |
| POST | `/logout` | 로그아웃 |
| GET | `/myInfo` | 내 정보 조회 |

### 도서 API (`/api/users`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/books?query={검색어}&topic={카테고리}` | 도서 검색 |
| GET | `/books/next?nextUrl={url}` | 다음 페이지 로드 |
| GET | `/books/autocomplete` | 자동완성 데이터 |
| GET | `/books/{bookId}?page={페이지번호}` | 도서 상세 (페이지네이션) |

## 환경 설정

### Backend (`application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password

cloud:
  aws:
    s3:
      bucket: your-bucket-name
    credentials:
      access-key: your-access-key
      secret-key: your-secret-key
    region:
      static: ap-northeast-2

jwt:
  secret: your-jwt-secret-key
```

### Frontend (`.env`)
```
REACT_APP_API_URL=http://localhost:8080/api
```

## 실행 방법

### Backend
```bash
cd backend
./gradlew bootRun
```

### Frontend
```bash
cd frontend
npm install
npm start
```

## 데이터 흐름

```
1. 도서 검색 요청
   Frontend → Backend → Gutendex API → Backend → Frontend

2. 도서 상세 조회 (최초)
   Frontend → Backend → Gutendex (텍스트 다운로드) → 파싱 → S3 저장 → Frontend

3. 도서 상세 조회 (캐시됨)
   Frontend → Backend → S3 조회 → Frontend
```

## 외부 API

- **Gutendex API**: https://gutendex.com/
  - Project Gutenberg 도서 메타데이터 및 텍스트 제공
  - 파라미터: `search`, `topic`, `languages`, `copyright` 등
