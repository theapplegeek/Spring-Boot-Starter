package it.theapplegeek.spring_starter_pack.user.error;

public class UserMessage {
  public static final String USER_NOT_FOUND = "http.error.user.notFound";
  public static final String USERNAME_ALREADY_EXISTS = "http.error.user.usernameExists";
  public static final String EMAIL_ALREADY_EXISTS = "http.error.user.emailExists";
  public static final String INVALID_OLD_PASSWORD = "http.error.user.invalidOldPassword";

  private UserMessage() {}
}
