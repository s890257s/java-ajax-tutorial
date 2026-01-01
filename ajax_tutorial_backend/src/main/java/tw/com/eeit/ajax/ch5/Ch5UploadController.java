package tw.com.eeit.ajax.ch5;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ch5")
public class Ch5UploadController {

    @PostMapping("/upload")
    public String upload(@RequestParam("myFile") MultipartFile file, 
                         @RequestParam("description") String description) throws IOException {
        
        System.out.println("收到檔案: " + file.getOriginalFilename());
        System.out.println("檔案大小: " + file.getSize());
        System.out.println("描述: " + description);
        
        // 實際專案會 save 到某個資料夾，這裡僅做演示
        // file.transferTo(new File("C:/uploads/" + file.getOriginalFilename()));

        return "上傳成功: " + file.getOriginalFilename();
    }
}
