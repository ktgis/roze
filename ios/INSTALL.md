# SDK 설치
Release 또는 Debug build configuration 에 맞는 framework 파일들을 프로젝트의  추가합니다.

프로젝트 설정 화면 > 원하는 Target 선택 > General 페인 선택 > Embeded Binaries 목록에 framework 추가 합니다.

일반적으로 4개의 SDK가 전달이 됩니다.

 * Map SDK 
 * Navi SDK
 * Place SDK
 * 공통 좌표계 SDK

# COCOAPODS 설정
## pod install
COCOAPODS 가 설치되어 있지 않다면 설치합니다. COCOAPODS 의 자세한 설치 및 사용법은 https://guides.cocoapods.org/ 를 참고하시기 바랍니다.

프로젝트 루트 디렉토리에서 아래 명령어를 실행하여 cocoapods dependency 를 내려받습니다.
```bash
pod install
```


# API 인증키 설정
## Bundle Identifier 입력
API Key 는 발급 요청시에 등록된 Bundle Identifier 로 설정된 앱에서만 사용될 수 있습니다.

프로젝트 설정 화면 > 원하는 Target 선택 > General 페인 선택 > Bundle Identifier 항목에 인증키 발급시 등록한 문자열을 입력합니다.

## Api Key 등록
SDK를 사용하기 위해서는 인증키가 필요합니다. SDK를 전달받으실때 함께 전달받은 인증키를 AppDelegate.swift 에 입력합니다.

```bash
...
class AppDelegate: UIResponder, UIApplicationDelegate {

var window: UIWindow?

static let apiKey = "전달받은 API 키"

...
```
