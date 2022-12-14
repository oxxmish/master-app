package ru.freemiumhosting.master.service.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.service.CleanerService;

import java.io.File;


@Service
@Slf4j
public class CleanerServiceImpl implements CleanerService {
    @Override
    @SneakyThrows
    public void cleanCachedLibs(String gitClonePath) {
        log.info("Старт очистки закэшированных зависимостей");
        try {
            FileUtils.deleteDirectory(new File("/usr/local/lib"));
            FileUtils.deleteDirectory(new File(gitClonePath));
            log.info("Очистка зависимостей завершена успешно");
        } catch (Exception e) {
            log.error("Ошибка при попытке очистить кеш", e);
        }
//        Process process = new ProcessBuilder()
//                .command("rm", "-r", "/usr/local/lib")
//                .command("rm", "-r", gitClonePath)
//                .inheritIO()
//                .start();
//        int exitCode = process.waitFor();
//        if (exitCode != 0) log.error("Ошибка при попытке очистить кеш");
    }
}
