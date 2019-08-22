package com.future.tcfm.controller;


import com.future.tcfm.service.ExcelReaderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin("**")
@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    ExcelReaderService excelReaderService;

    @PostMapping
    public ResponseEntity uploadExcelFile(@RequestPart("file")MultipartFile file){
        try {
            return excelReaderService.saveFile(file) ? new ResponseEntity<>("OK!",HttpStatus.OK) :  new ResponseEntity<>("Fail to read excel, something wrong happened!", HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Error 500: something wrong happened!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping
    public ResponseEntity<InputStreamResource> downloadTemplateExcelFile() throws IOException {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=template.xlsx");
            System.out.println("download Template");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(excelReaderService.loadFile()));
    }
}
