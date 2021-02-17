package com.example.demo.controller;

import com.example.demo.service.MarkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MarkController {

    private final ObjectMapper objectMapper;
    private final MarkService markService;

    // Первый - JSON по тем меткам, которые есть в исходных данных: одна метка - итоговое количество.
    @PostMapping(value = "/getMarksAndSumQuantity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    public byte[] getMarksAndSumQuantity(@RequestParam(value = "file") MultipartFile fileZip) throws Exception {
        log.debug("Got getMarksAndSumQuantity request. Filename: {}", fileZip.getOriginalFilename());
        return objectMapper.writeValueAsString(markService.sumByMark(markService.processCsvList(markService.parseZipToCsvList(fileZip)))).getBytes();
    }

    // Второй - как первый JSON, но включает все метки из эталонного списка, метки без количества - null.
    @ResponseBody
    @PostMapping(value = "/getAllMarksAndQuantity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public byte[] getAllMarksAndQuantityWithBaseMarks(@RequestParam(value = "file") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.sumByMarkWithBaseMarks(markService.processCsvList((markService.parseZipToCsvList(fileZip))))).getBytes();
    }

    // Третий - JSON по тем меткам, которые есть в исходных данных: одна метка - массив всех значений, по убыванию
    @ResponseBody
    @PostMapping(value = "/getMarksAndQuantityWithoutNull", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public byte[] getMarksAndSortedQuantityWithoutNull(@RequestParam(value = "file") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.sortQuantity(markService.processCsvList((markService.parseZipToCsvList(fileZip))))).getBytes();
    }
}
