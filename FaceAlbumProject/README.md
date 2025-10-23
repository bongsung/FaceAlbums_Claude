# Face Album Project

온디바이스 얼굴 인식 기반 가상 인물 앨범 안드로이드 애플리케이션

## 프로젝트 개요

ML Kit 얼굴 탐지와 TFLite 임베딩을 활용한 사진 자동 분류 시스템입니다.
MediaStore 감시를 통해 신규/변경된 사진을 자동으로 처리하며, 
파일명 변경이나 이동에도 SHA-256 해시 기반으로 사진을 추적합니다.

## 주요 기능

- **얼굴 탐지**: ML Kit Face Detection API 사용
- **얼굴 임베딩**: TFLite 모델 (현재는 더미 구현)
- **MediaStore 감시**: ContentObserver로 신규/변경 사진 자동 감지
- **WorkManager 큐잉**: 백그라운드 얼굴 탐지 및 임베딩 생성
- **가상 인물 앨범**: 인물별 사진 그룹화
- **파일 추적**: SHA-256 해시로 파일명 변경/이동 추적
- **제안 시스템**: 유사 얼굴 기반 자동 그룹화 제안

## 모듈 구조

```
:app          - UI 레이어 (Jetpack Compose)
:domain       - 비즈니스 로직 및 도메인 모델
:data         - 데이터 레이어 (Room Database)
:ml           - ML Kit & TFLite 얼굴 탐지/임베딩
:media        - MediaStore 감시 및 스캐너
:work         - WorkManager Workers
:common       - 공통 유틸리티
```

## 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose, Material3
- **DI**: Hilt
- **데이터베이스**: Room
- **백그라운드 작업**: WorkManager
- **얼굴 탐지**: ML Kit Face Detection
- **임베딩**: TensorFlow Lite (구현 예정)
- **아키텍처**: Clean Architecture, MVVM

## 빌드 요구사항

- Android Studio Hedgehog (2023.1.1) 이상
- Gradle 8.0+
- Kotlin 1.9.23
- minSdk 26 (Android 8.0)
- targetSdk 34 (Android 14)

## 설치 및 실행

```bash
# 저장소 클론
git clone <repository-url>

# 프로젝트 빌드
./gradlew build

# 앱 설치 및 실행
./gradlew installDebug
```

## 데이터베이스 스키마

### Person
- 가상 인물 정보
- 커버 사진, 이름, 사진 수

### Photo
- MediaStore 사진 정보
- contentHash (SHA-256)로 파일 추적

### Face
- 탐지된 얼굴 정보
- 바운딩 박스, 임베딩 벡터

### LinkPersonPhoto
- Person-Photo 다대다 관계

### PendingSuggestion
- 사용자 승인 대기 중인 제안

### WatchFolder
- 감시 대상 폴더 목록

## TODO 구현 항목

### 1. TFLite 모델 통합
현재 `DummyFaceEmbeddingGenerator`를 실제 TFLite 모델로 교체:
- FaceNet 또는 유사 모델 (.tflite) assets에 추가
- TensorFlow Lite Interpreter 초기화
- 전처리 (160x160 리사이즈, 정규화)
- 추론 실행 및 128차원 임베딩 추출

참고: `ml/src/main/java/com/facealbum/ml/DummyFaceEmbeddingGenerator.kt`

### 2. Export 기능
외부 갤러리로 앨범 내보내기:
- MediaStore에 사진 복사
- 앨범 메타데이터 생성
- 설정에서 활성화/비활성화

### 3. Settings Repository 구현
- SharedPreferences 또는 DataStore
- WatchFolder CRUD 완성
- Export 설정 저장/로드

### 4. 클러스터링 개선
현재 단순 유사도 비교를 DBSCAN 등으로 개선

### 5. UI 개선
- PersonDetail 화면 사진 그리드
- PhotoDetail 화면 얼굴 바운딩 박스 표시
- Settings 화면 완성
- 로딩/에러 상태 처리

## 권한

```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## 라이선스

이 프로젝트는 교육 목적으로 제공되며, 네트워크 권한이나 외부 업로드 기능은 포함하지 않습니다.

## 참고 사항

- **온디바이스 처리**: 모든 처리는 디바이스 내에서 수행됩니다
- **프라이버시**: 사진이나 얼굴 데이터는 외부로 전송되지 않습니다
- **성능**: 대용량 사진 라이브러리의 경우 초기 스캔에 시간이 소요될 수 있습니다

## 문의

프로젝트 관련 문의사항은 Issue를 통해 제출해주세요.
