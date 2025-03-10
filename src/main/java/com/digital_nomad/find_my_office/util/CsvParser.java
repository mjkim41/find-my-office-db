package com.digital_nomad.find_my_office.util;

import com.digital_nomad.find_my_office.service.AddressService;
import com.digital_nomad.find_my_office.domain.cafe.entity.Address;
import com.digital_nomad.find_my_office.domain.cafe.entity.Cafe;
import com.digital_nomad.find_my_office.exception.CsvParsingException;
import com.digital_nomad.find_my_office.exception.ErrorCode;
import com.digital_nomad.find_my_office.service.CafeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 소상공인진흥공단(SBIZ) 상가정보 CSV 파일을 PARSE 하는 클래스
// CommandLinerRunner : 애플리케이션 실행 시 자동 csv parse 하도록
@Component
@Slf4j
public class CsvParser implements CommandLineRunner {

    // 처음 실행여부를 관리하기 위한 파일 경로 (com.digital_nomad.find_my_office.isFirstRun 위치에 저장)
    private static final String CSV_PARSER_EXECUTED = "src/main/java/com/digital_nomad/find_my_office/isFirstRun/CsvParserExecuted.txt";

    @Autowired
    private CafeService cafeService;

    @Autowired
    private AddressService addressService;

    // 프로젝트 실행 시, csv 파싱 메소드(parseStoresCsvFile)를 자동 호출하도록 설정
    @Override
    public void run(String... args) throws Exception {

        // 1. 파일이 이미 존재하는지 확인(초기 실행시는 파일 없고, 그다음부터는 파일 생성되어 있음)
        File flag = new File(CSV_PARSER_EXECUTED);

        // 2. 파일이 존재하지 않으면 처음 실행인 것으로 판단하고 처리
        if (!flag.exists()) {
            parseStoresCsvFile();
            createCsvParsedFlagFile(flag); // csv parsing 끝난 후에, 완료 증거로 file 생성 -> 다음부터는 csv parsing 과정 생략됨
        } else {
            // 이미 실행된 경우 메시지 출력
            log.info("CSV parsing already completed.");
        }
    }

    private void createCsvParsedFlagFile(File flagFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(flagFile))) {
            writer.write("csv parsed");
        } catch (IOException e) {
            log.error("Error while creating the CSV parsed flag file.", e);
            e.printStackTrace();
        }
    }

    // csv parser 처음 실행 후, 두번째 실행부터는 실행안되게 application 설정을 바꿔주는 메소드
    private void updateFirstRunFlag() {

    }

    /*
    [ csv 파싱 로직 ]
      - 구현 기능 : 소상공인진흥공단 상가정보 파일에서, 업종으로 필터링
      - 사용 라이브러리 : apache commons csv 라이브러리 사용 (new CSVParse 부분: vanilla code 사용 시 BufferReader 생성 후 split)
      - 로직 : (1) PatchMatchinfResourcePatternResolver.getResource()로 data 폴더에 있는 csv 파일 추출
              (2) 파일을 input stream 으로 변환
              (2) 문자 스트림(reader)로 변환
              (4) csv 파일 파싱(CSVParser.getRecords()) - apache common csv 라이브러리
     */
    private void parseStoresCsvFile() throws IOException {

        // # data 폴더에서 csv 파일을 추출하는 메소드
        log.info("Extracting CSV files from data folder.");
        Resource[] csvFiles = extractCsvFiles();

        // 각각의 csvFile에 대하여 parsing
            for (Resource csvFile : csvFiles) {

                String filename = csvFile.getFilename(); // 로그용
                log.info("Reading CSV file: {}",filename);

                try (
                        InputStream inputStream = csvFile.getInputStream(); // (2)
                        Reader reader = new InputStreamReader(inputStream); // (3)
                        // (4) new CSVParser(reader 객체, csv 파일을 어떻게 처리할 것인지) : apache common csv 라이브러리
                        CSVParser allStoresRecords  = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader()); // (3)
                )
                {
                    // 테스트용 칼럼명 디버그
                    Map<String, Integer> headerMap = allStoresRecords.getHeaderMap();
                    log.debug(headerMap.keySet().toString());

                    // (4) (Apache common csv 라이브러리) CSVParserser.getRecords()로 모든 행 반환 후,
                    //     상권업종소분류명이 카페인 업체만 필터링
                    List<CSVRecord> records = allStoresRecords.getRecords();
                    List<CSVRecord> coffeeShops = records.stream()
                            .filter(record -> "카페".equals(record.get("상권업종소분류명")))
                            .collect(Collectors.toList());

                    // (5) Entity로 만들어 DB 업데이트
                    regularDbUpdate(coffeeShops, filename);

                } catch (IOException e) {
                    log.error("Error occurred while parsing CSV file: {}", csvFile.getFilename(), e);
                    throw new RuntimeException(e);
                }
            }
    }

    /**
     * csv 파일의 카페 정보를 CSVRecord -> Entity 변환 -> DB 저장
     * @param coffeeShops - csv 파일에서 카페업종만 필터링된 상점 정보
     * @param filename - csv 파일 이름(로그용)
     */
    private void regularDbUpdate(List<CSVRecord> coffeeShops, String filename) throws IOException {

        coffeeShops.forEach(coffee -> {
            // entity 생성
            Address address = creatAddressEntity(coffee);
            Cafe cafe = createCafeEntity(coffee); // 주소는 db 중복 저장을 방지 하기 위해 나중에 세팅
            cafeService.doRegularDbUpdate(cafe, address);
        });

        log.info("CSV file parsing completed: {}", filename);
    }

    private Address creatAddressEntity(CSVRecord coffee) {

        return Address.builder()
                .latitude(Double.valueOf(coffee.get("경도")))
                .longitude(Double.valueOf(coffee.get("위도")))
                .oldZipCode(coffee.get("구우편번호"))
                .oldAddress(coffee.get("지번주소"))
                .adminDistrictCode(coffee.get("행정동코드"))
                .administrativeDistrictName(coffee.get("행정동명"))
                .legalDistrictCode(coffee.get("법정동코드"))
                .legalDistrictName(coffee.get("법정동명"))
                .newAddressCode(coffee.get("도로명코드"))
                .newAddress(coffee.get("도로명"))
                .lotNumber(coffee.get("건물본번지"))
                .subLotNumber(coffee.get("건물부번지"))
                .newZipCode(coffee.get("신우편번호"))
                .provinceCode(coffee.get("시도코드"))
                .provinceName(coffee.get("시도명"))
                .cityCode(coffee.get("시군구코드"))
                .cityName(coffee.get("시군구명"))
                .dong(coffee.get("동정보"))
                .floor(coffee.get("층정보"))
                .ho(coffee.get("호정보"))
                .build();

    }

    private Cafe createCafeEntity(CSVRecord coffee) {
        return Cafe.builder()
                .id(coffee.get("상가업소번호"))
                .name(coffee.get("상호명"))
                .branchName(coffee.get("지점명"))
                .build();
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
