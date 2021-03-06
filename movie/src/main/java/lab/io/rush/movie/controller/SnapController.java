package lab.io.rush.movie.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lab.io.rush.movie.dto.MovieDTO;
import lab.io.rush.movie.dto.SnapMessageDto;
import lab.io.rush.movie.dto.response.ResponseMessage;
import lab.io.rush.movie.log.AutoLogger;
import lab.io.rush.movie.pojo.MovieTicket;
import lab.io.rush.movie.service.SnapService;

/**
 * 抢购相关web接口和页面
 * */
@Controller
public class SnapController {
	@AutoLogger
	private Logger logger;
	@Autowired
	@Qualifier("snapServiceImpl")
	private SnapService snapService;
	/**
     * 首页
     */
    @RequestMapping({"/", "/index", "/home"})
    public String index() {
        return "index";
    }

    /**
     * 获取电影票抢购列表
     * @return 电影票抢购列表
     */
    @RequestMapping("list")
    @ResponseBody
    public List<MovieTicket> list() {
        List<MovieTicket> MovieTickets = snapService.getAllMovieTicket();
        return MovieTickets;
    }

    /**
     * 电影票抢购详情页面
     *
     * @param movieId 电影票编号
     */
    @RequestMapping(value = "/movie/{movieId}")
    public String getMovieTicket(@PathVariable Integer movieId, Model model) {
        logger.debug("enter into getMovieTicket movieId=" + movieId);
        try {
            MovieDTO movieDTO = snapService.getMovieTicket(movieId);
            if (movieDTO.getMovieTicket() == null)
                throw new RuntimeException("电影票抢购信息不存在！");
            model.addAttribute("MovieTicket", movieDTO.getMovieTicket());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return "movie";
    }

    /**
     * 获取电影票抢购校验码
     *
     * @param movieId 电影票编号
     * @return json数据 如果抢购未开始，返回服务器时间。如果抢购开始，返回校验码
     */
    @ResponseBody
    @RequestMapping(value = "/movieMd5/{movieId}")
    public ResponseMessage getMovieTicketMd5(@PathVariable Integer movieId) {
        logger.debug("enter into getMovieTicketMd5 movieId=" + movieId);
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            MovieDTO MovieTicket = snapService.getMovieTicket(movieId);
            responseMessage.setData(MovieTicket);
        } catch (Exception e) {
            responseMessage.setStatus(false);
            responseMessage.setException(e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return responseMessage;
    }

    /**
     * 电影票抢购接口
     *
     * @param movieId 电影票编号
     * @param md5     校验码
     * @return json数据  包含抢购的状态信息和抢购记录
     */
    @ResponseBody
    @RequestMapping("/snap/{movieId}/{md5}")
    public ResponseMessage snapMovieTicket(@PathVariable Integer movieId, @PathVariable String md5,
                                          HttpSession session) {
        logger.debug("enter into snapMovieTicket movieId=" + movieId + " md5=" + md5);
        ResponseMessage responseMessage = new ResponseMessage();
        Object uid = session.getAttribute("uid");
        Object email = session.getAttribute("email");
        if (uid == null || email == null) {
            logger.debug("not login!");
            session.removeAttribute("uid");
            session.removeAttribute("email");

            responseMessage.setStatus(false);
            responseMessage.setErrorCode(1);
            responseMessage.setData("未登录，请重新登录...");
            return responseMessage;
        }
        try {
            SnapMessageDto snapMessageDto = snapService.snapMovie((Integer) uid, (String) email, movieId, md5);
            responseMessage.setData(snapMessageDto);
        } catch (Exception e) {
            responseMessage.setStatus(false);
            responseMessage.setException(e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return responseMessage;
    }

    @ResponseBody
    @RequestMapping(value = "/movieNum/{movieId}")
    public ResponseMessage getMovieTicketNum(@PathVariable Integer movieId) {
        logger.debug("enter into getMovieTicketNum movieId=" + movieId);
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            int MovieTicketNum = snapService.getMovieTicketNum(movieId);
            responseMessage.setData(MovieTicketNum);
        } catch (Exception e) {
            responseMessage.setStatus(false);
            responseMessage.setException(e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return responseMessage;
    }

    /**
     * 判断电影票抢购状态
     *
     * @param ticke 电影票对象
     * @return 返回状态字符串
     */
    private String getStatus(MovieTicket ticke) {
        Date date = new Date();
        if (ticke.getStartTime().after(date))
            return "未开始";
        else if (ticke.getEndTime().before(date))
            return "已结束";
        else
            return "正在抢购";
    }
}

