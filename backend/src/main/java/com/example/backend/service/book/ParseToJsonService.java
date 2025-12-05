package com.example.backend.service.book;

import com.example.backend.dto.book.gutendex.GutendexDocumentDto;
import com.example.backend.dto.book.gutendex.GutendexSentenceDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParseToJsonService {

    private static final String START = "\\*\\*\\* START OF THE PROJECT GUTENBERG EBOOK .*? \\*\\*\\*";
    private static final String END = "\\*\\*\\* END OF THE PROJECT GUTENBERG EBOOK .*? \\*\\*\\*";
    private static final String START_TOC = "\n\n\n\n CONTENTS";
    private static final String END_TOC = "\n\n\n\n";
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

        int tocStartIdx = coreContent.indexOf(START_TOC);
        if(tocStartIdx < 0) return tocList;

        String afterTocStart = coreContent.substring(tocStartIdx + START_TOC.length()); // 목차 이후 텍스트
        int tocEndIdx = afterTocStart.indexOf(END_TOC);
        if(tocEndIdx < 0) return tocList;

        String rawToc = afterTocStart.substring(0, tocEndIdx).trim();
        String[] lines = rawToc.split("\n");
        for(String line : lines) {
            String trimmedLien = line.trim();
            if(trimmedLien.isEmpty()) continue;

            int dotIdx = trimmedLien.indexOf(".");
            if(dotIdx > 0) {
                tocList.add(trimmedLien.substring(0, dotIdx).trim());
            } else {
                tocList.add(trimmedLien);
            }
        }
        return tocList;
    }

    private String removeTOC(String coreContent) {
        int tocStartIdx = coreContent.indexOf(START_TOC);
        if(tocStartIdx < 0) return coreContent;

        String afterTocContent = coreContent.substring(tocStartIdx + START_TOC.length());
        int tocEndIdx = coreContent.indexOf(END_TOC);
        if(tocEndIdx < 0) {
            return coreContent;
        } else {
            return afterTocContent.substring(tocEndIdx).trim();
        }
    }

    private List<GutendexSentenceDto> processBodyContent(String bodyContent) {
        List<GutendexSentenceDto> sentenceList = new ArrayList<>();

        String[] paragraphs = bodyContent.split("\n\\s*\n");
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
                        .paragraphNumber(paragraphNumber++)
                        .content(sentence)
                        .paragraphStart(paragraphStart)
                        .build());
            }
        }
        return sentenceList;
    }
}
