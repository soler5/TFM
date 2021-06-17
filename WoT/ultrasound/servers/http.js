// Final version
var express = require('express'),
  sensorRoutes = require('./../routes/sensors'),
  converter = require('./../middleware/converter'),
  cors = require('cors');

var app = express();

app.use(cors());

app.use('/sensors', sensorRoutes);

// For representation design
app.use(converter());
module.exports = app;

