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
    var keyPairObj = bean.generateKeyPair();
    return __.toNativeObject(keyPairObj);
};