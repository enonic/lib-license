package com.enonic.lib.license;

/**
 * License Manager for Enonic XP applications.
 * This class contains methods to generate and validate licenses using public key cryptography.
 *
 * @see LicenseDetails
 */
public interface LicenseManager
{
    /**
     * Generates a pair of keys: one private and one public.
     * <p>The private key is used to generate licenses.
     * <p>The public key is used to validate licenses.
     *
     * @return a new key pair.
     */
    KeyPair generateKeyPair();

    /**
     * Generates a new license using a private key.
     *
     * @param privateKey Private key to generate the license.
     * @param license    License details to include in the license.
     * @return The generated encoded license string.
     */
    String generateLicense( PrivateKey privateKey, LicenseDetails license );

    /**
     * Validates and decodes a license string using the public key.
     *
     * @param publicKey Public key to validate the license.
     * @param license   String containing the encoded license.
     * @return Object containing the license details. Or {@code null} if the license was not validated.
     */
    LicenseDetails validateLicense( PublicKey publicKey, String license );

    /**
     * Validates and decodes an installed license using the public key included in the current app.
     * <p>The public key file should be located in the current app resources with path "/app.pub".
     * <p>The license string will be looked up in the license directory and if it is installed in the node repository.
     * <p>The path for licenses in the filesystem is 'XP_HOME/license/[appKey].lic'
     * <p>The node repository for installed licenses is 'com.enonic.licensemanager'. Licenses will be stored in a node with path '/installed-licenses/[appKey]'
     *
     * @param appKey Application key used to locate the license.
     * @return Object containing the license details. Or {@code null} if the license was not validated.
     */
    LicenseDetails validateLicense( String appKey );

    /**
     * Stores a license in the Enonic node repository.
     * <p>Before installing it, the license is validated using the public key.
     * <p>The id of the node repository 'com.enonic.licensemanager'.
     * <p>If the node repository does not exist, it will be created.
     * <p>If the current user in context is not authenticated the installation will fail and the method will return false.
     *
     * @param license   String containing the encoded license.
     * @param publicKey Public key to validate the license.
     * @param appKey    Application key used to store the license.
     * @return True if the license was successfully installed, false otherwise.
     */
    boolean installLicense( String license, PublicKey publicKey, String appKey );

    /**
     * Removes a license from the Enonic node repository.
     * <p>The id of the node repository 'com.enonic.licensemanager'.
     * <p>If the current user in context is not authenticated the removal will fail and the method will return false.
     *
     * @param appKey Application key used to store the license.
     * @return True if the license was successfully uninstalled, false otherwise.
     */
    boolean uninstallLicense( String appKey );
}
