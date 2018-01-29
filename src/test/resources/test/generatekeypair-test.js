var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateKeyPair = function () {

    var keyPair = licenseLib.generateKeyPair();

    testing.assertNotNull(keyPair.privateKey);
    testing.assertNotNull(keyPair.publicKey);
    testing.assertNotNull(keyPair.toString());

};

exports.testLoadKeyPair = function () {

    var keyPair1 = licenseLib.generateKeyPair();
    var keyPair2 = licenseLib.loadKeyPair(keyPair1.toString());

    testing.assertEquals(keyPair1.privateKey, keyPair2.privateKey);
    testing.assertEquals(keyPair1.publicKey, keyPair2.publicKey);

};