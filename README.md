# Wake-up

<br />

## 🔗 서비스설명
<b> 
딥러닝을 이용한 졸음운전 방지 시스템은 구글의 ML Kit 딥러닝 기술을 활용하여 운전 중 운전자의 눈과 입의 상태를 실시간으로 감지하는 시스템을 구현하였습니다.  <br>
이 시스템은 운전자의 졸음 상태를 탐지하고, 졸음이 감지되면 단말기를 통해 경고 음성을 발생시켜 안전 운전을 촉구합니다.  <br>
또한, 운전자의 졸음 감지 통계 분석 결과를 애플리케이션 화면에 보여줌으로써 운전 습관을 개선하도록 유도합니다. <br>
</b><br>

<br />

## 🗓️ 프로젝트 기간

<br />

2022.08 ~ 2022.11 

<br />
<br />

## 👨‍👨‍👧‍👧 Team List
- 서가연
- 윤승현

<br />
<br />

## 🔨 Skill Stacks
- JAVA
- ML Kit

<br />
<br />

## 🔗 프로젝트 구현 내용(내 담당)

<br />

**눈을 감은 경우 경고 음성 출력**
- soundPool을 활용하여 경고음 출력

<br />

**하품한 경우 경고 음성 출력**
- soundPool을 활용하여 경고음 출력

<br />

**입을 벌리고 있을 때(하품)의 시간 계산**
- ML Kit의 Face Detection을 통해 얼굴 특징점을 추출하고, FaceContour와 List<PointF>를 활용하여 입의 좌표를 구함

<br />

**눈을 감은 횟수와 하품한 횟수 통계**
- 눈을 약 2초 이상 감은 다음 다시 눈을 뜨면 카운트가 하나씩 올라가게끔 개발
- 입을 약 2초 이상 크게 벌리고 있는(하품) 다음 다시 입을 닫으면 카운트가 하나씩 올라가게끔 개발

<br />

**UI 설계 및 구현**
- 레이아웃 에디터와 디자인 도구를 활용
- 간단하고 깔끔한 디자인을 통해 사용자가 손쉽게 사용할 수 있게 구현

<br />
<br />
<br />

## 📸 구현 결과

<br />
<br />
<img src="https://github.com/yunkr/Wake-up/assets/99308074/38c5af7e-d638-4701-af15-a2c70302e2c6">

<br />
<br />
<img src="https://github.com/yunkr/Wake-up/assets/99308074/f3f7e725-55db-415f-a07d-cbfa8dcebde6">

<br />
<br />
<img src="https://github.com/yunkr/Wake-up/assets/99308074/7df9f295-7718-4a9b-acb4-42972e8d48bf">

<br />
<br />
<br />
<br />

## 📂 다이어그램
[다이어그램](https://app.diagrams.net/#G1CktIv5HM0_aV3e0OBCzWYhqHjsNlIOod)
<br>
<br>
<br/>
<img src="https://github.com/yunkr/Wake-up/assets/99308074/8f3f80b4-0587-4249-9c8a-0cd840a53288">
<br>







