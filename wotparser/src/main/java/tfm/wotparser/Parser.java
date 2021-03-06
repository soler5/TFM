package tfm.wotparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.springframework.web.multipart.MultipartFile;

public class Parser {

	public static String getImport(String lib) {
		return "import " + lib + ";\n";
	}
	
	public static String getTimerConfiguration(int time, int caso) {
		if(caso==5) {
			return "task:Scheduler timer = new({\n"
					+ "intervalInMillis: "+time+",\n"
					+ "initialDelayInMillis: 0\n"
					+ "});\n"
					+ "var attachResult = timer.attach(service1);\n"
					+ "var startResult = timer.start();\n";

		}else {
		return "task:TimerConfiguration timerConfiguration = {\n"
				+ "intervalInMillis: "+time+",\n"
				+ "initialDelayInMillis: 0\n"
				+ "};\n"
				+ "listener task:Listener timer = new(timerConfiguration);\n";
		}
	}
	
	public static String newEndpoint(String name, String host) {
		return "http:Client "+name+" = new (\""+host+"\");\n";
	}
	
	public static String newsEndpoints(String[] endpoints, String base) {
		String salida ="";
		int j = 1;
		for (String s: endpoints) {  
			String[] d = s.split("\\s+");
			String c_host = d[1];
			String d_type = d[0];
			if(!d_type.equals("mqtt"))
				salida += Parser.newEndpoint(base+j, c_host);
		    j++;
		}

		return salida;
	}
		
	public static String getNewsBooleans(String[] endpoints, String base) {
		String salida ="";
		int i = 1;
		for (String s: endpoints) {  
			salida += Parser.getNewBoolean("fired"+i);
		    i++;
		}

		return salida;
	}
	
	public static String getNewBoolean(String name) {
		return "boolean "+name+" = false;\n";
	}
	
	//tipo 0 para get, 1 para mqtt
	public static String getMain(String[] antecedentes, int tipo) {
		
		String salida = "";
		salida += "\npublic function main() returns error? {\n";

		salida += "log:printInfo(\"INICIO DEL SERVICIO\");\n";
		
		if(tipo==0) {
			int i=1;
			for (String s: antecedentes) {  
				String[] d = s.split("\\s+");
				String d_host = d[1];
				String d_topic = d[2];
				salida += "\tmqtt:Client c"+i+" = check new(\"tcp://"+d_host+"\", \"\");\n"
						+ "    check c"+i+"->connect();\n"
						+ "    Callback cb"+i+" = new;\n"
						+ "    check c"+i+"->subscribe(\""+d_topic+"\", cb"+i+");\n\n";
				i++;
			}
			salida+= "\twhile(true){\n"
					+ "        runtime:sleep(1000);\n"
					+ "    }\n"
					+ "    check c1->disconnect();\n"
					+ "}\n\n";
		}else {
			salida += "}\n\n";
		}

		
		return salida;
	}
	
	public static String getCallback(String[] antecedentes, String[] consecuentes, String condicion, int caso) {
		String salida = "";
		int lastNumberCondition = antecedentes.length;
		ArrayList<String> condicionesAll = new ArrayList<String>();

		salida += "public type Callback object {\n"
				+ "    public function onMessage(mqtt:Message m)  {\n"
				+ "        var payload = checkpanic str:fromBytes(m.payload);\n"
				+ "        json jsonpayload = checkpanic payload.fromJsonString();\n";
		
		int i=1;
		
		if(antecedentes.length>1 || caso==5) { //Varios MQTT
			for (String s: antecedentes) {  
				String[] d = s.split("\\s+");
				String d_topic = d[2];
				String d_condition = d[3];
				condicionesAll.add(d_condition);
				salida += "\t\tif(m.topic == \""+d_topic+"\"){\n"
						+ "            if(jsonpayload."+d_condition+"){\n"
						+ "                fired"+i+" = true;\n"
						+ "            }else{\n"
						+ "                fired"+i+" = false;\n"
						+ "            }\n"
						+ "        }\n";
				i++;
			}
		
			
			String condicion_bal = condicion;

			if(caso!=5) {
				for(int k = 1; k <= lastNumberCondition;k++) {
					condicion_bal = condicion_bal.replace(""+k+"", "fired"+k);
				}
			}
			salida += ("\n\t\tif("+condicion_bal+"){\n");

		}else { //Un solo MQTT
    		String[] datos = antecedentes[0].split("\\s+");
    		String condition = datos[3];
    		salida += ("\t\t"+"if(jsonpayload."+condition+"){\r\n");
		}
		
		//SALIDAS
		i=1;
		for (String s: consecuentes) {  
			String[] d = s.split("\\s+");
			String d_method = d[0];
			String d_payload = d[2];
			if(d_method.equals("post"))
				salida += ("\t\t\tvar response"+i+" = consecuente"+i+"->put(\"\","+d_payload+");\n");
			i++;
		}
		salida += ("\t\t}\n"
				+ "\t}\n"
				+ "};\n");
		
		return salida;
	}
	
