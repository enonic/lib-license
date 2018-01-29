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
    bean.privateKey = privateKey;

    return __.toNativeObject(bean.generate());
};

/**
 * Validates a license using the public-key, and returns the license details.
 *
 * @param {string} license Encoded license string.
 * @param {string} [publicKey] Public key.
 * @returns {object|null} License details object, or null if the license is not valid.
 */
exports.validateLicense = function (license, publicKey) {
    if (license === undefined) {
        throw "Parameter 'license' is required";
    }
    if (publicKey === undefined) {
        throw "Parameter 'publicKey' is required";
    }

    var bean = __.newBean('com.enonic.lib.license.js.ValidateLicense');
    bean.license = license;
    bean.publicKey = publicKey;

    return __.toNativeObject(bean.validate());
};

function checkRequired(params, name) {
    if (params[name] === undefined) {
        throw "Parameter '" + name + "' is required";
    }
}