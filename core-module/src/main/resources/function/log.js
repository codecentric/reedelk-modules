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