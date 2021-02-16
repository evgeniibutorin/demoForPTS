package com.example.demo.service;

import com.example.demo.ApplicationSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MarkService {

    private final static Logger logger = LoggerFactory.getLogger(MarkService.class);

    private final ApplicationSettings applicationSettings;

    private final CsvMapper mapper = new CsvMapper()
            .configure(CsvParser.Feature.ALLOW_COMMENTS, true)
            .configure(CsvParser.Feature.WRAP_AS_ARRAY, true);


    public Map<String, List<Long>> sortQuantity(Map<String, List<Long>> mapToQuantityList) {
        mapToQuantityList.values().forEach(list -> list.sort(Comparator.reverseOrder()));
        return mapToQuantityList;
    };

    public List<String> parseZipToCsvList(MultipartFile fileZip) throws Exception {
        String extension = FilenameUtils.getExtension(fileZip.getOriginalFilename());
        if (!"zip".equals(extension)) {
            throw new RuntimeException("A " + extension + " file has been passed in the controller. Zip file expected.");
        }
        ZipInputStream zis = new ZipInputStream(fileZip.getInputStream());
        try {
            List<String> csvHolder = new ArrayList<>();
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {
                    logger.debug("There is directory in the archive.");
                } else {
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        logger.warn("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                        //todo: выдать исключение не инфор а варн если лог
                    }
                    StringBuilder s = new StringBuilder();
                    int read = 0;
                    byte[] buffer = new byte[1024];
                    while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                        s.append(new String(buffer, 0, read));
                    }
                    csvHolder.add(s.toString());
                }
                zipEntry = zis.getNextEntry();
            }
            return csvHolder;
        } finally {
            zis.closeEntry();
            zis.close();
        }
    }

    public Map<String, List<Long>> processCsv(String csv) throws JsonProcessingException {
        HashMap<String, List<Long>> markToQuantities = new HashMap<>();
        MappingIterator<String[]> csvRows = mapper.readerFor(String[].class).readValue(csv);

        int rowNumber = 1;
        while (csvRows.hasNext()) {
            rowNumber++;
            String[] row = csvRows.next();
            String mark = row[0];
            String quantity = row[1];
            // if quantity = "" (empty), we should not try to parse it to number
            if (StringUtils.hasText(quantity)) {
                try {
                    Long parsedQuantity = Long.parseLong(quantity);
                    List<Long> quantityList = markToQuantities.computeIfAbsent(mark, (key) -> new ArrayList<>());
                    quantityList.add(parsedQuantity);
                } catch (Exception exc) {
                    logger.error("Got error while parsing csv. Row number: {}. Row: {}", rowNumber, row, exc);
                    // todo: map to normal exception (with response status 4xx)
                    throw exc;
                }
            } else {
                logger.warn("Got empty quantity. Row number: {}. Row: {}", rowNumber, row);
            }
        }
        return markToQuantities;
    }

    public Map<String, List<Long>> processCsvList(List<String> csvList) throws JsonProcessingException {
        Map<String, List<Long>> resultMarkToQuantityList = new HashMap<>();
        for (String csv : csvList) {
            Map<String, List<Long>> markToQuantityList = processCsv(csv);
            for (Map.Entry<String, List<Long>> markToQuantityListEntry : markToQuantityList.entrySet()) {
                String mark = markToQuantityListEntry.getKey();
                List<Long> quantityList = markToQuantityListEntry.getValue();
                List<Long> resultQuantityList = resultMarkToQuantityList.computeIfAbsent(mark, (key) -> new ArrayList<>());
                resultQuantityList.addAll(quantityList);
            }
        }
        return resultMarkToQuantityList;
    }

    public Map<String, Long> sumByMark(Map<String, List<Long>> markToQuantityList) {
        Map<String, Long> mapAfterSort = new HashMap<>();
        for (String k : markToQuantityList.keySet()) {
            long sum = 0;
            for (long d : markToQuantityList.get(k)) {
                sum += d;
            }
            mapAfterSort.put(k, sum);
        }
        return mapAfterSort;
    }

    public Map<String, Long> sumByMarkWithBaseMarks(Map<String, List<Long>> markToQuantityList) {
        Map<String, Long> markToSumQuantity = sumByMark(markToQuantityList);
        applicationSettings.getBaseMarks().forEach(baseMark -> {
            if (!markToSumQuantity.containsKey(baseMark)) {
                markToSumQuantity.put(baseMark, null);
            }
        });

        return markToSumQuantity;
    }
}
