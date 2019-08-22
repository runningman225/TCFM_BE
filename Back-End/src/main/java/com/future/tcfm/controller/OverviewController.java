package com.future.tcfm.controller;

import com.future.tcfm.model.ReqResModel.Overview;
import com.future.tcfm.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/overview")
public class OverviewController {
    private final
    OverviewService overviewService;

    @Autowired
    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public Overview getData(@RequestParam("email") String email) {
        return overviewService.getData(email);
    }
}
