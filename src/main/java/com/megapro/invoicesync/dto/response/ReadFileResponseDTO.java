package com.megapro.invoicesync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadFileResponseDTO {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;
}
