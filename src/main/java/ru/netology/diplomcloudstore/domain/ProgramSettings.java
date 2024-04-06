package ru.netology.diplomcloudstore.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.netology.diplomcloudstore.entities.SettingEntity;
import ru.netology.diplomcloudstore.exceptions.NotFoundSettingInDatabaseException;
import ru.netology.diplomcloudstore.repositories.SettingRepository;

import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Getter
@Setter
public class ProgramSettings {
    //мапа с параметрам
    private Map<String, String> settings;

    //секретный ключ для генерации jwt токена
    private String secret;

    //время жизни jwt токена
    private Long expiration;

    //путь к корневой папке диска для хранения медиа контента
    private String cloudDiskPath;

    //режим отключения удаления файлов с диска. относится ко всему сервису, а не к пользователям
    private boolean doDelete;

    public ProgramSettings(SettingRepository settingRepository) throws NotFoundSettingInDatabaseException, NumberFormatException {
        initialize(settingRepository);
    }

    private void initialize(SettingRepository settingRepository) throws NotFoundSettingInDatabaseException, NumberFormatException {

        Map<String, String> mapSettings = settingRepository.findAllByActive(true).stream()
                .filter(SettingEntity::isActive)
                .collect(Collectors.toMap(SettingEntity::getSettingName, SettingEntity::getSettingValue));


        if (mapSettings == null || mapSettings.size() == 0) {
            String msg = "The rows setting not found in database for generate Jwt.Check the service settings in database";
            log.error(String.join(" ", "NotFoundRowsSettingInDbException:", msg));
            throw new NotFoundSettingInDatabaseException(msg);
        }

        this.settings = mapSettings;


        if (!mapSettings.containsKey("expired")) {
            String msg = "The row setting #expired#  not found in database for generate Jwt.Check the service settings in database";
            log.error(String.join(" ", "NotFoundRowsSettingInDbException:", msg));
            throw new NotFoundSettingInDatabaseException(msg);
        }


        try {
            this.expiration = Long.parseLong(mapSettings.get("expired"));
        } catch (NumberFormatException ex) {
            log.error(String.join(" ", "ProgramSettings.initialize:", "NumberFormatException:", "Settings in database is not a digit type"));
            throw new NumberFormatException("ProgramSettings.initialize: Settings #expired# in database is not a digit type. Bad convertion to Long");
        }

        if (!mapSettings.containsKey("secret")) {
            String msg = "The rows setting #secret#  not found in database for generate Jwt. Check the service settings in database";
            log.error(String.join(" ", "ProgramSettings.initialize:", "NotFoundRowsSettingInDbException:", msg));
            throw new NotFoundSettingInDatabaseException(msg);
        }

        this.secret = mapSettings.get("secret");

        if (!mapSettings.containsKey("cloudDiskPath")) {
            String msg = "The rows setting #cloudDiskPath#  not found in database for generate media store and next use. Check the service settings in database";
            log.error(String.join(" ", "ProgramSettings.initialize:", "NotFoundRowsSettingInDbException:", msg));
            throw new NotFoundSettingInDatabaseException(msg);
        }

        this.cloudDiskPath = mapSettings.get("cloudDiskPath");

        if (!mapSettings.containsKey("doDelete")) {
            String msg = "The rows setting #doDelete#  not found in database for generate media store and next use. Check the service settings in database";
            log.error(String.join(" ", "ProgramSettings.initialize:", "NotFoundRowsSettingInDbException:", msg));
            throw new NotFoundSettingInDatabaseException(msg);
        }

        this.doDelete = Boolean.parseBoolean(mapSettings.get("doDelete"));
    }
}
