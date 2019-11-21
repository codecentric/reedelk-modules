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

var Log = {};

(function(logger) {

    this.info = function(message) {
        logger.info(message);
    };

    this.debug = function(message) {
        logger.debug(message);
    };

    this.warn = function(message) {
        logger.warn(message);
    };

    this.error = function(message) {
        logger.error(message);
    };

    this.trace = function(message) {
        logger.trace(message);
    };

}).call(Log, logger);