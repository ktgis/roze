# Changelog
신버전 sdk 샘플 프로젝트 변경사항

## [2019-11-25-2]

### 추가
- 누락된 클래스 파일 추가

## [2019-11-25]

### 추가
- NAVI SDK 1.4.0 - 1.5.0 대응
- 안전운행 맵매칭 기능 추가 (1.4.0)
- onArrivedFerryRoute(FerryPoint ferryPoint) 메소드 추가로 페리경로 시작지점 도착여부 확인기능 추가
- 경로탐색 오류시 예외처리 코드 추가(1.5.0)
- 구간 단속 평균속도 초과 알림음 활성/비활성 기능 추가(1.5.0)

### 변경사항
- gradle 버전에 따른 build.gradle migration
- 앱시작시 NavigationManager.startTracking() -> navigationManager.startCruising() 로 변경
- 안전운행 안내정보 준비 이벤트 RouteGuidanceListener.onTrackingReadyEvent(boolean initialized) -> NavigationManager.TrackingEventListener 로 변경
- DriveFragment 맵매칭 좌표 반영
- kotlin 호환 코드 업데이트

## [2019-03-26]

### 변경사항
- 차선 안내 시 Pocket 차선 및 Bus 전용 차선 안내 추가
- 경유지 안내 시 멀티 입구점 적용 함수 추가
- 재 탐색 시 상세 원인 추가
- 고속도로 휴게소 진입 시 반복 이탈 현상 수정 (휴게소 내부 초기탐색 제외)

## [2018-06-12-2]

### 변경사항
- Protobuf 라이브러리 버전 3.1.0 -> 3.5.1 으로 변경

## [2018-06-12]

### 추가
- onSoundDeleteEvent(SoundManager soundManager, List<Long> ids) 메소드 추가로 사운드 삭제대항 확인 기능 추가
- 안전운행 기능 추가

### 변경사항
- 안전운행 안내 정보 SafetySpotGuidance -> SafetySpotInterface 로 타입 변경

## [2018-04-03-2]

### 추가
- 경로재탐색시 UI 데이터 동기화 오류에 따른 예외처리 추가

## [2018-04-03]

### 추가
- POI 검색시 예외처리 코드 추가

## [2017-12-19]

### 추가
- NAVI SDK 1.1.2 대응
- RozeError 발생시 서버 API 오류코드 추가


## [2017-11-15]

### 추가
- MAP SDK 신규 인증로직 추가
- 경로안내 정보 공유데이터 클래스 추가

### 변경사항
- Compile 버전 25 -> 26 으로 변경
- Android Architecture Components 라이브러리 버전 1.0.0-alpha5 -> 1.0.0-rc1 이상으로 변경
- support 라이브러리 버전 25.0.0 -> 26.1.0 으로 변경

## [2017-09-29]

### 추가
- Android Architecture Components 추가
- CommonUtil 클래스 추가
- 경로추가설정 화면 추가
- onSoundEnd() 메소드 추가로 사운드 재생 종료시점 확인 기능 추가
- Place SDK 추가

### 변경
- BaseActivity isResumed 상태에 따른 fragment replace 적용 방식 변경
- NavigationData 패키지 변경 com.kt.rozenavi.ui.main.navigation -> com.kt.rozenavi.ui.main.navigation.data
- NavigationData inner class 리팩토링 -> com.kt.rozenavi.ui.main.navigation.data.model 로 이동
- NavigationHipassView 코드 정리 및 리팩토링
- NavigationLaneView 코드 정리 및 리팩토링
- NavigationLowestGasView 코드 정리 및 리팩토링
- NavigationSpotView 코드 정리 및 리팩토링
- NavigationSpotView interval speed cam 특정 상황에서 UI 오류 수정
- NavigationSpotView 지도 위의 speed cam 마커가 특정 상황에서 UI 오류 수정
- NavigationTbtView 코드 정리 및 리팩토링
- 경로안내화면 지도 제어기능을 MapHelper 클래스로 분리
- glide 라이브러리 버전 3.7.0 -> 4.0.0 으로 변경
- rxJava 라이브러리 버전 1.2.0 -> 2.1.3 으로 변경
- rxAndroid 라이브러리 버전 1.2.1 -> 2.0.1 으로 변경
- retrofit 라이브러리 버전 2.0.2 -> 2.3.0 으로 변경
- okhttp3 라이브러리 버전 3.2.0 -> 3.8.0 으로 변경
- android build tool 버전 2.3.0 -> 2.3.3 으로 변경
- Map SDK 초기화 결과 파라매터 클래스 변경

## [2017-08-29]

### 추가
- 신규 샘플앱 추가

[2019-11-25-2]: https://github.com/ktgis/roze/commit/331c49ce67cea50d5af3b88f56094391881b9cba
[2019-11-25]: https://github.com/ktgis/roze/commit/55c88eadbb58f60db6f6635ee15d1677db331f6d
[2019-03-26]: https://github.com/ktgis/roze/commit/34ba5836e2d178c14107830963b4416716601474
[2018-06-12-2]: https://github.com/ktgis/roze/commit/611bf0e0ecdefe1d8f61eff9ff40a1ec1a25af47
[2018-06-12]: https://github.com/ktgis/roze/commit/35d3fcf545edd6b803e86853973fbbcb19dd6f27
[2018-04-03-2]: https://github.com/ktgis/roze/commit/a7611c53d9b5ec4889c46a051d964325c9d891ce
[2018-04-03]: https://github.com/ktgis/roze/commit/9ddefa605ff21a6921b658d800bf882c0e879beb
[2017-12-19]: https://github.com/ktgis/roze/commit/87330933ee23fe642568b4c17977b180987a17c0
[2017-11-15]: https://github.com/ktgis/roze/commit/1ebe8e21fd11f3854d0bdc8940221ad506c2535f
[2017-09-29]: https://github.com/ktgis/roze/commit/65c9784643855649189a55981258c9b43236524e
[2017-08-29]: https://github.com/ktgis/roze/commit/3f2c60129cb121a760ebc0cda6640e78d6289a1e
