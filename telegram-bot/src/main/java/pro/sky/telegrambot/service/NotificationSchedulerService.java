package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationSchedulerService {
    private final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Scheduled(cron = "0 * * * * *") // Каждую минуту в 0 секунд
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        logger.info("Looking for notifications to send at {}", now);

        List<NotificationTask> tasks = notificationTaskRepository.findByNotificationDateTime(now);

        if (tasks.isEmpty()) {
            logger.info("No notifications found for {}", now);
            return;
        }

        logger.info("Found {} notifications to send", tasks.size());

        for (NotificationTask task : tasks) {
            SendMessage message = new SendMessage(task.getChatId(), task.getMessageText());
            telegramBot.execute(message);
            logger.info("Sent notification to chat {}: {}", task.getChatId(), task.getMessageText());

        }
    }
}
