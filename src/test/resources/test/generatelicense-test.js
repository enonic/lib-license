var testing = require('/lib/xp/testing.js');
var licenseLib = require('/lib/license');


exports.testGenerateKeyPair = function () {

    var privateKey = 'MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3rmp_CiJ6ToUazOI_U-znf2s5yc3t9DQmvQTwAxcu8HCo4qIzG8dCZ-ThriOpmsWWwYELqKq6y5cgYGu1i2-fBdDM3Q8a5iBz4v7Vq8C1kB1-lcUdWjfarNzKo2latVmzIygFyhLUZZkq2HiQDYncB_tKmhnMuwmXBhLhS0Og5TSlDnmIej5VF9xyZ0rDYSMaNZ3xIhnx6DYyypSMMAt99X9a-v6HNPLt2fD3eGDU1BW4zZmfHy9yuTTOc_8cp2i48SLhYdNn1YRm7huClW1Cvy5hnhKhtndwjDhtuppBTB2pLxElxP642iQHoJSsqmiXjNrh_H5NKa4oDUaJ7KIBAgMBAAECggEAUb8nGFj7VTGC3ZWXj5WbVsAHiZV9t8w6NY9kFilZ9QL_MLqEc3iGatBwNdxZdM8z5s9Bzl2HdlDLdpZS-V4QFFbzQWmUomUOxmSdA51Hy8ZSyhZ_vnt7ZjAOp4Soi70wKaCY3FK9pJd-3mmu5nWQCEPpG5-PoeUP9I24c1oaxAzajmloX3Op5RHeo-flqmZBqxmgM3kFgZzp6jWasfgyjBKe9uAefM3CfpwSxpFMqdefbYPamCS6o4C_8BxlnV3UgWDWhYlqPSkmdkBpGrLUJ-nFsxgTEKHTTVI-L-SVHKej9rCZ3YdYyBrHsPMoMLhIRMCMMS1ipXj_ww7Pohf5fQKBgQD_ejksgHGOhMUX4xGWXphNzeOfWQWVcXih4rouEQMeqosGIJO3Pasw2fc6XBLW5oeFHh0VIHexJIyAUydj-k9A9pzNo7thQfhIz9DGIKWRiOSFFhEfNLpWAjkABd33RviE0TdFXQ6FCC5V0wGjMPuHKL-AIu0KriqVdtUyx8PHdwKBgQC4DpkH5VaEv0WGms946vf3HyjqUX-F1socIutPMwv0OPb-YVrvTE867O_XI0y5XFoWgW98zBvcf6E-IhqfhlZZLki2ijIAudU7PpToqQ7TMmzNbm666bUmtqSj95jDgyLwwqb_nIA_gM4SpruHZWcg3TyNI0NF2ZzYHDuFPxYwRwKBgF5qMQ79OPpOnvhA9pL3ypmWaXTOVX3xPX-2Zs_3gYunw1E0YOLra4TWSMPMmznIYHUVt_HC2fkhZLtX_8q3CusbOL_Wrr02wCdIhgNytT2ftbRpf1JlDigTEjWr9WgZVmbfWunLhj4r09Pr80L4Kzy8Fmmofqnfy-UI0am-od_pAoGACafKY12mkkQuc4c_hwpcg7xAuzoXRIAmDh_O1FLLN76dRm75BECuj1rKojCZ38d2emPD43oZpBGV9dp4JxI3CWqiKeFKCju1cewwh5wGyRGGH6jBalDETgmazXc5rlf8x0CO3i3plGs_ZD8W611ocZLpkEXfS5TE45lnje_nTBUCgYEAgxxDJRGn0Gre-HrfSPQdkEaZxa8qMw51QLyQb6YfoEBjzD1wQnc2kJjEElhVt1jmYEt8-mGPfY7D9O-KwR3DT4SuT7QxnlQkHoZGu1g0R_2s3pt4vxNwBHMeiXWQjDoUCUSaiYOzsaAvASwdOvE3O5GJFOm1Hqjq_Ce7r29ofgk';

    var license = licenseLib.generateLicense(privateKey, {
        name: 'name',
        organization: 'org'
    });

    testing.assertNotNull(license);
    testing.assertEquals('-----BEGIN LICENSE-----\r\n' +
                         'eyJuYW1lIjoibmFtZSIsIm9yZ2FuaXphdGlvbiI6Im9yZyJ9.FKOMzc4WRcWbOFt\r\n' +
                         'KkpR5RYz9QndcJLtCpY4jf6p6nZMY3TqjXbr4MJNcObP7uncb01GMkY2V2Lt0Ayi\r\n' +
                         '0RqcCpMv7znn2C7SZjz0tbad7Ck1Yka4HuzkGCGGIuWNLRgUQCFxaOBtd+u+ityB\r\n' +
                         'ZNSCEkK+wqtGWlm+Gfw1rh6aga79YPI5qBBIMZLu7AHWJ5pigU6E5DBOX1bCPoYk\r\n' +
                         'zwMGnlLxb7xQMiryLodQebJj3WDAPxQztb8RQxxymMy0XJTt85t4n9/LTtAZQcde\r\n' +
                         'oiTlnTxxa7Wd3pI7R36Nsf6RmzrSWXRHXd0mjIpgEdK5UGTAo8nnwva16F6wQACB\r\n' +
                         '8oIxw/w\r\n' +
                         '-----END LICENSE-----', license);

};
