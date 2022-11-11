package ru.freemiumhosting.master.service.builderinfo;

public interface BuilderInfoService {
    String getExecutableFileName(String pathToProject);
    String supportedLanguage();
}
