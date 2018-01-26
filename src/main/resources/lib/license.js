/**
 * Functions for license generation and validation.
 */

/**
 * Generate a public/private key pair to be used for license generation and validation.
 *
 * @returns {Object} Returns an object with a new key pair.
 */
exports.generateKeyPair = function () {
    var bean = __.newBean('com.enonic.lib.license.js.GenerateKeyPair');
    var keyPairObj = bean.generate();
    return __.toNativeObject(keyPairObj);
};

/**
 * Generates a license based on a private-key and license details.

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

function checkRequired(params, name) {
    if (params[name] === undefined) {
        throw "Parameter '" + name + "' is required";
    }
}