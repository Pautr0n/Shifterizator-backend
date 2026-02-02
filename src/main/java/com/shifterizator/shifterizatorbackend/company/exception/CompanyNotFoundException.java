    package com.shifterizator.shifterizatorbackend.company.exception;

    public class CompanyNotFoundException extends RuntimeException {
        public CompanyNotFoundException(String message) {
            super(message);
        }
    }
