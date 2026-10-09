package com.siec_acc.dto.response;

import java.util.List;

public record SliceResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        boolean hasNext,
        Integer nextPage   // null when there is no next page
) {}