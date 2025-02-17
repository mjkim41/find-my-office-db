package com.digital_nomad.find_my_office.util;

import com.digital_nomad.find_my_office.exception.CsvParsingException;
import com.digital_nomad.find_my_office.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

// 소상공인진흥공단(SBIZ) 상가정보 CSV 파일을 PARSE 하는 클래스
// CommandLinerRunner : 애플리케이션 실행 시 자동 csv parse 하도록
@Component
@Slf4j
public class CsvParser implements CommandLineRunner {

    // 프로젝트 실행 시, csv 파싱 메소드(parseStoresCsvFile)를 자동 호출하도록 설정
    @Override
    public void run(String... args) throws Exception {
        parseStoresCsvFile();
    }

    /*
    [ csv 파싱 로직 ]
      - 구현 기능 : 소상공인진흥공단 상가정보 파일에서, 업종으로 필터링
      - 사용 라이브러리 : apache commons csv 라이브러리 사용 (new CSVParse 부분: vanilla code 사용 시 BufferReader 생성 후 split)
      - 로직 : (1) PatchMatchinfResourcePatternResolver.getResource()로 data 폴더에 있는 csv 파일 추출
              (2) 파일을 input stream 으로 변환
              (2) 문자 스트림(reader)로 변환
              (4) csv 파일 파싱(CSVParser) - apache common csv 라이브러리
     */
    private void parseStoresCsvFile() throws IOException {

        // # data 폴더에서 csv 파일을 추출하는 메소드
        log.info("Extracting CSV files from data folder.");
        Resource[] csvFiles = extractCsvFiles();

        // 각각의 csvFile에 대하여 parsing
            for (Resource csvFile : csvFiles) {

                log.info("Reading CSV file: {}", csvFile.getFilename());

                try (
                        InputStream inputStream = csvFile.getInputStream(); // (2)
                        Reader reader = new InputStreamReader(inputStream); // (3)
                        // (4) new CSVParser(reader 객체, csv 파일을 어떻게 처리할 것인지) : apache common csv 라이브러리
                        CSVParser allStoresRecords  = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader()); // (3)
                )
                {

                    // 헤더 출력
                    Map<String, Integer> headerMap = allStoresRecords.getHeaderMap();
                    System.out.println(headerMap.keySet());

                    // 몇 줄만 출력하는 용
                    Iterator<CSVRecord> record = allStoresRecords.iterator();

                    // 테스트용 출력
                    int i = 0;
                    while (record.hasNext() && i < 50)  {

                        CSVRecord tuple = record.next();

                        // 카페인 것만 필터링
                        if (tuple.get("상권업종소분류명").equals("카페")) {
                            String 상가업소번호 = tuple.get("상가업소번호");
                            String 상호명 = tuple.get("상호명");
                            String 지점명 = tuple.get("지점명");
                            String 상권업종대분류코드 = tuple.get("상권업종대분류코드");
                            String 상권업종대분류명 = tuple.get("상권업종대분류명");
                            String 상권업종중분류코드 = tuple.get("상권업종중분류코드");
                            String 상권업종중분류명 = tuple.get("상권업종중분류명");
                            String 상권업종소분류코드 = tuple.get("상권업종소분류코드");
                            String 상권업종소분류명 = tuple.get("상권업종소분류명");
                            String 표준산업분류코드 = tuple.get("표준산업분류코드");
                            String 표준산업분류명 = tuple.get("표준산업분류명");
                            String 시도코드 = tuple.get("시도코드");
                            String 시도명 = tuple.get("시도명");
                            String 시군구코드 = tuple.get("시군구코드");
                            String 시군구명 = tuple.get("시군구명");
                            String 행정동코드 = tuple.get("행정동코드");
                            String 행정동명 = tuple.get("행정동명");
                            String 법정동코드 = tuple.get("법정동코드");
                            String 법정동명 = tuple.get("법정동명");
                            String 지번코드 = tuple.get("지번코드");
                            String 대지구분코드 = tuple.get("대지구분코드");
                            String 대지구분명 = tuple.get("대지구분명");
                            String 지번본번지 = tuple.get("지번본번지");
                            String 지번부번지 = tuple.get("지번부번지");
                            String 지번주소 = tuple.get("지번주소");
                            String 도로명코드 = tuple.get("도로명코드");
                            String 도로명 = tuple.get("도로명");
                            String 건물본번지 = tuple.get("건물본번지");
                            String 건물부번지 = tuple.get("건물부번지");
                            String 건물관리번호 = tuple.get("건물관리번호");
                            String 건물명 = tuple.get("건물명");
                            String 도로명주소 = tuple.get("도로명주소");
                            String 구우편번호 = tuple.get("구우편번호");
                            String 신우편번호 = tuple.get("신우편번호");
                            String 동정보 = tuple.get("동정보");
                            String 층정보 = tuple.get("층정보");
                            String 호정보 = tuple.get("호정보");
                            String 경도 = tuple.get("경도");
                            String 위도 = tuple.get("위도");

                            // 각 변수에 대한 값 출력
                            System.out.println("상가업소번호: " + 상가업소번호 +
                                    ", 상호명: " + 상호명 +
                                    ", 지점명: " + 지점명 +
                                    ", 상권업종대분류코드: " + 상권업종대분류코드 +
                                    ", 상권업종대분류명: " + 상권업종대분류명 +
                                    ", 상권업종중분류코드: " + 상권업종중분류코드 +
                                    ", 상권업종중분류명: " + 상권업종중분류명 +
                                    ", 상권업종소분류코드: " + 상권업종소분류코드 +
                                    ", 상권업종소분류명: " + 상권업종소분류명 +
                                    ", 표준산업분류코드: " + 표준산업분류코드 +
                                    ", 표준산업분류명: " + 표준산업분류명);

                            System.out.println("========================"); // 한 줄 띄우기
                            System.out.println();

                        }

                        i++;

                    }

                    log.info("CSV file parsing completed: {}", csvFile.getFilename());

                } catch (IOException e) {
                    log.error("Error occurred while parsing CSV file: {}", csvFile.getFilename(), e);
                    throw new RuntimeException(e);
                }


            }


    }

    // # data 폴더에서 csv 파일을 추출하는 메소드
    private Resource[] extractCsvFiles() throws IOException {
        // (1) PathMatchingResourcePatternResolver.getResources() : 해당 resources 경로에 있는 모든 파일 추출
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] csvFiles = resolver.getResources("classpath:/data/*.csv");

        // csv 파일 찾지 못하면, custom error를 throw
        if(csvFiles.length == 0) {
            throw new CsvParsingException(ErrorCode.CSV_NOT_FOUND);
        }

        return csvFiles;
    }

}
