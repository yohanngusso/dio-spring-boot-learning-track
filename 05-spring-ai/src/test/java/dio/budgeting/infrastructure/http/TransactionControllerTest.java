package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SumTransactionsByCategoryUseCase;
import dio.budgeting.application.output.CategoryTotalOutput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.ChatClientTestConfig.class)
class TransactionControllerTest {

    @TestConfiguration
    static class ChatClientTestConfig {
        @Bean
        ChatClient.Builder chatClientBuilder() {
            return ChatClient.builder(mock(ChatModel.class));
        }
    }

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PersistTransactionUseCase persistTransactionUseCase;
    @MockitoBean
    ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    @MockitoBean
    SumTransactionsByCategoryUseCase sumTransactionsByCategoryUseCase;
    @MockitoBean
    TranscriptionModel transcriptionModel;
    @MockitoBean
    TextToSpeechModel textToSpeechModel;

    @Test
    void should_return_201_when_transaction_is_valid() throws Exception {
        when(persistTransactionUseCase.execute(any()))
                .thenReturn(new TransactionOutput("id-1", "Mercado", "GROCERIES", 80.00));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Mercado", "category": "GROCERIES", "amount": 8000}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(80.00))
                .andExpect(jsonPath("$.category").value("GROCERIES"));
    }

    @Test
    void should_return_400_with_field_errors_when_body_is_invalid() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "", "amount": 0}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));

        verifyNoInteractions(persistTransactionUseCase);
    }

    @Test
    void should_return_category_total_in_reais() throws Exception {
        when(sumTransactionsByCategoryUseCase.execute(Category.GROCERIES))
                .thenReturn(new CategoryTotalOutput("GROCERIES", 120.50));

        mockMvc.perform(get("/transactions/GROCERIES/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.total").value(120.50));
    }

    @Test
    void should_return_400_when_category_is_unknown() throws Exception {
        mockMvc.perform(get("/transactions/INVALID/total"))
                .andExpect(status().isBadRequest());
    }
}
