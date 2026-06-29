package com.xarch.example.ai.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.ai.entity.RagDocument;
import com.xarch.example.ai.entity.RagKnowledgeBase;
import com.xarch.example.ai.mapper.RagDocumentMapper;
import com.xarch.example.ai.mapper.RagKnowledgeBaseMapper;
import com.xarch.example.ai.service.RagService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Stub RAG service — persists knowledge bases and documents and
 * returns deterministic stub results for search. Production wiring
 * should delegate to {@code knowledge-mcp} (ingestion / chunking) and
 * {@code vector-mcp} (vector search).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final RagKnowledgeBaseMapper kbMapper;
    private final RagDocumentMapper docMapper;

    @Override
    public PageResult<RagKnowledgeBase> pageKnowledgeBases(String keyword, int pageNum, int pageSize) {
        try {
            QueryWrapper wrapper = QueryWrapper.create().where("del_flag = 0");
            if (keyword != null && !keyword.isBlank()) {
                wrapper.and("name LIKE ?", "%" + keyword + "%");
            }
            wrapper.orderBy("create_time", false);
            var page = kbMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("RagService.pageKnowledgeBases unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    @Override
    public List<RagKnowledgeBase> listKnowledgeBases() {
        try {
            return kbMapper.selectListByQuery(
                    QueryWrapper.create().where("del_flag = 0")
                            .orderBy("create_time", false));
        } catch (Exception e) {
            log.warn("RagService.listKnowledgeBases unavailable: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public RagKnowledgeBase getKnowledgeBase(Long id) {
        try {
            return kbMapper.selectOneById(id);
        } catch (Exception e) {
            log.warn("RagService.getKnowledgeBase unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public RagKnowledgeBase createKnowledgeBase(RagKnowledgeBase kb) {
        kb.setCreateTime(LocalDateTime.now());
        kb.setUpdateTime(LocalDateTime.now());
        if (kb.getDelFlag() == null) {
            kb.setDelFlag(0);
        }
        if (kb.getDocumentCount() == null) {
            kb.setDocumentCount(0);
        }
        if (kb.getTotalChunks() == null) {
            kb.setTotalChunks(0L);
        }
        kbMapper.insert(kb);
        return kb;
    }

    @Override
    @Transactional
    public RagKnowledgeBase updateKnowledgeBase(RagKnowledgeBase kb) {
        kb.setUpdateTime(LocalDateTime.now());
        kbMapper.updateById(kb);
        return kb;
    }

    @Override
    @Transactional
    public void deleteKnowledgeBase(Long id) {
        try {
            RagKnowledgeBase kb = kbMapper.selectOneById(id);
            if (kb == null) {
                return;
            }
            kb.setDelFlag(1);
            kb.setUpdateTime(LocalDateTime.now());
            kbMapper.updateById(kb);
        } catch (Exception e) {
            log.warn("RagService.deleteKnowledgeBase failed: {}", e.getMessage());
        }
    }

    @Override
    public PageResult<RagDocument> pageDocuments(Long knowledgeBaseId, String keyword,
                                                 int pageNum, int pageSize) {
        try {
            QueryWrapper wrapper = QueryWrapper.create()
                    .where("del_flag = 0")
                    .and("knowledge_base_id = ?", knowledgeBaseId);
            if (keyword != null && !keyword.isBlank()) {
                wrapper.and("title LIKE ?", "%" + keyword + "%");
            }
            wrapper.orderBy("create_time", false);
            var page = docMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("RagService.pageDocuments unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    @Override
    @Transactional
    public RagDocument ingest(IngestRequest request) {
        RagDocument doc = new RagDocument();
        doc.setKnowledgeBaseId(request.getKnowledgeBaseId());
        doc.setTitle(request.getTitle());
        doc.setSourceUri(request.getSourceUri());
        doc.setContentType(request.getContentType() != null
                ? request.getContentType()
                : "text/plain");
        doc.setStatus(1);
        doc.setCreateUserId(request.getCreateUserId());
        doc.setCreateUserName(request.getCreateUserName());
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());
        doc.setDelFlag(0);
        docMapper.insert(doc);
        return doc;
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        try {
            RagDocument doc = docMapper.selectOneById(id);
            if (doc == null) {
                return;
            }
            doc.setDelFlag(1);
            doc.setUpdateTime(LocalDateTime.now());
            docMapper.updateById(doc);
        } catch (Exception e) {
            log.warn("RagService.deleteDocument failed: {}", e.getMessage());
        }
    }

    @Override
    public List<SearchHit> search(SearchRequest request) {
        // Production: route to vector-mcp / knowledge-mcp for actual
        // semantic search. The current stub returns an empty list so
        // the API surface is exercisable.
        log.debug("RagService.search query='{}' topK={}", request.getQuery(), request.getTopK());
        return List.of();
    }
}
