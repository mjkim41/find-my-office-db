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

    @Autowired
    private CafeService cafeService;

    @Autowired
    private AddressService addressService;

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
              (4) csv 파일 파싱(CSVParser.getRecords()) - apache common csv 라이브러리
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

                    // 테스트용 칼럼명 디버그
                    Map<String, Integer> headerMap = allStoresRecords.getHeaderMap();
                    log.debug(headerMap.keySet().toString());

                    // (4) (Apache common csv 라이브러리) CSVParserser.getRecords()로 모든 행 반환 후,
                    //     상권업종소분류명이 카페인 업체만 필터링
                    List<CSVRecord> records = allStoresRecords.getRecords();
                    List<CSVRecord> coffeeShops = records.stream()
                            .filter(record -> "카페".equals(record.get("상권업종소분류명")))
                            .collect(Collectors.toList());

                    coffeeShops.forEach(coffee -> {
                        // 주소  entity 생성 및 저장
                        Address cafeAddress = creatAddressEntity(coffee);
//                        cafeAddress = addressService.regularAddressDbUpdate(cafeAddress); // 위도 +경도 조합 없을 때만 저장
                        // 카페 entity 생성 및 db 저장
                        Cafe cafeEntity = createCafeEntity(coffee, cafeAddress);
                        cafeService.regularCafeDbUpdate(cafeEntity);
                    });

                    log.info("CSV file parsing completed: {}", csvFile.getFilename());


                } catch (IOException e) {
                    log.error("Error occurred while parsing CSV file: {}", csvFile.getFilename(), e);
                    throw new RuntimeException(e);
                }
            }


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
                .lotNumber(coffee.get("건물본번지"))
                .subLotNumber(coffee.get("건물부번지"))
                .newAddressCode(coffee.get("도로명코드"))
                .newAddress(coffee.get("도로명"))
                .newZipCode(coffee.get("신우편번호"))
                .ProvinceCode(coffee.get("시도코드"))
                .ProvinceName(coffee.get("시도명"))
                .CityCode(coffee.get("시군구코드"))
                .CityName(coffee.get("시군구명"))
                .dong(coffee.get("동정보"))
                .floor(coffee.get("층정보"))
                .ho(coffee.get("호정보"))
                .build();

    }

    private Cafe createCafeEntity(CSVRecord coffee, Address cafeAddress) {
        return Cafe.builder()
                .id(coffee.get("상가업소번호"))
                .name(coffee.get("상호명"))
                .branchName(coffee.get("지점명"))
                .address(cafeAddress)
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
