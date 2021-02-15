package com.example.demo.MarkService;

import com.example.demo.entity.Mark;
import com.example.demo.entity.MarkQuantity;
import com.example.demo.repository.MarkQuantityRepository;
import com.example.demo.repository.MarkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarkService {

    private final MarkRepository markRepository;

    private final MarkQuantityRepository markQuantityRepository;

    Logger logger = LoggerFactory.getLogger(MarkService.class);

    public MarkService(MarkRepository markRepository, MarkQuantityRepository markQuantityRepository) {
        this.markRepository = markRepository;
        this.markQuantityRepository = markQuantityRepository;
    }


    public List<Mark> parseCSVFile(final MultipartFile file) throws Exception {
        final List<Mark> marks = new ArrayList<>();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) { // BufferedReader класс считывает текст из символьного потока ввода, буферизируя прочитанные символы. InputStreamReader получает данные из потока, считывает байты и декодирует их в символы
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
            logger.error("Failed to parse CSV file ", e); // объявление логера ошибок
            throw new Exception("Failed to parse CSV file ", e);
        }
    }

    public void putIntoDB (ArrayList<String> fromController){
        for (int i = 0; i< fromController.size(); i++){
            String line = fromController.get(i);
            final String[] s = line.split(",");
            for (int j = 0; i< fromController.size(); i++){
                String word = s[i];
                if(word.contains("#")){
                    //todo:logg
                }
                if (word.contains("mark") && markRepository.findFirstByName(word)!=null){
                    MarkQuantity markQuantity = new MarkQuantity();
                    markQuantity.setQuantity(Integer.parseInt(s[i+1]));
                    markQuantity.setMark(markRepository.findFirstByName(word));
                    markQuantityRepository.save(markQuantity);
                    logger.info("The quantity for " + word + " has been added to the database.");

                }
                if (word.contains("mark") && markRepository.findFirstByName(word)==null){
                    Mark mark = new Mark();
                    mark.setName(word);
                    markRepository.save(mark);
                    MarkQuantity markQuantity = new MarkQuantity();
                    markQuantity.setQuantity(Integer.parseInt(s[i+1]));
                    markQuantity.setMark(markRepository.findFirstByName(word));
                    markQuantityRepository.save(markQuantity);
                    logger.info(word + " with quantity has been added to the database.");
                }

            }
        }
    }




}
