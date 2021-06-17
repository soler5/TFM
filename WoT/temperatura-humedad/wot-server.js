// Final version
var httpServer = require('./servers/http'),
  resources = require('./resources/model');

// Internal Plugins
var dhtPlugin = require('./plugins/internal/DHT22SensorPlugin'); //#A

// Internal Plugins for sensors/actuators connected to the PI GPIOs
// If you test this with real sensors do not forget to set simulate to 'false'
dhtPlugin.start({'simulate': false, 'frequency': 10000}); //#B
// External Plugins
//var coapPlugin = require('./plugins/external/coapPlugin');
//coapPlugin.start({'simulate': false, 'frequency': 10000});

// HTTP Server
var server = httpServer.listen(resources.pi.port, function () {
  console.log('HTTP server started...');

  // Websockets server
  //wsServer.listen(server);

  console.info('Your WoT Pi is up and running on port %s', resources.pi.port);
});
//#A Require all the sensor plugins you need
//#B Start them with a parameter object; here you start them on a laptop so you activate the simulation function

