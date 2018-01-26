var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateKeyPair = function () {

    var keyPair = licenseLib.generateKeyPair();

    testing.assertNotNull(keyPair.privateKey);
    testing.assertNotNull(keyPair.publicKey);
    testing.assertNotNull(keyPair.privateKeyBytes);
    testing.assertNotNull(keyPair.publicKeyBytes);

};
