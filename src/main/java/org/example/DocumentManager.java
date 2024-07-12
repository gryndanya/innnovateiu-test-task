import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> documentStorage = new ConcurrentHashMap<>();
    private int idCounter = 1;

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(generateUniqueId());
        }
        documentStorage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documentStorage.values().stream()
                .filter(document -> matchWithRequest(document, request))
                .collect(toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documentStorage.get(id));
    }

    private String generateUniqueId() {
        return String.valueOf(idCounter++);
    }

    private boolean matchWithRequest(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null) {
            for (String prefix : request.getTitlePrefixes()) {
                if (document.getTitle().startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }

        if (request.getContainsContents() != null) {
            for (String content : request.getContainsContents()) {
                if (document.getContent().contains(content)) {
                    return true;
                }
            }
            return false;
        }

        if (request.getAuthorIds() != null) {
            if (!request.getAuthorIds().contains(document.getAuthor().getId())) {
                return false;
            }
        }

        if (request.getCreatedFrom() != null) {
            if (document.getCreated().isBefore(request.getCreatedFrom())) {
                return false;
            }
        }

        if (request.getCreatedTo() != null) {
            if (document.getCreated().isAfter(request.getCreatedTo())) {
                return false;
            }
        }

        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}