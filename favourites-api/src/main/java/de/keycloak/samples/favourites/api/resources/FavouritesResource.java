package de.keycloak.samples.favourites.api.resources;

import de.keycloak.samples.favourites.api.model.Favourite;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FavouritesResource {

    private final Favourite favouriteOfTom = Favourite.builder().withComment("Nice restaurant").withId("123456").withRating(5).withUserName("tom").build();

    private final Favourite favouriteOfTim = Favourite.builder().withComment("Bad restaurant").withId("225588").withRating(2).withUserName("tim").build();

    private final List<Favourite> favourites = Stream.of(favouriteOfTim, favouriteOfTom).collect(Collectors.toList());

    @RequestMapping(value = "/favourites/{username}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<Favourite>> getFavouritesOfUser(@PathVariable final String username, final Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final String authenticatedUserName = principal.getName();
        if (!username.equals(authenticatedUserName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        final List<Favourite> favouritesByUser = this.favourites.stream().filter(r -> r.getUserName().equalsIgnoreCase(authenticatedUserName)).collect(Collectors.toList());
        return ResponseEntity.ok(favouritesByUser);
    }

    @RequestMapping(value = "/favourites", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Favourite>> getAllFavourites() {
        return ResponseEntity.ok(favourites);
    }
}
