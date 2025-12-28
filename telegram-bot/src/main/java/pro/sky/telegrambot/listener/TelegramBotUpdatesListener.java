package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String START_COMMAND = "/start";

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            if (update.message() != null && update.message().text() != null) {
                Long chatId = update.message().chat().id();
                String text = update.message().text();

                if (text.equals(START_COMMAND)) {
                    sendStartMessage(chatId);
                } else {
                    processReminder(chatId, text);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendStartMessage(Long chatId) {
        String welcomeMessage = """
                Привет! Я бот для создания напоминай""";

        SendMessage message = new SendMessage(chatId, welcomeMessage);
        telegramBot.execute(message);
    }

    private void processReminder(Long chatId, String text) {
        Matcher matcher = PATTERN.matcher(text);

        if (matcher.matches()) {
            String dateTimeString = matcher.group(1);
            String reminderText = matcher.group(3);

            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

                if (dateTime.isBefore(LocalDateTime.now())) {
                    sendMessage(chatId, "Нельзя создать напоминание на прошедшее время!");
                    return;
                }

                NotificationTask task = new NotificationTask(chatId, reminderText, dateTime);
                notificationTaskRepository.save(task);

                sendMessage(chatId, "Напоминание успешно создано на " + dateTimeString);

            } catch (DateTimeParseException e) {
                sendMessage(chatId, "Неверный формат даты и времени. Используйте ДД.ММ.ГГГГ ЧЧ:MM");
            }
        } else {
            sendMessage(chatId, "Неверный формат сообщения. Используйте: ДД.ММ.ГГГГ ЧЧ:MM Текст напоминания");
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        telegramBot.execute(message);
    }
}
