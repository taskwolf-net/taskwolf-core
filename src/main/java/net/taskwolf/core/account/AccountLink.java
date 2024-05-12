package net.taskwolf.core.account;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountLink {
  /**
   * Checks whether a user account exists
   * @param id The id of an user or an organization
   * @return A future that contains the existence boolean
   */
  CompletableFuture<Boolean> accountExists(UUID id);

  /**
   * Is used to find all accounts that are registered for one user / organization
   * @param id The id of an user or an organization
   * @return The list of account identifiers
   */
  CompletableFuture<List<String>> findAccounts(UUID id);

  /**
   * Removes an account
   * @param id The id of an user or an organization
   * @param identifier The account identifier
   */
  void removeAccount(UUID id, String identifier);

  /**
   * Is used to find the registration url of the module
   * @param id The id of an user or an organization
   * @param apiKey The api key to verify user request
   * @return The registration url
   */
  String registrationUrl(UUID id, String apiKey);

  /**
   * Builds the description that is shown in the registration process
   * @return The description of the account link
   */
  String description();
}
