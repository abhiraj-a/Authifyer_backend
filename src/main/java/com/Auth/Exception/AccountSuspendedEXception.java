package com.Auth.Exception;

import org.springframework.http.HttpStatus;

public class AccountSuspendedEXception  extends ApiException{
    public AccountSuspendedEXception() {
        super("Account suspended" , HttpStatus.FORBIDDEN);
    }
}
