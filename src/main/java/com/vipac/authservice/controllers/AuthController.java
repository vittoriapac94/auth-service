package com.vipac.authservice.controllers;

import com.vipac.authservice.configs.MessagingConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthController {
	static final String lecturesServiceURL = "https://localhost:9443/";
    static final String oktaServiceURL = "https://localhost:8443/";

    @Autowired
    private RabbitTemplate template;

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

    @RequestMapping(value = {"/okta"}, method = RequestMethod.GET)
    public ModelAndView oktalogin() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:" + oktaServiceURL );
        return modelAndView;
    }
    
    @RequestMapping(value = "/lectures", method = RequestMethod.GET)
	public ModelAndView lectures(HttpServletRequest request, HttpServletResponse response) {
    	ModelAndView modelAndView = new ModelAndView("redirect:" + lecturesServiceURL +"getAll");
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	System.out.println("auth: "+auth);
    	User user = userService.findUserByEmail(auth.getName());

        Gson gson = new Gson();
        String userJSON = gson.toJson(user);
        String integrityAuth = org.apache.commons.codec.digest.DigestUtils.sha1Hex(userJSON);

    	//Set cookie
        Cookie cookie = new Cookie("sessionID", integrityAuth);
        cookie.setMaxAge(7 * 24 * 60 * 60); // expires in 7 days
        cookie.setSecure(true);
        //add cookie to response
        response.addCookie(cookie);

        //Publish user on queue
        template.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.ROUTING_KEY, user);

    	modelAndView.addObject("currentUserJSON", userJSON);
        modelAndView.addObject("integrityAuth", integrityAuth);
	    return modelAndView;
	}

    @RequestMapping(value = "/lecturessaml", method = RequestMethod.GET)
    public ModelAndView lecturesSaml(String currentUserJSON) {
        ModelAndView modelAndView = new ModelAndView("redirect:" + lecturesServiceURL +"getAll");

        String integrityAuth = org.apache.commons.codec.digest.DigestUtils.sha1Hex(currentUserJSON);
        modelAndView.addObject("currentUserJSON", currentUserJSON);
        modelAndView.addObject("integrityAuth", integrityAuth);
        return modelAndView;
    }
    

}