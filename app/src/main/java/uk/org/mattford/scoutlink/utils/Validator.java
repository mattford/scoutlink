package uk.org.mattford.scoutlink.utils;

/**
 * Provides methods for validating various things.
 *
 * Created by Matt Ford on 04/04/2015.
 */
public class Validator {

    public static boolean isValidNickname(String nickname) {
        return nickname.matches("\\A[a-z_\\-\\[\\]\\\\^{}|`][a-z0-9_\\-\\[\\]\\\\^{}|`]*\\z");
    }
}
