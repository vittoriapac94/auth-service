package com.vipac.authservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.vipac.authservice.domains.User;
import com.vipac.authservice.services.CustomUserDetailService;

@Controller
public class AuthController {
	static final String lecturesServiceURL = "http://35.205.228.95/";

    @Autowired
    private CustomUserDetailService userService;
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login(String fromView) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("fromView", fromView);
        modelAndView.setViewName("login");
        return modelAndView;
    }
    
    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public ModelAndView signup() {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("signup");
        return modelAndView;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ModelAndView createNewUser(User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByEmail(user.getEmail());
        if (userExists != null) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "Esiste già un utente registrato con questa e-mail");
        } else {
        	userExists = userService.findUserByMatricola(user.getMatricola());
        	if (userExists != null) {
                bindingResult
                        .rejectValue("matricola", "error.user",
                                "Esiste già un utente registrato con questa matricola");
        	}
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("signup");
        } else {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "Utente registrato con successo");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("login");

        }
        return modelAndView;
    }


    @RequestMapping(value = {"/", "/home"}, method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        return modelAndView;
    }
    
    @RequestMapping(value = "/lectures", method = RequestMethod.GET)
	public ModelAndView lectures() {
    	ModelAndView modelAndView = new ModelAndView("redirect:" + lecturesServiceURL +"getAll");
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	User user = userService.findUserByEmail(auth.getName());
    	Gson gson = new Gson();
    	String userJSON = gson.toJson(user);
    	modelAndView.addObject("currentUserJSON", userJSON);
	    return modelAndView;
	}
    

}