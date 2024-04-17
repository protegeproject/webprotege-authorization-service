package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.authorization.model.AuthorizationCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authorization-commands")
public class AuthorizationCommandsController {

    private final AuthorizationCommandsService authorizationCommandsService;

    public AuthorizationCommandsController(AuthorizationCommandsService authorizationCommandsService) {
        this.authorizationCommandsService = authorizationCommandsService;
    }

    @PostMapping
    @RequestMapping("/status-authorization")
    public ResponseEntity<GetAuthorizationStatusResponse> getAuthorizationStatus(@RequestBody AuthorizationCommand<GetAuthorizationStatusRequest, GetAuthorizationStatusResponse> authorizationCommand) {

        if(authorizationCommand.request() != null) {
            GetAuthorizationStatusResponse response = authorizationCommandsService.handleAuthorizationStatusCommand((GetAuthorizationStatusRequest) authorizationCommand.request(), authorizationCommand.context());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

}
