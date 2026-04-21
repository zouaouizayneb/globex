package tn.fst.backend.backend.dto;

import lombok.Data;

@Data
public class PageRequest {
    private int page = 0;
    private int size = 10;
    private String sortBy = "dateOrder";
    private String sortDirection = "DESC";
}