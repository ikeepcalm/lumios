package dev.ua.ikeepcalm.lumios.telegram.campus;

/**
 * Abstraction over the eCampus subscription API.
 *
 * Implementations must:
 * - Never log or store the supplied credentials.
 * - Throw {@link CampusAuthException} for authentication failures (wrong credentials, account locked, etc.).
 * - Throw {@link CampusAuthException} for network or server-side failures so callers can surface a meaningful error.
 */
public interface CampusApiClient {

    /**
     * Authenticates the user against campus and subscribes them to grade notifications.
     *
     * @param username   eCampus login
     * @param password   eCampus password (handled in memory only, never persisted)
     * @param webhookUrl the URL campus should POST grade events to
     * @param externalId opaque identifier included in each campus callback (use Telegram user ID)
     * @return {@link CampusSubscriptionResult} containing the access token required for unsubscription
     * @throws CampusAuthException if authentication fails or the subscription cannot be created
     */
    CampusSubscriptionResult subscribe(String username, String password, String webhookUrl, String externalId) throws CampusAuthException;

    /**
     * Revokes a previously created subscription.
     *
     * @param accessToken the Bearer token returned during subscription
     * @throws CampusAuthException if the revocation request fails
     */
    void unsubscribe(String accessToken) throws CampusAuthException;

}
