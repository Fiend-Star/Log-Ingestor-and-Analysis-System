package com.fiendstar.logIngestor;

import com.fiendstar.logIngestor.controller.LogController;
import com.fiendstar.logIngestor.repository.LogEventRepository;
import com.fiendstar.logIngestor.service.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@SpringBootTest
class LogIngestorApplicationTests {

    @MockBean
    private LogEventRepository logRepository;

    @MockBean
    private LogService logService;

    @MockBean
    private LogController logController;

    @Test
    void contextLoads() {
    }

}