	public static String newService(String[] antecedentes, String[] consecuentes, String condicion, int lastNumberCondition, int caso) {
		    
		ArrayList<String> condicionesAll = new ArrayList<String>();

		String salida = "";

		if(caso==5) {
			salida += "service service1 = service {\n";
		}else {
			salida += "service timerService on timer {\n";
		}
		salida += "\tresource function onTrigger() {\n";
		
		int i=1;
		for (String s: antecedentes) {  
			String[] d = s.split("\\s+");
			String d_condition = d[2];
			String d_type = d[0];
			condicionesAll.add(d_condition);
			if(d_type.equals("get")) {
				salida += "\t\tvar res"+i+" = antecedente"+i+"->get(\"\");\n";
			}
			i++;

		}
			salida += "\n\t\tif(";
			
			
  		if(caso==5) {
  	  		i=1;
    		boolean escribo = false;
      		for (String s: antecedentes) {  
    			String[] d = s.split("\\s+");
    			String d_type = d[0];
    			if(d_type.equals("get")) {
	    			if(escribo==false) {
	    				salida +=("res"+i+" is http:Response");
		    			escribo=true;
	    			}else
	    				salida +=(" && res"+i+" is http:Response");
    			}
    			i++;
			}
  		}else {
  	  		for (int j=1; j<=antecedentes.length;j++) {  
  				if(j==1)
  	    			salida += "res"+j+" is http:Response";
  				else
  	    			salida += " && res"+j+" is http:Response";
  			}
  		}

		salida += ") {\n";
  		i=1;
  		
  		if(caso==5) {
    		for (String s: antecedentes) {  
    			String[] d = s.split("\\s+");
    			String d_type = d[0];

    			if(d_type.equals("get")) {
    				salida +=("\t\t\tjson jsonpayload"+i+" = checkpanic res"+i+".getJsonPayload();\n"+
	    						"\t\t\tvar condition"+i+" = <float>jsonpayload"+i+"."+condicionesAll.get(i-1)+";\n");
    				salida +=("            if(condition"+i+"){\r\n"
    						+ "                firedGet"+i+" = true;\r\n"
    						+ "            }else{\r\n"
    						+ "                firedGet"+i+" = false;\r\n"
    						+ "            }\r\n");
    			}
    			i++;
			}

  		}else {
  			for (int k=1; k<=antecedentes.length;k++) {  
  				salida += "\t\t\tjson jsonpayload"+k+" = checkpanic res"+k+".getJsonPayload();\n"+
						"\t\t\tvar condition"+k+" = <float>jsonpayload"+k+"."+condicionesAll.get(k-1)+";\n";
  			}
  		}
		

		String condicion_bal = (condicion!=null) ? condicion : "1";

  		if(caso!=5) {
			for(int l = 1; l <= lastNumberCondition;l++) {
				condicion_bal = condicion_bal.replace(""+l+"", "condition"+l);
			}
  		}
		salida += "\t\t\tif("+condicion_bal+"){\n";
		
		//SALIDAS
		i=1;
		for (String s: consecuentes) {  
			String[] d = s.split("\\s+");
			String d_method = d[0];
			String d_payload = d[2];
			if(d_method.equals("post"))
				salida += "\t\t\t\tvar response"+i+" = consecuente"+i+"->put(\"\","+d_payload+");\r\n";
			i++;
		}

		if(caso==5) {
			salida += "\t\t\t}\n"
					+ "\t\t}\n"
					+ "\t}\n"
					+ "};\n";
		}else {
			salida += "\t\t\t}\n"
					+ "\t\t}\n"
					+ "\t}\n"
					+ "}\n";
		}

		return salida;
	}
	
