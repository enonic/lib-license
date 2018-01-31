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
 * @param {string} license.name Name.
 * @param {string} license.organization Organization.
 * @param {string|Date} [license.issueTime] Time when the license was issued.
 * @param {string|Date} [license.expiryTime] Expiration time for the license.
 * @param {object} [license.properties] Custom key-value properties.
 * @returns {object} License object.
 */
exports.generateLicense = function (privateKey, license) {
    if (privateKey === undefined) {
        throw "Parameter 'privateKey' is required";
    }
    checkRequired(license, 'name');

    var bean = __.newBean('com.enonic.lib.license.js.GenerateLicense');
    bean.name = license.name;
    bean.organization = license.organization;
    bean.issueTime = license.issueTime;
    bean.expiryTime = license.expiryTime;
    bean.properties = license.properties || {};
    bean.privateKey = privateKey;

    return __.toNativeObject(bean.generate());
};

/**
 * Validates a license using the public-key, and returns the license details.
 *
 * @param {string} [license] Encoded license string. Optional, if not set it will look for it in XP_HOME/license.
 * @param {string} [publicKey] Public key. Optional, if not set it will look for it in the current app.
 * @returns {object|null} License details object, or null if the license is not valid.
 */
exports.validateLicense = function (license, publicKey) {
    var bean = __.newBean('com.enonic.lib.license.js.ValidateLicense');
    bean.license = __.nullOrValue(license);
    bean.publicKey = __.nullOrValue(publicKey);
    if (license == null) {
        bean.app = __.nullOrValue(app.name);
    }
    if (publicKey == null) {
        bean.publicKeyResource = __.nullOrValue(resolve('/license.key'));
    }

    return __.toNativeObject(bean.validate());
};

function checkRequired(params, name) {
    if (params[name] === undefined) {
        throw "Parameter '" + name + "' is required";
    }
}