declare module "/lib/license" {
    export interface KeyPair {
        /**
         * Base64-encoded string of the private key.
         */
        privateKey: string;

        /**
         * Base64-encoded string of the public key.
         */
        publicKey: string;

        /**
         * Serialized string form of the key pair, suitable for passing to `loadKeyPair`.
         */
        toString(): string;
    }

    export interface SerializedKeyPair {
        privateKey: string;
        publicKey: string;
        string: string;
    }

    export interface LicenseParams {
        /**
         * The entity that issued this license.
         */
        issuedBy: string;

        /**
         * The entity this license is issued to.
         */
        issuedTo: string;

        /**
         * Time when the license was issued. ISO-8601 string or Date.
         */
        issueTime?: string | Date;

        /**
         * Expiration time for the license. ISO-8601 string or Date.
         */
        expiryTime?: string | Date;

        /**
         * Custom key-value properties. Values are coerced to strings.
         */
        properties?: Record<string, unknown>;
    }

    export interface ValidateLicenseParams {
        /**
         * Encoded license string. If omitted, the library looks in `XP_HOME/license/<appKey>.lic`,
         * falling back to a license installed in the repository via `installLicense`.
         */
        license?: string | null;

        /**
         * Public key used to validate the license. If omitted, the library reads
         * `src/main/resources/app.pub` from the current app.
         */
        publicKey?: string | null;

        /**
         * Application key. If omitted, the current application key is used.
         */
        appKey?: string | null;
    }

    export interface InstallLicenseParams {
        /**
         * Encoded license string.
         */
        license: string;

        /**
         * Public key used to validate the license. If omitted, the library reads
         * `src/main/resources/app.pub` from the current app.
         */
        publicKey?: string | null;

        /**
         * Application key.
         */
        appKey: string;
    }

    export interface LicenseDetails {
        /**
         * The entity this license is issued to.
         */
        issuedTo: string;

        /**
         * The entity that issued this license.
         */
        issuedBy: string;

        /**
         * ISO-8601 string of the issue time, if set.
         */
        issueTime?: string;

        /**
         * ISO-8601 string of the expiry time, if set.
         */
        expiryTime?: string;

        /**
         * Whether the license has expired.
         */
        expired: boolean;

        /**
         * Custom key-value properties supplied when the license was generated.
         */
        data: Record<string, string>;
    }

    /**
     * Generates a public/private key pair to be used for license generation and validation.
     */
    export function generateKeyPair(): KeyPair;

    /**
     * Loads a key pair from its serialized string form.
     *
     * @param keyPairString Serialized key pair.
     * @returns The key pair, or `null` if the input is not valid.
     */
    export function loadKeyPair(keyPairString: string): SerializedKeyPair | null;

    /**
     * Generates a license based on a private key and license details.
     *
     * @param privateKey Private key string.
     * @param license License details.
     * @returns The encoded license string.
     */
    export function generateLicense(privateKey: string, license: LicenseParams): string;

    /**
     * Validates a license using the public key and returns the license details.
     *
     * @param params Validation parameters. All fields are optional; see `ValidateLicenseParams`.
     * @returns The license details, or `null` if the license is not valid.
     */
    export function validateLicense(params?: ValidateLicenseParams): LicenseDetails | null;

    /**
     * Validates and stores a license in the XP node repository.
     *
     * @param params Installation parameters.
     * @returns `true` if the license was successfully installed, `false` otherwise.
     */
    export function installLicense(params: InstallLicenseParams): boolean;

    /**
     * Removes an installed license from the XP repository.
     *
     * @param appKey Application key.
     */
    export function uninstallLicense(appKey: string): void;
}

export {};
