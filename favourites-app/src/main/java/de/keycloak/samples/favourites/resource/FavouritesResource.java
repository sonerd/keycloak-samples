package de.keycloak.samples.favourites.resource;

import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FavouritesResource {

    @GetMapping(path = "/favourites")
    public String getFavourites(Model model) {
        model.addAttribute("favourites", Arrays.asList("VW Passat", "Audi A4", "BMW 520i"));
        return "favourites";
    }

    @GetMapping(path = "/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        request.logout();
        return "/";
    }
}
