package com.example.backend.service.book;

import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.example.backend.dto.book.gutendex.GutendexSentenceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ParseToJsonService {

    private static final String START = "\\*\\*\\* START OF THE PROJECT GUTENBERG EBOOK .*? \\*\\*\\*";
    private static final String END = "\\*\\*\\* END OF THE PROJECT GUTENBERG EBOOK .*? \\*\\*\\*";
    private static final String START_TOC = "(?i)CONTENTS[\\s\\S]*?";
    private static final Pattern SENTENCE_PATTERN =  Pattern.compile("(?<=([.!?])\\s)");

    public GutendexDocumentDto convertTextToDocumentDto(String textContent, String title) {
        // 실제 내용 추출
        String coreContent = extractCoreContent(textContent);
        // 목차 분리
        List<String> tableOfContent = extractAndProcessTOC(coreContent);
        // 본문 분리
        String bodyContent = removeTOC(coreContent);
        List<GutendexSentenceDto> sentences = processBodyContent(bodyContent);

        return GutendexDocumentDto.builder()
                .title(title)
                .tableOfContent(tableOfContent)
                .sentences(sentences)
                .build();
    }

    private String extractCoreContent(String textContent) {
        Pattern start = Pattern.compile(START, Pattern.CASE_INSENSITIVE);
        Pattern end = Pattern.compile(END, Pattern.CASE_INSENSITIVE);

        Matcher startMatcher = start.matcher(textContent);
        Matcher endMatcher = end.matcher(textContent);

        int startIdx = 0;
        int endIdx = textContent.length();

        if (startMatcher.find()) {
            startIdx = startMatcher.end(); // 시작 마커가 끝나는 부분
        }
        if (endMatcher.find()) {
            endIdx = endMatcher.start(); // 종료 마커가 시작하는 부분
        }
        if(startIdx < endIdx) {
            return textContent.substring(startIdx, endIdx).trim();
        }
        return textContent;
    }

    private List<String> extractAndProcessTOC(String coreContent) {
        List<String> tocList = new ArrayList<>();
        Pattern pattern = Pattern.compile(START_TOC);
        Matcher matcher = pattern.matcher(coreContent);

        if (!matcher.find()) {
            log.info("START_TOC 패턴을 찾을 수 없습니다.");
            return tocList;
        }

        int tocStartIdx = matcher.end();

        String afterTocStart = coreContent.substring(tocStartIdx); // 목차 이후 텍스트
        String[] lines = afterTocStart.split("\r?\n");
        int consecutiveEmptyLines = 0;
        boolean inTocSection = false;
        for(String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                // 빈 줄이 나오면 카운터를 증가시킵니다.
                consecutiveEmptyLines++;
            } else {
                // 내용이 있는 줄이 나오면 카운터를 초기화합니다.
                consecutiveEmptyLines = 0;

                // 목차 본문이 시작되었음을 표시
                inTocSection = true;
            }

            if (consecutiveEmptyLines >= 2 && inTocSection) {
                break; // 반복문 종료
            }
            if (consecutiveEmptyLines == 0 && inTocSection) {

                if (trimmedLine.matches("\\.+")) {
                    continue;
                }
                // 항목 끝의 마침표(.)를 기준으로 제목만 추출하는 로직 (원본 코드 유지)
                int dotIdx = trimmedLine.indexOf(".");
                if(dotIdx > 0) {
                    // 숫자가 아닌 글자만 포함하는 경우를 위해 trim() 처리
                    tocList.add(trimmedLine.substring(0, dotIdx).trim());
                } else {
                    tocList.add(trimmedLine);
                }
            }
        }
        return tocList;
    }

    private String removeTOC(String coreContent) {
        Pattern pattern = Pattern.compile(START_TOC);
        Matcher matcher = pattern.matcher(coreContent);

        if (!matcher.find()) {
            return coreContent;
        }

        int tocStartIdx = matcher.end();
        String afterTocStart = coreContent.substring(tocStartIdx);

        String[] lines = afterTocStart.split("\r?\n");

        int consecutiveEmptyLines = 0;
        boolean inTocSection = false;
        int endLineIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();

            if (trimmed.isEmpty()) {
                consecutiveEmptyLines++;
            } else {
                consecutiveEmptyLines = 0;
                inTocSection = true;
            }

            if (inTocSection && consecutiveEmptyLines >= 2) {
                endLineIndex = i + 1;
                break;
            }
        }

        if (endLineIndex < 0) {
            return coreContent;
        }

        return coreContent.substring(tocStartIdx + endLineIndex);
    }


    private List<GutendexSentenceDto> processBodyContent(String bodyContent) {
        List<GutendexSentenceDto> sentenceList = new ArrayList<>();

        String[] paragraphs = bodyContent.split("\r?\n\\s*\r?\n");
        int paragraphNumber = 1;

        for (String paragraph : paragraphs) {
            String trimmedParagraph = paragraph.trim();
            if(trimmedParagraph.isEmpty()) continue;

            String[] sentences = SENTENCE_PATTERN.split(trimmedParagraph);
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                if(sentence.isEmpty()) continue;

                boolean paragraphStart = (i == 0);
                sentenceList.add(GutendexSentenceDto.builder()
                        .sentenceNumber(i + 1)
                        .paragraphNumber(paragraphNumber)
                        .content(sentence)
                        .paragraphStart(paragraphStart)
                        .build());
            }
            paragraphNumber++;
        }
        return sentenceList;
    }
}
