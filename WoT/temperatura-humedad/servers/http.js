// Final version
var express = require('express'),
  sensorRoutes = require('./../routes/sensors'),
  thingsRoutes = require('./../routes/things'),
  converter = require('./../middleware/converter'),
  cors = require('cors'),
  bodyParser = require('body-parser');

var app = express();

app.use(bodyParser.json());

app.use(cors());

app.use('/pi/sensors', sensorRoutes);
app.use('/things', thingsRoutes);

app.get('/pi', function (req, res) {
  res.send('This is the WoT-Pi for DHT sensor!')
});

// For representation design
app.use(converter());
module.exports = app;