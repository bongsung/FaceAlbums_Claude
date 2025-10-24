# Face Album App - 주요 수정사항

## 개요
사용자가 선택한 폴더의 사진들을 분석하여 얼굴을 감지하고, 인물별로 앨범을 만드는 앱으로 수정했습니다.

## 핵심 기능 변경사항

### 1. 폴더 선택 기반 동작
- **이전**: MediaStore 전체 스캔
- **변경**: 사용자가 선택한 특정 폴더만 스캔
- **구현**: 
  - FolderAwareMediaScanner 클래스 추가
  - 폴더 선택 UI (Android Storage Access Framework 사용)
  - WatchFolder 엔티티로 감시 폴더 관리

### 2. 3개 탭 구조
- **Folders 탭**: 선택된 폴더 목록 표시
- **People 탭**: 생성된 인물 앨범 표시 
- **New Faces 탭**: 아직 분류되지 않은 얼굴 표시

### 3. 얼굴 추가 워크플로우
1. 새로운 얼굴 발견 시 New Faces 탭에 표시
2. 사용자가 얼굴 클릭
3. 이름 입력 다이얼로그 표시
4. 새 인물 생성 또는 기존 인물에 추가
5. 해당 인물 앨범에 사진 링크 (물리적 복사 없음)

### 4. 실시간 감시
- ContentObserver 대신 폴더별 감시 구현
- 새 사진 추가 시 자동 얼굴 감지
- 기존 인물과 매칭 또는 새 얼굴로 분류

## 주요 컴포넌트 수정

### UI 레이어
```kotlin
// HomeScreen.kt - 새로운 구조
- 3개 탭 네비게이션
- 폴더 선택 FAB
- 새 얼굴 배지 표시
- AddFaceDialog 컴포넌트

// HomeViewModel.kt 
- watchFolders: StateFlow<List<WatchFolder>>
- unassignedFaces: StateFlow<List<Face>>
- addWatchFolder(path: String)
- addFaceAsPerson(face: Face, name: String)
```

### 도메인 레이어
```kotlin
// 새로운 Use Cases
- ScanFolderUseCase
- AddFaceToPersonUseCase
- GetUnassignedFacesUseCase

// Repository 인터페이스 확장
- FaceRepository.getUnassignedFacesFlow()
- SettingsRepository.getWatchFolders()
```

### 데이터 레이어
```kotlin
// 엔티티 수정
- WatchFolder: 감시 폴더 정보
- Face: 인물 연결 없는 얼굴도 저장
- LinkPersonPhoto: 다대다 관계 유지

// DAO 수정
- FaceDao: getUnassignedFaces() 추가
- WatchFolderDao: 폴더 CRUD
```

### 워커
```kotlin
// FolderScanWorker (새로 추가)
- 특정 폴더 스캔
- 얼굴 감지 및 저장
- 진행률 리포트

// 수정된 워커들
- MediaSyncWorker: 폴더별 동기화
- ClusteringWorker: 새 얼굴만 처리
```

## 데이터 흐름

### 폴더 추가 시
1. 사용자가 폴더 선택 (Storage Access Framework)
2. WatchFolder 엔티티 생성
3. FolderScanWorker 큐잉
4. 폴더 내 사진 스캔
5. 얼굴 감지 및 임베딩 생성
6. 새 얼굴을 New Faces 탭에 표시

### 새 얼굴 추가 시
1. 사용자가 New Faces 탭에서 얼굴 선택
2. 이름 입력 다이얼로그
3. Person 엔티티 생성/선택
4. LinkPersonPhoto로 연결
5. People 탭에 앨범 표시

### 실시간 감시
1. 폴더별 FileObserver 또는 주기적 스캔
2. 새 파일 감지 시 FaceDetection
3. 기존 Person과 매칭 (임베딩 유사도)
4. 자동 추가 또는 제안 생성

## 기술적 특징

### 성능 최적화
- 폴더별 선택적 스캔
- 이미 처리된 사진 스킵 (processedAt 체크)
- SHA-256 해시로 중복 방지
- 임베딩 캐싱

### 프라이버시
- 온디바이스 처리
- 사진 물리적 복사 없음 (링크만)
- 외부 서버 통신 없음

### 사용성
- 직관적인 3탭 구조
- 실시간 새 얼굴 알림
- 드래그 앤 드롭 지원 가능
- 일괄 처리 옵션

## 다음 단계 구현 사항

1. **얼굴 병합**: 같은 사람의 여러 얼굴 그룹화
2. **수동 태깅**: 사진에서 얼굴 직접 선택
3. **내보내기**: 인물별 폴더로 내보내기
4. **검색**: 인물 이름으로 사진 검색
5. **설정**: 
   - 얼굴 감지 민감도
   - 자동 그룹화 임계값
   - 백그라운드 스캔 주기

## 빌드 및 실행

```bash
# Gradle Sync
./gradlew sync

# 빌드
./gradlew assembleDebug

# 설치
./gradlew installDebug
```

## 권한 요구사항
- READ_EXTERNAL_STORAGE / READ_MEDIA_IMAGES
- 폴더 접근 권한 (SAF)
- 백그라운드 작업 (WorkManager)

## 테스트 시나리오

1. 앱 실행 → 폴더 탭 확인
2. FAB 클릭 → 폴더 선택
3. 자동 스캔 → New Faces 탭 확인
4. 얼굴 클릭 → 이름 입력
5. People 탭 → 생성된 앨범 확인
6. 앨범 클릭 → 링크된 사진들 확인
7. 선택한 폴더에 새 사진 추가
8. 자동 감지 및 분류 확인
