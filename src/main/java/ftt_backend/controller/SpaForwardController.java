/**
 * React 라우팅에서 주소창 직접 쳤을때 404 안 나오기 위한 컨트롤러
* */
package ftt_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {
    // 점(.)이 없는 모든 경로에 대해 index.html로 포워딩
    @RequestMapping(value = {"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
