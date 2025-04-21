/**
 * React 라우팅에서 주소창 직접 쳤을때 404 안 나오기 위한 컨트롤러
* */
package ftt_backend.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class SpaForwardController {

    /**
     * 1) GET 요청만 처리
     * 2) 경로에 '.' 가 없는 순수 HTML 히스토리 네비게이션
     * 3) api, ws, static, uploads 로 시작하지 않는 요청만 index.html 로 포워드
     */
    @RequestMapping(
            value = {
                    // 첫 세그먼트가 api|ws|static|uploads 가 아니어야 함 (ws는 제외해야 함)
                    "/{path:^(?!api|ws|static|uploads)[^\\.]*}",
                    "/**/{path:^(?!api|ws|static|uploads)[^\\.]*}"
            },
            method = RequestMethod.GET
    )
    public String forward() {
        return "forward:/index.html";
    }
}