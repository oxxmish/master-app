package ru.freemiumhosting.master.model;

public enum ProjectStatus {
    UNDEFINED, //Проект не создан
    CREATED, //Проект создан, не запускался ещё
    DEPLOY_IN_PROGRESS, //Скачаны и проверены сорцы с гита, создан докерфайл - ожидаем сборки образа и деплоя
    RUNNING, //Работает в кластере
    ERROR, //В ошибке
    STOPPED, //Остановлен но может быть запущен
}
