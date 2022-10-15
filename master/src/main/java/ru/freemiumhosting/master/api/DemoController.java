package ru.freemiumhosting.master.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.repository.UserRep;


@Controller
@RequiredArgsConstructor
public class DemoController {

    private final UserRep userRep;
    private final ProjectRep projectRep;
    private final DockerAPI dockerAPI;

//    @PostMapping("/api/create")
//    public String createTag(@RequestBody LinkRequest req) {
//        User user = new User(req.getLink());
//        userRep.save(user);
//        return "Deploy";
//    }

    @PostMapping("/api/createProject")
    public String createProject(@ModelAttribute Project project) {
        projectRep.save(project);
        dockerAPI.postLink(project.getLink());
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
