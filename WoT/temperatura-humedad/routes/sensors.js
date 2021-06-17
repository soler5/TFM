// Final version
var express = require('express'),
  router = express.Router(),
  resources = require('./../resources/model');
  var td = require('./../resources/td.json');

router.route('/').get(function (req, res, next) {
  req.result = td; //#A
  next(); //#B
});

router.route('/temperature').get(function (req, res, next) {
  req.result = resources.pi.sensors.temperature;
  next();
});

router.route('/humidity').get(function (req, res, next) {
  req.result = resources.pi.sensors.humidity;
  next();
});

module.exports = router;