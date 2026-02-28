package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponse {
    private Boolean success;
    private String message;
    private String filePath;
    private String fileName;
    private Long fileSize;
}
