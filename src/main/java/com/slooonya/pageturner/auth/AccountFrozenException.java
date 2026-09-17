package com.slooonya.pageturner.auth;


public class AccountFrozenException extends RuntimeException{
    public AccountFrozenException(String message){
        super(message);
    }
}

