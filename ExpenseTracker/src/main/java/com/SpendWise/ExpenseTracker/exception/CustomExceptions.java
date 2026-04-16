package com.SpendWise.ExpenseTracker.exception;

public class CustomExceptions {

    private CustomExceptions() {
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}

/*
setters = create empty object, then fill fields
builder = create object in a cleaner chained style

What is Claims?
Claims is basically a map-like object containing token data such as:
•subject
•issue time
•expiration
•custom fields if you added any

Parsing means reading some input and breaking it into meaningful structured parts.
Plain English:
•raw input comes in as text/string
•parser understands its format
•converts it into something usable

Encoding:
convert data from one representation to another like text to Base64, UTF- 8 conversion

Encrypting:
convert readable data into unreadable data using key, sending secure data over n/w

Hashing:
convert data into a fixed output using a one-way algorithm
Example:
passwords
 */
