package com.vallesol.bff.controller;

import com.vallesol.bff.client.GeoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoProxyController {

    private final GeoClient geoClient;

    @GetMapping("/mapped-reports")
    public ResponseEntity<?> getMappedReports() {
        return ResponseEntity.ok(geoClient.findAllMappedReports());
    }
}
