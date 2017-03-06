Feature: My Fancy Feature
  Scenario: Fancy button opens the fancy dialog
    Given user is logged in
    And she is in the fancy module
    When she clicks the fancy button
    Then the facny dialog pops up

  Scenario: Fancy dialog is fancy
    Given user is logged in
    And the fancy dialog is opened
    When she looks at it
    Then it looks fancy