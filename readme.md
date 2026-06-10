# Dodo Server Backend

유저 관심사 기반의 도도 서비스 백엔드 API 서버입니다.

## 🛠️ 기술 스택 (Tech Stack)

| 분류 | 기술 |
| :--- | :--- |
| **Framework** | Spring Boot 3.4.3 |
| **Auth** | Spring Security, Google OAuth 2.0, JWT |
| **Database** | MySQL 8.0, Redis 7.0 |
| **ORM / Query** | Spring Data JPA, Querydsl |
| **Infrastructure** | AWS (EC2, S3, CloudFront) |
| **DevOps** | Docker, Docker Compose, GitHub Actions |
| **Documentation** | Swagger (SpringDoc) |
| **Testing** | JUnit 5, JaCoCo (Coverage 70%+) |
| **Messaging** | Firebase Cloud Messaging (FCM) |

## 📊 DB 설계 (ERD)

프로젝트의 상세한 데이터베이스 구조 및 ERD는 아래 버튼을 클릭하여 확인할 수 있습니다.

[![ERD](https://img.shields.io/badge/Notion-ERD%20보기-black?style=for-the-badge&logo=notion)](https://ballistic-bone-7be.notion.site/ERD-335119ac972c80b19001c40ec6b06776?source=copy_link)

## 🏗️ 프로젝트 패키지 구조 (Package Structure)

도메인 주도 설계(DDD)를 지향하며, 각 도메인별로 책임이 분리된 패키지 구조를 가집니다.

```text
src/main/java/com/dodo/dodoserver/
├── 📂 domain             # 핵심 비즈니스 로직
│   ├── 📂 ad             # 광고주, 광고 관리
│   ├── 📂 admin          # 어드민 전용 기능 (통계, 제재, 데이터 관리 등)
│   ├── 📂 auth           # 사용자 인증/인가 (OAuth2, JWT, 토큰 관리)
│   ├── 📂 nest           # 둥지(게시물) 생성, 위치 기반 탐색 및 해금 로직
│   ├── 📂 user           # 사용자 프로필, 계정 및 디바이스 정보(FCM) 관리
│   ├── 📂 inquiry        # 사용자 1:1 문의 처리
│   ├── 📂 category       # 둥지 카테고리 관리
│   ├── 📂 notice         # 공지사항 관리
│   ├── 📂 postcard       # 엽서 CRUD, 교환
│   ├── 📂 report         # 게시물 및 댓글 신고 처리 시스템
│   └── 📂 mypage         # 마이페이지 관리
├── 📂 global             # 공통 설정 및 보안
│   ├── 📂 config         # DB, Redis, Security, Firebase 등 설정
│   ├── 📂 security       # JWT 필터 및 OAuth2 서비스 구현
│   └── 📂 common         # 공통 응답(ApiResponseDto) 및 유틸리티, 상수
├── 📂 infrastructure     # 외부 인프라 서비스
│   ├── 📂 s3             # AWS S3 파일 업로드
│   └── 📂 fcm            # Firebase 푸시 알림
└── 📂 error              # 예외 처리 및 에러 코드 정의
```

## 🔨 빌드 방법 (Build Instructions)

프로젝트 빌드 시 JaCoCo 테스트 커버리지 검증이 자동으로 수행됩니다.

### 1. 로컬 빌드 및 테스트
```bash
# 테스트 수행 및 커버리지 확인 (70% 하한선 검사 포함)
./gradlew check

# 빌드 및 JAR 생성
./gradlew build
```

### 2. 도커 빌드 및 배포 (Docker Build & Deploy)
프로젝트를 도커 이미지로 빌드하고 배포하는 스크립트입니다.
```bash
#!/bin/bash
set -e

# 1. 테스트 및 검증
./gradlew check --no-daemon

# 2. 도커 이미지 빌드
docker build -t <DOCKERHUB_USERNAME>/dodo-server:latest .

# 3. 도커 허브 푸시
docker push <DOCKERHUB_USERNAME>/dodo-server:latest

# 4. 컨테이너 실행
docker compose up -d
```

## 📊 테스트 커버리지 정책 (Test Coverage Policy)

코드 품질 관리 및 안정성 확보를 위해 **JaCoCo**를 통한 테스트 커버리지를 강제하고 있습니다.

### 1. 커버리지 기준 (Quality Gate)
- **전체 커버리지 하한선: 70%** (70% 미달 시 빌드 실패)
- **주요 측정 지표**:
    - **LINE**: 소스 코드 라인 기준 실행 비율
    - **INSTRUCTION**: 자바 바이트코드 명령 단위 실행 비율 (가장 정밀한 지표)
    - **BRANCH**: 조건문(`if`, `switch` 등)의 분기 실행 비율 (최소 50% 이상 권장)

### 2. 리포트 확인 방법
빌드 완료 후 로컬 환경에서 상세한 커버리지 리포트를 확인할 수 있습니다.
- **경로**: `build/reports/jacoco/test/html/index.html`
- 해당 파일을 브라우저로 열면 클래스별, 메서드별 상세 커버리지 현황(실행된 라인/미실행된 라인)을 시각적으로 확인할 수 있습니다.

### 3. 측정 제외 대상 (Exclusions)
순수 비즈니스 로직 검증에 집중하기 위해 다음 대상은 측정에서 제외됩니다.
- **QueryDSL 생성 클래스**: `**/Q*`
- **데이터 객체**: `**/*Dto*`, `**/*Request*`, `**/*Response*`
- **예외 처리 및 에러 코드**: `**/*Exception*`, `**/*ErrorCode*`
- **설정 및 보안**: `global/config/**`, `global/security/**`

## 🚀 CI/CD 로직 (CI/CD Pipeline)

본 프로젝트는 GitHub Actions와 Docker를 활용하여 자동화된 배포 프로세스를 따릅니다.

1. **CI (Continuous Integration)**
   - `develop` 브랜치에 코드가 `push`되거나 PR이 `merge`되면 트리거됩니다.
   - `./gradlew check`를 통해 테스트 및 커버리지를 검증하며, 실패 시 빌드가 중단됩니다.
2. **CD (Continuous Deployment)**
   - 검증된 코드를 기반으로 Docker 이미지를 빌드합니다.
   - 빌드된 이미지를 **Docker Hub**에 푸시합니다.
   - **EC2** 서버에 접속하여 최신 이미지를 `pull` 받고 `docker-compose up`을 통해 자동화 된 배포를 수행합니다.

## 🔑 GitHub Actions Secrets 설정

보안을 위해 환경 변수는 GitHub Secrets를 통해 런타임에 주입됩니다. 배포를 위해 다음 시크릿 설정이 필요합니다.

| 구분 | 시크릿 키 (Secret Key) | 설명 |
| :--- | :--- | :--- |
| **Infrastructure** | `EC2_HOST`, `EC2_SSH_KEY` | 배포 서버 접속 정보 |
| | `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN` | 도커 이미지 푸시 권한 |
| **Application** | `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL 데이터베이스 정보 |
| | `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | OAuth2 로그인 연동 |
| | `JWT_SECRET`, `JWT_EXPIRATION_*` | 토큰 발급 및 만료 설정 |
| | `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `AWS_REGION` | S3 스토리지 연동 |
| **External** | `FIREBASE_SERVICE_ACCOUNT` | FCM 알림 발송용 서비스 계정 JSON |

---

## 🚨 에러 핸들링 가이드 (Error Handling)

우리 프로젝트는 일관된 에러 응답 규격을 유지하기 위해 `ErrorCode`와 `BusinessException`을 중심으로 전역 예외 처리를 수행합니다.

### 1. 에러 응답 규격 (Error Response)
에러 발생 시 `ApiResponseDto`를 통해 다음 형식으로 응답합니다.
```json
{
  "status": "ERROR",
  "code": "U001",
  "message": "사용자를 찾을 수 없습니다.",
  "data": null
}
```

### 2. 예외 처리 흐름
1.  **ErrorCode 정의**: `com.dodo.dodoserver.error.ErrorCode` Enum에 새로운 에러 코드 추가.
2.  **비즈니스 예외 발생**: 서비스 레이어 등에서 조건 미충족 시 `BusinessException`을 던짐.
3.  **전역 처리**: `GlobalExceptionHandler`에서 해당 예외를 캐치하여 HTTP 상태 코드와 함께 응답.

### 3. 코드 작성 예시

#### Step 1: ErrorCode 추가
도메인별 접두사(User: U, Nest: N 등)를 사용하여 코드를 정의합니다.
```java
// ErrorCode.java
USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
```

#### Step 2: BusinessException 발생
비즈니스 로직 검증 실패 시 `BusinessException`을 활용합니다.
```java
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
}
```

#### Step 3: GlobalExceptionHandler (자동 처리)
정의된 모든 `BusinessException`은 별도의 추가 작업 없이 `ErrorCode`에 설정된 `HttpStatus`와 메시지로 클라이언트에게 전달됩니다.

### 4. 주의 사항
- 새로운 도메인을 추가할 때는 `ErrorCode`에 해당 도메인 전용 섹션을 만들어 관리합니다.
- 단순 유효성 검증(`@Valid`) 실패는 `INPUT_VALIDATION_ERROR(G005)` 코드로 자동 처리됩니다.

---

## 🤝 협업 규칙 (Collaboration Rules)

이 프로젝트는 원활한 협업과 코드 품질 관리를 위해 다음 규칙을 따릅니다.

### 1. 브랜치 전략 (Branch Strategy)
`Shared Repository` 모델을 사용하며, 브랜치명은 `kebab-case`를 사용합니다.
- **형식**: `type/issue-number/description`
- **예시**: `feat/12/login-api`, `fix/45/security-patch`

### 2. 커밋 컨벤션 (Commit Convention)
커밋 메시지에는 반드시 관련 이슈 번호를 포함합니다.
- **형식**: `type/#issue-number: subject`
- **예시**: `feat/#12: 로그인 API 구현`, `fix/#45: 토큰 만료 로직 수정`

| 커밋 유형 | 의미 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `refactor` | 코드 리팩토링 |
| `test` | 테스트 코드, 리팩토링 테스트 코드 추가 |
| `chore` | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
| `!HOTFIX` | 급하게 치명적인 버그를 고쳐야 하는 경우 |

### 3. 개발 원칙
- 모든 기능 구현 시 해당 기능을 검증하는 **테스트 코드를 반드시 포함**해야 합니다.
- **JaCoCo 커버리지 70%** 미달 시 빌드가 실패하므로 테스트 작성에 유의하세요.
- 공간 연산 쿼리 작성 시 `Querydsl`과 네이티브 쿼리를 적절히 혼용합니다.

