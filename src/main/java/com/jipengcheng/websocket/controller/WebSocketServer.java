package com.jipengcheng.websocket.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/api/chat/{nickname}")
@Component
@Scope(scopeName = "prototype")
public class WebSocketServer {
    public WebSocketServer(){
        System.out.println("服务端构造");
    }
    public static ConcurrentHashMap<String,WebSocketServer> map=new ConcurrentHashMap<>();
    private Session session;
    private String  nickname;
    @OnOpen
    public void open(Session session, @PathParam("nickname") String nickname) throws IOException {
        if (map.containsKey(nickname)){
            session.getBasicRemote().sendText("亲，昵称已存在");
            session.close();
        }else {
            System.out.println("连接："+nickname);
            this.session=session;
            this.nickname=nickname;
            map.put(nickname,this);
        }
    }
    @OnMessage
    public void message(String msg,Session session) throws IOException {
        System.out.println("接受消息："+msg);
        batchMsg(msg);
    }
    //错误信息
    @OnError
    public void error(Session session,Throwable throwable){
        System.out.println("崩了"+throwable.getMessage());
    }
    //接收 关闭连接
    @OnClose
    public void close(Session session) throws IOException {
        System.out.println("关闭");
        map.remove(nickname);
        //告诉别人 谁谁  下线了
        batchMsg(nickname+" 下线啦");
    }

    public void batchMsg(String msg) throws IOException {
        for (String k:map.keySet()){
            if (!k.equals(nickname)){
                map.get(k).session.getBasicRemote().sendText(nickname+"-说："+msg);
            }
        }
    }
}
