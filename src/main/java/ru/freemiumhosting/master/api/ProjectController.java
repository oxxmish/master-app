package ru.freemiumhosting.master.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.freemiumhosting.master.model.dto.AdminViewDto;
import ru.freemiumhosting.master.model.dto.LogsDto;
import ru.freemiumhosting.master.model.dto.ProjectDto;
import ru.freemiumhosting.master.service.ProjectService;
import ru.freemiumhosting.master.service.impl.EnvService;
import ru.freemiumhosting.master.utils.exception.DeployException;
import ru.freemiumhosting.master.utils.exception.KuberException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProjectDto> getUsersProjects() {
       return projectService.getUsersProjects();
    }

    @GetMapping(value = "/adminView", produces = MediaType.APPLICATION_JSON_VALUE)
    public AdminViewDto getAdminsProjects() {
       return projectService.getAdminView();
    }

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectDto getParticularProject(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectDto createProject(@RequestBody ProjectDto dto) throws DeployException {
        return projectService.createProject(dto);
        //envService.createEnvs(dto.getEnvNames(),dto.getEnvValues(),project);
    }

    @PutMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ProjectDto updateProject(@RequestBody ProjectDto dto, @PathVariable Long id) throws DeployException {
        dto.setId(id);
        return projectService.updateProject(dto);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteProject(@PathVariable Long id) throws KuberException {
        projectService.deleteProject(id);
    }

    @PostMapping(value = "/{id}/rebuild", produces = MediaType.APPLICATION_JSON_VALUE)
    public void rebuildProject(@PathVariable Long id) {
        projectService.rebuildProject(id);
    }

    @PostMapping(value = "/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public void startProject(@PathVariable Long id) {
        projectService.startProject(id);
    }

    @PostMapping(value = "/{id}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void stopProject(@PathVariable Long id) {
        projectService.stopProject(id);
    }

    @GetMapping(value = "/{id}/logs", produces = MediaType.APPLICATION_JSON_VALUE)
    public LogsDto getLogs(@PathVariable Long id) {
       return projectService.getLogs(id);
    }
}
