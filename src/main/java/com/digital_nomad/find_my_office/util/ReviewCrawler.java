package com.digital_nomad.find_my_office.util;

import jdk.jshell.spi.ExecutionControlProvider;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

// 리뷰 크롤링을 위해 WebDriver 불러오는 클래스
@Slf4j
public class ReviewCrawler {

    public static WebDriver getDriver() {

        // 1. chromedriver 경로 설정 (드라이버 파일 프로젝트에 저장해두었음)
        Path path = Paths.get("src/main/resources/driver/chromedriver132.exe");
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

    // ## 테스트용
    static class StoreForTest {

        String storeName;
        String newAddress;

        public StoreForTest(String storeName, String newAddress) {
            this.storeName = storeName;
            this.newAddress = newAddress;
        }

    }

    public static void main(String[] args) {

        WebDriver driver = getDriver();

        // 테스트용 : 크롤링할 업체 목록
        List<StoreForTest> stores = new ArrayList<>();
        stores.add(new StoreForTest("ㄴㅇㄹ피", "경기도ㅇㄹㅇㄹ산본로386번길"));
        stores.add(new StoreForTest("너트커피", "경기도 군포시 산본로386번길"));

        // url 생성
        List<String> urls = stores.stream()
                .map((store) -> {
                    try {
                        String encodedStoreName = URLEncoder.encode(store.storeName, StandardCharsets.UTF_8.toString()); // 공백포함용으로 인코딩
                        String encodedNewAddress = URLEncoder.encode(store.newAddress, StandardCharsets.UTF_8.toString()); // 공백포함용으로 인코딩
                        return String.format("https://map.naver.com/p/search/%s%%20%s", encodedStoreName, encodedNewAddress);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());

        // 크롤링 시작
        urls.forEach((url) -> {

            log.info("start crawling: {}", url);

            // 네이버 플레이스 접속
            driver.get(url);

            // 장소가 있는지 확인(리뷰버튼 생겼는지 확인)
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                // iframe으로 전환
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("entryIframe")));

                // 리뷰 버튼 찾기 및 클릭
                WebElement reviewsButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@class, '_tab-menu') and .//span[contains(text(), '리뷰')]]")));
                reviewsButton.click();
                Thread.sleep(3000);

                // 페이지 끝까지 스크롤 + 더보기 클릭
                JavascriptExecutor js = (JavascriptExecutor) driver;
                long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

                while (true) {
                    js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    Thread.sleep(2000); // 페이지 로딩 시간을 충분히 주기 위해 대기

                    long newHeight = (long) js.executeScript("return document.body.scrollHeight");

                    if (newHeight == lastHeight) {
                        // 페이지 높이가 변하지 않았으면 더보기 버튼 클릭
                        try {
                            WebElement moreButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[.//span[contains(text(), '더보기')]]")));
                            js.executeScript("arguments[0].scrollIntoView(true);", moreButton); // 더보기 버튼이 화면에 보이도록 스크롤
                            Thread.sleep(1000); // 더보기 버튼이 보인 후에도 잠시 대기
                            js.executeScript("arguments[0].click();", moreButton); // JavaScript로 클릭 이벤트 직접 호출
                            Thread.sleep(5000); // 클릭 후 페이지 로딩 대기
                        } catch (NoSuchElementException e) {
                            log.info("더보기 버튼을 클릭하여 리뷰를 모두 불러왔습니다.");
                            break; // 더보기 버튼을 찾지 못하면 루프 종료
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                            log.warn("기타 에러 발생");
                            break; // 기타 에러 발생 시에도 반복문 종료
                        }
                    } else {
                        lastHeight = newHeight; // 페이지 높이가 변했으면 마지막 높이 업데이트
                    }
                }

                // TRY 끝
            } catch (NoSuchElementException e) {
                log.info("no reviews found");
            } catch (Exception e) {
                log.warn(e.getMessage());
                log.warn("그외 에러");
            }

            // 장소가 있으면, 리뷰 버튼 클릭

        }); // 크롤링 끝
    }
}
