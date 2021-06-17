import ballerina/http;
import ballerina/log;
import ballerina/task;
task:TimerConfiguration timerConfiguration = {
intervalInMillis: 1000,
initialDelayInMillis: 0
};
listener task:Listener timer = new(timerConfiguration);
http:Client antecedente1 = new ("http://192.168.0.37:8485/pi/sensors/temperature");
http:Client consecuente1 = new ("http://192.168.0.37:8487/pi/actuators/leds/1");

public function main() returns error? {
log:printInfo("INICIO DEL SERVICIO");
}

service timerService on timer {
    resource function onTrigger() {
		var res1 = antecedente1->get("");

		if(res1 is http:Response) {
			json jsonpayload1 = checkpanic res1.getJsonPayload();
			var condition1 = <float>jsonpayload1.value<25;
			if(condition1){
				var response1 = consecuente1->put("",{"value":true});
			}
		}
	}
}
