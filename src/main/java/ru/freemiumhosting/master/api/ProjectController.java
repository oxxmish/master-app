package ru.freemiumhosting.master.api;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.freemiumhosting.master.exception.DeployException;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.service.ProjectService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;


    @PostMapping("/api/createProject")
    public String createProject(@ModelAttribute Project project) {
        String errorMessage = null;
        try {
            projectService.createProject(project);
        } catch (DeployException deployException) {
            log.error("Error executing request", deployException);
            errorMessage = deployException.getMessage();
        } catch (Exception e) {
            log.error("Error executing request", e);
            errorMessage = e.getMessage();
        }
        return errorMessage == null ? "redirect:/projects" : MessageFormat.format(
            "redirect:/deploy/?errorMessage={1}", project.getId(), URLEncoder.encode(errorMessage));
    }

    @PostMapping("/api/updateProject")
    public String updateProject(@ModelAttribute Project project) {
        String errorMessage = null;
        try {
            projectService.updateProject(project);
        } catch (DeployException deployException) {
            log.error("Error executing request", deployException);
            errorMessage = deployException.getMessage();
        } catch (Exception e) {
            log.error("Error executing request", e);
            errorMessage = e.getMessage();
        }
        return errorMessage == null ? "redirect:/projects" : MessageFormat.format(
            "redirect:/projects/{0}/?errorMessage={1}", project.getId(), URLEncoder.encode(errorMessage));
    }

    @GetMapping("/deploy")
    public String startDeploy(Model model, @RequestParam(required = false) String errorMessage) {
        model.addAttribute("project", new Project());
        if (!StringUtils.isEmpty(errorMessage)) {
            model.addAttribute("errorMessage", "*Ошибка: " + errorMessage);
        }
        return "Deploy";
    }

    @GetMapping("/projects")
    public String getProjects(Model model) {
        List<Project> projects = projectService.getAllProjects();
        model.addAttribute("projects", projects);
        return "Projects";
    }

    @GetMapping("/projects/{projectId}")
    public String getProjectById(Model model, @PathVariable Long projectId,
                                 @RequestParam(required = false) String errorMessage) {
        Project project = projectService.findProjectById(projectId);
        model.addAttribute("project", project);
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