	public static void parseFile(MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		    String fileOutput ="output/salida.bal";
		    BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
		    BufferedWriter writer = new BufferedWriter(new FileWriter(fileOutput));
		    String antecedentes = reader.readLine();
    		String[] antecedentesAll = antecedentes.split(" , ");
    		
    		//Contamos l??neas
            BufferedReader readerLines = new BufferedReader(new InputStreamReader(file.getInputStream()));;
    		long lines = 0;
            while (readerLines.readLine() != null) lines++;
            readerLines.close();
            
    		String condicion = null;
    		if(lines==3)
    			condicion = reader.readLine();

		    int lastNumberCondition = antecedentesAll.length;

		    String consecuentes = reader.readLine();
    		String[] consecuentesAll = consecuentes.split(" , ");

		    //Imports iniciales
    		writer.write(Parser.getImport("ballerina/http"));
    		writer.write(Parser.getImport("ballerina/log"));
    		
		    //Tratamiento de los antecedentes
		    if(antecedentes.contains(",")) { //M??s de un antecedente
		    	if(!antecedentes.contains("mqtt")) { //Solo get
		    		
		    		writer.write(Parser.getImport("ballerina/task"));
		    		writer.write(Parser.newsEndpoints(antecedentesAll, "antecedente"));
		    		writer.write(Parser.newsEndpoints(consecuentesAll, "consecuente"));
		      		writer.write(Parser.getTimerConfiguration(Integer.parseInt(antecedentes.split(" ")[3]),3));
		      		writer.write(Parser.getMain(null, 1));
		    		writer.write(Parser.newService(antecedentesAll, consecuentesAll, condicion, lastNumberCondition,3));

		    	} else if (!antecedentes.contains("get")){ //Varios MQTT
		    		
		    		writer.write(Parser.getImport("pzfreo/mqtt"));
		    		writer.write(Parser.getImport("ballerina/runtime"));
		    		writer.write(Parser.getImport("ballerina/lang.'string as str"));


		    		writer.write(Parser.newsEndpoints(consecuentesAll, "consecuente"));

		    		
		    		writer.write(Parser.getNewsBooleans(antecedentesAll, "fired"));


			       	writer.write(Parser.getMain(antecedentesAll, 0));		    		

			    	writer.write(Parser.getCallback(antecedentesAll, consecuentesAll, condicion, 4));

		    		
		    	}else { //mqtt y get		
		    			 
		    		writer.write(Parser.getImport("pzfreo/mqtt"));
		    		writer.write(Parser.getImport("ballerina/runtime"));
		    		writer.write(Parser.getImport("ballerina/lang.'string as str"));
		    		writer.write(Parser.getImport("ballerina/task"));


					ArrayList<String> antecedentesMqtt = new ArrayList<String>();
					ArrayList<String> antecedentesGet = new ArrayList<String>();

		    		for (String s: antecedentesAll) {  
		    			if(s.contains("mqtt")) {
		    				antecedentesMqtt.add(s);
		    			}else {
		    				antecedentesGet.add(s);
		    			}
		    		}
		
		    		writer.write(Parser.newsEndpoints(antecedentesAll, "antecedente"));

		    		int i=1;

			    	writer.write(Parser.newsEndpoints(consecuentesAll, "consecuente"));


		       	    i=1;
		    		for (String s: antecedentesAll) {
		    			if(s.contains("mqtt")) 
				    		writer.write(Parser.getNewBoolean("fired"+i));
		    			else 
				    		writer.write(Parser.getNewBoolean("firedGet"+i));
		      			i++;
					}
		    		
		    		
		    		writer.write(Parser.getTimerConfiguration(Integer.parseInt(antecedentesGet.get(0).split(" ")[3]),5));

			       	writer.write(Parser.getMain(antecedentesMqtt.toArray(new String[0]), 0));		    		

		    		
		    		String condicion_bal_mqtt_get = condicion;

		    		for(int k = 1; k <= antecedentesAll.length;k++) {
		    			System.out.println(antecedentesAll[k-1]);
		    			if(antecedentesAll[k-1].contains("mqtt")) {
			    			condicion_bal_mqtt_get = condicion_bal_mqtt_get.replace(""+k+"", "fired"+k);
		    			}else {
			    			condicion_bal_mqtt_get = condicion_bal_mqtt_get.replace(""+k+"", "firedGet"+k);
		    			}
		    		}
		    		
			    	writer.write(Parser.getCallback(antecedentesMqtt.toArray(new String[0]), consecuentesAll, condicion_bal_mqtt_get, 5));

		    		writer.write(Parser.newService(antecedentesAll, consecuentesAll, condicion_bal_mqtt_get, lastNumberCondition, 5));


		    	}
		    }else{ //Un solo antecedente
		    	if(antecedentes.contains("mqtt")) {// antecedente MQTT
		    		
		    		writer.write(Parser.getImport("pzfreo/mqtt"));
		    		writer.write(Parser.getImport("ballerina/runtime"));
		    		writer.write(Parser.getImport("ballerina/lang.'string as str"));		    		
		    				    		
			    	writer.write(Parser.newsEndpoints(consecuentesAll, "consecuente"));    		
		    		
			       	writer.write(Parser.getMain(antecedentesAll,0));		    		
		    					    	
			    	writer.write(Parser.getCallback(antecedentesAll, consecuentesAll, condicion, 2));
			    	
		    	}else if (antecedentes.contains("get")) { // antecedente GET
		    		
		    		writer.write(Parser.getImport("ballerina/task"));
		    		writer.write(Parser.getTimerConfiguration(Integer.parseInt(antecedentes.split(" ")[3]),1));
		    		
		    		writer.write(Parser.newsEndpoints(antecedentesAll, "antecedente"));
		    		
			    	writer.write(Parser.newsEndpoints(consecuentesAll, "consecuente"));
			    	
		      		writer.write(Parser.getMain(null, 1));

		    		writer.write(Parser.newService(antecedentesAll, consecuentesAll, condicion, lastNumberCondition, 1));
		    		   		
		 	   	}
		    }
		    	
		    writer.close();
		    reader.close();
		
	}
}
