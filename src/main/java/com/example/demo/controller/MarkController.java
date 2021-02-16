package com.example.demo.controller;

import com.example.demo.service.MarkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MarkController {

    private ObjectMapper objectMapper;
    private MarkService markService;

    Logger logger = LoggerFactory.getLogger(MarkController.class);

    public MarkController(ObjectMapper objectMapper, MarkService markService) {
        this.objectMapper = objectMapper;
        this.markService = markService;
    }

    @PostMapping(value = "/getMarksAndSumQuantity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody
    byte[] getMarksAndSumQuantity(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.sumCointer(markService.parseArrayStringToMap(markService.parseZipToArrayString(fileZip)))).getBytes();
    }

    @PostMapping(value = "/getAllMarksAndQuantity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody
    byte[] getAllMarksAndQuantity(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.parseArrayStringToMap((markService.parseZipToArrayString(fileZip)))).getBytes();
    }

    @PostMapping(value = "/getMarksAndQuantityWithoutNull", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody
    byte[] getMarksAndQuantityWithoutNull(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.selectMapsWithoutNull(markService.parseArrayStringToMap((markService.parseZipToArrayString(fileZip))))).getBytes();
    }
}
