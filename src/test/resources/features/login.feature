Feature: Practice Test Automation login

  The framework uses a public demo login page to show Playwright, Java, Gradle,
  Cucumber, page objects, and Playwright MCP self-healing locators.

  Background:
    Given the user opens the practice login page

  @smoke @self-healing
  Scenario: Successful login with valid credentials
    When the user logs in with username "student" and password "Password123"
    Then the secure area should be displayed
    And the user should be able to log out

  @negative @self-healing
  Scenario: Invalid username shows an error message
    When the user logs in with username "wrong-user" and password "Password123"
    Then the login error should contain "Your username is invalid"
