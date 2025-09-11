# 🤖 Claude 컨텍스트 관리 시스템

이 디렉토리는 Claude AI가 StockQuest 프로젝트를 일관되게 개발할 수 있도록 하는 컨텍스트 관리 시스템입니다.

## 📁 디렉토리 구조

```
.claude/
├── README.md                    # 이 파일
├── AUTOSTART.md                # ⭐ 작업 시작 시 필수 읽기
├── PROJECT_CONTEXT.md          # 프로젝트 전체 맥락과 목표
├── ARCHITECTURE_RULES.md       # 헥사고날 아키텍처 규칙
├── DEVELOPMENT_PATTERNS.md     # 개발 패턴과 템플릿
├── CURRENT_STATE.md            # 현재 프로젝트 상태
└── REFRESH_CONTEXT.md          # 컨텍스트 갱신 방법
```

## 🎯 목적

1. **일관성 보장**: 세션이 바뀌어도 동일한 아키텍처와 패턴 유지
2. **컨텍스트 유지**: 프로젝트의 현재 상태와 목표 기억
3. **자동화**: 반복적인 설명 없이 효율적인 개발
4. **품질 보장**: 아키텍처 규칙과 코딩 컨벤션 준수

## 🚀 사용 방법

### Claude 작업 시작 시
```markdown
1. .claude/AUTOSTART.md 읽기 (필수)
2. 현재 git 브랜치 확인
3. 도커 서비스 상태 확인
4. 관련 도메인 컨텍스트 파악
```

### 새 기능 개발 시
```markdown
1. ARCHITECTURE_RULES.md 확인
2. DEVELOPMENT_PATTERNS.md 참조
3. 기존 패턴과 일관성 검증
4. 테스트 작성 후 구현
```

### 사용자 명령어
- `--refresh` : 전체 컨텍스트 갱신
- `--status` : 현재 상태 요약  
- `--check` : 아키텍처 규칙 준수 확인
- `--sync` : 코드와 문서 동기화

## ⚠️ 중요 규칙

### 절대 변경하지 말 것
- Hexagonal Architecture 패턴
- Feature-Sliced Design 구조
- Domain Layer 순수성
- 네이밍 컨벤션

### 항상 확인할 것
- 의존성 방향 (Domain ← Application ← Adapter)
- 테스트 커버리지 목표 (80%)
- 보안 규칙 준수
- 에러 처리 패턴

## 📊 자동 추적 항목

- 코드 라인 수 및 복잡도
- 테스트 커버리지
- 완성된 기능 개수
- 기술 부채 항목
- 성능 메트릭

## 🔄 갱신 주기

- **일일**: git status, docker 서비스 확인
- **주간**: 진행도 업데이트, 메트릭 재계산
- **월간**: 아키텍처 규칙 검토, 패턴 개선

## 💡 베스트 프랙티스

1. **작업 시작 전 반드시 AUTOSTART.md 읽기**
2. **새 기능은 Domain → Application → Adapter 순서로 개발**
3. **모든 변경사항에 대해 테스트 작성**
4. **커밋 전 아키텍처 규칙 준수 확인**

---

**🎯 핵심**: 이 시스템을 통해 Claude는 언제나 StockQuest 프로젝트의 철학과 아키텍처를 유지하며 개발할 수 있습니다.

**📅 마지막 업데이트**: 2025-09-11  
**👤 관리자**: seongmin1117