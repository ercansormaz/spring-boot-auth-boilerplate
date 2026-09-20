package dev.ercan.auth.boilerplate.constant;

public class ApiEndpoints {

  public static final String ERROR = "/error";

  // Auth
  public static final String ANONYMOUS_AUTH = "/v1/auth/anonymous";
  public static final String REFRESH_TOKEN_AUTH = "/v1/auth/refresh";

  private ApiEndpoints() {
    /* This utility class should not be instantiated */
  }

}
