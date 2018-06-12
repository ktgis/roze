# Changelog
구버전 sdk 샘플 프로젝트 변경사항

## [2018-06-12]

### 추가
- onSoundDeleteEvent(SoundManager soundManager, List<Long> ids) 메소드 추가로 사운드 삭제대항 확인 기능 추가

### 변경사항
- NAVI SDK 1.2.0 대응
- protobuf 라이브러리 버전 3.1.0 -> 3.5.1 으로 변경
- 고속도로 정보 거리 업데이트 방식 수정 HighWayView.setHighwayGuidaces() 참고

## [2017-12-19]

### 추가
- NAVI SDK 1.1.2 대응
- RozeError 발생시 서버 API 오류코드 추가

## [2017-11-15]

### 변경사항
- NAVI SDK 1.0.4 이후 버전과 1.0.3 이전 버전의 라이브러리 의존성 정리

## [2017-11-13]

### 변경사항
- 구버전과의 호환성을 위해 onSoundEnd() 메소드 @Override annotation 삭제

## [2017-11-07]

### 변경사항
- 구버전과의 호환성을 위해 NAVI SDK 1.0.3 이전 버전용 라이브러리 의존성 복구

## [2017-09-29]

### 추가
- onSoundEnd() 메소드 추가로 사운드 재생 종료시점 확인 기능 추가

### 변경사항
- glide 라이브러리 버전 3.7.0 -> 4.0.0 으로 변경
- rxJava 라이브러리 버전 1.2.0 -> 2.1.3 으로 변경
- rxAndroid 라이브러리 버전 1.2.1 -> 2.0.1 으로 변경
- retrofit 라이브러리 버전 2.0.2 -> 2.3.0 으로 변경
- okhttp3 라이브러리 버전 3.2.0 -> 3.8.0 으로 변경
- android build tool 버전 2.3.0 -> 2.3.3 으로 변경
- Map SDK 초기화 결과 파라매터 클래스 변경

## [2017-07-19]

### 변경
- 지도 초기화 타이밍 오류 수정
- 경로검색시 GPS 상태에 따른 방향값 설정 추가

## [2017-07-07]

### 추가
- 경로상 방향 패턴 기능 추가

## [2017-06-26]

### 추가
- 진행 속도 및 Turn 정보에 따른 줌레벨 변경 로직 추가 : ZoomChanger

### 변경사항
- 지도 최대 줌레벨 12 -> 13으로 변경

## [2017-04-25]

### 추가
- NAVI SDK / MAP SDK 인증키 기능 추가

### 변경사항
- 경로 이탈 / 경로 진입실패 처리 로직 오류 수정

## [2017-04-14]

### 추가
- NAVI SDK 데이터 저장 폴더 변경기능 추가
- RozeOptions 초기화 기능 추가
- 반복음 사운드 종료 기능 추가
- 사용자 재탐색 / 자동재탐색(교통정보 재탐색) 실패시 이전경로 사용 로직 추가

### 변경사항
- 고속도로 정보 인터페이스 BaseHighway -> HighwayGuidace로 변경

## [2017-03-31]

### 추가
- RozeOptions 설정된 유종타입 반환 기능 추가

### 변경사항
- 고속도로 휴게소 유종정보 타입 int -> enum 으로 변경

## [2017-02-21]

### 추가
- 지도 초기화 정보 코드 추가

## [2017-02-08]

### 추가
- 권한요청시 다시묻지 않기 선택항목 선택시 로직 추가
- 반복음 발성 로직 추가

### 변경사항
- 유가정보 타입 OilPrice -> EnergyPrice 로 변경

## [2017-01-18]

### 변경사항
- 지도 Heading 변경시 내위치 마커 제어로직 수정

## [2017-01-17]

### 추가
- NAVI SDK 샘플앱 추가

[2018-06-12]: https://github.com/ktgis/roze/commit/06d02327104793badb298916c6e152790894841a
[2017-12-19]: https://github.com/ktgis/roze/commit/33d1f91701ddca9fa5823fe5614bf8194e6d2c5c
[2017-11-15]: https://github.com/ktgis/roze/commit/e4e1683a330ebf9312f6b6d4c512604ba2bc8067
[2017-11-13]: https://github.com/ktgis/roze/commit/64a8b4539c442fc6df4bf302805e90108c056035
[2017-11-07]: https://github.com/ktgis/roze/commit/baee7073f5aad513783b8390c3d948ff1e2b03c6
[2017-09-29]: https://github.com/ktgis/roze/commit/55a5ccd78d11719ac72a0e998e4135857e4c9752
[2017-07-19]: https://github.com/ktgis/roze/commit/05ebd4412b46c5d2a224f0f2b3a1138cc8658454
[2017-07-07]: https://github.com/ktgis/roze/commit/75835b580c7f5c67ac76fef9bd3a69f3dea420e7
[2017-06-26]: https://github.com/ktgis/roze/commit/e2dbb4b6464aa87f14ad01cdc64fe21df6044cd0
[2017-04-25]: https://github.com/ktgis/roze/commit/60cb6f6250518a443a49b53d572d4ecd05f1ad58
[2017-04-14]: https://github.com/ktgis/roze/commit/3971b170902930edbf1d52ac2dd7622a7218b591
[2017-03-31]: https://github.com/ktgis/roze/commit/c49785a7b3d39c0583161dcdb89cc054e10f8ccb
[2017-02-21]: https://github.com/ktgis/roze/commit/70fc9bc1d2319bf2be4f8103f839bfe984daa1cc
[2017-02-08]: https://github.com/ktgis/roze/commit/9679e0d2c48ba7ab1101d4eb2d117d63cacf7361
[2017-01-18]: https://github.com/ktgis/roze/commit/a288e92a96b955e3ecd5b82588ce0e34258d7ec5
[2017-01-17]: https://github.com/ktgis/roze/commit/322ac1539af74207b863272c4733fc7a16cdb0af