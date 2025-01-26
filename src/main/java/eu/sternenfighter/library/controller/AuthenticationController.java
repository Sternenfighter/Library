package eu.sternenfighter.library.controller;

import eu.sternenfighter.library.dto.LoginUser;
import eu.sternenfighter.library.dto.User;
import eu.sternenfighter.library.model.Customer;
import eu.sternenfighter.library.service.CustomerService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final CustomerService customerService;

    @Autowired
    public AuthenticationController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping()
    public ResponseEntity<User> login(@Valid @RequestBody LoginUser loginUser) {
        try {
            if (loginUser.getEmail().matches("([a-zA-Z0-9])+@([a-zA-Z0-9])+\\.([a-zA-Z0-9])+")) {
                Optional<Customer> customer = customerService.login(loginUser.getEmail(), loginUser.getPassword());
                if (customer.isPresent()) {
                    String token = getJWTToken(customer.get().getName());
                    User user = new User();
                    user.setEmail(loginUser.getEmail());
                    user.setToken(token);
                    return new ResponseEntity<>(user, HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getJWTToken(String username) {
        String secretKey = "mySecretKey";
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");

        String token = Jwts.builder().setId("softtekJWT").setSubject(username).claim("authorities",
                        grantedAuthorities.stream().map(GrantedAuthority::getAuthority).toList())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

        return "Bearer " + token;
    }
}
