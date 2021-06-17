// Final version
var httpServer = require('./servers/http'),
resources = require('./resources/model');

// Internal Plugins
var hcPlugin = require('./plugins/internal/hcPlugin'); //#A

// Internal Plugins for sensors/actuators connected to the PI GPIOs
// If you test this with real sensors do not forget to set simulate to 'false'
hcPlugin.start({'simulate': false, 'frequency': 10000}); //#B

// HTTP Server
var server = httpServer.listen(resources.pi.port, function () {
  console.log('HTTP server started...');
  console.info('Your WoT Pi is up and running on port %s', resources.pi.port);
});

