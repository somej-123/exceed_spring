# Spring Boot JWT 인증 시스템 구현

이 프로젝트는 Spring Boot를 사용하여 JWT(JSON Web Token) 기반의 사용자 인증 시스템을 구현한 예제입니다.

## 기술 스택

- Java 17
- Spring Boot 3.4.4
- Spring Security
- PostgreSQL
- MyBatis
- JWT (JSON Web Token)
- Lombok
- Gradle

## 주요 기능

### 1. 사용자 인증
- 회원가입 (`/api/users/register`)
- 로그인 (`/api/users/login`)
- 사용자 정보 조회 (`/api/users/me`)

### 2. JWT 인증 처리
- JWT 토큰 생성 및 검증
- Spring Security를 통한 인증 처리
- 보호된 엔드포인트에 대한 접근 제어

### 리프레시 토큰(Refresh Token) 기반 인증 기능 추가 (2024-06-07)

### 1. 백엔드
- 로그인 시 access token과 refresh token을 모두 발급, refresh token은 DB(user_info.refresh_token)에 저장
- access token 만료 시 /api/users/refresh 엔드포인트에서 refresh token으로 새 access/refresh token 재발급
- JwtUtil에 refresh 토큰 생성/만료 검증 메서드 추가, application.yml에 refresh-expiration(2주) 설정
- UserInfoService/UserInfoMapper에 refresh token 저장/조회/갱신 메서드 및 쿼리 추가
- DB user_info 테이블에 refresh_token 컬럼(VARCHAR(512)) 추가 필요

### 2. 프론트엔드(React)
- 로그인 시 응답의 token, refreshToken 모두 localStorage에 저장
- access token 만료(401/403) 시 refresh token으로 /api/users/refresh 호출, 새 토큰으로 갱신 후 재시도
- refresh token도 만료/불일치 시 자동 로그아웃 처리

### 3. API 예시
- POST /api/users/refresh
  - 요청: { "refreshToken": "..." }
  - 응답: { "accessToken": "...", "refreshToken": "...", "userId": "..." }

### 4. DB 컬럼 추가 쿼리
```sql
ALTER TABLE public.user_info ADD COLUMN refresh_token VARCHAR(512);
```

### 5. 기타
- 기존 회원도 문제없이 사용 가능(컬럼 NULL 허용)
- refresh token은 보안상 탈취/유출에 주의, 로그아웃 시 반드시 삭제

## 프로젝트 구조

```
src/main/java/com/blog/exceed/
├── config/
│   ├── SecurityConfig.java        # Spring Security 설정
│   └── JwtAuthenticationFilter.java   # JWT 인증 필터
├── controller/
│   └── UserInfoController.java    # 사용자 관련 API 컨트롤러
├── service/
│   └── UserInfoService.java       # 사용자 관련 비즈니스 로직
├── mapper/
│   └── UserInfoMapper.java        # MyBatis 매퍼 인터페이스
├── dao/
│   └── UserInfoDao.java           # 데이터 접근 객체
├── dto/
│   ├── LoginRequest.java          # 로그인 요청 DTO
│   └── LoginResponse.java         # 로그인 응답 DTO
└── util/
    └── JwtUtil.java              # JWT 유틸리티 클래스
```

## 주요 구현 사항

### 1. JWT 인증 구현
```java
@Component
public class JwtUtil {
    private final Key key;
    private final long validityInMilliseconds = 3600000; // 1시간

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 토큰 생성, 검증, 사용자 ID 추출 메서드 구현
}
```

### 2. Spring Security 설정
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                .anyRequest().authenticated()
            );
        // JWT 필터 추가
        return http.build();
    }
}
```

### 3. JWT 인증 필터
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) {
        // JWT 토큰 추출 및 검증
        // 인증 정보 설정
    }
}
```

## API 엔드포인트

### 1. 회원가입
- **URL:** `/api/users/register`
- **Method:** POST
- **Request Body:**
  ```json
  {
      "userId": "user123",
      "password": "password123"
  }
  ```
- **Response:**
  ```json
  {
      "message": "회원가입이 완료되었습니다."
  }
  ```

