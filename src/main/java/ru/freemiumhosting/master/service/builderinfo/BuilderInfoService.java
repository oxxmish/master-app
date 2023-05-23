package ru.freemiumhosting.master.service.builderinfo;

import ru.freemiumhosting.master.utils.exception.InvalidProjectException;

public interface BuilderInfoService {
    /**
     * Should check project for correctness and return path to executable file or throw InvalidProjectException otherwise.
     */
    String validateProjectAndGetExecutableFileName(String pathToProject) throws
        InvalidProjectException;
    String supportedLanguage();
}
