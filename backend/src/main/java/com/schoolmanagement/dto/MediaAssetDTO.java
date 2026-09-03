package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAssetDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** Relative public URL: /v1/public/media/{id}. */
    private String url;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
}
