package dev.ercan.auth.boilerplate.constant;

public class ApiEndpoints {

  public static final String ERROR = "/error";

  // Auth
  public static final String ANONYMOUS_AUTH = "/v1/auth/anonymous";
  public static final String REFRESH_TOKEN_AUTH = "/v1/auth/refresh";
  public static final String GOOGLE_AUTH = "/v1/auth/google";
  public static final String APPLE_AUTH = "/v1/auth/apple";
  public static final String SESSIONS = "/v1/auth/sessions";
  public static final String LOGOUT = "/v1/auth/logout";

  private ApiEndpoints() {
    /* This utility class should not be instantiated */
  }

}
