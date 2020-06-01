package com.MrPhoto.photoapplication;

// opencsv와 관련된 import
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenCsv {

    /**
     * csv 파일을 읽는 함수
     * @param filePath
     * @throws IOException
     * @throws CsvException
     */
    public static void readData(String filePath) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(filePath));
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            for (int i = 0; i < nextLine.length; i++) {
                System.out.println(i + " " + nextLine[i]);
//                Log.i(i+ "=" + nextLine[i], "정보");
            }
        }
    }

    /**
     * csv 파일의 전체 목록 받기
     * - 스티커, 필터의 창을 출력할 때 목록을 받아오기 위해 실행될 함수
     * - csv파일의 모든 내용을 읽어 list로 리턴 시키는 함수
     * @param filePath: 파일이 저장된 경로
     * @return List<String[]> list
     * @throws IOException
     * @throws CsvException
     */
    public List<String[]> readAll(String filePath) throws IOException, CsvException {
        // opencsv 라이브러리에서 제공해주는 csv파일을 읽을 수 있는 클래스 선언
        CSVReader csvReader = new CSVReader(new FileReader(filePath));
        // csv파일에 있던 내용을 저장할 list 선언
        List<String[]> list = new ArrayList<>();
        // list에 csvReader로 csv파일의 모든 내용 저장
        list = csvReader.readAll();

        csvReader.close();
        // 내용이 저장된 list 반환
        return list;
    }

    /**
     * csv 파일의 생성
     * - 만약에 해당 경로에 생성된 파일이 없을 경우 새로운 파일을 생성한다.
     * @param filePath: 파일이 저장된 경로
     * @param data: 파일에 저장될 데이터
     * @throws IOException
     */
    public static void writeData(String filePath, String data) throws IOException {
        // opencsv의 CSVWriter 클래스를 통해 해당 경로에 새로운 csv파일의 생성
        CSVWriter writer = new CSVWriter(new FileWriter(filePath));
        // 데이터를 배열에 넣어 csv파일에 값을 넣기
        String[] entries = {data};
        writer.writeNext(entries);

        writer.close();
    }

    /**
     * csv 파일에 목록 한 줄 추가
     * - 스티커, 필터가 즐겨찾기에 저장될 때 목록에 추가하기 위해 실행될 함수
     * - csv 파일에 새로운 내용을 한 줄 추가
     * @param filePath: 파일이 저장된 경로
     * @param data: 파일에 저장될 데이터
     * @param stringArray: 추가되기 전의 목록 list
     * @throws IOException
     */
    public static void addData(String filePath, String data, List<String[]> stringArray) throws IOException {
        // opencsv의 목록에 쓰기 기능을 위해 CSVWriter 클래스 생성
        CSVWriter writer = new CSVWriter(new FileWriter(filePath));
        // CSVWriter 로 쓰면 파일이 새롭게 써지기 때문에 추가되기 전의 목록 list를 writeAll() 함수로 추가
        writer.writeAll(stringArray);
        // 새롭게 추가할 data를 다음 줄에 추가
        String[] entries = {data};
        writer.writeNext(entries);

        writer.close();
    }

    /**
     * csv 파일에 목록 한 줄 삭제
     * - 스티커, 필터가 즐겨찾기에서 해제될 때 목록에서 삭제하기 위해 실행될 파일
     * - csv 파일의 내용에서 삭제하고 싶은 내용을 찾아서 삭제한다.
     * @param filePath: 파일이 저장된 경로
     * @param data: 파일에서 삭제될 데이터
     * @param stringArray: 추가되기 전의 목록 list
     * @throws IOException
     */
    public static void removeData(String filePath, String data, List<String[]> stringArray) throws IOException {
        // 삭제하고자 하는 내용을 제외하고 다시 쓰기 위해서 CSVWriter클래스를 선언
        CSVWriter writer = new CSVWriter(new FileWriter(filePath));

        int idx = 0;
        int removeIdx = 0;
        // list에서 String[]로 한 줄씩 꺼내서 for in에 돌린다.
        for (String[] arrays: stringArray) {
            // String[] 에서 String으로 하나씩 꺼낸다
            for(String array: arrays){
                if (array.equals(data)) {
                    // 삭제해야 할 data를 비교해서 같으면 removeIdx 에 넣는다.
                    removeIdx = idx;
                }
            }
            idx++;
        }

        // 가져온 list에서 삭제해야할 index를 넣어 해당 부분의 줄을 삭제한다.
        stringArray.remove(removeIdx);
        // 삭제된 list를 다시 파일에 쓰기
        writer.writeAll(stringArray);

        writer.close();
    }

    public static void main(String[] args) throws IOException, CsvException {
        // csv 파일의 수정을 하기 위한 메소드가 선언된 클래스 선언
        OpenCsv csv = new OpenCsv();

//        // 1) 현재 sticker.csv 파일의 목록 확인
//        csv.readData("app/src/main/assets/sticker.csv");

        // list 선언 후 sticker.csv 파일의 목록 반환받기
        List<String[]> list = new ArrayList<>();
        list = csv.readAll("app/src/main/assets/sticker.csv");
//
//        // 2) 새로운 내용(R_011)의 추가
//        csv.addData("app/src/main/assets/sticker.csv", "R_011", list);
//        csv.readData("app/src/main/assets/sticker.csv");

        // 3) 내용(R_004)의 삭제
        csv.removeData("app/src/main/assets/sticker.csv", "R_004", list);
        csv.readData("app/src/main/assets/sticker.csv");

    }
}
