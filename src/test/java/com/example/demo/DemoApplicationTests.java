package com.example.demo;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void correctZipParsing() throws Exception {
        File testFile = getTestFile();
        MockMultipartFile file
                = new MockMultipartFile("file", testFile.getName(), MediaType.MULTIPART_FORM_DATA_VALUE, FileUtils.readFileToByteArray(testFile));
        mockMvc.perform(MockMvcRequestBuilders.multipart("/getMarksAndSumQuantity")
                .file(file))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private File getTestFile() throws IOException {
        return ResourceUtils.getFile("classpath:request/content/source_archive.zip");
    }

}
