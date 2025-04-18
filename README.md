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

// ... existing code ... 