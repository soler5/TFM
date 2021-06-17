// Final version
var express = require('express'),
  router = express.Router(),
  resources = require('./../resources/model');
  var td = require('./../resources/td.json');

router.route('/').get(function (req, res, next) {
  req.result = td; 
  next(); 
});

router.route('/distance').get(function (req, res, next) {
  req.result = resources.pi.sensors.hc;
  next();
});
module.exports = router;

//#A Assign the results to a new property of the req object that you pass along from middleware to middleware
//#B Call the next middleware; the framework will ensure the next middleware gets access to req (including the req.result) and res
