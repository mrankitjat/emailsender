package com.example.recruitment.controller;

import com.example.recruitment.dto.EmailRequest;
import com.example.recruitment.dto.EmailResponse;
import com.example.recruitment.exception.GlobalExceptionHandler;
import com.example.recruitment.service.RecruitmentOrchestrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecruitmentOrchestrationService orchestrationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        EmailController controller = new EmailController(orchestrationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/email/send with full request body should return 200 OK")
    void testSendEmails_FullBody() throws Exception {
        EmailRequest request = new EmailRequest(
                List.of("abc@company.com", "xyz@company.com"),
                "Application for Software Developer",
                "Dear Recruiter..."
        );

        EmailResponse response = new EmailResponse(2, 2, 0, List.of());
        when(orchestrationService.processAndSendEmails(any(EmailRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecipients").value(2))
                .andExpect(jsonPath("$.success").value(2))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.failedEmails").isEmpty());
    }

    @Test
    @DisplayName("POST /api/email/send with empty JSON body should return 200 OK")
    void testSendEmails_EmptyBody() throws Exception {
        EmailResponse response = new EmailResponse(100, 98, 2, List.of("abc@test.com", "xyz@test.com"));
        when(orchestrationService.processAndSendEmails(any())).thenReturn(response);

        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecipients").value(100))
                .andExpect(jsonPath("$.success").value(98))
                .andExpect(jsonPath("$.failed").value(2))
                .andExpect(jsonPath("$.failedEmails[0]").value("abc@test.com"))
                .andExpect(jsonPath("$.failedEmails[1]").value("xyz@test.com"));
    }

    @Test
    @DisplayName("POST /api/email/send without body payload should return 200 OK")
    void testSendEmails_NoPayload() throws Exception {
        EmailResponse response = new EmailResponse(3, 3, 0, List.of());
        when(orchestrationService.processAndSendEmails(null)).thenReturn(response);

        mockMvc.perform(post("/api/email/send"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecipients").value(3))
                .andExpect(jsonPath("$.success").value(3))
                .andExpect(jsonPath("$.failed").value(0));
    }
}
