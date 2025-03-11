package com.digital_nomad.find_my_office.util;

import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.domain.cafe.entity.Review;
import com.digital_nomad.find_my_office.domain.cafe.entity.ReviewCrawlingStatus;
import com.digital_nomad.find_my_office.domain.cafe.entity.ReviewImage;
import com.digital_nomad.find_my_office.exception.CrwalingException;
import com.digital_nomad.find_my_office.exception.ErrorCode;
import com.digital_nomad.find_my_office.repository.CafeRepository;
import com.digital_nomad.find_my_office.repository.ReviewImageRepository;
import com.digital_nomad.find_my_office.repository.ReviewStatusRepository;
import com.digital_nomad.find_my_office.service.CafeService;
import com.digital_nomad.find_my_office.service.ReviewImageService;
import com.digital_nomad.find_my_office.service.ReviewService;
import com.digital_nomad.find_my_office.service.ReviewStatusService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 리뷰 크롤링을 위해 WebDriver 불러오는 클래스
@Slf4j
@RequiredArgsConstructor
@Component
public class ReviewCrawler implements CommandLineRunner {

    // 처음 실행여부를 관리하기 위한 파일 경로 (com.digital_nomad.find_my_office.isFirstRun 위치에 저장)
    private static final String REVIEW_CRAWLING_EXECUTED = "src/main/java/com/digital_nomad/find_my_office/isFirstRun/reviewCrawlingExecuted.txt";

    private final CafeService cafeService;
    private final ReviewStatusService reviewStatusService;
    private final ReviewService reviewService;
    private final CafeRepository cafeRepository;
    private final ReviewImageService reviewImageService;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewStatusRepository reviewStatusRepository;
    @PersistenceContext
    private EntityManager entityManager;

    // 어프리케이션 실행 시 txt 파일 실행 여부에 따라, txt 파일 없으면(=처음 실행히면) 리뷰 크롤링 하도록 설정
    @Override
    public void run(String... args) throws Exception {

        // 1. 파일이 이미 존재하는지 확인(초기 실행시는 파일 없고, 그다음부터는 파일 생성되어 있음)
        File flag = new File(REVIEW_CRAWLING_EXECUTED);

        // 2. 파일이 존재하지 않으면 처음 실행인 것으로 판단하고 처리
        if (!flag.exists()) {
            log.info("====== 리뷰 크롤링 데이터 없음(flag 파일 없음)========");
            setUpAndStartCrawling();
            createReviewCrawlingFlagFile(flag); //review crwaling 끝난 후에, 완료 증거로 file 생성 -> 다음부터는 csv parsing 과정 생략됨
        } else {
            // 이미 실행된 경우 메시지 출력
            log.info("Review crawling already completed.");
        }
    }

