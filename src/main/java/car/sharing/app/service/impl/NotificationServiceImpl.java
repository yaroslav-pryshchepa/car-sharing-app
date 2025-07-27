package car.sharing.app.service.impl;

import car.sharing.app.service.NotificationService;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final String telegramBotToken;
    private final String telegramChatId;

    public NotificationServiceImpl() {
        Dotenv dotenv = Dotenv.load();
        this.telegramBotToken = dotenv.get("TELEGRAM_BOT_TOKEN");
        this.telegramChatId = dotenv.get("TELEGRAM_CHAT_ID");
    }

    @Override
    public void sendMessage(String message) {
        String url = "https://api.telegram.org/bot" + telegramBotToken + "/sendMessage";

        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> body = new HashMap<>();
        body.put("chat_id", telegramChatId);
        body.put("text", message);

        try {
            restTemplate.postForObject(url, body, String.class);
        } catch (Exception e) {
            log.error("Failed to send Telegram message", e);
        }
    }
}