### 2. 로그인
- **URL:** `/api/users/login`
- **Method:** POST
- **Request Body:**
  ```json
  {
      "userId": "user123",
      "password": "password123"
  }
  ```
- **Response:**
  ```json
  {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "userId": "user123",
      "message": "로그인이 성공적으로 완료되었습니다."
  }
  ```

### 3. 로그아웃
- **URL:** `/api/users/logout`
- **Method:** POST
- **Headers:** 
  ```
  Authorization: Bearer {JWT_TOKEN}
  ```
- **Response:**
  ```json
  {
      "message": "로그아웃이 성공적으로 완료되었습니다."
  }
  ```

### 4. 사용자 정보 조회
- **URL:** `/api/users/me`
- **Method:** GET
- **Headers:** 
  ```
  Authorization: Bearer {JWT_TOKEN}
  ```
- **Response:**
  ```json
  {
      "userId": "user123"
  }
  ```

## 보안 특징

1. **비밀번호 암호화**
   - BCrypt를 사용한 안전한 비밀번호 해싱
   - 평문 비밀번호 저장 방지

2. **JWT 토큰 보안**
   - 토큰 만료 시간 설정 (1시간)
   - 안전한 서명 키 사용
   - 토큰 검증 메커니즘 구현

3. **Spring Security**
   - 인증되지 않은 접근 차단
   - CSRF 보호
   - 세션리스(Stateless) 인증 구현

## 설정 파일 (application.yml)

```yaml
jwt:
  secret: exceednewsecretkey12345678901234567890123456789012

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

## 시작하기

1. PostgreSQL 데이터베이스 설정
2. application.yml 파일에서 데이터베이스 접속 정보 수정
3. Gradle을 사용하여 프로젝트 빌드
   ```bash
   ./gradlew build
   ```
4. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```

## 참고사항

- JWT 시크릿 키는 실제 운영 환경에서 더 복잡하고 안전한 값으로 변경해야 합니다.
- 데이터베이스 접속 정보는 환경 변수나 외부 설정으로 관리하는 것이 좋습니다.
- 로그인 시도 횟수 제한, IP 차단 등의 추가적인 보안 기능을 고려해볼 수 있습니다.

## 프론트엔드 JWT 인증 처리

### 1. JWT 토큰 저장
```javascript
// 로그인 성공 후 JWT 토큰 저장
const handleLogin = async (userId, password) => {
  try {
    const response = await axios.post('/api/users/login', {
      userId,
      password
    });
    
    // JWT 토큰을 localStorage에 저장
    localStorage.setItem('token', response.data.token);
    // 사용자 정보 저장
    localStorage.setItem('userId', response.data.userId);
    
    // 로그인 성공 처리 (예: 메인 페이지로 리다이렉트)
    navigate('/main');
  } catch (error) {
    console.error('로그인 실패:', error);
  }
};
```

### 2. API 요청 시 JWT 토큰 사용
```javascript
// axios 인터셉터 설정
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
```

### 3. 인증 상태 관리 (React Context 예시)
```javascript
// AuthContext.js
import { createContext, useState, useContext } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));

  const login = (token, userId) => {
    localStorage.setItem('token', token);
    localStorage.setItem('userId', userId);
    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### 4. 보호된 라우트 구현
```javascript
// PrivateRoute.js
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

const PrivateRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  
  return isAuthenticated ? children : <Navigate to="/login" />;
};

// App.js에서 사용
const App = () => {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/protected"
          element={
            <PrivateRoute>
              <ProtectedPage />
            </PrivateRoute>
          }
        />
      </Routes>
    </AuthProvider>
  );
};
```

### 5. 토큰 만료 처리
```javascript
// axios 응답 인터셉터
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 토큰 만료 시 처리
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      // 로그인 페이지로 리다이렉트
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 보안 고려사항

1. **토큰 저장**
   - localStorage 대신 더 안전한 httpOnly 쿠키 사용 고려
   - XSS 공격 방지를 위한 추가 보안 조치 필요

2. **토큰 갱신**
   - 액세스 토큰 만료 시 자동 갱신 메커니즘 구현
   - 리프레시 토큰 활용 고려

