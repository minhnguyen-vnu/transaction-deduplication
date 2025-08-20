package com.example.shipment_service.ui.restful;

import com.example.shipment_service.core.domain.dto.shipment.CreateOrUpdateShipmentDTO;
import com.example.shipment_service.core.domain.dto.shipment.ShipmentDTO;
import com.example.shipment_service.core.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentDTO> create(@RequestBody CreateOrUpdateShipmentDTO dto) {
        return ResponseEntity.ok(shipmentService.createShipment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getAll() {
        return ResponseEntity.ok(shipmentService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentDTO> update(@PathVariable Long id,
                                              @RequestBody CreateOrUpdateShipmentDTO dto) {
        return ResponseEntity.ok(shipmentService.updateShipment(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}