var mqtt = require('mqtt');
var resources = require('./../../resources/model');
var client = mqtt.connect("mqtt://192.168.0.37:1883");
var interval, sensor;
var model = resources.pi.sensors;
var pluginName = 'US sensor';
var localParams = {'simulate': false, 'frequency': 5000};

exports.start = function (params) {
  localParams = params;
  client.on('connect', function () {
    connectHardware();
  });
};

exports.stop = function () {
  if (params.simulate) {
    clearInterval(interval);
  } else {
    sensor.unexport();
  }
  console.info('%s plugin stopped!', pluginName);
};

function connectHardware() {

  const Gpio = require('pigpio').Gpio;
  const MICROSECONDS_PER_CM = 1e6/34321;

  const trigger = new Gpio(23, {mode: Gpio.OUTPUT});
  const ECHO = new Gpio(24, {mode: Gpio.INPUT, alert: true});

  trigger.digitalWrite(0);

  const warchHCSR04 = () =>{
    var starTick;
    ECHO.on('alert', (level, tick)=>{
      if (level==1){
        starTick = tick;
      }else{
        const endTick = tick;
        const diff = (endTick>>0) - (starTick>>0);
        model.hc.value = diff/2/MICROSECONDS_PER_CM;
        var date = new Date();

        if(model.hc.value<50){
          client.publish('short-distance', '{"value":1,"date":"'+ date.getDay()+'/'+date.getMonth()+'/'+date.getFullYear()+' '+date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()+'"}');

        }else{
          client.publish('short-distance', '{"value":0,"date":"'+ date.getDay()+'/'+date.getMonth()+'/'+date.getFullYear()+' '+date.getHours()+':'+date.getMinutes()+':'+date.getSeconds()+'"}');
        }
      }
    })
  }
  warchHCSR04();

  setInterval(()=>{
    trigger.trigger(10, 1);
  }, 1000);
};

