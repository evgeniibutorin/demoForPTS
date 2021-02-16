package com.example.demo.MarkService;

import com.example.demo.entity.Mark;
import com.example.demo.entity.MarkQuantity;
import com.example.demo.repository.MarkQuantityRepository;
import com.example.demo.repository.MarkRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class MarkService {

    private final MarkRepository markRepository;
    private final MarkQuantityRepository markQuantityRepository;
    Logger logger = LoggerFactory.getLogger(MarkService.class);

    public MarkService(MarkRepository markRepository, MarkQuantityRepository markQuantityRepository) {
        this.markRepository = markRepository;
        this.markQuantityRepository = markQuantityRepository;
    }

    public ArrayList<String> parseZipToArrayString(MultipartFile fileZip)throws Exception {
        String extension = FilenameUtils.getExtension(fileZip.getOriginalFilename());
        if (!extension.equals("zip")) {
            throw new RuntimeException("A " + extension + " file has been passed in the controller. Zip file expected.");
        }
        {
            ZipInputStream zis = new ZipInputStream(fileZip.getInputStream());
            try {
                ArrayList<String> csvHolder = new ArrayList<>();
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    if (zipEntry.isDirectory()) {
                        logger.debug("There is directory in the archive.");
                    } else {
                        String entryName = FilenameUtils.getExtension(zipEntry.getName());
                        if (!entryName.equals("csv")) {
                            logger.info("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                            //выдать исключение не инфор а варн если лог
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
    }

        public List<Mark> parseCSVFile(final MultipartFile file) throws Exception {
        final List<Mark> marks = new ArrayList<>();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] data = line.split(",");
                if (markRepository.findFirstByName(data[0]) != null) {
                    logger.info("Object " + data[0] + " has been added to the database");
                } else {
                    final Mark mark = new Mark();
                    mark.setName(data[0]);
                    marks.add(mark);
                    logger.info("Object " + data[0] + " is contained in the database");
                }
            }
            return markRepository.saveAll(marks);
        } catch (final IOException e) {
            logger.error("Failed to parse CSV file ", e);
            throw new Exception("Failed to parse CSV file ", e);
        }
    }

    public void createMarkAndQuantity(ArrayList<String> fromController) {
        for (int i = 0; i < fromController.size(); i++) {
            String line = fromController.get(i);
            logger.info("Line from controller: " + line);
            final String[] s = line.split("\n");//в массиве лежат строки после разделения по пробелам
            for (int j = 0; j < s.length; j++) {
                String word = s[j];
                if (s[j].contains("#")) {
                    logger.info("The comment was found in csv file: " + word);
                } else {
                    String[] afterSplit = word.split(",");
                    if (afterSplit.length == 2 && afterSplit[0].contains("mark") && markRepository.findFirstByName(afterSplit[0]) != null) {
                        MarkQuantity markQuantity = new MarkQuantity();
                        markQuantity.setQuantity(Integer.parseInt(afterSplit[1]));
                        markQuantity.setMark(markRepository.findFirstByName(afterSplit[0]));
                        markQuantityRepository.save(markQuantity);
                        logger.info("The quantity " + afterSplit[1] + " for " + afterSplit[0] + " has been added to the database.");
                    }
                    if (afterSplit.length == 2 && afterSplit[0].contains("mark") && markRepository.findFirstByName(afterSplit[0]) == null) {
                        Mark mark = new Mark();
                        mark.setName(afterSplit[0]);
                        markRepository.save(mark);
                        MarkQuantity markQuantity = new MarkQuantity();
                        markQuantity.setQuantity(Integer.parseInt(afterSplit[1]));
                        markQuantity.setMark(markRepository.findFirstByName(afterSplit[0]));
                        markQuantityRepository.save(markQuantity);
                        logger.info(afterSplit[0] + " with quantity " + afterSplit[1] + " has been added to the database.");
                    }
                    if (afterSplit.length < 2 && afterSplit[0].contains("mark") && markRepository.findFirstByName(afterSplit[0]) == null) {
                        Mark mark = new Mark();
                        mark.setName(afterSplit[0]);
                        markRepository.save(mark);
                        logger.info(afterSplit[0] + " without quantity has been added to the database.");
                    }
                }
            }
        }
    }

    public HashMap<String, ArrayList<Integer>> parseArrayStringToMap(ArrayList<String> fromController) {
        HashMap<String, ArrayList<Integer>> resultMap = new HashMap<>();
        for (int i = 0; i < fromController.size(); i++) {
            String line = fromController.get(i);
            logger.info("Line from controller: " + line);
            final String[] s = line.split("\n");//в массиве лежат строки после разделения по пробелам
            for (int j = 0; j < s.length; j++) {
                String word = s[j];
                if (s[j].contains("#")) {
                    logger.info("The comment was found in csv file: " + word);
                } else {
                    String[] StringAfterSplit = word.split(",");
                    if (StringAfterSplit.length == 2 && StringAfterSplit[0].contains("mark") && !resultMap.containsKey(StringAfterSplit[0])) {
                        resultMap.put(StringAfterSplit[0], new ArrayList<Integer>(Integer.parseInt(StringAfterSplit[1])));
                        logger.info("The quantity " + StringAfterSplit[1] + " for " + StringAfterSplit[0] + " has been added to the HashMap.");
                    }
                    if (StringAfterSplit.length == 2 && StringAfterSplit[0].contains("mark") && resultMap.containsKey(StringAfterSplit[0])) {
                        resultMap.get(StringAfterSplit[0]).add(Integer.parseInt(StringAfterSplit[1]));
                        logger.info(StringAfterSplit[0] + " with quantity " + StringAfterSplit[1] + " has been added to the HashMap.");
                    }
                    if (StringAfterSplit.length < 2 && StringAfterSplit[0].contains("mark") && !resultMap.containsKey(StringAfterSplit[0])) {
                        resultMap.put(StringAfterSplit[0], new ArrayList<Integer>());
                        logger.info(StringAfterSplit[0] + " without quantity has been added to the database.");
                    }
                }
            }
            System.out.println(resultMap);
        }
        for (String k : resultMap.keySet()) {
            Collections.sort(resultMap.get(k), Collections.reverseOrder());
        }
        return resultMap;
    }

    public HashMap<String, Integer> sumCointer(HashMap<String, ArrayList<Integer>> standardMap) {
        HashMap<String, Integer> mapAfterSort = new HashMap<>();
        for (String k : standardMap.keySet()) {
            int sum = 0;
            for (Integer d : standardMap.get(k)) {
                sum += d;
            }
            mapAfterSort.put(k, sum);
        }
        return mapAfterSort;
    }

    public HashMap<String, Integer> selectMapsWithoutNull(HashMap<String, ArrayList<Integer>> standartMap) {
        HashMap<String, Integer> mapAfterSort = new HashMap<>();
        for (String k : standartMap.keySet()) {
            if (standartMap.get(k) == null) {
                continue;
            }
            int sum = 0;
            for (Integer d : standartMap.get(k)) {
                sum += d;
            }
            mapAfterSort.put(k, sum);
        }
        return mapAfterSort;
    }
}





