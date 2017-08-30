# SDK 설치
aar파일로 전달되는 SDK 파일을 프로젝트에 추가 합니다. 

android studio에서 사용시 File -> New -> New Module -> import .JAR/.AAR Package로 추가하시면 됩니다.

일반적으로 3개의 SDK가 전달이 됩니다.

 * Map SDK 
 * Navi SDK
 * 공통 좌표계 SDK

# Build.gradle 확인
## SDK 의존성 확인
build.gradle에 기본적으로 실행에 필요한 라이브러리나 설정이 적용 되어있습니다.
```bash
compile project(':geom')
compile project(':maps-release')
compile project(':roze-release')
```
설치된 SDK의 dependency를 확인합니다. 현재 build.gradle 상에서는 임의의 모듈명으로 포함이 되어있기 때문에
실제로 추가된 모듈의 명칭을 확인하시고 수정해 주시면 됩니다.

## Navi SDK 필수 의존성 확인
Navi SDK를 사용하기 위해서는 아래와 같은 라이브러리 dependency설정이 필요합니다.

현재 샘플코드의 build.gradle에는 추가가 되어있기 때문에 그대로 사용하시면 별도의 설정은 필요하지 않습니다.

```bash
compile "io.reactivex:rxjava:1.2.0"
compile "io.reactivex:rxandroid:1.2.1"
compile "com.squareup.retrofit2:retrofit:2.0.2"
compile "com.squareup.retrofit2:converter-gson:2.0.2"
compile "com.squareup.retrofit2:adapter-rxjava:2.0.2"
compile 'com.squareup.retrofit2:converter-protobuf:2.1.0'
compile "com.squareup.okhttp3:okhttp-urlconnection:3.2.0"
compile "com.squareup.okhttp3:logging-interceptor:3.2.0"
compile 'com.google.protobuf:protobuf-java:3.1.0'
compile 'org.apache.commons:commons-lang3:3.4'
compile "com.google.guava:guava:19.0"
compile "com.google.android.gms:play-services-location:9.2.0"
```

# AndroidManifest.xml 설정
SDK를 사용하기 위해서는 인증키가 필요합니다. SDK를 전달받으실때 함께 전달받은 인증키를 AndroidManifest.xml에 추가를 하게 됩니다.

```bash
<?xml version="1.0"encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="앱 패키지 명">

    ...
	
    <application >
	
	...
	
        <meta-data
            android:name="giskey" //key name은 giskey로 설정해주세요.
            android:value="전달 받은 sdk key" /> // 전달받은 key를 value에 넣어 주세요.
    </application>
</manifest>
```
AndroidManifest.xml에 meta-data로 키정보가 입력할 수 있도록 되어있습니다. 샘플코드에는 키 정보를 입력할 수 있도록
안내멘트가 입력이 되어있으므로 value항목에 전달받은 키를 넣어주시면 됩니다.