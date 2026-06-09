# Dodo Server Backend

유저 관심사 기반의 도도 서비스 백엔드 API 서버입니다.

## 🏗️ 프로젝트 패키지 구조 (Package Structure)

도메인 주도 설계(DDD)를 지향하며, 각 도메인별로 책임이 분리된 패키지 구조를 가집니다.

```text
src/main/java/com/dodo/dodoserver/
├── 📂 domain             # 핵심 비즈니스 로직
│   ├── 📂 ad             # 광고 관리
│   ├── 📂 admin          # 관리자 기능 (통계, 유저/게시물 관리 등)
│   ├── 📂 auth           # 인증/인가 (OAuth2, JWT)
│   ├── 📂 nest           # 둥지(게시물) 및 공간 데이터 관리
│   ├── 📂 user           # 사용자 프로필 및 계정 관리
│   └── (기타 도메인: inquiry, category, notice, postcard, report, mypage)
├── 📂 global             # 공통 설정 및 보안
│   ├── 📂 config         # DB, Redis, Security, Firebase 등 설정
│   ├── 📂 security       # JWT 필터 및 OAuth2 서비스 구현
│   └── 📂 common         # 공통 응답(ApiResponseDto) 및 유틸리티
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

