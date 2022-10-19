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
public class MainController {

    private final ProjectRep projectRep;
    private final ProjectService projectService;

    @PostMapping("/api/createProject")
    public String createProject(@ModelAttribute Project project) {
        projectRep.save(project);
        projectService.createProject(project);
        return "Projects";
    }

    @GetMapping("/deploy")
    public String startDeploy(Model model) {
        model.addAttribute("project",new Project());
        return "Deploy";
    }

    @GetMapping("/projects")
    public String getProjects(Model model) {
        return "Projects";
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        return "Profile";
    }
}
