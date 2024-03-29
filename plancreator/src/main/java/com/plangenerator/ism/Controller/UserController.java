package com.plangenerator.ism.Controller;


import com.plangenerator.ism.Dto.FormData;
import com.plangenerator.ism.JsonClasses.Item;
import com.plangenerator.ism.Model.User;
import com.plangenerator.ism.Service.DepartmentService;
import com.plangenerator.ism.Service.MinimumService;
import com.plangenerator.ism.Service.UserService;
import com.plangenerator.ism.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Irakli Kardava
 */
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserServiceImpl userServiceimpl;

    @Autowired
    private MinimumService minimumService;

    @Autowired
    private DepartmentService depatmentService;

    Map<String, String> userInfoMap = new HashMap<String, String>();

    List<Item> plans = new ArrayList<Item>();

    /**
     * @param model
     * @param error
     * @param logout
     * @return login view
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout) {
        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");
        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");
        return "login";
    }

    /**
     * @param model
     * @return sign up view
     */
    @GetMapping(value = "/signup")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    /**
     * @param user
     * @param model
     * @return view sign up
     */
    @PostMapping(value = "/signup")
    public String registration(@ModelAttribute("user") User user, Model model) {
        if (userServiceimpl.isEsist(user.getUsername())) {
            model.addAttribute("usererr", "This username already used!");
            return "signup";
        }
        if (!user.getPasswordConfirm().equals(user.getPassword())) {
            model.addAttribute("passerror", "Password does not match!");
            return "signup";
        }
        userService.save(user);
        model.addAttribute("msg", "User registered successfully");
        return "signup";
    }

    @GetMapping(value = "/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value = "/plan")
    public String plan(ModelMap model) {
        model.addAttribute("map", userInfoMap);
        return "user/plan";
    }

//    @PreAuthorize("hasRole('ROLE_USER')")
//    @RequestMapping(value = "/form", method = RequestMethod.GET)
//    public String startForm(Model model) {
//        System.out.println(model.containsAttribute("plans"));
//        return "user/form";
//    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/plan", method = RequestMethod.POST)
    public String mapFormData(@ModelAttribute("user") FormData user, Model model) {
        userInfoMap.put("name", user.getName());
        userInfoMap.put("surname", user.getSurName());
        userInfoMap.put("teacher", user.getTeacher());
        userInfoMap.put("semester", user.getSemester());
        userInfoMap.put("albumNr", user.getAlbumNumber());
        userInfoMap.put("year", user.getYear());
        System.out.println(userInfoMap);
        return "user/plan";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value = "/user/welcome")
    public Model ShowUserDetails(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            model.addAttribute("username", userDetail.getUsername());
            model.addAttribute("minimum", minimumService.getMinimumList());
            model.addAttribute("mincount", minimumService.countPlan() + " Programs");
            model.addAttribute("depcout", depatmentService.countDepartment() + " Departments");
            model.addAttribute("usercout", userServiceimpl.countUser() + " Users");
        }

        return model;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public @ResponseBody
    ModelAndView addNewWorker(@RequestBody List<Item> jsonString) {
        plans = jsonString;
        System.out.println(new ModelAndView("user/form", "plans", plans));
        return new ModelAndView("user/form", "plans", plans);

    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public ModelAndView download(HttpServletRequest req, HttpServletResponse res) {
        System.out.println(plans);
        String typeReport = req.getParameter("type");
        if (typeReport != null && typeReport.equals("xls")) {
            return new ModelAndView(new XlsView(),"plans", plans);

        }
        return new ModelAndView("user/form", "plans", plans);
    }
}
