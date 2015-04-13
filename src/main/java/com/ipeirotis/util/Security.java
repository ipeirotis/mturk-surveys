package com.ipeirotis.util;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;

public class Security {

    public static void verifyAuthenticatedUser(User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}
