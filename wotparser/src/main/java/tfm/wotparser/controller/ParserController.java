package tfm.wotparser.controller;

import tfm.wotparser.Parser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.MediaType;

@Controller
public class ParserController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/parser")
	public String parser(@RequestParam("file") MultipartFile file) throws IOException {
		
		if (file.isEmpty()) {
	        return "error";
        } else {
        	Parser.parseFile(file);
            return "ok";
        }
	}
    
    @GetMapping("/run")
    public String runScript(){
    	final String cmd = "cmd /c start C:\\Users\\Juanfran\\Desktop\\TFM\\parser\\wotparser.zip_expanded\\wotparser\\output\\exec.bat";
    	try {
    	    Process process = Runtime.getRuntime().exec(cmd);
            return "run";

    	} catch (Exception e) {
    	    e.printStackTrace(System.err);
	        return "error";
    	}
    }
    
    @GetMapping("/stop")
    public String stop() {
    	try {
        	Process prClose = Runtime.getRuntime().exec("taskkill /im cmd.exe");
            return "stop";

    	} catch (Exception e) {
    	    e.printStackTrace(System.err);
	        return "error";
    	}
    }
    
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download() throws FileNotFoundException {
    	
      String fileName = "salida.bal";
      File file_output = new File("output/" + fileName);
      InputStreamResource resource = new InputStreamResource(new FileInputStream(file_output));

      return ResponseEntity.ok()
              // Content-Disposition
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file_output.getName())
              // Content-Type
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              // Contet-Length
              .contentLength(file_output.length()) //
              .body(resource);
    }	
}
