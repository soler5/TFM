var resources = require('./../../resources/model');
var mqtt = require('mqtt');
var interval, sensor;
var model = resources.pi.sensors.pir;
var pluginName = resources.pi.sensors.pir.name;
var localParams = {'simulate': false, 'frequency': 2000};

var client = mqtt.connect("mqtt://192.168.0.37:1883");

exports.start = function (params) { //#A
  localParams = params;

  client.on('connect', function () {
    connectHardware();
  });
};

exports.stop = function () { //#A
  if (localParams.simulate) {
    clearInterval(interval);
  } else {
    sensor.unexport();
  }
  console.info('%s plugin stopped!', pluginName);
};

function connectHardware() { //#B
  var Gpio = require('onoff').Gpio;
  sensor = new Gpio(model.gpio, 'in', 'both'); //#C
  sensor.watch(function (err, value) { //#D
    if (err) exit(err);
    model.value = !!value;
    showValue();
    //console.info(value);
    var date = new Date();
    if(model.value == true){
      client.publish('presence', '{"value":1,"date":"'+ date.getDay()+'/'+date.getMonth()+'/'+date.getFullYear()+' '+date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()+'"}');
    }else{
      client.publish('presence', '{"value":0,"date":"'+ date.getDay()+'/'+date.getMonth()+'/'+date.getFullYear()+' '+date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()+'"}');
    }
  });
  console.info('Hardware %s sensor started!', pluginName);
};

function showValue() {
  console.info(model.value ? 'there is someone!' : 'not anymore!');
};

//#A starts and stops the plugin, should be accessible from other Node.js files so we export them
//#B require and connect the actual hardware driver and configure it
//#C configure the GPIO pin to which the PIR sensor is connected
//#D start listening for GPIO events, the callback will be invoked on events
//#E allows the plugin to be in simulation mode. This is very useful when developing or when you want to test your code on a device with no sensors connected, such as your laptop.

/*
var resources = require('./../../resources/model');
//var mqtt = require('mqtt');
var amqp = require('amqplib/callback_api');
var interval, sensor;
var model = resources.pi.sensors.pir;
var pluginName = resources.pi.sensors.pir.name;
var localParams = {'simulate': false, 'frequency': 2000};

//var client = mqtt.connect("mqtt://192.168.0.37:1883");
var client = amqp.connect('amqp://192.168.0.37', function(error0, connection) {});
var amqp = require('amqplib/callback_api');

exports.start = function (params) { //#A
  localParams = params;

  amqp.connect('amqp://localhost', function(error0, connection) {
    if (error0) {
      throw error0;
    }
    connection.createChannel(function(error1, channel) {
      if (error1) {
        throw error1;
      }
      var queue = 'presence';
      var msg = '';

      var Gpio = require('onoff').Gpio;
      sensor = new Gpio(model.gpio, 'in', 'both'); //#C
      sensor.watch(function (err, value) { //#D
        if (err) exit(err);
        model.value = !!value;
        showValue();
        //console.info(value);

        
        var date = new Date();
        if(model.value == true){
          msg = '{"value":1,"date":"'+ date.getDay()+'/'+date.getMonth()+'/'+date.getFullYear()+' '+date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()+'"}';
        }else{
          msg = '{"value":0,"date":"'+ date.getDay()+'/'+date.getMonth()+'/'+date.getFullYear()+' '+date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()+'"}';
        }

        channel.assertQueue(queue, {
          durable: false
        });
  
        channel.sendToQueue(queue, new Buffer(msg)
        );
      });


      });


      console.info('Hardware %s sensor started!', pluginName);
  });
};

exports.stop = function () { //#A
  if (localParams.simulate) {
    clearInterval(interval);
  } else {
    sensor.unexport();
  }
  console.info('%s plugin stopped!', pluginName);
};

function showValue() {
  console.info(model.value ? 'there is someone!' : 'not anymore!');
};*/