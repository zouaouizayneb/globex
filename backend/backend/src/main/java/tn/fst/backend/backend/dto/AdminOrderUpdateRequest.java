package tn.fst.backend.backend.dto;

public class AdminOrderUpdateRequest {
    private String status;
    private Long transporteurId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTransporteurId() {
        return transporteurId;
    }

    public void setTransporteurId(Long transporteurId) {
        this.transporteurId = transporteurId;
    }
}
