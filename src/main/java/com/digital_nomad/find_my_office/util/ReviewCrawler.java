package com.digital_nomad.find_my_office.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

// 리뷰 크롤링을 위해 WebDriver 불러오는 클래스
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
//        options.addArguments("--headless");

        // 3. ChromeDriver 객체 생성 후 반환
        return new ChromeDriver(options);
    }

    public static void main(String[] args) {

        WebDriver driver = getDriver();

        // 구글 맵 검색(상호명 + 도로명 주소 앞부분)
        driver.get("https://www.google.com/maps?q=키미아트 서울특별시 종로구 평창30길");

        // 5초 대기 후 종료
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 리뷰 항목 찾기 (구글 맵에서 리뷰 목록은 'section ' 내에 있음)
        List<WebElement> reviews = driver.findElements(By.cssSelector("span[jsname='bN97Pc']")); // 리뷰 텍스트가 포함된 요소

        // 리뷰 버튼 클릭 (리뷰가 포함된 버튼을 찾아 클릭)
        try {
            // 리뷰 버튼이 나타날 때까지 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 10초가 지나면 timeoutexception 발생
            WebElement reviewsButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[text()='리뷰']")));  // <div>리뷰<div> 요소가 발견될 때 까지 대기
            // 버튼 클릭
            reviewsButton.click();
            Thread.sleep(3000); // 리뷰 패널이 로드될 시간을 기다림
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (WebElement review : reviews) {
            System.out.println("리뷰: " + review.getText());
        }


        // 브라우저 종료
        driver.quit();
    }
}
