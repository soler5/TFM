// Final version
var express = require('express'),
  router = express.Router(),
  resources = require('./../resources/model');
  var td = require('./../resources/td.json');

router.route('/').get(function (req, res, next) {
  req.result = td; //#A
  next(); //#B
});

router.route('/pir').get(function (req, res, next) {
  req.result = resources.pi.sensors.pir;
  next();
});

module.exports = router;