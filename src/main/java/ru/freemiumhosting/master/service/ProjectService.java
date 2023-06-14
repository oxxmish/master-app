package ru.freemiumhosting.master.service;


import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.freemiumhosting.master.model.Logs;
import ru.freemiumhosting.master.model.ProjectStatus;
import ru.freemiumhosting.master.model.dto.AdminViewDto;
import ru.freemiumhosting.master.model.dto.ClusterStatisticsDto;
import ru.freemiumhosting.master.model.dto.LogsDto;
import ru.freemiumhosting.master.model.dto.ProjectDto;
import ru.freemiumhosting.master.repository.LogsRepository;
import ru.freemiumhosting.master.security.SecurityUser;
import ru.freemiumhosting.master.service.impl.*;
import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.utils.exception.InvalidProjectException;
import ru.freemiumhosting.master.utils.exception.KuberException;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.builderinfo.BuilderInfoService;
import ru.freemiumhosting.master.utils.mappers.ProjectMapper;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private final KubernetesService kubernetesService;
    private final DeployService deployService;
    private final ProjectRep projectRep;
    private final ProjectMapper projectMapper = ProjectMapper.INSTANCE;
    private final LogsRepository logsRepository;

    public ProjectDto getProjectById(Long projectId) {
        Project project = projectRep.findByIdAndOwnerId(projectId, SecurityUser.getCurrentUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Не найден запрашиваемый проект у текущего пользователя"));
        return projectMapper.projectToProjectDto(project);
    }

    public List<ProjectDto> getUsersProjects() {
        SecurityUser currentUser =  SecurityUser.getCurrentUser();
        List<Project> userProjects = projectRep.findByOwnerName(currentUser.getUsername());
        return userProjects.stream().map(projectMapper::projectToProjectDto).collect(Collectors.toList());
    }

    @SneakyThrows
    @Transactional
    public ProjectDto createProject(ProjectDto projectDto) throws DeployException {
        log.info("Start creating project with name " + projectDto.getName());
        Project project = projectMapper.projectDtoToProject(projectDto);
        project.setCreatedDate(OffsetDateTime.now());
        setUsersInfo(project);
        setDefaultRequests(project);
        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
        project = projectRep.save(project);

        deployService.deployProject(project);

        log.info("Finish creating project with name " + projectDto.getName());
        return projectMapper.projectToProjectDto(project);
    }

    private void setDefaultRequests(Project project) {
        project.setCpuRequest(0.5);
        project.setRamRequest(500.0);
        project.setStorageRequest(1.0);

        //TODO переделать фронт чтобы ел null
        project.setCpuConsumption(0.5);
        project.setStorageConsumption(1.0);
        project.setRamConsumption(500.0);
    }

    private void checkNameOfProject(Project project) {
        long check = projectRep.countByNameIgnoreCaseAndOwnerId(project.getName(), project.getOwnerId());
        if (check > 0) {
            throw new IllegalStateException("Проект с таким именем у пользователя существует");
        }
    }

    private void setUsersInfo(Project project) {
        SecurityUser currentUser = SecurityUser.getCurrentUser();
        project.setOwnerId(currentUser.getUserId());
        project.setOwnerName(currentUser.getUsername());
    }

    @Transactional
    public ProjectDto updateProject(ProjectDto projectDto) throws DeployException {
        log.info("Start updating project with name " + projectDto.getName());
        Project project = checkProjectExistenceOrThrow(projectDto.getId());
        checkNameOfProject(project);
        setChangedFields(project, projectDto);
        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
        project = projectRep.save(project);

        deployService.redeployProject(project);
        log.info("Finish creating project with name " + projectDto.getName());
        return projectMapper.projectToProjectDto(project);
    }

    private void setChangedFields(Project project, ProjectDto projectDto) {
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setGitUrl(projectDto.getGitUrl());
        project.setGitBranch(projectDto.getGitBranch());
        project.setPorts(projectDto.getPorts());
        project.setEnvs(projectDto.getEnvs());
    }

    @Transactional
    public void deleteProject(Long projectId) throws KuberException {
        log.info("Start deleting project with id " + projectId);
        checkProjectExistenceOrThrow(projectId);
        //TODO delete kuber objects
//        kubernetesService.deleteKubernetesObjects(project);
        projectRep.deleteById(projectId);
        log.info("Finish deleting project with id " + projectId);
    }

    @Transactional
    @SneakyThrows
    public void rebuildProject(Long projectId) {
        log.info("Start rebuilding project with id " + projectId);
        Project project = checkProjectExistenceOrThrow(projectId);
        project.setStatus(ProjectStatus.DEPLOY_IN_PROGRESS);
        project = projectRep.save(project);
        deployService.redeployProject(project);
        log.info("Finish rebuilding project with id " + projectId);
    }

    public void startProject(Long id) {
        log.info("Starting project with id " + id);
        Project project = checkProjectExistenceOrThrow(id);
        if (!(project.getStatus() == ProjectStatus.STOPPED)) {
            throw new InvalidProjectException("Старт проекта можно осуществить только из статуса STOOPED");
        }
        kubernetesService.startProject(project);
        log.info("Project started with id " + id);
    }

    public void stopProject(Long id) {
        log.info("Stopping project with id " + id);
        Project project = checkProjectExistenceOrThrow(id);
        if (!(project.getStatus() == ProjectStatus.ACTIVE)) {
            throw new InvalidProjectException("Стоп проекта можно осуществить только из статуса ACTIVE");
        }
        kubernetesService.stopProject(project);
        log.info("Project stopped with id " + id);
    }

    public AdminViewDto getAdminView() {
        List<ProjectDto> dtos = projectRep.findAll().stream().map(projectMapper::projectToProjectDto).collect(Collectors.toList());
        ClusterStatisticsDto dto = new ClusterStatisticsDto();
        dto.setCurrentCpu(dtos.stream().mapToDouble(ProjectDto::getCpuRequest).sum());
        dto.setCurrentRam(dtos.stream().mapToDouble(ProjectDto::getRamRequest).sum());
        dto.setCurrentStorage(dtos.stream().mapToDouble(ProjectDto::getStorageRequest).sum());
        return new AdminViewDto(dto, dtos);
    }

    public LogsDto getLogs(Long id) {
        Project project = checkProjectExistenceOrThrow(id);
        if (project.getStatus() == ProjectStatus.ACTIVE) {
            return new LogsDto(kubernetesService.getLogsOfActiveProject(project));
        } else {
            String buildLog = logsRepository.findById(project.getId()).map(Logs::getLogMessage).orElse("Build log empty");
            return new LogsDto(buildLog);
        }
    }

    private Project checkProjectExistenceOrThrow(Long projectId) {
        return projectRep.findByIdAndOwnerId(projectId, SecurityUser.getCurrentUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Не найден редактируемый проект у текущего пользователя"));
    }
}
