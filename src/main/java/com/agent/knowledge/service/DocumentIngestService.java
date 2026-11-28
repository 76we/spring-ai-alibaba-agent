package com.agent.knowledge.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentIngestService {

    static final int CHUNK_SIZE = 400;
    static final int OVERLAP = 80;
    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourceLoader;

    /**
     * md文件内容 -> splitByHeading 按标题拆章节 -> 每个章节判断长度，长的交给  splitByLength  重叠切小块
     *  -> 循环调用  buildChunkDoc  生成带 metadata 的 Document，入库向量库。
     **/
    public DocumentIngestService(VectorStore vectorStore, ResourcePatternResolver resourceLoader) {
        this.vectorStore = vectorStore;
        this.resourceLoader = resourceLoader;
    }

    public int send() throws IOException {
        Resource[] files = resourceLoader.getResources("classpath*:/docs/*.md");
        List<Document> all = new ArrayList<>();
        int idx = 0;

        for (Resource f : files) {
            String text = new String(f.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String src = f.getFilename();
            String docId = src.replaceAll("\\.md$", "");
            String vis = visibilityOf(src);

            for (String[] sec : splitByHeading(text)) {
                for (String piece : splitByLength(sec[1], CHUNK_SIZE, OVERLAP)) {
                    all.add(Document.builder()
                            .text(piece)
                            .metadata("docId", docId)
                            .metadata("source", src)
                            .metadata("section", sec[0])
                            .metadata("visibility", vis)
                            .metadata("tenantId", "acme")
                            .metadata("version", "v1")
                            .metadata("chunkIndex", idx++)
                            .build());
                }
            }
        }
        vectorStore.add(all);
        return all.size();
    }

    private String visibilityOf(String filename) {
        if (filename.startsWith("salary")) return "HR_FINANCE";
        if (filename.startsWith("procurement")) return "FINANCE";
        return "ALL";
    }


    private List<String[]> splitByHeading(String text) {
        List<String[]> out = new ArrayList<>();
        String title = "概述";
        StringBuilder body = new StringBuilder();
        for (String line : text.split("\\R")) {
            if (line.matches("^#{1,3}\\s+.+")) {
                if (!body.toString().isBlank()) {
                    out.add(new String[]{title, body.toString()});
                    body.setLength(0);
                }
                title = line.replaceFirst("^#{1,3}\\s+", "").trim();
            } else {
                body.append(line).append('\n');
            }
        }
        if (!body.toString().isBlank()) {
            out.add(new String[]{title, body.toString()});
        }
        return out;
    }

    private List<String> splitByLength(String text, int size, int overlap) {
        List<String> parts = new ArrayList<>();
        String clean = text.trim();
        if (clean.isEmpty()) return parts;
        int start = 0;
        while (start < clean.length()) {
            int end = Math.min(start + size, clean.length());
            parts.add(clean.substring(start, end));
            if (end == clean.length()) break;
            start = end - overlap;
        }
        return parts;
    }
}
