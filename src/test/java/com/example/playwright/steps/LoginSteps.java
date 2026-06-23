package com.example.playwright.steps;

import com.example.playwright.core.DriverManager;
import com.example.playwright.healing.AiHealingAdvisor;
import com.example.playwright.pages.LoginPage;
import com.example.playwright.pages.SecurePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class LoginSteps {
    private final AiHealingAdvisor aiHealingAdvisor = new AiHealingAdvisor();

    @Given("the user opens the practice login page")
    public void theUserOpensThePracticeLoginPage() {
        loginPage().open();
    }

    @When("the user logs in with username {string} and password {string}")
    public void theUserLogsInWithUsernameAndPassword(String username, String password) {
        loginPage().login(username, password);
    }

    @Then("the secure area should be displayed")
    public void theSecureAreaShouldBeDisplayed() {
        assertTrue(securePage().isDisplayed(), "Secure area should be visible after valid login");
    }

    @Then("the user should be able to log out")
    public void theUserShouldBeAbleToLogOut() {
        securePage().logout();
        assertTrue(DriverManager.page().url().contains("practice-test-login"), "Logout should return to login page");
    }

    @Then("the login error should contain {string}")
    public void theLoginErrorShouldContain(String expectedMessage) {
        assertTrue(loginPage().errorMessage().contains(expectedMessage),
                "Login error should contain: " + expectedMessage);
    }

    private LoginPage loginPage() {
        return new LoginPage(DriverManager.page(), aiHealingAdvisor);
    }

    private SecurePage securePage() {
        return new SecurePage(DriverManager.page(), aiHealingAdvisor);
    }
}
