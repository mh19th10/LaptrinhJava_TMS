package vn.edu.uth.quanlidaythem.controller;

import org.springframework.web.bind.annotation.*;
import vn.edu.uth.quanlidaythem.domain.Fee;
import vn.edu.uth.quanlidaythem.service.FeeService;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@CrossOrigin(origins = "*")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @GetMapping
    public List<Fee> getAll() {
        return feeService.getAllFees();
    }

    @GetMapping("/search")
    public List<Fee> search(@RequestParam String keyword) {
        return feeService.search(keyword);
    }
}
