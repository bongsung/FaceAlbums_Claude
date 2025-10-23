# 프로젝트 구조

## 전체 디렉토리 구조

```
FaceAlbumProject/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml
├── README.md
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/facealbum/app/
│       │   │   ├── FaceAlbumApplication.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── di/
│       │   │   │   ├── CommonModule.kt
│       │   │   │   ├── DatabaseModule.kt
│       │   │   │   ├── MLModule.kt
│       │   │   │   └── RepositoryModule.kt
│       │   │   └── ui/
│       │   │       ├── navigation/
│       │   │       │   ├── Screen.kt
│       │   │       │   └── FaceAlbumNavHost.kt
│       │   │       ├── screens/
│       │   │       │   ├── home/
│       │   │       │   │   ├── HomeScreen.kt
│       │   │       │   │   └── HomeViewModel.kt
│       │   │       │   ├── suggestions/
│       │   │       │   │   ├── SuggestionsScreen.kt
│       │   │       │   │   └── SuggestionsViewModel.kt
│       │   │       │   ├── persondetail/
│       │   │       │   ├── photodetail/
│       │   │       │   └── settings/
│       │   │       └── theme/
│       │   │           ├── Theme.kt
│       │   │           └── Type.kt
│       │   └── res/
│       │       └── values/
│       │           ├── strings.xml
│       │           └── themes.xml
│       ├── test/
│       │   └── java/com/facealbum/app/
│       │       └── ui/screens/home/
│       │           └── HomeViewModelTest.kt
│       └── androidTest/
├── common/
│   ├── build.gradle.kts
│   └── src/main/java/com/facealbum/common/
│       ├── Constants.kt
│       ├── Result.kt
│       └── DispatcherProvider.kt
├── domain/
│   ├── build.gradle.kts
│   └── src/main/java/com/facealbum/domain/
│       ├── model/
│       │   ├── Person.kt
│       │   ├── Photo.kt
│       │   ├── Face.kt
│       │   ├── PendingSuggestion.kt
│       │   └── WatchFolder.kt
│       ├── repository/
│       │   ├── PersonRepository.kt
│       │   ├── PhotoRepository.kt
│       │   ├── FaceRepository.kt
│       │   ├── SuggestionRepository.kt
│       │   └── SettingsRepository.kt
│       └── usecase/
│           ├── GetAllPersonsUseCase.kt
│           ├── CreatePersonUseCase.kt
│           ├── GetPhotosForPersonUseCase.kt
│           ├── GetPendingSuggestionsUseCase.kt
│           └── ProcessSuggestionUseCase.kt
├── data/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/facealbum/data/
│       │   ├── local/
│       │   │   ├── FaceAlbumDatabase.kt
│       │   │   ├── entity/
│       │   │   │   ├── PersonEntity.kt
│       │   │   │   ├── PhotoEntity.kt
│       │   │   │   ├── FaceEntity.kt
│       │   │   │   ├── LinkPersonPhotoEntity.kt
│       │   │   │   ├── PendingSuggestionEntity.kt
│       │   │   │   └── WatchFolderEntity.kt
│       │   │   └── dao/
│       │   │       ├── PersonDao.kt
│       │   │       ├── PhotoDao.kt
│       │   │       ├── FaceDao.kt
│       │   │       ├── SuggestionDao.kt
│       │   │       └── WatchFolderDao.kt
│       │   └── repository/
│       │       ├── PersonRepositoryImpl.kt
│       │       ├── PhotoRepositoryImpl.kt
│       │       ├── FaceRepositoryImpl.kt
│       │       └── SuggestionRepositoryImpl.kt
│       └── androidTest/java/com/facealbum/data/
│           └── repository/
│               └── PersonRepositoryImplTest.kt
├── ml/
│   ├── build.gradle.kts
│   └── src/main/java/com/facealbum/ml/
│       ├── FaceDetector.kt
│       ├── MLKitFaceDetector.kt
│       ├── FaceEmbeddingGenerator.kt
│       └── DummyFaceEmbeddingGenerator.kt
├── media/
│   ├── build.gradle.kts
│   └── src/main/java/com/facealbum/media/
│       ├── MediaStoreObserver.kt
│       └── MediaStoreScanner.kt
└── work/
    ├── build.gradle.kts
    └── src/main/java/com/facealbum/work/
        ├── MediaSyncWorker.kt
        ├── FaceDetectionWorker.kt
        └── ClusteringWorker.kt
```

## 모듈 설명

### :app
- 메인 애플리케이션 모듈
- Jetpack Compose UI
- Hilt DI 설정
- Navigation 그래프
- ViewModels

### :common
- 공통 유틸리티
- Result 래퍼 클래스
- Constants
- DispatcherProvider

### :domain
- 비즈니스 로직 레이어
- 도메인 모델
- Repository 인터페이스
- Use Cases

### :data
- 데이터 레이어 구현
- Room Database 및 Entity
- DAO 인터페이스
- Repository 구현체

### :ml
- ML Kit 얼굴 탐지
- TFLite 임베딩 생성
- 더미 구현체 포함

### :media
- MediaStore 감시
- ContentObserver
- 사진 스캐너
- SHA-256 해시 계산

### :work
- WorkManager Workers
- MediaSync Worker
- FaceDetection Worker
- Clustering Worker

## 데이터 흐름

1. **MediaStore 변경 감지**
   - MediaStoreObserver가 변경 감지
   - MediaSyncWorker 큐잉

2. **사진 동기화**
   - MediaStoreScanner로 사진 정보 조회
   - SHA-256 해시 계산
   - PhotoRepository에 저장
   - FaceDetectionWorker 큐잉

3. **얼굴 탐지**
   - MLKitFaceDetector로 얼굴 탐지
   - FaceEmbeddingGenerator로 임베딩 생성
   - FaceRepository에 저장
   - ClusteringWorker 큐잉

4. **클러스터링**
   - 유사 얼굴 검색
   - PendingSuggestion 생성
   - UI에서 사용자 확인

5. **UI 업데이트**
   - Flow로 실시간 데이터 구독
   - Compose로 자동 리컴포지션
