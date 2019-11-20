var Util = {};

(function() {

    var system = Java.type('java.lang.System');
    var uuid = Java.type('java.util.UUID');

    this.tmpdir = function() {
        return system.getProperty("java.io.tmpdir");
    };

    this.uuid = function() {
        return uuid.randomUUID().toString();
    };

}).call(Util);