## 기술
- 라이브러리 : SASS, APACHE COMMON CSV
- 프레임워크 : SPRING
- 언어 : JavaScript

## 사용 방법
### 1. 루트 폴더에 '.env' 파일 생성 후, 아래 환경 변수 설정
- DB_URL = '입력'
- DB_USERNAME = '입력'
- DB_PASSWORD = '입력'

### 2. 소상공인 진흥공단 상권정보를 다운받아, src > resources > data에 저장
 - 소상공인 진흥공단 상권정보(2025.01) : https://www.data.go.kr/data/15083033/fileData.do


## 트러블 슈팅
1. 일하기 좋은 카페 수집 방법
 - 초기 설계 : Google Places API를 통하여 카페 기본정보 + 리뷰를 모아 일하기 좋은 카펠르 추출하려고 하였음
 - 초기 설게 한계 : Places API는 호출 1회 당 리뷰 5개만 호출됨 -> 장시간 소요 및 과금 이슈 발생
 - 트러블 슈팅 :
   (1) 소상공인 진흥공단의 전국 상점 정보 csv 파일을 파싱하여, 그 중 카페 업종만 필터링하여 DB 저장
   (2) DB에 저장된 카페를 크롤링(Selenium 라이브러리 사용)

2. 전국 카페 정보 DB 저장 동작 실행 시점
 - 초기 설계 : CommandLineRunner를 이용하여, 어플리케이션 실행 시 csv 파일을 parsing 하여 DB에 저장
 - 초기 설계 한계 : 매번 어플리케이션 실행 시마다 DB 업데이트 작업이 일어나므로, 개발 단계에서 시간과 리소스 낭비
 - 트러블 슈팅 : properties 설정 파일에 first-run.enabled 항목 추가 -> CommandLineRunner 실행 조건을 해당 값에 따라 제어
 