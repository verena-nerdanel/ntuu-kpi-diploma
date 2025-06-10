package org.vg.markusbro.core.service.plugins.llm;

import lombok.Data;

import java.util.List;

@Data
public class ApiResponse {
    private List<OutputItem> output;
}

@Data
class OutputItem {
    private List<ContentItem> content;
}

@Data
class ContentItem {
    private String type;
    private String text;
}