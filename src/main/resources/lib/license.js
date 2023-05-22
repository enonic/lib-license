/**
 * Functions for license generation and validation.
 */

function KeyPair(keyPair) {
    this.privateKey = keyPair.privateKey;
    this.publicKey = keyPair.publicKey;
    this._string = keyPair.string;
}

KeyPair.prototype.toString = function () {
    return this._string;
};

/**
 * Generate a public/private key pair to be used for license generation and validation.
 *
 * @returns {Object} Returns an object with a new key pair.
 */
exports.generateKeyPair = function () {
    var bean = __.newBean('com.enonic.lib.license.js.GenerateKeyPair');
    var keyPairObj = bean.generate();
    var keyPair = __.toNativeObject(keyPairObj);
    return new KeyPair(keyPair);
};

/**
 * Load a key pair from a string and returns it as an object.
 *
 * @param {string} keyPairString Key key.
 * @returns {Object|null} Returns an object with a new key pair, or null if the input is not valid.
 */
exports.loadKeyPair = function (keyPairString) {
    var bean = __.newBean('com.enonic.lib.license.js.GenerateKeyPair');
    var keyPairObj = bean.load(keyPairString);
    return __.toNativeObject(keyPairObj);
};

/**
 * Generates a license based on a private-key and license details.
 *
 * @param {string} privateKey Private key.
 * @param {object} license JSON with the parameters.
 * @param {string} license.issuedBy The entity that issued this license.
 * @param {string} license.issuedTo The entity this license is issued to.
 * @param {string|Date} [license.issueTime] Time when the license was issued.
 * @param {string|Date} [license.expiryTime] Expiration time for the license.
 * @param {object} [license.properties] Custom key-value properties.
 * @returns {string} License string.
 */
exports.generateLicense = function (privateKey, license) {
    if (privateKey === undefined) {
        throw "Parameter 'privateKey' is required";
    }
    checkRequired(license, 'issuedBy');
    checkRequired(license, 'issuedTo');

    var bean = __.newBean('com.enonic.lib.license.js.GenerateLicense');
    bean.setIssuedBy(license.issuedBy);
    bean.setIssuedTo(license.issuedTo);
    bean.setIssueTime(safeToIsoString(license.issueTime));
    bean.setExpiryTime(safeToIsoString(license.expiryTime));
    bean.setProperties(license.properties || {});
    bean.setPrivateKey(privateKey);

    return __.toNativeObject(bean.generate());
};

/**
 * Validates a license using the public-key, and returns the license details.
 *
 * @param {object} params JSON with the parameters.
 * @param {string} [params.license] Encoded license string. Optional, if not set it will look for it in 'XP_HOME/license/<appKey>.lic'. Otherwise it will check it is installed in the repository.
 * @param {string} [params.publicKey] Public key. Optional, if not set it will look for it in the current app.
 * @param {string} [params.appKey] Application key. If not set it will use the current application key.
 *
 * @returns {object|null} License details object, or null if the license is not valid.
 */
exports.validateLicense = function (params) {
    params = params || {};
    var bean = __.newBean('com.enonic.lib.license.js.ValidateLicense');
    bean.setLicense(__.nullOrValue(params.license));
    bean.setPublicKey(__.nullOrValue(params.publicKey));
    bean.setApp(__.nullOrValue(params.appKey || app.name));
    if (params.publicKey == null) {
        bean.setPublicKeyResource(__.nullOrValue(resolve('/app.pub')));
    }

    return __.toNativeObject(bean.validate());
};

/**
 * Validates and stores a license in a XP node repo.
 *
 * @param {object} params JSON with the parameters.
 * @param {string} params.license Encoded license string.
 * @param {string} [params.publicKey] Public key to validate the license. Optional, if not set it will look for it in the current app.
 * @param {string} params.appKey Application key.
 *
 * @returns {boolean} True if the license was successfully installed, false otherwise.
 */
exports.installLicense = function (params) {
    checkRequired(params, 'license');
    checkRequired(params, 'appKey');

    var bean = __.newBean('com.enonic.lib.license.js.InstallLicense');
    bean.setLicense(__.nullOrValue(params.license));
    bean.setPublicKey(__.nullOrValue(params.publicKey));
    bean.setAppKey(__.nullOrValue(params.appKey));
    if (params.publicKey == null) {
        bean.setPublicKeyResource(__.nullOrValue(resolve('/app.pub')));
    }

    return __.toNativeObject(bean.install());
};

/**
 * Removes an installed license from XP repo.
 *
 * @param {string} appKey Application key.
 */
exports.uninstallLicense = function (appKey) {

    var bean = __.newBean('com.enonic.lib.license.js.InstallLicense');
    bean.setAppKey(__.nullOrValue(appKey));

    return __.toNativeObject(bean.uninstall());
};

function checkRequired(params, name) {
    if (params[name] === undefined) {
        throw "Parameter '" + name + "' is required";
    }
}

function safeToIsoString(value) {
    if (typeof value === 'object' && value !== null && 'toISOString' in value) {
        return value.toISOString();
    } else {
        return value;
    }
}
