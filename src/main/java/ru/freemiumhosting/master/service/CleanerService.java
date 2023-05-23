package ru.freemiumhosting.master.service;

import ru.freemiumhosting.master.utils.exception.CleanException;

public interface CleanerService {
    void cleanCachedLibs(String gitClonePath) throws CleanException;
}
