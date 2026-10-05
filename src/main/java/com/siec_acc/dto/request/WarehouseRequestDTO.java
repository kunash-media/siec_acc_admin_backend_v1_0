package com.siec_acc.dto.request;


public class WarehouseRequestDTO {
    private String warehouseName;
    private String warehouseAddress;

    public WarehouseRequestDTO(String warehouseName, String warehouseAddress) {
        this.warehouseName = warehouseName;
        this.warehouseAddress = warehouseAddress;
    }

    public WarehouseRequestDTO() {

    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseAddress() {
        return warehouseAddress;
    }

    public void setWarehouseAddress(String warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
    }
}
