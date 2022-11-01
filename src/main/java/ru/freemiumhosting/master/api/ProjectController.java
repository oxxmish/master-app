package ru.freemiumhosting.master.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.ProjectService;


@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRep projectRep;
    private final ProjectService projectService;

    @PostMapping("/api/createProject")
    public String createProject(@ModelAttribute Project project) {
        projectRep.save(project);
        //projectService.createProject(project);
        return "redirect:/projects";
    }

    @PostMapping("/api/updateProject")
    public String updateProject(@ModelAttribute Project project,
                                @ModelAttribute("launch") String launch) {
        project.setLBU(launch);
        projectRep.save(project);
        //projectService.createProject(project);
        return "redirect:/projects";
    }

    @GetMapping("/deploy")
    public String startDeploy(Model model) {
        model.addAttribute("project", new Project());
        return "Deploy";
    }

    @GetMapping("/projects")
    public String getProjects(Model model) {
        return "Projects";
    }

    @GetMapping("/projects/{projectId}")
    public String getProjectById(Model model, @PathVariable Long projectId) {
        Project project = projectRep.findProjectById(projectId);
        model.addAttribute("project", project);
        return "Project";
    }
}
