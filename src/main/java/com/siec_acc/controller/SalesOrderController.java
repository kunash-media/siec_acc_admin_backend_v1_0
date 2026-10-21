package com.siec_acc.controller;

import com.siec_acc.dto.request.SalesOrderRequestDto;
import com.siec_acc.dto.response.SalesOrderListResponseDto;
import com.siec_acc.dto.response.SalesOrderResponseDto;
import com.siec_acc.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @PostMapping("/create-sales-order")
    public ResponseEntity<SalesOrderResponseDto> createSalesOrder(@RequestBody SalesOrderRequestDto requestDto) {
        return ResponseEntity.ok(salesOrderService.createSalesOrder(requestDto));
    }

    @GetMapping("/get-sales-order-by-id/{id}")
    public ResponseEntity<SalesOrderResponseDto> getSalesOrderById(@PathVariable String id) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(id));
    }

    @GetMapping("/get-all-sales-orders")
    public ResponseEntity<List<SalesOrderResponseDto>> getAllSalesOrders() {
        return ResponseEntity.ok(salesOrderService.getAllSalesOrders());
    }

    /** Common lightweight list for other modules (e.g. Delivery Challan). Optional ?status=approved */
    @GetMapping("/sales-order-list")
    public ResponseEntity<List<SalesOrderListResponseDto>> getSalesOrderList(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderList(status));
    }

    @PutMapping("/update-sales-order/{id}")
    public ResponseEntity<SalesOrderResponseDto> updateSalesOrder(@PathVariable String id,
                                                                  @RequestBody SalesOrderRequestDto requestDto) {
        return ResponseEntity.ok(salesOrderService.updateSalesOrder(id, requestDto));
    }

    @PatchMapping("/patch-sales-order/{id}")
    public ResponseEntity<SalesOrderResponseDto> patchSalesOrder(@PathVariable String id,
                                                                 @RequestBody SalesOrderRequestDto requestDto) {
        return ResponseEntity.ok(salesOrderService.patchSalesOrder(id, requestDto));
    }

    @DeleteMapping("/delete-sales-order/{id}")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable String id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.noContent().build();
    }
}