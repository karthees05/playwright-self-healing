package com.example.playwright.steps;

import com.example.playwright.core.DriverManager;
import com.example.playwright.pages.LoginPage;
import com.example.playwright.pages.SecurePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class LoginSteps {
    /** Opens the login page for the Cucumber Given step. */
    @Given("the user opens the practice login page")
    public void theUserOpensThePracticeLoginPage() {
        loginPage().open();
    }

    /** Submits credentials supplied by the Cucumber scenario. */
    @When("the user logs in with username {string} and password {string}")
    public void theUserLogsInWithUsernameAndPassword(String username, String password) {
        loginPage().login(username, password);
    }

    /** Asserts that valid login displays the secure-area heading. */
    @Then("the secure area should be displayed")
    public void theSecureAreaShouldBeDisplayed() {
        assertTrue(securePage().isDisplayed(), "Secure area should be visible after valid login");
    }

    /** Logs out and asserts navigation returns to the login page. */
    @Then("the user should be able to log out")
    public void theUserShouldBeAbleToLogOut() {
        securePage().logout();
        assertTrue(DriverManager.page().url().contains("practice-test-login"), "Logout should return to login page");
    }

    /** Asserts that the displayed error includes the expected message. */
    @Then("the login error should contain {string}")
    public void theLoginErrorShouldContain(String expectedMessage) {
        assertTrue(loginPage().errorMessage().contains(expectedMessage),
                "Login error should contain: " + expectedMessage);
    }

    /** Creates a login page object for the current scenario's browser. */
    private LoginPage loginPage() {
        return new LoginPage(DriverManager.page());
    }

    /** Creates a secure page object for the current scenario's browser. */
    private SecurePage securePage() {
        return new SecurePage(DriverManager.page());
    }
}
