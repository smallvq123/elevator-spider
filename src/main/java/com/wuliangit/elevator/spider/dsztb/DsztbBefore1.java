package com.wuliangit.elevator.spider.dsztb;

import com.wuliangit.elevator.entity.Bid;
import com.wuliangit.elevator.service.BidService;
import com.wuliangit.elevator.spider.Common;
import com.wuliangit.elevator.util.SpringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/26.
 */
public class DsztbBefore1 implements PageProcessor{
    private Site site = Site.me().setRetryTimes(2).setSleepTime(2000);
    private static final String BASE_URL = "http://www.dsztb.gov.cn";
    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        if(page.getUrl().regex("http://www.dsztb.gov.cn/dsztb/InfoDetail/.*").match()){
            String title = html.xpath("//*[@id=\"tdTitle\"]/font[1]/b/text()").toString();
            System.out.println(title);
            BidService bidService = SpringUtils.getBean(BidService.class);
            if(!bidService.isExistByTitle(title) && Common.hasZhaoBiaoKeyword(title)){
                String all = html.xpath("//*[@id=\"tdTitle\"]/font[2]/text()").toString();
                String public_time = all.substring(all.indexOf("：") + 2, all.indexOf("】") - 1).replaceAll("/", "-");
                System.out.println(public_time);
                String content = html.xpath("//*[@id=\"TDContent\"]").toString();
                Bid bid = new Bid();
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = sf.parse(public_time);
                    date = java.sql.Date.valueOf(public_time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                System.out.println(date);
                bid.setPublicTime(date);
                bid.setTitle(title);
                bid.setContent(content);
                bid.setType(Common.BID_STATE_ZHAOBIAO);
                bid.setUrl(page.getUrl().toString());
                bidService.insertBid(bid);
            }


        }else{
            Selectable selectable = html.xpath("/html/body/div/div[2]/div/div[2]/div[2]/table/tbody/tr/td/table/tbody");
            Selectable links = selectable.links();
            List<String> requestList = links.all();

            for(String request: requestList){
                request = BASE_URL + request;
            }
            page.addTargetRequests(requestList);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static Request[] getRequest() {

        List<Request> requests = new ArrayList<Request>();

        for (int i = 1; i <= 4; i++) {

            Request request = new Request("http://www.dsztb.gov.cn/dsztb/gcjs/010008/?Paging=" + i);
            //设置get请求
            request.setMethod(HttpConstant.Method.GET);
            requests.add(request);
        }
        return requests.toArray(new Request[] {});
    }
}
