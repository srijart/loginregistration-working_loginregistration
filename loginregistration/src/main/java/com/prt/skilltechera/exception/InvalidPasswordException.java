package com.prt.skilltechera.exception;

public class InvalidPasswordException extends RuntimeException{
    public InvalidPasswordException(String Message) {
        super(Message);
    }
}