    private void createReviewCrawlingFlagFile(File flagFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(flagFile))) {
            writer.write("review crawling finished");
        } catch (IOException e) {
            log.error("Error while creating the review crawling flag file.", e);
            e.printStackTrace();
        }
    }

    public WebDriver getDriver() {

        // 1. chromedriver 경로 설정 (드라이버 파일 프로젝트에 저장해두었음)
        Path path = Paths.get("src/main/resources/driver/chromedriver134.exe");
        String absolutePath = path.toAbsolutePath().toString();
        System.out.println(absolutePath); // 경로 제대로 설정되었는지 확인

        // 시스템에 설정
        System.setProperty("webdriver.chrome.driver", absolutePath);

        // 2. 옵션 설정 (브라우저 창 띄우고 진행)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"); // 구글 막힘 방지

        // 3. ChromeDriver 객체 생성 후 반환
        return new ChromeDriver(options);
    }

    @SneakyThrows
    public void setUpAndStartCrawling() {

        // 테스트용 : 서울특별시 업체만 고름
        List<Cafe> stores = cafeService.getCafeByProvinceName("서울특별시");

        // 드라이버 준비
        WebDriver driver = getDriver();

        // 크롤링 시작
        doCrawling(driver, stores);

    }


    // 본격적으로 크롤링 시작
    private void doCrawling(WebDriver driver, List<Cafe> stores) throws Exception {

        for (Cafe store : stores) {

            String reviewUrl = getReviewUrl(store); // 리뷰용 주소 반환
            log.info("============= start crawling: {}================", store.getName());
            driver.get(reviewUrl);   // url 접속
            // 리뷰 탭으로 이동(작업 성공여부를 boolean으로 반환 - false일 시 업체가 존재하지 않음. 기타 에러는 exception 발생)
            boolean flag = navigateToReviewTab(driver, reviewUrl, store);

            // naver place 에서 업체 조회안되었으면 : db에 없는 업체라고 저장
            if (!flag) {
                reviewStatusService.saveReviewCrawlingStatus(store, false);
                log.info("Review Crawling Status DB Updated(Place not found on Naver place page)");
            } else {
                scrollAndLoadAllReviews(driver, store);
                crawlLoadedReviews(driver, store);
            }

            log.info("============= end of crawling: {}================", store.getName());

        }
    }

    // 로딩된 리뷰를 수집
    private void crawlLoadedReviews(WebDriver driver, Cafe store) {

        log.info("crwalLoadedReview: {}", store.getName());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // reviewsContainer 태그 찾기 (<li class="place_apply_pui>를 감싸는 ul 태그)
        WebElement reviewsContainer
                = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[contains(@class, 'place_apply_pui')]/ancestor::ul")));

        // reviewsContainer의 각 review li 태그 찾기  (<li class="place_apply_pui>)
        List<WebElement> reviewItems = reviewsContainer.findElements(By.xpath(".//li[contains(@class, 'place_apply_pui')]"));

        // 업체 모든 리뷰를 리스트로 전달하기 위해, extractReviewDetails에 전달할 리스트
        List<Review> reviewList = new ArrayList<>();
        // 각각의 review (li 태그)에서, 리뷰 내용 추출 -> DB에 저장(업체 단위로 한 번에 저장됨)
        for (WebElement reviewItem : reviewItems) {
            extractReviewDetails(reviewItem, driver, reviewList, store);
        }
    }

    /**
     * review를 담은 각각의 li 태그에서 각각의 상세 내용 추출하여 리스트로 반환
     *
     * @param reviewItem - 각각의 리뷰를 담은 부모 태그
     * @param driver     - chrome driver
     * @param reviewList - 리뷰 리스트를 담기 위해, 빈 배열을 부모 메소드에서 전달하였음
     * @param store      - 카페
     */
    private void extractReviewDetails(WebElement reviewItem, WebDriver driver, List<Review> reviewList, Cafe store) {

        String reviewer;
        List<String> imageUrls = new ArrayList<>();  // 사진 URL들을 저장할 리스트
        String reviewText;
        LocalDate reviewDate;

        // 사진 있는지 확인(여부가 따라 태그 경로 달라짐)
        boolean hasPhotosFlag = hasPhotos(reviewItem, driver);

        reviewer = extractReviewer(reviewItem); // 리뷰어 추출
        imageUrls = extractImageUrls(reviewItem);         // 사진 url 추출
        reviewText = extractReviewText(reviewItem, hasPhotosFlag);         // 리뷰 내용 추출+
        String reviewDateInString = extractReviewDate(reviewItem, hasPhotosFlag);       // 리뷰 작성일 추출

        // 만약 리뷰일자가 2021.10 이전이라서 review 태그의 틀이 다르면, 크롤링에서 제외하기 위해 null 반환
        if ("2021.10 이전 리뷰. 크롤링에서 제외".equals(reviewDateInString)) {
            log.info("2021.10 이전 리뷰. 크롤링에서 제외");
            return;
        }

        // 날짜 형식 변환
        reviewDate = changeReviewDateToTextDateForm(reviewDateInString);

        // 리뷰를 review 엔티티로 반환(review crwaling status, review image entity 정보까지 포함)
        saveReviewToDB(store, reviewer, imageUrls, reviewText, reviewDate);

    }

    private void saveReviewToDB(Cafe cafe, String reviewer, List<String> imageUrls, String reviewText, LocalDate reviewDate) {

        log.info("staring saveReviewToDB Method : {}", cafe.getName());

        Cafe foundCafe = cafeRepository.findById(cafe.getId()).orElseThrow();

        // 가게 별 첫번째 DB인지 확인
        ReviewCrawlingStatus savedReviewCrawlingStatus;

        if (!reviewStatusService.existsByCafeAndIsReviewed(foundCafe, true)) {
             // review status db 저장
            log.info("saving review crawling status to DB : {}", cafe.getName());
            ReviewCrawlingStatus newReviewCrawlingStatus = ReviewCrawlingStatus.builder()
                    .isReviewed(true)
                    .cafeExists(true) // 이 단계까지 온 cafe는 naver place에 존재하는 카페
                    .reviews(null) // ! reviews 만들고 뒤에서 세팅할 것임
                    .cafe(foundCafe) // cafe 그대로 넣어주면 중복 에러 발생
                    .build();
            savedReviewCrawlingStatus = reviewStatusService.saveReviewCrawlingStatus(foundCafe, true);
        } else {
            savedReviewCrawlingStatus = reviewStatusRepository.findByCafeAndIsReviewed(foundCafe, true);
            log.info("review crawling status already saved to DB : {}", cafe.getName());
        }

        // Review Entity DB 저장(reviewImages는 뒤에서 설정)
        Review reviewEntity = Review
                .builder()
                .reviewCrawlingStatus(reviewStatusRepository.findById(savedReviewCrawlingStatus.getId()).orElseThrow())
                .reviewer(reviewer)
                .reviewImages(null) // ! 뒤에서 설정
                .content(reviewText)
                .date(reviewDate)
                .build();

        log.info("saving review to DB : {}", cafe.getName());
        Review savedReview = reviewService.save(reviewEntity);

        // reviewImage 엔터티 만들기
        boolean hasPhotoReviews = storeHasPhotoReviews(imageUrls);
        List<ReviewImage> reviewImagesEntities = new ArrayList<>();
        if (hasPhotoReviews) {
            log.info("saving review images to DB : {}", cafe.getName());
            // 사진이 있으면,reviewImages 엔티티 만들고 리뷰 ENTITY의 이미지 변경해주기
            Review foundReview = reviewService.findById(savedReview.getId()).orElseThrow();

            imageUrls.forEach(imageUrl -> {

                ReviewImage reviewImage = ReviewImage.builder()
                        .review(foundReview)
                        .imageUrl(imageUrl)
                        .build();
                // db에 저장
                ReviewImage savedReviewImage = reviewImageService.save(reviewImage);

                // review 엔터티에서 reviewImage 갈아주기
                Review targetReview = reviewService.findByIdWithReviewImages(foundReview.getId());
                List<ReviewImage> priorReviewImages = targetReview.getReviewImages();
                priorReviewImages.add(reviewImageRepository.findById(savedReviewImage.getId()).orElseThrow());

                reviewService.findByIdWithReviewImages(foundReview.getId())
                        .setReviewImages(priorReviewImages);
                // 변경 사항을 데이터베이스에 반영하기 위해 리뷰 엔터티 다시 저장
                reviewService.save(foundReview);
            });

        }
    }

    // imageUrls이 빈 리스트이면 false, 아니면 true
    private boolean storeHasPhotoReviews(List<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }




    // 0000년 0월 0일 형식의 날짜를 LocalDate 으로 변환
    private LocalDate changeReviewDateToTextDateForm(String reviewDateInText) {
        String[] dateParts = reviewDateInText.split("\\s*[년|월|일]\\s*");
        int year = Integer.parseInt(dateParts[0].trim());
        int month = Integer.parseInt(dateParts[1].trim());
        int date = Integer.parseInt(dateParts[2].trim());
        return LocalDate.of(year, month, date);
    }

    private String extractReviewDate(WebElement reviewItem, boolean hasPhotosFlag) {

        try {
           // 사진이 있는 경우
           if (hasPhotosFlag) {
               // 방문일자 : reviewItem > 7번째 div > 2번째 div > 첫번쨰 div > 1번째 span > 클래스 이름이 'pui__blind'이고 내용이 '방문일'이 아닌 span 태그
               return reviewItem.findElement(By.xpath("./div[7]/div[2]/div[1]//span[@class='pui__blind' and not(text()='방문일')]")).getText();
           } else {
               // 방문일자 : reviewItem > 6번째 div > 2번째 div > 첫번쨰 div > 1번째 span > 클래스 이름이 'pui__blind'이고 내용이 '방문일'이 아닌 span 태그
               return reviewItem.findElement(By.xpath("./div[6]/div[2]/div[1]//span[@class='pui__blind' and not(text()='방문일')]")).getText();
           }
       } catch (NoSuchElementException e) {
           return  "2021.10 이전 리뷰. 크롤링에서 제외";
       }
    }

    private String extractReviewText(WebElement reviewItem, boolean hasPhotosFlag) {
        try {
            // 사진이 있는 경우
            if (hasPhotosFlag) {
                // 리뷰 내용 : reviewItem > 5번째 div > a 태그의 내용
                return reviewItem.findElement(By.xpath(".//div[5]//a")).getText();
            } else {
                // 리뷰 내용 : reviewItem > 4번째 div > a 태그의 내용
                return reviewItem.findElement(By.xpath(".//div[4]//a")).getText();
            }
        } catch (NoSuchElementException e) {
            return  "2021.10 이전 리뷰. 크롤링에서 제외";
        }

    }

    private List<String> extractImageUrls(WebElement reviewItem) {

        List<WebElement> reviewImages = reviewItem.findElements(By.xpath(".//div[2]//div//div//div//div//div//a[contains(@class, 'place_thumb')]//img"));
        List<String> imageUrls = new ArrayList<>();
        for (WebElement image : reviewImages) {
            String imageUrl = image.getAttribute("src");
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    // 작성자 : reviewItem > 첫번째 div > 두번쨰 a 태그 > div > span > span 태그의 내용
    private String extractReviewer(WebElement reviewItem) {
        return reviewItem.findElement(By.xpath(".//div[1]//a[2]//div//span//span")).getText();
    }


    /**
     * review에서 레이지로딩 안에 있는 img 태그가 보이도록 강제로 레이지로딩을 제거하고, 이후에 이미지가 있는지 여부를 판단
     * @param reviewItem - 각각의 리뷰가 담긴 li 태그
     * @param driver
     * @return 이미지 있는지 여부
     */
    private boolean hasPhotos(WebElement reviewItem, WebDriver driver) {

        // ===== 시작 : 레이지로딩 제거 =========
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            // lazyload-wrapper 클래스를 제거하여 이미지 로드 강제화
            js.executeScript("var elements = arguments[0].querySelectorAll('.lazyload-wrapper'); elements.forEach(function(element) { element.classList.remove('lazyload-wrapper'); });", reviewItem);
            // 스크롤을 사용하여 이미지가 화면에 나타나도록 함
            js.executeScript("arguments[0].scrollIntoView(true);", reviewItem);
            Thread.sleep(2000); // 페이지 로딩 시간을 충분히 주기 위해 대기
            // 추가로 img 태그가 완전히 로드될 때까지 기다림
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // ====== 끝 : 레이지 로딩 제거 ==========

            // img 태그 가져오기(없으면 빈 배열, 있으면 image url 반환)
            wait.until((ExpectedCondition<Boolean>) wd -> {
                List<WebElement> images = reviewItem.findElements(By.xpath(".//div[2]//div//div//div//div//div//a[contains(@class, 'place_thumb')]//img"));
                return !images.isEmpty();
            });

            List<WebElement> images = reviewItem.findElements(By.xpath(".//div[2]//div//div//div//div//div//a[contains(@class, 'place_thumb')]//img"));

            if (!images.isEmpty()) {
                return true;
            } else {
                return false;
            }
        // 이미지 없을 때 발생하는 에러
        } catch (TimeoutException | InterruptedException e) {
//            log.info("No images found within the specified wait time.");
            return false;
        }
    }


    // 더보기 버튼 클릭하여 리뷰 모두 불러오기
    private void scrollAndLoadAllReviews(WebDriver driver, Cafe store) throws Exception {

        log.info("scrollAndLoadAllReviews method implementing for : {}", store.getName());
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight"); // 현재 페이지 높이 구하기

            // ======== 시작 : 스크롤 로직 =========
            // 현재 페이지 전체 높이 구한 후 -> 한 번 더 스크롤링 한 후 전체 높이 구함 -> 둘이 높이 차이가 없다면, 더이상 스크롤 하지 않아도 된다는 뜻
            while (true) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000); // 페이지 로딩 시간을 충분히 주기 위해 대기

                long newHeight = (long) js.executeScript("return document.body.scrollHeight");

                if (newHeight == lastHeight) {
                    // ======= 끝 : 스크롤 로직 ============
                    // ======= 여기서부터는 더보기 클릭 로직 ===========
                    // 페이지 높이가 변하지 않았으면 더보기 버튼 클릭
                    try {
                        WebElement moreButton
                                = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[.//span[contains(text(), '더보기')]]")));
                        js.executeScript("arguments[0].scrollIntoView(true);", moreButton); // 더보기 버튼이 화면에 보이도록 스크롤
                        Thread.sleep(1000); // 더보기 버튼이 보인 후에도 잠시 대기
                        js.executeScript("arguments[0].click();", moreButton); // JavaScript로 클릭 이벤트 직접 호출
                        Thread.sleep(3000); // 클릭 후 페이지 로딩 대기
                        // 더 이상 더보기 버튼 나오지 않을 경우 : 즉, 모든 리뷰 로딩한 경우
                    } catch (NoSuchElementException | TimeoutException e) {
                        log.info("All reviews loaded (using 'view more' button)");
                        break; // 더보기 버튼을 찾지 못하면 루프 종료
                    } catch (Exception e) {
                        throw new Exception("Error while loading reviews: " + e.getMessage());
                    }
                } else {
                    lastHeight = newHeight; // 페이지 높이가 변했으면 마지막 높이 업데이트
                }
            } // end of While
        } catch (Exception e) {
            log.warn("리뷰 스크롤링 중 오류 발생: {}", e.getMessage());
            throw new CrwalingException(ErrorCode.REVIEW_BUTTON_NOT_FOUND, String.format("Review Button not found(%s)", store.getName()));
        }
        log.info("{} 업체 리뷰 전체 스크롤 완료", store.getName());
    }

    /**
     *
     * @param driver - chromeDriver (getDriver() 에서 받음)
     * @param reviewUrl
     * @return 작업 성공여부(review button이 있는지에 따라 판단)
     */
    private boolean navigateToReviewTab(WebDriver driver, String reviewUrl, Cafe store) throws Exception {

        // 장소가 있는지 확인(리뷰버튼 생겼는지 확인)
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // iframe으로 전환
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("entryIframe")));

            // 리뷰 버튼 찾기 및 클릭
            WebElement reviewsButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class, '_tab-menu') and .//span[contains(text(), '리뷰')]]")));
            reviewsButton.click();
            Thread.sleep(3000);

            // 여기까지 성공했을 시, 다음 단계로 넘어가기 위해 boolean으로 성공여부 반환
            return true;

        // 없는 업체인 경우 : entryIframe를 찾지 못하여 timeoutexception 발생 : false 반환
        } catch (TimeoutException e) {
            log.info("The store doesn't exist on Naver place (No reviews button found){}", store.getName());
            return false;
        } catch (Exception e) {
            log.warn("Error while crawling {}: {}", reviewUrl, e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    // 리뷰할 업체의 상호명 + 주소를 담은 객체를 전달해 주면, naver 리뷰 검색용 주소 생성
    public String getReviewUrl(Cafe store) {
        try {
            String encodedStoreName = URLEncoder.encode(store.getName(), StandardCharsets.UTF_8.toString()); // 공백포함용으로 인코딩
            String encodedNewAddress = URLEncoder.encode(store.getAddress().getNewAddress(), StandardCharsets.UTF_8); // 공백포함용으로 인코딩

            return String.format("https://map.naver.com/p/search/%s%%20%s", encodedStoreName, encodedNewAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}


