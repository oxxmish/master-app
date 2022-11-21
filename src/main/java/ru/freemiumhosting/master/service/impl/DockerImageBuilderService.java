package ru.freemiumhosting.master.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class DockerImageBuilderService {

    @SneakyThrows
    public void startDeploy() {
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectOutput(new File("build_logs.log"))
                .redirectError(new File("error_log.log"))
                .command("cmd.exe", "/c", "docker ps -v").start();
        System.out.println("123");
    }

}
