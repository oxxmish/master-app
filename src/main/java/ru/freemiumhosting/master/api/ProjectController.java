package ru.freemiumhosting.master.api;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.freemiumhosting.master.model.dto.ProjectDto;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.service.ProjectService;
import ru.freemiumhosting.master.service.impl.EnvService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final EnvService envService;

    @PostMapping("/api/createProject")
    public String createProject(@ModelAttribute ProjectDto dto) {
        String errorMessage = null;
        try {
            projectService.createProject(dto);
            //envService.createEnvs(dto.getEnvNames(),dto.getEnvValues(),project);
        } catch (Exception deployException) {
            log.error("Error executing request", deployException);
            errorMessage = deployException.getMessage();
        }
        return errorMessage == null ? "redirect:/projects" : MessageFormat.format(
            "redirect:/deploy/?errorMessage={0}", URLEncoder.encode(errorMessage));
    }

    @Transactional
    @PostMapping("/api/updateProject")
    public String updateProject(@ModelAttribute ProjectDto dto) {
        String errorMessage = null;
        //var project = projectMapper.toEntity(dto); //TODO: need delete ALL previous envs and persist new ones
        try {
            projectService.updateProject(dto);
        } catch (Exception deployException) {
            log.error("Error executing request", deployException);
            errorMessage = deployException.getMessage();
        }
        return errorMessage == null ? "redirect:/projects" : MessageFormat.format(
            "redirect:/deploy/?errorMessage={1}", dto.getId(), URLEncoder.encode(errorMessage));
    }

    @GetMapping("/projects/updateDeploy/{projectId}")
    public String updateDeploy(@PathVariable Long projectId) {
        String errorMessage = null;
        try {
            Project project = projectService.findProjectById(projectId);
            projectService.updateDeploy(project);
        } catch (Exception e) {
            log.error("Error executing request", e);
            errorMessage = e.getMessage();
        }
        return errorMessage == null ? "redirect:/projects" : MessageFormat.format(
            "redirect:/projects?errorMessage={0}", URLEncoder.encode(errorMessage));
    }

    @GetMapping("/projects/delete/{projectId}")
    public String deleteProject(@PathVariable Long projectId) {
        String errorMessage = null;
        try {
            Project project = projectService.findProjectById(projectId);
            projectService.deleteProject(project);
        } catch (Exception e) {
            log.error("Error executing request", e);
            errorMessage = e.getMessage();
        }
        return errorMessage == null ? "redirect:/projects" : MessageFormat.format(
            "redirect:/projects?errorMessage={0}", URLEncoder.encode(errorMessage));
    }

    @GetMapping("/deploy")
    public String startDeploy(Model model, @RequestParam(required = false) String errorMessage) {
        model.addAttribute("project", new ProjectDto());
        if (!StringUtils.isEmpty(errorMessage)) {
            model.addAttribute("errorMessage", "*Ошибка: " + errorMessage);
        }
        return "Deploy";
    }

    @GetMapping("/projects")
    public String getProjects(Model model, @RequestParam(required = false) String errorMessage) {
        List<Project> projects = projectService.getAllProjects();
        model.addAttribute("projects", projects);
        if (!StringUtils.isEmpty(errorMessage)) {
            model.addAttribute("errorMessage", "*Ошибка: " + errorMessage);
        }
        return "Projects";
    }

    @GetMapping("/projects/{projectId}")
    public String getProjectById(Model model, @PathVariable Long projectId,
                                 @RequestParam(required = false) String errorMessage) {
        Project project = projectService.findProjectById(projectId);
        model.addAttribute("project", project);
        model.addAttribute("envs", envService.getEnvsByProject(project));

        if (!StringUtils.isEmpty(errorMessage)) {
            model.addAttribute("errorMessage", "*Ошибка: " + errorMessage);
        }
        return "Project";
    }


    @GetMapping("/")
    public String index(Model model) {
        return "Index";
    }
}
