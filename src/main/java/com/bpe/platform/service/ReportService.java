package com.bpe.platform.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class ReportService {

    private static final String SCRIPT_PATH = "src/main/resources/monthly-report/getServerStatus.py";
    private static final String TEMPLATE_PATH = "src/main/resources/monthly-report/template/";
    private static final String OUTPUT_PATH = "src/main/resources/monthly-report/";

    public String executePythonScript() {
        try {
            // 출력 디렉토리 생성
            createOutputDirectory();
            
            // 현재 날짜로 파일명 생성
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년_MM월"));
            String fileName = "RnD_월간보고_" + currentDate + "_현황.pptx";
            
            // Python 스크립트 실행
            ProcessBuilder processBuilder = new ProcessBuilder();
            
            // 운영체제에 따라 Python 경로 설정
            String pythonCommand;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                pythonCommand = "python";
            } else {
                // 서버에서 Python 3.9 사용 (패키지가 설치된 버전)
                pythonCommand = "/usr/local/bin/python3.9";
            }
            
            processBuilder.command(pythonCommand, SCRIPT_PATH, fileName);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            
            // 환경 변수 설정
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            
            Process process = processBuilder.start();
            
            // 스크립트 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println("Python Output: " + line);
                
                // 진행 상황 파싱
                if (line.startsWith("PROGRESS:")) {
                    String progress = line.substring(9); // "PROGRESS:" 제거
                    System.out.println("Progress Update: " + progress);
                }
            }
            
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
                System.err.println("Python Error: " + line);
            }
            
            // 타임아웃 설정 (5분)
            boolean finished = process.waitFor(300, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return "오류: 보고서 생성 시간이 초과되었습니다.";
            }
            
            int exitCode = process.exitValue();
            
            if (exitCode == 0) {
                return "성공: " + output.toString();
            } else {
                return "실패: " + errorOutput.toString();
            }
            
        } catch (InterruptedException e) {
            // 프로세스가 중단된 경우
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            e.printStackTrace();
            return "오류: 보고서 생성이 중단되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "오류: " + e.getMessage();
        }
    }


    public File getLatestReportFile() {
        try {
            Path outputDir = Paths.get(OUTPUT_PATH);
            System.out.println("파일 검색 경로: " + outputDir.toAbsolutePath());
            System.out.println("경로 존재 여부: " + Files.exists(outputDir));
            
            if (!Files.exists(outputDir)) {
                System.out.println("출력 디렉토리가 존재하지 않습니다: " + outputDir);
                return null;
            }
            
            // .pptx 파일과 .txt 파일 모두 찾기
            File pptxFile = Files.list(outputDir)
                    .filter(path -> path.toString().endsWith(".pptx"))
                    .map(Path::toFile)
                    .max(Comparator.comparing(File::lastModified))
                    .orElse(null);
            
            if (pptxFile != null) {
                System.out.println("발견된 PowerPoint 파일: " + pptxFile.getAbsolutePath());
                return pptxFile;
            }
            
            // .pptx 파일이 없으면 .txt 파일 찾기
            File txtFile = Files.list(outputDir)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .map(Path::toFile)
                    .max(Comparator.comparing(File::lastModified))
                    .orElse(null);
            
            if (txtFile != null) {
                System.out.println("발견된 텍스트 파일: " + txtFile.getAbsolutePath());
            } else {
                System.out.println("발견된 파일이 없습니다.");
            }
            
            return txtFile;
                    
        } catch (Exception e) {
            System.out.println("파일 검색 중 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void createOutputDirectory() {
        try {
            Path outputPath = Paths.get(OUTPUT_PATH);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
