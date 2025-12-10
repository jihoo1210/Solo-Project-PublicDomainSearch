package com.example.backend.service.book;

import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.example.backend.dto.book.gutendex.GutendexSentenceDto;
import com.example.backend.dto.book.gutendex.TocEntry;
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
    private static final String START_TOC = "CONTENTS[\\s\\S]*?";
    private static final Pattern SENTENCE_PATTERN =  Pattern.compile("(?<=([.!?])\\s)");

    public GutendexDocumentDto convertTextToDocumentDto(String textContent, String title) {
        // 실제 내용 추출
        String coreContent = extractCoreContent(textContent);
        // 목차 분리
        List<TocEntry> tableOfContent = extractAndProcessTOC(coreContent);
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

    private List<TocEntry> extractAndProcessTOC(String coreContent) {
        List<TocEntry> tocList = new ArrayList<>();
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

        // 로마숫자 + . + 특수문자 + 제목 패턴 (예: I.—Loomings, II.—The Carpet Bag)
        Pattern romanPattern = Pattern.compile("^\\s*([IVXLCDM]+)\\.?[—\\-–](.+)$");
        // 일반 숫자 + . + 제목 패턴 (예: 1. Chapter One)
        Pattern numberPattern = Pattern.compile("^\\s*(\\d+)\\.\\s*(.+)$");
        // CHAPTER + 숫자/로마숫자 패턴 (예: CHAPTER I, CHAPTER 1)
        Pattern chapterPattern = Pattern.compile("^\\s*(CHAPTER\\s+[IVXLCDM0-9]+)[\\.\\s—\\-–]*(.*)$", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                consecutiveEmptyLines++;
            } else {
                consecutiveEmptyLines = 0;
                inTocSection = true;
            }

            // 연속 빈 줄 3줄 이상이면 목차 끝으로 판단 (긴 목차 지원)
            if (consecutiveEmptyLines >= 3 && inTocSection) {
                break;
            }

            if (consecutiveEmptyLines == 0 && inTocSection) {
                // 점만 있는 줄 건너뛰기
                if (trimmedLine.matches("[\\.\\s]+")) {
                    continue;
                }

                String title = null;
                String chapterKey = null;

                // 1. 로마숫자 패턴 확인 (I.—Loomings)
                Matcher romanMatcher = romanPattern.matcher(trimmedLine);
                if (romanMatcher.find()) {
                    String romanNum = romanMatcher.group(1);
                    String chapterTitle = romanMatcher.group(2).trim();
                    title = romanNum + ". " + chapterTitle;
                    chapterKey = chapterTitle.replaceAll("[^a-zA-Z0-9\\s]", "").trim().toUpperCase();
                }

                // 2. CHAPTER 패턴 확인
                if (title == null) {
                    Matcher chapterMatcher = chapterPattern.matcher(trimmedLine);
                    if (chapterMatcher.find()) {
                        String chapterNum = chapterMatcher.group(1).trim();
                        String chapterTitle = chapterMatcher.group(2).trim();
                        title = chapterNum + (chapterTitle.isEmpty() ? "" : " - " + chapterTitle);
                        chapterKey = (chapterNum + " " + chapterTitle).replaceAll("[^a-zA-Z0-9\\s]", "").trim().toUpperCase();
                    }
                }

                // 3. 일반 숫자 패턴 확인 (1. Title)
                if (title == null) {
                    Matcher numberMatcher = numberPattern.matcher(trimmedLine);
                    if (numberMatcher.find()) {
                        String num = numberMatcher.group(1);
                        String chapterTitle = numberMatcher.group(2).trim();
                        title = num + ". " + chapterTitle;
                        chapterKey = chapterTitle.replaceAll("[^a-zA-Z0-9\\s]", "").trim().toUpperCase();
                    }
                }

                // 4. 기타 형식 (. 기준 분리)
                if (title == null) {
                    int dotIdx = trimmedLine.indexOf(".");
                    if (dotIdx > 0) {
                        title = trimmedLine.substring(0, dotIdx).trim();
                        String afterDot = trimmedLine.substring(dotIdx + 1).trim();
                        chapterKey = afterDot.replaceAll("[^a-zA-Z0-9\\s]", "").trim().toUpperCase();
                    } else {
                        title = trimmedLine;
                        chapterKey = trimmedLine.replaceAll("[^a-zA-Z0-9\\s]", "").trim().toUpperCase();
                    }
                }

                if (chapterKey != null && !chapterKey.isEmpty()) {
                    tocList.add(TocEntry.builder()
                            .title(title)
                            .chapterKey(chapterKey)
                            .build());
                    log.debug("TOC Entry - title: {}, chapterKey: {}", title, chapterKey);
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

        int tocStartIdx = matcher.start(); // CONTENTS 시작 위치
        String beforeToc = coreContent.substring(0, tocStartIdx);

        String afterTocStart = coreContent.substring(matcher.end());
        String[] lines = afterTocStart.split("\r?\n");

        int consecutiveEmptyLines = 0;
        boolean inTocSection = false;
        int charCount = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                consecutiveEmptyLines++;
            } else {
                consecutiveEmptyLines = 0;
                inTocSection = true;
            }

            charCount += line.length() + 1; // +1 for newline

            // 연속 빈 줄 3줄 이상이면 목차 끝으로 판단 (긴 목차 지원)
            if (inTocSection && consecutiveEmptyLines >= 3) {
                break;
            }
        }

        String afterToc = afterTocStart.substring(Math.min(charCount, afterTocStart.length()));
        return beforeToc.trim() + "\n\n" + afterToc.trim();
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
