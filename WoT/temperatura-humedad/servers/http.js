// Final version
var express = require('express'),
  sensorRoutes = require('./../routes/sensors'),
  converter = require('./../middleware/converter'),
  cors = require('cors'),
  bodyParser = require('body-parser');
  var td = require('./../resources/td.json');

var app = express();

app.use(bodyParser.json());

app.use(cors());

app.use('/sensors', sensorRoutes);

app.get('/', function (req, res) {
  req.result = td; //#A
});

// For representation design
app.use(converter());
module.exports = app;