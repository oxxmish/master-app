package ru.freemiumhosting.master.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.repository.ProjectRep;
import ru.freemiumhosting.master.service.ProjectService;


@Controller
@RequiredArgsConstructor
public class ProfileController {

    @GetMapping("/profile")
    public String getProfile(Model model) {
        return "Profile";
    }

    @GetMapping("/about")
    public String getAbout(Model model) {
        return "About";
    }
}