3. **로그아웃**
   - 클라이언트 측 토큰 삭제
   - 필요한 경우 서버 측 토큰 무효화

4. **에러 처리**
   - 네트워크 오류
   - 인증 실패
   - 토큰 만료
   - 서버 오류 

## 로그아웃 처리 구현

### 1. 토큰 블랙리스트 서비스
```java
@Service
public class TokenBlacklistService {
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    // 만료된 토큰을 주기적으로 제거하는 스케줄러
    public TokenBlacklistService() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::removeExpiredTokens, 0, 1, TimeUnit.HOURS);
    }

    // 토큰을 블랙리스트에 추가
    public void addToBlacklist(String token, long expirationTime) {
        blacklist.put(token, expirationTime);
    }

    // 토큰이 블랙리스트에 있는지 확인
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }
}
```

### 2. JWT 필터에서의 블랙리스트 확인
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                              HttpServletResponse response, 
                              FilterChain filterChain) {
    String token = extractToken(request);
    if (token != null && jwtUtil.validateToken(token)) {
        // 토큰이 블랙리스트에 있는지 확인
        if (tokenBlacklistService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("토큰이 무효화되었습니다.");
            return;
        }
        // ... 인증 처리 ...
    }
}
```

### 3. 로그아웃 프로세스 상세 설명

#### 백엔드 처리
1. **토큰 추출 및 검증**
   ```java
   String token = authHeader.substring(7);
   Date expiration = jwtUtil.getExpirationDateFromToken(token);
   ```

2. **블랙리스트 처리**
   ```java
   tokenBlacklistService.addToBlacklist(token, expirationTime);
   ```

3. **보안 컨텍스트 정리**
   ```java
   SecurityContextHolder.clearContext();
   ```

#### 프론트엔드 처리
1. **로그아웃 요청 전송**
   ```javascript
   const token = localStorage.getItem('token');
   await axios.post('/api/users/logout', null, {
     headers: {
       'Authorization': `Bearer ${token}`
     }
   });
   ```

2. **로컬 스토리지 정리**
   ```javascript
   localStorage.clear(); // 모든 인증 관련 데이터 제거
   ```

3. **상태 관리 초기화**
   ```javascript
   // Redux를 사용하는 경우
   dispatch(resetAuthState());
   
   // React Context를 사용하는 경우
   authContext.reset();
   ```

### 4. 보안 강화 방안

1. **토큰 저장소 보안**
   - Redis를 사용한 블랙리스트 관리
   ```java
   @Service
   public class TokenBlacklistService {
       private final RedisTemplate<String, String> redisTemplate;
       
       public void addToBlacklist(String token, long expirationTime) {
           Duration ttl = Duration.ofMillis(expirationTime - System.currentTimeMillis());
           redisTemplate.opsForValue().set(token, "blacklisted", ttl);
       }
   }
   ```

2. **분산 환경 지원**
   ```java
   @Configuration
   public class RedisConfig {
       @Bean
       public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
           RedisTemplate<String, String> template = new RedisTemplate<>();
           template.setConnectionFactory(connectionFactory);
           template.setKeySerializer(new StringRedisSerializer());
           template.setValueSerializer(new StringRedisSerializer());
           return template;
       }
   }
   ```

3. **토큰 재사용 방지**
   ```java
   public class JwtAuthenticationFilter extends OncePerRequestFilter {
       @Override
       protected void doFilterInternal(...) {
           if (tokenBlacklistService.isBlacklisted(token)) {
               response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
               response.setContentType("application/json");
               response.getWriter().write("{\"error\":\"토큰이 무효화되었습니다.\"}");
               return;
           }
       }
   }
   ```

4. **로그아웃 이벤트 처리**
   ```java
   @Component
   public class LogoutEventListener {
       @EventListener
       public void handleLogoutEvent(LogoutSuccessEvent event) {
           // 추가적인 정리 작업 수행
           // 예: 사용자 세션 정보 정리, 활성 연결 종료 등
       }
   }
   ```

### 5. 모니터링 및 디버깅

1. **로그아웃 감사 로그**
   ```java
   @Slf4j
   @Service
   public class LogoutAuditService {
       public void logLogoutEvent(String userId, String token) {
           log.info("Logout event - User: {}, Token: {}, Time: {}", 
               userId, maskToken(token), LocalDateTime.now());
       }
       
       private String maskToken(String token) {
           // 토큰의 일부만 로그에 기록
           return token.substring(0, 10) + "...";
       }
   }
   ```

2. **블랙리스트 모니터링**
   ```java
   @Scheduled(fixedRate = 3600000) // 1시간마다
   public void reportBlacklistStatus() {
       int size = blacklist.size();
       log.info("Current blacklist size: {}", size);
       if (size > 10000) {
           log.warn("Blacklist size exceeds threshold");
       }
   }
   ```

# 블로그 백엔드 개발 작업 내역 (2025-05-12 오전 02:44, KST)

## 1. 테이블 설계 및 생성
- 기존 user_info 테이블 구조에 맞춰 블로그 관련 테이블(blog_post, category, tag, post_tag, comment, post_like, attachment) 설계 및 DDL 작성
- 모든 사용자 관련 외래키는 user_info(id)를 참조하도록 설계
- 테이블 삭제(DROP) 및 재생성 SQL도 함께 정리

### 오늘 추가로 생성한 블로그 관련 DB 테이블 목록 및 설명

| 테이블명      | 주요 컬럼 및 설명 |
|--------------|------------------|
| **blog_post** | id, title, content, author_id, created_at, updated_at, is_deleted, view_count, summary, thumbnail_url, category_id, is_published, like_count, comment_count, slug (블로그 글 정보) |
| **category**  | id, name, description, created_at (글 카테고리) |
| **tag**       | id, name (태그) |
| **post_tag**  | post_id, tag_id (글-태그 N:M 매핑) |
| **comment**   | id, post_id, author_id, content, parent_id, created_at, updated_at, is_deleted (댓글 및 대댓글) |
| **post_like** | id, post_id, user_id, created_at (글 좋아요) |
| **attachment**| id, post_id, file_url, file_type, file_size, uploaded_at (첨부파일/이미지) |

- 각 테이블의 외래키는 user_info(id) 또는 blog_post(id) 등으로 연결되어 데이터 무결성 보장
- 테이블 생성/삭제 DDL, 컬럼 타입 등은 README 또는 별도 SQL 파일 참고

## 2. 파일 업로드 및 이미지 저장 경로
- 이미지 업로드 API(`/api/blog/upload-image`) 구현
- 업로드 경로를 application.yml에서 `blog.image.upload-dir`로 관리 (예: C:/projectImage)
- 업로드 파일 최대 크기 50MB로 제한 (application.yml 및 컨트롤러에서 이중 체크)
- 업로드 성공 시, 실제 파일의 원본 경로(절대경로)와 접근용 URL을 모두 응답에 포함

## 3. 이미지 정적 리소스 서빙
- Spring WebMvcConfigurer를 활용해 `/images/**` URL을 `C:/projectImage/` 폴더와 매핑
- 프론트엔드에서 백엔드 서버의 `/images/파일명`으로 접근 시 실제 파일 반환

## 4. 예외 및 에러 처리
- 업로드 파일 미첨부, 크기 초과, IO 예외 등 상황별로 명확한 에러 메시지 반환
- 프론트엔드에서 axios 등으로 업로드 시, 표준화된 에러 응답 처리 가능

## 5. 프론트엔드 연동 및 주의사항
- 파일 업로드 시 form-data의 key는 반드시 `file`이어야 함
- 프론트엔드에서 이미지 접근 시 백엔드 서버의 `/images/파일명` URL 사용
- Vite 등 개발 서버의 `/images` 경로와는 별개이므로, 반드시 백엔드로 요청해야 함

## 6. 기타
- application.yml, BlogController, WebConfig 등 주요 파일 수정
- 서버 재시작 필요 사항, 경로/권한 문제, CORS 등 실무 팁 안내

---

**최종 점검:**
- 테이블 구조, 파일 업로드, 이미지 접근, 에러 처리 등 블로그 백엔드의 핵심 기능을 모두 구현 및 점검함
- 추가 문의/요구사항 발생 시 README에 계속 업데이트 예정