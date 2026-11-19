package com.siec_acc.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Statement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** One generic, metadata-driven payload for every report: the page renders columns/rows/totals as described here. */
public final class ReportDto {

    private ReportDto() {}

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Report {
        private Meta meta;
        private List<Card> cards;               // the 4 stat cards
        private List<Card> breakdown;           // optional strip (aging buckets); empty when unused
        private List<Column> columns;
        private List<Map<String, Object>> rows;
        private Map<String, Object> totals;     // numeric columns only, computed over ALL rows (not just this page)
        private Pagination pagination;
        private List<String> notes;
        private List<Statement> statements;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Meta {
        private String type;
        private String title;
        private LocalDate from;                 // null for snapshot reports
        private LocalDate to;
        private LocalDate asOf;
        private String groupBy;
        private List<Option> groupByOptions;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Option {
        private String value;
        private String label;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Card {
        private String key;
        private String label;
        private String kind;                    // count | money
        private BigDecimal value;
        private String tone;                    // default | primary | amber | red | green | blue | signed
    }

    /**
     * type: text | date | int | qty | money | percent | status
     * subKey: second (muted) line under the value, read from the same row.
     * unitKey: for qty columns, the row key holding the unit to print after the number.
     * tone: default | muted | primary | amber | red | green | blue | signed (green when >= 0, red when negative).
     */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Column {
        private String key;
        private String label;
        private String align;                   // left | right | center
        private String type;
        private String tone;
        private String subKey;
        private String unitKey;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Pagination {
        private int page;                       // 1-based
        private int size;
        private long totalItems;
        private int totalPages;
    }

    /** One panel of the accounting report. */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Statement {
        private String key;
        private String title;
        private String subtitle;
        private List<StatementLine> lines;
    }

    /** kind: header | line | subtotal | grand | memo. tone: default | green | red | primary | muted | signed. */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StatementLine {
        private String label;
        private BigDecimal amount;              // null for header rows
        private String kind;
        private String tone;
        private int indent;
    }

}