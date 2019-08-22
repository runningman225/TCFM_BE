package com.future.tcfm.service;

import com.future.tcfm.model.ReqResModel.Overview;
import org.springframework.http.ResponseEntity;

public interface OverviewService {
    Overview getData(String email);
}
